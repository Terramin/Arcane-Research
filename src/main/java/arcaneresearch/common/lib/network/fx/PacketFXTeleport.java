package arcaneresearch.common.lib.network.fx;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.client.lib.events.PortalWarpFx;

import java.util.function.Supplier;

public class PacketFXTeleport {

    public PacketFXTeleport() {
    }

    public static void encode(PacketFXTeleport packet, FriendlyByteBuf buffer) {
    }

    public static PacketFXTeleport decode(FriendlyByteBuf buffer) {
        return new PacketFXTeleport();
    }

    public static void handle(PacketFXTeleport packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(PacketFXTeleport::handleClient);
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient() {
        PortalWarpFx.start();
    }
}
