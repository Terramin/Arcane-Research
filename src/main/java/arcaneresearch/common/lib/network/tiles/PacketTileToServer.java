package arcaneresearch.common.lib.network.tiles;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.tiles.TileArcaneresearch;

// NEOFORGE_PORT: 1:1 from Forge .../network/tiles/PacketTileToServer.java (PLAY_TO_SERVER).
// Same transport adaptation + null-tag normalization as PacketTileToClient.
// Sender: NetworkEvent.Context.getSender() -> IPayloadContext.player().
// Payload id "arcaneresearch:tile_to_server".
public class PacketTileToServer implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketTileToServer> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "tile_to_server"));

    public static final StreamCodec<ByteBuf, PacketTileToServer> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.LONG, p -> p.pos,
            ByteBufCodecs.COMPOUND_TAG, p -> p.nbt,
            PacketTileToServer::new);

    private long pos;
    private CompoundTag nbt;

    public PacketTileToServer() {
        this.nbt = new CompoundTag();
    }

    public PacketTileToServer(BlockPos pos, CompoundTag nbt) {
        this.pos = pos.asLong();
        this.nbt = nbt != null ? nbt : new CompoundTag();
    }

    private PacketTileToServer(long pos, CompoundTag nbt) {
        this.pos = pos;
        this.nbt = nbt != null ? nbt : new CompoundTag();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketTileToServer msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if (!(player instanceof ServerPlayer serverPlayer)) return;

            Level world = serverPlayer.level();
            BlockPos blockPos = BlockPos.of(msg.pos);

            if (blockPos.distSqr(serverPlayer.blockPosition()) > 64) {
                return;
            }

            BlockEntity te = world.getBlockEntity(blockPos);
            if (te instanceof TileArcaneresearch arcaneresearchTile) {
                arcaneresearchTile.messageFromClient(msg.nbt != null ? msg.nbt : new CompoundTag(), serverPlayer);
            }
        });
    }
}
