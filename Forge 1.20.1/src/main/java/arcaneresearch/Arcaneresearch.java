package arcaneresearch;
import arcaneresearch.helper.TintHelper;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;
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
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.items.casters.FocusInit;
import arcaneresearch.common.golems.GolemProperties;
import arcaneresearch.common.golems.seals.SealHandler;
import arcaneresearch.common.lib.InternalMethodHandler;
import arcaneresearch.common.config.ConfigResearch;
import arcaneresearch.common.config.ConfigAspects;
import arcaneresearch.common.config.ConfigMultiblocks;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.aspects.Aspect;
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
    public Arcaneresearch() {
        instance = this;
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::processIMC);
        modEventBus.addListener(this::registerIngredientTypes);
        modEventBus.addListener(this::registerEntityAttributes);
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
        arcaneresearch.init.ModLootModifiers.LOOT_MODIFIERS.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);
        ModStructures.STRUCTURE_TYPES.register(modEventBus);
        ModStructures.STRUCTURE_PIECE_TYPES.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
    }
    public static Arcaneresearch getInstance() {
        return instance;
    }
    private void registerIngredientTypes(final RegisterEvent event) {
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.RECIPE_SERIALIZERS)) {
            CraftingHelper.register(
                    arcaneresearch.common.lib.crafting.CrystalAspectIngredient.TYPE_ID,
                    arcaneresearch.common.lib.crafting.CrystalAspectIngredient.Serializer.INSTANCE);
        }
    }
    private void registerEntityAttributes(final net.minecraftforge.event.entity.EntityAttributeModificationEvent event) {
        if (!event.has(net.minecraft.world.entity.EntityType.PLAYER,
                net.minecraftforge.common.ForgeMod.STEP_HEIGHT_ADDITION.get())) {
            event.add(net.minecraft.world.entity.EntityType.PLAYER,
                    net.minecraftforge.common.ForgeMod.STEP_HEIGHT_ADDITION.get());
        }
    }
    private void processIMC(final InterModProcessEvent event) {
        arcaneresearch.proxies.Proxies.get().checkInterModComs(event);
    }
    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Arcaneresearch common setup");
        ArcaneresearchApi.internalMethods = new InternalMethodHandler();
        LOGGER.info("Initialized ArcaneresearchApi internal methods");
        PacketHandler.init();
        event.enqueueWork(() -> {
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
        });
    }
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        ConfigResearch.postInit();
        LOGGER.info("Arcaneresearch server starting");
    }
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
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
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onAddPackFinders(AddPackFindersEvent event) {
            if (event.getPackType() != PackType.CLIENT_RESOURCES) {
                return;
            }
            event.addRepositorySource(packConsumer -> {
                try {
                    java.nio.file.Path packPath = ModList.get().getModFileById(MODID).getFile()
                            .findResource("resourcepacks", "legacy-textures");
                    if (packPath == null || !java.nio.file.Files.isDirectory(packPath)) {
                        return;
                    }
                    Pack pack = Pack.readMetaAndCreate(
                            "builtin/arcaneresearch_legacy_textures",
                            Component.literal("Arcaneresearch Legacy Textures"),
                            false,
                            id -> new PathPackResources(id, packPath, true),
                            PackType.CLIENT_RESOURCES,
                            Pack.Position.TOP,
                            PackSource.BUILT_IN);
                    if (pack != null) {
                        packConsumer.accept(pack);
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to register legacy textures pack", e);
                }
            });
        }
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("Arcaneresearch client setup");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
            event.enqueueWork(() -> {
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.JAR_NORMAL.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.JAR_VOID.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.JAR_BRAIN.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(arcaneresearch.common.blocks.aura.AuraBlocks.JAR_NODE.get(),
                        RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.LIQUID_DEATH.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.PURIFYING_FLUID.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.FLUX_GOO.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.FLUX_GAS.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.GREATWOOD_LEAVES.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.SILVERWOOD_LEAVES.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.GREATWOOD_SAPLING.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.SILVERWOOD_SAPLING.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.VISHROOM.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.CINDERPEARL.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.SHIMMERLEAF.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_GREATWOOD_SAPLING.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_SILVERWOOD_SAPLING.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_VISHROOM.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_CINDERPEARL.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_SHIMMERLEAF.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.REDSTONE_RELAY.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.CONDENSER_LATTICE.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.CONDENSER_LATTICE_DIRTY.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.INFERNAL_FURNACE.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.STABILIZER.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.INLAY.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.TAINT_SOIL.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.TAINT_ROCK.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.TAINT_CRUST.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.TAINT_GEYSER.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.TAINT_FIBRE.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.ACTIVATOR_RAIL.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.GRASS_AMBIENT.get(), RenderType.cutoutMipped());
                ItemBlockRenderTypes.setRenderLayer(
                        arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_PORTAL.get(), RenderType.translucent());
                ItemProperties.register(ModBlocks.JAR_NORMAL.get().asItem(),
                        new ResourceLocation(MODID, "fill"),
                        (stack, level, entity, seed) ->
                                arcaneresearch.common.blocks.essentia.BlockJarItem.getFill(stack));
                ItemProperties.register(ModBlocks.JAR_VOID.get().asItem(),
                        new ResourceLocation(MODID, "fill"),
                        (stack, level, entity, seed) ->
                                arcaneresearch.common.blocks.essentia.BlockJarItem.getFill(stack));
                ItemProperties.register(ModItems.GRAPPLE_GUN.get(),
                        new ResourceLocation(MODID, "type"),
                        (stack, level, entity, seed) ->
                                (stack.hasTag() && stack.getTag().getByte("loaded") == 1) ? 1.0F : 0.0F);
                ItemProperties.register(ModItems.PRIMORDIAL_PEARL.get(),
                        new ResourceLocation(MODID, "type"),
                        (stack, level, entity, seed) -> {
                            int damage = stack.getDamageValue();
                            return damage < 3 ? 0.0F : (damage < 6 ? 1.0F : 2.0F);
                        });
                net.minecraft.world.item.Item[] casters = {
                        ModItems.CASTER_BASIC.get()
                };
                ItemProperties.register(ModItems.VERDANT_CHARM.get(),
                        new ResourceLocation(MODID, "type"),
                        (stack, level, entity, seed) ->
                                (stack.hasTag() ? stack.getTag().getByte("type") : 0));
                for (net.minecraft.world.item.Item caster : casters) {
                    ItemProperties.register(caster,
                            new ResourceLocation(MODID, "focus"),
                            (stack, level, entity, seed) -> {
                                if (stack.getItem() instanceof arcaneresearch.common.items.casters.ItemCaster itemCaster
                                        && itemCaster.getFocus(stack) != null) {
                                    return 1.0F;
                                }
                                return 0.0F;
                            });
                }
                ItemProperties.register(ModBlocks.MIRROR_ESSENTIA.get().asItem(),
                        new ResourceLocation(MODID, "linked"),
                        (stack, level, entity, seed) ->
                                (stack.hasTag() && stack.getTag().contains("linkX")) ? 1.0F : 0.0F);
                ItemProperties.register(ModItems.BONE_BOW.get(),
                        new ResourceLocation("pulling"),
                        (stack, level, entity, seed) ->
                                entity != null && entity.isUsingItem()
                                        && entity.getUseItem() == stack ? 1.0F : 0.0F);
                ItemProperties.register(ModItems.BONE_BOW.get(),
                        new ResourceLocation("pull"),
                        (stack, level, entity, seed) -> {
                            if (entity == null || entity.getUseItem() != stack) {
                                return 0.0F;
                            }
                            int charge = stack.getUseDuration() - entity.getUseItemRemainingTicks();
                            if (charge >= 13) {
                                return 0.65F;
                            }
                            if (charge > 7) {
                                return 0.35F;
                            }
                            return 0.0F;
                        });
                ItemProperties.register(
                        arcaneresearch.common.items.wands.WandItems.WAND_CAP.get(),
                        new ResourceLocation(MODID, "cap"),
                        (stack, level, entity, seed) ->
                                arcaneresearch.common.items.wands.ItemWandCap.getCapIndex(stack));
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.ARCANERESEARCH_GOLEM.get(),
                    arcaneresearch.client.renderers.entity.GolemRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.TURRET_CROSSBOW.get(),
                    arcaneresearch.client.renderers.entity.TurretCrossbowRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.TURRET_CROSSBOW_ADVANCED.get(),
                    arcaneresearch.client.renderers.entity.TurretCrossbowAdvancedRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.ARCANE_BORE.get(),
                    arcaneresearch.client.renderers.entity.ArcaneBoreRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.PECH.get(),
                    arcaneresearch.client.renderers.entity.PechRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.WISP.get(),
                    arcaneresearch.client.renderers.entity.WispRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.FLUX_RIFT.get(),
                    arcaneresearch.client.renderers.entity.FluxRiftRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.BRAINY_ZOMBIE.get(),
                    arcaneresearch.client.renderers.entity.BrainyZombieRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.GIANT_BRAINY_ZOMBIE.get(),
                    arcaneresearch.client.renderers.entity.BrainyZombieRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.THAUMIC_SLIME.get(),
                    arcaneresearch.client.renderers.entity.ThaumicSlimeRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.MIND_SPIDER.get(),
                    arcaneresearch.client.renderers.entity.MindSpiderRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.FIRE_BAT.get(),
                    arcaneresearch.client.renderers.entity.FireBatRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.CULTIST.get(),
                    arcaneresearch.client.renderers.entity.CultistRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.CULTIST_KNIGHT.get(),
                    arcaneresearch.client.renderers.entity.CultistRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.CULTIST_CLERIC.get(),
                    arcaneresearch.client.renderers.entity.CultistRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.TAINT_CRAWLER.get(),
                    arcaneresearch.client.renderers.entity.TaintCrawlerRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.TAINT_SWARM.get(),
                    arcaneresearch.client.renderers.entity.TaintSwarmRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.TAINTACLE.get(),
                    arcaneresearch.client.renderers.entity.TaintacleRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.TAINTACLE_SMALL.get(),
                    arcaneresearch.client.renderers.entity.TaintacleRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.TAINTACLE_GIANT.get(),
                    arcaneresearch.client.renderers.entity.TaintacleRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.TAINT_SEED.get(),
                    arcaneresearch.client.renderers.entity.TaintSeedRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.TAINT_SEED_PRIME.get(),
                    arcaneresearch.client.renderers.entity.TaintSeedRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.ELDRITCH_CRAB.get(),
                    arcaneresearch.client.renderers.entity.EldritchCrabRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.ELDRITCH_GUARDIAN.get(),
                    arcaneresearch.client.renderers.entity.EldritchGuardianRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.ELDRITCH_GOLEM.get(),
                    arcaneresearch.client.renderers.entity.EldritchGolemRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.SPELL_BAT.get(),
                    arcaneresearch.client.renderers.entity.SpellBatRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.INHABITED_ZOMBIE.get(),
                    arcaneresearch.client.renderers.entity.InhabitedZombieRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.CULTIST_LEADER.get(),
                    arcaneresearch.client.renderers.entity.CultistLeaderRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.CULTIST_PORTAL_LESSER.get(),
                    arcaneresearch.client.renderers.entity.CultistPortalRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.CULTIST_PORTAL_GREATER.get(),
                    arcaneresearch.client.renderers.entity.CultistPortalGreaterRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.GOLEM_ORB.get(),
                    arcaneresearch.client.renderers.entity.GolemOrbRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.GOLEM_DART.get(),
                    arcaneresearch.client.renderers.entity.GolemDartRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.PRIMAL_ARROW.get(),
                    arcaneresearch.client.renderers.entity.PrimalArrowRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.ELDRITCH_ORB.get(),
                    arcaneresearch.client.renderers.entity.EldritchOrbRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.HOMING_SHARD.get(),
                    arcaneresearch.client.renderers.entity.HomingShardRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.FOCUS_MINE.get(),
                    arcaneresearch.client.renderers.entity.FocusMineRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.FOCUS_CLOUD.get(),
                    arcaneresearch.client.renderers.entity.FocusCloudRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.GRAPPLE.get(),
                    arcaneresearch.client.renderers.entity.GrappleRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.RIFT_BLAST.get(),
                    arcaneresearch.client.renderers.entity.RiftBlastRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.FALLING_TAINT.get(),
                    arcaneresearch.client.renderers.entity.FallingTaintRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.SPECIAL_ITEM.get(),
                    arcaneresearch.client.renderers.entity.SpecialItemRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.FOLLOWING_ITEM.get(),
                    arcaneresearch.client.renderers.entity.SpecialItemRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.FOCUS_PROJECTILE.get(),
                    arcaneresearch.client.renderers.entity.NoProjectileRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.ALUMENTUM.get(),
                    arcaneresearch.client.renderers.entity.NoProjectileRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.CAUSALITY_COLLAPSER.get(),
                    arcaneresearch.client.renderers.entity.NoProjectileRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.BOTTLE_TAINT.get(),
                    arcaneresearch.client.renderers.entity.BottleTaintRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                    ModEntities.ELDRITCH_WARDEN.get(),
                    arcaneresearch.client.renderers.entity.EldritchWardenRenderer::new
                );
                LOGGER.info("Registered Arcaneresearch entity renderers");
                net.minecraft.client.gui.screens.MenuScreens.register(
                    ModMenuTypes.GOLEM_BUILDER.get(),
                    arcaneresearch.client.gui.screens.GolemBuilderScreen::new
                );
                net.minecraft.client.gui.screens.MenuScreens.register(
                    ModMenuTypes.ARCANE_WORKBENCH.get(),
                    arcaneresearch.client.gui.screens.ArcaneWorkbenchScreen::new
                );
                net.minecraft.client.gui.screens.MenuScreens.register(
                    ModMenuTypes.THAUMATORIUM.get(),
                    arcaneresearch.client.gui.screens.ThaumatoriumScreen::new
                );
                net.minecraft.client.gui.screens.MenuScreens.register(
                    ModMenuTypes.SMELTER.get(),
                    arcaneresearch.client.gui.screens.SmelterScreen::new
                );
                net.minecraft.client.gui.screens.MenuScreens.register(
                    ModMenuTypes.RESEARCH_TABLE.get(),
                    arcaneresearch.client.gui.screens.ResearchTableScreen::new
                );
                net.minecraft.client.gui.screens.MenuScreens.register(
                    ModMenuTypes.DECONSTRUCTION_TABLE.get(),
                    arcaneresearch.client.gui.screens.DeconstructScreen::new
                );
                net.minecraft.client.gui.screens.MenuScreens.register(
                    ModMenuTypes.FOCAL_MANIPULATOR.get(),
                    arcaneresearch.client.gui.screens.FocalManipulatorScreen::new
                );
                net.minecraft.client.gui.screens.MenuScreens.register(
                    ModMenuTypes.FOCUS_POUCH.get(),
                    arcaneresearch.client.gui.screens.FocusPouchScreen::new
                );
                net.minecraft.client.gui.screens.MenuScreens.register(
                    ModMenuTypes.HAND_MIRROR.get(),
                    arcaneresearch.client.gui.screens.HandMirrorScreen::new
                );
                net.minecraft.client.gui.screens.MenuScreens.register(
                    ModMenuTypes.POTION_SPRAYER.get(),
                    arcaneresearch.client.gui.screens.PotionSprayerScreen::new
                );
                net.minecraft.client.gui.screens.MenuScreens.register(
                    ModMenuTypes.SPA.get(),
                    arcaneresearch.client.gui.screens.SpaScreen::new
                );
                net.minecraft.client.gui.screens.MenuScreens.register(
                    ModMenuTypes.VOID_SIPHON.get(),
                    arcaneresearch.client.gui.screens.VoidSiphonScreen::new
                );
                net.minecraft.client.gui.screens.MenuScreens.register(
                    ModMenuTypes.TURRET_BASIC.get(),
                    arcaneresearch.client.gui.screens.TurretScreen::new
                );
                net.minecraft.client.gui.screens.MenuScreens.register(
                    ModMenuTypes.TURRET_ADVANCED.get(),
                    arcaneresearch.client.gui.screens.TurretScreen::new
                );
                net.minecraft.client.gui.screens.MenuScreens.register(
                    ModMenuTypes.ARCANE_BORE.get(),
                    arcaneresearch.client.gui.screens.ArcaneBoreScreen::new
                );
                net.minecraft.client.gui.screens.MenuScreens.register(
                    ModMenuTypes.PECH_TRADING.get(),
                    arcaneresearch.client.gui.screens.PechScreen::new
                );
                net.minecraft.client.gui.screens.MenuScreens.register(
                    ModMenuTypes.HUNGRY_CHEST.get(),
                    arcaneresearch.client.gui.screens.HungryChestScreen::new
                );
                net.minecraft.client.gui.screens.MenuScreens.register(
                    ModMenuTypes.LOGISTICS.get(),
                    arcaneresearch.client.gui.screens.LogisticsScreen::new
                );
                net.minecraft.client.gui.screens.MenuScreens.register(
                    ModMenuTypes.SEAL.get(),
                    arcaneresearch.client.gui.screens.SealScreen::new
                );
                net.minecraft.client.gui.screens.MenuScreens.register(
                    ModMenuTypes.AURA_TOTEM.get(),
                    arcaneresearch.client.gui.screens.AuraTotemScreen::new
                );
                LOGGER.info("Registered Arcaneresearch menu screens");
            });
        }
        @SubscribeEvent
        public static void onRegisterLayerDefinitions(net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions event) {
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
        public static void onRegisterRenderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(
                ModBlockEntities.JAR.get(),
                arcaneresearch.client.renderers.tile.JarRenderer::new
            );
            event.registerBlockEntityRenderer(
                ModBlockEntities.DECONSTRUCTION_TABLE.get(),
                arcaneresearch.client.renderers.tile.DeconstructTileRenderer::new
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
            event.registerEntityRenderer(ModEntities.SPECIAL_ITEM.get(),
                    arcaneresearch.client.renderers.entity.SpecialItemRenderer::new);
            event.registerEntityRenderer(ModEntities.FOLLOWING_ITEM.get(),
                    arcaneresearch.client.renderers.entity.SpecialItemRenderer::new);
            event.registerEntityRenderer(ModEntities.FALLING_TAINT.get(),
                    arcaneresearch.client.renderers.entity.FallingTaintRenderer::new);
            event.registerEntityRenderer(ModEntities.FLUX_RIFT.get(),
                    arcaneresearch.client.renderers.entity.FluxRiftRenderer::new);
            event.registerEntityRenderer(ModEntities.BRAINY_ZOMBIE.get(),
                    arcaneresearch.client.renderers.entity.BrainyZombieRenderer::new);
            event.registerEntityRenderer(ModEntities.GIANT_BRAINY_ZOMBIE.get(),
                    arcaneresearch.client.renderers.entity.BrainyZombieRenderer::new);
            event.registerEntityRenderer(ModEntities.INHABITED_ZOMBIE.get(),
                    arcaneresearch.client.renderers.entity.InhabitedZombieRenderer::new);
            event.registerEntityRenderer(ModEntities.FIRE_BAT.get(),
                    arcaneresearch.client.renderers.entity.FireBatRenderer::new);
            event.registerEntityRenderer(ModEntities.SPELL_BAT.get(),
                    arcaneresearch.client.renderers.entity.SpellBatRenderer::new);
            event.registerEntityRenderer(ModEntities.PECH.get(),
                    arcaneresearch.client.renderers.entity.PechRenderer::new);
            event.registerEntityRenderer(ModEntities.WISP.get(),
                    arcaneresearch.client.renderers.entity.WispRenderer::new);
            event.registerEntityRenderer(ModEntities.THAUMIC_SLIME.get(),
                    arcaneresearch.client.renderers.entity.ThaumicSlimeRenderer::new);
            event.registerEntityRenderer(ModEntities.TAINT_CRAWLER.get(),
                    arcaneresearch.client.renderers.entity.TaintCrawlerRenderer::new);
            event.registerEntityRenderer(ModEntities.TAINT_SEED.get(),
                    arcaneresearch.client.renderers.entity.TaintSeedRenderer::new);
            event.registerEntityRenderer(ModEntities.TAINT_SEED_PRIME.get(),
                    arcaneresearch.client.renderers.entity.TaintSeedRenderer::new);
            event.registerEntityRenderer(ModEntities.TAINTACLE.get(),
                    arcaneresearch.client.renderers.entity.TaintacleRenderer::new);
            event.registerEntityRenderer(ModEntities.TAINTACLE_SMALL.get(),
                    arcaneresearch.client.renderers.entity.TaintacleRenderer::new);
            event.registerEntityRenderer(ModEntities.TAINTACLE_GIANT.get(),
                    arcaneresearch.client.renderers.entity.TaintacleRenderer::new);
            event.registerEntityRenderer(ModEntities.TAINT_SWARM.get(),
                    arcaneresearch.client.renderers.entity.TaintSwarmRenderer::new);
            event.registerEntityRenderer(ModEntities.CULTIST.get(),
                    arcaneresearch.client.renderers.entity.CultistRenderer::new);
            event.registerEntityRenderer(ModEntities.CULTIST_KNIGHT.get(),
                    arcaneresearch.client.renderers.entity.CultistRenderer::new);
            event.registerEntityRenderer(ModEntities.CULTIST_CLERIC.get(),
                    arcaneresearch.client.renderers.entity.CultistRenderer::new);
            event.registerEntityRenderer(ModEntities.CULTIST_LEADER.get(),
                    arcaneresearch.client.renderers.entity.CultistLeaderRenderer::new);
            event.registerEntityRenderer(ModEntities.ELDRITCH_GUARDIAN.get(),
                    arcaneresearch.client.renderers.entity.EldritchGuardianRenderer::new);
            event.registerEntityRenderer(ModEntities.ELDRITCH_WARDEN.get(),
                    arcaneresearch.client.renderers.entity.EldritchWardenRenderer::new);
            event.registerEntityRenderer(ModEntities.MIND_SPIDER.get(),
                    arcaneresearch.client.renderers.entity.MindSpiderRenderer::new);
            event.registerEntityRenderer(ModEntities.ELDRITCH_CRAB.get(),
                    arcaneresearch.client.renderers.entity.EldritchCrabRenderer::new);
            event.registerEntityRenderer(ModEntities.ELDRITCH_GOLEM.get(),
                    arcaneresearch.client.renderers.entity.EldritchGolemRenderer::new);
            event.registerEntityRenderer(ModEntities.CULTIST_PORTAL_LESSER.get(),
                    arcaneresearch.client.renderers.entity.CultistPortalRenderer::new);
            event.registerEntityRenderer(ModEntities.CULTIST_PORTAL_GREATER.get(),
                    arcaneresearch.client.renderers.entity.CultistPortalGreaterRenderer::new);
            event.registerEntityRenderer(ModEntities.FOCUS_PROJECTILE.get(),
                    arcaneresearch.client.renderers.entity.NoProjectileRenderer::new);
            event.registerEntityRenderer(ModEntities.ALUMENTUM.get(),
                    arcaneresearch.client.renderers.entity.NoProjectileRenderer::new);
            event.registerEntityRenderer(ModEntities.CAUSALITY_COLLAPSER.get(),
                    arcaneresearch.client.renderers.entity.NoProjectileRenderer::new);
            event.registerEntityRenderer(ModEntities.FOCUS_CLOUD.get(),
                    arcaneresearch.client.renderers.entity.FocusCloudRenderer::new);
            event.registerEntityRenderer(ModEntities.FOCUS_MINE.get(),
                    arcaneresearch.client.renderers.entity.FocusMineRenderer::new);
            event.registerEntityRenderer(ModEntities.GOLEM_ORB.get(),
                    arcaneresearch.client.renderers.entity.GolemOrbRenderer::new);
            event.registerEntityRenderer(ModEntities.GOLEM_DART.get(),
                    arcaneresearch.client.renderers.entity.GolemDartRenderer::new);
            event.registerEntityRenderer(ModEntities.PRIMAL_ARROW.get(),
                    arcaneresearch.client.renderers.entity.PrimalArrowRenderer::new);
            event.registerEntityRenderer(ModEntities.HOMING_SHARD.get(),
                    arcaneresearch.client.renderers.entity.HomingShardRenderer::new);
            event.registerEntityRenderer(ModEntities.BOTTLE_TAINT.get(),
                    arcaneresearch.client.renderers.entity.BottleTaintRenderer::new);
            event.registerEntityRenderer(ModEntities.ELDRITCH_ORB.get(),
                    arcaneresearch.client.renderers.entity.EldritchOrbRenderer::new);
            event.registerEntityRenderer(ModEntities.GRAPPLE.get(),
                    arcaneresearch.client.renderers.entity.GrappleRenderer::new);
            event.registerEntityRenderer(ModEntities.RIFT_BLAST.get(),
                    arcaneresearch.client.renderers.entity.RiftBlastRenderer::new);
            event.registerEntityRenderer(ModEntities.TURRET_CROSSBOW.get(),
                    arcaneresearch.client.renderers.entity.TurretCrossbowRenderer::new);
            event.registerEntityRenderer(ModEntities.TURRET_CROSSBOW_ADVANCED.get(),
                    arcaneresearch.client.renderers.entity.TurretCrossbowAdvancedRenderer::new);
            event.registerEntityRenderer(ModEntities.ARCANE_BORE.get(),
                    arcaneresearch.client.renderers.entity.ArcaneBoreRenderer::new);
            event.registerEntityRenderer(ModEntities.ARCANERESEARCH_GOLEM.get(),
                    arcaneresearch.client.renderers.entity.GolemRenderer::new);
            LOGGER.info("Registered Arcaneresearch entity renderers");
        }
        @SubscribeEvent
        public static void onRegisterKeyMappings(net.minecraftforge.client.event.RegisterKeyMappingsEvent event) {
            KeyHandler.registerKeyMappings(event);
        }
        @SubscribeEvent
        public static void onRegisterTooltipComponents(
                net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent event) {
            arcaneresearch.client.lib.events.AspectTooltipEvents.registerFactory(event);
        }
        @SubscribeEvent
        public static void onModifyBakingResult(net.minecraftforge.client.event.ModelEvent.ModifyBakingResult event) {
            try {
                arcaneresearch.client.models.block.CrystalBakedModel crystalModel =
                        arcaneresearch.client.models.block.CrystalBakedModel.create();
                String[] crystals = arcaneresearch.proxies.ProxyBlock.crystalModelPaths();
                int replaced = 0;
                for (String name : crystals) {
                    for (java.util.Map.Entry<net.minecraft.resources.ResourceLocation,
                            net.minecraft.client.resources.model.BakedModel> entry
                            : event.getModels().entrySet()) {
                        net.minecraft.resources.ResourceLocation key = entry.getKey();
                        if (!MODID.equals(key.getNamespace()) || !name.equals(key.getPath())) {
                            continue;
                        }
                        String variant = key instanceof net.minecraft.client.resources.model.ModelResourceLocation mrl
                                ? mrl.getVariant() : "";
                        if (!variant.isEmpty() && !variant.contains("size") && !variant.contains("gen")) {
                            continue;
                        }
                        entry.setValue(crystalModel);
                        replaced++;
                    }
                }
                LOGGER.info("Replaced {} crystal baked models with CrystalBakedModel", replaced);
            } catch (Exception e) {
                LOGGER.error("Failed to install CrystalBakedModel", e);
            }
        }
        @SubscribeEvent
        public static void onRegisterItemColors(net.minecraftforge.client.event.RegisterColorHandlersEvent.Item event) {
            arcaneresearch.common.blocks.misc.BlockNitor[] nitorBlocks = {
                    (arcaneresearch.common.blocks.misc.BlockNitor) ModBlocks.NITOR_WHITE.get(),
                    (arcaneresearch.common.blocks.misc.BlockNitor) ModBlocks.NITOR_ORANGE.get(),
                    (arcaneresearch.common.blocks.misc.BlockNitor) ModBlocks.NITOR_MAGENTA.get(),
                    (arcaneresearch.common.blocks.misc.BlockNitor) ModBlocks.NITOR_LIGHT_BLUE.get(),
                    (arcaneresearch.common.blocks.misc.BlockNitor) ModBlocks.NITOR_YELLOW.get(),
                    (arcaneresearch.common.blocks.misc.BlockNitor) ModBlocks.NITOR_LIME.get(),
                    (arcaneresearch.common.blocks.misc.BlockNitor) ModBlocks.NITOR_PINK.get(),
                    (arcaneresearch.common.blocks.misc.BlockNitor) ModBlocks.NITOR_GRAY.get(),
                    (arcaneresearch.common.blocks.misc.BlockNitor) ModBlocks.NITOR_LIGHT_GRAY.get(),
                    (arcaneresearch.common.blocks.misc.BlockNitor) ModBlocks.NITOR_CYAN.get(),
                    (arcaneresearch.common.blocks.misc.BlockNitor) ModBlocks.NITOR_PURPLE.get(),
                    (arcaneresearch.common.blocks.misc.BlockNitor) ModBlocks.NITOR_BLUE.get(),
                    (arcaneresearch.common.blocks.misc.BlockNitor) ModBlocks.NITOR_BROWN.get(),
                    (arcaneresearch.common.blocks.misc.BlockNitor) ModBlocks.NITOR_GREEN.get(),
                    (arcaneresearch.common.blocks.misc.BlockNitor) ModBlocks.NITOR_RED.get(),
                    (arcaneresearch.common.blocks.misc.BlockNitor) ModBlocks.NITOR_BLACK.get()
            };
            for (arcaneresearch.common.blocks.misc.BlockNitor nitorBlock : nitorBlocks) {
                final int nitorColor = nitorBlock.getColorValue();
                event.register((stack, tintIndex) -> nitorColor, nitorBlock.asItem());
            }
            event.register(
                (stack, tintIndex) -> tintIndex == 1
                        ? TintHelper.phialAspect(stack) : 0xFFFFFF,
                ModItems.PHIAL_FILLED.get()
            );
            event.register(
                (stack, tintIndex) -> tintIndex == 1
                        ? TintHelper.jarAspect(stack) : 0xFFFFFF,
                ModBlocks.JAR_NORMAL.get().asItem(),
                ModBlocks.JAR_VOID.get().asItem()
            );
            LOGGER.info("Registered Arcaneresearch item color handlers");
            event.register(
                (stack, tintIndex) -> TintHelper.crystalEssenceAspect(stack),
                ModItems.CRYSTAL_ESSENCE.get()
            );
            event.register(
                (stack, tintIndex) -> TintHelper.wispEssenceAspect(stack),
                ModItems.WISP_ESSENCE.get()
            );
            event.register(
                (stack, tintIndex) -> tintIndex == 1
                        ? TintHelper.labelAspect(stack) : 0xFFFFFF,
                ModItems.LABEL_FILLED.get()
            );
            event.register((stack, tintIndex) -> Aspect.AIR.getColor(), ModBlocks.CRYSTAL_AIR.get().asItem());
            event.register((stack, tintIndex) -> Aspect.FIRE.getColor(), ModBlocks.CRYSTAL_FIRE.get().asItem());
            event.register((stack, tintIndex) -> Aspect.WATER.getColor(), ModBlocks.CRYSTAL_WATER.get().asItem());
            event.register((stack, tintIndex) -> Aspect.EARTH.getColor(), ModBlocks.CRYSTAL_EARTH.get().asItem());
            event.register((stack, tintIndex) -> Aspect.ORDER.getColor(), ModBlocks.CRYSTAL_ORDER.get().asItem());
            event.register((stack, tintIndex) -> Aspect.ENTROPY.getColor(), ModBlocks.CRYSTAL_ENTROPY.get().asItem());
            event.register((stack, tintIndex) -> Aspect.FLUX.getColor(), ModBlocks.CRYSTAL_FLUX.get().asItem());
            event.register(
                (stack, tintIndex) -> TintHelper.candleMapColor(stack),
                ModBlocks.CANDLE_WHITE.get().asItem(),
                ModBlocks.CANDLE_ORANGE.get().asItem(),
                ModBlocks.CANDLE_MAGENTA.get().asItem(),
                ModBlocks.CANDLE_LIGHT_BLUE.get().asItem(),
                ModBlocks.CANDLE_YELLOW.get().asItem(),
                ModBlocks.CANDLE_LIME.get().asItem(),
                ModBlocks.CANDLE_PINK.get().asItem(),
                ModBlocks.CANDLE_GRAY.get().asItem(),
                ModBlocks.CANDLE_LIGHT_GRAY.get().asItem(),
                ModBlocks.CANDLE_CYAN.get().asItem(),
                ModBlocks.CANDLE_PURPLE.get().asItem(),
                ModBlocks.CANDLE_BLUE.get().asItem(),
                ModBlocks.CANDLE_BROWN.get().asItem(),
                ModBlocks.CANDLE_GREEN.get().asItem(),
                ModBlocks.CANDLE_RED.get().asItem(),
                ModBlocks.CANDLE_BLACK.get().asItem()
            );
            event.register((stack, tintIndex) -> net.minecraft.world.level.GrassColor.get(0.5D, 1.0D),
                    ModBlocks.GRASS_AMBIENT.get().asItem());
            event.register(
                (stack, tintIndex) -> TintHelper.bannerColor(stack, tintIndex),
                ModBlocks.BANNER_WHITE.get().asItem(),
                ModBlocks.BANNER_ORANGE.get().asItem(),
                ModBlocks.BANNER_MAGENTA.get().asItem(),
                ModBlocks.BANNER_LIGHT_BLUE.get().asItem(),
                ModBlocks.BANNER_YELLOW.get().asItem(),
                ModBlocks.BANNER_LIME.get().asItem(),
                ModBlocks.BANNER_PINK.get().asItem(),
                ModBlocks.BANNER_GRAY.get().asItem(),
                ModBlocks.BANNER_LIGHT_GRAY.get().asItem(),
                ModBlocks.BANNER_CYAN.get().asItem(),
                ModBlocks.BANNER_PURPLE.get().asItem(),
                ModBlocks.BANNER_BLUE.get().asItem(),
                ModBlocks.BANNER_BROWN.get().asItem(),
                ModBlocks.BANNER_GREEN.get().asItem(),
                ModBlocks.BANNER_RED.get().asItem(),
                ModBlocks.BANNER_BLACK.get().asItem(),
                ModBlocks.BANNER_CRIMSON_CULT.get().asItem()
            );
            event.register(
                (stack, tintIndex) -> event.getBlockColors().getColor(
                        net.minecraft.world.level.block.Block.byItem(stack.getItem()).defaultBlockState(),
                        null, null, tintIndex),
                ModBlocks.GREATWOOD_LEAVES.get().asItem(),
                ModBlocks.SILVERWOOD_LEAVES.get().asItem()
            );
            event.register(
                (stack, tintIndex) -> TintHelper.casterFocusColor(stack, tintIndex),
                ModItems.CASTER_BASIC.get()
            );
            event.register(
                (stack, tintIndex) -> TintHelper.focusColor(stack),
                ModItems.FOCUS_1.get(),
                ModItems.FOCUS_BLANK.get(),
                ModItems.FOCUS_ADVANCED.get()
            );
            event.register(
                (stack, tintIndex) -> TintHelper.golemMaterialColor(stack),
                ModItems.GOLEM_PLACER.get()
            );
            event.register(
                (stack, tintIndex) -> TintHelper.dyeableColor(stack, tintIndex),
                ModItems.VOID_ROBE_HELM.get(),
                ModItems.VOID_ROBE_CHEST.get(),
                ModItems.VOID_ROBE_LEGS.get(),
                ModItems.CLOTH_CHEST.get(),
                ModItems.CLOTH_LEGS.get(),
                ModItems.CLOTH_BOOTS.get()
            );
        }
        @SubscribeEvent
        public static void onRegisterBlockColors(net.minecraftforge.client.event.RegisterColorHandlersEvent.Block event) {
            event.register(
                (state, level, pos, tintIndex) -> {
                    if (state.getBlock() instanceof arcaneresearch.common.blocks.world.ore.BlockCrystalTC crystal) {
                        return crystal.getAspect().getColor();
                    }
                    return 0xFFFFFF;
                },
                ModBlocks.CRYSTAL_AIR.get(),
                ModBlocks.CRYSTAL_FIRE.get(),
                ModBlocks.CRYSTAL_WATER.get(),
                ModBlocks.CRYSTAL_EARTH.get(),
                ModBlocks.CRYSTAL_ORDER.get(),
                ModBlocks.CRYSTAL_ENTROPY.get(),
                ModBlocks.CRYSTAL_FLUX.get()
            );
            event.register(
                (state, level, pos, tintIndex) -> {
                    if (state.getBlock() instanceof arcaneresearch.common.blocks.basic.BlockCandle candle) {
                        return candle.getColor().getMapColor().col;
                    }
                    return 0xFFFFFF;
                },
                ModBlocks.CANDLE_WHITE.get(),
                ModBlocks.CANDLE_ORANGE.get(),
                ModBlocks.CANDLE_MAGENTA.get(),
                ModBlocks.CANDLE_LIGHT_BLUE.get(),
                ModBlocks.CANDLE_YELLOW.get(),
                ModBlocks.CANDLE_LIME.get(),
                ModBlocks.CANDLE_PINK.get(),
                ModBlocks.CANDLE_GRAY.get(),
                ModBlocks.CANDLE_LIGHT_GRAY.get(),
                ModBlocks.CANDLE_CYAN.get(),
                ModBlocks.CANDLE_PURPLE.get(),
                ModBlocks.CANDLE_BLUE.get(),
                ModBlocks.CANDLE_BROWN.get(),
                ModBlocks.CANDLE_GREEN.get(),
                ModBlocks.CANDLE_RED.get(),
                ModBlocks.CANDLE_BLACK.get()
            );
            event.register(
                (state, level, pos, tintIndex) -> {
                    if (tintIndex == 0 && level != null && pos != null
                            && level.getBlockEntity(pos) instanceof arcaneresearch.common.tiles.devices.TileStabilizer stabilizer) {
                        return arcaneresearch.common.blocks.devices.BlockStabilizer.colorMultiplier(stabilizer.getEnergy());
                    }
                    return tintIndex == 0
                            ? arcaneresearch.common.blocks.devices.BlockStabilizer.colorMultiplier(0)
                            : 0xFFFFFF;
                },
                ModBlocks.STABILIZER.get()
            );
            event.register(
                (state, level, pos, tintIndex) -> {
                    if (state.getBlock() instanceof arcaneresearch.common.blocks.basic.BlockBannerTC banner) {
                        net.minecraft.world.item.DyeColor dye = banner.getDyeColor();
                        return dye != null ? dye.getMapColor().col
                                : net.minecraft.world.level.material.MapColor.COLOR_RED.col;
                    }
                    return 0xFFFFFF;
                },
                ModBlocks.BANNER_WHITE.get(),
                ModBlocks.BANNER_ORANGE.get(),
                ModBlocks.BANNER_MAGENTA.get(),
                ModBlocks.BANNER_LIGHT_BLUE.get(),
                ModBlocks.BANNER_YELLOW.get(),
                ModBlocks.BANNER_LIME.get(),
                ModBlocks.BANNER_PINK.get(),
                ModBlocks.BANNER_GRAY.get(),
                ModBlocks.BANNER_LIGHT_GRAY.get(),
                ModBlocks.BANNER_CYAN.get(),
                ModBlocks.BANNER_PURPLE.get(),
                ModBlocks.BANNER_BLUE.get(),
                ModBlocks.BANNER_BROWN.get(),
                ModBlocks.BANNER_GREEN.get(),
                ModBlocks.BANNER_RED.get(),
                ModBlocks.BANNER_BLACK.get(),
                ModBlocks.BANNER_CRIMSON_CULT.get()
            );
            event.register(
                (state, level, pos, tintIndex) -> {
                    if (level != null && pos != null) {
                        return net.minecraft.client.renderer.BiomeColors.getAverageGrassColor(level, pos);
                    }
                    return net.minecraft.world.level.GrassColor.get(0.5D, 1.0D);
                },
                ModBlocks.GRASS_AMBIENT.get()
            );
            event.register(
                (state, level, pos, tintIndex) ->
                        TintHelper.nitorBlockColor(state),
                ModBlocks.NITOR_WHITE.get(),
                ModBlocks.NITOR_ORANGE.get(),
                ModBlocks.NITOR_MAGENTA.get(),
                ModBlocks.NITOR_LIGHT_BLUE.get(),
                ModBlocks.NITOR_YELLOW.get(),
                ModBlocks.NITOR_LIME.get(),
                ModBlocks.NITOR_PINK.get(),
                ModBlocks.NITOR_GRAY.get(),
                ModBlocks.NITOR_LIGHT_GRAY.get(),
                ModBlocks.NITOR_CYAN.get(),
                ModBlocks.NITOR_PURPLE.get(),
                ModBlocks.NITOR_BLUE.get(),
                ModBlocks.NITOR_BROWN.get(),
                ModBlocks.NITOR_GREEN.get(),
                ModBlocks.NITOR_RED.get(),
                ModBlocks.NITOR_BLACK.get()
            );
            event.register(
                (state, level, pos, tintIndex) -> {
                    if (state.getBlock() == ModBlocks.SILVERWOOD_LEAVES.get()) {
                        return 0xFFFFFF;
                    }
                    if (level != null && pos != null) {
                        return net.minecraft.client.renderer.BiomeColors.getAverageFoliageColor(level, pos);
                    }
                    return net.minecraft.world.level.FoliageColor.getDefaultColor();
                },
                ModBlocks.GREATWOOD_LEAVES.get(),
                ModBlocks.SILVERWOOD_LEAVES.get()
            );
            event.register(
                (state, level, pos, tintIndex) -> {
                    if (tintIndex == 1 && level != null && pos != null
                            && state.getBlock() instanceof arcaneresearch.common.blocks.essentia.BlockTube
                            && level.getBlockEntity(pos) instanceof arcaneresearch.common.tiles.essentia.TileTubeFilter filter
                            && filter.aspectFilter != null) {
                        return filter.aspectFilter.getColor();
                    }
                    return 0xFFFFFF;
                },
                ModBlocks.TUBE_FILTER.get()
            );
            event.register(
                (state, level, pos, tintIndex) ->
                        TintHelper.inlayChargeColor(state, tintIndex),
                ModBlocks.INLAY.get()
            );
            LOGGER.info("Registered Arcaneresearch block color handlers");
        }
    }
    @OnlyIn(Dist.CLIENT)
    public static Level getClientWorld() {
        return Minecraft.getInstance().level;
    }
    @OnlyIn(Dist.CLIENT)
    public static boolean isShiftKeyDown() {
        return net.minecraft.client.gui.screens.Screen.hasShiftDown();
    }
}
