package arcaneresearch.common.lib.crafting;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectHelper;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IEssentiaContainerItem;
import arcaneresearch.api.crafting.IArcaneRecipe;
import arcaneresearch.api.crafting.IArcaneWorkbench;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.init.ModRecipeTypes;
import java.util.List;
public class ArcaneresearchCraftingManager {
    public static final int ASPECT_CAP = 500;
    private static final List<InfusionRecipeType> dynamicInfusionRecipes = new java.util.ArrayList<>();
    public static void registerDynamicInfusionRecipe(InfusionRecipeType recipe) {
        dynamicInfusionRecipes.add(recipe);
    }
    public static void registerDynamicRecipes() {
        dynamicInfusionRecipes.clear();
        dynamicInfusionRecipes.add(new InfusionRunicAugmentRecipe());
    }
    public static IArcaneRecipe findMatchingArcaneRecipe(CraftingContainer matrix, Player player) {
        if (player == null || matrix == null) {
            return null;
        }
        if (!(matrix instanceof IArcaneWorkbench workbench)) {
            return null;
        }
        Level level = player.level();
        for (Recipe<?> recipe : level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.ARCANE_WORKBENCH.get())) {
            if (recipe instanceof IArcaneRecipe arcaneRecipe) {
                if (arcaneRecipe.matches(workbench, level)) {
                    if (arcaneRecipe.getResearch() != null && arcaneRecipe.getResearch().isEmpty()
                            && isWandGatedRecipe(arcaneRecipe.getId())
                            && !ArcaneWandDynamicRecipe.hasWandAccess(player)) {
                        continue;
                    }
                    String research = arcaneRecipe.getResearch();
                    if (research != null && !research.isEmpty()) {
                        if (!ArcaneresearchCapabilities.isResearchKnown(player, research)) {
                            continue;
                        }
                    }
                    return arcaneRecipe;
                }
            }
        }
        IArcaneRecipe wandRecipe = ArcaneWandDynamicRecipe.match(matrix, player);
        if (wandRecipe != null) {
            return wandRecipe;
        }
        return null;
    }
    private static boolean isWandGatedRecipe(ResourceLocation id) {
        if (id == null || !"arcaneresearch".equals(id.getNamespace())) {
            return false;
        }
        String path = id.getPath();
        return path.startsWith("wand_cap_") || path.equals("wand_wood_iron");
    }
    public static ItemStack findMatchingArcaneRecipeResult(CraftingContainer matrix, Player player) {
        IArcaneRecipe recipe = findMatchingArcaneRecipe(matrix, player);
        if (recipe != null && matrix instanceof IArcaneWorkbench workbench) {
            return recipe.assemble(workbench, player.level().registryAccess());
        }
        return ItemStack.EMPTY;
    }
    public static AspectList findMatchingArcaneRecipeCrystals(CraftingContainer matrix, Player player) {
        IArcaneRecipe recipe = findMatchingArcaneRecipe(matrix, player);
        return recipe != null ? recipe.getCrystals() : null;
    }
    public static int findMatchingArcaneRecipeVis(CraftingContainer matrix, Player player) {
        IArcaneRecipe recipe = findMatchingArcaneRecipe(matrix, player);
        return recipe != null ? recipe.getVis() : 0;
    }
    public static CrucibleRecipeType findMatchingCrucibleRecipe(AspectList crucibleAspects,
                                                                ItemStack catalyst,
                                                                Player player,
                                                                Level level) {
        if (catalyst.isEmpty() || level == null) {
            return null;
        }
        for (Recipe<?> recipe : level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.CRUCIBLE.get())) {
            if (recipe instanceof CrucibleRecipeType crucibleRecipe) {
                if (crucibleRecipe.matchesCrucible(crucibleAspects, catalyst)) {
                    String research = crucibleRecipe.getResearch();
                    if (research != null && !research.isEmpty() && player != null) {
                        if (!ArcaneresearchCapabilities.isResearchKnown(player, research)) {
                            continue;
                        }
                    }
                    return crucibleRecipe;
                }
            }
        }
        return null;
    }
    public static List<CrucibleRecipeType> findCrucibleRecipesForCatalyst(ItemStack catalyst, Level level) {
        return level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.CRUCIBLE.get())
                .stream()
                .filter(recipe -> recipe instanceof CrucibleRecipeType)
                .map(recipe -> (CrucibleRecipeType) recipe)
                .filter(recipe -> recipe.catalystMatches(catalyst))
                .toList();
    }
    private static int bridgedCrucibleFingerprint = 0;
    private static final java.util.Set<net.minecraft.resources.ResourceLocation> bridgedCrucibleKeys =
            new java.util.HashSet<>();
    public static void ensureCrucibleBridge(@javax.annotation.Nullable Level level) {
        if (level == null) return;
        java.util.List<CrucibleRecipeType> datapack;
        try {
            datapack = level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.CRUCIBLE.get());
        } catch (Exception e) {
            return;
        }
        int fingerprint = datapack.size() + 1;
        for (CrucibleRecipeType recipe : datapack) {
            fingerprint = 31 * fingerprint + recipe.getId().hashCode();
        }
        if (fingerprint == bridgedCrucibleFingerprint && !bridgedCrucibleKeys.isEmpty()) {
            return;
        }
        java.util.Map<net.minecraft.resources.ResourceLocation, arcaneresearch.api.crafting.IArcaneresearchRecipe> converted =
                new java.util.HashMap<>();
        for (CrucibleRecipeType type : datapack) {
            try {
                if (type.getResultItem().isEmpty() || type.getCatalyst() == null) continue;
                AspectList aspects = type.getAspects() != null ? type.getAspects().copy() : new AspectList();
                arcaneresearch.api.crafting.CrucibleRecipe recipe = new arcaneresearch.api.crafting.CrucibleRecipe(
                        type.getResearch() != null ? type.getResearch() : "",
                        type.getResultItem().copy(),
                        type.getCatalyst(),
                        aspects);
                recipe.setGroup(type.getGroup() != null ? type.getGroup() : "");
                converted.put(type.getId(), recipe);
            } catch (Exception e) {
                arcaneresearch.Arcaneresearch.LOGGER.warn("Skipping invalid crucible recipe {}: {}",
                        type.getId(), e.toString());
            }
        }
        java.util.Map<net.minecraft.resources.ResourceLocation, arcaneresearch.api.crafting.IArcaneresearchRecipe> catalog =
                arcaneresearch.api.ArcaneresearchApi.getCraftingRecipes();
        synchronized (catalog) {
            for (net.minecraft.resources.ResourceLocation key : bridgedCrucibleKeys) {
                catalog.remove(key);
            }
            bridgedCrucibleKeys.clear();
            catalog.putAll(converted);
            bridgedCrucibleKeys.addAll(converted.keySet());
            bridgedCrucibleFingerprint = fingerprint;
        }
    }
    public static InfusionRecipeType findMatchingInfusionRecipe(List<ItemStack> pedestalItems,
                                                                ItemStack centralItem,
                                                                Player player,
                                                                Level level) {
        if (centralItem.isEmpty() || level == null || player == null) {
            return null;
        }
        for (InfusionRecipeType infusionRecipe : dynamicInfusionRecipes) {
            if (infusionRecipe.matchesInfusion(pedestalItems, centralItem, level, player)) {
                return infusionRecipe;
            }
        }
        for (Recipe<?> recipe : level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.INFUSION.get())) {
            if (recipe instanceof InfusionRecipeType infusionRecipe) {
                if (infusionRecipe.matchesInfusion(pedestalItems, centralItem, level, player)) {
                    return infusionRecipe;
                }
            }
        }
        try {
            for (java.util.Map.Entry<net.minecraft.resources.ResourceLocation,
                    arcaneresearch.api.crafting.IArcaneresearchRecipe> entry :
                    arcaneresearch.api.ArcaneresearchApi.getCraftingRecipes().entrySet()) {
                if (entry.getValue() instanceof arcaneresearch.api.crafting.InfusionRecipe legacy) {
                    LegacyInfusionRecipeAdapter adapter =
                            new LegacyInfusionRecipeAdapter(entry.getKey(), legacy);
                    if (adapter.matchesInfusion(pedestalItems, centralItem, level, player)) {
                        return adapter;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
    public static AspectList getObjectTags(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return new AspectList();
        }
        AspectList aspects = AspectHelper.getObjectAspects(stack);
        if (stack.getItem() instanceof IEssentiaContainerItem container) {
            if (!container.ignoreContainedAspects()) {
                AspectList contained = container.getAspects(stack);
                if (contained != null && contained.size() > 0) {
                    if (aspects == null) {
                        aspects = contained;
                    } else {
                        AspectList merged = new AspectList();
                        for (Aspect tag : contained.getAspects()) {
                            merged.add(tag, contained.getAmount(tag));
                        }
                        for (Aspect tag : aspects.getAspects()) {
                            merged.add(tag, aspects.getAmount(tag));
                        }
                        aspects = merged;
                    }
                }
            }
        }
        if (aspects == null) {
            aspects = generateTags(stack);
        }
        aspects = getBonusTags(stack, aspects);
        return capAspects(aspects, ASPECT_CAP);
    }
    public static void registerObjectAspects(ItemStack stack, AspectList aspects) {
        if (stack != null && !stack.isEmpty() && aspects != null) {
            AspectHelper.registerObjectTag(stack, aspects);
        }
    }
    public static AspectList generateTags(ItemStack stack) {
        return generateTags(stack, new java.util.ArrayList<>());
    }
    public static AspectList generateTags(ItemStack stack, java.util.ArrayList<String> history) {
        if (history == null) {
            history = new java.util.ArrayList<>();
        }
        if (stack == null || stack.isEmpty()) {
            return new AspectList();
        }
        ItemStack sc = stack.copy();
        sc.setCount(1);
        if (arcaneresearch.api.ArcaneresearchApi.exists(sc)) {
            AspectList known = AspectHelper.getObjectAspects(sc);
            if (known != null) {
                return known;
            }
        }
        String ss = sc.save(new net.minecraft.nbt.CompoundTag()).toString();
        if (history.contains(ss)) {
            return null;
        }
        history.add(ss);
        if (history.size() < 100) {
            AspectList ret = generateTagsFromRecipes(sc, history);
            ret = capAspects(ret, ASPECT_CAP);
            arcaneresearch.api.ArcaneresearchApi.registerObjectTag(stack, ret);
            return ret;
        }
        return null;
    }
    private static AspectList getTagsRecursive(ItemStack stack, java.util.ArrayList<String> history) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        AspectList registered = AspectHelper.getObjectAspects(stack);
        if (registered != null) {
            return capAspects(getBonusTags(stack, registered), ASPECT_CAP);
        }
        return generateTags(stack, history);
    }
    private static AspectList generateTagsFromCrucibleRecipes(ItemStack stack,
                                                              java.util.ArrayList<String> history) {
        arcaneresearch.api.crafting.CrucibleRecipe cr =
                arcaneresearch.api.ArcaneresearchApi.getCrucibleRecipe(stack);
        if (cr == null) {
            return null;
        }
        AspectList ot = cr.getAspects().copy();
        int ss = Math.max(1, cr.getRecipeOutput().getCount());
        ItemStack[] matches = cr.getCatalyst().getItems();
        ItemStack cat = matches.length > 0 ? matches[0] : ItemStack.EMPTY;
        if (cat == null || cat.isEmpty()) {
            return null;
        }
        AspectList ot2 = getTagsRecursive(cat, history);
        AspectList out = new AspectList();
        if (ot2 != null && ot2.size() > 0) {
            for (Aspect tt : ot2.getAspects()) {
                out.add(tt, ot2.getAmount(tt));
            }
        }
        for (Aspect tt : ot.getAspects()) {
            int amt = (int) (Math.sqrt(ot.getAmount(tt)) / ss);
            out.add(tt, amt);
        }
        for (Aspect as : out.getAspects()) {
            if (out.getAmount(as) <= 0) {
                out.remove(as);
            }
        }
        return out;
    }
    private static AspectList generateTagsFromInfusionRecipes(ItemStack stack,
                                                              java.util.ArrayList<String> history) {
        arcaneresearch.api.crafting.InfusionRecipe cr =
                arcaneresearch.api.ArcaneresearchApi.getInfusionRecipe(stack);
        if (cr != null && cr.getRecipeOutput() instanceof ItemStack output) {
            AspectList ot = cr.getAspects().copy();
            NonNullList<Ingredient> ingredients = NonNullList.create();
            ingredients.add(cr.getRecipeInput());
            ingredients.addAll(cr.getComponents());
            AspectList out = new AspectList();
            AspectList ot2 = getAspectsFromIngredients(ingredients, output, history);
            for (Aspect tt : ot2.getAspects()) {
                out.add(tt, ot2.getAmount(tt));
            }
            for (Aspect tt : ot.getAspects()) {
                int amt = (int) (Math.sqrt(ot.getAmount(tt)) / Math.max(1, output.getCount()));
                out.add(tt, amt);
            }
            for (Aspect as : out.getAspects()) {
                if (out.getAmount(as) <= 0) {
                    out.remove(as);
                }
            }
            return out;
        }
        return null;
    }
    private static AspectList generateTagsFromCraftingRecipes(ItemStack stack,
                                                              java.util.ArrayList<String> history) {
        Level level = craftingLevel();
        if (level == null) {
            return null;
        }
        AspectList ret = null;
        int value = Integer.MAX_VALUE;
        for (net.minecraft.world.item.crafting.Recipe<?> recipe :
                level.getRecipeManager().getRecipes()) {
            ItemStack output;
            NonNullList<Ingredient> ingredients;
            int vis = 0;
            if (recipe instanceof net.minecraft.world.item.crafting.CraftingRecipe crafting) {
                output = crafting.getResultItem(level.registryAccess());
                ingredients = crafting.getIngredients();
            } else if (recipe instanceof IArcaneRecipe arcane) {
                output = arcane.getResultItem(level.registryAccess());
                ingredients = arcane.getIngredients();
                vis = arcane.getVis();
            } else {
                continue;
            }
            if (output == null || output.isEmpty() || output.getItem() != stack.getItem()) {
                continue;
            }
            AspectList ph = new AspectList();
            try {
                ph = getAspectsFromIngredients(ingredients, output, history);
                if (vis > 0) {
                    ph.add(Aspect.MAGIC,
                            (int) (Math.sqrt(1 + vis / 2.0) / (float) Math.max(1, output.getCount())));
                }
                for (Aspect as : ph.copy().getAspects()) {
                    if (ph.getAmount(as) <= 0) {
                        ph.remove(as);
                    }
                }
                if (ph.visSize() >= value || ph.visSize() <= 0) {
                    continue;
                }
                ret = ph;
                value = ph.visSize();
            } catch (Exception ignored) {
            }
        }
        return ret;
    }
    private static AspectList getAspectsFromIngredients(NonNullList<Ingredient> ingredients,
                                                        ItemStack recipeOut,
                                                        java.util.ArrayList<String> history) {
        AspectList out = new AspectList();
        AspectList mid = new AspectList();
        NonNullList<ItemStack> remainders = NonNullList.create();
        for (Ingredient ingredient : ingredients) {
            ItemStack[] stacks = ingredient.getItems();
            if (stacks.length <= 0) {
                continue;
            }
            ItemStack remainder =
                    net.minecraftforge.common.ForgeHooks.getCraftingRemainingItem(stacks[0]);
            if (remainder != null && !remainder.isEmpty()) {
                remainders.add(remainder);
            }
            AspectList obj = getTagsRecursive(stacks[0], history);
            if (obj == null) {
                continue;
            }
            for (Aspect as : obj.getAspects()) {
                if (as != null) {
                    mid.add(as, obj.getAmount(as));
                }
            }
        }
        for (ItemStack remainder : remainders) {
            if (!remainder.isEmpty()) {
                AspectList obj = getTagsRecursive(remainder, history);
                if (obj == null) {
                    continue;
                }
                for (Aspect as : obj.getAspects()) {
                    mid.reduce(as, obj.getAmount(as));
                }
            }
        }
        for (Aspect as : mid.getAspects()) {
            if (as != null) {
                float v = mid.getAmount(as) * 0.75f / Math.max(1, recipeOut.getCount());
                if (v < 1.0f && v > 0.75f) {
                    v = 1.0f;
                }
                out.add(as, (int) v);
            }
        }
        for (Aspect as : out.getAspects()) {
            if (out.getAmount(as) <= 0) {
                out.remove(as);
            }
        }
        return out;
    }
    private static AspectList generateTagsFromRecipes(ItemStack stack,
                                                      java.util.ArrayList<String> history) {
        AspectList ret = generateTagsFromCrucibleRecipes(stack, history);
        if (ret != null) {
            return ret;
        }
        ret = generateTagsFromInfusionRecipes(stack, history);
        if (ret != null) {
            return ret;
        }
        ret = generateTagsFromCraftingRecipes(stack, history);
        if (ret != null) {
            return ret;
        }
        AspectList fallback = new AspectList();
        fallback.add(Aspect.ENTROPY, 1);
        return fallback;
    }
    private static Level craftingLevel() {
        try {
            net.minecraft.server.MinecraftServer server =
                    net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                return server.getLevel(Level.OVERWORLD);
            }
        } catch (Exception ignored) {
        }
        return null;
    }
    private static AspectList getBonusTags(ItemStack stack, AspectList sourceTags) {
        AspectList result = new AspectList();
        if (stack == null || stack.isEmpty()) {
            return result;
        }
        if (sourceTags != null) {
            for (Aspect aspect : sourceTags.getAspects()) {
                if (aspect != null) {
                    result.add(aspect, sourceTags.getAmount(aspect));
                }
            }
        }
        net.minecraft.world.item.Item item = stack.getItem();
        if (item instanceof net.minecraft.world.item.ArmorItem armorItem) {
            int defense = armorItem.getDefense();
            if (defense > 0) {
                result.merge(Aspect.PROTECT, defense * 4);
            }
        }
        if (item instanceof net.minecraft.world.item.SwordItem swordItem) {
            float damage = swordItem.getDamage();
            if (damage > 0) {
                result.merge(Aspect.AVERSION, (int) (damage * 4));
            }
        }
        if (item instanceof net.minecraft.world.item.BowItem) {
            result.merge(Aspect.AVERSION, 10);
            result.merge(Aspect.FLIGHT, 5);
        }
        if (item instanceof net.minecraft.world.item.TieredItem tieredItem) {
            int tier = tieredItem.getTier().getLevel();
            result.merge(Aspect.TOOL, (tier + 1) * 4);
        }
        var enchantments = stack.getEnchantmentTags();
        if (enchantments != null && !enchantments.isEmpty()) {
            result.merge(Aspect.MAGIC, enchantments.size() * 3);
        }
        if (item.isEdible()) {
            var food = item.getFoodProperties();
            if (food != null) {
                result.merge(Aspect.LIFE, food.getNutrition());
                if (food.getSaturationModifier() > 0.5f) {
                    result.merge(Aspect.DESIRE, 2);
                }
            }
        }
        return result;
    }
    private static AspectList capAspects(AspectList source, int maxAmount) {
        if (source == null) {
            return new AspectList();
        }
        AspectList result = new AspectList();
        for (Aspect aspect : source.getAspects()) {
            if (aspect != null) {
                int amount = Math.min(maxAmount, source.getAmount(aspect));
                result.add(aspect, amount);
            }
        }
        return result;
    }
}
