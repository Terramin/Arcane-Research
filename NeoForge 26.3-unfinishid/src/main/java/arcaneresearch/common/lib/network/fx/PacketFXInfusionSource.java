package arcaneresearch.common.lib.network.fx;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.tiles.crafting.TileInfusionMatrix;
import arcaneresearch.common.tiles.crafting.TilePedestal;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// NEOFORGE_PORT: 1:1 from Forge .../network/fx/PacketFXInfusionSource.java (PLAY_TO_CLIENT).
// Fields/ctors/handleClient logic preserved verbatim; transport -> CustomPacketPayload
// with manual StreamCodec (wire order identical to old encode/decode).
// Payload id "arcaneresearch:fx_infusion_source".
public class PacketFXInfusionSource implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketFXInfusionSource> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "fx_infusion_source"));

    public static final StreamCodec<ByteBuf, PacketFXInfusionSource> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketFXInfusionSource packet) {
            buf.writeLong(packet.p1);
            buf.writeLong(packet.p2);
            buf.writeInt(packet.color);
        }

        @Override
        public PacketFXInfusionSource decode(ByteBuf buf) {
            long p1 = buf.readLong();
            long p2 = buf.readLong();
            int color = buf.readInt();
            return new PacketFXInfusionSource(p1, p2, color);
        }
    };


    private final long p1;
    private final long p2;
    private final int color;

    public PacketFXInfusionSource(BlockPos matrixPos, BlockPos sourcePos, int color) {
        this.p1 = matrixPos.asLong();
        this.p2 = sourcePos.asLong();
        this.color = color;
    }

    private PacketFXInfusionSource(long p1, long p2, int color) {
        this.p1 = p1;
        this.p2 = p2;
        this.color = color;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXInfusionSource msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleClient(msg, ctx.player()));
    }

    private static void handleClient(PacketFXInfusionSource packet, Player player) {
            if (player == null) return;
            Level level = player.level();
            if (level == null) return;
    
            BlockPos matrixPos = BlockPos.of(packet.p1);
            BlockPos sourcePos = BlockPos.of(packet.p2);
    
            String key = sourcePos.getX() + ":" + sourcePos.getY() + ":" + sourcePos.getZ() + ":" + packet.color;
    
            BlockEntity tile = level.getBlockEntity(matrixPos);
            if (tile instanceof TileInfusionMatrix matrix) {
    
                int count = 15;
                BlockEntity sourceTile = level.getBlockEntity(sourcePos);
                if (sourceTile instanceof TilePedestal) {
                    count = 120;
                }
    
                if (matrix.sourceFX.containsKey(key)) {
                    TileInfusionMatrix.SourceFX sf = matrix.sourceFX.get(key);
                    sf.ticks = count;
                    matrix.sourceFX.put(key, sf);
                } else {
                    matrix.sourceFX.put(key, new TileInfusionMatrix.SourceFX(sourcePos, count, packet.color));
                }
            }
        }
}
