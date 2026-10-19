package arcaneresearch.init;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.tiles.crafting.*;
import arcaneresearch.common.tiles.devices.*;
import arcaneresearch.common.tiles.essentia.*;
import arcaneresearch.common.tiles.misc.TileBanner;
import arcaneresearch.common.tiles.misc.TileBarrierStone;
import arcaneresearch.common.tiles.misc.TileHole;
import arcaneresearch.common.tiles.misc.TileNitor;
public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Arcaneresearch.MODID);
    public static final RegistryObject<BlockEntityType<TileJar>> JAR =
            BLOCK_ENTITIES.register("jar",
                    () -> BlockEntityType.Builder.of(TileJar::new,
                            ModBlocks.JAR_NORMAL.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<DeconstructTile>> DECONSTRUCTION_TABLE =
            BLOCK_ENTITIES.register("deconstruction_table",
                    () -> BlockEntityType.Builder.of(DeconstructTile::new,
                            ModBlocks.DECONSTRUCTION_TABLE.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileJarVoid>> JAR_VOID =
            BLOCK_ENTITIES.register("jar_void",
                    () -> BlockEntityType.Builder.of(TileJarVoid::new,
                            ModBlocks.JAR_VOID.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileJarBrain>> JAR_BRAIN =
            BLOCK_ENTITIES.register("jar_brain",
                    () -> BlockEntityType.Builder.of(TileJarBrain::new,
                            ModBlocks.JAR_BRAIN.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileAlembic>> ALEMBIC =
            BLOCK_ENTITIES.register("alembic",
                    () -> BlockEntityType.Builder.of(TileAlembic::new,
                            ModBlocks.ALEMBIC.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileSmelter>> SMELTER =
            BLOCK_ENTITIES.register("smelter",
                    () -> BlockEntityType.Builder.of(TileSmelter::new,
                            ModBlocks.SMELTER.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileTube>> TUBE =
            BLOCK_ENTITIES.register("tube",
                    () -> BlockEntityType.Builder.of(TileTube::new,
                            ModBlocks.TUBE_NORMAL.get(),
                            ModBlocks.TUBE_RESTRICTED.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileTubeFilter>> TUBE_FILTER =
            BLOCK_ENTITIES.register("tube_filter",
                    () -> BlockEntityType.Builder.of(TileTubeFilter::new,
                            ModBlocks.TUBE_FILTER.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileTubeValve>> TUBE_VALVE =
            BLOCK_ENTITIES.register("tube_valve",
                    () -> BlockEntityType.Builder.of(TileTubeValve::new,
                            ModBlocks.TUBE_VALVE.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileTubeBuffer>> TUBE_BUFFER =
            BLOCK_ENTITIES.register("tube_buffer",
                    () -> BlockEntityType.Builder.of(TileTubeBuffer::new,
                            ModBlocks.TUBE_BUFFER.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileTubeOneway>> TUBE_ONEWAY =
            BLOCK_ENTITIES.register("tube_oneway",
                    () -> BlockEntityType.Builder.of(TileTubeOneway::new,
                            ModBlocks.TUBE_ONEWAY.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileTubeRestrict>> TUBE_RESTRICT =
            BLOCK_ENTITIES.register("tube_restrict",
                    () -> BlockEntityType.Builder.of(TileTubeRestrict::new,
                            ModBlocks.TUBE_RESTRICTED.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileCentrifuge>> CENTRIFUGE =
            BLOCK_ENTITIES.register("centrifuge",
                    () -> BlockEntityType.Builder.of(TileCentrifuge::new,
                            ModBlocks.CENTRIFUGE.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileEssentiaReservoir>> ESSENTIA_RESERVOIR =
            BLOCK_ENTITIES.register("essentia_reservoir",
                    () -> BlockEntityType.Builder.of(TileEssentiaReservoir::new,
                            ModBlocks.ESSENTIA_RESERVOIR.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileEssentiaInput>> ESSENTIA_INPUT =
            BLOCK_ENTITIES.register("essentia_input",
                    () -> BlockEntityType.Builder.of(TileEssentiaInput::new,
                            ModBlocks.ESSENTIA_INPUT.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileEssentiaOutput>> ESSENTIA_OUTPUT =
            BLOCK_ENTITIES.register("essentia_output",
                    () -> BlockEntityType.Builder.of(TileEssentiaOutput::new,
                            ModBlocks.ESSENTIA_OUTPUT.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TilePedestal>> PEDESTAL =
            BLOCK_ENTITIES.register("pedestal",
                    () -> BlockEntityType.Builder.of(TilePedestal::new,
                            ModBlocks.PEDESTAL_ARCANE.get(),
                            ModBlocks.PEDESTAL_ANCIENT.get(),
                            ModBlocks.PEDESTAL_ELDRITCH.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileCrucible>> CRUCIBLE =
            BLOCK_ENTITIES.register("crucible",
                    () -> BlockEntityType.Builder.of(TileCrucible::new,
                            ModBlocks.CRUCIBLE.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileArcaneWorkbench>> ARCANE_WORKBENCH =
            BLOCK_ENTITIES.register("arcane_workbench",
                    () -> BlockEntityType.Builder.of(TileArcaneWorkbench::new,
                            ModBlocks.ARCANE_WORKBENCH.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileResearchTable>> RESEARCH_TABLE =
            BLOCK_ENTITIES.register("research_table",
                    () -> BlockEntityType.Builder.of(TileResearchTable::new,
                            ModBlocks.RESEARCH_TABLE.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileInfusionMatrix>> INFUSION_MATRIX =
            BLOCK_ENTITIES.register("infusion_matrix",
                    () -> BlockEntityType.Builder.of(TileInfusionMatrix::new,
                            ModBlocks.INFUSION_MATRIX.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileFocalManipulator>> FOCAL_MANIPULATOR =
            BLOCK_ENTITIES.register("focal_manipulator",
                    () -> BlockEntityType.Builder.of(TileFocalManipulator::new,
                            ModBlocks.FOCAL_MANIPULATOR.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileThaumatorium>> THAUMATORIUM =
            BLOCK_ENTITIES.register("thaumatorium",
                    () -> BlockEntityType.Builder.of(TileThaumatorium::new,
                            ModBlocks.THAUMATORIUM.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileThaumatoriumTop>> THAUMATORIUM_TOP =
            BLOCK_ENTITIES.register("thaumatorium_top",
                    () -> BlockEntityType.Builder.of(TileThaumatoriumTop::new,
                            ModBlocks.THAUMATORIUM_TOP.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TilePatternCrafter>> PATTERN_CRAFTER =
            BLOCK_ENTITIES.register("pattern_crafter",
                    () -> BlockEntityType.Builder.of(TilePatternCrafter::new,
                            ModBlocks.PATTERN_CRAFTER.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileBellows>> BELLOWS =
            BLOCK_ENTITIES.register("bellows",
                    () -> BlockEntityType.Builder.of(TileBellows::new,
                            ModBlocks.BELLOWS.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileHungryChest>> HUNGRY_CHEST =
            BLOCK_ENTITIES.register("hungry_chest",
                    () -> BlockEntityType.Builder.of(TileHungryChest::new,
                            ModBlocks.HUNGRY_CHEST.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileLampArcane>> LAMP_ARCANE =
            BLOCK_ENTITIES.register("lamp_arcane",
                    () -> BlockEntityType.Builder.of(TileLampArcane::new,
                            ModBlocks.LAMP_ARCANE.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileLampGrowth>> LAMP_GROWTH =
            BLOCK_ENTITIES.register("lamp_growth",
                    () -> BlockEntityType.Builder.of(TileLampGrowth::new,
                            ModBlocks.LAMP_GROWTH.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileLampFertility>> LAMP_FERTILITY =
            BLOCK_ENTITIES.register("lamp_fertility",
                    () -> BlockEntityType.Builder.of(TileLampFertility::new,
                            ModBlocks.LAMP_FERTILITY.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileMirror>> MIRROR_ITEM =
            BLOCK_ENTITIES.register("mirror_item",
                    () -> BlockEntityType.Builder.of(TileMirror::new,
                            ModBlocks.MIRROR_ITEM.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileStabilizer>> STABILIZER =
            BLOCK_ENTITIES.register("stabilizer",
                    () -> BlockEntityType.Builder.of(TileStabilizer::new,
                            ModBlocks.STABILIZER.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileMirrorEssentia>> MIRROR_ESSENTIA =
            BLOCK_ENTITIES.register("mirror_essentia",
                    () -> BlockEntityType.Builder.of(TileMirrorEssentia::new,
                            ModBlocks.MIRROR_ESSENTIA.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileVisGenerator>> VIS_GENERATOR =
            BLOCK_ENTITIES.register("vis_generator",
                    () -> BlockEntityType.Builder.of(TileVisGenerator::new,
                            ModBlocks.VIS_GENERATOR.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileCondenser>> CONDENSER =
            BLOCK_ENTITIES.register("condenser",
                    () -> BlockEntityType.Builder.of(TileCondenser::new,
                            ModBlocks.CONDENSER.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileArcaneEar>> ARCANE_EAR =
            BLOCK_ENTITIES.register("arcane_ear",
                    () -> BlockEntityType.Builder.of(TileArcaneEar::new,
                            ModBlocks.ARCANE_EAR.get(),
                            ModBlocks.ARCANE_EAR_TOGGLE.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileRedstoneRelay>> REDSTONE_RELAY =
            BLOCK_ENTITIES.register("redstone_relay",
                    () -> BlockEntityType.Builder.of(TileRedstoneRelay::new,
                            ModBlocks.REDSTONE_RELAY.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileInfernalFurnace>> INFERNAL_FURNACE =
            BLOCK_ENTITIES.register("infernal_furnace",
                    () -> BlockEntityType.Builder.of(TileInfernalFurnace::new,
                            ModBlocks.INFERNAL_FURNACE.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileSpa>> SPA =
            BLOCK_ENTITIES.register("spa",
                    () -> BlockEntityType.Builder.of(TileSpa::new,
                            ModBlocks.SPA.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileFluxScrubber>> FLUX_SCRUBBER =
            BLOCK_ENTITIES.register("flux_scrubber",
                    () -> BlockEntityType.Builder.of(TileFluxScrubber::new,
                            ModBlocks.FLUX_SCRUBBER.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileGolemBuilder>> GOLEM_BUILDER =
            BLOCK_ENTITIES.register("golem_builder",
                    () -> BlockEntityType.Builder.of(TileGolemBuilder::new,
                            ModBlocks.GOLEM_BUILDER.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileHole>> HOLE =
            BLOCK_ENTITIES.register("hole",
                    () -> BlockEntityType.Builder.of(TileHole::new,
                            ModBlocks.HOLE.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileBarrierStone>> BARRIER_STONE =
            BLOCK_ENTITIES.register("barrier_stone",
                    () -> BlockEntityType.Builder.of(TileBarrierStone::new,
                            ModBlocks.PAVING_STONE_BARRIER.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileNitor>> NITOR =
            BLOCK_ENTITIES.register("nitor",
                    () -> BlockEntityType.Builder.of(TileNitor::new,
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
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileLevitator>> LEVITATOR =
            BLOCK_ENTITIES.register("levitator",
                    () -> BlockEntityType.Builder.of(TileLevitator::new,
                            ModBlocks.LEVITATOR.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileRechargePedestal>> RECHARGE_PEDESTAL =
            BLOCK_ENTITIES.register("recharge_pedestal",
                    () -> BlockEntityType.Builder.of(TileRechargePedestal::new,
                            ModBlocks.RECHARGE_PEDESTAL.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileDioptra>> DIOPTRA =
            BLOCK_ENTITIES.register("dioptra",
                    () -> BlockEntityType.Builder.of(TileDioptra::new,
                            ModBlocks.DIOPTRA.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileVoidSiphon>> VOID_SIPHON =
            BLOCK_ENTITIES.register("void_siphon",
                    () -> BlockEntityType.Builder.of(TileVoidSiphon::new,
                            ModBlocks.VOID_SIPHON.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TilePotionSprayer>> POTION_SPRAYER =
            BLOCK_ENTITIES.register("potion_sprayer",
                    () -> BlockEntityType.Builder.of(TilePotionSprayer::new,
                            ModBlocks.POTION_SPRAYER.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileWaterJug>> WATER_JUG =
            BLOCK_ENTITIES.register("water_jug",
                    () -> BlockEntityType.Builder.of(TileWaterJug::new,
                            ModBlocks.EVERFULL_URN.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<TileBanner>> BANNER =
            BLOCK_ENTITIES.register("banner",
                    () -> BlockEntityType.Builder.of(TileBanner::new,
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
                    ).build(null));
}
