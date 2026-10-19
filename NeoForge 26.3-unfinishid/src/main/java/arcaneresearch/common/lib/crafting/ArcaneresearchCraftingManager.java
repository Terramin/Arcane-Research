package arcaneresearch.common.lib.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
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

    // NEOFORGE_PORT (26.3): Level.getRecipeManager() + RecipeManager.recipeMap() both
    // gone. Server path -> ServerLevel.recipeAccess()/server RecipeManager (both expose
    // getRecipes()); client path -> ClientRecipeCache (RecipesReceivedEvent RecipeMap).
    static java.util.Collection<net.minecraft.world.item.crafting.RecipeHolder<?>> allHolders(Level level) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            return serverLevel.recipeAccess().getRecipes();
        }
        if (level != null && level.getServer() != null) {
            return level.getServer().getRecipeManager().getRecipes();
        }
        return ClientRecipeCache.values();
    }

    static <I extends net.minecraft.world.item.crafting.RecipeInput, T extends net.minecraft.world.item.crafting.Recipe<I>>
            java.util.Collection<net.minecraft.world.item.crafting.RecipeHolder<T>> holdersOfType(
            Level level, net.minecraft.world.item.crafting.RecipeType<T> type) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            return serverLevel.recipeAccess().getRecipes().stream()
                    .filter(h -> h.value().getType() == type)
                    .map(h -> (net.minecraft.world.item.crafting.RecipeHolder<T>) h)
                    .toList();
        }
        if (level != null && level.getServer() != null) {
            return level.getServer().getRecipeManager().getRecipes().stream()
                    .filter(h -> h.value().getType() == type)
                    .map(h -> (net.minecraft.world.item.crafting.RecipeHolder<T>) h)
                    .toList();
        }
        return ClientRecipeCache.byType(type);
    }

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

        // NEOFORGE_PORT: getAllRecipesFor gone in 26.x -> recipeMap().byType(type)
        // returns Collection<RecipeHolder<T>>; research-gating logic VERBATIM.
        for (net.minecraft.world.item.crafting.RecipeHolder<IArcaneRecipe> holder :
                holdersOfType(level, ModRecipeTypes.ARCANE_WORKBENCH.get())) {
            IArcaneRecipe arcaneRecipe = holder.value();
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

        IArcaneRecipe wandRecipe = ArcaneWandDynamicRecipe.match(matrix, player);
        if (wandRecipe != null) {
            return wandRecipe;
        }

        return null;
    }

    private static boolean isWandGatedRecipe(Identifier id) {
        if (id == null || !"arcaneresearch".equals(id.getNamespace())) {
            return false;
        }
        String path = id.getPath();
        return path.startsWith("wand_cap_") || path.equals("wand_wood_iron");
    }

    public static ItemStack findMatchingArcaneRecipeResult(CraftingContainer matrix, Player player) {
        IArcaneRecipe recipe = findMatchingArcaneRecipe(matrix, player);
        if (recipe != null && matrix instanceof IArcaneWorkbench workbench) {
            return recipe.assemble(workbench);
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

        // NEOFORGE_PORT: getAllRecipesFor gone in 26.x -> recipeMap().byType(type)
        // + holder.value(); research-gating logic VERBATIM.
        for (net.minecraft.world.item.crafting.RecipeHolder<CrucibleRecipeType> holder :
                holdersOfType(level, ModRecipeTypes.CRUCIBLE.get())) {
            CrucibleRecipeType crucibleRecipe = holder.value();
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

        return null;
    }

    public static List<CrucibleRecipeType> findCrucibleRecipesForCatalyst(ItemStack catalyst, Level level) {
        return new java.util.ArrayList<>(holdersOfType(level, ModRecipeTypes.CRUCIBLE.get()))
                .stream()
                .map(net.minecraft.world.item.crafting.RecipeHolder::value)
                .filter(recipe -> recipe.catalystMatches(catalyst))
                .toList();
    }

    private static int bridgedCrucibleFingerprint = 0;
    private static final java.util.Set<net.minecraft.resources.Identifier> bridgedCrucibleKeys =
            new java.util.HashSet<>();

    /**
     * The thaumatorium tile works with code-registered {@link arcaneresearch.api.crafting.CrucibleRecipe}
     * entries, while content lives in datapack {@link CrucibleRecipeType} JSONs. This bridge converts
     * the datapack registry into the code catalog (replacing only its own previous output), so every
     * JSON crucible recipe automatically works in the thaumatorium, including addon datapacks.
     * Cheap no-op unless the datapack set changed (fingerprint gate).
     */
    public static void ensureCrucibleBridge(@javax.annotation.Nullable Level level) {
        if (level == null) return;
        java.util.List<net.minecraft.world.item.crafting.RecipeHolder<CrucibleRecipeType>> datapack;
        try {
            datapack = new java.util.ArrayList<>(holdersOfType(level, ModRecipeTypes.CRUCIBLE.get()));
        } catch (Exception e) {
            return;
        }
        int fingerprint = datapack.size() + 1;
        for (net.minecraft.world.item.crafting.RecipeHolder<CrucibleRecipeType> holder : datapack) {
            fingerprint = 31 * fingerprint + holder.id().identifier().hashCode();
        }
        if (fingerprint == bridgedCrucibleFingerprint && !bridgedCrucibleKeys.isEmpty()) {
            return;
        }
        java.util.Map<net.minecraft.resources.Identifier, arcaneresearch.api.crafting.IArcaneresearchRecipe> converted =
                new java.util.HashMap<>();
        for (net.minecraft.world.item.crafting.RecipeHolder<CrucibleRecipeType> holder : datapack) {
            try {
                CrucibleRecipeType type = holder.value();
                if (type.getResultItem().isEmpty() || type.getCatalyst() == null) continue;
                AspectList aspects = type.getAspects() != null ? type.getAspects().copy() : new AspectList();
                arcaneresearch.api.crafting.CrucibleRecipe recipe = new arcaneresearch.api.crafting.CrucibleRecipe(
                        type.getResearch() != null ? type.getResearch() : "",
                        type.getResultItem().copy(),
                        type.getCatalyst(),
                        aspects);
                recipe.setGroup(type.getGroup() != null ? type.getGroup() : "");
                converted.put(holder.id().identifier(), recipe);
            } catch (Exception e) {
                arcaneresearch.Arcaneresearch.LOGGER.warn("Skipping invalid crucible recipe {}: {}",
                        holder.id().identifier(), e.toString());
            }
        }
        java.util.Map<net.minecraft.resources.Identifier, arcaneresearch.api.crafting.IArcaneresearchRecipe> catalog =
                arcaneresearch.api.ArcaneresearchApi.getCraftingRecipes();
        synchronized (catalog) {
            for (net.minecraft.resources.Identifier key : bridgedCrucibleKeys) {
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

        // NEOFORGE_PORT: getAllRecipesFor gone in 26.x -> recipeMap().byType(type)
        // + holder.value(); matching logic VERBATIM.
        for (net.minecraft.world.item.crafting.RecipeHolder<InfusionRecipeType> holder :
                holdersOfType(level, ModRecipeTypes.INFUSION.get())) {
            InfusionRecipeType infusionRecipe = holder.value();
            if (infusionRecipe.matchesInfusion(pedestalItems, centralItem, level, player)) {
                return infusionRecipe;
            }
        }

        try {
            for (java.util.Map.Entry<net.minecraft.resources.Identifier,
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

        // Парити ThaumcraftCraftingManager.getBonusTags (1.7.10): NBT-аспекты
        // IEssentiaContainerItem (висп-эссенция, фиал, кристалл) объединяются
        // с зарегистрированными тегами (складываются), а при отсутствии
        // зарегистрированных — становятся основным содержимым
        // (прежнее поведение порта сохранено).
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
        // NEOFORGE_PORT: ItemStack.save(CompoundTag) gone -> CODEC encoding
        // (same purpose: stable per-stack history key).
        String ss = ItemStack.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, sc)
                .result().map(Object::toString).orElse("");
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
        // NEOFORGE_PORT: Ingredient#getItems():ItemStack[] removed in 26.x; items()
        // exposes Holder<Item> (works for custom ingredients too).
        List<ItemStack> matches = cr.getCatalyst().items()
                .map(holder -> new ItemStack(holder.value())).toList();
        ItemStack cat = !matches.isEmpty() ? matches.get(0) : ItemStack.EMPTY;
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
        // NEOFORGE_PORT: getRecipes() gone with the holder rework -> recipeMap().values()
        // + holder.value(). Recipe no longer exposes getIngredients()/getResultItem(
        // RegistryAccess): arcane recipes use the IArcaneRecipe legacy helpers;
        // vanilla grid recipes use placementInfo().ingredients() for inputs and a
        // static assemble(CraftingInput.EMPTY) for the output (shaped/shapeless ignore
        // the input; specials that need input return EMPTY and are skipped below).
        for (net.minecraft.world.item.crafting.RecipeHolder<?> holder :
                allHolders(level)) {
            net.minecraft.world.item.crafting.Recipe<?> recipe = holder.value();
            ItemStack output;
            List<Ingredient> ingredients;
            int vis = 0;
            if (recipe instanceof IArcaneRecipe arcane) {
                output = arcane.getResultItem();
                ingredients = arcane.getIngredients();
                vis = arcane.getVis();
            } else if (recipe instanceof net.minecraft.world.item.crafting.CraftingRecipe crafting) {
                ingredients = crafting.placementInfo().ingredients();
                ItemStack assembled;
                try {
                    assembled = crafting.assemble(net.minecraft.world.item.crafting.CraftingInput.EMPTY);
                } catch (Exception e) {
                    continue;
                }
                output = assembled;
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

    private static AspectList getAspectsFromIngredients(List<Ingredient> ingredients,
                                                        ItemStack recipeOut,
                                                        java.util.ArrayList<String> history) {
        AspectList out = new AspectList();
        AspectList mid = new AspectList();
        List<ItemStack> remainders = new java.util.ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            // NEOFORGE_PORT: Ingredient#getItems():ItemStack[] removed; first display
            // holder rebuilt as a stack (same representative-input sampling as before).
            ItemStack[] stacks = ingredient.items()
                    .map(holder -> new ItemStack(holder.value())).toArray(ItemStack[]::new);
            if (stacks.length <= 0) {
                continue;
            }

            ItemStack remainder =
                    arcaneresearch.common.lib.events.CraftingEvents.getCraftingRemainingItem(stacks[0]);
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
                    net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
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

        // NEOFORGE_PORT: ArmorItem/SwordItem/TieredItem classes removed in 26.x; armor and
        // weapon stats now come from the EQUIPPABLE/ATTRIBUTE_MODIFIERS/TOOL components
        // (identical numbers for vanilla gear; same aspect yields as the Forge code).
        var equippable = stack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE);
        var modifiers = stack.get(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS);
        if (equippable != null && modifiers != null) {
            int defense = (int) modifiers.compute(
                    net.minecraft.world.entity.ai.attributes.Attributes.ARMOR, 0.0, equippable.slot());
            if (defense > 0) {
                result.merge(Aspect.PROTECT, defense * 4);
            }
        }

        if (stack.is(net.minecraft.tags.ItemTags.SWORDS)) {
            double damage = modifiers != null ? modifiers.compute(
                    net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, 1.0,
                    net.minecraft.world.entity.EquipmentSlot.MAINHAND) : 1.0;
            if (damage > 0) {
                result.merge(Aspect.AVERSION, (int) (damage * 4));
            }
        }

        if (item instanceof net.minecraft.world.item.BowItem) {
            result.merge(Aspect.AVERSION, 10);
            result.merge(Aspect.FLIGHT, 5);
        }

        var tool = stack.get(net.minecraft.core.component.DataComponents.TOOL);
        if (tool != null) {
            // Old formula was (tier + 1) * 4; mining speed (wood/stone/iron/diamond = 2/4/6/8)
            // times 2 gives identical yields for the vanilla tiers.
            result.merge(Aspect.TOOL, (int) (tool.defaultMiningSpeed() * 2));
        }

        var enchantments = stack.getEnchantments();
        if (!enchantments.isEmpty()) {

            result.merge(Aspect.MAGIC, enchantments.size() * 3);
        }

        var food = stack.get(net.minecraft.core.component.DataComponents.FOOD);
        if (food != null) {
            // NEOFORGE_PORT: FoodProperties is a record in 26.x -> nutrition()/saturation().
            result.merge(Aspect.LIFE, food.nutrition());
            if (food.saturation() > 0.5f) {
                result.merge(Aspect.DESIRE, 2);
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
