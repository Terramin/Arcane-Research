package arcaneresearch.common.tiles;

import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.tiles.PacketTileToClient;
import arcaneresearch.common.lib.network.tiles.PacketTileToServer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public abstract class TileArcaneresearch extends BlockEntity {

    public TileArcaneresearch(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        writeSyncNBT(tag);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        readSyncNBT(tag);
    }

    protected void writeSyncNBT(CompoundTag tag) {
    }

    protected void readSyncNBT(CompoundTag tag) {
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        writeSyncNBT(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void syncTile(boolean rerender) {
        if (level != null && !level.isClientSide) {
            setChanged();
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, rerender ? 3 : 2);
        }
    }

    public void markDirtyAndSync() {
        if (level != null && !level.isClientSide) {
            syncTile(false);
        } else {
            setChanged();
        }
    }

    public void messageFromServer(CompoundTag nbt) {
    }

    public void messageFromClient(CompoundTag nbt, ServerPlayer player) {
    }

    public void sendMessageToClients(CompoundTag nbt) {
        if (level != null && !level.isClientSide) {
            PacketHandler.sendToAllTracking(new PacketTileToClient(worldPosition, nbt), this);
        }
    }

    public void sendMessageToServer(CompoundTag nbt) {
        if (level != null && level.isClientSide) {
            PacketHandler.sendToServer(new PacketTileToServer(worldPosition, nbt));
        }
    }
}