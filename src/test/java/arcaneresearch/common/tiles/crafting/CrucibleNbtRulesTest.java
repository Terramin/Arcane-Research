package arcaneresearch.common.tiles.crafting;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Тигель: NBT-нормализация")
class CrucibleNbtRulesTest {

    @Test
    @DisplayName("heat is stored as a signed short and round-trips")
    void heatRoundTrip() {
        CompoundTag tag = new CompoundTag();
        CrucibleNbtRules.writeHeat(tag, 151);

        assertEquals(151, tag.getShort("Heat"));
        assertEquals(151, CrucibleNbtRules.readHeat(tag));
    }

    @Test
    @DisplayName("out-of-range heat is clamped instead of silently wrapping")
    void heatClamping() {
        assertEquals(32767, CrucibleNbtRules.clampHeat(Integer.MAX_VALUE));
        assertEquals(-32768, CrucibleNbtRules.clampHeat(Integer.MIN_VALUE));
        assertEquals(0, CrucibleNbtRules.clampHeat(0));
        assertEquals(200, CrucibleNbtRules.clampHeat(200));

        CompoundTag tag = new CompoundTag();
        CrucibleNbtRules.writeHeat(tag, Integer.MAX_VALUE);
        assertEquals(32767, CrucibleNbtRules.readHeat(tag));
    }

    @Test
    @DisplayName("legacy Empty flag is detected and absent by default")
    void emptyFlag() {
        CompoundTag tag = new CompoundTag();
        assertFalse(CrucibleNbtRules.hasEmptyFlag(tag));

        tag.putBoolean(CrucibleNbtRules.EMPTY, true);
        assertTrue(CrucibleNbtRules.hasEmptyFlag(tag));
    }

    @Test
    @DisplayName("missing heat key reads as zero")
    void missingHeat() {
        assertEquals(0, CrucibleNbtRules.readHeat(new CompoundTag()));
    }

    @Test
    @DisplayName("negative heat from a corrupted tag is preserved as short")
    void negativeHeat() {
        CompoundTag tag = new CompoundTag();
        tag.putShort(CrucibleNbtRules.HEAT, (short) -5);
        assertEquals(-5, CrucibleNbtRules.readHeat(tag));
        assertEquals(0, Math.max(0, CrucibleNbtRules.readHeat(tag)));
    }
}
