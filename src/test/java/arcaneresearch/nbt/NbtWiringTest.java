package arcaneresearch.nbt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("NBT: симметрия записи и чтения block entity")
class NbtWiringTest {

    private static final Path JAVA = Path.of(System.getProperty(
            "arcaneresearch.resources.dir", "src/main/resources")).getParent().resolve("java");

    /** Tiles that delegate NBT handling to shared helpers instead of a local pair. */
    private static final Set<String> DELEGATING_TILES = Set.of("TileNodeEnergized.java");

    @Test
    void tileSyncMethodsAreDeclaredInPairs() {
        List<Path> tiles = sources("arcaneresearch/common/tiles");
        assertTrue(tiles.size() > 40, "unexpectedly few tiles: " + tiles.size());

        for (Path file : tiles) {
            if (DELEGATING_TILES.contains(file.getFileName().toString())) {
                continue;
            }
            String source = read(file);
            boolean writes = source.contains("void writeSyncNBT(");
            boolean reads = source.contains("void readSyncNBT(");
            assertEquals(writes, reads, file.getFileName() + ": write/read pair mismatch");
        }
    }

    @Test
    void compoundTagSupportsTheValuesUsedByTileSerializers() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("count", 7);
        tag.putBoolean("active", true);
        tag.putString("key", "FIRSTSTEPS");
        tag.putLong("pos", 42L);
        tag.putFloat("ratio", 0.5f);

        // CompoundTag accessors return Optional in 26.x.
        assertEquals(7, tag.getInt("count").orElseThrow());
        assertTrue(tag.getBoolean("active").orElse(false));
        assertEquals("FIRSTSTEPS", tag.getString("key").orElseThrow());
        assertEquals(42L, tag.getLong("pos").orElseThrow());
        assertEquals(0.5f, tag.getFloat("ratio").orElse(0.0f), 1.0e-6f);
        assertFalse(tag.contains("absent"));
    }

    @Test
    void hexNoteDataSurvivesAnNbtRoundTrip() {
        var original = new arcaneresearch.discovered.table.HexResearchNoteData();
        original.key = "PORTALSCROLL";
        original.color = 0x334455;
        original.complete = true;
        original.copies = 2;
        for (int q = -1; q <= 1; q++) {
            for (int r = -1; r <= 1; r++) {
                String key = q + ":" + r;
                original.hexes.put(key, new arcaneresearch.discovered.table.HexUtils.Hex(q, r));
                original.hexEntries.put(key,
                        new arcaneresearch.discovered.table.HexResearchNoteData.HexEntry(null, 2));
            }
        }

        CompoundTag tag = new CompoundTag();
        original.writeToNBT(tag);
        var restored = new arcaneresearch.discovered.table.HexResearchNoteData();
        restored.readFromNBT(tag);

        assertEquals(original.key, restored.key);
        assertEquals(original.color, restored.color);
        assertEquals(original.complete, restored.complete);
        assertEquals(original.copies, restored.copies);
        assertEquals(original.hexes.keySet(), restored.hexes.keySet());
        assertEquals(original.hexEntries.size(), restored.hexEntries.size());
        assertFalse(restored.hexEntries.values().stream().anyMatch(entry -> entry.type != 2));
    }

    private static List<Path> sources(String relative) {
        try (Stream<Path> files = Files.walk(JAVA.resolve(relative))) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .sorted().toList();
        } catch (Exception e) {
            throw new AssertionError("Cannot list " + relative, e);
        }
    }

    private static String read(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new AssertionError("Cannot read " + file, e);
        }
    }
}
