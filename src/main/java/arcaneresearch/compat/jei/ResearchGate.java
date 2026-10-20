package arcaneresearch.compat.jei;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.crafting.IArcaneRecipe;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.common.lib.crafting.CrucibleRecipeType;
import arcaneresearch.common.lib.crafting.InfusionRecipeType;
import java.util.*;
@OnlyIn(Dist.CLIENT)
public final class ResearchGate {
    private static final Set<Object> shown = Collections.newSetFromMap(new IdentityHashMap<>());
    private static IJeiRuntime runtime;
    private static int syncCountdown = -1;
    private static int idleTicks = 0;
    private static List<IArcaneRecipe> arcane = List.of();
    private static List<CrucibleRecipeType> crucible = List.of();
    private static List<InfusionRecipeType> infusion = List.of();
    private static List<ArcaneresearchApi.BluePrint> multiblocks = List.of();
    private ResearchGate() {
    }
    public static void track(List<IArcaneRecipe> arcaneRecipes,
                             List<CrucibleRecipeType> crucibleRecipes,
                             List<InfusionRecipeType> infusionRecipes,
                             List<ArcaneresearchApi.BluePrint> blueprints) {
        arcane = arcaneRecipes;
        crucible = crucibleRecipes;
        infusion = infusionRecipes;
        multiblocks = blueprints;
        shown.clear();
        shown.addAll(arcane);
        shown.addAll(crucible);
        shown.addAll(infusion);
        shown.addAll(multiblocks);
        syncCountdown = 20;
    }
    public static void attachRuntime(IJeiRuntime rt) {
        runtime = rt;
        syncCountdown = 20;
    }
    public static void requestRefresh() {
        if (syncCountdown < 0) {
            syncCountdown = 2;
        }
    }
    public static void clientTick() {
        if (syncCountdown < 0) {
            if (++idleTicks < 100) {
                return;
            }
            idleTicks = 0;
        } else {
            if (--syncCountdown > 0) {
                return;
            }
            syncCountdown = -1;
        }
        refresh();
    }
    private static void refresh() {
        if (runtime == null) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null || player.level() == null) {
            return;
        }
        try {
            IRecipeManager manager = runtime.getRecipeManager();
            refreshType(manager, ArcaneresearchJEIPlugin.ARCANE_TYPE, arcane);
            refreshType(manager, ArcaneresearchJEIPlugin.CRUCIBLE_TYPE, crucible);
            refreshType(manager, ArcaneresearchJEIPlugin.INFUSION_TYPE, infusion);
            refreshType(manager, ArcaneresearchJEIPlugin.MULTIBLOCK_TYPE, multiblocks);
        } catch (Exception e) {
            Arcaneresearch.LOGGER.debug("JEI research gate refresh failed", e);
        }
    }
    private static <T> void refreshType(IRecipeManager manager, RecipeType<T> type, List<T> all) {
        Player player = Minecraft.getInstance().player;
        List<T> toShow = new ArrayList<>();
        List<T> toHide = new ArrayList<>();
        for (T recipe : all) {
            boolean visible = isVisible(player, researchKey(recipe));
            boolean isShown = shown.contains(recipe);
            if (visible && !isShown) {
                toShow.add(recipe);
            } else if (!visible && isShown) {
                toHide.add(recipe);
            }
        }
        if (!toHide.isEmpty()) {
            manager.hideRecipes(type, toHide);
            shown.removeAll(toHide);
        }
        if (!toShow.isEmpty()) {
            manager.unhideRecipes(type, toShow);
            shown.addAll(toShow);
        }
    }
    private static boolean isVisible(Player player, String researchKey) {
        if (researchKey == null || researchKey.isEmpty()) {
            return true;
        }
        for (String part : researchKey.split("&&")) {
            String key = part.trim();
            if (!key.isEmpty() && !ArcaneresearchCapabilities.isResearchComplete(player, key)) {
                return false;
            }
        }
        return true;
    }
    private static String researchKey(Object recipe) {
        if (recipe instanceof IArcaneRecipe arcane) {
            return arcane.getResearch();
        }
        if (recipe instanceof CrucibleRecipeType crucible) {
            return crucible.getResearch();
        }
        if (recipe instanceof InfusionRecipeType infusion) {
            return infusion.getResearch();
        }
        if (recipe instanceof ArcaneresearchApi.BluePrint blueprint) {
            return blueprint.getResearch();
        }
        return "";
    }
}
