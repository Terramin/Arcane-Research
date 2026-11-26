package arcaneresearch.common.tiles.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
    void mutableXpMaxMatchesOriginalLimit() {
        TileJarBrain brain = brain();
        assertEquals(TileJarBrain.MAX_XP, brain.xpMax);

        brain.xpMax = 100;
        CompoundTag tag = new CompoundTag();
        tag.putInt("XP", 250);
        brain.load(tag);

        assertEquals(100, brain.getXP());
        assertEquals(1.0F, brain.getFillPercent(), 1.0E-6F);
        assertEquals(15, brain.getComparatorOutput());
    }

    @Test
    void zeroXpMaxDoesNotDivideByZero() {
        TileJarBrain brain = brain();
        brain.xpMax = 0;
        assertEquals(0.0F, brain.getFillPercent(), 1.0E-6F);
        assertEquals(0, brain.getComparatorOutput());
    }

    @Test
    void syncTagRoundTripsStoredXp() {
        TileJarBrain brain = brain();
        brain.xp = 777;

        CompoundTag tag = brain.getUpdateTag();
        assertEquals(777, tag.getInt("XP"));

        TileJarBrain loaded = brain();
        loaded.load(tag);
        assertEquals(777, loaded.getXP());
    }

    @Test
    void negativeXpFromNbtIsClampedToZero() {
        TileJarBrain brain = brain();
        CompoundTag tag = new CompoundTag();
        tag.putInt("XP", -50);

        brain.load(tag);

        assertEquals(0, brain.getXP());
        assertEquals(0, brain.getComparatorOutput());
    }

    @Test
    void xpMaxIsMutableButNotSerializedLikeTheOriginal() {
        TileJarBrain brain = brain();
        brain.xpMax = 100;
        brain.xp = 100;

        CompoundTag tag = brain.getUpdateTag();
        assertFalse(tag.contains("xpMax"));

        TileJarBrain loaded = brain();
        loaded.load(tag);
        assertEquals(100, loaded.getXP());
        assertEquals(TileJarBrain.MAX_XP, loaded.xpMax);
    }

    @Test
    void tickCountAccessorsRoundTrip() {
        TileJarBrain brain = brain();
        assertEquals(0, brain.getTickCount());

        brain.setTickCount(7);
        assertEquals(7, brain.getTickCount());
    }
}
