package arcaneresearch.common.lib.network.fx;

import arcaneresearch.client.fx.FXDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Sparkle burst sent when a ward is created or lifted, as in TC4. */
public class PacketFXWardSparkle {
    private final BlockPos pos;
    private final int color;

    public PacketFXWardSparkle(BlockPos pos, int color) {
        this.pos = pos;
        this.color = color;
    }

    public static void encode(PacketFXWardSparkle packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeInt(packet.color);
    }

    public static PacketFXWardSparkle decode(FriendlyByteBuf buf) {
        return new PacketFXWardSparkle(buf.readBlockPos(), buf.readInt());
    }

    public static void handle(PacketFXWardSparkle packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(packet));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PacketFXWardSparkle packet) {
        FXDispatcher.INSTANCE.drawWardSparkle(packet.pos, packet.color);
    }
}
