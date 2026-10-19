package arcaneresearch.common.config;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.crafting.InfusionRecipe;
import arcaneresearch.common.lib.enchantment.EnumInfusionEnchantment;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModItems;

public class ConfigRecipes {

    public static void registerFakeRecipes() {
        // NEOFORGE_PORT: vanilla ShapelessRecipe ctor is now (CommonInfo, CraftingBookInfo,
        // ItemStackTemplate, List<Ingredient>) — no id/group params; Ingredient.EMPTY removed.
        // Fake recipes are display-only (thaumonomicon book), never matched against grids.
        ArcaneresearchApi.addFakeCraftingRecipe(
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "salis_mundus_fake"),
                new ShapelessRecipe(new Recipe.CommonInfo(false),
                        new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""),
                        ItemStackTemplate.fromStack(new ItemStack(ModItems.SALIS_MUNDUS.get())),
                        java.util.List.of(
                                Ingredient.of(Items.FLINT),
                                Ingredient.of(Items.BOWL),
                                Ingredient.of(Items.REDSTONE),
                                Ingredient.of(ModItems.CRYSTAL_ESSENCE.get()),
                                Ingredient.of(ModItems.CRYSTAL_ESSENCE.get()),
                                Ingredient.of(ModItems.CRYSTAL_ESSENCE.get()))));

        ArcaneresearchApi.addFakeCraftingRecipe(
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "triple_meat_treat_fake"),
                new ShapelessRecipe(new Recipe.CommonInfo(false),
                        new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""),
                        ItemStackTemplate.fromStack(new ItemStack(ModItems.TRIPLE_MEAT_TREAT.get())),
                        java.util.List.of(
                                Ingredient.of(ModItems.CHUNKS_BEEF.get()),
                                Ingredient.of(ModItems.CHUNKS_BEEF.get()),
                                Ingredient.of(ModItems.CHUNKS_BEEF.get()),
                                Ingredient.of(Items.SUGAR))));

        registerInfusionEnchantmentFake("burrowing", EnumInfusionEnchantment.BURROWING,
                new AspectList().add(Aspect.SENSES, 80).add(Aspect.EARTH, 150),
                new ItemStack(Items.RABBIT_FOOT), new ItemStack(Items.WOODEN_PICKAXE));
        registerInfusionEnchantmentFake("collector", EnumInfusionEnchantment.COLLECTOR,
                new AspectList().add(Aspect.DESIRE, 80).add(Aspect.WATER, 100),
                new ItemStack(Items.LEAD), new ItemStack(Items.STONE_AXE));
        registerInfusionEnchantmentFake("destructive", EnumInfusionEnchantment.DESTRUCTIVE,
                new AspectList().add(Aspect.AVERSION, 200).add(Aspect.ENTROPY, 250),
                new ItemStack(Items.TNT), new ItemStack(Items.STONE_PICKAXE));
        registerInfusionEnchantmentFake("refining", EnumInfusionEnchantment.REFINING,
                new AspectList().add(Aspect.ORDER, 80).add(Aspect.EXCHANGE, 60),
                new ItemStack(ModItems.SALIS_MUNDUS.get()), new ItemStack(Items.IRON_PICKAXE));
        registerInfusionEnchantmentFake("sounding", EnumInfusionEnchantment.SOUNDING,
                new AspectList().add(Aspect.SENSES, 40).add(Aspect.FIRE, 60),
                new ItemStack(Items.MAP), new ItemStack(Items.GOLDEN_PICKAXE));
        registerInfusionEnchantmentFake("arcing", EnumInfusionEnchantment.ARCING,
                new AspectList().add(Aspect.ENERGY, 40).add(Aspect.AIR, 60),
                new ItemStack(Items.REDSTONE_BLOCK), new ItemStack(Items.WOODEN_SWORD));
        registerInfusionEnchantmentFake("essence", EnumInfusionEnchantment.ESSENCE,
                new AspectList().add(Aspect.BEAST, 40).add(Aspect.FLUX, 60),
                new ItemStack(ModItems.CRYSTAL_ESSENCE.get()), new ItemStack(Items.STONE_SWORD));
        registerInfusionEnchantmentFake("lamplight", EnumInfusionEnchantment.LAMPLIGHT,
                new AspectList().add(Aspect.LIGHT, 80).add(Aspect.AIR, 20),
                new ItemStack(ModBlocks.NITOR_YELLOW.get()), new ItemStack(Items.GOLDEN_PICKAXE));

        for (int level = 0; level < 3; level++) {
            ItemStack ring = new ItemStack(ModItems.BAUBLE_RING_MUNDANE.get());
            if (level > 0) {
                // NEOFORGE_PORT: getOrCreateTag removed -> CustomData.update (same NBT shape).
                final byte runic = (byte) level;
                CustomData.update(DataComponents.CUSTOM_DATA, ring, tag -> tag.putByte("TC.RUNIC", runic));
            }
            int vis = 20 + (int) (20.0 * Math.pow(2.0, level));
            AspectList runicCost = new AspectList().add(Aspect.PROTECT, vis)
                    .add(Aspect.CRYSTAL, vis / 2).add(Aspect.ENERGY, vis / 2);
            Object[] runicComponents =
                    arcaneresearch.common.lib.crafting.InfusionRunicAugmentRecipe
                            .componentsForCharge(level).toArray();
            ArcaneresearchApi.addFakeCraftingRecipe(
                    Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "runic_armor_fake_" + level),
                    new InfusionRecipe("RUNICSHIELDING", ring, 5 + level / 2, runicCost,
                            new ItemStack(ModItems.BAUBLE_RING_MUNDANE.get()),
                            runicComponents));
        }

        Arcaneresearch.LOGGER.info("Registered {} fake thaumonomicon recipes",
                ArcaneresearchApi.getCraftingRecipesFake().size());
    }

    public static void registerSmeltingBonuses() {
        int before = arcaneresearch.api.internal.CommonInternals.smeltingBonus.size();

        ArcaneresearchApi.addSmeltingBonus("oreGold", new ItemStack(Items.GOLD_NUGGET));
        ArcaneresearchApi.addSmeltingBonus("oreIron", new ItemStack(Items.IRON_NUGGET));
        ArcaneresearchApi.addSmeltingBonus("oreCinnabar", new ItemStack(ModItems.QUICKSILVER_NUGGET.get()));
        ArcaneresearchApi.addSmeltingBonus("oreCopper", new ItemStack(ModItems.COPPER_NUGGET.get()));
        ArcaneresearchApi.addSmeltingBonus("oreTin", new ItemStack(ModItems.TIN_NUGGET.get()));
        ArcaneresearchApi.addSmeltingBonus("oreSilver", new ItemStack(ModItems.SILVER_NUGGET.get()));
        ArcaneresearchApi.addSmeltingBonus("oreLead", new ItemStack(ModItems.LEAD_NUGGET.get()));
        ArcaneresearchApi.addSmeltingBonus("oreQuartz", new ItemStack(ModItems.QUARTZ_NUGGET.get()));

        ArcaneresearchApi.addSmeltingBonus(new ItemStack(ModItems.CLUSTER_IRON.get()), new ItemStack(Items.IRON_NUGGET));
        ArcaneresearchApi.addSmeltingBonus(new ItemStack(ModItems.CLUSTER_GOLD.get()), new ItemStack(Items.GOLD_NUGGET));
        ArcaneresearchApi.addSmeltingBonus(new ItemStack(ModItems.CLUSTER_CINNABAR.get()), new ItemStack(ModItems.QUICKSILVER_NUGGET.get()));
        ArcaneresearchApi.addSmeltingBonus(new ItemStack(ModItems.CLUSTER_COPPER.get()), new ItemStack(ModItems.COPPER_NUGGET.get()));
        ArcaneresearchApi.addSmeltingBonus(new ItemStack(ModItems.CLUSTER_TIN.get()), new ItemStack(ModItems.TIN_NUGGET.get()));
        ArcaneresearchApi.addSmeltingBonus(new ItemStack(ModItems.CLUSTER_SILVER.get()), new ItemStack(ModItems.SILVER_NUGGET.get()));
        ArcaneresearchApi.addSmeltingBonus(new ItemStack(ModItems.CLUSTER_LEAD.get()), new ItemStack(ModItems.LEAD_NUGGET.get()));
        ArcaneresearchApi.addSmeltingBonus(new ItemStack(ModItems.CLUSTER_QUARTZ.get()), new ItemStack(ModItems.QUARTZ_NUGGET.get()));

        ArcaneresearchApi.addSmeltingBonus(new ItemStack(Items.BEEF), new ItemStack(ModItems.CHUNKS_BEEF.get()));
        ArcaneresearchApi.addSmeltingBonus(new ItemStack(Items.CHICKEN), new ItemStack(ModItems.CHUNKS_CHICKEN.get()));
        ArcaneresearchApi.addSmeltingBonus(new ItemStack(Items.PORKCHOP), new ItemStack(ModItems.CHUNKS_PORK.get()));
        ArcaneresearchApi.addSmeltingBonus(new ItemStack(Items.RABBIT), new ItemStack(ModItems.CHUNKS_RABBIT.get()));
        ArcaneresearchApi.addSmeltingBonus(new ItemStack(Items.MUTTON), new ItemStack(ModItems.CHUNKS_MUTTON.get()));
        ArcaneresearchApi.addSmeltingBonus(new ItemStack(Items.COD), new ItemStack(ModItems.CHUNKS_FISH.get()));
        ArcaneresearchApi.addSmeltingBonus(new ItemStack(Items.SALMON), new ItemStack(ModItems.CHUNKS_FISH.get()));
        ArcaneresearchApi.addSmeltingBonus(new ItemStack(Items.TROPICAL_FISH), new ItemStack(ModItems.CHUNKS_FISH.get()));
        ArcaneresearchApi.addSmeltingBonus(new ItemStack(Items.PUFFERFISH), new ItemStack(ModItems.CHUNKS_FISH.get()));

        ArcaneresearchApi.addSmeltingBonus("oreDiamond", new ItemStack(ModItems.QUARTZ_NUGGET.get()), 0.025f);
        ArcaneresearchApi.addSmeltingBonus("oreRedstone", new ItemStack(ModItems.QUARTZ_NUGGET.get()), 0.01f);
        ArcaneresearchApi.addSmeltingBonus("oreLapis", new ItemStack(ModItems.QUARTZ_NUGGET.get()), 0.01f);
        ArcaneresearchApi.addSmeltingBonus("oreEmerald", new ItemStack(ModItems.QUARTZ_NUGGET.get()), 0.025f);
        ArcaneresearchApi.addSmeltingBonus("oreGold", new ItemStack(ModItems.QUARTZ_NUGGET.get()), 0.02f);
        ArcaneresearchApi.addSmeltingBonus("oreIron", new ItemStack(ModItems.QUARTZ_NUGGET.get()), 0.01f);
        ArcaneresearchApi.addSmeltingBonus("oreCinnabar", new ItemStack(ModItems.QUARTZ_NUGGET.get()), 0.025f);
        ArcaneresearchApi.addSmeltingBonus("oreQuartz", new ItemStack(ModItems.QUARTZ_NUGGET.get()), 0.01f);
        Arcaneresearch.LOGGER.info("Registered {} infernal furnace smelting bonuses",
                arcaneresearch.api.internal.CommonInternals.smeltingBonus.size() - before);
    }

    private static void registerInfusionEnchantmentFake(String name, EnumInfusionEnchantment ench,
                                                        AspectList aspects, ItemStack extra, ItemStack tool) {
        ItemStack output = tool.copy();
        EnumInfusionEnchantment.addInfusionEnchantment(output, ench, 1);
        ArcaneresearchApi.addFakeCraftingRecipe(
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "ie_" + name + "_fake"),
                new InfusionRecipe("INFUSIONENCHANTMENT", output, 4, aspects,
                        new ItemStack(Items.ENCHANTED_BOOK),
                        new ItemStack(Items.ENCHANTED_BOOK), extra));
    }
}
