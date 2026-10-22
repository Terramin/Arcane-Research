package thaumictinker.foci;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.FocusUpgradeType;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.init.ModSounds;

 




 
public class DislocationFocus extends TtFocusBase {

    private static final AspectList COST = new AspectList().add(Aspect.ENTROPY, 500).add(Aspect.ORDER, 500).add(Aspect.EARTH, 100);
    private static final AspectList EXTRA_TILE = new AspectList().add(Aspect.ENTROPY, 2000).add(Aspect.ORDER, 2000).add(Aspect.EARTH, 400);
    private static final AspectList EXTRA_SPAWNER = new AspectList().add(Aspect.ENTROPY, 9500).add(Aspect.ORDER, 9500).add(Aspect.EARTH, 4900);

    private static final String HELD_TAG = "DislocatedBlock";

    public DislocationFocus() {
        super("focusdislocation", 0xFFB200);
    }

    private static AspectList extra(BlockEntity blockEntity) {
        if (blockEntity instanceof SpawnerBlockEntity) {
            return EXTRA_SPAWNER.copy();
        }
        if (blockEntity != null) {
            return EXTRA_TILE.copy();
        }
        return new AspectList();
    }

    public static boolean isBlacklisted(Level level, BlockPos pos, BlockState state) {
        return state.isAir()
                || state.is(Blocks.PISTON_HEAD)
                || state.is(Blocks.MOVING_PISTON)
                || state.getDestroySpeed(level, pos) == -1.0F;
    }

    @Nullable
    public static DislocatedBlock held(ItemStack focus) {
        if (focus.isEmpty() || !focus.hasTag() || !focus.getTag().contains(HELD_TAG)) {
            return null;
        }
        return DislocatedBlock.load(focus.getTag().getCompound(HELD_TAG));
    }

    private static void setHeld(ItemStack focus, @Nullable DislocatedBlock held) {
        if (held == null) {
            if (focus.hasTag()) {
                focus.getTag().remove(HELD_TAG);
            }
        } else {
            focus.getOrCreateTag().put(HELD_TAG, held.save());
        }
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 0xFFB200;
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        DislocatedBlock current = held(focus);
        return current == null ? "DISLOCATION" : "DISLOCATION0";
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
    public boolean onFocusActivation(ItemStack wand, Level level, LivingEntity entity, @Nullable HitResult hit, int useCount) {
        if (!(entity instanceof Player player)) {
            return false;
        }
        if (!(hit instanceof BlockHitResult blockHit) || !(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        BlockPos clicked = blockHit.getBlockPos();
        Direction face = blockHit.getDirection();
        if (!player.mayUseItemAt(clicked, face, wand) || !serverLevel.mayInteract(player, clicked)) {
            return false;
        }
        IWand wandItem = (IWand) wand.getItem();
        ItemStack focus = wandItem.getFocusStack(wand);
        DislocatedBlock current = held(focus);
        boolean done;
        if (current != null) {
            done = place(serverLevel, player, wand, focus, current, clicked, face);
        } else {
            done = pickUp(serverLevel, player, wand, focus, clicked);
        }
        if (done) {
            wandItem.setFocus(wand, focus);
        }
        return done;
    }

    public static boolean place(ServerLevel level, Player player, ItemStack wand, ItemStack focus, DislocatedBlock held, BlockPos clicked, Direction face) {
        BlockPos target = clicked.relative(face);
        BlockState state = held.state();
        if (!level.isInWorldBounds(target)
                || !level.getBlockState(target).canBeReplaced()
                || !player.mayUseItemAt(target, face, wand)
                || !level.mayInteract(player, target)
                || !state.canSurvive(level, target)) {
            return false;
        }
        if (!level.setBlock(target, state, Block.UPDATE_ALL)) {
            return false;
        }
        if (!held.tile().isEmpty()) {
            CompoundTag tag = held.tile().copy();
            tag.putInt("x", target.getX());
            tag.putInt("y", target.getY());
            tag.putInt("z", target.getZ());
            BlockEntity blockEntity = BlockEntity.loadStatic(target, state, tag);
            if (blockEntity != null) {
                level.setBlockEntity(blockEntity);
                level.sendBlockUpdated(target, state, state, Block.UPDATE_ALL);
            }
        }
        setHeld(focus, null);
        FociEffects.send(player, FociEffects.PLACE_BURST, target);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.WAND.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
        return true;
    }

    public static boolean pickUp(ServerLevel level, Player player, ItemStack wand, ItemStack focus, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (isBlacklisted(level, pos, state)) {
            return false;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        IWand wandItem = (IWand) wand.getItem();
        AspectList extra = extra(blockEntity);
        if (!wandItem.consumeAllVis(wand, player, extra, !level.isClientSide, false)) {
            return false;
        }
        CompoundTag tile = blockEntity == null ? new CompoundTag() : blockEntity.saveWithFullMetadata();
        setHeld(focus, new DislocatedBlock(state, tile));
        if (blockEntity != null) {
            level.removeBlockEntity(pos);
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        FociEffects.send(player, FociEffects.PICK_BURST, pos);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), state.getSoundType().getBreakSound(), SoundSource.PLAYERS, 1.0F, 1.0F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.WAND.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
        return true;
    }
}
