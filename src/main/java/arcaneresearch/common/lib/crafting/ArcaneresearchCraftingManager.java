package arcaneresearch.common.lib.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import arcaneresearch.api.ArcaneresearchApi;
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

    /**
     * Finds the available crucible recipe with the highest aspect cost.
     * Recipes found without a player are ignored, exactly as in the original.
     */
    public static CrucibleMatch findMatchingCrucibleRecipe(AspectList crucibleAspects,
                                                           ItemStack catalyst,
                                                           Player player,
                                                           Level level) {
        if (catalyst.isEmpty() || level == null || player == null) {
            return null;
        }
        ItemStack single = catalyst.copy();
        single.setCount(1);

        CrucibleMatch best = null;
        int highest = 0;

        for (Recipe<?> recipe : level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.CRUCIBLE.get())) {
            if (recipe instanceof CrucibleRecipeType crucibleRecipe
                    && crucibleRecipe.getAspects() != null
                    && knowsRecipe(player, crucibleRecipe.getResearch())
                    && crucibleRecipe.matchesCrucible(crucibleAspects, single)) {
                CrucibleMatch match = CrucibleMatch.of(crucibleRecipe);
                if (match.visSize() > highest) {
                    highest = match.visSize();
                    best = match;
                }
            }
        }

        // Programmatically registered recipes live in the legacy catalog.
        for (Object recipe : arcaneresearch.api.ArcaneresearchApi.getCraftingRecipes().values()) {
            if (recipe instanceof arcaneresearch.api.crafting.CrucibleRecipe legacy
                    && legacy.getAspects() != null
                    && knowsRecipe(player, legacy.getResearch())
                    && legacy.matches(crucibleAspects, single)) {
                CrucibleMatch match = CrucibleMatch.of(legacy);
                if (match.visSize() > highest) {
                    highest = match.visSize();
                    best = match;
                }
            }
        }
        return best;
    }

    private static boolean knowsRecipe(Player player, String research) {
        return research == null || research.isEmpty()
                || arcaneresearch.api.capabilities.ArcaneresearchCapabilities
                        .knowsResearchStrict(player, research);
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
    /** Incremented whenever the bridged catalogue is replaced, so tiles can re-resolve. */
    private static volatile int catalogVersion = 0;

    /** @return current version of the bridged crucible catalogue */
    public static int catalogVersion() {
        return catalogVersion;
    }

    public static void ensureCrucibleBridge(@javax.annotation.Nullable Level level) {
        if (level == null) return;
        java.util.List<CrucibleRecipeType> datapack;
        try {
            datapack = level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.CRUCIBLE.get());
        } catch (Exception e) {
            return;
        }
        // A client with an empty recipe manager must not clear the shared catalog.
        if (datapack.isEmpty()) return;
        // Include result and aspects: /reload may change them without changing IDs.
        int fingerprint = arcaneresearch.common.tiles.crafting.ThaumatoriumRules.recipeFingerprint(
                datapack.stream()
                        .map(recipe -> recipe.getId() + "|" + recipe.getResultItem()
                                + "|" + recipe.getAspects())
                        .map(String::hashCode)
                        .toList());
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
            catalogVersion++;
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

    /** Maximum number of *distinct* aspects kept on one item stack. */
    public static final int OBJECT_ASPECT_CAP = 10;

    /** @return all aspects of the stack, never null; uses a fresh recursion guard */
    public static AspectList getObjectTags(ItemStack stack) {
        return getObjectTags(stack, new java.util.ArrayList<>());
    }

    /**
     * Resolves all aspects of a stack, including bonuses, containers and recipes.
     *
     * @param stack item to inspect
     * @param history recursion guard shared with recipe generation; mutated with
     *                the serialized stack key on each recipe lookup
     * @return a non-null list, capped to {@link #ASPECT_CAP} per aspect and
     *         {@link #OBJECT_ASPECT_CAP} distinct aspects
     */
    public static AspectList getObjectTags(ItemStack stack, java.util.ArrayList<String> history) {
        if (stack == null || stack.isEmpty()) {
            return new AspectList();
        }
        AspectList aspects = AspectHelper.getObjectAspects(stack);
        if (stack.getItem() instanceof IEssentiaContainerItem container) {
            aspects = AspectTagRules.containerAspects(aspects, container.getAspects(stack),
                    container.ignoreContainedAspects());
        }
        if (aspects == null) {
            aspects = generateTags(stack, history);
        }
        aspects = getBonusTags(stack, aspects);
        return AspectHelper.cullTags(capAspects(aspects, ASPECT_CAP), OBJECT_ASPECT_CAP);
    }

    public static void registerObjectAspects(ItemStack stack, AspectList aspects) {
        if (stack != null && !stack.isEmpty() && aspects != null) {
            AspectHelper.registerObjectTag(stack, aspects);
        }
    }

    /**
     * Generates aspects for an item and caches the result, including an empty list
     * for unknown items.
     *
     * @param stack item to inspect
     * @return aspects, or null only for a null/empty stack
     */
    public static AspectList generateTags(ItemStack stack) {
        AspectList tags = generateTags(stack, new java.util.ArrayList<>());
        if (tags == null && stack != null && !stack.isEmpty()) {
            // The original caches an empty list so unknown items are not re-scanned
            // on every tooltip, scan or crucible tick.
            AspectList empty = new AspectList();
            ArcaneresearchApi.registerObjectTag(stack, empty);
            return empty;
        }
        return tags;
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
            if (ret == null) {
                return null;
            }
            ret = capAspects(ret, ASPECT_CAP);
            arcaneresearch.api.ArcaneresearchApi.registerObjectTag(stack, ret);
            return ret;
        }
        return null;
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
        AspectList ot2 = getObjectTags(cat, history);
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
            AspectList obj = getObjectTags(stacks[0], history);
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
                AspectList obj = getObjectTags(remainder, history);
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
        // Unknown items have no aspects; the original returns null instead of
        // inventing an ENTROPY point that would dissolve them in the crucible.
        return ret;
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

    /**
     * Adds bonus aspects from armour, weapons, enchantments and food.
     * Enchantments are read from the {@code id}/{@code lvl} NBT fields; aspects
     * contributed by several enchantments are combined with max, not sum.
     *
     * @param stack item to inspect
     * @param sourceTags aspects already resolved for the item
     */
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
        ListTag enchantments = enchantmentTags(stack);
        int magic = 0;
        for (int i = 0; i < enchantments.size(); i++) {
            CompoundTag entry = enchantments.getCompound(i);
            String id = entry.getString("id");
            EnchantmentAspectRules.Contribution contribution = EnchantmentAspectRules.contribution(
                    id, entry.getInt("lvl"), rarityBonus(id));
            for (Aspect aspect : contribution.aspects().getAspects()) {
                result.merge(aspect, contribution.aspects().getAmount(aspect));
            }
            magic += contribution.magic();
        }
        if (magic > 0) {
            result.merge(Aspect.MAGIC, magic);
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

    /**
     * Rarity bonus from the enchantment registry; unknown ids use the vanilla table.
     *
     * @param id enchantment id from NBT ({@code id} field)
     * @return 0/2/4/6 for common/uncommon/rare/very rare
     */
    private static int rarityBonus(String id) {
        ResourceLocation key = ResourceLocation.tryParse(id);
        Enchantment enchantment = key == null ? null : BuiltInRegistries.ENCHANTMENT.get(key);
        if (enchantment == null) {
            return EnchantmentAspectRules.vanillaRarityBonus(id);
        }
        return switch (enchantment.getRarity()) {
            case UNCOMMON -> 2;
            case RARE -> 4;
            case VERY_RARE -> 6;
            default -> 0;
        };
    }

    /**
     * Enchanted books store their enchantments under {@code StoredEnchantments},
     * not {@code Enchantments}; read the correct list for each item type.
     *
     * @param stack item to inspect
     * @return list of enchantment compounds, never null
     */
    private static ListTag enchantmentTags(ItemStack stack) {
        return AspectTagRules.enchantmentTag(stack.getItem() instanceof EnchantedBookItem,
                stack.getTag(), stack.getEnchantmentTags());
    }

    /**
     * Copies aspects and clamps every amount to {@code maxAmount}.
     *
     * @param source aspects to copy, may be null
     * @param maxAmount per-aspect ceiling (500 in the original)
     * @return a new, non-null list
     */
    private static AspectList capAspects(AspectList source, int maxAmount) {
        return AspectTagRules.cap(source, maxAmount);
    }
}
