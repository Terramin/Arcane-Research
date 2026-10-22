package arcaneresearch.common.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.tiles.PacketTileToClient;

import javax.annotation.Nullable;

public abstract class TileArcaneresearch extends BlockEntity {

    public TileArcaneresearch(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    
    
    
    
    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        writeSyncNBT(out);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        readSyncNBT(in);
    }

    protected void writeSyncNBT(ValueOutput out) {

    }

    protected void readSyncNBT(ValueInput in) {

    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        writeSyncNBT(out);
        tag.merge(out.buildResult());
        return tag;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState newState) {
        
        
        
        
        super.preRemoveSideEffects(pos, newState);
        
        
        
        
        
    }

    
    
    
    
    protected static boolean hasBoolean(net.minecraft.world.level.storage.ValueInput in, String key) {
        return in.read(key, com.mojang.serialization.Codec.BOOL).isPresent();
    }

    protected static byte[] readBytes(net.minecraft.world.level.storage.ValueInput in, String key) {
        return in.read(key, com.mojang.serialization.Codec.BYTE_BUFFER).map(bb -> {
            byte[] a = new byte[bb.remaining()];
            bb.get(a);
            return a;
        }).orElse(new byte[0]);
    }

    protected static void writeBytes(ValueOutput out, String key, byte[] data) {
        out.store(key, com.mojang.serialization.Codec.BYTE_BUFFER, java.nio.ByteBuffer.wrap(data));
    }

    protected static java.util.List<CompoundTag> readTagList(
            net.minecraft.world.level.storage.ValueInput in, String key) {
        return in.read(key, CompoundTag.CODEC.listOf()).orElse(java.util.List.of());
    }

    protected static void writeTagList(ValueOutput out, String key, java.util.List<CompoundTag> entries) {
        out.store(key, CompoundTag.CODEC.listOf(), entries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void syncTile(boolean rerender) {
        if (level != null && !level.isClientSide()) {
            setChanged();
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, rerender ? 3 : 2);
        }
    }

    public void markDirtyAndSync() {
        setChanged();
        syncTile(false);
    }

    public void messageFromServer(CompoundTag nbt) {

    }

    public void messageFromClient(CompoundTag nbt, ServerPlayer player) {

    }

    public void sendMessageToClients(CompoundTag nbt) {
        if (level != null && !level.isClientSide()) {
            PacketHandler.sendToAllTracking(new PacketTileToClient(worldPosition, nbt), this);
        }
    }

    public void sendMessageToServer(CompoundTag nbt) {
        if (level != null && level.isClientSide()) {
            PacketHandler.sendToServer(new arcaneresearch.common.lib.network.tiles.PacketTileToServer(worldPosition, nbt));
        }
    }
}
