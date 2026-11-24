package arcaneresearch.common.tiles.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Brain jar XP storage")
class TileJarBrainTest {
    private static TileJarBrain brain() {
        return new TileJarBrain(null, BlockPos.ZERO, null);
    }

    @Test
    void emptyBrainHasNoOutput() {
        TileJarBrain brain = brain();
        assertEquals(0, brain.getXP());
        assertEquals(0.0F, brain.getFillPercent(), 1.0E-6F);
        assertEquals(0, brain.getComparatorOutput());
        assertEquals(0, brain.getUpdateTag().getInt("XP"));
    }

    @Test
    void fillPercentAndComparatorScaleWithStoredXp() {
        TileJarBrain brain = brain();

        brain.xp = 500;
        assertEquals(0.25F, brain.getFillPercent(), 1.0E-6F);
        assertEquals(4, brain.getComparatorOutput());

        brain.xp = 1000;
        assertEquals(0.5F, brain.getFillPercent(), 1.0E-6F);
        assertEquals(8, brain.getComparatorOutput());

        brain.xp = 1500;
        assertEquals(0.75F, brain.getFillPercent(), 1.0E-6F);
        assertEquals(11, brain.getComparatorOutput());

        brain.xp = TileJarBrain.MAX_XP;
        assertEquals(1.0F, brain.getFillPercent(), 1.0E-6F);
        assertEquals(15, brain.getComparatorOutput());
    }

    @Test
    void nbtLoadKeepsValueBelowMax() {
        TileJarBrain brain = brain();
        CompoundTag tag = new CompoundTag();
        tag.putInt("XP", 123);

        brain.load(tag);

        assertEquals(123, brain.getXP());
        assertEquals(123, brain.getUpdateTag().getInt("XP"));
    }

    @Test
    void nbtLoadClampsXpAboveMax() {
        TileJarBrain brain = brain();
        CompoundTag tag = new CompoundTag();
        tag.putInt("XP", TileJarBrain.MAX_XP * 5);

        brain.load(tag);

        assertEquals(TileJarBrain.MAX_XP, brain.getXP());
        assertEquals(1.0F, brain.getFillPercent(), 1.0E-6F);
        assertEquals(15, brain.getComparatorOutput());
    }

    @Test
    void tickCountAccessorsRoundTrip() {
        TileJarBrain brain = brain();
        assertEquals(0, brain.getTickCount());

        brain.setTickCount(7);
        assertEquals(7, brain.getTickCount());
    }
}
