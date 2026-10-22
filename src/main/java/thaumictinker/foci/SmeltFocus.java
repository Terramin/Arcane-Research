package thaumictinker.foci;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.FocusUpgradeType;
import arcaneresearch.api.wands.IWand;

 



 
public class SmeltFocus extends TtFocusBase {

    private static final AspectList COST = new AspectList().add(Aspect.FIRE, 45).add(Aspect.ENTROPY, 12);
    private static final double RANGE = 10.0;
    private static final int SOUND_COOLDOWN = 3;
    private static final Map<UUID, State> STATES = new HashMap<>();

    public SmeltFocus() {
        super("focussmelt", 0xFF0000);
    }

    public static final class State {
        public final BlockPos pos;
        public int progress;

        public State(BlockPos pos, int progress) {
            this.pos = pos;
            this.progress = progress;
        }
    }

    @Nullable
    public static State state(UUID player) {
        return STATES.get(player);
    }

    public static void clearState(UUID player) {
        STATES.remove(player);
    }

    public static void clearStates() {
        STATES.clear();
    }

    @Nullable
    public static BlockState smeltResult(ServerLevel level, BlockState state) {
        net.minecraft.world.item.Item item = state.getBlock().asItem();
        if (item == Items.AIR) {
            return null;
        }
        SimpleContainer container = new SimpleContainer(new ItemStack(item));
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, container, level);
        ItemStack result = recipe.map(r -> r.assemble(container, level.registryAccess())).orElse(ItemStack.EMPTY);
        if (result.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock().defaultBlockState();
        }
        return null;
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 0xFF0000;
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "SMELT";
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        return COST.copy();
    }

    @Nullable
    @Override
    public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return new FocusUpgradeType[]{FocusUpgradeType.treasure, FocusUpgradeType.potency};
    }

    @Override
    public boolean onFocusActivation(ItemStack wand, Level level, LivingEntity entity, @Nullable HitResult hit, int count) {
        if (!(entity instanceof Player player) || !(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        IWand wandItem = (IWand) wand.getItem();
        if (!wandItem.consumeAllVis(wand, player, COST.copy(), true, false)) {
            return false;
        }
        HitResult rawHit = player.pick(RANGE, 1.0F, false);
        if (!(rawHit instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        BlockPos pos = blockHit.getBlockPos();
        BlockState current = serverLevel.getBlockState(pos);
        BlockState result = smeltResult(serverLevel, current);
        BlockEntity world = serverLevel.getBlockEntity(pos);
        if (result == null || result == current || world != null) {
            return false;
        }
        State data = STATES.get(player.getUUID());
        boolean decremented = false;
        if (data != null && data.pos.equals(pos)) {
            data.progress--;
            decremented = true;
            if (data.progress <= 0) {
                if (serverLevel.mayInteract(player, pos)) {
                    serverLevel.setBlock(pos, result, Block.UPDATE_ALL);
                }
                STATES.remove(player.getUUID());
                decremented = false;
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 0.6F, 1.0F);
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.0F, 1.0F);
                FociEffects.send(player, FociEffects.SMELT_DONE, pos);
            }
        }
        if (!decremented) {
            int potency = wandItem.getFocusPotency(wand);
            STATES.put(player.getUUID(), new State(pos.immutable(), 20 - Math.min(3, potency) * 5));
        } else {
            if (count % SOUND_COOLDOWN == 0) {
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.2F, 1.0F);
            }
            FociEffects.send(player, FociEffects.SMELT_PROGRESS, pos);
        }
        FociEffects.send(player, FociEffects.SMELT_BEAM, pos);
        return true;
    }
}
