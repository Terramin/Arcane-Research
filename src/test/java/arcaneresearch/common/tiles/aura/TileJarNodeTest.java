package arcaneresearch.common.tiles.aura;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Aura jar node essentia storage")
class TileJarNodeTest {
    private static TileJarNode node() {
        return new TileJarNode(null, BlockPos.ZERO, null);
    }

    @Test
    void settingAspectsAlsoCopiesTheBase() {
        TileJarNode node = node();
        AspectList source = new AspectList().add(Aspect.FIRE, 10);

        node.setAspects(source);

        assertEquals(10, node.getAspects().getAmount(Aspect.FIRE));
        assertEquals(10, node.getAspectsBase().getAmount(Aspect.FIRE));
        source.add(Aspect.FIRE, 5);
        assertEquals(10, node.getAspects().getAmount(Aspect.FIRE));
    }

    @Test
    void nodeCanOnlyBeRefilledUpToItsBaseAmount() {
        TileJarNode node = node();
        node.setAspects(new AspectList().add(Aspect.WATER, 10));
        assertTrue(node.takeFromContainer(Aspect.WATER, 4));
        assertEquals(6, node.getAspects().getAmount(Aspect.WATER));

        assertEquals(0, node.addToContainer(Aspect.WATER, 4));
        assertEquals(10, node.getAspects().getAmount(Aspect.WATER));

        // A full node returns everything the caller tried to add.
        assertEquals(5, node.addToContainer(Aspect.WATER, 5));
        assertEquals(10, node.getAspects().getAmount(Aspect.WATER));
        assertEquals(10, node.getAspectsBase().getAmount(Aspect.WATER));
    }

    @Test
    void essentiaCanBeDrainedDownToZero() {
        TileJarNode node = node();
        node.setAspects(new AspectList().add(Aspect.EARTH, 4));

        assertTrue(node.doesContainerContainAmount(Aspect.EARTH, 4));
        assertFalse(node.doesContainerContainAmount(Aspect.EARTH, 5));
        assertFalse(node.takeFromContainer(Aspect.EARTH, 5));

        assertTrue(node.takeFromContainer(Aspect.EARTH, 4));
        assertEquals(0, node.getAspects().getAmount(Aspect.EARTH));
        assertFalse(node.takeFromContainer(Aspect.EARTH, 1));
    }
}
