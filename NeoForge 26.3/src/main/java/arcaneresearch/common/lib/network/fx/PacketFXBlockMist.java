package arcaneresearch.common.lib.network.fx;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.fx.FXDispatcher;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// NEOFORGE_PORT: 1:1 from Forge .../network/fx/PacketFXBlockMist.java (PLAY_TO_CLIENT).
// Fields/ctors/handleClient logic preserved verbatim; transport -> CustomPacketPayload
// with manual StreamCodec (wire order identical to old encode/decode).
// Payload id "arcaneresearch:fx_block_mist".
public class PacketFXBlockMist implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketFXBlockMist> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "fx_block_mist"));

    public static final StreamCodec<ByteBuf, PacketFXBlockMist> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketFXBlockMist packet) {
            buf.writeLong(packet.loc);
            buf.writeInt(packet.color);
        }

        @Override
        public PacketFXBlockMist decode(ByteBuf buf) {
            long loc = buf.readLong();
            int color = buf.readInt();
            return new PacketFXBlockMist(loc, color);
        }
    };


    private final long loc;
    private final int color;

    public PacketFXBlockMist(BlockPos pos, int color) {
        this.loc = pos.asLong();
        this.color = color;
    }

    private PacketFXBlockMist(long loc, int color) {
        this.loc = loc;
        this.color = color;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXBlockMist msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleClient(msg));
    }

    private static void handleClient(PacketFXBlockMist packet) {
            BlockPos pos = BlockPos.of(packet.loc);
            FXDispatcher.INSTANCE.drawBlockMistParticles(pos, packet.color);
        }
}
