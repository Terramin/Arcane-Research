package arcaneresearch.common.tiles.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.common.lib.utils.InventoryUtils;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModSounds;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Random;

public class TilePatternCrafter extends TileArcaneresearch {
    public byte type = 0;
    public float rot = 0;
    public float rp = 0;
    public int rotTicks = 0;

    private int count;
    private float power = 0.0f;
    private ItemStack outStack = ItemStack.EMPTY;
    private CraftingContainer craftMatrix;

    public TilePatternCrafter(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        this.count = new Random().nextInt(20);
        initCraftMatrix();
    }

    public TilePatternCrafter(BlockPos pos, BlockState state) {
        this(ModBlockEntities.PATTERN_CRAFTER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TilePatternCrafter tile) {
        tile.tickServer(level);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TilePatternCrafter tile) {
        tile.tickClient(level);
    }

    private void initCraftMatrix() {
        this.craftMatrix = new TransientCraftingContainer(new AbstractContainerMenu(null, -1) {
            @Override
            public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slot) {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean stillValid(@NotNull Player player) {
                return false;
            }
        }, 3, 3);
    }

    private void tickClient(Level level) {
        if (level == null) return;

        if (rotTicks > 0) {
            rotTicks--;

            int clackInterval = Math.max(1, (int) rp);

            if (rotTicks % clackInterval == 0) {
                level.playLocalSound(
                        worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                        ModSounds.CLACK.get(), SoundSource.BLOCKS, 0.2f, 1.7f, false
                );
            }

            rp++;
        } else {
            rp *= 0.8f;
        }

        rot += rp;
    }

    private void tickServer(Level level) {
        if (level == null) return;

        count++;

        if (count % 20 != 0 || !isEnabled(level)) return;

        if (power <= 0.0f) {
            power += AuraHelper.drainVis(level, worldPosition, 5.0f, false);
        }

        IItemHandler above = getItemHandler(level, worldPosition.above(), Direction.DOWN);
        IItemHandler below = getItemHandler(level, worldPosition.below(), Direction.UP);

        if (above == null || below == null) return;

        int amt = getPatternItemCount();

        for (int a = 0; a < above.getSlots(); a++) {
            ItemStack testStack = above.getStackInSlot(a).copy();

            if (testStack.isEmpty()) continue;

            testStack.setCount(amt);

            ItemStack required = testStack.copy();

            if (InventoryUtils.removeStackFrom(above, required, ArcaneresearchInvHelper.InvFilter.BASEORE, true).getCount() != amt) {
                continue;
            }

            if (!craft(level, testStack)) continue;
            if (power < 1.0f) continue;
            if (!ItemHandlerHelper.insertItem(below, outStack.copy(), true).isEmpty()) continue;

            boolean canInsertByproducts = true;

            for (int i = 0; i < 9; i++) {
                ItemStack byproduct = craftMatrix.getItem(i);

                if (!byproduct.isEmpty() && !ItemHandlerHelper.insertItem(below, byproduct.copy(), true).isEmpty()) {
                    canInsertByproducts = false;
                    break;
                }
            }

            if (!canInsertByproducts) continue;

            ItemHandlerHelper.insertItem(below, outStack.copy(), false);

            for (int i = 0; i < 9; i++) {
                ItemStack byproduct = craftMatrix.getItem(i);

                if (!byproduct.isEmpty()) {
                    ItemHandlerHelper.insertItem(below, byproduct.copy(), false);
                }
            }

            InventoryUtils.removeStackFrom(above, required, ArcaneresearchInvHelper.InvFilter.BASEORE, false);

            level.blockEvent(worldPosition, getBlockState().getBlock(), 1, 0);
            power--;
            break;
        }
    }

    private IItemHandler getItemHandler(Level level, BlockPos pos, Direction side) {
        if (level == null) return null;

        BlockEntity be = level.getBlockEntity(pos);

        if (be == null) return null;

        return be.getCapability(ForgeCapabilities.ITEM_HANDLER, side).orElse(null);
    }

    private int getPatternItemCount() {
        return switch (type) {
            case 1 -> 1;
            case 2, 3 -> 2;
            case 4 -> 4;
            case 5, 6 -> 3;
            case 7, 8 -> 6;
            case 9 -> 8;
            default -> 9;
        };
    }

    private boolean craft(Level level, ItemStack inStack) {
        if (level == null) return false;

        outStack = ItemStack.EMPTY;

        for (int i = 0; i < 9; i++) {
            craftMatrix.setItem(i, ItemStack.EMPTY);
        }

        ItemStack single = inStack.copyWithCount(1);

        switch (type) {
            case 0 -> {
                for (int a = 0; a < 9; a++) {
                    craftMatrix.setItem(a, single.copy());
                }
            }
            case 1 -> craftMatrix.setItem(0, single.copy());
            case 2 -> {
                craftMatrix.setItem(0, single.copy());
                craftMatrix.setItem(1, single.copy());
            }
            case 3 -> {
                craftMatrix.setItem(0, single.copy());
                craftMatrix.setItem(3, single.copy());
            }
            case 4 -> {
                craftMatrix.setItem(0, single.copy());
                craftMatrix.setItem(1, single.copy());
                craftMatrix.setItem(3, single.copy());
                craftMatrix.setItem(4, single.copy());
            }
            case 5 -> {
                for (int a = 0; a < 3; a++) {
                    craftMatrix.setItem(a, single.copy());
                }
            }
            case 6 -> {
                for (int a = 0; a < 3; a++) {
                    craftMatrix.setItem(a * 3, single.copy());
                }
            }
            case 7 -> {
                for (int a = 0; a < 6; a++) {
                    craftMatrix.setItem(a, single.copy());
                }
            }
            case 8 -> {
                for (int a = 0; a < 2; a++) {
                    for (int b = 0; b < 3; b++) {
                        craftMatrix.setItem(a + b * 3, single.copy());
                    }
                }
            }
            case 9 -> {
                for (int a = 0; a < 9; a++) {
                    if (a != 4) {
                        craftMatrix.setItem(a, single.copy());
                    }
                }
            }
        }

        Optional<CraftingRecipe> recipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, craftMatrix, level);

        if (recipe.isEmpty()) return false;

        CraftingRecipe craftingRecipe = recipe.get();

        outStack = craftingRecipe.assemble(craftMatrix, level.registryAccess());

        if (outStack.isEmpty()) return false;

        NonNullList<ItemStack> remaining = craftingRecipe.getRemainingItems(craftMatrix);

        for (int i = 0; i < remaining.size(); i++) {
            ItemStack original = craftMatrix.getItem(i);
            ItemStack leftover = remaining.get(i);

            if (!original.isEmpty()) {
                craftMatrix.setItem(i, ItemStack.EMPTY);
            }

            if (!leftover.isEmpty()) {
                craftMatrix.setItem(i, leftover);
            }
        }

        return true;
    }

    public void cycle() {
        type++;

        if (type > 9) {
            type = 0;
        }

        syncTile(false);
        setChanged();
    }

    private boolean isEnabled(Level level) {
        if (level == null) return false;

        BlockState state = getBlockState();

        if (state.hasProperty(BlockStateProperties.ENABLED)) {
            return state.getValue(BlockStateProperties.ENABLED);
        }

        return !level.hasNeighborSignal(worldPosition);
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == 1) {
            if (level != null && level.isClientSide) {
                rotTicks = 10;
            }
            return true;
        }

        return super.triggerEvent(id, param);
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        tag.putByte("type", type);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);

        byte t = tag.getByte("type");

        if (t < 0) t = 0;
        if (t > 9) t = 9;

        type = t;
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putFloat("power", power);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        power = tag.getFloat("power");
    }
}