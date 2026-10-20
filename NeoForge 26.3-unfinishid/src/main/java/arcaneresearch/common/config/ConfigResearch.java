package arcaneresearch.common.config;

import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.parrot.Parrot; // NEOFORGE_PORT: subpackage
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Enderman; // NEOFORGE_PORT: Enderman -> Enderman
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.spider.Spider; // NEOFORGE_PORT: subpackage
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.registries.BuiltInRegistries; // NEOFORGE_PORT: was ForgeRegistries
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.entities.IEldritchMob;
import arcaneresearch.api.research.*;
import arcaneresearch.api.research.theorycraft.TheorycraftManager;
import arcaneresearch.common.blocks.aura.AuraBlocks;
import arcaneresearch.common.entities.EntityFluxRift;
import arcaneresearch.common.entities.construct.EntityOwnedConstruct;
import arcaneresearch.common.entities.monster.*;
import arcaneresearch.common.entities.monster.boss.EntityArcaneresearchBoss;
import arcaneresearch.common.entities.monster.cult.EntityCultist;
import arcaneresearch.common.entities.monster.tainted.EntityTaintCrawler;
import arcaneresearch.common.entities.monster.tainted.EntityTaintSeed;
import arcaneresearch.common.entities.monster.tainted.EntityTaintSwarm;
import arcaneresearch.common.entities.monster.tainted.EntityTaintacle;
import arcaneresearch.common.golems.EntityArcaneresearchGolem;
import arcaneresearch.common.lib.research.*;
import arcaneresearch.common.lib.research.theorycraft.TheoryRegistry;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModItems;

public class ConfigResearch {

    public static final String[] TC_CATEGORIES = {
            "BASICS", "ALCHEMY", "AUROMANCY", "ARTIFICE", "INFUSION", "GOLEMANCY", "ELDRITCH", "LEGACY"
    };

    private static final Identifier BACK_OVER = Identifier.fromNamespaceAndPath("arcaneresearch", "textures/gui/gui_research_back_over.png");

    public static void init() {
        Arcaneresearch.LOGGER.info("Initializing Arcaneresearch research system...");

        initCategories();
        initScannables();
        initTheorycraft();
        initWarp();

        for (String cat : TC_CATEGORIES) {
            // NEOFORGE_PORT: Identifier(String,String) ctor private -> fromNamespaceAndPath.
            ArcaneresearchApi.registerResearchLocation(Identifier.fromNamespaceAndPath("arcaneresearch", "research/" + cat.toLowerCase()));
        }
        ArcaneresearchApi.registerResearchLocation(Identifier.fromNamespaceAndPath("arcaneresearch", "research/scans"));

        Arcaneresearch.LOGGER.info("Research categories and JSON locations registered");
    }

    /**
     * Enchantment scans (datapack registry — needs RegistryAccess, so server start,
     * same as pech book trades). Forge enumerated ForgeRegistries.ENCHANTMENTS here.
     */
    // Enchantments are a datapack registry: scannables are rebuilt per server start
    // (previous server's holders removed so repeated world loads never duplicate them).
    private static final java.util.List<ScanEnchantment> enchantmentScans = new java.util.ArrayList<>();

    public static void initEnchantmentScans(net.minecraft.core.HolderLookup.Provider registries) {
        try {
            enchantmentScans.forEach(ScanningManager::removeScannableThing);
            enchantmentScans.clear();
            for (var holder : registries.lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                    .listElements().toList()) {
                if (holder != null && holder.isBound()) {
                    ScanEnchantment scan = new ScanEnchantment(holder);
                    enchantmentScans.add(scan);
                    ScanningManager.addScannableThing(scan);
                }
            }
        } catch (Exception e) {
            Arcaneresearch.LOGGER.warn("Failed to register enchantment scannables", e);
        }
    }

    public static void postInit() {
        Arcaneresearch.LOGGER.info("Parsing research JSON files...");
        ResearchManager.parseAllResearch();
        Arcaneresearch.LOGGER.info("Research system initialized with {} categories",
                ResearchCategories.researchCategories.size());
    }

    private static void initCategories() {

        ResearchCategories.registerCategory("BASICS", null,
                new AspectList()
                        .add(Aspect.PLANT, 5).add(Aspect.ORDER, 5).add(Aspect.ENTROPY, 5)
                        .add(Aspect.AIR, 5).add(Aspect.FIRE, 5).add(Aspect.EARTH, 3).add(Aspect.WATER, 5),
                Identifier.fromNamespaceAndPath("arcaneresearch", "textures/item/thaumonomicon_cheat.png"),
                Identifier.fromNamespaceAndPath("arcaneresearch", "textures/gui/gui_research_back_1.png"),
                BACK_OVER);

        ResearchCategories.registerCategory("AUROMANCY", "UNLOCKAUROMANCY",
                new AspectList()
                        .add(Aspect.AURA, 20).add(Aspect.MAGIC, 20).add(Aspect.FLUX, 15)
                        .add(Aspect.CRYSTAL, 5).add(Aspect.COLD, 5).add(Aspect.AIR, 5),
                Identifier.fromNamespaceAndPath("arcaneresearch", "textures/research/cat_auromancy.png"),
                Identifier.fromNamespaceAndPath("arcaneresearch", "textures/gui/gui_research_back_2.png"),
                BACK_OVER);

        ResearchCategories.registerCategory("ALCHEMY", "UNLOCKALCHEMY",
                new AspectList()
                        .add(Aspect.ALCHEMY, 30).add(Aspect.FLUX, 10).add(Aspect.MAGIC, 10)
                        .add(Aspect.LIFE, 5).add(Aspect.AVERSION, 5).add(Aspect.DESIRE, 5).add(Aspect.WATER, 5),
                Identifier.fromNamespaceAndPath("arcaneresearch", "textures/research/cat_alchemy.png"),
                Identifier.fromNamespaceAndPath("arcaneresearch", "textures/gui/gui_research_back_3.png"),
                BACK_OVER);

        ResearchCategories.registerCategory("ARTIFICE", "UNLOCKARTIFICE",
                new AspectList()
                        .add(Aspect.MECHANISM, 10).add(Aspect.CRAFT, 10).add(Aspect.METAL, 10)
                        .add(Aspect.TOOL, 10).add(Aspect.ENERGY, 10).add(Aspect.LIGHT, 5)
                        .add(Aspect.FLIGHT, 5).add(Aspect.TRAP, 5).add(Aspect.FIRE, 5),
                Identifier.fromNamespaceAndPath("arcaneresearch", "textures/research/cat_artifice.png"),
                Identifier.fromNamespaceAndPath("arcaneresearch", "textures/gui/gui_research_back_4.png"),
                BACK_OVER);

        ResearchCategories.registerCategory("INFUSION", "UNLOCKINFUSION",
                new AspectList()
                        .add(Aspect.MAGIC, 30).add(Aspect.PROTECT, 10).add(Aspect.TOOL, 10)
                        .add(Aspect.FLUX, 5).add(Aspect.CRAFT, 5).add(Aspect.SOUL, 5).add(Aspect.EARTH, 3),
                Identifier.fromNamespaceAndPath("arcaneresearch", "textures/research/cat_infusion.png"),
                Identifier.fromNamespaceAndPath("arcaneresearch", "textures/gui/gui_research_back_7.png"),
                BACK_OVER);

        ResearchCategories.registerCategory("GOLEMANCY", "UNLOCKGOLEMANCY",
                new AspectList()
                        .add(Aspect.MAN, 20).add(Aspect.MOTION, 10).add(Aspect.MIND, 10)
                        .add(Aspect.MECHANISM, 10).add(Aspect.EXCHANGE, 5).add(Aspect.SENSES, 5)
                        .add(Aspect.BEAST, 5).add(Aspect.ORDER, 5),
                Identifier.fromNamespaceAndPath("arcaneresearch", "textures/research/cat_golemancy.png"),
                Identifier.fromNamespaceAndPath("arcaneresearch", "textures/gui/gui_research_back_5.png"),
                BACK_OVER);

        ResearchCategories.registerCategory("ELDRITCH", "UNLOCKELDRITCH",
                new AspectList()
                        .add(Aspect.ELDRITCH, 20).add(Aspect.DARKNESS, 10).add(Aspect.MAGIC, 5)
                        .add(Aspect.MIND, 5).add(Aspect.VOID, 5).add(Aspect.DEATH, 5)
                        .add(Aspect.UNDEAD, 5).add(Aspect.ENTROPY, 5),
                Identifier.fromNamespaceAndPath("arcaneresearch", "textures/research/cat_eldritch.png"),
                Identifier.fromNamespaceAndPath("arcaneresearch", "textures/gui/gui_research_back_6.png"),
                BACK_OVER);

        ResearchCategories.registerCategory("LEGACY", "FIRSTSTEPS",
                new AspectList(),
                Identifier.fromNamespaceAndPath("arcaneresearch", "textures/research/r_legacyknowledges.png"),
                Identifier.fromNamespaceAndPath("arcaneresearch", "textures/gui/gui_research_back_1.png"),
                BACK_OVER);

        Arcaneresearch.LOGGER.info("Registered {} research categories", TC_CATEGORIES.length);
    }

    private static void initScannables() {

        ScanningManager.addScannableThing(new ScanGeneric());

        // NEOFORGE_PORT: enchantments are datapack-driven in 26.x (no BuiltIn registry);
        // their scans register at server start (initEnchantmentScans, registryAccess).
        // NEOFORGE_PORT: Registry.holders() gone -> listElements() (HolderLookup API).
        for (var holder : BuiltInRegistries.MOB_EFFECT.listElements().toList()) {
            if (holder != null) {
                ScanningManager.addScannableThing(new ScanPotion(holder));
            }
        }

        ScanningManager.addScannableThing(new ScanEntity("!Wisp", EntityWisp.class, true));
        ScanningManager.addScannableThing(new ScanEntity("!ThaumSlime", EntityThaumicSlime.class, true));
        ScanningManager.addScannableThing(new ScanEntity("!Firebat", EntityFireBat.class, true));
        ScanningManager.addScannableThing(new ScanEntity("!Pech", EntityPech.class, true));
        ScanningManager.addScannableThing(new ScanEntity("!BrainyZombie", EntityBrainyZombie.class, true));
        ScanningManager.addScannableThing(new ScanEntity("!EldritchCrab", EntityEldritchCrab.class, true));
        ScanningManager.addScannableThing(new ScanEntity("!EldritchCrab", EntityInhabitedZombie.class, true));
        ScanningManager.addScannableThing(new ScanEntity("!CrimsonCultist", EntityCultist.class, true));
        ScanningManager.addScannableThing(new ScanEntity("!EldritchGuardian", EntityEldritchGuardian.class, true));
        ScanningManager.addScannableThing(new ScanEntity("!TaintCrawler", EntityTaintCrawler.class, true));
        ScanningManager.addScannableThing(new ScanEntity("!Taintacle", EntityTaintacle.class, true));
        ScanningManager.addScannableThing(new ScanEntity("!TaintSeed", EntityTaintSeed.class, true));
        ScanningManager.addScannableThing(new ScanEntity("!TaintSwarm", EntityTaintSwarm.class, true));
        ScanningManager.addScannableThing(new ScanEntity("f_toomuchflux", EntityFluxRift.class, true));
        ScanningManager.addScannableThing(new ScanEntity("!FluxRift", EntityFluxRift.class, true));

        ScanningManager.addScannableThing(new ScanEntity("f_golem", EntityArcaneresearchGolem.class, true));
        ScanningManager.addScannableThing(new ScanEntity("f_golem", EntityOwnedConstruct.class, true));
        ScanningManager.addScannableThing(new ScanEntity("f_SPIDER", Spider.class, true));
        ScanningManager.addScannableThing(new ScanEntity("f_BAT", Bat.class, true));
        ScanningManager.addScannableThing(new ScanEntity("f_BAT", EntityFireBat.class, true));
        ScanningManager.addScannableThing(new ScanEntity("f_FLY", Bat.class, true));
        ScanningManager.addScannableThing(new ScanEntity("f_FLY", Parrot.class, true));
        ScanningManager.addScannableThing(new ScanEntity("f_FLY", EntityFireBat.class, true));
        ScanningManager.addScannableThing(new ScanEntity("f_FLY", EntityTaintSwarm.class, true));
        ScanningManager.addScannableThing(new ScanEntity("f_FLY", EntityWisp.class, true));
        ScanningManager.addScannableThing(new ScanEntity("f_FLY", Ghast.class, true));
        ScanningManager.addScannableThing(new ScanEntity("f_FLY", Blaze.class, true));
        ScanningManager.addScannableThing(new ScanEntity("!ORMOB", IEldritchMob.class, true));
        ScanningManager.addScannableThing(new ScanEntity("!ORBOSS", EntityArcaneresearchBoss.class, true));
        ScanningManager.addScannableThing(new ScanEntity("f_TELEPORT", Enderman.class, true));
        ScanningManager.addScannableThing(new ScanEntity("f_BRAIN", EntityBrainyZombie.class, true));

        ScanningManager.addScannableThing(new ScanBlock("!ORBLOCK1",
                ModBlocks.ANCIENT_STONE.get(), ModBlocks.ANCIENT_STONE_TILE.get()));
        ScanningManager.addScannableThing(new ScanBlock("!ORBLOCK2",
                ModBlocks.ELDRITCH_STONE_TILE.get()));
        ScanningManager.addScannableThing(new ScanBlock("!ORBLOCK3",
                ModBlocks.ANCIENT_STONE_GLYPHED.get()));
        ScanningManager.addScannableThing(new ScanBlock("ORE",
                ModBlocks.AMBER_ORE.get(), ModBlocks.CINNABAR_ORE.get(),
                ModBlocks.CRYSTAL_AIR.get(), ModBlocks.CRYSTAL_FIRE.get(),
                ModBlocks.CRYSTAL_WATER.get(), ModBlocks.CRYSTAL_EARTH.get(),
                ModBlocks.CRYSTAL_ORDER.get(), ModBlocks.CRYSTAL_ENTROPY.get(),
                ModBlocks.CRYSTAL_FLUX.get()));
        ScanningManager.addScannableThing(new ScanBlock("!OREAMBER", ModBlocks.AMBER_ORE.get()));
        ScanningManager.addScannableThing(new ScanBlock("!ORECINNABAR", ModBlocks.CINNABAR_ORE.get()));
        ScanningManager.addScannableThing(new ScanBlock("!ORECRYSTAL",
                ModBlocks.CRYSTAL_AIR.get(), ModBlocks.CRYSTAL_FIRE.get(),
                ModBlocks.CRYSTAL_WATER.get(), ModBlocks.CRYSTAL_EARTH.get(),
                ModBlocks.CRYSTAL_ORDER.get(), ModBlocks.CRYSTAL_ENTROPY.get(),
                ModBlocks.CRYSTAL_FLUX.get()));

        ScanningManager.addScannableThing(new ScanBlock("PLANTS",
                ModBlocks.GREATWOOD_LOG.get(), ModBlocks.SILVERWOOD_LOG.get(),
                ModBlocks.GREATWOOD_SAPLING.get(), ModBlocks.SILVERWOOD_SAPLING.get(),
                ModBlocks.CINDERPEARL.get(), ModBlocks.SHIMMERLEAF.get(), ModBlocks.VISHROOM.get()));
        ScanningManager.addScannableThing(new ScanBlock("!PLANTWOOD", ModBlocks.GREATWOOD_LOG.get()));
        ScanningManager.addScannableThing(new ScanBlock("!PLANTWOOD", ModBlocks.SILVERWOOD_LOG.get()));
        ScanningManager.addScannableThing(new ScanBlock("!PLANTWOOD", ModBlocks.GREATWOOD_SAPLING.get()));
        ScanningManager.addScannableThing(new ScanBlock("!PLANTWOOD", ModBlocks.SILVERWOOD_SAPLING.get()));
        ScanningManager.addScannableThing(new ScanBlock("!PLANTCINDERPEARL", ModBlocks.CINDERPEARL.get()));
        ScanningManager.addScannableThing(new ScanBlock("!PLANTSHIMMERLEAF", ModBlocks.SHIMMERLEAF.get()));
        ScanningManager.addScannableThing(new ScanBlock("!PLANTVISHROOM", ModBlocks.VISHROOM.get()));

        ScanningManager.addScannableThing(new ScanItem("PRIMPEARL", new ItemStack(ModItems.PRIMORDIAL_PEARL.get())));
        ScanningManager.addScannableThing(new ScanItem("!DRAGONBREATH", new ItemStack(Items.DRAGON_BREATH)));
        ScanningManager.addScannableThing(new ScanItem("!TOTEMUNDYING", new ItemStack(Items.TOTEM_OF_UNDYING)));
        ScanningManager.addScannableThing(new ScanItem("f_TELEPORT", new ItemStack(Items.ENDER_PEARL)));
        ScanningManager.addScannableThing(new ScanItem("f_BRAIN", new ItemStack(ModItems.ZOMBIE_BRAIN.get())));
        ScanningManager.addScannableThing(new ScanItem("f_arrow", new ItemStack(Items.ARROW)));
        // NEOFORGE_PORT: projectiles moved to subpackages in 26.x (verified in 26.3 jar).
        ScanningManager.addScannableThing(new ScanEntity("f_arrow",
                net.minecraft.world.entity.projectile.arrow.AbstractArrow.class, true));
        ScanningManager.addScannableThing(new ScanEntity("f_fireball",
                net.minecraft.world.entity.projectile.hurtingprojectile.Fireball.class, true));
        ScanningManager.addScannableThing(new ScanEntity("f_spit",
                net.minecraft.world.entity.projectile.LlamaSpit.class, true));
        ScanningManager.addScannableThing(new ScanItem("f_VOIDSEED", new ItemStack(ModItems.VOID_SEED.get())));
        ScanningManager.addScannableThing(new ScanItem("f_MATCLAY", new ItemStack(Items.CLAY_BALL)));
        // NEOFORGE_PORT: dyed terracotta grouped in Blocks.DYED_TERRACOTTA (ColorCollection,
        // lowercase color accessors, verified in 26.3 jar); same 17 blocks.
        var dyedTerracotta = Blocks.DYED_TERRACOTTA;
        ScanningManager.addScannableThing(new ScanBlock("f_MATCLAY",
                Blocks.TERRACOTTA, dyedTerracotta.white(), dyedTerracotta.orange(),
                dyedTerracotta.magenta(), dyedTerracotta.lightBlue(), dyedTerracotta.yellow(),
                dyedTerracotta.lime(), dyedTerracotta.pink(), dyedTerracotta.gray(),
                dyedTerracotta.lightGray(), dyedTerracotta.cyan(), dyedTerracotta.purple(),
                dyedTerracotta.blue(), dyedTerracotta.brown(), dyedTerracotta.green(),
                dyedTerracotta.red(), dyedTerracotta.black()));
        ScanningManager.addScannableThing(new ScanOreDictionary("f_MATIRON",
                new String[]{"oreIron", "ingotIron", "blockIron", "plateIron"}));
        ScanningManager.addScannableThing(new ScanOreDictionary("f_MATBRASS",
                new String[]{"ingotBrass", "blockBrass", "plateBrass"}));
        ScanningManager.addScannableThing(new ScanOreDictionary("f_MATTHAUMIUM",
                new String[]{"ingotThaumium", "blockThaumium", "plateThaumium"}));
        ScanningManager.addScannableThing(new ScanOreDictionary("f_MATVOID",
                new String[]{"ingotVoid", "blockVoid", "plateVoid"}));

        ScanningManager.addScannableThing(new ScanBlock("f_TELEPORT",
                Blocks.NETHER_PORTAL, Blocks.END_PORTAL, Blocks.END_PORTAL_FRAME));
        ScanningManager.addScannableThing(new ScanBlock("f_DISPENSER", Blocks.DISPENSER));
        ScanningManager.addScannableThing(new ScanItem("f_DISPENSER", new ItemStack(Blocks.DISPENSER)));

        ScanningManager.addScannableThing(new ScanSky());

        ScanningManager.addScannableThing(new ScanBlock("!arcaneresearch:aura_node",
                AuraBlocks.AURA_NODE.get()));

        Arcaneresearch.LOGGER.info("Registered {} scannable objects", ScanningManager.getScannableCount());
    }

    private static void initTheorycraft() {
        TheoryRegistry.init();

        Arcaneresearch.LOGGER.info("Registered {} theorycraft cards and {} aids",
                TheorycraftManager.cards.size(), TheorycraftManager.aids.size());
    }

    private static void initWarp() {

        ArcaneresearchApi.addWarpToItem(new ItemStack(ModBlocks.JAR_BRAIN.get()), 1);
    }

    public static void checkPeriodicResearch(Player player) {
        if (player.level().isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
        if (knowledge == null) return;

        // NEOFORGE_PORT: ResourceKey.location() -> identifier() in 26.x.
        Identifier dimKey = player.level().dimension().identifier();

        // NEOFORGE_PORT (26.3): Forge displayClientMessage(msg, true) = action-bar overlay ->
        // sendOverlayMessage (was sendSystemMessage = chat).
        if (!knowledge.isResearchKnown("m_hellandback") && dimKey.getPath().contains("nether")) {
            knowledge.addResearch("m_hellandback");
            knowledge.sync(serverPlayer);
            player.sendOverlayMessage(
                    net.minecraft.network.chat.Component.literal("\u00A75" +
                            net.minecraft.network.chat.Component.translatable("got.hellandback").getString()));
        }

        if (!knowledge.isResearchKnown("m_endoftheworld") && dimKey.getPath().contains("end")) {
            knowledge.addResearch("m_endoftheworld");
            knowledge.sync(serverPlayer);
            player.sendOverlayMessage(
                    net.minecraft.network.chat.Component.literal("\u00A75" +
                            net.minecraft.network.chat.Component.translatable("got.endoftheworld").getString()));
        }

        if (knowledge.isResearchKnown("UNLOCKAUROMANCY@1") && !knowledge.isResearchKnown("UNLOCKAUROMANCY@2")) {

            if (player.getY() < 10 && !knowledge.isResearchKnown("m_deepdown")) {
                knowledge.addResearch("m_deepdown");
                knowledge.sync(serverPlayer);
                player.sendOverlayMessage(
                        net.minecraft.network.chat.Component.literal("\u00A75" +
                                net.minecraft.network.chat.Component.translatable("got.deepdown").getString()));
            }

            // NEOFORGE_PORT (26.3): Level.getMaxBuildHeight() (= minY + height, exclusive top, 320 in the
            // overworld) gone -> getMaxY() + 1 (same value; dimensionType().height() was 384).
            int worldHeight = player.level().getMaxY() + 1;
            if (player.getY() > worldHeight * 0.4 && !knowledge.isResearchKnown("m_uphigh")) {
                knowledge.addResearch("m_uphigh");
                knowledge.sync(serverPlayer);
                player.sendOverlayMessage(
                        net.minecraft.network.chat.Component.literal("\u00A75" +
                                net.minecraft.network.chat.Component.translatable("got.uphigh").getString()));
            }
        }

        var stats = serverPlayer.getStats();
        if (!knowledge.isResearchKnown("m_walker")
                && stats.getValue(net.minecraft.stats.Stats.CUSTOM, net.minecraft.stats.Stats.WALK_ONE_CM) > 160000) {
            knowledge.addResearch("m_walker");
            knowledge.sync(serverPlayer);
        }
        if (!knowledge.isResearchKnown("m_runner")
                && stats.getValue(net.minecraft.stats.Stats.CUSTOM, net.minecraft.stats.Stats.SPRINT_ONE_CM) > 80000) {
            knowledge.addResearch("m_runner");
            knowledge.sync(serverPlayer);
        }
        if (!knowledge.isResearchKnown("m_jumper")
                && stats.getValue(net.minecraft.stats.Stats.CUSTOM, net.minecraft.stats.Stats.JUMP) > 500) {
            knowledge.addResearch("m_jumper");
            knowledge.sync(serverPlayer);
        }
        if (!knowledge.isResearchKnown("m_swimmer")
                && stats.getValue(net.minecraft.stats.Stats.CUSTOM, net.minecraft.stats.Stats.SWIM_ONE_CM) > 8000) {
            knowledge.addResearch("m_swimmer");
            knowledge.sync(serverPlayer);
        }
    }
}
