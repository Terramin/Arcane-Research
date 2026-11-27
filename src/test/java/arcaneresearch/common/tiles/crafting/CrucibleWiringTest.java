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

@DisplayName("Тигель: регрессионные проверки исходника")
class CrucibleWiringTest {

    private static final Path JAVA = Path.of(System.getProperty(
            "arcaneresearch.resources.dir", Path.of("src", "main", "resources").toString()))
            .getParent().resolve("java");

    private static String java(String relativePath) {
        try {
            return Files.readString(JAVA.resolve(relativePath), StandardCharsets.UTF_8)
                    .replaceAll("\\s+", "");
        } catch (IOException e) {
            return fail("Cannot read " + relativePath, e);
        }
    }

    private static final String MANAGER =
            "arcaneresearch/common/lib/crafting/ArcaneresearchCraftingManager.java";
    private static final String TILE = "arcaneresearch/common/tiles/crafting/TileCrucible.java";
    private static final String BLOCK = "arcaneresearch/common/blocks/crafting/BlockCrucible.java";

    @Test
    @DisplayName("research gate requires a player and uses strict compound-aware checks")
    void researchGate() {
        String manager = java(MANAGER);
        int start = manager.indexOf("publicstaticCrucibleMatchfindMatchingCrucibleRecipe(");
        assertTrue(start >= 0);
        int end = manager.indexOf("privatestaticbooleanknowsRecipe(", start);
        String method = manager.substring(start, end);
        assertTrue(method.contains("player==null"));
        assertTrue(method.contains("knowsRecipe(player,"));
        assertTrue(manager.contains("api.capabilities.ArcaneresearchCapabilities.knowsResearchStrict(player,research)"));
        int helper = manager.indexOf("privatestaticbooleanknowsRecipe(");
        assertTrue(helper >= 0);
        assertFalse(manager.substring(helper, helper + 300).contains("isResearchKnown"));
    }

    @Test
    @DisplayName("unknown items do not get a synthetic ENTROPY aspect")
    void noEntropyFallback() {
        String manager = java(MANAGER);
        assertFalse(manager.contains("fallback.add(Aspect.ENTROPY,1)"));
        assertTrue(manager.contains("returnret;"));
    }

    @Test
    @DisplayName("best matching recipe is chosen by vis size, not load order")
    void bestRecipeSelection() {
        String manager = java(MANAGER);
        assertFalse(manager.contains("intbestvis=0"));
        assertTrue(manager.contains("visSize()>highest"));
        assertTrue(manager.contains("ArcaneresearchApi.getCraftingRecipes().values()"));
    }

    @Test
    @DisplayName("fluid capability syncs the tile on real fill and drain")
    void fluidSync() {
        String tile = java(TILE);
        assertTrue(tile.contains("classSyncingFluidTankextendsFluidTank"));
        assertTrue(tile.contains("action.execute()&&filled>0"));
        assertTrue(tile.contains("action.execute()&&!drained.isEmpty()"));
        assertTrue(tile.contains("LazyOptional.of(()->tank)"));
    }

    @Test
    @DisplayName("spill syncs even with no aspects; block update follows bucket interaction")
    void spillAndBlockUpdate() {
        String tile = java(TILE);
        int start = tile.indexOf("publicvoidspillRandom()");
        assertTrue(start >= 0);
        String method = tile.substring(start, start + 600);
        assertFalse(method.contains("aspects.size()<=0"));
        assertTrue(method.contains("markDirtyAndSync()"));

        String block = java(BLOCK);
        assertTrue(block.contains("sendBlockUpdated(pos,state,state,3)"));
    }

    @Test
    @DisplayName("essentia containers replace base aspects instead of merging them")
    void essentiaContainerReplacement() {
        String manager = java(MANAGER);
        assertTrue(manager.contains("AspectTagRules.containerAspects("));
        assertFalse(manager.contains("merged.add(tag,aspects.getAmount(tag))"));
        String rules = java("arcaneresearch/common/lib/crafting/AspectTagRules.java");
        assertTrue(rules.contains("returncontained.copy()"));
    }

    @Test
    @DisplayName("boiling event plays the spill sound")
    void boilSound() {
        String tile = java(TILE);
        assertTrue(tile.contains("playLocalSound("));
        assertTrue(tile.contains("ModSounds.SPILL.get()"));
    }

    @Test
    @DisplayName("string tag registrations are deferred until tags are bound")
    void deferredTagRegistration() {
        String api = java("arcaneresearch/api/ArcaneresearchApi.java");
        assertTrue(api.contains("flushPendingTags()"));
        assertTrue(api.contains("isBound()"));
        assertTrue(api.contains("Cannotresolveaspecttag"));

        String handler = java("arcaneresearch/common/lib/events/TagReloadHandler.java");
        assertTrue(handler.contains("TagsUpdatedEvent"));
        assertTrue(handler.contains("ArcaneresearchApi.flushPendingTags()"));
    }

    @Test
    @DisplayName("ConfigAspects avoids item tags that do not exist in 1.20.1")
    void noMissingItemTags() {
        String config = java("arcaneresearch/common/config/ConfigAspects.java");
        for (String missing : new String[]{"minecraft:grass", "forge:dirt", "forge:vines",
                "forge:dusts/iron", "forge:dusts/gold", "forge:dusts/copper"}) {
            assertFalse(config.contains("\"" + missing + "\""), missing);
        }
        assertTrue(config.contains("newItemStack(Blocks.DIRT)"));
    }

    @Test
    @DisplayName("unknown items cache an empty aspect list instead of rescanning recipes")
    void emptyAspectsCached() {
        String manager = java(MANAGER);
        assertTrue(manager.contains("AspectListempty=newAspectList()"));
        assertTrue(manager.contains("registerObjectTag(stack,empty)"));
    }

    @Test
    @DisplayName("enchantments use the profile table and books read StoredEnchantments")
    void enchantmentAspects() {
        String manager = java(MANAGER);
        assertTrue(manager.contains("EnchantmentAspectRules.contribution("));
        assertTrue(manager.contains("AspectTagRules.enchantmentTag("));
        assertFalse(manager.contains("Aspect.MAGIC,enchantments.size()*3"));
    }

    @Test
    @DisplayName("recipe recursion goes through getObjectTags, not a stripped-down helper")
    void recursionUsesObjectTags() {
        String manager = java(MANAGER);
        assertFalse(manager.contains("getTagsRecursive("));
        assertTrue(manager.contains("getObjectTags(cat,history)"));
        assertTrue(manager.contains("getObjectTags(stacks[0],history)"));
        assertTrue(manager.contains("getObjectTags(remainder,history)"));
    }

    @Test
    @DisplayName("object tags are culled to ten aspects")
    void cullingApplied() {
        String manager = java(MANAGER);
        assertTrue(manager.contains("OBJECT_ASPECT_CAP=10"));
        assertTrue(manager.contains("AspectHelper.cullTags(capAspects(aspects,ASPECT_CAP),OBJECT_ASPECT_CAP)"));
    }

    @Test
    @DisplayName("right click smelts exactly one item from the held stack")
    void oneItemPerClick() {
        String block = java(BLOCK);
        int start = block.indexOf("publicInteractionResultuse(BlockState");
        assertTrue(start >= 0);
        String method = block.substring(start, start + 2600);

        assertTrue(method.contains("single.setCount(1)"));
        assertTrue(method.contains("crucible.attemptSmelt(single,"));
        assertFalse(method.contains("crucible.attemptSmelt(heldItem.copy()"));

        // shrink must be inside the failure branch, before the early SUCCESS return
        int failure = method.indexOf("if(result==null||result.isEmpty())");
        int shrink = method.indexOf("heldItem.shrink(1)");
        int success = method.indexOf("returnInteractionResult.SUCCESS", shrink);
        assertTrue(failure >= 0 && shrink > failure && success > shrink,
                "stack must shrink only when the single item was consumed");
        assertFalse(method.contains("player.setItemInHand(hand,result)"));
    }

    @Test
    @DisplayName("crafting event is fired for the player with the crafted output")
    void craftingEventFired() {
        String tile = java(TILE);
        assertTrue(tile.contains("ForgeEventFactory.firePlayerCraftingEvent(player,out,"));
        assertTrue(tile.contains("newnet.minecraft.world.SimpleContainer(item.copy())"));
    }

    @Test
    @DisplayName("recipes with null aspects are skipped before matching")
    void nullAspectsSkippedBeforeMatch() {
        String manager = java(MANAGER);
        assertTrue(manager.contains("crucibleRecipe.getAspects()!=null"));
        assertTrue(manager.contains("legacy.getAspects()!=null"));
        assertTrue(manager.indexOf("crucibleRecipe.getAspects()!=null")
                < manager.indexOf(".matchesCrucible("));
        assertTrue(manager.indexOf("legacy.getAspects()!=null")
                < manager.indexOf("legacy.matches("));
    }

    @Test
    @DisplayName("overflow spill does not reset the periodic counter")
    void spillCounterIndependent() {
        String tile = java(TILE);
        int start = tile.indexOf("publicstaticvoidserverTick(");
        assertTrue(start >= 0);
        String method = tile.substring(start, start + 1400);
        assertTrue(method.contains("CrucibleRules.shouldSpill(vis)"));
        assertTrue(method.contains("tickCounter>=CrucibleRules.SPILL_INTERVAL"));
        assertFalse(method.contains("shouldSpill(vis,tile.tickCounter)"));
    }

    @Test
    @DisplayName("heat counters and fluid height delegate to pure rules")
    void usesRules() {
        String tile = java(TILE);
        assertTrue(tile.contains("CrucibleRules.heatDelta("));
        assertTrue(tile.contains("CrucibleRules.shouldSyncHeat("));
        assertTrue(tile.contains("CrucibleRules.shouldSpill("));
        assertTrue(tile.contains("CrucibleRules.fluidHeight("));
        assertTrue(tile.contains("CrucibleRules.isHeated("));
    }
}
