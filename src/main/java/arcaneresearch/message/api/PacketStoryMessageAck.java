package arcaneresearch.message.api;

import arcaneresearch.Arcaneresearch;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Objects;
import java.util.UUID;

/** Client-to-server payload acknowledging delivery of a story message. */
public class PacketStoryMessageAck implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketStoryMessageAck> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "story_message_ack"));

    public static final StreamCodec<ByteBuf, PacketStoryMessageAck> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketStoryMessageAck packet) {
            buf.writeLong(packet.id.getMostSignificantBits());
            buf.writeLong(packet.id.getLeastSignificantBits());
            buf.writeByte(packet.status.ordinal());
        }

        @Override
        public PacketStoryMessageAck decode(ByteBuf buf) {
            UUID id = new UUID(buf.readLong(), buf.readLong());
            DeliveryStatus[] values = DeliveryStatus.values();
            int ordinal = buf.readByte();
            DeliveryStatus status = values[Math.max(0, Math.min(values.length - 1, ordinal))];
            return new PacketStoryMessageAck(id, status);
        }
    };

    private final UUID id;
    private final DeliveryStatus status;

    public PacketStoryMessageAck(UUID id, DeliveryStatus status) {
        this.id = Objects.requireNonNull(id, "id");
        this.status = Objects.requireNonNull(status, "status");
    }

    public UUID id() {
        return id;
    }

    public DeliveryStatus status() {
        return status;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketStoryMessageAck pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> StoryTellingAPI.onAck(pkt.id, pkt.status));
    }
}
