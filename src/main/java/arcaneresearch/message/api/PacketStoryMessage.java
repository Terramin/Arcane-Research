package arcaneresearch.message.api;

import arcaneresearch.Arcaneresearch;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Objects;
import java.util.UUID;

/** Server-to-client payload carrying one story message. */
public class PacketStoryMessage implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketStoryMessage> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "story_message"));

    public static final StreamCodec<ByteBuf, PacketStoryMessage> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketStoryMessage packet) {
            buf.writeBoolean(packet.id != null);
            if (packet.id != null) {
                writeUuid(buf, packet.id);
            }
            StoryMessage msg = packet.message;
            ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.encode(buf, msg.title());
            ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.encode(buf, msg.text());
            Identifier.STREAM_CODEC.encode(buf, msg.icon());
            buf.writeInt(msg.titleColor());
            buf.writeInt(msg.textColor());
            buf.writeInt(msg.durationTicks());
            buf.writeInt(msg.maxWaitTicks());
            buf.writeBoolean(msg.shake());
            buf.writeBoolean(msg.typewriter());
        }

        @Override
        public PacketStoryMessage decode(ByteBuf buf) {
            UUID id = buf.readBoolean() ? readUuid(buf) : null;
            StoryMessage msg = new StoryMessage(
                    ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.decode(buf),
                    ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.decode(buf),
                    Identifier.STREAM_CODEC.decode(buf),
                    buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(),
                    buf.readBoolean(), buf.readBoolean());
            return new PacketStoryMessage(id, msg);
        }
    };

    private final UUID id;
    private final StoryMessage message;

    public PacketStoryMessage(UUID id, StoryMessage message) {
        this.id = id;
        this.message = Objects.requireNonNull(message, "message");
    }

    public UUID id() {
        return id;
    }

    public StoryMessage message() {
        return message;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketStoryMessage pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> NetworkClientStoryMessageHandler.handle(pkt));
    }

    private static void writeUuid(ByteBuf buf, UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    private static UUID readUuid(ByteBuf buf) {
        return new UUID(buf.readLong(), buf.readLong());
    }
}
