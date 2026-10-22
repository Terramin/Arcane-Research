package arcaneresearch;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterSelectItemModelPropertyEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import arcaneresearch.helper.TintHelper;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModItems;
import arcaneresearch.init.ModCreativeTabs;
import arcaneresearch.init.ModEntities;
import arcaneresearch.init.ModEffects;
import arcaneresearch.init.ModSounds;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModMenuTypes;
import arcaneresearch.init.ModRecipeTypes;
import arcaneresearch.init.ModRecipeSerializers;
import arcaneresearch.init.ModFeatures;
import arcaneresearch.init.ModFluids;
import arcaneresearch.init.ModStructures;
import arcaneresearch.init.ModIngredientTypes;
import arcaneresearch.init.ModLootModifiers;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.items.casters.FocusInit;
import arcaneresearch.common.golems.GolemProperties;
import arcaneresearch.common.golems.seals.SealHandler;
import arcaneresearch.common.lib.InternalMethodHandler;
import arcaneresearch.common.config.ConfigResearch;
import arcaneresearch.common.config.ConfigAspects;
import arcaneresearch.common.config.ConfigMultiblocks;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.client.lib.events.KeyHandler;
import arcaneresearch.client.models.armor.CultistPlateArmorModel;
import arcaneresearch.client.models.armor.CultistRobeArmorModel;
import arcaneresearch.client.models.armor.FortressArmorModel;
import arcaneresearch.client.models.armor.LeaderArmorModel;
import arcaneresearch.client.models.armor.RobeArmorModel;
import arcaneresearch.client.models.armor.ThaumiumArmorModel;
import arcaneresearch.client.models.armor.VoidArmorModel;
import arcaneresearch.client.models.armor.VoidRobeArmorModel;













@Mod(Arcaneresearch.MODID)
public class Arcaneresearch {

    public static final String MODID = "arcaneresearch";
    public static final String MODNAME = "Arcaneresearch";
    public static final String VERSION = "6.2.0";

    public static final Logger LOGGER = LogUtils.getLogger();

    private static Arcaneresearch instance;

    public Arcaneresearch(IEventBus modEventBus, ModContainer modContainer) {
        instance = this;

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::processIMC);
        modEventBus.addListener(this::registerEntityAttributes);
        
        modEventBus.addListener(PacketHandler::register);
        
        
        
        modEventBus.addListener(ModBlockEntities::registerCapabilities);

        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.BLOCK_ITEMS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModEffects.MOB_EFFECTS.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);

        arcaneresearch.common.items.wands.WandItems.ITEMS.register(modEventBus);
        arcaneresearch.common.menu.WandMenuTypes.TYPES.register(modEventBus);
        arcaneresearch.common.entities.WandEntities.ENTITY_TYPES.register(modEventBus);

        arcaneresearch.common.blocks.aura.AuraBlocks.BLOCKS.register(modEventBus);
        arcaneresearch.common.blocks.aura.AuraBlocks.BLOCK_ITEMS.register(modEventBus);
        arcaneresearch.common.blocks.aura.AuraBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModRecipeTypes.RECIPE_TYPES.register(modEventBus);
        ModRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        
        
        ModIngredientTypes.register(modEventBus);
        ModLootModifiers.LOOT_MODIFIERS.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);
        ModStructures.STRUCTURE_TYPES.register(modEventBus);
        ModStructures.STRUCTURE_PIECE_TYPES.register(modEventBus);
        
        
        arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
        
        
        
        
        arcaneresearch.common.world.aura.AuraChunkHandler.register();

    }

    public static Arcaneresearch getInstance() {
        return instance;
    }

    private void registerEntityAttributes(final EntityAttributeModificationEvent event) {
        
        
        if (!event.has(net.minecraft.world.entity.EntityTypes.PLAYER, Attributes.STEP_HEIGHT)) {
            event.add(net.minecraft.world.entity.EntityTypes.PLAYER, Attributes.STEP_HEIGHT);
        }
    }

    private void processIMC(final InterModProcessEvent event) {
        arcaneresearch.proxies.Proxies.get().checkInterModComs(event);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

        LOGGER.info("Arcaneresearch common setup");

        ArcaneresearchApi.internalMethods = new InternalMethodHandler();
        LOGGER.info("Initialized ArcaneresearchApi internal methods");

        
        
        
        
        
        
        
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

        
        
        runContentSetupOnce();

        
        
        arcaneresearch.common.config.ConfigEntities.initPechBookTrades(event.getServer().registryAccess());
        
        arcaneresearch.common.config.ConfigResearch.initEnchantmentScans(event.getServer().registryAccess());
        
        
        arcaneresearch.common.config.ConfigAspects.registerPotionAspects();

        LOGGER.info("Arcaneresearch server starting");
    }

    
    
    
    
    
    @SubscribeEvent
    public void onDefaultComponentsBound(net.neoforged.neoforge.event.DefaultDataComponentsBoundEvent event) {
        if (event.shouldUpdateStaticData()) {
            runContentSetupOnce();
        }
    }

    private static boolean contentSetupDone = false;

    
    
    
    
    
    private static synchronized void runContentSetupOnce() {
        if (contentSetupDone) {
            return;
        }
        contentSetupDone = true;

        arcaneresearch.common.world.biomes.BiomeHandler.registerBiomeInfo();
        LOGGER.info("Registered Arcaneresearch biome info");

        FocusInit.registerFoci();
        LOGGER.info("Registered {} focus elements", FocusInit.getAllFocusKeys().length);

        arcaneresearch.common.items.wands.WandItems.initWands();

        arcaneresearch.common.lib.aura.ScanNode.register();

        GolemProperties.registerDefaultParts();
        LOGGER.info("Registered golem parts");

        SealHandler.registerDefaultSeals();

        ConfigResearch.init();

        ConfigAspects.init();

        arcaneresearch.common.config.ModConfig.modCompatibility();

        arcaneresearch.common.config.ConfigEntities.initPechTrades();

        arcaneresearch.common.config.ConfigBlocks.initMisc();

        arcaneresearch.common.config.ConfigItems.initMisc();

        arcaneresearch.api.items.ItemsTC.init();

        arcaneresearch.common.config.ModConfig.postInitLoot();

        arcaneresearch.common.config.ConfigEntities.postInitEntitySpawns();

        ConfigMultiblocks.init();

        arcaneresearch.init.ModBlocks.addFlowerPotPlants();
        LOGGER.info("Registered Arcaneresearch flower pot plants");

        arcaneresearch.common.config.ConfigRecipes.registerFakeRecipes();

        arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager.registerDynamicRecipes();

        arcaneresearch.common.config.ConfigRecipes.registerSmeltingBonuses();

        arcaneresearch.common.config.ModConfig.postInitMisc();

        arcaneresearch.common.lib.BehaviorDispenseAlumetum.register();

        
        
        
        
        ConfigResearch.postInit();

        LOGGER.info("Arcaneresearch setup complete!");
    }

    @EventBusSubscriber(modid = MODID)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerEntityAttributes(EntityAttributeCreationEvent event) {


            event.put(ModEntities.WISP.get(),
                    arcaneresearch.common.entities.monster.EntityWisp.createAttributes().build());
            event.put(ModEntities.FIRE_BAT.get(),
                    arcaneresearch.common.entities.monster.EntityFireBat.createAttributes().build());

            event.put(arcaneresearch.common.entities.WandEntities.HELLBAT.get(),
                    arcaneresearch.common.entities.monster.EntityFireBat.createAttributes().build());
            event.put(ModEntities.BRAINY_ZOMBIE.get(),
                    arcaneresearch.common.entities.monster.EntityBrainyZombie.createAttributes().build());
            event.put(ModEntities.MIND_SPIDER.get(),
                    arcaneresearch.common.entities.monster.EntityMindSpider.createAttributes().build());
            event.put(ModEntities.THAUMIC_SLIME.get(),
                    arcaneresearch.common.entities.monster.EntityThaumicSlime.createAttributes().build());
            event.put(ModEntities.GIANT_BRAINY_ZOMBIE.get(),
                    arcaneresearch.common.entities.monster.EntityGiantBrainyZombie.createAttributes().build());
            event.put(ModEntities.INHABITED_ZOMBIE.get(),
                    arcaneresearch.common.entities.monster.EntityInhabitedZombie.createAttributes().build());

            event.put(ModEntities.ELDRITCH_CRAB.get(),
                    arcaneresearch.common.entities.monster.EntityEldritchCrab.createAttributes().build());
            event.put(ModEntities.SPELL_BAT.get(),
                    arcaneresearch.common.entities.monster.EntitySpellBat.createAttributes().build());

            event.put(ModEntities.TAINT_CRAWLER.get(),
                    arcaneresearch.common.entities.monster.tainted.EntityTaintCrawler.createAttributes().build());
            event.put(ModEntities.TAINT_SWARM.get(),
                    arcaneresearch.common.entities.monster.tainted.EntityTaintSwarm.createAttributes().build());
            event.put(ModEntities.TAINTACLE.get(),
                    arcaneresearch.common.entities.monster.tainted.EntityTaintacle.createAttributes().build());
            event.put(ModEntities.TAINTACLE_SMALL.get(),
                    arcaneresearch.common.entities.monster.tainted.EntityTaintacleSmall.createAttributes().build());
            event.put(ModEntities.TAINT_SEED.get(),
                    arcaneresearch.common.entities.monster.tainted.EntityTaintSeed.createAttributes().build());
            event.put(ModEntities.TAINT_SEED_PRIME.get(),
                    arcaneresearch.common.entities.monster.tainted.EntityTaintSeedPrime.createAttributes().build());

            event.put(ModEntities.ELDRITCH_GUARDIAN.get(),
                    arcaneresearch.common.entities.monster.EntityEldritchGuardian.createAttributes().build());

            event.put(ModEntities.CULTIST.get(),
                    arcaneresearch.common.entities.monster.cult.EntityCultist.createAttributes().build());
            event.put(ModEntities.CULTIST_KNIGHT.get(),
                    arcaneresearch.common.entities.monster.cult.EntityCultistKnight.createAttributes().build());
            event.put(ModEntities.CULTIST_CLERIC.get(),
                    arcaneresearch.common.entities.monster.cult.EntityCultistCleric.createAttributes().build());
            event.put(ModEntities.CULTIST_PORTAL_LESSER.get(),
                    arcaneresearch.common.entities.monster.cult.EntityCultistPortalLesser.createAttributes().build());

            event.put(ModEntities.CULTIST_LEADER.get(),
                    arcaneresearch.common.entities.monster.boss.EntityCultistLeader.createAttributes().build());
            event.put(ModEntities.TAINTACLE_GIANT.get(),
                    arcaneresearch.common.entities.monster.boss.EntityTaintacleGiant.createAttributes().build());
            event.put(ModEntities.CULTIST_PORTAL_GREATER.get(),
                    arcaneresearch.common.entities.monster.boss.EntityCultistPortalGreater.createAttributes().build());
            event.put(ModEntities.ELDRITCH_GOLEM.get(),
                    arcaneresearch.common.entities.monster.boss.EntityEldritchGolem.createAttributes().build());
            event.put(ModEntities.ELDRITCH_WARDEN.get(),
                    arcaneresearch.common.entities.monster.boss.EntityEldritchWarden.createAttributes().build());

            event.put(ModEntities.PECH.get(),
                    arcaneresearch.common.entities.monster.EntityPech.createAttributes().build());

            event.put(ModEntities.TURRET_CROSSBOW.get(),
                    arcaneresearch.common.entities.construct.EntityTurretCrossbow.createAttributes().build());
            event.put(ModEntities.TURRET_CROSSBOW_ADVANCED.get(),
                    arcaneresearch.common.entities.construct.EntityTurretCrossbowAdvanced.createAttributes().build());
            event.put(ModEntities.ARCANE_BORE.get(),
                    arcaneresearch.common.entities.construct.EntityArcaneBore.createAttributes().build());

            event.put(ModEntities.ARCANERESEARCH_GOLEM.get(),
                    arcaneresearch.common.golems.EntityArcaneresearchGolem.createAttributes().build());

            LOGGER.info("Registered Arcaneresearch entity attributes");
        }
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onAddPackFinders(AddPackFindersEvent event) {
            if (event.getPackType() != PackType.CLIENT_RESOURCES) {
                return;
            }
            
            
            
            event.addRepositorySource(packConsumer -> {
                try {
                    java.nio.file.Path packPath = findLegacyPackPath();
                    if (packPath == null) {
                        return;
                    }
                    net.minecraft.server.packs.repository.Pack pack =
                            net.minecraft.server.packs.repository.Pack.readMetaAndCreate(
                                    new net.minecraft.server.packs.PackLocationInfo(
                                            "builtin/arcaneresearch_legacy_textures",
                                            Component.literal("Arcaneresearch Legacy Textures"),
                                            net.minecraft.server.packs.repository.PackSource.BUILT_IN,
                                            java.util.Optional.empty()),
                                    new net.minecraft.server.packs.PathPackResources.PathResourcesSupplier(packPath),
                                    PackType.CLIENT_RESOURCES,
                                    new net.minecraft.server.packs.PackSelectionConfig(false,
                                            net.minecraft.server.packs.repository.Pack.Position.TOP, false));
                    if (pack != null) {
                        packConsumer.accept(pack);
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to register legacy textures pack", e);
                }
            });
        }

        private static java.nio.file.Path findLegacyPackPath() {
            try {
                for (java.nio.file.Path root : net.neoforged.fml.ModList.get()
                        .getModFileById(MODID).getFile().getContents().getContentRoots()) {
                    java.nio.file.Path candidate = root.resolve("resourcepacks/legacy-textures");
                    if (candidate != null && java.nio.file.Files.isDirectory(candidate)) {
                        return candidate;
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to locate legacy textures pack", e);
            }
            return null;
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

            LOGGER.info("Arcaneresearch client setup");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());

            
            
            
            
            
            

        }

        @SubscribeEvent
        public static void onRegisterItemModelProperties(RegisterSelectItemModelPropertyEvent event) {
            
            
            
            
            event.register(
                    Identifier.fromNamespaceAndPath("minecraft", "pulling"),
                    BoneBowPulling.TYPE
            );
            event.register(
                    Identifier.fromNamespaceAndPath("minecraft", "pull"),
                    BoneBowPull.TYPE
            );
        }

        @SubscribeEvent
        public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
            
            event.register(arcaneresearch.init.ModMenuTypes.GOLEM_BUILDER.get(),
                    arcaneresearch.client.gui.screens.GolemBuilderScreen::new);
            event.register(arcaneresearch.init.ModMenuTypes.ARCANE_WORKBENCH.get(),
                    arcaneresearch.client.gui.screens.ArcaneWorkbenchScreen::new);
            event.register(arcaneresearch.init.ModMenuTypes.THAUMATORIUM.get(),
                    arcaneresearch.client.gui.screens.ThaumatoriumScreen::new);
            event.register(arcaneresearch.init.ModMenuTypes.SMELTER.get(),
                    arcaneresearch.client.gui.screens.SmelterScreen::new);
            event.register(arcaneresearch.init.ModMenuTypes.RESEARCH_TABLE.get(),
                    arcaneresearch.client.gui.screens.ResearchTableScreen::new);
            
            event.register(arcaneresearch.init.ModMenuTypes.DECONSTRUCTION_TABLE.get(),
                    arcaneresearch.client.gui.screens.DeconstructScreen::new);
            event.register(arcaneresearch.init.ModMenuTypes.FOCAL_MANIPULATOR.get(),
                    arcaneresearch.client.gui.screens.FocalManipulatorScreen::new);
            event.register(arcaneresearch.init.ModMenuTypes.FOCUS_POUCH.get(),
                    arcaneresearch.client.gui.screens.FocusPouchScreen::new);
            event.register(arcaneresearch.init.ModMenuTypes.HAND_MIRROR.get(),
                    arcaneresearch.client.gui.screens.HandMirrorScreen::new);
            event.register(arcaneresearch.init.ModMenuTypes.POTION_SPRAYER.get(),
                    arcaneresearch.client.gui.screens.PotionSprayerScreen::new);
            event.register(arcaneresearch.init.ModMenuTypes.SPA.get(),
                    arcaneresearch.client.gui.screens.SpaScreen::new);
            event.register(arcaneresearch.init.ModMenuTypes.VOID_SIPHON.get(),
                    arcaneresearch.client.gui.screens.VoidSiphonScreen::new);
            event.register(arcaneresearch.init.ModMenuTypes.TURRET_BASIC.get(),
                    arcaneresearch.client.gui.screens.TurretScreen::new);
            event.register(arcaneresearch.init.ModMenuTypes.TURRET_ADVANCED.get(),
                    arcaneresearch.client.gui.screens.TurretScreen::new);
            event.register(arcaneresearch.init.ModMenuTypes.ARCANE_BORE.get(),
                    arcaneresearch.client.gui.screens.ArcaneBoreScreen::new);
            event.register(arcaneresearch.init.ModMenuTypes.PECH_TRADING.get(),
                    arcaneresearch.client.gui.screens.PechScreen::new);
            event.register(arcaneresearch.init.ModMenuTypes.HUNGRY_CHEST.get(),
                    arcaneresearch.client.gui.screens.HungryChestScreen::new);
            event.register(arcaneresearch.init.ModMenuTypes.LOGISTICS.get(),
                    arcaneresearch.client.gui.screens.LogisticsScreen::new);
            event.register(arcaneresearch.init.ModMenuTypes.SEAL.get(),
                    arcaneresearch.client.gui.screens.SealScreen::new);
            event.register(arcaneresearch.init.ModMenuTypes.AURA_TOTEM.get(),
                    arcaneresearch.client.gui.screens.AuraTotemScreen::new);
            LOGGER.info("Registered Arcaneresearch menu screens");
        }

        @SubscribeEvent
        public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {


            event.registerLayerDefinition(
                arcaneresearch.client.models.block.JarNodeModel.LAYER_LOCATION,
                arcaneresearch.client.models.block.JarNodeModel::createBodyLayer
            );
            event.registerLayerDefinition(
                arcaneresearch.client.models.armor.GogglesHeadModel.LAYER_LOCATION,
                arcaneresearch.client.models.armor.GogglesHeadModel::createBodyLayer
            );
            event.registerLayerDefinition(
                arcaneresearch.client.models.entity.GolemModel.LAYER_LOCATION,
                arcaneresearch.client.models.entity.GolemModel::createBodyLayer
            );
            event.registerLayerDefinition(
                arcaneresearch.client.models.entity.CrossbowModel.LAYER_LOCATION,
                arcaneresearch.client.models.entity.CrossbowModel::createBodyLayer
            );

            event.registerLayerDefinition(
                arcaneresearch.client.models.entity.ArcaneBoreModel.LAYER_LOCATION,
                arcaneresearch.client.models.entity.ArcaneBoreModel::createBodyLayer
            );
            event.registerLayerDefinition(
                arcaneresearch.client.models.entity.PechModel.LAYER_LOCATION,
                arcaneresearch.client.models.entity.PechModel::createBodyLayer
            );
            event.registerLayerDefinition(
                arcaneresearch.client.models.entity.TaintacleModel.LAYER_LOCATION,
                arcaneresearch.client.models.entity.TaintacleModel::createBodyLayer
            );
            event.registerLayerDefinition(
                arcaneresearch.client.models.entity.TaintSeedModel.LAYER_LOCATION,
                arcaneresearch.client.models.entity.TaintSeedModel::createBodyLayer
            );
            event.registerLayerDefinition(
                arcaneresearch.client.models.entity.EldritchGolemModel.LAYER_LOCATION,
                arcaneresearch.client.models.entity.EldritchGolemModel::createBodyLayer
            );
            event.registerLayerDefinition(
                arcaneresearch.client.models.entity.EldritchCrabModel.LAYER_LOCATION,
                arcaneresearch.client.models.entity.EldritchCrabModel::createBodyLayer
            );
            event.registerLayerDefinition(
                arcaneresearch.client.models.entity.EldritchGuardianModel.LAYER_LOCATION,
                arcaneresearch.client.models.entity.EldritchGuardianModel::createBodyLayer
            );
            event.registerLayerDefinition(
                arcaneresearch.client.models.entity.FireBatModel.LAYER_LOCATION,
                arcaneresearch.client.models.entity.FireBatModel::createBodyLayer
            );
            event.registerLayerDefinition(
                arcaneresearch.client.models.entity.SpellBatModel.LAYER_LOCATION,
                arcaneresearch.client.models.entity.SpellBatModel::createBodyLayer
            );
            event.registerLayerDefinition(
                arcaneresearch.client.models.entity.GrapplerModel.LAYER_LOCATION,
                arcaneresearch.client.models.entity.GrapplerModel::createBodyLayer
            );
            event.registerLayerDefinition(
                LeaderArmorModel.LAYER_LOCATION,
                LeaderArmorModel::createBodyLayer
            );
            event.registerLayerDefinition(
                FortressArmorModel.LAYER_LOCATION,
                FortressArmorModel::createBodyLayer
            );
            event.registerLayerDefinition(
                CultistRobeArmorModel.LAYER_LOCATION,
                CultistRobeArmorModel::createBodyLayer
            );
            event.registerLayerDefinition(
                CultistPlateArmorModel.LAYER_LOCATION,
                CultistPlateArmorModel::createBodyLayer
            );
            event.registerLayerDefinition(
                ThaumiumArmorModel.LAYER_LOCATION,
                ThaumiumArmorModel::createBodyLayer
            );
            event.registerLayerDefinition(
                ThaumiumArmorModel.INNER_LAYER_LOCATION,
                ThaumiumArmorModel::createInnerBodyLayer
            );
            event.registerLayerDefinition(
                VoidArmorModel.LAYER_LOCATION,
                VoidArmorModel::createBodyLayer
            );
            event.registerLayerDefinition(
                VoidArmorModel.INNER_LAYER_LOCATION,
                VoidArmorModel::createInnerBodyLayer
            );
            event.registerLayerDefinition(
                VoidRobeArmorModel.LAYER_LOCATION,
                VoidRobeArmorModel::createBodyLayer
            );
            event.registerLayerDefinition(
                RobeArmorModel.LAYER_LOCATION,
                RobeArmorModel::createBodyLayer
            );
            event.registerLayerDefinition(
                RobeArmorModel.INNER_LAYER_LOCATION,
                RobeArmorModel::createInnerBodyLayer
            );
            event.registerLayerDefinition(
                arcaneresearch.client.models.block.JarModel.LAYER_LOCATION,
                arcaneresearch.client.models.block.JarModel::createBodyLayer
            );

            event.registerLayerDefinition(
                arcaneresearch.client.models.block.CentrifugeModel.LAYER_LOCATION,
                arcaneresearch.client.models.block.CentrifugeModel::createBodyLayer
            );
            event.registerLayerDefinition(
                arcaneresearch.client.models.block.BellowsModel.LAYER_LOCATION,
                arcaneresearch.client.models.block.BellowsModel::createBodyLayer
            );
            event.registerLayerDefinition(
                arcaneresearch.client.models.block.BellowsNozzleModel.LAYER_LOCATION,
                arcaneresearch.client.models.block.BellowsNozzleModel::createBodyLayer
            );
            event.registerLayerDefinition(
                arcaneresearch.client.models.block.TubeValveModel.LAYER_LOCATION,
                arcaneresearch.client.models.block.TubeValveModel::createBodyLayer
            );
            event.registerLayerDefinition(
                arcaneresearch.client.models.block.BrainModel.LAYER_LOCATION,
                arcaneresearch.client.models.block.BrainModel::createBodyLayer
            );
            event.registerLayerDefinition(
                arcaneresearch.client.models.block.BannerModel.LAYER_LOCATION,
                arcaneresearch.client.models.block.BannerModel::createBodyLayer
            );
            event.registerLayerDefinition(
                arcaneresearch.client.models.block.ResearchTableModel.LAYER_LOCATION,
                arcaneresearch.client.models.block.ResearchTableModel::createBodyLayer
            );
            LOGGER.info("Registered Arcaneresearch model layers");
        }

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            
            
            event.registerBlockEntityRenderer(
                ModBlockEntities.JAR.get(),
                arcaneresearch.client.renderers.tile.JarRenderer::new
            );
            event.registerBlockEntityRenderer(
                arcaneresearch.common.blocks.aura.AuraBlockEntities.ELDRITCH_PORTAL.get(),
                arcaneresearch.client.renderers.tile.EldritchPortalRenderer::new
            );
            event.registerBlockEntityRenderer(
                arcaneresearch.common.blocks.aura.AuraBlockEntities.ELDRITCH_ALTAR.get(),
                arcaneresearch.client.renderers.tile.EldritchAltarRenderer::new
            );
            event.registerBlockEntityRenderer(
                arcaneresearch.common.blocks.aura.AuraBlockEntities.ELDRITCH_OBELISK.get(),
                arcaneresearch.client.renderers.tile.EldritchObeliskRenderer::new
            );
            event.registerBlockEntityRenderer(
                arcaneresearch.common.blocks.aura.AuraBlockEntities.ELDRITCH_CAP.get(),
                arcaneresearch.client.renderers.tile.EldritchCapRenderer::new
            );
            event.registerBlockEntityRenderer(
                arcaneresearch.common.blocks.aura.AuraBlockEntities.ELDRITCH_CRAB.get(),
                arcaneresearch.client.renderers.tile.EldritchCrabRenderer::new
            );
            event.registerBlockEntityRenderer(
                ModBlockEntities.JAR_VOID.get(),
                arcaneresearch.client.renderers.tile.JarRenderer::new
            );

            
            event.registerBlockEntityRenderer(
                ModBlockEntities.DECONSTRUCTION_TABLE.get(),
                arcaneresearch.client.renderers.tile.DeconstructTileRenderer::new
            );

            event.registerBlockEntityRenderer(
                ModBlockEntities.PEDESTAL.get(),
                arcaneresearch.client.renderers.tile.PedestalRenderer::new
            );
            event.registerBlockEntityRenderer(
                ModBlockEntities.RECHARGE_PEDESTAL.get(),
                arcaneresearch.client.renderers.tile.RechargePedestalRenderer::new
            );

            event.registerBlockEntityRenderer(
                ModBlockEntities.CRUCIBLE.get(),
                arcaneresearch.client.renderers.tile.CrucibleRenderer::new
            );
            event.registerBlockEntityRenderer(
                ModBlockEntities.INFUSION_MATRIX.get(),
                arcaneresearch.client.renderers.tile.InfusionMatrixRenderer::new
            );
            event.registerBlockEntityRenderer(
                ModBlockEntities.RESEARCH_TABLE.get(),
                arcaneresearch.client.renderers.tile.ResearchTableRenderer::new
            );

            event.registerBlockEntityRenderer(
                ModBlockEntities.CENTRIFUGE.get(),
                arcaneresearch.client.renderers.tile.CentrifugeRenderer::new
            );
            event.registerBlockEntityRenderer(
                ModBlockEntities.ALEMBIC.get(),
                arcaneresearch.client.renderers.tile.AlembicRenderer::new
            );

            event.registerBlockEntityRenderer(
                ModBlockEntities.BELLOWS.get(),
                arcaneresearch.client.renderers.tile.BellowsRenderer::new
            );
            event.registerBlockEntityRenderer(
                ModBlockEntities.GOLEM_BUILDER.get(),
                arcaneresearch.client.renderers.tile.GolemBuilderRenderer::new
            );
            event.registerBlockEntityRenderer(
                ModBlockEntities.MIRROR_ITEM.get(),
                arcaneresearch.client.renderers.tile.MirrorRenderer::new
            );
            event.registerBlockEntityRenderer(
                ModBlockEntities.MIRROR_ESSENTIA.get(),
                arcaneresearch.client.renderers.tile.EssentiaMirrorRenderer::new
            );

            event.registerBlockEntityRenderer(
                ModBlockEntities.TUBE_VALVE.get(),
                arcaneresearch.client.renderers.tile.TubeValveRenderer::new
            );
            event.registerBlockEntityRenderer(
                ModBlockEntities.TUBE_BUFFER.get(),
                arcaneresearch.client.renderers.tile.TubeBufferRenderer::new
            );
            event.registerBlockEntityRenderer(
                ModBlockEntities.TUBE_ONEWAY.get(),
                arcaneresearch.client.renderers.tile.TubeOnewayRenderer::new
            );

            event.registerBlockEntityRenderer(
                ModBlockEntities.JAR_BRAIN.get(),
                arcaneresearch.client.renderers.tile.JarBrainRenderer::new
            );

            event.registerBlockEntityRenderer(
                ModBlockEntities.BANNER.get(),
                arcaneresearch.client.renderers.tile.BannerRenderer::new
            );

            event.registerBlockEntityRenderer(
                ModBlockEntities.HUNGRY_CHEST.get(),
                arcaneresearch.client.renderers.tile.HungryChestRenderer::new
            );

            event.registerBlockEntityRenderer(
                ModBlockEntities.VOID_SIPHON.get(),
                arcaneresearch.client.renderers.tile.VoidSiphonRenderer::new
            );

            event.registerBlockEntityRenderer(
                ModBlockEntities.DIOPTRA.get(),
                arcaneresearch.client.renderers.tile.DioptraRenderer::new
            );
            event.registerBlockEntityRenderer(
                ModBlockEntities.FOCAL_MANIPULATOR.get(),
                arcaneresearch.client.renderers.tile.FocalManipulatorRenderer::new
            );
            event.registerBlockEntityRenderer(
                ModBlockEntities.HOLE.get(),
                arcaneresearch.client.renderers.tile.HoleRenderer::new
            );
            event.registerBlockEntityRenderer(
                ModBlockEntities.PATTERN_CRAFTER.get(),
                arcaneresearch.client.renderers.tile.PatternCrafterRenderer::new
            );
            event.registerBlockEntityRenderer(
                ModBlockEntities.THAUMATORIUM.get(),
                arcaneresearch.client.renderers.tile.ThaumatoriumRenderer::new
            );
            event.registerBlockEntityRenderer(
                arcaneresearch.common.blocks.aura.AuraBlockEntities.NODE_CONVERTER.get(),
                arcaneresearch.client.renderers.tile.NodeConverterRenderer::new
            );
            event.registerBlockEntityRenderer(
                arcaneresearch.common.blocks.aura.AuraBlockEntities.STABILIZER_TILE.get(),
                arcaneresearch.client.renderers.tile.StabilizerRenderer::new
            );
            event.registerBlockEntityRenderer(
                arcaneresearch.common.blocks.aura.AuraBlockEntities.TRANSDUCER_TILE.get(),
                arcaneresearch.client.renderers.tile.TransducerRenderer::new
            );
            event.registerBlockEntityRenderer(
                arcaneresearch.common.blocks.aura.AuraBlockEntities.ENERGIZED_NODE.get(),
                arcaneresearch.client.renderers.tile.EnergizedNodeRenderer::new
            );
            LOGGER.info("Registered Arcaneresearch block entity renderers");
            event.registerEntityRenderer(
                    ModEntities.ARCANERESEARCH_GOLEM.get(),
                    arcaneresearch.client.renderers.entity.GolemRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.TURRET_CROSSBOW.get(),
                    arcaneresearch.client.renderers.entity.TurretCrossbowRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.TURRET_CROSSBOW_ADVANCED.get(),
                    arcaneresearch.client.renderers.entity.TurretCrossbowAdvancedRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.ARCANE_BORE.get(),
                    arcaneresearch.client.renderers.entity.ArcaneBoreRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.PECH.get(),
                    arcaneresearch.client.renderers.entity.PechRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.WISP.get(),
                    arcaneresearch.client.renderers.entity.WispRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.FLUX_RIFT.get(),
                    arcaneresearch.client.renderers.entity.FluxRiftRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.BRAINY_ZOMBIE.get(),
                    arcaneresearch.client.renderers.entity.BrainyZombieRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.GIANT_BRAINY_ZOMBIE.get(),
                    arcaneresearch.client.renderers.entity.BrainyZombieRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.THAUMIC_SLIME.get(),
                    arcaneresearch.client.renderers.entity.ThaumicSlimeRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.MIND_SPIDER.get(),
                    arcaneresearch.client.renderers.entity.MindSpiderRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.FIRE_BAT.get(),
                    arcaneresearch.client.renderers.entity.FireBatRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.CULTIST.get(),
                    arcaneresearch.client.renderers.entity.CultistRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.CULTIST_KNIGHT.get(),
                    arcaneresearch.client.renderers.entity.CultistRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.CULTIST_CLERIC.get(),
                    arcaneresearch.client.renderers.entity.CultistRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.TAINT_CRAWLER.get(),
                    arcaneresearch.client.renderers.entity.TaintCrawlerRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.TAINT_SWARM.get(),
                    arcaneresearch.client.renderers.entity.TaintSwarmRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.TAINTACLE.get(),
                    arcaneresearch.client.renderers.entity.TaintacleRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.TAINTACLE_SMALL.get(),
                    arcaneresearch.client.renderers.entity.TaintacleRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.TAINTACLE_GIANT.get(),
                    arcaneresearch.client.renderers.entity.TaintacleRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.TAINT_SEED.get(),
                    arcaneresearch.client.renderers.entity.TaintSeedRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.TAINT_SEED_PRIME.get(),
                    arcaneresearch.client.renderers.entity.TaintSeedRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.ELDRITCH_CRAB.get(),
                    arcaneresearch.client.renderers.entity.EldritchCrabRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.ELDRITCH_GUARDIAN.get(),
                    arcaneresearch.client.renderers.entity.EldritchGuardianRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.ELDRITCH_GOLEM.get(),
                    arcaneresearch.client.renderers.entity.EldritchGolemRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.SPELL_BAT.get(),
                    arcaneresearch.client.renderers.entity.SpellBatRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.INHABITED_ZOMBIE.get(),
                    arcaneresearch.client.renderers.entity.InhabitedZombieRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.CULTIST_LEADER.get(),
                    arcaneresearch.client.renderers.entity.CultistLeaderRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.CULTIST_PORTAL_LESSER.get(),
                    arcaneresearch.client.renderers.entity.CultistPortalRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.CULTIST_PORTAL_GREATER.get(),
                    arcaneresearch.client.renderers.entity.CultistPortalGreaterRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.GOLEM_ORB.get(),
                    arcaneresearch.client.renderers.entity.GolemOrbRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.GOLEM_DART.get(),
                    arcaneresearch.client.renderers.entity.GolemDartRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.ELDRITCH_ORB.get(),
                    arcaneresearch.client.renderers.entity.EldritchOrbRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.HOMING_SHARD.get(),
                    arcaneresearch.client.renderers.entity.HomingShardRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.FOCUS_MINE.get(),
                    arcaneresearch.client.renderers.entity.FocusMineRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.FOCUS_CLOUD.get(),
                    arcaneresearch.client.renderers.entity.FocusCloudRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.GRAPPLE.get(),
                    arcaneresearch.client.renderers.entity.GrappleRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.RIFT_BLAST.get(),
                    arcaneresearch.client.renderers.entity.RiftBlastRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.FALLING_TAINT.get(),
                    arcaneresearch.client.renderers.entity.FallingTaintRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.SPECIAL_ITEM.get(),
                    arcaneresearch.client.renderers.entity.SpecialItemRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.FOLLOWING_ITEM.get(),
                    arcaneresearch.client.renderers.entity.SpecialItemRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.FOCUS_PROJECTILE.get(),
                    arcaneresearch.client.renderers.entity.NoProjectileRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.ALUMENTUM.get(),
                    arcaneresearch.client.renderers.entity.NoProjectileRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.CAUSALITY_COLLAPSER.get(),
                    arcaneresearch.client.renderers.entity.NoProjectileRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.BOTTLE_TAINT.get(),
                    arcaneresearch.client.renderers.entity.BottleTaintRenderer::new
            );
            
            event.registerEntityRenderer(
                    ModEntities.PRIMAL_ARROW.get(),
                    arcaneresearch.client.renderers.entity.PrimalArrowRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.ELDRITCH_WARDEN.get(),
                    arcaneresearch.client.renderers.entity.EldritchWardenRenderer::new
            );
            LOGGER.info("Registered Arcaneresearch entity renderers");
        }

        
@SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {

            KeyHandler.registerKeyMappings(event);
        }

        
        
        
        
        
        
        
        @SubscribeEvent
        public static void onClientLoggingIn(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn event) {
            if (event.getConnection().isMemoryConnection()) {
                return;
            }
            ConfigResearch.initEnchantmentScans(event.getPlayer().registryAccess());
        }

        @SubscribeEvent
        public static void onRegisterTooltipComponents(
                RegisterClientTooltipComponentFactoriesEvent event) {

            arcaneresearch.client.lib.events.AspectTooltipEvents.registerFactory(event);
        }

        @SubscribeEvent
        public static void onModifyBakingResult(
                net.neoforged.neoforge.client.event.ModelEvent.ModifyBakingResult event) {
            
            
            
            
            LOGGER.info("Crystal models wired via RegisterBlockStateModels");
        }

        @SubscribeEvent
        public static void onRegisterItemColors(
                net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.ItemTintSources event) {
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            LOGGER.info("Item tints registered (ItemTintSource, 26.3 data-driven)");
        }

        @SubscribeEvent
        public static void onRegisterBlockColors(
                net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.BlockTintSources event) {
            
            
            
            
            event.register(java.util.List.of((net.minecraft.client.color.block.BlockTintSource) state -> {
                if (state.getBlock() instanceof arcaneresearch.common.blocks.world.ore.BlockCrystalTC crystal) {
                    return 0xFF000000 | crystal.getAspect().getColor();
                }
                return 0xFFFFFFFF;
            }),
                    arcaneresearch.init.ModBlocks.CRYSTAL_AIR.get(),
                    arcaneresearch.init.ModBlocks.CRYSTAL_FIRE.get(),
                    arcaneresearch.init.ModBlocks.CRYSTAL_WATER.get(),
                    arcaneresearch.init.ModBlocks.CRYSTAL_EARTH.get(),
                    arcaneresearch.init.ModBlocks.CRYSTAL_ORDER.get(),
                    arcaneresearch.init.ModBlocks.CRYSTAL_ENTROPY.get(),
                    arcaneresearch.init.ModBlocks.CRYSTAL_FLUX.get());
            
            
            
            event.register(java.util.List.of((net.minecraft.client.color.block.BlockTintSource) state -> {
                if (state.getBlock() instanceof arcaneresearch.common.blocks.misc.BlockNitor nitor) {
                    return 0xFF000000 | nitor.getColorValue();
                }
                return 0xFFFFFFFF;
            }),
                    arcaneresearch.init.ModBlocks.NITOR_WHITE.get(),
                    arcaneresearch.init.ModBlocks.NITOR_ORANGE.get(),
                    arcaneresearch.init.ModBlocks.NITOR_MAGENTA.get(),
                    arcaneresearch.init.ModBlocks.NITOR_LIGHT_BLUE.get(),
                    arcaneresearch.init.ModBlocks.NITOR_YELLOW.get(),
                    arcaneresearch.init.ModBlocks.NITOR_LIME.get(),
                    arcaneresearch.init.ModBlocks.NITOR_PINK.get(),
                    arcaneresearch.init.ModBlocks.NITOR_GRAY.get(),
                    arcaneresearch.init.ModBlocks.NITOR_LIGHT_GRAY.get(),
                    arcaneresearch.init.ModBlocks.NITOR_CYAN.get(),
                    arcaneresearch.init.ModBlocks.NITOR_PURPLE.get(),
                    arcaneresearch.init.ModBlocks.NITOR_BLUE.get(),
                    arcaneresearch.init.ModBlocks.NITOR_BROWN.get(),
                    arcaneresearch.init.ModBlocks.NITOR_GREEN.get(),
                    arcaneresearch.init.ModBlocks.NITOR_RED.get(),
                    arcaneresearch.init.ModBlocks.NITOR_BLACK.get());
            
            
            event.register(java.util.List.of((net.minecraft.client.color.block.BlockTintSource) state -> {
                if (state.getBlock() instanceof arcaneresearch.common.blocks.basic.BlockCandle candle) {
                    return 0xFF000000 | candle.getColor().getMapColor().col;
                }
                return 0xFFFFFFFF;
            }),
                    arcaneresearch.init.ModBlocks.CANDLE_WHITE.get(),
                    arcaneresearch.init.ModBlocks.CANDLE_ORANGE.get(),
                    arcaneresearch.init.ModBlocks.CANDLE_MAGENTA.get(),
                    arcaneresearch.init.ModBlocks.CANDLE_LIGHT_BLUE.get(),
                    arcaneresearch.init.ModBlocks.CANDLE_YELLOW.get(),
                    arcaneresearch.init.ModBlocks.CANDLE_LIME.get(),
                    arcaneresearch.init.ModBlocks.CANDLE_PINK.get(),
                    arcaneresearch.init.ModBlocks.CANDLE_GRAY.get(),
                    arcaneresearch.init.ModBlocks.CANDLE_LIGHT_GRAY.get(),
                    arcaneresearch.init.ModBlocks.CANDLE_CYAN.get(),
                    arcaneresearch.init.ModBlocks.CANDLE_PURPLE.get(),
                    arcaneresearch.init.ModBlocks.CANDLE_BLUE.get(),
                    arcaneresearch.init.ModBlocks.CANDLE_BROWN.get(),
                    arcaneresearch.init.ModBlocks.CANDLE_GREEN.get(),
                    arcaneresearch.init.ModBlocks.CANDLE_RED.get(),
                    arcaneresearch.init.ModBlocks.CANDLE_BLACK.get());
            
            
            
            event.register(java.util.List.of((net.minecraft.client.color.block.BlockTintSource) state -> {
                if (state.getBlock() instanceof arcaneresearch.common.blocks.basic.BlockBannerTC banner) {
                    net.minecraft.world.item.DyeColor dye = banner.getDyeColor();
                    return 0xFF000000 | (dye != null ? dye.getMapColor().col
                            : net.minecraft.world.level.material.MapColor.COLOR_RED.col);
                }
                return 0xFFFFFFFF;
            }),
                    arcaneresearch.init.ModBlocks.BANNER_WHITE.get(),
                    arcaneresearch.init.ModBlocks.BANNER_ORANGE.get(),
                    arcaneresearch.init.ModBlocks.BANNER_MAGENTA.get(),
                    arcaneresearch.init.ModBlocks.BANNER_LIGHT_BLUE.get(),
                    arcaneresearch.init.ModBlocks.BANNER_YELLOW.get(),
                    arcaneresearch.init.ModBlocks.BANNER_LIME.get(),
                    arcaneresearch.init.ModBlocks.BANNER_PINK.get(),
                    arcaneresearch.init.ModBlocks.BANNER_GRAY.get(),
                    arcaneresearch.init.ModBlocks.BANNER_LIGHT_GRAY.get(),
                    arcaneresearch.init.ModBlocks.BANNER_CYAN.get(),
                    arcaneresearch.init.ModBlocks.BANNER_PURPLE.get(),
                    arcaneresearch.init.ModBlocks.BANNER_BLUE.get(),
                    arcaneresearch.init.ModBlocks.BANNER_BROWN.get(),
                    arcaneresearch.init.ModBlocks.BANNER_GREEN.get(),
                    arcaneresearch.init.ModBlocks.BANNER_RED.get(),
                    arcaneresearch.init.ModBlocks.BANNER_BLACK.get(),
                    arcaneresearch.init.ModBlocks.BANNER_CRIMSON_CULT.get());
            
            
            
            event.register(java.util.List.of(new net.minecraft.client.color.block.BlockTintSource() {
                @Override
                public int color(net.minecraft.world.level.block.state.BlockState state) {
                    return 0xFF000000 | net.minecraft.world.level.GrassColor.get(0.5D, 1.0D);
                }

                @Override
                public int colorInWorld(net.minecraft.world.level.block.state.BlockState state,
                                        net.minecraft.client.renderer.block.BlockAndTintGetter level,
                                        net.minecraft.core.BlockPos pos) {
                    if (level != null && pos != null) {
                        return 0xFF000000 | net.minecraft.client.renderer.BiomeColors.getAverageGrassColor(level, pos);
                    }
                    return color(state);
                }
            }), arcaneresearch.init.ModBlocks.GRASS_AMBIENT.get());
            
            
            
            
            event.register(java.util.List.of(new net.minecraft.client.color.block.BlockTintSource() {
                @Override
                public int color(net.minecraft.world.level.block.state.BlockState state) {
                    if (state.getBlock() == arcaneresearch.init.ModBlocks.SILVERWOOD_LEAVES.get()) {
                        return 0xFFFFFFFF;
                    }
                    return 0xFF000000 | net.minecraft.world.level.FoliageColor.FOLIAGE_DEFAULT;
                }

                @Override
                public int colorInWorld(net.minecraft.world.level.block.state.BlockState state,
                                        net.minecraft.client.renderer.block.BlockAndTintGetter level,
                                        net.minecraft.core.BlockPos pos) {
                    if (state.getBlock() == arcaneresearch.init.ModBlocks.SILVERWOOD_LEAVES.get()) {
                        return 0xFFFFFFFF;
                    }
                    if (level != null && pos != null) {
                        return 0xFF000000 | net.minecraft.client.renderer.BiomeColors.getAverageFoliageColor(level, pos);
                    }
                    return color(state);
                }
            }),
                    arcaneresearch.init.ModBlocks.GREATWOOD_LEAVES.get(),
                    arcaneresearch.init.ModBlocks.SILVERWOOD_LEAVES.get());
            
            
            
            
            event.register(java.util.List.of(new net.minecraft.client.color.block.BlockTintSource() {
                @Override
                public int color(net.minecraft.world.level.block.state.BlockState state) {
                    return 0xFFFFFFFF;
                }

                @Override
                public int colorInWorld(net.minecraft.world.level.block.state.BlockState state,
                                        net.minecraft.client.renderer.block.BlockAndTintGetter level,
                                        net.minecraft.core.BlockPos pos) {
                    if (level != null && pos != null
                            && state.getBlock() instanceof arcaneresearch.common.blocks.essentia.BlockTube
                            && level.getBlockEntity(pos) instanceof arcaneresearch.common.tiles.essentia.TileTubeFilter filter
                            && filter.aspectFilter != null) {
                        return 0xFF000000 | filter.aspectFilter.getColor();
                    }
                    return 0xFFFFFFFF;
                }
            }), arcaneresearch.init.ModBlocks.TUBE_FILTER.get());
            
            
            event.register(java.util.List.of(new net.minecraft.client.color.block.BlockTintSource() {
                @Override
                public int color(net.minecraft.world.level.block.state.BlockState state) {
                    return 0xFF000000 | arcaneresearch.common.blocks.devices.BlockStabilizer.colorMultiplier(0);
                }

                @Override
                public int colorInWorld(net.minecraft.world.level.block.state.BlockState state,
                                        net.minecraft.client.renderer.block.BlockAndTintGetter level,
                                        net.minecraft.core.BlockPos pos) {
                    if (level != null && pos != null
                            && level.getBlockEntity(pos) instanceof arcaneresearch.common.tiles.devices.TileStabilizer stabilizer) {
                        return 0xFF000000 | arcaneresearch.common.blocks.devices.BlockStabilizer.colorMultiplier(stabilizer.getEnergy());
                    }
                    return color(state);
                }
            }), arcaneresearch.init.ModBlocks.STABILIZER.get());
            
            
            event.register(java.util.List.of((net.minecraft.client.color.block.BlockTintSource) state -> {
                if (state.getBlock() instanceof arcaneresearch.common.blocks.devices.BlockInlay
                        && state.hasProperty(arcaneresearch.common.blocks.devices.BlockInlay.CHARGE)) {
                    return 0xFF000000 | arcaneresearch.common.blocks.devices.BlockInlay.colorMultiplier(
                            state.getValue(arcaneresearch.common.blocks.devices.BlockInlay.CHARGE));
                }
                return 0xFFFFFFFF;
            }), arcaneresearch.init.ModBlocks.INLAY.get());
            LOGGER.info("Block tints registered (BlockTintSource, 26.3)");
        }

        
        
        record BoneBowPulling() implements SelectItemModelProperty<Float> {
            public static final SelectItemModelProperty.Type<BoneBowPulling, Float> TYPE =
                    SelectItemModelProperty.Type.create(MapCodec.unit(new BoneBowPulling()), Codec.FLOAT);

            @Override
            public Float get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner,
                             int seed, ItemDisplayContext displayContext) {
                if (!stack.is(ModItems.BONE_BOW.get())) {
                    return 0.0F;
                }
                return owner != null && owner.isUsingItem() && owner.getUseItem() == stack ? 1.0F : 0.0F;
            }

            @Override
            public SelectItemModelProperty.Type<BoneBowPulling, Float> type() {
                return TYPE;
            }

            @Override
            public Codec<Float> valueCodec() {
                return Codec.FLOAT;
            }
        }

        record BoneBowPull() implements SelectItemModelProperty<Float> {
            public static final SelectItemModelProperty.Type<BoneBowPull, Float> TYPE =
                    SelectItemModelProperty.Type.create(MapCodec.unit(new BoneBowPull()), Codec.FLOAT);

            @Override
            public Float get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner,
                             int seed, ItemDisplayContext displayContext) {
                if (!stack.is(ModItems.BONE_BOW.get())) {
                    return 0.0F;
                }
                if (owner == null || owner.getUseItem() != stack) {
                    return 0.0F;
                }
                int charge = stack.getUseDuration(owner) - owner.getUseItemRemainingTicks();
                if (charge >= 13) {
                    return 0.65F;
                }
                if (charge > 7) {
                    return 0.35F;
                }
                return 0.0F;
            }

            @Override
            public SelectItemModelProperty.Type<BoneBowPull, Float> type() {
                return TYPE;
            }

            @Override
            public Codec<Float> valueCodec() {
                return Codec.FLOAT;
            }
        }
    }

    
    
    
    
    
    
}
