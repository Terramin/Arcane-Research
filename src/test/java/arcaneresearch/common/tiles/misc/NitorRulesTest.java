package arcaneresearch.common.tiles.misc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("Нитор: правила и ресурсы")
class NitorRulesTest {

    private static final Path ROOT = Path.of(System.getProperty(
            "arcaneresearch.resources.dir", Path.of("src", "main", "resources").toString()));
    private static final Path JAVA = ROOT.getParent().resolve("java");

    private static String java(String relativePath) {
        try {
            return Files.readString(JAVA.resolve(relativePath), StandardCharsets.UTF_8)
                    .replaceAll("\\s+", "");
        } catch (IOException e) {
            return fail("Cannot read " + relativePath, e);
        }
    }

    private static boolean resource(String relativePath) {
        return Files.isRegularFile(ROOT.resolve(relativePath));
    }

    @Test
    @DisplayName("tuning constants match the original block and tile")
    void constants() {
        assertEquals(10, NitorRules.CORE_INTERVAL);
        assertEquals(0.45, NitorRules.FLAME_Y);
        assertEquals(0.49, NitorRules.CORE_Y);
        assertEquals(0.025, NitorRules.FLAME_POS_JITTER);
        assertEquals(0.0025, NitorRules.FLAME_VX_JITTER);
        assertEquals(0.06, NitorRules.FLAME_VY_SPEED);
        assertEquals(10, NitorRules.FLAME_AGE);
        assertEquals(5, NitorRules.FLAME_AGE_JITTER);
        assertEquals(10, NitorRules.CORE_AGE);
        assertEquals(0.66f, NitorRules.FLAME_ALPHA);
        assertEquals(0.33000001311302185, NitorRules.SHAPE_MIN);
        assertEquals(0.6600000262260437, NitorRules.SHAPE_MAX);
    }

    @Test
    @DisplayName("core particle is drawn every tenth tick")
    void coreInterval() {
        assertTrue(NitorRules.shouldDrawCore(0));
        for (int i = 1; i < 10; i++) {
            assertFalse(NitorRules.shouldDrawCore(i));
        }
        assertTrue(NitorRules.shouldDrawCore(10));
        assertTrue(NitorRules.shouldDrawCore(20));
    }

    @Test
    @DisplayName("flame offsets and velocities stay within the original amplitudes")
    void flameMath() {
        assertEquals(0.0, NitorRules.flameOffset(0.0), 1e-9);
        assertEquals(0.025, NitorRules.flameOffset(1.0), 1e-9);
        assertEquals(0.0025, NitorRules.flameVelocity(1.0), 1e-9);
        assertEquals(-0.0025, NitorRules.flameVelocity(-1.0), 1e-9);
        assertEquals(0.0, NitorRules.flameRise(0.0f), 1e-9);
        assertEquals(0.03, NitorRules.flameRise(0.5f), 1e-9);
        assertEquals(0.06, NitorRules.flameRise(1.0f), 1e-9);
    }

    @Test
    @DisplayName("tile and block delegate their numbers to the rules class")
    void wiring() {
        String tile = java("arcaneresearch/common/tiles/misc/TileNitor.java");
        assertTrue(tile.contains("NitorRules.flameOffset("));
        assertTrue(tile.contains("NitorRules.flameVelocity("));
        assertTrue(tile.contains("NitorRules.shouldDrawCore("));
        assertFalse(tile.contains("count++%10"));

        String block = java("arcaneresearch/common/blocks/misc/BlockNitor.java");
        assertTrue(block.contains("NitorRules.SHAPE_MIN"));
        assertTrue(block.contains("NitorRules.SHAPE_MAX"));
        assertFalse(block.contains(".replaceable()"),
                "a replaceable nitor is silently destroyed by a misplaced block");
        assertTrue(block.contains(".noCollission()"));
        assertTrue(block.contains("canBeReplaced(BlockStatestate,Fluidfluid)"),
                "flowing water/lava must not wash the nitor away");
        assertTrue(block.contains("implementsEntityBlock,SimpleWaterloggedBlock"));
        assertTrue(block.contains("publicstaticfinalBooleanPropertyWATERLOGGED=BlockStateProperties.WATERLOGGED"));
        assertTrue(block.contains("getStateForPlacement(BlockPlaceContextcontext)"));
        assertTrue(block.contains("getFluidState(BlockStatestate)"));
        assertTrue(block.contains("Fluids.WATER.getSource(false)"));
    }

    @Test
    @DisplayName("render type switches to no-depth only for submerged fluid-visible particles")
    void renderKind() {
        assertEquals(NitorRules.RenderKind.ADDITIVE, NitorRules.renderKind(0, false, false));
        assertEquals(NitorRules.RenderKind.NORMAL, NitorRules.renderKind(1, false, false));
        assertEquals(NitorRules.RenderKind.ADDITIVE_NO_DEPTH, NitorRules.renderKind(2, false, false));
        assertEquals(NitorRules.RenderKind.NORMAL_NO_DEPTH, NitorRules.renderKind(3, false, false));

        assertEquals(NitorRules.RenderKind.ADDITIVE_NO_DEPTH, NitorRules.renderKind(0, true, true));
        assertEquals(NitorRules.RenderKind.NORMAL_NO_DEPTH, NitorRules.renderKind(1, true, true));
        assertEquals(NitorRules.RenderKind.ADDITIVE, NitorRules.renderKind(0, true, false));
        assertEquals(NitorRules.RenderKind.NORMAL, NitorRules.renderKind(1, true, false));
        assertEquals(NitorRules.RenderKind.ADDITIVE, NitorRules.renderKind(0, false, true));
    }

    @Test
    @DisplayName("nitor particles render through water so they are visible from above")
    void particlesVisibleInFluid() {
        String dispatcher = java("arcaneresearch/client/fx/FXDispatcher.java");
        int count = 0;
        int index = 0;
        while ((index = dispatcher.indexOf("setVisibleInFluid(true)", index)) >= 0) {
            count++;
            index += "setVisibleInFluid(true)".length();
        }
        assertEquals(2, count, "both core and flame particles need the fluid-visible flag");

        String particle = java("arcaneresearch/client/fx/particles/FXGeneric.java");
        assertTrue(particle.contains("visibleInFluid"));
        assertTrue(particle.contains("getFluidState(fluidPos).isEmpty()"));
        assertTrue(particle.contains("TC_ADDITIVE_NO_DEPTH"));
    }

    @Test
    @DisplayName("all sixteen nitor colors have blockstates, models, loot and dye recipes")
    void colorResources() {
        String[] colors = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink",
                "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"};
        for (String color : colors) {
            assertTrue(resource("assets/arcaneresearch/blockstates/nitor_" + color + ".json"), color);
            assertTrue(resource("assets/arcaneresearch/models/block/nitor_" + color + ".json"), color);
            assertTrue(resource("assets/arcaneresearch/models/item/nitor_" + color + ".json"), color);
            assertTrue(resource("data/arcaneresearch/loot_tables/blocks/nitor_" + color + ".json"), color);
            assertTrue(resource("data/arcaneresearch/recipes/crafting/nitor_" + color + ".json"), color);
        }
        assertTrue(resource("data/arcaneresearch/recipes/crucible/nitor.json"));
        assertTrue(resource("data/arcaneresearch/tags/blocks/nitor.json"));
    }

    @Test
    @DisplayName("the nitor tag contains every registered color")
    void nitorTag() {
        try {
            String tag = Files.readString(ROOT.resolve("data/arcaneresearch/tags/blocks/nitor.json"));
            for (String color : new String[]{"white", "light_blue", "light_gray", "black"}) {
                assertTrue(tag.contains("arcaneresearch:nitor_" + color), color);
            }
            assertFalse(tag.contains("arcaneresearch:nitor_lightblue"));
            assertFalse(tag.contains("arcaneresearch:nitor_silver"));
        } catch (IOException e) {
            fail("Cannot read nitor tag", e);
        }
    }
}
