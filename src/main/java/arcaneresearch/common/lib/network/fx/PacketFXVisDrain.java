package arcaneresearch.common.lib.network.fx;

import arcaneresearch.client.fx.FXDispatcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Thaumcraft's PacketFXVisDrain. Spawns a single flying vis sparkle from the
 * consuming block toward the supplying vis node.
 */
public class PacketFXVisDrain {
    private final int sourceX, sourceY, sourceZ;
    private final int targetX, targetY, targetZ;
    private final int color;

    public PacketFXVisDrain(int sourceX, int sourceY, int sourceZ,
                            int targetX, int targetY, int targetZ, int color) {
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.sourceZ = sourceZ;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        this.color = color;
    }

    public static void encode(PacketFXVisDrain packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.sourceX);
        buffer.writeInt(packet.sourceY);
        buffer.writeInt(packet.sourceZ);
        buffer.writeInt(packet.targetX);
        buffer.writeInt(packet.targetY);
        buffer.writeInt(packet.targetZ);
        buffer.writeInt(packet.color);
    }

    public static PacketFXVisDrain decode(FriendlyByteBuf buffer) {
        return new PacketFXVisDrain(
                buffer.readInt(), buffer.readInt(), buffer.readInt(),
                buffer.readInt(), buffer.readInt(), buffer.readInt(),
                buffer.readInt());
    }

    public static void handle(PacketFXVisDrain packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(packet));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PacketFXVisDrain packet) {
        FXDispatcher.INSTANCE.visDrainSparkle(
                packet.targetX, packet.targetY, packet.targetZ,
                packet.sourceX, packet.sourceY, packet.sourceZ,
                packet.color);
    }
}
