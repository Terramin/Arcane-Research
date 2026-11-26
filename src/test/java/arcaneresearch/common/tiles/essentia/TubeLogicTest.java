package arcaneresearch.common.tiles.essentia;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Tube routing rules")
class TubeLogicTest {
    private static TileTube tube() {
        return new TileTube(null, BlockPos.ZERO, null);
    }

    @Test
    void sideToggleAffectsConnectivityAndRespectsOtherSides() {
        TileTube tube = tube();
        assertTrue(tube.isConnectable(Direction.NORTH));

        tube.toggleSide(Direction.NORTH);

        assertFalse(tube.isConnectable(Direction.NORTH));
        assertFalse(tube.canInputFrom(Direction.NORTH));
        assertFalse(tube.canOutputTo(Direction.NORTH));
        assertTrue(tube.isConnectable(Direction.SOUTH));
        assertFalse(tube.isConnectable(null));
    }

    @Test
    void togglingSideRefreshesTheNeighboursRenderedConnections() throws Exception {
        Path java = Path.of(System.getProperty("arcaneresearch.resources.dir", "src/main/resources"))
                .getParent().resolve("java/arcaneresearch/common/tiles/essentia");
        for (String file : new String[]{"TileTube.java", "TileTubeValve.java"}) {
            String source = Files.readString(java.resolve(file), StandardCharsets.UTF_8);
            assertTrue(source.contains("otherTube.syncConnectionState()"),
                    file + " must refresh the neighbour blockstate");
        }
    }

    @Test
    void acceptsOnlyOneUnitFromAnOpenSideAndRejectsClosedSides() {
        TileTube tube = tube();

        assertEquals(1, tube.addEssentia(Aspect.FIRE, 1, Direction.NORTH));
        assertSame(Aspect.FIRE, tube.getEssentiaType(null));
        assertEquals(1, tube.getEssentiaAmount(null));
        // Tubes hold a single unit; a second one is refused.
        assertEquals(0, tube.addEssentia(Aspect.WATER, 1, Direction.NORTH));

        tube.toggleSide(Direction.SOUTH);
        assertEquals(0, tube.addEssentia(Aspect.WATER, 1, Direction.SOUTH));
    }

    @Test
    void givesEssentiaOnlyToMatchingTypeAndOpenSide() {
        TileTube tube = tube();
        tube.addEssentia(Aspect.FIRE, 1, Direction.NORTH);

        assertEquals(0, tube.takeEssentia(Aspect.WATER, 1, Direction.SOUTH));
        assertEquals(0, tube.takeEssentia(Aspect.FIRE, 1, null));

        tube.toggleSide(Direction.NORTH);
        assertEquals(0, tube.takeEssentia(Aspect.FIRE, 1, Direction.NORTH));
        tube.toggleSide(Direction.NORTH);

        assertEquals(1, tube.takeEssentia(Aspect.FIRE, 1, Direction.SOUTH));
        assertEquals(0, tube.getEssentiaAmount(null));
        assertNull(tube.getEssentiaType(null));
    }

    @Test
    void valveBlocksItsFacingSideAndCanSuppressSuction() {
        TileTubeValve valve = new TileTubeValve(null, BlockPos.ZERO, null);
        valve.facing = Direction.NORTH;

        assertFalse(valve.isConnectable(Direction.NORTH));
        assertTrue(valve.isConnectable(Direction.SOUTH));

        valve.setOpen(false);
        assertFalse(valve.isOpen());
        valve.setSuction(Aspect.FIRE, 10);
        assertEquals(0, valve.getSuctionAmount(null));

        valve.setOpen(true);
        valve.setSuction(Aspect.WATER, 7);
        assertSame(Aspect.WATER, valve.getSuctionType(null));
        assertEquals(7, valve.getSuctionAmount(null));
    }

    @Test
    void filterExposesCachedFilterList() {
        TileTubeFilter filter = new TileTubeFilter(null, BlockPos.ZERO, null);
        assertNull(filter.getAspects());

        filter.setFilter(Aspect.ORDER);
        AspectList list = filter.getAspects();
        assertSame(list, filter.getAspects());
        assertEquals(-1, list.getAmount(Aspect.ORDER));

        filter.setFilter(Aspect.ENTROPY);
        assertEquals(-1, filter.getAspects().getAmount(Aspect.ENTROPY));
        filter.clearFilter();
        assertNull(filter.getAspects());
    }

    @Test
    void bufferStoresUpToMaxAmount() {
        TileTubeBuffer buffer = new TileTubeBuffer(null, BlockPos.ZERO, null);
        for (int i = 0; i < TileTubeBuffer.MAX_AMOUNT; i++) {
            assertEquals(0, buffer.addToContainer(Aspect.FIRE, 1));
        }
        assertEquals(TileTubeBuffer.MAX_AMOUNT, buffer.getEssentiaAmount(null));
        assertEquals(TileTubeBuffer.MAX_AMOUNT, buffer.containerContains(Aspect.FIRE));
        assertTrue(buffer.doesContainerContainAmount(Aspect.FIRE, TileTubeBuffer.MAX_AMOUNT));
        // Full buffer refuses more.
        assertEquals(1, buffer.addToContainer(Aspect.FIRE, 1));

        assertTrue(buffer.takeFromContainer(Aspect.FIRE, 3));
        assertEquals(TileTubeBuffer.MAX_AMOUNT - 3, buffer.getEssentiaAmount(null));
    }

    @Test
    void bufferChokeCyclesBetweenOpenRestrictedAndClosed() {
        TileTubeBuffer buffer = new TileTubeBuffer(null, BlockPos.ZERO, null);
        Direction side = Direction.EAST;

        assertEquals(0, buffer.getChokeState(side));
        assertEquals(1, buffer.getSuctionAmount(side));

        buffer.cycleChoke(side);
        assertEquals(1, buffer.getChokeState(side));
        assertEquals(1, buffer.getSuctionAmount(side));

        buffer.cycleChoke(side);
        assertEquals(2, buffer.getChokeState(side));
        assertEquals(0, buffer.getSuctionAmount(side));

        buffer.cycleChoke(side);
        assertEquals(0, buffer.getChokeState(side));
        assertEquals(1, buffer.getSuctionAmount(side));
    }
}
