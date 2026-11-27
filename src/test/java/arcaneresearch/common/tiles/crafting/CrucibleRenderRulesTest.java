package arcaneresearch.common.tiles.crafting;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Тигель: цвет жидкости")
class CrucibleRenderRulesTest {

    @Test
    @DisplayName("empty crucible uses the biome water colour and is opaque")
    void empty() {
        int water = 0x3F76E4;
        CrucibleRenderRules.FluidColor color =
                CrucibleRenderRules.fluidColor(new AspectList(), water);

        assertEquals(0x3F / 255.0f, color.r(), 1e-6f);
        assertEquals(0x76 / 255.0f, color.g(), 1e-6f);
        assertEquals(0xE4 / 255.0f, color.b(), 1e-6f);
        assertEquals(1.0f, color.alpha(), 1e-6f);
    }

    @Test
    @DisplayName("dissolved aspects colour the liquid")
    void aspectColour() {
        AspectList aspects = new AspectList().add(Aspect.AIR, 10);

        CrucibleRenderRules.FluidColor color =
                CrucibleRenderRules.fluidColor(aspects, CrucibleRenderRules.DEFAULT_WATER_COLOR);

        assertEquals(0xFF / 255.0f, color.r(), 1e-6f);
        assertEquals(0xFF / 255.0f, color.g(), 1e-6f);
        assertEquals(0x7E / 255.0f, color.b(), 1e-6f);
        assertEquals(0.745f, color.alpha(), 1e-6f);
    }

    @Test
    @DisplayName("mixed aspects are averaged by amount")
    void mixedAspects() {
        AspectList aspects = new AspectList().add(Aspect.AIR, 1).add(Aspect.AIR, 1);
        CrucibleRenderRules.FluidColor one =
                CrucibleRenderRules.fluidColor(new AspectList().add(Aspect.AIR, 2), 0);
        CrucibleRenderRules.FluidColor two = CrucibleRenderRules.fluidColor(aspects, 0);

        assertEquals(one.r(), two.r(), 1e-6f);
        assertEquals(one.g(), two.g(), 1e-6f);
        assertEquals(one.b(), two.b(), 1e-6f);
    }

    @Test
    @DisplayName("opacity never drops below 0.5 and null aspects are treated as empty")
    void clampedAlpha() {
        CrucibleRenderRules.FluidColor full = CrucibleRenderRules.fluidColor(
                new AspectList().add(Aspect.AIR, 5000), 0);
        assertEquals(0.5f, full.alpha(), 1e-6f);

        CrucibleRenderRules.FluidColor noAspects =
                CrucibleRenderRules.fluidColor(null, 0x3F76E4);
        assertEquals(0x3F / 255.0f, noAspects.r(), 1e-6f);
        assertEquals(1.0f, noAspects.alpha(), 1e-6f);
    }

    @Test
    @DisplayName("all channels stay in [0,1] for any vis and water colour")
    void bounded() {
        for (int vis = 0; vis <= 2000; vis += 137) {
            AspectList aspects = new AspectList().add(Aspect.AIR, vis);
            CrucibleRenderRules.FluidColor color =
                    CrucibleRenderRules.fluidColor(aspects, 0x3F76E4);
            assertTrue(color.r() >= 0.0f && color.r() <= 1.0f);
            assertTrue(color.g() >= 0.0f && color.g() <= 1.0f);
            assertTrue(color.b() >= 0.0f && color.b() <= 1.0f);
            assertTrue(color.alpha() >= 0.5f && color.alpha() <= 1.0f);
        }
    }
}
