package arcaneresearch.common.world.aura;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import arcaneresearch.common.lib.utils.PosXY;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Аура: состояние чанка и расход вис")
class AuraStateTest {

    private static final float MAX = 32766.0f;

    @Test
    void auraValuesAreClampedToTheLegalRange() {
        AuraChunk chunk = new AuraChunk(new ChunkPos(3, -4));

        chunk.setVis(-5.0f);
        assertEquals(0.0f, chunk.getVis(), 1.0e-6f);
        chunk.setFlux(-1.0f);
        assertEquals(0.0f, chunk.getFlux(), 1.0e-6f);

        chunk.setVis(100000.0f);
        chunk.setFlux(MAX + 1.0f);
        assertEquals(MAX, chunk.getVis(), 1.0e-3f);
        assertEquals(MAX, chunk.getFlux(), 1.0e-3f);
    }

    @Test
    void baseAndLocationAreStored() {
        AuraChunk chunk = new AuraChunk(new ChunkPos(3, -4));
        chunk.setBase((short) 500);
        chunk.setVis(120.0f);
        chunk.setFlux(30.0f);

        assertEquals(500, chunk.getBase());
        assertEquals(120.0f, chunk.getVis(), 1.0e-6f);
        assertEquals(30.0f, chunk.getFlux(), 1.0e-6f);
        assertEquals(new ChunkPos(3, -4), chunk.getLoc());
        assertEquals(new PosXY(3, -4), chunk.getLocXY());
        assertNull(chunk.getChunk());
        assertFalse(chunk.isModified());
    }

    @Test
    void nullLocationIsTolerated() {
        AuraChunk chunk = new AuraChunk((ChunkPos) null);
        assertNull(chunk.getLocXY());
    }

    @Test
    void visCanBeDrainedInSimulationWithoutChangingTheChunk() {
        AuraChunk chunk = new AuraChunk(new ChunkPos(0, 0));
        chunk.setVis(100.0f);

        assertTrue(AuraHandler.modifyVisInChunk(chunk, -30.0f, false));
        assertEquals(100.0f, chunk.getVis(), 1.0e-6f);

        assertTrue(AuraHandler.modifyVisInChunk(chunk, -30.0f, true));
        assertEquals(70.0f, chunk.getVis(), 1.0e-6f);

        assertTrue(AuraHandler.modifyVisInChunk(chunk, -1000.0f, true));
        assertEquals(0.0f, chunk.getVis(), 1.0e-6f);
        assertFalse(AuraHandler.modifyVisInChunk(null, 5.0f, true));
    }

    @Test
    void auraCeilingMatchesTheConfiguredMaximum() {
        assertEquals(500, AuraHandler.AURA_CEILING);
    }
}
