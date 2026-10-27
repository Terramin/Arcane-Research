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
import java.util.regex.Pattern;

@OnlyIn(Dist.CLIENT)
public final class ResearchGate {
    private static final Pattern AND_SPLIT = Pattern.compile("&&");
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
        arcane = arcaneRecipes == null ? List.of() : arcaneRecipes;
        crucible = crucibleRecipes == null ? List.of() : crucibleRecipes;
        infusion = infusionRecipes == null ? List.of() : infusionRecipes;
        multiblocks = blueprints == null ? List.of() : blueprints;
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
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }
        try {
            IRecipeManager manager = runtime.getRecipeManager();
            refreshType(manager, player, ArcaneresearchJEIPlugin.ARCANE_TYPE, arcane);
            refreshType(manager, player, ArcaneresearchJEIPlugin.CRUCIBLE_TYPE, crucible);
            refreshType(manager, player, ArcaneresearchJEIPlugin.INFUSION_TYPE, infusion);
            refreshType(manager, player, ArcaneresearchJEIPlugin.MULTIBLOCK_TYPE, multiblocks);
        } catch (Exception e) {
            Arcaneresearch.LOGGER.debug("JEI research gate refresh failed", e);
        }
    }

    private static <T> void refreshType(IRecipeManager manager, Player player,
                                        RecipeType<T> type, List<T> all) {
        if (all == null || all.isEmpty()) {
            return;
        }
        List<T> toShow = null;
        List<T> toHide = null;
        for (T recipe : all) {
            if (recipe == null) {
                continue;
            }
            boolean visible = isVisible(player, researchKey(recipe));
            boolean isShown = shown.contains(recipe);
            if (visible && !isShown) {
                if (toShow == null) {
                    toShow = new ArrayList<>();
                }
                toShow.add(recipe);
            } else if (!visible && isShown) {
                if (toHide == null) {
                    toHide = new ArrayList<>();
                }
                toHide.add(recipe);
            }
        }
        if (toHide != null && !toHide.isEmpty()) {
            manager.hideRecipes(type, toHide);
            toHide.forEach(shown::remove);
        }
        if (toShow != null && !toShow.isEmpty()) {
            manager.unhideRecipes(type, toShow);
            shown.addAll(toShow);
        }
    }

    private static boolean isVisible(Player player, String researchKey) {
        if (researchKey == null || researchKey.isEmpty()) {
            return true;
        }
        for (String part : AND_SPLIT.split(researchKey)) {
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