package arcaneresearch.common.lib.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.crafting.IArcaneRecipe;
import arcaneresearch.api.crafting.IArcaneWorkbench;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.api.wands.WandCap;
import arcaneresearch.api.wands.WandRod;
import arcaneresearch.common.items.wands.ItemWandCap;
import arcaneresearch.common.items.wands.WandItems;
import arcaneresearch.common.lib.wands.WandResearchHelper;
import arcaneresearch.init.ModRecipeTypes;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ArcaneWandDynamicRecipe implements IArcaneRecipe {

    private final String capTag;
    private final String rodTag;
    private final AspectList crystals;
    private final int vis;
    private final boolean sceptre;

    private ArcaneWandDynamicRecipe(String capTag, String rodTag, AspectList crystals, int vis) {
        this(capTag, rodTag, crystals, vis, false);
    }

    private ArcaneWandDynamicRecipe(String capTag, String rodTag, AspectList crystals, int vis,
                                    boolean sceptre) {
        this.capTag = capTag;
        this.rodTag = rodTag;
        this.crystals = crystals;
        this.vis = vis;
        this.sceptre = sceptre;
    }

    public static ArcaneWandDynamicRecipe match(CraftingContainer matrix, Player player) {
        if (matrix == null) {
            return null;
        }
        ArcaneWandDynamicRecipe normal = tryMatchNormal(matrix, player);
        if (normal != null) {
            return normal;
        }
        return matchSceptre(matrix, player);
    }

    private static ArcaneWandDynamicRecipe tryMatchNormal(CraftingContainer matrix, Player player) {
        try {
            if (!ArcaneWandRecipe.matches(matrix, null)) {
                return null;
            }
            ItemStack result = ArcaneWandRecipe.getCraftingResult(matrix);
            if (result.isEmpty() || !(result.getItem() instanceof IWand wand)) {
                return null;
            }
            WandCap cap = wand.getCap(result);
            WandRod rod = wand.getRod(result);
            if (cap == null || rod == null) {
                return null;
            }
            if (!researchAllows(player, cap.getResearch()) || !researchAllows(player, rod.getResearch())) {
                return null;
            }
            if (!hasWandAccess(player)) {
                return null;
            }
            AspectList cost = ArcaneWandRecipe.getVisCost(matrix);
            int vis = ArcaneWandRecipe.wandVisCost(cap.getCraftCost(), rod.getCraftCost());
            return new ArcaneWandDynamicRecipe(cap.getTag(), rod.getTag(), cost, vis);
        } catch (Exception e) {
            return null;
        }
    }

    public static ArcaneWandDynamicRecipe matchSceptre(CraftingContainer matrix, Player player) {
        if (matrix == null) {
            return null;
        }
        try {
            if (!ArcaneWandRecipe.matchesSceptre(matrix, null)) {
                return null;
            }
            ItemStack result = ArcaneWandRecipe.getSceptreResult(matrix);
            if (result.isEmpty() || !(result.getItem() instanceof IWand wand)) {
                return null;
            }
            WandCap cap = wand.getCap(result);
            WandRod rod = wand.getRod(result);
            if (cap == null || rod == null) {
                return null;
            }
            if (!researchAllows(player, cap.getResearch()) || !researchAllows(player, rod.getResearch())) {
                return null;
            }
            if (!hasWandAccess(player)) {
                return null;
            }
            AspectList cost = ArcaneWandRecipe.getSceptreVisCost(matrix);
            int vis = Math.min((cap.getCraftCost() + rod.getCraftCost()) * 45, 1800);
            return new ArcaneWandDynamicRecipe(cap.getTag(), rod.getTag(), cost, vis, true);
        } catch (Exception e) {
            return null;
        }
    }

    static boolean researchAllows(Player player, String key) {
        if (player == null || key == null) {
            return true;
        }
        try {
            if (ResearchCategories.getResearch(key) == null) {
                return true;
            }
        } catch (Exception ignored) {
            return true;
        }
        return WandResearchHelper.isResearchComplete(player, key);
    }

    public static List<ArcaneWandDynamicRecipe> allPairs() {
        List<ArcaneWandDynamicRecipe> out = new ArrayList<>();
        for (WandRod rod : WandRod.rods.values()) {
            for (WandCap cap : WandCap.caps.values()) {
                if (rod.getTag().equals("wood") && cap.getTag().equals("iron")) {
                    continue;
                }
                out.add(forPair(cap, rod));
            }
        }
        for (WandRod rod : WandRod.rods.values()) {
            for (WandCap cap : WandCap.caps.values()) {
                out.add(forSceptre(cap, rod));
            }
        }
        return out;
    }

    public static ArcaneWandDynamicRecipe forPair(WandCap cap, WandRod rod) {
        AspectList cost = new AspectList();
        int vis = 0;
        try {
            int cc = cap.getCraftCost();
            int cr = rod.getCraftCost();
            if (cc >= 0 && cr >= 0) {
                int amount = ArcaneWandRecipe.wandCrystalCost(cc, cr);
                for (Aspect as : Aspect.getPrimalAspects()) {
                    cost.add(as, amount);
                }
                vis = ArcaneWandRecipe.wandVisCost(cc, cr);
            }
        } catch (Exception ignored) {
        }
        return new ArcaneWandDynamicRecipe(cap.getTag(), rod.getTag(), cost, vis);
    }

    public static ArcaneWandDynamicRecipe forSceptre(WandCap cap, WandRod rod) {
        AspectList cost = new AspectList();
        int vis = 0;
        try {
            int cc = cap.getCraftCost();
            int cr = rod.getCraftCost();
            if (cc >= 0 && cr >= 0) {
                int amount = Math.min((int) ((cc + cr) * 1.5F), 96);
                for (Aspect as : Aspect.getPrimalAspects()) {
                    cost.add(as, amount);
                }
                vis = Math.min((cc + cr) * 45, 1800);
            }
        } catch (Exception ignored) {
        }
        return new ArcaneWandDynamicRecipe(cap.getTag(), rod.getTag(), cost, vis, true);
    }

    public static boolean hasWandAccess(Player player) {
        if (player == null) {
            return true;
        }
        try {
            return arcaneresearch.common.lib.research.ResearchManager.doesPlayerHaveRequisites(player,
                    "LEGACYWANDS");
        } catch (Exception e) {
            return false;
        }
    }

    private ItemStack buildWand() {
        ItemStack out = WandItems.createWandStack();
        if (out.getItem() instanceof IWand w) {
            w.setCap(out, WandCap.caps.get(capTag));
            w.setRod(out, WandRod.rods.get(rodTag));
            if (sceptre) {
                arcaneresearch.common.items.wands.ItemWand.setSceptre(out);
            }
        }
        return out;
    }

    @Override
    public boolean matches(@NotNull IArcaneWorkbench container, @NotNull Level level) {
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull IArcaneWorkbench container, @NotNull RegistryAccess registryAccess) {
        return buildWand();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
        return buildWand();
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath("arcaneresearch",
                (sceptre ? "sceptre_dynamic_" : "wand_dynamic_") + rodTag + "_" + capTag);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ShapedArcaneRecipe.Serializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipeTypes.ARCANE_WORKBENCH.get();
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> out = NonNullList.withSize(9, Ingredient.EMPTY);
        try {
            WandCap cap = WandCap.caps.get(capTag);
            WandRod rod = WandRod.rods.get(rodTag);
            if (sceptre) {
                if (cap != null) {
                    ItemStack capStack = ItemWandCap.createStack(cap);
                    if (!capStack.isEmpty()) {
                        Ingredient capIng = Ingredient.of(capStack);
                        out.set(1, capIng);
                        out.set(5, capIng);
                        out.set(6, capIng);
                    }
                }
                if (rod != null) {
                    ItemStack rodStack = rod.getItem().copy();
                    if (!rodStack.isEmpty()) {
                        out.set(4, Ingredient.of(rodStack));
                    }
                }
                if (arcaneresearch.init.ModItems.SALIS_MUNDUS.isPresent()) {
                    out.set(2, Ingredient.of(
                            new ItemStack(arcaneresearch.init.ModItems.SALIS_MUNDUS.get())));
                }
                return out;
            }
            if (cap != null) {
                ItemStack capStack = ItemWandCap.createStack(cap);
                if (!capStack.isEmpty()) {
                    Ingredient capIng = Ingredient.of(capStack);
                    out.set(2, capIng);
                    out.set(6, capIng);
                }
            }
            if (rod != null) {
                ItemStack rodStack = rod.getItem().copy();
                if (!rodStack.isEmpty()) {
                    out.set(4, Ingredient.of(rodStack));
                }
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    @Override
    public @NotNull String getGroup() {
        return "wand";
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public int getVis() {
        return vis;
    }

    @Override
    public String getResearch() {
        return "";
    }

    @Override
    public AspectList getCrystals() {
        return crystals.copy();
    }
}