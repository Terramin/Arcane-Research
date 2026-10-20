package arcaneresearch.client.gui;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.crafting.IArcaneresearchRecipe;
import arcaneresearch.api.internal.CommonInternals;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@OnlyIn(Dist.CLIENT)
public class RecipeGroups {
    private static final Map<String, List<ResourceLocation>> groups = new HashMap<>();
    private static int lastRecipeCount = -1;
    public static List<ResourceLocation> getGroupRecipes(ResourceLocation groupId) {
        compileIfNeeded();
        List<ResourceLocation> members = groups.get(groupId.toString());
        return members != null ? members : List.of();
    }
    public static boolean isGroup(ResourceLocation id) {
        compileIfNeeded();
        return groups.containsKey(id.toString());
    }
    public static List<ResourceLocation> expand(ResourceLocation id) {
        List<ResourceLocation> members = getGroupRecipes(id);
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
        RecipeManager manager = mc.level.getRecipeManager();
        int count = manager.getRecipes().size();
        if (count == lastRecipeCount && !groups.isEmpty()) {
            return;
        }
        compile(manager);
        lastRecipeCount = count;
    }
    private static void compile(RecipeManager manager) {
        groups.clear();
        for (Recipe<?> recipe : manager.getRecipes()) {
            String group = recipe.getGroup();
            if (group == null || group.trim().isEmpty() || group.startsWith("minecraft")) {
                continue;
            }
            if (!group.contains(":")) {
                continue;
            }
            groups.computeIfAbsent(group, k -> new ArrayList<>()).add(recipe.getId());
        }
        for (Map.Entry<ResourceLocation, IArcaneresearchRecipe> e : CommonInternals.craftingRecipeCatalog.entrySet()) {
            addCatalogGroup(e.getKey(), e.getValue() == null ? null : e.getValue().getGroup());
        }
        for (Map.Entry<ResourceLocation, Object> e : CommonInternals.craftingRecipeCatalogFake.entrySet()) {
            Object recipe = e.getValue();
            String group = null;
            if (recipe instanceof IArcaneresearchRecipe tcRecipe) {
                group = tcRecipe.getGroup();
            } else if (recipe instanceof Recipe<?> vanillaRecipe) {
                group = vanillaRecipe.getGroup();
            }
            addCatalogGroup(e.getKey(), group);
        }
    }
    private static void addCatalogGroup(ResourceLocation id, String group) {
        if (group == null || group.trim().isEmpty() || !group.contains(":")) {
            return;
        }
        groups.computeIfAbsent(group, k -> new ArrayList<>()).add(id);
    }
}
