package arcaneresearch.network;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import arcaneresearch.common.lib.network.fx.PacketFXBlockArc;
import arcaneresearch.common.lib.network.fx.PacketFXBlockMist;
import arcaneresearch.common.lib.network.fx.PacketFXEssentiaSource;
import arcaneresearch.common.lib.network.fx.PacketFXInfusionSource;
import arcaneresearch.common.lib.network.fx.PacketFXPollute;
import arcaneresearch.common.lib.network.fx.PacketFXShield;
import arcaneresearch.common.lib.network.fx.PacketFXSlash;
import arcaneresearch.common.lib.network.fx.PacketFXSonic;
import arcaneresearch.common.lib.network.fx.PacketFXWispZap;
import arcaneresearch.common.lib.network.misc.PacketBiomeChange;
import arcaneresearch.common.lib.network.misc.PacketFocusChangeToServer;
import arcaneresearch.common.lib.network.misc.PacketMiscStringToServer;
import arcaneresearch.common.lib.network.misc.PacketNote;
import arcaneresearch.common.lib.network.misc.PacketSelectThaumotoriumRecipeToServer;
import arcaneresearch.common.lib.network.playerdata.PacketFocusNameToServer;
import arcaneresearch.common.lib.network.playerdata.PacketRunicCharge;
import arcaneresearch.common.lib.network.playerdata.PacketSyncProgressToServer;
import arcaneresearch.common.lib.network.playerdata.PacketSyncResearchFlagsToServer;
import arcaneresearch.common.lib.network.playerdata.PacketWarpMessage;
import arcaneresearch.common.lib.network.misc.PacketItemKeyToServer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Сеть: сериализация пакетов и регистрация")
class PacketSerializationTest {

    private static final Path JAVA = Path.of(System.getProperty(
            "arcaneresearch.resources.dir", "src/main/resources")).getParent().resolve("java");

    private static Stream<Arguments> simplePackets() {
        return Stream.of(
                Arguments.of(new PacketMiscStringToServer(7, "hello")),
                Arguments.of(new PacketMiscStringToServer(-1, "")),
                Arguments.of(new PacketWarpMessage(null, (byte) 2, 5)),
                Arguments.of(new PacketWarpMessage(null, (byte) 0, -3)),
                Arguments.of(new PacketFXShield(11, 22)),
                Arguments.of(new PacketFXSlash(1, 2)),
                Arguments.of(new PacketFXWispZap(3, 4)),
                Arguments.of(new PacketFXSonic(99)),
                Arguments.of(new PacketFXBlockMist(new BlockPos(1, 2, 3), 0x00FF00)),
                Arguments.of(new PacketFXInfusionSource(new BlockPos(-4, 5, 6), new BlockPos(7, -8, 9), 12345)),
                Arguments.of(new PacketFXEssentiaSource(new BlockPos(1, 1, 1), (byte) 1, (byte) -1, (byte) 0, 42, 3)),
                Arguments.of(new PacketFXPollute(new BlockPos(0, 0, 0), 0.75f)),
                Arguments.of(new PacketFXBlockArc(1, 2, 3, 4.5f, 6.5f, 7.5f, 1f, 0f, 0.5f)),
                Arguments.of(new PacketFocusChangeToServer("arcaneresearch:focus_fire")),
                Arguments.of(new PacketItemKeyToServer(34, 2)),
                Arguments.of(new PacketNote(10, 64, -20, "minecraft:overworld", (byte) 7)),
                Arguments.of(new PacketBiomeChange(1, 2, 3, "minecraft:plains")),
                Arguments.of(new PacketSelectThaumotoriumRecipeToServer(null, new BlockPos(3, 4, 5), 123456)),
                Arguments.of(new PacketFocusNameToServer(new BlockPos(1, 2, 3), "Имя фокуса")),
                Arguments.of(new PacketRunicCharge(5, 10, 20)),
                Arguments.of(new PacketSyncProgressToServer("FIRSTSTEPS", true, false, true)),
                Arguments.of(new PacketSyncResearchFlagsToServer("ARCANEBORE", true, false, true)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("simplePackets")
    void encodeDecodeRoundTripPreservesBytes(Object packet) throws Exception {
        byte[] original = encode(packet);
        assertTrue(original.length > 0, "empty encoding for " + packet.getClass().getSimpleName());

        Object decoded = decode(packet.getClass(), original);
        assertNotNull(decoded);
        assertArrayEquals(original, encode(decoded),
                "round-trip changed bytes for " + packet.getClass().getSimpleName());
    }

    @Test
    void everyNetworkPacketIsRegisteredInTheHandler() {
        Path network = JAVA.resolve("arcaneresearch/common/lib/network");
        Path handler = JAVA.resolve("arcaneresearch/common/lib/network/PacketHandler.java");
        String registration;
        try {
            registration = Files.readString(handler, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new AssertionError("Cannot read PacketHandler", e);
        }

        List<String> packets;
        try (Stream<Path> files = Files.walk(network)) {
            packets = files.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.startsWith("Packet") && name.endsWith(".java")
                            && !name.equals("PacketHandler.java"))
                    .map(name -> name.substring(0, name.length() - 5))
                    .sorted().toList();
        } catch (Exception e) {
            throw new AssertionError("Cannot list packets", e);
        }

        assertTrue(packets.size() > 40, "unexpectedly few packets: " + packets.size());
        for (String packet : packets) {
            // NeoForge 26.x registers payload TYPE/STREAM_CODEC pairs, not PacketX.class.
            assertTrue(registration.contains(packet + ".TYPE"),
                    "packet not registered: " + packet);
            String source = readSource(network, packet);
            assertTrue(source.contains("CustomPacketPayload"),
                    packet + " does not implement CustomPacketPayload");
            assertTrue(source.contains("CustomPacketPayload.Type<"),
                    packet + " has no payload TYPE");
            assertTrue(source.contains("STREAM_CODEC"),
                    packet + " has no STREAM_CODEC");
        }
    }

    private static String readSource(Path directory, String simpleName) {
        try (Stream<Path> files = Files.walk(directory)) {
            Path file = files.filter(path -> path.getFileName().toString().equals(simpleName + ".java"))
                    .findFirst().orElseThrow();
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return fail("Cannot read " + simpleName, e);
        }
    }

    /** 26.x packets expose a static StreamCodec instead of encode(Packet, FriendlyByteBuf). */
    @SuppressWarnings("unchecked")
    private static StreamCodec<ByteBuf, Object> streamCodec(Class<?> type) throws Exception {
        Field field = type.getField("STREAM_CODEC");
        return (StreamCodec<ByteBuf, Object>) field.get(null);
    }

    private static byte[] encode(Object packet) throws Exception {
        ByteBuf buffer = Unpooled.buffer();
        streamCodec(packet.getClass()).encode(buffer, packet);
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), bytes);
        return bytes;
    }

    private static Object decode(Class<?> type, byte[] bytes) throws Exception {
        return streamCodec(type).decode(Unpooled.wrappedBuffer(bytes));
    }
}
