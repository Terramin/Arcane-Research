package arcaneresearch.client.renderers.item;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Jar: liquid quads are separated from the glass")
class JarItemQuadsTest {
    private static BakedQuad quad(int tintIndex) {
        // 4 vertices * 8 ints: 1.20.1 BakedQuad vertex layout.
        return new BakedQuad(new int[32], tintIndex, Direction.SOUTH, null, false);
    }

    @Test
    void liquidHasTintIndexOne() {
        assertTrue(JarItemQuads.isLiquid(quad(1)));
        assertFalse(JarItemQuads.isLiquid(quad(-1)));
        assertFalse(JarItemQuads.isLiquid(quad(0)));
    }

    @Test
    void partitionSeparatesLiquidFromGlass() {
        BakedQuad liquid = quad(1);
        BakedQuad glassBody = quad(-1);
        BakedQuad glassLid = quad(-1);
        List<BakedQuad> liquidOut = new ArrayList<>();
        List<BakedQuad> glassOut = new ArrayList<>();

        JarItemQuads.partition(List.of(liquid, glassBody, glassLid), liquidOut, glassOut);

        assertEquals(List.of(liquid), liquidOut);
        assertEquals(List.of(glassBody, glassLid), glassOut);
    }
}
