package arcaneresearch.init;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries; 

import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.tiles.crafting.*;
import arcaneresearch.common.tiles.devices.*;
import arcaneresearch.common.tiles.essentia.*;
import arcaneresearch.common.tiles.misc.TileBanner;
import arcaneresearch.common.tiles.misc.TileBarrierStone;
import arcaneresearch.common.tiles.misc.TileHole;
import arcaneresearch.common.tiles.misc.TileNitor;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Arcaneresearch.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileJar>> JAR =
            BLOCK_ENTITIES.register("jar",
                    () -> new BlockEntityType<>(TileJar::new,
                            ModBlocks.JAR_NORMAL.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileJarVoid>> JAR_VOID =
            BLOCK_ENTITIES.register("jar_void",
                    () -> new BlockEntityType<>(TileJarVoid::new,
                            ModBlocks.JAR_VOID.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DeconstructTile>> DECONSTRUCTION_TABLE =
            BLOCK_ENTITIES.register("deconstruction_table",
                    () -> new BlockEntityType<>(DeconstructTile::new,
                            ModBlocks.DECONSTRUCTION_TABLE.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileJarBrain>> JAR_BRAIN =
            BLOCK_ENTITIES.register("jar_brain",
                    () -> new BlockEntityType<>(TileJarBrain::new,
                            ModBlocks.JAR_BRAIN.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileAlembic>> ALEMBIC =
            BLOCK_ENTITIES.register("alembic",
                    () -> new BlockEntityType<>(TileAlembic::new,
                            ModBlocks.ALEMBIC.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileSmelter>> SMELTER =
            BLOCK_ENTITIES.register("smelter",
                    () -> new BlockEntityType<>(TileSmelter::new,
                            ModBlocks.SMELTER.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileTube>> TUBE =
            BLOCK_ENTITIES.register("tube",
                    () -> new BlockEntityType<>(TileTube::new,
                            ModBlocks.TUBE_NORMAL.get(),
                            ModBlocks.TUBE_RESTRICTED.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileTubeFilter>> TUBE_FILTER =
            BLOCK_ENTITIES.register("tube_filter",
                    () -> new BlockEntityType<>(TileTubeFilter::new,
                            ModBlocks.TUBE_FILTER.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileTubeValve>> TUBE_VALVE =
            BLOCK_ENTITIES.register("tube_valve",
                    () -> new BlockEntityType<>(TileTubeValve::new,
                            ModBlocks.TUBE_VALVE.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileTubeBuffer>> TUBE_BUFFER =
            BLOCK_ENTITIES.register("tube_buffer",
                    () -> new BlockEntityType<>(TileTubeBuffer::new,
                            ModBlocks.TUBE_BUFFER.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileTubeOneway>> TUBE_ONEWAY =
            BLOCK_ENTITIES.register("tube_oneway",
                    () -> new BlockEntityType<>(TileTubeOneway::new,
                            ModBlocks.TUBE_ONEWAY.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileTubeRestrict>> TUBE_RESTRICT =
            BLOCK_ENTITIES.register("tube_restrict",
                    () -> new BlockEntityType<>(TileTubeRestrict::new,
                            ModBlocks.TUBE_RESTRICTED.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileCentrifuge>> CENTRIFUGE =
            BLOCK_ENTITIES.register("centrifuge",
                    () -> new BlockEntityType<>(TileCentrifuge::new,
                            ModBlocks.CENTRIFUGE.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEssentiaReservoir>> ESSENTIA_RESERVOIR =
            BLOCK_ENTITIES.register("essentia_reservoir",
                    () -> new BlockEntityType<>(TileEssentiaReservoir::new,
                            ModBlocks.ESSENTIA_RESERVOIR.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEssentiaInput>> ESSENTIA_INPUT =
            BLOCK_ENTITIES.register("essentia_input",
                    () -> new BlockEntityType<>(TileEssentiaInput::new,
                            ModBlocks.ESSENTIA_INPUT.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEssentiaOutput>> ESSENTIA_OUTPUT =
            BLOCK_ENTITIES.register("essentia_output",
                    () -> new BlockEntityType<>(TileEssentiaOutput::new,
                            ModBlocks.ESSENTIA_OUTPUT.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TilePedestal>> PEDESTAL =
            BLOCK_ENTITIES.register("pedestal",
                    () -> new BlockEntityType<>(TilePedestal::new,
                            ModBlocks.PEDESTAL_ARCANE.get(),
                            ModBlocks.PEDESTAL_ANCIENT.get(),
                            ModBlocks.PEDESTAL_ELDRITCH.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileCrucible>> CRUCIBLE =
            BLOCK_ENTITIES.register("crucible",
                    () -> new BlockEntityType<>(TileCrucible::new,
                            ModBlocks.CRUCIBLE.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileArcaneWorkbench>> ARCANE_WORKBENCH =
            BLOCK_ENTITIES.register("arcane_workbench",
                    () -> new BlockEntityType<>(TileArcaneWorkbench::new,
                            ModBlocks.ARCANE_WORKBENCH.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileResearchTable>> RESEARCH_TABLE =
            BLOCK_ENTITIES.register("research_table",
                    () -> new BlockEntityType<>(TileResearchTable::new,
                            ModBlocks.RESEARCH_TABLE.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileInfusionMatrix>> INFUSION_MATRIX =
            BLOCK_ENTITIES.register("infusion_matrix",
                    () -> new BlockEntityType<>(TileInfusionMatrix::new,
                            ModBlocks.INFUSION_MATRIX.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileFocalManipulator>> FOCAL_MANIPULATOR =
            BLOCK_ENTITIES.register("focal_manipulator",
                    () -> new BlockEntityType<>(TileFocalManipulator::new,
                            ModBlocks.FOCAL_MANIPULATOR.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileThaumatorium>> THAUMATORIUM =
            BLOCK_ENTITIES.register("thaumatorium",
                    () -> new BlockEntityType<>(TileThaumatorium::new,
                            ModBlocks.THAUMATORIUM.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileThaumatoriumTop>> THAUMATORIUM_TOP =
            BLOCK_ENTITIES.register("thaumatorium_top",
                    () -> new BlockEntityType<>(TileThaumatoriumTop::new,
                            ModBlocks.THAUMATORIUM_TOP.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TilePatternCrafter>> PATTERN_CRAFTER =
            BLOCK_ENTITIES.register("pattern_crafter",
                    () -> new BlockEntityType<>(TilePatternCrafter::new,
                            ModBlocks.PATTERN_CRAFTER.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileBellows>> BELLOWS =
            BLOCK_ENTITIES.register("bellows",
                    () -> new BlockEntityType<>(TileBellows::new,
                            ModBlocks.BELLOWS.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileHungryChest>> HUNGRY_CHEST =
            BLOCK_ENTITIES.register("hungry_chest",
                    () -> new BlockEntityType<>(TileHungryChest::new,
                            ModBlocks.HUNGRY_CHEST.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileLampArcane>> LAMP_ARCANE =
            BLOCK_ENTITIES.register("lamp_arcane",
                    () -> new BlockEntityType<>(TileLampArcane::new,
                            ModBlocks.LAMP_ARCANE.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileLampGrowth>> LAMP_GROWTH =
            BLOCK_ENTITIES.register("lamp_growth",
                    () -> new BlockEntityType<>(TileLampGrowth::new,
                            ModBlocks.LAMP_GROWTH.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileLampFertility>> LAMP_FERTILITY =
            BLOCK_ENTITIES.register("lamp_fertility",
                    () -> new BlockEntityType<>(TileLampFertility::new,
                            ModBlocks.LAMP_FERTILITY.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileMirror>> MIRROR_ITEM =
            BLOCK_ENTITIES.register("mirror_item",
                    () -> new BlockEntityType<>(TileMirror::new,
                            ModBlocks.MIRROR_ITEM.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileStabilizer>> STABILIZER =
            BLOCK_ENTITIES.register("stabilizer",
                    () -> new BlockEntityType<>(TileStabilizer::new,
                            ModBlocks.STABILIZER.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileMirrorEssentia>> MIRROR_ESSENTIA =
            BLOCK_ENTITIES.register("mirror_essentia",
                    () -> new BlockEntityType<>(TileMirrorEssentia::new,
                            ModBlocks.MIRROR_ESSENTIA.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileVisGenerator>> VIS_GENERATOR =
            BLOCK_ENTITIES.register("vis_generator",
                    () -> new BlockEntityType<>(TileVisGenerator::new,
                            ModBlocks.VIS_GENERATOR.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileCondenser>> CONDENSER =
            BLOCK_ENTITIES.register("condenser",
                    () -> new BlockEntityType<>(TileCondenser::new,
                            ModBlocks.CONDENSER.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileArcaneEar>> ARCANE_EAR =
            BLOCK_ENTITIES.register("arcane_ear",
                    () -> new BlockEntityType<>(TileArcaneEar::new,
                            ModBlocks.ARCANE_EAR.get(),
                            ModBlocks.ARCANE_EAR_TOGGLE.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileRedstoneRelay>> REDSTONE_RELAY =
            BLOCK_ENTITIES.register("redstone_relay",
                    () -> new BlockEntityType<>(TileRedstoneRelay::new,
                            ModBlocks.REDSTONE_RELAY.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileInfernalFurnace>> INFERNAL_FURNACE =
            BLOCK_ENTITIES.register("infernal_furnace",
                    () -> new BlockEntityType<>(TileInfernalFurnace::new,
                            ModBlocks.INFERNAL_FURNACE.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileSpa>> SPA =
            BLOCK_ENTITIES.register("spa",
                    () -> new BlockEntityType<>(TileSpa::new,
                            ModBlocks.SPA.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileFluxScrubber>> FLUX_SCRUBBER =
            BLOCK_ENTITIES.register("flux_scrubber",
                    () -> new BlockEntityType<>(TileFluxScrubber::new,
                            ModBlocks.FLUX_SCRUBBER.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileGolemBuilder>> GOLEM_BUILDER =
            BLOCK_ENTITIES.register("golem_builder",
                    () -> new BlockEntityType<>(TileGolemBuilder::new,
                            ModBlocks.GOLEM_BUILDER.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileHole>> HOLE =
            BLOCK_ENTITIES.register("hole",
                    () -> new BlockEntityType<>(TileHole::new,
                            ModBlocks.HOLE.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileBarrierStone>> BARRIER_STONE =
            BLOCK_ENTITIES.register("barrier_stone",
                    () -> new BlockEntityType<>(TileBarrierStone::new,
                            ModBlocks.PAVING_STONE_BARRIER.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileNitor>> NITOR =
            BLOCK_ENTITIES.register("nitor",
                    () -> new BlockEntityType<>(TileNitor::new,
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
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileLevitator>> LEVITATOR =
            BLOCK_ENTITIES.register("levitator",
                    () -> new BlockEntityType<>(TileLevitator::new,
                            ModBlocks.LEVITATOR.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileRechargePedestal>> RECHARGE_PEDESTAL =
            BLOCK_ENTITIES.register("recharge_pedestal",
                    () -> new BlockEntityType<>(TileRechargePedestal::new,
                            ModBlocks.RECHARGE_PEDESTAL.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileDioptra>> DIOPTRA =
            BLOCK_ENTITIES.register("dioptra",
                    () -> new BlockEntityType<>(TileDioptra::new,
                            ModBlocks.DIOPTRA.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileVoidSiphon>> VOID_SIPHON =
            BLOCK_ENTITIES.register("void_siphon",
                    () -> new BlockEntityType<>(TileVoidSiphon::new,
                            ModBlocks.VOID_SIPHON.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TilePotionSprayer>> POTION_SPRAYER =
            BLOCK_ENTITIES.register("potion_sprayer",
                    () -> new BlockEntityType<>(TilePotionSprayer::new,
                            ModBlocks.POTION_SPRAYER.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileWaterJug>> WATER_JUG =
            BLOCK_ENTITIES.register("water_jug",
                    () -> new BlockEntityType<>(TileWaterJug::new,
                            ModBlocks.EVERFULL_URN.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileBanner>> BANNER =
            BLOCK_ENTITIES.register("banner",
                    () -> new BlockEntityType<>(TileBanner::new,
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
                    ));

    
    
    
    
    
    
    
    
    
    
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (var holder : BLOCK_ENTITIES.getEntries()) {
            registerContainerCap(event, holder.get());
        }
        
        for (var holder : arcaneresearch.common.blocks.aura.AuraBlockEntities.BLOCK_ENTITIES.getEntries()) {
            registerContainerCap(event, holder.get());
        }
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, CRUCIBLE.get(),
                (be, side) -> be.getFluidHandler(side));
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, SPA.get(),
                (be, side) -> be.getFluidHandler(side));
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, WATER_JUG.get(),
                (be, side) -> be.getFluidHandler(side));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, VIS_GENERATOR.get(),
                (be, side) -> be.getEnergyHandler(side));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerContainerCap(RegisterCapabilitiesEvent event, BlockEntityType<?> type) {
        event.registerBlockEntity((net.neoforged.neoforge.capabilities.BlockCapability) Capabilities.Item.BLOCK,
                (BlockEntityType) type,
                (be, side) -> itemHandlerFor((BlockEntity) be, (Direction) side));
    }

    @javax.annotation.Nullable
    private static ResourceHandler<ItemResource> itemHandlerFor(BlockEntity be, @javax.annotation.Nullable Direction side) {
        if (be instanceof TileHungryChest chest) {
            return VanillaContainerWrapper.of(chest);
        }
        if (side != null && be instanceof arcaneresearch.common.tiles.TileArcaneresearchInventory inventory) {
            if (inventory instanceof WorldlyContainer worldly) {
                return new WorldlyContainerWrapper(worldly, side);
            }
            return VanillaContainerWrapper.of(inventory);
        }
        return null;
    }
}
