package arcaneresearch.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import arcaneresearch.api.aspects.AspectHelper;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.crafting.CrucibleRecipe;
import arcaneresearch.api.crafting.IArcaneRecipe;
import arcaneresearch.api.crafting.IArcaneresearchRecipe;
import arcaneresearch.api.crafting.InfusionRecipe;
import arcaneresearch.api.crafting.Part;
import arcaneresearch.api.internal.CommonInternals;
import arcaneresearch.api.internal.DummyInternalMethodHandler;
import arcaneresearch.api.internal.IInternalMethodHandler;
import arcaneresearch.api.internal.WeightedRandomLoot;
import arcaneresearch.api.research.IScanEventHandler;
import net.minecraftforge.registries.ForgeRegistries;

public class ArcaneresearchApi {
    public static IInternalMethodHandler internalMethods = new DummyInternalMethodHandler();

    private record PendingTag(String name, AspectList aspects, boolean complex) {
    }

    /** String tag registrations are deferred until Forge binds the item tags. */
    private static final List<PendingTag> PENDING_TAGS = new ArrayList<>();

    public static void registerResearchLocation(ResourceLocation loc) {
        if (!CommonInternals.jsonLocs.containsKey(loc.toString())) {
            CommonInternals.jsonLocs.put(loc.toString(), loc);
        }
    }

    public static class SmeltBonus {
        public Object in;
        public ItemStack out;
        public float chance;

        public SmeltBonus(Object in, ItemStack out, float chance) {
            this.in = in;
            this.out = out;
            this.chance = chance;
        }
    }

    public static void addSmeltingBonus(Object in, ItemStack out, float chance) {
        if (in instanceof ItemStack || in instanceof String) {
            CommonInternals.smeltingBonus.add(new SmeltBonus(in, out, chance));
        }
    }

    public static void addSmeltingBonus(Object in, ItemStack out) {
        if (in instanceof ItemStack || in instanceof String) {
            CommonInternals.smeltingBonus.add(new SmeltBonus(in, out, 0.33f));
        }
    }

    public static Map<ResourceLocation, IArcaneresearchRecipe> getCraftingRecipes() {
        return CommonInternals.craftingRecipeCatalog;
    }

    public static Map<ResourceLocation, Object> getCraftingRecipesFake() {
        return CommonInternals.craftingRecipeCatalogFake;
    }

    public static void addFakeCraftingRecipe(ResourceLocation registry, Object recipe) {
        getCraftingRecipesFake().put(registry, recipe);
    }

    public static void addMultiblockRecipeToCatalog(ResourceLocation registry, BluePrint recipe) {
        getCraftingRecipes().put(registry, recipe);
    }

    public static class BluePrint implements IArcaneresearchRecipe {
        Part[][][] parts;
        String research;
        ItemStack displayStack;
        ItemStack[] ingredientList;
        private String group;

        public BluePrint(String research, Part[][][] parts, ItemStack... ingredientList) {
            this.parts = parts;
            this.research = research;
            this.ingredientList = ingredientList;
        }

        public BluePrint(String research, ItemStack display, Part[][][] parts, ItemStack... ingredientList) {
            this.parts = parts;
            this.research = research;
            this.displayStack = display;
            this.ingredientList = ingredientList;
        }

        public Part[][][] getParts() {
            return parts;
        }

        @Override
        public String getResearch() {
            return research;
        }

        public ItemStack[] getIngredientList() {
            return ingredientList;
        }

        public ItemStack getDisplayStack() {
            return displayStack;
        }

        public String getGroup() {
            return group;
        }

        public BluePrint setGroup(ResourceLocation loc) {
            group = loc.toString();
            return this;
        }
    }

    public static void addArcaneCraftingRecipe(ResourceLocation registry, IArcaneRecipe recipe) {
        getCraftingRecipes().put(registry, recipe);
    }

    public static void addInfusionCraftingRecipe(ResourceLocation registry, InfusionRecipe recipe) {
        getCraftingRecipes().put(registry, recipe);
    }

    public static InfusionRecipe getInfusionRecipe(ItemStack res) {
        for (Object r : getCraftingRecipes().values()) {
            if (r instanceof InfusionRecipe infusionRecipe) {
                if (infusionRecipe.getRecipeOutput() instanceof ItemStack output) {
                    if (ItemStack.isSameItem(output, res)) {
                        return infusionRecipe;
                    }
                }
            }
        }
        return null;
    }

    public static void addCrucibleRecipe(ResourceLocation registry, CrucibleRecipe recipe) {
        getCraftingRecipes().put(registry, recipe);
    }

    public static CrucibleRecipe getCrucibleRecipe(ItemStack stack) {
        for (Object r : getCraftingRecipes().values()) {
            if (r instanceof CrucibleRecipe crucibleRecipe) {
                if (ItemStack.isSameItem(crucibleRecipe.getRecipeOutput(), stack)) {
                    return crucibleRecipe;
                }
            }
        }
        return null;
    }

    public static CrucibleRecipe getCrucibleRecipeFromHash(int hash) {
        for (Object recipe : getCraftingRecipes().values()) {
            if (recipe instanceof CrucibleRecipe crucibleRecipe && crucibleRecipe.hash == hash) {
                return crucibleRecipe;
            }
        }
        return null;
    }

    public static boolean exists(ItemStack item) {
        if (item == null || item.isEmpty()) return false;
        return AspectHelper.getObjectAspects(item) != null;
    }

    public static void registerObjectTag(ItemStack item, AspectList aspects) {
        if (item == null || item.isEmpty()) return;
        if (aspects == null) aspects = new AspectList();
        AspectHelper.registerObjectTag(item, aspects);
    }

    /**
     * Registers aspects for every item in a named item tag, deferring until tags
     * are bound if called during common setup.
     *
     * @param tagName item tag id
     * @param aspects aspects to register (copied)
     */
    public static void registerObjectTag(String tagName, AspectList aspects) {
        queueTag(tagName, aspects, false);
    }

    /**
     * Queues or immediately applies a tag registration.
     *
     * @param complex whether to merge with existing aspects instead of replacing
     */
    private static void queueTag(String tagName, AspectList aspects, boolean complex) {
        if (tagName == null || aspects == null) {
            return;
        }
        ResourceLocation tagLoc = ResourceLocation.tryParse(tagName);
        if (tagLoc == null) {
            arcaneresearch.Arcaneresearch.LOGGER.warn("Invalid aspect tag name: {}", tagName);
            return;
        }
        TagKey<Item> tagKey = ItemTags.create(tagLoc);
        try {
            if (!ForgeRegistries.ITEMS.tags().getTag(tagKey).isBound()) {
                // Tags are bound after common setup; retry on TagsUpdatedEvent.
                PENDING_TAGS.add(new PendingTag(tagName, aspects.copy(), complex));
                return;
            }
        } catch (Exception e) {
            arcaneresearch.Arcaneresearch.LOGGER.warn("Cannot resolve aspect tag {}: {}", tagName, e.toString());
            return;
        }
        applyTag(tagName, tagKey, aspects, complex);
    }

    /** Applies a bound tag registration to every item it contains. */
    private static void applyTag(String tagName, TagKey<Item> tagKey, AspectList aspects, boolean complex) {
        try {
            for (Item item : ForgeRegistries.ITEMS.tags().getTag(tagKey)) {
                if (complex) {
                    registerComplexObjectTag(new ItemStack(item), aspects.copy());
                } else {
                    registerObjectTag(new ItemStack(item), aspects.copy());
                }
            }
        } catch (Exception e) {
            arcaneresearch.Arcaneresearch.LOGGER.warn("Cannot apply aspect tag {}: {}", tagName, e.toString());
        }
    }

    /** Applies registrations that were queued before item tags were bound. */
    public static void flushPendingTags() {
        if (PENDING_TAGS.isEmpty()) {
            return;
        }
        for (PendingTag pending : PENDING_TAGS) {
            ResourceLocation loc = ResourceLocation.tryParse(pending.name());
            if (loc != null) {
                applyTag(pending.name(), ItemTags.create(loc), pending.aspects(), pending.complex());
            }
        }
        PENDING_TAGS.clear();
    }

    public static void registerComplexObjectTag(ItemStack item, AspectList aspects) {
        if (item == null || item.isEmpty()) return;
        if (aspects == null) aspects = new AspectList();
        if (!exists(item)) {
            AspectList tmp = AspectHelper.getObjectAspects(item);
            if (tmp != null && tmp.size() > 0) {
                for (var tag : tmp.getAspects()) {
                    aspects.add(tag, tmp.getAmount(tag));
                }
            }
            registerObjectTag(item, aspects);
        } else {
            AspectList tmp = AspectHelper.getObjectAspects(item);
            if (tmp == null) tmp = new AspectList();
            for (var tag : aspects.getAspects()) {
                tmp.merge(tag, aspects.getAmount(tag));
            }
            registerObjectTag(item, tmp);
        }
    }

    public static void registerComplexObjectTag(String tagName, AspectList aspects) {
        queueTag(tagName, aspects, true);
    }

    public static class EntityTagsNBT {
        public String name;
        public Object value;

        public EntityTagsNBT(String name, Object value) {
            this.name = name;
            this.value = value;
        }
    }

    public static class EntityTags {
        public String entityName;
        public EntityTagsNBT[] nbts;
        public AspectList aspects;

        public EntityTags(String entityName, AspectList aspects, EntityTagsNBT... nbts) {
            this.entityName = entityName;
            this.nbts = nbts;
            this.aspects = aspects;
        }
    }

    public static void registerEntityTag(String entityName, AspectList aspects, EntityTagsNBT... nbt) {
        CommonInternals.scanEntities.add(new EntityTags(entityName, aspects, nbt));
        if (nbt == null || nbt.length == 0) {
            try {
                AspectHelper.registerEntityTag(ResourceLocation.parse(entityName), aspects);
            } catch (Exception ignored) {
            }
        }
    }

    public static final List<IScanEventHandler> scanEventhandlers = new ArrayList<>();

    public static void registerScanEventHandler(IScanEventHandler handler) {
        if (handler != null && !scanEventhandlers.contains(handler)) {
            scanEventhandlers.add(handler);
        }
    }

    public static void addWarpToItem(ItemStack craftresult, int amount) {
        if (craftresult == null || craftresult.isEmpty()) return;
        CommonInternals.warpMap.put(
                Arrays.asList(craftresult.getItem(), craftresult.getDamageValue()),
                amount
        );
    }

    public static int getWarp(ItemStack in) {
        if (in == null || in.isEmpty()) return 0;
        Integer warp = CommonInternals.warpMap.get(
                Arrays.asList(in.getItem(), in.getDamageValue())
        );
        return warp != null ? warp : 0;
    }

    public static void addLootBagItem(ItemStack item, int weight, int... bagTypes) {
        if (bagTypes == null || bagTypes.length == 0) {
            WeightedRandomLoot.lootBagCommon.add(new WeightedRandomLoot(item, weight));
        } else {
            for (int rarity : bagTypes) {
                switch (rarity) {
                    case 0 -> WeightedRandomLoot.lootBagCommon.add(new WeightedRandomLoot(item, weight));
                    case 1 -> WeightedRandomLoot.lootBagUncommon.add(new WeightedRandomLoot(item, weight));
                    case 2 -> WeightedRandomLoot.lootBagRare.add(new WeightedRandomLoot(item, weight));
                }
            }
        }
    }

    public static void registerSeed(Block block, ItemStack seed) {
        CommonInternals.seedList.put(block.getDescriptionId(), seed);
    }

    public static ItemStack getSeed(Block block) {
        return CommonInternals.seedList.get(block.getDescriptionId());
    }
}
