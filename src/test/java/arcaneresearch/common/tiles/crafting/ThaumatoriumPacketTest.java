package arcaneresearch.common.tiles.crafting;

import arcaneresearch.common.lib.network.misc.PacketSelectThaumotoriumRecipeToServer;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Alchemy furnace: recipe-selection packet robustness")
class ThaumatoriumPacketTest {

    private static byte[] encode(BlockPos pos, int hash) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PacketSelectThaumotoriumRecipeToServer.encode(
                new PacketSelectThaumotoriumRecipeToServer(null, pos, hash), buf);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), bytes);
        return bytes;
    }

    private static Object decode(byte[] bytes) throws Exception {
        Method decode = PacketSelectThaumotoriumRecipeToServer.class
                .getDeclaredMethod("decode", FriendlyByteBuf.class);
        decode.setAccessible(true);
        return decode.invoke(null, new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes)));
    }

    @Test
    @DisplayName("extreme positions and hashes survive encode/decode unchanged")
    void extremeValuesRoundTrip() throws Exception {
        BlockPos[] positions = {BlockPos.ZERO, new BlockPos(30000000, 300, -30000000)};
        int[] hashes = {0, 1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE};

        for (BlockPos pos : positions) {
            for (int hash : hashes) {
                byte[] bytes = encode(pos, hash);
                assertEquals(12, bytes.length, "long pos + int hash");
                assertNotNull(decode(bytes));
                assertArrayEquals(bytes, reencode(decode(bytes)), "unstable encoding for " + hash);
            }
        }
    }

    private static byte[] reencode(Object packet) throws Exception {
        Method encode = PacketSelectThaumotoriumRecipeToServer.class
                .getMethod("encode", PacketSelectThaumotoriumRecipeToServer.class, FriendlyByteBuf.class);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        encode.invoke(null, packet, buf);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), bytes);
        return bytes;
    }

    @Test
    @DisplayName("truncated payloads are rejected instead of silently misread")
    void truncatedPayload() {
        byte[] full = encode(BlockPos.ZERO, 42);
        for (int length = 0; length < 12; length++) {
            byte[] truncated = Arrays.copyOf(full, length);
            assertThrows(Exception.class, () -> decode(truncated), "length=" + length);
        }
    }

    @Test
    @DisplayName("extra trailing bytes are ignored")
    void trailingBytesAreIgnored() throws Exception {
        byte[] full = encode(new BlockPos(1, 2, 3), 777);
        byte[] extended = Arrays.copyOf(full, full.length + 8);
        assertEquals(777, readLongPlusInt(extended));
        assertNotNull(decode(extended));
    }

    private static int readLongPlusInt(byte[] bytes) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
        buf.readLong();
        return buf.readInt();
    }
}
