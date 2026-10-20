package arcaneresearch.common.config;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.common.lib.utils.CropUtils;
import arcaneresearch.common.lib.utils.Utils;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

// NEOFORGE_PORT: 1:1 from Forge 1.20.1 common/config/ModConfig.java.
// NOTE: Forge had no ForgeConfigSpec here — values are plain statics synced via
//   syncFlatAliases()/load(); identical approach kept (no spec system to migrate).
public class ModConfig {

    public static float auraSize = 4.0f;
    public static ArrayList<Aspect> aspectOrder = new ArrayList<>();
    public static boolean foundCopperIngot = false;
    public static boolean foundTinIngot = false;
    public static boolean foundSilverIngot = false;
    public static boolean foundLeadIngot = false;
    public static boolean foundCopperOre = false;
    public static boolean foundTinOre = false;
    public static boolean foundSilverOre = false;
    public static boolean foundLeadOre = false;
    public static boolean isHalloween = false;

    public static int taintSpreadArea = CONFIG_WORLD.taintSpreadArea;
    public static float taintSpreadRate = CONFIG_WORLD.taintSpreadRate;
    public static boolean generateOre = true;
    public static boolean generateCrystals = CONFIG_WORLD.generateCrystals;
    public static boolean generateTrees = CONFIG_WORLD.generateTrees;
    public static boolean generateStructures = CONFIG_WORLD.generateStructure;
    public static boolean generateAura = CONFIG_WORLD.generateAura;
    public static boolean generateCinnabar = CONFIG_WORLD.generateCinnabar;
    public static boolean generateAmber = CONFIG_WORLD.generateAmber;
    public static boolean generateQuartz = CONFIG_WORLD.generateQuartz;
    public static boolean generateMagicForest = CONFIG_WORLD.generateMagicForest;
    public static int biomeMagicalForestWeight = CONFIG_WORLD.biomeMagicalForestWeight;
    public static boolean allowSpawnAngryZombie = CONFIG_WORLD.allowSpawnAngryZombie;
    public static boolean allowSpawnFireBat = CONFIG_WORLD.allowSpawnFireBat;
    public static boolean allowSpawnWisp = CONFIG_WORLD.allowSpawnWisp;
    public static boolean allowSpawnTaintacle = CONFIG_WORLD.allowSpawnTaintacle;
    public static boolean allowSpawnPech = CONFIG_WORLD.allowSpawnPech;
    public static boolean allowSpawnElder = CONFIG_WORLD.allowSpawnElder;
    public static int oreDensity = CONFIG_WORLD.oreDensity;

    public static boolean wussMode = CONFIG_MISC.wussMode;
    public static boolean noSleep = CONFIG_MISC.noSleep;
    public static float visRegenRate = 1.0f;
    public static float fluxDissipationRate = 1.0f;
    public static int maxPermWarp = 100;
    public static float warpEventFrequency = 1.0f;

    public static float researchDifficulty = 1.0f;
    public static boolean hardResearch = false;
    public static boolean allowCheatSheet = CONFIG_MISC.allowCheatSheet;
    public static int shieldRecharge = CONFIG_MISC.shieldRecharge;
    public static int shieldWait = CONFIG_MISC.shieldWait;
    public static int shieldCost = CONFIG_MISC.shieldCost;

    public static int maxGolemsPerPlayer = 64;
    public static float golemRangeMultiplier = 1.0f;

    public static int baseVisPerChunk = 500;
    public static float fluxRiftThreshold = 0.75f;

    public static int auraTickRate = 20;
    public static int maxAreaEffectEntities = 32;

    public static String[] dimensionBlacklist = {};

    public static void syncFlatAliases() {
        taintSpreadArea = CONFIG_WORLD.taintSpreadArea;
        taintSpreadRate = CONFIG_WORLD.taintSpreadRate;
        generateCrystals = CONFIG_WORLD.generateCrystals;
        generateTrees = CONFIG_WORLD.generateTrees;
        generateStructures = CONFIG_WORLD.generateStructure;
        generateAura = CONFIG_WORLD.generateAura;
        generateCinnabar = CONFIG_WORLD.generateCinnabar;
        generateAmber = CONFIG_WORLD.generateAmber;
        generateQuartz = CONFIG_WORLD.generateQuartz;
        generateMagicForest = CONFIG_WORLD.generateMagicForest;
        biomeMagicalForestWeight = CONFIG_WORLD.biomeMagicalForestWeight;
        allowSpawnAngryZombie = CONFIG_WORLD.allowSpawnAngryZombie;
        allowSpawnFireBat = CONFIG_WORLD.allowSpawnFireBat;
        allowSpawnWisp = CONFIG_WORLD.allowSpawnWisp;
        allowSpawnTaintacle = CONFIG_WORLD.allowSpawnTaintacle;
        allowSpawnPech = CONFIG_WORLD.allowSpawnPech;
        allowSpawnElder = CONFIG_WORLD.allowSpawnElder;
        oreDensity = CONFIG_WORLD.oreDensity;
        wussMode = CONFIG_MISC.wussMode;
        noSleep = CONFIG_MISC.noSleep;
        allowCheatSheet = CONFIG_MISC.allowCheatSheet;
        shieldRecharge = CONFIG_MISC.shieldRecharge;
        shieldWait = CONFIG_MISC.shieldWait;
        shieldCost = CONFIG_MISC.shieldCost;
    }

    public static boolean isDimensionAllowed(String dimensionName) {
        for (String blacklisted : dimensionBlacklist) {
            if (blacklisted.equals(dimensionName)) {
                return false;
            }
        }
        return true;
    }

    public static void initAspectOrder() {
        aspectOrder.clear();
        Collection<Aspect> pa = Aspect.aspects.values();
        for (Aspect aspect : pa) {
            aspectOrder.add(aspect);
        }
    }

    public static void updateHalloweenFlag() {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            if (calendar.get(Calendar.MONTH) + 1 == 10 && calendar.get(Calendar.DAY_OF_MONTH) == 31) {
                isHalloween = true;
            }
        } catch (Exception ignored) {
        }
    }

    public static void postInitLoot() {
        ArcaneresearchApi.addLootBagItem(new ItemStack(Items.GOLD_NUGGET, 1), 2500, 0);
        ArcaneresearchApi.addLootBagItem(new ItemStack(Items.GOLD_NUGGET, 2), 2250, 1);
        ArcaneresearchApi.addLootBagItem(new ItemStack(Items.GOLD_NUGGET, 3), 2000, 2);
        ArcaneresearchApi.addLootBagItem(new ItemStack(ModItems.SALIS_MUNDUS.get()), 3, 0);
        ArcaneresearchApi.addLootBagItem(new ItemStack(ModItems.SALIS_MUNDUS.get()), 6, 1);
        ArcaneresearchApi.addLootBagItem(new ItemStack(ModItems.SALIS_MUNDUS.get()), 9, 2);
        ArcaneresearchApi.addLootBagItem(new ItemStack(Items.CHORUS_FRUIT), 5, 0, 1, 2);
        ArcaneresearchApi.addLootBagItem(new ItemStack(Items.COMPASS), 5, 0, 1, 2);
        ArcaneresearchApi.addLootBagItem(new ItemStack(Items.COOKIE), 5, 0, 1, 2);

        ItemStack pearlMote = new ItemStack(ModItems.PRIMORDIAL_PEARL.get());
        pearlMote.setDamageValue(7);
        ItemStack pearlNodule6 = new ItemStack(ModItems.PRIMORDIAL_PEARL.get());
        pearlNodule6.setDamageValue(6);
        ItemStack pearlNodule5 = new ItemStack(ModItems.PRIMORDIAL_PEARL.get());
        pearlNodule5.setDamageValue(5);
        ItemStack pearlNodule3 = new ItemStack(ModItems.PRIMORDIAL_PEARL.get());
        pearlNodule3.setDamageValue(3);
        ArcaneresearchApi.addLootBagItem(pearlMote, 1, 0);
        ArcaneresearchApi.addLootBagItem(pearlMote.copy(), 3, 1);
        ArcaneresearchApi.addLootBagItem(pearlNodule6, 1, 1);
        ArcaneresearchApi.addLootBagItem(pearlNodule5, 9, 2);
        ArcaneresearchApi.addLootBagItem(pearlNodule3, 3, 2);
        ArcaneresearchApi.addLootBagItem(new ItemStack(ModItems.PRIMORDIAL_PEARL.get()), 1, 2);
        ArcaneresearchApi.addLootBagItem(new ItemStack(Items.NETHER_STAR), 1, 2);
        ArcaneresearchApi.addLootBagItem(new ItemStack(Items.DIAMOND), 10, 0);
        ArcaneresearchApi.addLootBagItem(new ItemStack(Items.DIAMOND), 50, 1, 2);
        ArcaneresearchApi.addLootBagItem(new ItemStack(Items.EMERALD), 15, 0);
        ArcaneresearchApi.addLootBagItem(new ItemStack(Items.EMERALD), 75, 1, 2);
        ArcaneresearchApi.addLootBagItem(new ItemStack(Items.GOLD_INGOT), 100, 0, 1, 2);
        ArcaneresearchApi.addLootBagItem(new ItemStack(Items.ENDER_PEARL), 100, 0, 1, 2);
        ArcaneresearchApi.addLootBagItem(new ItemStack(ModItems.AMULET_VIS_FOUND.get()), 6, 1, 2);
        ArcaneresearchApi.addLootBagItem(new ItemStack(ModItems.BAUBLE_AMULET_MUNDANE.get()), 10, 0);
        ArcaneresearchApi.addLootBagItem(new ItemStack(ModItems.BAUBLE_RING_MUNDANE.get()), 10, 0);
        ArcaneresearchApi.addLootBagItem(new ItemStack(ModItems.BAUBLE_GIRDLE_MUNDANE.get()), 10, 0);
        ArcaneresearchApi.addLootBagItem(new ItemStack(ModItems.BAUBLE_RING_APPRENTICE.get()), 5, 2);
        // NEOFORGE_PORT (26.3): Forge loot-bag entry (lesser runic ring) was dropped in the port.
        ArcaneresearchApi.addLootBagItem(new ItemStack(ModItems.RUNIC_RING_LESSER.get()), 5, 1, 2);
        ArcaneresearchApi.addLootBagItem(new ItemStack(ModItems.BAUBLE_AMULET_FANCY.get()), 5, 1);
        ArcaneresearchApi.addLootBagItem(new ItemStack(ModItems.BAUBLE_RING_FANCY.get()), 5, 1);
        ArcaneresearchApi.addLootBagItem(new ItemStack(ModItems.BAUBLE_GIRDLE_FANCY.get()), 5, 1);
        ArcaneresearchApi.addLootBagItem(new ItemStack(Items.EXPERIENCE_BOTTLE), 5, 0);
        ArcaneresearchApi.addLootBagItem(new ItemStack(Items.EXPERIENCE_BOTTLE), 10, 1);
        ArcaneresearchApi.addLootBagItem(new ItemStack(Items.EXPERIENCE_BOTTLE), 20, 2);
        ArcaneresearchApi.addLootBagItem(new ItemStack(Items.GOLDEN_APPLE, 1), 1, 0);
        ArcaneresearchApi.addLootBagItem(new ItemStack(Items.GOLDEN_APPLE, 1), 2, 1);
        ArcaneresearchApi.addLootBagItem(new ItemStack(Items.GOLDEN_APPLE, 1), 3, 2);
        ArcaneresearchApi.addLootBagItem(new ItemStack(Items.GOLDEN_APPLE, 1), 3, 0);
        ArcaneresearchApi.addLootBagItem(new ItemStack(Items.GOLDEN_APPLE, 1), 6, 1);
        ArcaneresearchApi.addLootBagItem(new ItemStack(Items.GOLDEN_APPLE, 1), 9, 2);
        ArcaneresearchApi.addLootBagItem(new ItemStack(Items.BOOK), 10, 0, 1, 2);

        // NEOFORGE_PORT: ForgeRegistries.POTIONS + PotionUtils.setPotion ->
        // BuiltInRegistries.POTION (values, wrapped as holders) + PotionContents.createItemStack.
        for (net.minecraft.world.item.alchemy.Potion potion : BuiltInRegistries.POTION) {
            var holder = BuiltInRegistries.POTION.wrapAsHolder(potion);
            ArcaneresearchApi.addLootBagItem(net.minecraft.world.item.alchemy.PotionContents.createItemStack(Items.POTION, holder), 2, 0, 1, 2);
            ArcaneresearchApi.addLootBagItem(net.minecraft.world.item.alchemy.PotionContents.createItemStack(Items.SPLASH_POTION, holder), 2, 0, 1, 2);
            ArcaneresearchApi.addLootBagItem(net.minecraft.world.item.alchemy.PotionContents.createItemStack(Items.LINGERING_POTION, holder), 2, 1, 2);
        }

        ItemStack[] commonLoot = {new ItemStack(ModItems.LOOT_BAG_COMMON.get()), new ItemStack(ModItems.THAUMIUM_INGOT.get()), new ItemStack(ModItems.AMBER.get())};
        ItemStack[] uncommonLoot = {new ItemStack(ModItems.LOOT_BAG_UNCOMMON.get()), new ItemStack(ModItems.BAUBLE_AMULET_MUNDANE.get()), new ItemStack(ModItems.BAUBLE_RING_MUNDANE.get()), new ItemStack(ModItems.BAUBLE_GIRDLE_MUNDANE.get())};
        ItemStack[] rareLoot = {new ItemStack(ModItems.LOOT_BAG_RARE.get()), new ItemStack(ModItems.THAUMONOMICON.get()), new ItemStack(ModItems.THAUMIUM_SWORD.get()), new ItemStack(ModItems.THAUMIUM_AXE.get()), new ItemStack(ModItems.THAUMIUM_HOE.get()), new ItemStack(ModItems.THAUMIUM_PICK.get()), new ItemStack(ModItems.BAUBLE_RING_APPRENTICE.get()), new ItemStack(ModItems.BAUBLE_AMULET_FANCY.get()), new ItemStack(ModItems.BAUBLE_RING_FANCY.get()), new ItemStack(ModItems.BAUBLE_GIRDLE_FANCY.get()), new ItemStack(ModItems.AMULET_VIS_FOUND.get()),
                // NEOFORGE_PORT (26.3): Forge rare-loot entry dropped in the port.
                new ItemStack(ModItems.RUNIC_RING_LESSER.get())};
    }

    public static void modCompatibility() {
        Arcaneresearch.LOGGER.info("Checking for mod & oredict compatibilities");
        try {

            // NEOFORGE_PORT: ForgeRegistries.ITEMS -> BuiltInRegistries.ITEM (values iterable 1:1).
            for (Item item : BuiltInRegistries.ITEM) {
                net.minecraft.resources.Identifier id = BuiltInRegistries.ITEM.getKey(item);
                if (id == null) {
                    continue;
                }
                String path = id.getPath();
                if (!id.getNamespace().equals("minecraft") && !id.getNamespace().equals("arcaneresearch")) {
                    if (path.endsWith("_ore") || path.startsWith("ore_") || path.contains("ore_")) {
                        String lower = path.toLowerCase(java.util.Locale.ROOT);
                        if (lower.contains("iron") && !isVanillaIronOre(item)) {
                            Utils.addSpecialMiningResult(new ItemStack(item), new ItemStack(ModItems.CLUSTER_IRON.get()), 1.0f);
                        } else if (lower.contains("gold") && !isVanillaGoldOre(item)) {
                            Utils.addSpecialMiningResult(new ItemStack(item), new ItemStack(ModItems.CLUSTER_GOLD.get()), 1.0f);
                        } else if (lower.contains("copper")) {
                            Utils.addSpecialMiningResult(new ItemStack(item), new ItemStack(ModItems.CLUSTER_COPPER.get()), 1.0f);
                            foundCopperOre = true;
                        } else if (lower.contains("tin")) {
                            Utils.addSpecialMiningResult(new ItemStack(item), new ItemStack(ModItems.CLUSTER_TIN.get()), 1.0f);
                            foundTinOre = true;
                        } else if (lower.contains("silver")) {
                            Utils.addSpecialMiningResult(new ItemStack(item), new ItemStack(ModItems.CLUSTER_SILVER.get()), 1.0f);
                            foundSilverOre = true;
                        } else if (lower.contains("lead")) {
                            Utils.addSpecialMiningResult(new ItemStack(item), new ItemStack(ModItems.CLUSTER_LEAD.get()), 1.0f);
                            foundLeadOre = true;
                        }
                    }
                    String lower = path.toLowerCase(java.util.Locale.ROOT);
                    if (lower.contains("copper_ingot") || lower.equals("copper_ingot") || lower.endsWith("ingot_copper")) {
                        foundCopperIngot = true;
                    } else if (lower.contains("tin_ingot") || lower.endsWith("ingot_tin")) {
                        foundTinIngot = true;
                    } else if (lower.contains("silver_ingot") || lower.endsWith("ingot_silver")) {
                        foundSilverIngot = true;
                    } else if (lower.contains("lead_ingot") || lower.endsWith("ingot_lead")) {
                        foundLeadIngot = true;
                    }
                }
            }

            foundCopperOre = true;
            foundTinOre = true;
            foundSilverOre = true;
            foundLeadOre = true;
            foundCopperIngot = true;
            foundTinIngot = true;
            foundSilverIngot = true;
            foundLeadIngot = true;
        } catch (Exception ex) {
            Arcaneresearch.LOGGER.warn("Mod compatibility scan failed, continuing with defaults", ex);
        }

        Arcaneresearch.LOGGER.info("Adding entities to MFR safari net blacklist.");
        registerSafariNetBlacklist(arcaneresearch.common.entities.construct.EntityOwnedConstruct.class);
        registerSafariNetBlacklist(arcaneresearch.common.entities.EntityFallingTaint.class);
        registerSafariNetBlacklist(arcaneresearch.common.entities.monster.EntityWisp.class);
        registerSafariNetBlacklist(arcaneresearch.common.entities.monster.EntityPech.class);
        registerSafariNetBlacklist(arcaneresearch.common.entities.monster.EntityEldritchGuardian.class);
        registerSafariNetBlacklist(arcaneresearch.common.entities.monster.boss.EntityEldritchWarden.class);
        registerSafariNetBlacklist(arcaneresearch.common.entities.monster.boss.EntityEldritchGolem.class);
        registerSafariNetBlacklist(arcaneresearch.common.entities.monster.cult.EntityCultistCleric.class);
        registerSafariNetBlacklist(arcaneresearch.common.entities.monster.cult.EntityCultistKnight.class);
        registerSafariNetBlacklist(arcaneresearch.common.entities.monster.boss.EntityCultistLeader.class);
        registerSafariNetBlacklist(arcaneresearch.common.entities.monster.boss.EntityCultistPortalGreater.class);
        registerSafariNetBlacklist(arcaneresearch.common.entities.monster.cult.EntityCultistPortalLesser.class);
        registerSafariNetBlacklist(arcaneresearch.common.entities.monster.EntityEldritchCrab.class);
        registerSafariNetBlacklist(arcaneresearch.common.entities.monster.EntityInhabitedZombie.class);
    }

    private static boolean isVanillaIronOre(Item item) {
        return item == Blocks.IRON_ORE.asItem() || item == Blocks.DEEPSLATE_IRON_ORE.asItem();
    }

    private static boolean isVanillaGoldOre(Item item) {
        return item == Blocks.GOLD_ORE.asItem() || item == Blocks.DEEPSLATE_GOLD_ORE.asItem()
                || item == Blocks.NETHER_GOLD_ORE.asItem();
    }

    public static void registerSafariNetBlacklist(Class<?> blacklistedEntity) {
        try {
            Class<?> registry = Class.forName("powercrystals.minefactoryreloaded.MFRRegistry");
            if (registry != null) {
                Method reg = registry.getMethod("registerSafariNetBlacklist", Class.class);
                reg.invoke(registry, blacklistedEntity);
            }
        } catch (Exception ex) {

        }
    }

    public static void postInitMisc() {
        // NEOFORGE_PORT: IPlantable removed in NeoForge; seeds are BlockItems whose block
        // is plant-like (same replacement as SealHarvest: BushBlock/CropBlock/StemBlock/SaplingBlock).
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof net.minecraft.world.item.BlockItem blockItem) {
                try {
                    net.minecraft.world.level.block.Block block = blockItem.getBlock();
                    if (block instanceof net.minecraft.world.level.block.BushBlock
                            || block instanceof net.minecraft.world.level.block.CropBlock
                            || block instanceof net.minecraft.world.level.block.StemBlock
                            || block instanceof net.minecraft.world.level.block.SaplingBlock) {
                        ArcaneresearchApi.registerSeed(block, new ItemStack(item));
                    }
                } catch (Exception ex) {

                }
            }
        }
        CropUtils.addStandardCrop(Blocks.MELON, 32767);
        CropUtils.addStandardCrop(Blocks.PUMPKIN, 32767);
        CropUtils.addStackedCrop(Blocks.SUGAR_CANE, 32767);
        CropUtils.addStackedCrop(Blocks.CACTUS, 32767);
        CropUtils.addStandardCrop(Blocks.NETHER_WART, 3);
        ArcaneresearchApi.registerSeed(Blocks.COCOA, new ItemStack(Items.COCOA_BEANS));
        Utils.addSpecialMiningResult(new ItemStack(Blocks.IRON_ORE), new ItemStack(ModItems.CLUSTER_IRON.get()), 1.0f);
        Utils.addSpecialMiningResult(new ItemStack(Blocks.GOLD_ORE), new ItemStack(ModItems.CLUSTER_GOLD.get()), 1.0f);
        Utils.addSpecialMiningResult(new ItemStack(ModBlocks.CINNABAR_ORE.get()), new ItemStack(ModItems.CLUSTER_CINNABAR.get()), 1.0f);
        Utils.addSpecialMiningResult(new ItemStack(Items.QUARTZ), new ItemStack(ModItems.CLUSTER_QUARTZ.get()), 1.0f);
        initAspectOrder();
    }

    public static void load() {
        syncFlatAliases();
    }

    public static void save() {
    }

    public static class CONFIG_GRAPHICS {
        public static boolean largeTagText = false;
        public static boolean disableShaders = false;
        public static boolean nostress = false;
        public static boolean crooked = true;
        public static boolean dialBottom = false;
        public static boolean showTags = false;
        public static boolean blueBiome = false;
        public static boolean showGolemEmotes = true;
    }

    public static class CONFIG_WORLD {
        public static int overworldDim = 0;
        public static int dimensionOuterId = -42;
        public static int oreDensity = 100;
        public static boolean generateMagicForest = true;
        public static int biomeMagicalForestWeight = 5;
        public static float taintSpreadRate = 100.0f;
        public static int taintSpreadArea = 32;
        public static boolean generateAura = true;
        public static boolean generateStructure = true;
        public static boolean generateCinnabar = true;
        public static boolean generateAmber = true;
        public static boolean generateQuartz = true;
        public static boolean generateCrystals = true;
        public static boolean generateTrees = true;
        public static String regenKey = "DEFAULT";
        public static boolean regenAura = false;
        public static boolean regenStructure = false;
        public static boolean regenCinnabar = false;
        public static boolean regenAmber = false;
        public static boolean regenQuartz = false;
        public static boolean regenCrystals = false;
        public static boolean regenTrees = false;
        public static boolean allowSpawnAngryZombie = true;
        public static boolean allowSpawnFireBat = true;
        public static boolean allowSpawnWisp = true;
        public static boolean allowSpawnTaintacle = true;
        public static boolean allowSpawnPech = true;
        public static boolean allowSpawnElder = true;
        public static boolean allowChampionMobs = true;
    }

    public static class CONFIG_MISC {
        public static boolean noSleep = false;
        public static boolean wussMode = false;
        public static boolean allowCheatSheet = false;
        public static int shieldRecharge = 2000;
        public static int shieldWait = 4000;
        public static int shieldCost = 1;
    }
}
