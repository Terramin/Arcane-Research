package arcaneresearch.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was ResourceLocation
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import arcaneresearch.api.crafting.IArcaneresearchRecipe;
import arcaneresearch.api.internal.CommonInternals;
import arcaneresearch.common.lib.crafting.ClientRecipeCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class RecipeGroups {

    private static final Map<String, List<Identifier>> groups = new HashMap<>();
    private static int lastRecipeCount = -1;

    public static List<Identifier> getGroupRecipes(Identifier groupId) {
        compileIfNeeded();
        List<Identifier> members = groups.get(groupId.toString());
        return members != null ? members : List.of();
    }

    public static boolean isGroup(Identifier id) {
        compileIfNeeded();
        return groups.containsKey(id.toString());
    }

    public static List<Identifier> expand(Identifier id) {
        List<Identifier> members = getGroupRecipes(id);
        if (!members.isEmpty()) {
            return members;
        }
        return List.of(id);
    }

    private static void compileIfNeeded() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        // NEOFORGE_PORT: ClientLevel.getRecipeManager gone in 26.3 -> ClientRecipeCache (RecipesReceivedEvent).
        Collection<RecipeHolder<?>> holders = ClientRecipeCache.values();
        int count = holders.size();
        if (count == lastRecipeCount && !groups.isEmpty()) {
            return;
        }
        compile(holders);
        lastRecipeCount = count;
    }

    private static void compile(Collection<RecipeHolder<?>> holders) {
        groups.clear();

        for (RecipeHolder<?> holder : holders) {
            Recipe<?> recipe = holder.value();
            String group = recipe.group();
            if (group == null || group.trim().isEmpty() || group.startsWith("minecraft")) {
                continue;
            }
            if (!group.contains(":")) {
                continue;
            }
            groups.computeIfAbsent(group, k -> new ArrayList<>()).add(holder.id().identifier());
        }

        for (Map.Entry<Identifier, IArcaneresearchRecipe> e : CommonInternals.craftingRecipeCatalog.entrySet()) {
            addCatalogGroup(e.getKey(), e.getValue() == null ? null : e.getValue().getGroup());
        }

        for (Map.Entry<Identifier, Object> e : CommonInternals.craftingRecipeCatalogFake.entrySet()) {
            Object recipe = e.getValue();
            String group = null;
            if (recipe instanceof IArcaneresearchRecipe tcRecipe) {
                group = tcRecipe.getGroup();
            } else if (recipe instanceof Recipe<?> vanillaRecipe) {
                group = vanillaRecipe.group();
            }
            addCatalogGroup(e.getKey(), group);
        }
    }

    private static void addCatalogGroup(Identifier id, String group) {
        if (group == null || group.trim().isEmpty() || !group.contains(":")) {
            return;
        }
        groups.computeIfAbsent(group, k -> new ArrayList<>()).add(id);
    }
}