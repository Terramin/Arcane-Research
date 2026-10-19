package arcaneresearch.common.tiles.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModSounds;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileTubeValve extends TileTube {

    public boolean allowFlow = true;
    public float rotation = 0.0f;
    private boolean wasPoweredLastTick = false;

    public TileTubeValve(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileTubeValve(BlockPos pos, BlockState state) {
        this(ModBlockEntities.TUBE_VALVE.get(), pos, state);
    }

    public static void serverTickValve(Level level, BlockPos pos, BlockState state, TileTubeValve tile) {

        if (tile.count % 5 == 0) {
            boolean gettingPower = tile.gettingPower();

            if (!tile.wasPoweredLastTick && gettingPower && tile.allowFlow) {
                tile.allowFlow = false;
                level.playSound(null, pos, ModSounds.SQUEEK.get(), SoundSource.BLOCKS,
                        0.7f, 0.9f + level.getRandom().nextFloat() * 0.2f);
                tile.markDirtyAndSync();
            }

            if (tile.wasPoweredLastTick && !gettingPower && !tile.allowFlow) {
                tile.allowFlow = true;
                level.playSound(null, pos, ModSounds.SQUEEK.get(), SoundSource.BLOCKS,
                        0.7f, 0.9f + level.getRandom().nextFloat() * 0.2f);
                tile.markDirtyAndSync();
            }

            tile.wasPoweredLastTick = gettingPower;
        }

        TileTube.serverTick(level, pos, state, tile);
    }

    public static void clientTickValve(Level level, BlockPos pos, BlockState state, TileTubeValve tile) {

        if (!tile.allowFlow && tile.rotation < 360.0f) {
            tile.rotation += 20.0f;
        } else if (tile.allowFlow && tile.rotation > 0.0f) {
            tile.rotation -= 20.0f;
        }

        TileTube.clientTick(level, pos, state, tile);
    }

    @Override
    protected void writeSyncNBT(ValueOutput tag) {
        super.writeSyncNBT(tag);
        tag.putBoolean("Flow", allowFlow);
        tag.putBoolean("HadPower", wasPoweredLastTick);
    }

    @Override
    protected void readSyncNBT(ValueInput tag) {
        super.readSyncNBT(tag);

        allowFlow = hasBoolean(tag, "Flow") ? tag.getBooleanOr("Flow", false) : tag.getBooleanOr("flow", false);
        wasPoweredLastTick = hasBoolean(tag, "HadPower") ? tag.getBooleanOr("HadPower", false) : tag.getBooleanOr("hadpower", false);
    }

    public boolean gettingPower() {

        return level != null && level.getBestNeighborSignal(worldPosition) > 0;
    }

    @Override
    public boolean onCasterRightClick(Level level, ItemStack casterStack, Player player,
                                      BlockPos pos, Direction side, InteractionHand hand) {
        if (player.isShiftKeyDown()) {

            level.playSound(player, worldPosition, ModSounds.TOOL.get(), SoundSource.BLOCKS, 0.5f,
                    0.9f + level.getRandom().nextFloat() * 0.2f);
            player.swing(hand, net.minecraft.world.item.component.SwingAnimation.DEFAULT, false);
            markDirtyAndSync();
            for (int i = 1; i < 20; i++) {
                Direction candidate = Direction.values()[(facing.ordinal() + i) % 6];
                if (!canConnectSide(candidate)) {
                    facing = candidate;
                    markDirtyAndSync();
                    syncConnectionState();
                    break;
                }
            }
            return true;
        }
        return super.onCasterRightClick(level, casterStack, player, pos, side, hand);
    }

    @Override
    public boolean isConnectable(Direction face) {

        return face != facing && super.isConnectable(face);
    }

    @Override
    public void setSuction(Aspect aspect, int amount) {

        if (allowFlow) {
            super.setSuction(aspect, amount);
        }
    }

    public boolean isOpen() {
        return allowFlow;
    }

    public void setOpen(boolean open) {
        this.allowFlow = open;
        markDirtyAndSync();
    }
}
