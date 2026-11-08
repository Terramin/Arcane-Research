package arcaneresearch.message.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketStoryMessageAck {

    private final UUID id;
    private final DeliveryStatus status;

    public PacketStoryMessageAck(UUID id, DeliveryStatus status) {
        this.id = Objects.requireNonNull(id, "id");
        this.status = Objects.requireNonNull(status, "status");
    }

    public UUID id() { return id; }
    public DeliveryStatus status() { return status; }

    public static void handle(PacketStoryMessageAck pkt, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> StoryTellingAPI.onAck(pkt.id, pkt.status));
        ctx.setPacketHandled(true);
    }

    public static void encode(PacketStoryMessageAck pkt, FriendlyByteBuf buf) {
        buf.writeUUID(pkt.id);
        buf.writeEnum(pkt.status);
    }

    public static PacketStoryMessageAck decode(FriendlyByteBuf buf) {
        return new PacketStoryMessageAck(buf.readUUID(), buf.readEnum(DeliveryStatus.class));
    }
}
