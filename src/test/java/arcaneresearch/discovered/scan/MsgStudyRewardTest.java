package arcaneresearch.discovered.scan;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Сканирование: пакет награды за изучение")
class MsgStudyRewardTest {

    @Test
    void emptyRewardEncodesAsZeroEntries() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        new MsgStudyReward().encode(buffer);

        assertEquals(1, buffer.readableBytes());
        assertEquals(0, buffer.getByte(0));

        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), bytes);
        MsgStudyReward reward = new MsgStudyReward(new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes)));
        FriendlyByteBuf again = new FriendlyByteBuf(Unpooled.buffer());
        reward.encode(again);
        assertEquals(bytes.length, again.readableBytes());
    }

    @Test
    void craftedRewardRoundTripsExactly() {
        FriendlyByteBuf source = new FriendlyByteBuf(Unpooled.buffer());
        source.writeVarInt(2);
        source.writeUtf("aer", 64);
        source.writeVarInt(5);
        source.writeUtf("ignis", 64);
        source.writeVarInt(12);

        byte[] bytes = new byte[source.readableBytes()];
        source.getBytes(source.readerIndex(), bytes);

        FriendlyByteBuf decoded = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
        MsgStudyReward reward = new MsgStudyReward(decoded);

        FriendlyByteBuf reencoded = new FriendlyByteBuf(Unpooled.buffer());
        reward.encode(reencoded);
        byte[] again = new byte[reencoded.readableBytes()];
        reencoded.getBytes(reencoded.readerIndex(), again);

        assertArrayEquals(bytes, again);
    }

    @Test
    void excessiveEntryCountIsClamped() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeVarInt(5000);

        MsgStudyReward reward = new MsgStudyReward(buffer);
        FriendlyByteBuf reencoded = new FriendlyByteBuf(Unpooled.buffer());
        reward.encode(reencoded);

        assertTrue(reencoded.readableBytes() > 0);
        assertEquals(0, reencoded.readVarInt());
    }
}
