package arcaneresearch.discovered.scan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import arcaneresearch.discovered.scan.ScanTargets.Kind;
import arcaneresearch.discovered.scan.ScanTargets.Target;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Сканирование: стабильные ключи целей")
class ScanTargetsTest {

    @Test
    void blockAndNodeKeysIncludeCoordinates() {
        BlockPos pos = new BlockPos(3, -4, 5);

        assertEquals("B:3,-4,5", new Target(Kind.BLOCK, pos, null, "").steadyKey());
        assertEquals("N:3,-4,5", new Target(Kind.NODE, pos, null, "").steadyKey());
    }

    @Test
    void missingTargetsProduceEmptyKeys() {
        assertEquals("", new Target(Kind.NONE, null, null, "").steadyKey());
        assertEquals("", new Target(Kind.BLOCK, null, null, "").steadyKey());
        assertEquals("", new Target(Kind.NODE, null, null, "").steadyKey());
        assertEquals("", new Target(Kind.ENTITY, null, null, "").steadyKey());
        assertEquals("", new Target(null, null, null, "").steadyKey());
    }

    @Test
    void scanRangeIsNineBlocks() {
        assertEquals(9.0D, ScanTargets.RANGE);
    }
}
