package arcaneresearch.common.lib.crafting;

import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import arcaneresearch.Arcaneresearch;

import java.util.Collection;
import java.util.List;

// NEOFORGE_PORT (26.3): client levels expose only RecipeAccess (propertySet +
// stonecutterRecipes) — no recipe query API. The sanctioned client source is the
// RecipeMap delivered by RecipesReceivedEvent (same approach as the JEI-side
// lookup; structural pattern only, own implementation).
// Server code must use ServerLevel.recipeAccess()/server RecipeManager instead.
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public final class ClientRecipeCache {

    private static volatile RecipeMap map = RecipeMap.EMPTY;

    private ClientRecipeCache() {
    }

    @SubscribeEvent
    public static void onRecipesReceived(RecipesReceivedEvent event) {
        map = event.getRecipeMap() != null ? event.getRecipeMap() : RecipeMap.EMPTY;
        // NEOFORGE_PORT (26.3) FIX: with the synced recipes available, refresh the client's
        // potion aspects with real brewing reagent data. 1.20.1 Forge had static PotionBrewing
        // on the client; a remote-server 26.3 client fell back to MAGIC 5 / ALCHEMY 5 for
        // thaumometer/JEI aspect display. Registration is an idempotent map overwrite keyed
        // identically to the content-setup pass (AspectHelper.registerObjectTag).
        if (map.values() != null && !map.values().isEmpty()) {
            try {
                arcaneresearch.common.config.ConfigAspects.registerPotionAspects(map.values());
            } catch (Exception e) {
                Arcaneresearch.LOGGER.debug("Client potion aspect refresh failed", e);
            }
        }
    }

    public static Collection<RecipeHolder<?>> values() {
        return map.values();
    }

    public static <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> byType(RecipeType<T> type) {
        return map.byType(type);
    }

    public static <I extends RecipeInput, T extends Recipe<I>> List<T> valuesOfType(RecipeType<T> type) {
        return map.byType(type).stream().map(RecipeHolder::value).toList();
    }

    // NEOFORGE_PORT: RecipeManager.byKey(ResourceKey) removed with the manager on the
    // client; RecipeMap.byKey(ResourceKey) is the 26.x source (nullable -> Optional).
    public static java.util.Optional<RecipeHolder<?>> byKey(net.minecraft.resources.Identifier id) {
        if (id == null) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.ofNullable(map.byKey(
                net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE, id)));
    }

    /**
     * Result ItemStack for recipe-book/search display (Forge getResultItem parity).
     * Arcane recipes use the legacy no-arg helper; vanilla results resolve through
     * Recipe.display() ItemStackSlotDisplay (tag/other displays -> context resolve).
     * The resolveForFirstStack fallback mirrors renderCraftingRecipe's output path,
     * so bookmark icons always match the modal grid output.
     */
    public static net.minecraft.world.item.ItemStack displayResultStack(Recipe<?> recipe) {
        if (recipe == null) {
            return null;
        }
        if (recipe instanceof arcaneresearch.api.crafting.IArcaneRecipe arcane) {
            return arcane.getResultItem();
        }
        try {
            for (var display : recipe.display()) {
                if (display.result() instanceof net.minecraft.world.item.crafting.display.SlotDisplay.ItemStackSlotDisplay item) {
                    return item.stack().create();
                }
            }
        } catch (Exception ignored) {
        }
        try {
            var level = net.minecraft.client.Minecraft.getInstance().level;
            if (level != null) {
                var ctx = net.minecraft.world.item.crafting.display.SlotDisplayContext.fromLevel(level);
                for (var display : recipe.display()) {
                    var resolved = display.result().resolveForFirstStack(ctx);
                    if (resolved != null && !resolved.isEmpty()) {
                        return resolved;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
