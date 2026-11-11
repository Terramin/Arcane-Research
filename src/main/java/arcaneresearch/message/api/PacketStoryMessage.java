package arcaneresearch.message.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketStoryMessage {

    private final UUID id;
    private final StoryMessage message;

    public PacketStoryMessage(UUID id, StoryMessage message) {
        this.id = id;
        this.message = Objects.requireNonNull(message, "message");
    }

    public UUID id() { return id; }
    public StoryMessage message() { return message; }

    public static void handle(PacketStoryMessage pkt, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> NetworkClientStoryMessageHandler.handle(pkt)));
        ctx.setPacketHandled(true);
    }

    public static void encode(PacketStoryMessage pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.id != null);
        if (pkt.id != null) buf.writeUUID(pkt.id);
        StoryMessage msg = pkt.message;
        buf.writeComponent(msg.title());
        buf.writeComponent(msg.text());
        buf.writeResourceLocation(msg.icon());
        buf.writeInt(msg.titleColor());
        buf.writeInt(msg.textColor());
        buf.writeInt(msg.durationTicks());
        buf.writeInt(msg.maxWaitTicks());
        buf.writeBoolean(msg.shake());
        buf.writeBoolean(msg.typewriter());
    }

    public static PacketStoryMessage decode(FriendlyByteBuf buf) {
        UUID id = buf.readBoolean() ? buf.readUUID() : null;
        StoryMessage msg = new StoryMessage(
                buf.readComponent(), buf.readComponent(), buf.readResourceLocation(),
                buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(),
                buf.readBoolean(), buf.readBoolean());
        return new PacketStoryMessage(id, msg);
    }
}