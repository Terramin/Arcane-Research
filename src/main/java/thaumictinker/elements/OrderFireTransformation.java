package thaumictinker.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

 



 
public final class OrderFireTransformation implements FireTransformation {

    private static final AbstractContainerMenu DUMMY_MENU = new AbstractContainerMenu(MenuType.GENERIC_9x3, 0) {
        @Override
        public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player player, int index) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(net.minecraft.world.entity.player.Player player) {
            return false;
        }
    };

    private final Map<Block, Optional<Block>> results = new ConcurrentHashMap<>();

    public OrderFireTransformation() {
        reset();
    }

    public void reset() {
        results.clear();
        for (Block block : new Block[]{Blocks.STONE, Blocks.GLASS, Blocks.SAND, Blocks.RED_SAND, Blocks.GRAVEL}) {
            results.put(block, Optional.empty());
        }
    }

    @Override
    @Nullable
    public BlockState result(LevelReader level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        Block block = state.getBlock();
        Optional<Block> result = results.get(block);
        if (result == null) {
            result = Optional.ofNullable(compute(serverLevel, pos, state));
            results.put(block, result);
        }
        return result.map(Block::defaultBlockState).orElse(null);
    }

    @Override
    public int decayChance(LevelReader level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return 1;
        }
        int dropSize = drops(serverLevel, pos, state).size();
        if (dropSize == 2) {
            return 2;
        }
        if (dropSize >= 3) {
            return 1;
        }
        return 3;
    }

    private static List<ItemStack> drops(ServerLevel level, BlockPos pos, BlockState state) {
        return Block.getDrops(state, level, pos, level.getBlockEntity(pos));
    }

    private static @Nullable Block compute(ServerLevel level, BlockPos pos, BlockState state) {
        List<ItemStack> drops = drops(level, pos, state);
        if (drops.isEmpty() || !allEqual(drops)) {
            return null;
        }
        ItemStack stack = drops.get(0);
        ItemStack blockStack = craft(level, stack);
        if (blockStack.isEmpty()) {
            ItemStack ingot = smelt(level, stack);
            if (!ingot.isEmpty()) {
                blockStack = craft(level, ingot);
            }
        }
        if (blockStack.getItem() instanceof BlockItem item && item.getBlock() != Blocks.AIR) {
            return item.getBlock();
        }
        return null;
    }

    private static boolean allEqual(List<ItemStack> stacks) {
        for (ItemStack element : stacks) {
            if (!ItemStack.isSameItem(element, stacks.get(0))
                    || !java.util.Objects.equals(element.getTag(), stacks.get(0).getTag())) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("null")
    private static ItemStack craft(ServerLevel level, ItemStack stack) {
        TransientCraftingContainer container = new TransientCraftingContainer(DUMMY_MENU, 3, 3);
        for (int index = 0; index < 9; index++) {
            container.setItem(index, stack.copyWithCount(1));
        }
        return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, container, level)
                .map(recipe -> recipe.assemble(container, level.registryAccess()))
                .orElse(ItemStack.EMPTY);
    }

    @SuppressWarnings("null")
    private static ItemStack smelt(ServerLevel level, ItemStack stack) {
        SimpleContainer container = new SimpleContainer(1);
        container.setItem(0, stack.copyWithCount(1));
        return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, container, level)
                .map(recipe -> recipe.assemble(container, level.registryAccess()))
                .orElse(ItemStack.EMPTY);
    }
}
