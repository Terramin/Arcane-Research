package arcaneresearch.common.tiles.crafting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/** Source-level regression guards for the furnace fixes (no Minecraft bootstrap). */
@DisplayName("Alchemy furnace: regression wiring")
class ThaumatoriumWiringTest {

    private static final Path JAVA = Path.of(System.getProperty(
            "arcaneresearch.resources.dir", Path.of("src", "main", "resources").toString()))
            .getParent().resolve("java");

    private static String source(String relativePath) {
        try {
            return Files.readString(JAVA.resolve(relativePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return fail("Cannot read source " + relativePath, e);
        }
    }

    private static final String TILE = "arcaneresearch/common/tiles/crafting/TileThaumatorium.java";
    private static final String BODY = "arcaneresearch/common/blocks/crafting/BlockThaumatorium.java";
    private static final String TOP = "arcaneresearch/common/blocks/crafting/BlockThaumatoriumTop.java";
    private static final String PACKET =
            "arcaneresearch/common/lib/network/misc/PacketSelectThaumotoriumRecipeToServer.java";

    @Test
    @DisplayName("client tick builds the recipe bridge and resolves stored hashes")
    void clientTickResolvesRecipes() {
        String source = source(TILE).replaceAll("\\s+", "");
        int start = source.indexOf("publicstaticvoidclientTick(");
        int end = source.indexOf("protectedvoidwriteSyncNBT(");
        assertTrue(start >= 0 && end > start);
        String clientTick = source.substring(start, end);
        assertTrue(clientTick.contains("ensureCrucibleBridge(level)"));
        assertTrue(clientTick.contains("resolveRecipes()"));
    }

    @Test
    @DisplayName("unknown recipe hashes are kept instead of being dropped on sync")
    void syncKeepsHashes() {
        String source = source(TILE).replaceAll("\\s+", "");
        int start = source.indexOf("protectedvoidreadSyncNBT(");
        int end = source.indexOf("@Nullable", start);
        assertTrue(start >= 0 && end > start);
        String read = source.substring(start, end);
        assertTrue(read.contains("recipeHash.add(hash)"));
        assertFalse(read.contains("if(recipe!=null){recipeEssentia.add"));
        assertTrue(read.contains("clearCurrentRecipe()"));
    }

    @Test
    @DisplayName("recipe removal clears the current recipe and suction")
    void removalClearsCurrentRecipe() {
        String source = source(PACKET).replaceAll("\\s+", "");
        assertTrue(source.contains("clearCurrentRecipe()"));
        assertFalse(source.contains("currentCraft=-1"));
    }

    @Test
    @DisplayName("converting the furnace to brass still drops the catalyst")
    void brassConversionDropsContents() {
        for (String path : new String[]{BODY, TOP}) {
            String source = source(path).replaceAll("\\s+", "");
            assertTrue(source.contains("publicvoidonRemove("), path);
            assertTrue(source.contains("shouldDropContents("), path);
            assertTrue(source.contains("booleanbrassConversion=newState.is("), path);
            assertTrue(source.contains("if(ThaumatoriumRules.shouldDropBlockItem(brassConversion))"), path);
        }
    }

    @Test
    @DisplayName("vent particles use an independent Y velocity and a padded render box")
    void particlesAndRenderBox() {
        String source = source(TILE).replaceAll("\\s+", "");
        assertTrue(source.contains("floatvy=ThaumatoriumRules.ventSpeed("));
        assertTrue(source.contains("fx/4.0f+vx,vy,fz/4.0f+vz"));
        assertTrue(source.contains("-0.1,worldPosition.getY()-0.1"));
        assertTrue(source.contains("+1.1,worldPosition.getY()+2.1,worldPosition.getZ()+1.1"));
    }

    @Test
    @DisplayName("Salis Mundus activation is not reverted by strict multiblock checks")
    void salisMundusCanActivate() {
        String body = source(BODY).replaceAll("\\s+", "");
        assertFalse(body.contains("facing==Direction.UP&&!facingState.is("),
                "the top-block check reverts the furnace during the swap");

        String top = source(TOP).replaceAll("\\s+", "");
        assertFalse(top.contains("publicBlockStateupdateShape("),
                "the top block must not revert itself before the furnace is placed");

        String config = source("arcaneresearch/common/config/ConfigMultiblocks.java")
                .replaceAll("\\s+", "");
        assertTrue(config.contains("newDustTriggerMultiblock(\"THAUMATORIUM\",blueprint)"));
    }

    @Test
    @DisplayName("quick move never requests reverse merging")
    void quickMoveUsesNoReverse() {
        String menu = source("arcaneresearch/common/menu/ThaumatoriumMenu.java")
                .replaceAll("\\s+", "");
        assertTrue(menu.contains("moveItemStackTo(stackInSlot,range[0],range[1],false)"));
    }

    @Test
    @DisplayName("catalogue version invalidates resolved recipes after a reload")
    void catalogVersionInvalidatesResolution() {
        String tile = source(TILE).replaceAll("\\s+", "");
        assertTrue(tile.contains("ArcaneresearchCraftingManager.catalogVersion()"));
        assertTrue(tile.contains("resolvedCatalogVersion"));

        String manager = source("arcaneresearch/common/lib/crafting/ArcaneresearchCraftingManager.java")
                .replaceAll("\\s+", "");
        assertTrue(manager.contains("catalogVersion++"));
        assertTrue(manager.contains("if(datapack.isEmpty())return;"));
    }

    @Test
    @DisplayName("top block is only placed when the furnace itself survived placement")
    void topPlacementChecksLowerBlock() {
        String block = source(BODY).replaceAll("\\s+", "");
        assertTrue(block.contains("level.getBlockState(pos).is(ModBlocks.THAUMATORIUM.get())"));
    }

    @Test
    @DisplayName("placement no longer rejects an occupied block above")
    void placementIsNotBlocked() {
        String source = source(BODY).replaceAll("\\s+", "");
        int start = source.indexOf("publicBlockStategetStateForPlacement(");
        assertTrue(start >= 0);
        String method = source.substring(start, start + 250);
        assertFalse(method.contains("canBeReplaced"));
    }
}
