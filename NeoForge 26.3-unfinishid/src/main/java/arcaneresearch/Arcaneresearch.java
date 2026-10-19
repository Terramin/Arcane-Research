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

// NEOFORGE_PORT: 1:1 from Forge 1.20.1 Arcaneresearch.java (registration ORDER preserved).
// REQUIRED changes only:
//   ctor (IEventBus, ModContainer) instead of FMLJavaModLoadingContext;
//   MinecraftForge -> NeoForge; ResourceLocation -> Identifier;
//   registerIngredientTypes/RegisterEvent/CraftingHelper -> ModIngredientTypes.register(bus);
//   ForgeMod.STEP_HEIGHT_ADDITION -> Attributes.STEP_HEIGHT;
//   render-layer setup -> model-JSON render_type (see TODO in onClientSetup);
//   ItemProperties.register -> select/range property events (see TODO in onClientSetup);
//   EntityRenderers/MenuScreens direct calls -> RegisterRenderers/RegisterMenuScreensEvent;
//   onAddPackFinders -> event.addPackFinders helper;
//   item/block colors -> ItemTintSource/BlockTintSource data system (see TODOs);
//   ModifyBakingResult (BakedModel gone) -> model-system rework (see TODO).
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
        // NEOFORGE_PORT: network payloads (RegisterPayloadHandlersEvent, mod bus).
        modEventBus.addListener(PacketHandler::register);

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
        // NEOFORGE_PORT: replaces registerIngredientTypes/RegisterEvent/CraftingHelper
        // (CrystalAspectIngredient arcaneresearch:crystal + NBTTC arcaneresearch:nbt_test).
        ModIngredientTypes.register(modEventBus);
        ModLootModifiers.LOOT_MODIFIERS.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);
        ModStructures.STRUCTURE_TYPES.register(modEventBus);
        ModStructures.STRUCTURE_PIECE_TYPES.register(modEventBus);
        // NEOFORGE_PORT: without this the ATTACHMENT_TYPES holders stay unbound and
        // every player login dies with "Trying to access unbound value ... knowledge".
        arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
        // NEOFORGE_PORT: Forge auto-subscribed AuraChunkHandler via
        // @Mod.EventBusSubscriber(bus = FORGE); without this call chunk
        // load/save events never fire, aura never generates and
        // AuraHandler.drainVis() always returns 0 -> wands never recharge.
        arcaneresearch.common.world.aura.AuraChunkHandler.register();

    }

    public static Arcaneresearch getInstance() {
        return instance;
    }

    private void registerEntityAttributes(final EntityAttributeModificationEvent event) {
        // NEOFORGE_PORT: ForgeMod.STEP_HEIGHT_ADDITION removed -> vanilla STEP_HEIGHT (Holder);
        // 26.3: EntityType statics moved to EntityTypes (verified in 26.3 jar).
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

        // NEOFORGE_PORT: Forge PacketHandler.init() (SimpleChannel build on FMLCommonSetup)
        // -> payload registrar is a mod-bus RegisterPayloadHandlersEvent listener
        // (wired in ctor); nothing to do here.
        // NOTE: NO content setup here — DeferredHolders (items/components) are not yet
        // bound during commonSetup in 26.x and any ItemStack creation crashes bootstrap
        // with "Components not bound yet". Everything runs in runContentSetupOnce()
        // at server start instead (fires on integrated server too).
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

        // NEOFORGE_PORT: enchanted-book pech trades need datapack enchantment holders,
        // unavailable at commonSetup; appended here (same entries/costs as Forge).
        arcaneresearch.common.config.ConfigEntities.initPechBookTrades(event.getServer().registryAccess());
        // NEOFORGE_PORT: same for enchantment scannables (were static ForgeRegistries loop).
        arcaneresearch.common.config.ConfigResearch.initEnchantmentScans(event.getServer().registryAccess());

        runContentSetupOnce();

        // NEOFORGE_PORT: postInit (research JSON parsing) must run AFTER
        // runContentSetupOnce (which calls ConfigResearch.init registering categories
        // + JSON locations). Forge order was commonSetup-init... then server-start
        // postInit-parse; calling parse first yields "0 categories" (verified in log).
        ConfigResearch.postInit();

        LOGGER.info("Arcaneresearch server starting");
    }

    private static boolean contentSetupDone = false;

    // NEOFORGE_PORT: since 26.1 item components bind only when world resources load
    // (before that ANY ItemStack creation crashes with "Components not bound yet" -
    // verified: commonSetup is too early). So the whole Forge commonSetup content
    // block runs here at server start instead, once (integrated server fires this
    // too). Every consumer (GUI, JEI, recipes, tabs, ticks) runs strictly after
    // a world exists.
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
            // NEOFORGE_PORT: Forge 1:1 (findResource + PathPackResources + null-check).
            // The addPackFinders() helper is NOT used: it feeds a null pack into the
            // repository when metadata is unreadable (hard crash at startup).
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

            // TODO_PORT (render layers): the ~50 ItemBlockRenderTypes.setRenderLayer calls from
            // Forge onClientSetup move to model JSON in 26.x (vanilla mechanism verified
            // in the 26.3 client jar: per-texture "force_translucent", cf. glass.json).
            // DONE: jars (normal/void/brain glass + liquid slots, incl. inherited fill
            // variants) via force_translucent in models/block/jar_*.json.
            // (translucent: node/fluids/goo/gas/portal; cutout: leaves/saplings/plants/
            //  pots/relay/lattice/furnace/stabilizer/inlay/taint/rail; cutoutMipped: grass_ambient).
            // TODO_PORT (item predicates): the 8 ItemProperties.register calls (jar fill,
            // grapple loaded, pearl damage, charm type, caster focus, mirror link, wand cap)
            // move to select/range property events reading Data Components (NBT is gone).

        }

        @SubscribeEvent
        public static void onRegisterItemModelProperties(RegisterSelectItemModelPropertyEvent event) {
            // Паритет ItemBowBone (1.7.10): иконки натяжения костяного лука.
            // Ванильный ItemProperties регистрирует "pulling"/"pull" только на Items.BOW,
            // поэтому для своего лука предикаты регистрируются вручную.
            // Пороги: j >= 13 → bonebow_pulling_2; j > 7 → _1; j > 0 → _0.
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
            // NEOFORGE_PORT: MenuScreens.register -> RegisterMenuScreensEvent (same pairs as Forge).
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
            // NEOFORGE_PORT (Forge e45d90b): TempDeconClient's screen registration moved here.
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
            // TODO_PORT (render phase): new BlockEntityRenderer<T,S>/EntityRenderer<T,S>
            // submit API (verified: old register**(type, ::new) shapes gone in 26.3).
            // Bodies below preserved verbatim for the rework; re-enable per renderer.
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

            // NEOFORGE_PORT (Forge e45d90b): TempDeconClient's renderer registration moved here.
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
            // Паритет Forge: первородная стрела (TC4 parity).
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
        public static void onRegisterTooltipComponents(
                RegisterClientTooltipComponentFactoriesEvent event) {

            arcaneresearch.client.lib.events.AspectTooltipEvents.registerFactory(event);
        }

        @SubscribeEvent
        public static void onModifyBakingResult(
                net.neoforged.neoforge.client.event.ModelEvent.ModifyBakingResult event) {
            // NEOFORGE_PORT: Forge replaced per-variant crystal models with
            // CrystalBakedModel here; in 26.3 this is wired via
            // RegisterBlockStateModels (CrystalModelRegistration, custom model
            // type "arcaneresearch:crystal" in the crystal_* blockstates).
            LOGGER.info("Crystal models wired via RegisterBlockStateModels");
        }

        @SubscribeEvent
        public static void onRegisterItemColors(
                net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.ItemTintSources event) {
            // TODO_PORT (tint rework): 26.3 item tints are data-driven ItemTintSource
            // (vanilla models declare "tints"). Forge lambdas to convert (same colors/order):
            // caster (focus color @tint>0),
            // foci x3 (focus color), golem_placer (material color from props), robes x6 (dyed color).
            // DONE: phial (aspect[0] @tint1) -> PhialTintSources (arcaneresearch:phial).
            // DONE: labels (aspect @tint1) -> LabelTintSources (arcaneresearch:label).
            // DONE: jars (aspect[0] @tint1) -> JarTintSources (arcaneresearch:jar_aspect),
            // wired per fill-variant in assets/arcaneresearch/items/jar_{normal,void}.json.
            // DONE: nitor x16 (getColorValue) -> client/renderers/item/NitorTintSources
            // (arcaneresearch:nitor), wired in assets/arcaneresearch/items/nitor_*.json.
            // DONE: candles x16 (dye map color) -> CandleTintSources (arcaneresearch:candle).
            // DONE: banners x17 (dye @tint1, aspect @tint2) -> BannerTintSources
            // (arcaneresearch:banner_dye/aspect + constant white @tint0).
            // DONE: leaves x2 + grass_ambient -> FoliageTintSources (arcaneresearch:leaf/grass_ambient).
            // DONE: crystal_essence (aspect[0]) + crystals x7 (fixed color) ->
            // client/renderers/item/CrystalTintSources (arcaneresearch:crystal_aspect),
            // wired in assets/arcaneresearch/items/crystal_*.json.
            // DONE: wand focus tint -> focus-group vertex color in WandSpecialRenderer
            // (custom geometry has no tintIndex pipeline; Forge tintIndex-1 contract kept).
            // DONE: wisp essence (aspect[0] color, else aspect cycle) ->
            // WispEssenceTintSources (arcaneresearch:essence_aspect), wired in
            // assets/arcaneresearch/items/wisp_essence.json.
            LOGGER.info("Item tints registered (ItemTintSource, 26.3 data-driven)");
        }

        @SubscribeEvent
        public static void onRegisterBlockColors(
                net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.BlockTintSources event) {
            // NEOFORGE_PORT: 26.3 block tints are BlockTintSource lists. Crystals:
            // Forge lambda parity (aspect color for tintIndex 0 quads).
            // Opaque OR: Forge BlockColors ignored alpha (RGB only); 26.3 tint
            // layers are ARGB, so alpha is forced for the same visible result.
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
            // NEOFORGE_PORT: Forge onRegisterBlockColors nitor entries 1:1
            // (per-block getColorValue(), any tintIndex). Opaque OR: same
            // ARGB transport adaptation as crystals above.
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
            // NEOFORGE_PORT: Forge onRegisterBlockColors candle entries 1:1
            // (dye map color, any tintIndex). Opaque OR: same ARGB adaptation.
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
            // NEOFORGE_PORT: Forge onRegisterBlockColors banner entries 1:1
            // (dye map color, null dye i.e. crimson cult -> COLOR_RED; aspect plays
            // no role in-world, same as Forge). Opaque OR: same ARGB adaptation.
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
            // NEOFORGE_PORT: Forge grass_ambient entry 1:1 (biome grass color in
            // world, GrassColor.get(0.5, 1.0) fallback). colorInWorld override carries
            // the level/pos branch (verified BlockTintSource shape in the 26.3 jar).
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
            // NEOFORGE_PORT: Forge leaves entries 1:1 (silverwood always white,
            // greatwood biome foliage color, FoliageColor default fallback).
            // FoliageColor.getDefaultColor() is gone in 26.x; FOLIAGE_DEFAULT holds
            // the same value (verified constant in the 26.3 client jar).
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
            // NEOFORGE_PORT: Forge tube_filter entry 1:1 (tile TileTubeFilter
            // aspectFilter color; tintIndex 1 in Forge maps to the filter faces here,
            // same as the jar liquid faces). colorInWorld override carries the
            // level/pos/tile branch (verified BlockTintSource shape in the 26.3 jar).
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
            // TODO_PORT (tint rework): same for blocks. Forge lambdas to convert:
            // stabilizer (energy), inlay (charge).
            // DONE: crystals x7 + nitors x16 + candles x16 + banners x17 +
            // grass_ambient + leaves x2 + tube_filter above (same values as Forge, opaque OR).
            LOGGER.info("Block tints deferred (BlockTintSource data rework, see TODO)");
        }

        // Bone bow pulling/pull item model properties (ported from Forge ItemProperties.register).
        // Vanilla registers "pulling"/"pull" only for Items.BOW; our bow registers them explicitly.
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

    // NEOFORGE_PORT: client-only helpers (Minecraft/ClientLevel) must not live in
    // the outer mod class: JVM verification of Arcaneresearch on a dedicated
    // server resolves them at link time -> NoClassDefFoundError: ClientLevel
    // during mod construction (@OnlyIn no longer strips in NeoForge 26).
    // They had no direct callers left (PacketSealToClient uses
    // Proxies.get().getClientLevel()); client code belongs in arcaneresearch.client.*.
}
