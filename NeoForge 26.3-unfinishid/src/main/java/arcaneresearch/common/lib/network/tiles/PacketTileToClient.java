package arcaneresearch.common.lib.network.tiles;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.tiles.TileArcaneresearch;

// NEOFORGE_PORT: 1:1 from Forge .../network/tiles/PacketTileToClient.java (PLAY_TO_CLIENT).
// Fields/ctors/handle logic preserved verbatim; transport -> CustomPacketPayload
// (same pattern as PacketSyncKnowledge; see its javadoc).
// Null-tag normalization added in ctors (old FriendlyByteBuf.writeNbt was null-safe;
// ByteBufCodecs.COMPOUND_TAG expects non-null).
// Payload id "arcaneresearch:tile_to_client".
public class PacketTileToClient implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketTileToClient> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "tile_to_client"));

    public static final StreamCodec<ByteBuf, PacketTileToClient> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.LONG, p -> p.pos,
            ByteBufCodecs.COMPOUND_TAG, p -> p.nbt,
            PacketTileToClient::new);

    private long pos;
    private CompoundTag nbt;

    public PacketTileToClient() {
        this.nbt = new CompoundTag();
    }

    public PacketTileToClient(BlockPos pos, CompoundTag nbt) {
        this.pos = pos.asLong();
        this.nbt = nbt != null ? nbt : new CompoundTag();
    }

    private PacketTileToClient(long pos, CompoundTag nbt) {
        this.pos = pos;
        this.nbt = nbt != null ? nbt : new CompoundTag();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketTileToClient msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleOnClient(msg, ctx.player()));
    }

    private static void handleOnClient(PacketTileToClient msg, Player player) {
        if (player == null) return;
        Level world = player.level();
        if (world == null) return;

        BlockPos blockPos = BlockPos.of(msg.pos);
        BlockEntity te = world.getBlockEntity(blockPos);

        if (te instanceof TileArcaneresearch arcaneresearchTile) {
            arcaneresearchTile.messageFromServer(msg.nbt != null ? msg.nbt : new CompoundTag());
        }
    }
}