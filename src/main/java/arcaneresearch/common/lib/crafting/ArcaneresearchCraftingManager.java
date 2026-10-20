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

    // NEOFORGE_PORT (26.3): 1.20.1 synced every recipe to the client; 26.x syncs none unless
    // requested. Without this ClientRecipeCache stays empty on clients (arcane workbench
    // screen/menu matching, research-page recipes, client-side crucible lookups). Requests
    // all recipe types, i.e. the Forge behavior.
    @net.neoforged.fml.common.EventBusSubscriber(modid = arcaneresearch.Arcaneresearch.MODID)
    public static final class RecipeSync {
        private RecipeSync() {
        }

        @net.neoforged.bus.api.SubscribeEvent
        public static void onDatapackSync(net.neoforged.neoforge.event.OnDatapackSyncEvent event) {
            event.sendRecipes(net.minecraft.core.registries.BuiltInRegistries.RECIPE_TYPE);
        }
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

                // NEOFORGE_PORT (26.3): datapack recipes are holder-keyed - IArcaneRecipe#getId()
                // is null for them, so the Forge recipe id comes from the holder key.
                if (arcaneRecipe.getResearch() != null && arcaneRecipe.getResearch().isEmpty()
                        && isWandGatedRecipe(arcaneRecipe.getId() != null
                                ? arcaneRecipe.getId() : holder.id().identifier())
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
        // NEOFORGE_PORT (26.3): registry-aware ops - plain NbtOps cannot encode dynamic-registry
        // components (enchantments, trims...), so every such stack got the same "" key and was
        // cut off as a false recursion cycle. Without a server (remote client) or on any other
        // encode failure the key falls back to item id + component patch, never a shared "".
        net.minecraft.server.MinecraftServer keyServer =
                net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        com.mojang.serialization.DynamicOps<net.minecraft.nbt.Tag> keyOps = keyServer != null
                ? keyServer.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE)
                : net.minecraft.nbt.NbtOps.INSTANCE;
        String ss = ItemStack.CODEC.encodeStart(keyOps, sc)
                .result().map(Object::toString)
                .orElseGet(() -> net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(sc.getItem())
                        + sc.getComponentsPatch().toString());
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
        List<ItemStack> matches = ingredientStacks(cr.getCatalyst());
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
            ItemStack[] stacks = ingredientStacks(ingredient).toArray(ItemStack[]::new);
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

    // NEOFORGE_PORT (26.3): Forge Ingredient#getItems() returned display stacks WITH their
    // NBT (crystal aspect, StrictNBT/PartialNBT data). Simple 26.x ingredients only expose
    // item holders (no data to lose); custom ones (arcaneresearch:crystal,
    // neoforge:components, ...) carry it only in display(), so that is resolved instead.
    private static List<ItemStack> ingredientStacks(Ingredient ingredient) {
        if (ingredient == null) {
            return List.of();
        }
        if (ingredient.isCustom()) {
            try {
                Level level = craftingLevel();
                net.minecraft.util.context.ContextMap context = level != null
                        ? net.minecraft.world.item.crafting.display.SlotDisplayContext.fromLevel(level)
                        : net.minecraft.util.context.ContextMap.EMPTY;
                List<ItemStack> resolved = ingredient.display().resolveForStacks(context);
                if (!resolved.isEmpty()) {
                    return resolved;
                }
            } catch (Exception ignored) {
            }
        }
        return ingredient.items().map(holder -> new ItemStack(holder.value())).toList();
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
        var tool = stack.get(net.minecraft.core.component.DataComponents.TOOL);
        // NEOFORGE_PORT (26.3): ArmorItem was humanoid armor only (no horse/wolf body armor).
        if (equippable != null && modifiers != null
                && equippable.slot().getType() == net.minecraft.world.entity.EquipmentSlot.Type.HUMANOID_ARMOR) {
            int defense = (int) modifiers.compute(
                    net.minecraft.world.entity.ai.attributes.Attributes.ARMOR, 0.0, equippable.slot());
            if (defense > 0) {
                result.merge(Aspect.PROTECT, defense * 4);
            }
        }

        // NEOFORGE_PORT (26.3): SwordItem#getDamage() was the ATTACK_DAMAGE modifier alone
        // (material bonus + base, WITHOUT the player's 1.0 base attribute) -> base 0.0.
        // SwordItem subclasses = #minecraft:swords or anything built by Item.Properties.sword()
        // (sword_efficient TOOL rule; covers this mod's swords, which are not in the tag).
        if (isSword(stack, tool)) {
            double damage = modifiers != null ? modifiers.compute(
                    net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, 0.0,
                    net.minecraft.world.entity.EquipmentSlot.MAINHAND) : 0.0;
            if (damage > 0) {
                result.merge(Aspect.AVERSION, (int) (damage * 4));
            }
        }

        if (item instanceof net.minecraft.world.item.BowItem) {
            result.merge(Aspect.AVERSION, 10);
            result.merge(Aspect.FLIGHT, 5);
        }

        // NEOFORGE_PORT (26.3): TieredItem/Tier#getLevel() are gone, and Tool#defaultMiningSpeed
        // is 1.0 for every 26.x tool (the material speed lives in a rule), so it cannot stand
        // in for the tier. The Forge level is recovered from the tool material instead; Forge
        // formula (level + 1) * 4 unchanged. Non-tiered TOOL holders (shears, mace) get nothing.
        int tierLevel = toolTierLevel(stack, tool);
        if (tierLevel >= 0) {
            result.merge(Aspect.TOOL, (tierLevel + 1) * 4);
        }

        var enchantments = stack.getEnchantments();
        if (!enchantments.isEmpty()) {

            result.merge(Aspect.MAGIC, enchantments.size() * 3);
        }

        var food = stack.get(net.minecraft.core.component.DataComponents.FOOD);
        if (food != null) {
            // NEOFORGE_PORT: FoodProperties is a record in 26.x -> nutrition()/saturation().
            result.merge(Aspect.LIFE, food.nutrition());
            // NEOFORGE_PORT (26.3): saturation() is the absolute value (nutrition * modifier * 2,
            // FoodConstants.saturationByModifier); Forge compared the MODIFIER with 0.5.
            float saturationModifier = food.nutrition() > 0
                    ? food.saturation() / (food.nutrition() * 2.0f) : 0.0f;
            if (saturationModifier > 0.5f) {
                result.merge(Aspect.DESIRE, 2);
            }
        }

        return result;
    }

    private static boolean isSword(ItemStack stack, net.minecraft.world.item.component.Tool tool) {
        if (stack.is(net.minecraft.tags.ItemTags.SWORDS)) {
            return true;
        }
        if (tool == null) {
            return false;
        }
        for (net.minecraft.world.item.component.Tool.Rule rule : tool.rules()) {
            if (rule.blocks().unwrapKey().filter(net.minecraft.tags.BlockTags.SWORD_EFFICIENT::equals).isPresent()) {
                return true;
            }
        }
        return false;
    }

    // Forge Tier#getLevel() per tool material (vanilla values; mod values from the Forge Tier
    // definitions: thaumium/elemental 3, void/crimson void 4, primal void 5). Copper did not
    // exist in 1.20.1 and mines like stone -> 1. Holder class = lazy init (no early class load).
    private static final class ToolTiers {
        static final List<java.util.Map.Entry<net.minecraft.world.item.ToolMaterial, Integer>> MATERIALS = List.of(
                java.util.Map.entry(net.minecraft.world.item.ToolMaterial.WOOD, 0),
                java.util.Map.entry(net.minecraft.world.item.ToolMaterial.STONE, 1),
                java.util.Map.entry(net.minecraft.world.item.ToolMaterial.COPPER, 1),
                java.util.Map.entry(net.minecraft.world.item.ToolMaterial.IRON, 2),
                java.util.Map.entry(net.minecraft.world.item.ToolMaterial.DIAMOND, 3),
                java.util.Map.entry(net.minecraft.world.item.ToolMaterial.GOLD, 0),
                java.util.Map.entry(net.minecraft.world.item.ToolMaterial.NETHERITE, 4),
                java.util.Map.entry(arcaneresearch.api.ArcaneresearchMaterials.TOOLMAT_THAUMIUM, 3),
                java.util.Map.entry(arcaneresearch.api.ArcaneresearchMaterials.TOOLMAT_VOID, 4),
                java.util.Map.entry(arcaneresearch.api.ArcaneresearchMaterials.TOOLMAT_ELEMENTAL, 3),
                java.util.Map.entry(arcaneresearch.api.ArcaneresearchToolMaterials.TOOLMAT_CRIMSON_VOID, 4),
                java.util.Map.entry(arcaneresearch.api.ArcaneresearchToolMaterials.TOOLMAT_PRIMAL_VOID, 5));

        static final java.util.Map<net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block>, Integer> INCORRECT_FOR =
                java.util.Map.of(
                        net.minecraft.tags.BlockTags.INCORRECT_FOR_WOODEN_TOOL, 0,
                        net.minecraft.tags.BlockTags.INCORRECT_FOR_GOLD_TOOL, 0,
                        net.minecraft.tags.BlockTags.INCORRECT_FOR_STONE_TOOL, 1,
                        net.minecraft.tags.BlockTags.INCORRECT_FOR_COPPER_TOOL, 1,
                        net.minecraft.tags.BlockTags.INCORRECT_FOR_IRON_TOOL, 2,
                        net.minecraft.tags.BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 3,
                        net.minecraft.tags.BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 4);
    }

    /**
     * Forge "instanceof TieredItem ? getTier().getLevel()" (SwordItem + DiggerItem), -1 if not
     * tiered. Diggers carry ToolMaterial.applyToolProperties' deny-drops rule on an
     * incorrect_for_*_tool tag, swords the sword rule; the material that built the item is
     * matched by its repair tag + durability (both copied verbatim from the material).
     */
    private static int toolTierLevel(ItemStack stack, net.minecraft.world.item.component.Tool tool) {
        if (tool == null) {
            return -1;
        }
        int denyLevel = -1;
        for (net.minecraft.world.item.component.Tool.Rule rule : tool.rules()) {
            if (rule.correctForDrops().isPresent() && !rule.correctForDrops().get()) {
                var key = rule.blocks().unwrapKey();
                if (key.isPresent() && ToolTiers.INCORRECT_FOR.containsKey(key.get())) {
                    denyLevel = ToolTiers.INCORRECT_FOR.get(key.get());
                }
            }
        }
        if (denyLevel < 0 && !isSword(stack, tool)) {
            return -1;
        }
        var repairable = stack.get(net.minecraft.core.component.DataComponents.REPAIRABLE);
        var repairTag = repairable != null ? repairable.items().unwrapKey() : java.util.Optional.<net.minecraft.tags.TagKey<net.minecraft.world.item.Item>>empty();
        int byTag = -1;
        if (repairTag.isPresent()) {
            for (java.util.Map.Entry<net.minecraft.world.item.ToolMaterial, Integer> entry : ToolTiers.MATERIALS) {
                if (entry.getKey().repairItems().equals(repairTag.get())) {
                    if (entry.getKey().durability() == stack.getMaxDamage()) {
                        return entry.getValue();
                    }
                    if (byTag < 0) {
                        byTag = entry.getValue();
                    }
                }
            }
        }
        if (denyLevel >= 0) {
            return denyLevel;
        }
        return Math.max(byTag, 0);
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
