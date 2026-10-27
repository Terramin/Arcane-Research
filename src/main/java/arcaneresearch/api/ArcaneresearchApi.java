package arcaneresearch.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.resources.Identifier;
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

public class ArcaneresearchApi {

    public static IInternalMethodHandler internalMethods = new DummyInternalMethodHandler();

    public static void registerResearchLocation(Identifier loc) {
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

    public static Map<Identifier, IArcaneresearchRecipe> getCraftingRecipes() {
        return CommonInternals.craftingRecipeCatalog;
    }

    public static Map<Identifier, Object> getCraftingRecipesFake() {
        return CommonInternals.craftingRecipeCatalogFake;
    }

    public static void addFakeCraftingRecipe(Identifier registry, Object recipe) {
        getCraftingRecipesFake().put(registry, recipe);
    }

    public static void addMultiblockRecipeToCatalog(Identifier registry, BluePrint recipe) {
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

        public BluePrint setGroup(Identifier loc) {
            group = loc.toString();
            return this;
        }
    }

    public static void addArcaneCraftingRecipe(Identifier registry, IArcaneRecipe recipe) {
        getCraftingRecipes().put(registry, recipe);
    }

    public static void addInfusionCraftingRecipe(Identifier registry, InfusionRecipe recipe) {
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

    public static void addCrucibleRecipe(Identifier registry, CrucibleRecipe recipe) {
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

    public static void registerObjectTag(String tagName, AspectList aspects) {
        try {
            Identifier tagLoc = Identifier.tryParse(tagName);
            net.minecraft.tags.TagKey<Item> tagKey = net.minecraft.tags.ItemTags.create(tagLoc);
            
            
            for (var holder : net.minecraft.core.registries.BuiltInRegistries.ITEM.getTagOrEmpty(tagKey)) {
                registerObjectTag(new ItemStack(holder.value()), aspects.copy());
            }
        } catch (Exception e) {}
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
        try {
            Identifier tagLoc = Identifier.parse(tagName);
            net.minecraft.tags.TagKey<Item> tagKey = net.minecraft.tags.ItemTags.create(tagLoc);
            
            for (var holder : net.minecraft.core.registries.BuiltInRegistries.ITEM.getTagOrEmpty(tagKey)) {
                registerComplexObjectTag(new ItemStack(holder.value()), aspects.copy());
            }
        } catch (Exception e) {}
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
                AspectHelper.registerEntityTag(Identifier.parse(entityName), aspects);
            } catch (Exception e) {}
        }
    }

      
    public static final java.util.List<IScanEventHandler> scanEventhandlers = new java.util.ArrayList<>();

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
