package arcaneresearch.common.lib.network.fx;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.lib.events.PortalWarpFx;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Clientbound FX packet that starts the portal warp screen effect. */
public class PacketFXTeleport implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketFXTeleport> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "fx_teleport"));

    public static final StreamCodec<ByteBuf, PacketFXTeleport> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketFXTeleport packet) {
        }

        @Override
        public PacketFXTeleport decode(ByteBuf buf) {
            return new PacketFXTeleport();
        }
    };

    public PacketFXTeleport() {
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXTeleport packet, IPayloadContext ctx) {
        ctx.enqueueWork(PacketFXTeleport::handleClient);
    }

    private static void handleClient() {
        PortalWarpFx.start();
    }
}
