package arcaneresearch.discovered.scan;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Сканирование: пакет награды за изучение")
class MsgStudyRewardTest {

    private static byte[] bytes(ByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), bytes);
        return bytes;
    }

    @Test
    void emptyRewardEncodesAsZeroEntries() {
        ByteBuf buffer = Unpooled.buffer();
        MsgStudyReward.STREAM_CODEC.encode(buffer, new MsgStudyReward());

        assertEquals(1, buffer.readableBytes());
        assertEquals(0, buffer.getByte(0));

        byte[] original = bytes(buffer);
        MsgStudyReward reward = MsgStudyReward.STREAM_CODEC.decode(buffer);
        ByteBuf again = Unpooled.buffer();
        MsgStudyReward.STREAM_CODEC.encode(again, reward);
        assertArrayEquals(original, bytes(again));
    }

    @Test
    void craftedRewardRoundTripsExactly() {
        byte[] bytes = bytes(Unpooled.buffer()
                .writeByte(2)
                .writeByte(3).writeBytes("aer".getBytes(java.nio.charset.StandardCharsets.UTF_8))
                .writeByte(5)
                .writeByte(5).writeBytes("ignis".getBytes(java.nio.charset.StandardCharsets.UTF_8))
                .writeByte(12));

        ByteBuf decoded = Unpooled.wrappedBuffer(bytes);
        MsgStudyReward reward = MsgStudyReward.STREAM_CODEC.decode(decoded);

        ByteBuf reencoded = Unpooled.buffer();
        MsgStudyReward.STREAM_CODEC.encode(reencoded, reward);
        assertArrayEquals(bytes, bytes(reencoded));
    }

    @Test
    void excessiveEntryCountIsClamped() {
        ByteBuf buffer = Unpooled.buffer();
        net.minecraft.network.codec.ByteBufCodecs.VAR_INT.encode(buffer, 5000);
        MsgStudyReward reward = MsgStudyReward.STREAM_CODEC.decode(buffer);
        ByteBuf reencoded = Unpooled.buffer();
        MsgStudyReward.STREAM_CODEC.encode(reencoded, reward);

        assertTrue(reencoded.readableBytes() > 0);
        assertEquals(0, reencoded.readByte());
    }
}
