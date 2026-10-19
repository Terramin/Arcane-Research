package arcaneresearch.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries; // NEOFORGE_PORT: was ForgeRegistries
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was ResourceLocation
// NEOFORGE_PORT: DeferredBlock<Block> -> DeferredBlock<Block> below.
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.common.blocks.basic.*;
import arcaneresearch.common.blocks.crafting.*;
import arcaneresearch.common.blocks.devices.*;
import arcaneresearch.common.blocks.essentia.*;
import arcaneresearch.common.blocks.misc.*;
import arcaneresearch.common.blocks.world.BlockGrassAmbient;
import arcaneresearch.common.blocks.world.BlockLoot;
import arcaneresearch.common.blocks.world.ore.BlockCrystalTC;
import arcaneresearch.common.blocks.world.ore.BlockOreTC;
import arcaneresearch.common.blocks.world.plants.*;
import arcaneresearch.common.blocks.world.taint.*;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Arcaneresearch.MODID);
    public static final DeferredRegister.Items BLOCK_ITEMS = DeferredRegister.createItems(Arcaneresearch.MODID);

    public static final DeferredBlock<Block> ARCANE_STONE = registerBlock("arcane_stone",
            props -> BlockStoneTC.create(props, true));

    public static final DeferredBlock<Block> ARCANE_STONE_BRICK = registerBlock("arcane_stone_brick",
            props -> BlockStoneTC.create(props, true));

    public static final DeferredBlock<Block> ANCIENT_STONE = registerBlock("ancient_stone",
            props -> BlockStoneTC.create(props, true));

    public static final DeferredBlock<Block> ANCIENT_STONE_TILE = registerBlock("ancient_stone_tile",
            props -> BlockStoneTC.create(props, false));

    public static final DeferredBlock<Block> ANCIENT_STONE_ROCK = registerBlock("ancient_rock",
            props -> BlockStoneTC.createUnbreakable(props));

    public static final DeferredBlock<Block> ANCIENT_STONE_GLYPHED = registerBlock("ancient_stone_glyphed",
            props -> BlockStoneTC.create(props, false));

    /**
     * Плита обсидиана TC4 (blockCosmeticSolid мет 0/1, текстура
     * "obsidiantile") — мостовая элдритч-кольца. Аналогичная запись в
     * рефе — ModBlocks.OBSIDIAN_TILE.
     */
    public static final DeferredBlock<Block> OBSIDIAN_TILE = registerBlock("obsidian_tile",
            props -> new Block(props
                    .requiresCorrectToolForDrops()
                    .strength(50.0f, 1200.0f)));

    public static final DeferredBlock<Block> ANCIENT_STONE_DOORWAY = registerBlock("ancient_pedestal_doorway",
            props -> BlockStoneTC.createUnbreakable(props));

    public static final DeferredBlock<Block> ELDRITCH_STONE_TILE = registerBlock("eldritch_stone_tile",
            props -> BlockStoneTC.createReinforced(props));

    public static final DeferredBlock<Block> ELDRITCH_STONE = registerBlock("eldritch_stone",
            props -> BlockStoneTC.create(props, true));

    public static final DeferredBlock<Block> POROUS_STONE = registerBlock("porous_stone",
            props -> new BlockStonePorous(props
                    .mapColor(MapColor.STONE)
                    .strength(1.0f, 5.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> ARCANE_STONE_STAIRS = registerBlock("arcane_stone_stairs",
            props -> new StairBlock(ARCANE_STONE.get().defaultBlockState(),
                    props.mapColor(MapColor.STONE)
                            .strength(2.0f, 10.0f)
                            .sound(SoundType.STONE)));

    public static final DeferredBlock<Block> ARCANE_STONE_BRICK_STAIRS = registerBlock("arcane_stone_brick_stairs",
            props -> new StairBlock(ARCANE_STONE_BRICK.get().defaultBlockState(),
                    props.mapColor(MapColor.STONE)
                            .strength(2.0f, 10.0f)
                            .sound(SoundType.STONE)));

    public static final DeferredBlock<Block> ANCIENT_STONE_STAIRS = registerBlock("ancient_stone_stairs",
            props -> new StairBlock(ANCIENT_STONE.get().defaultBlockState(),
                    props.mapColor(MapColor.STONE)
                            .strength(2.0f, 10.0f)
                            .sound(SoundType.STONE)));

    public static final DeferredBlock<Block> ARCANE_STONE_SLAB = registerBlock("arcane_stone_slab",
            props -> new SlabBlock(props
                    .mapColor(MapColor.STONE)
                    .strength(2.0f, 10.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> ARCANE_STONE_BRICK_SLAB = registerBlock("arcane_stone_brick_slab",
            props -> new SlabBlock(props
                    .mapColor(MapColor.STONE)
                    .strength(2.0f, 10.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> ANCIENT_STONE_SLAB = registerBlock("ancient_stone_slab",
            props -> new SlabBlock(props
                    .mapColor(MapColor.STONE)
                    .strength(2.0f, 10.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> ELDRITCH_STONE_SLAB = registerBlock("eldritch_stone_slab",
            props -> new SlabBlock(props
                    .mapColor(MapColor.STONE)
                    .strength(2.0f, 10.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> ARCANE_PILLAR = registerBlock("arcane_pillar",
            BlockPillarTC::create);

    public static final DeferredBlock<Block> ANCIENT_PILLAR = registerBlock("ancient_pillar",
            BlockPillarTC::create);

    public static final DeferredBlock<Block> ELDRITCH_PILLAR = registerBlock("eldritch_pillar",
            props -> new BlockPillarTC(props
                    .mapColor(MapColor.STONE)
                    .strength(15.0f, 1000.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> GREATWOOD_LOG = registerBlock("greatwood_log",
            BlockLogTC::createGreatwood);

    public static final DeferredBlock<Block> SILVERWOOD_LOG = registerBlock("silverwood_log",
            BlockLogTC::createSilverwood);

    public static final DeferredBlock<Block> GREATWOOD_PLANKS = registerBlock("greatwood_planks",
            BlockPlanksTC::createGreatwood);

    public static final DeferredBlock<Block> SILVERWOOD_PLANKS = registerBlock("silverwood_planks",
            BlockPlanksTC::createSilverwood);

    public static final DeferredBlock<Block> GREATWOOD_STAIRS = registerBlock("greatwood_stairs",
            props -> new BlockStairsTC(() -> GREATWOOD_PLANKS.get().defaultBlockState(),
                    props.mapColor(MapColor.COLOR_BROWN)
                            .strength(2.0f, 2.0f)
                            .sound(SoundType.WOOD), true));

    public static final DeferredBlock<Block> SILVERWOOD_STAIRS = registerBlock("silverwood_stairs",
            props -> new BlockStairsTC(() -> SILVERWOOD_PLANKS.get().defaultBlockState(),
                    props.mapColor(MapColor.QUARTZ)
                            .strength(2.0f, 2.0f)
                            .sound(SoundType.WOOD), true));

    public static final DeferredBlock<Block> GREATWOOD_SLAB = registerBlock("greatwood_slab",
            props -> new BlockSlabTC(props
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(1.2f, 2.0f)
                    .sound(SoundType.WOOD), true));

    public static final DeferredBlock<Block> SILVERWOOD_SLAB = registerBlock("silverwood_slab",
            props -> new BlockSlabTC(props
                    .mapColor(MapColor.QUARTZ)
                    .strength(1.0f, 2.0f)
                    .sound(SoundType.WOOD), true));

    public static final DeferredBlock<Block> BRASS_BLOCK = registerBlock("brass_block",
            props -> BlockMetalTC.create(props, MapColor.GOLD));

    public static final DeferredBlock<Block> THAUMIUM_BLOCK = registerBlock("thaumium_block",
            props -> BlockMetalTC.create(props, MapColor.COLOR_PURPLE));

    public static final DeferredBlock<Block> VOID_METAL_BLOCK = registerBlock("void_metal_block",
            props -> BlockMetalTC.create(props, MapColor.COLOR_BLACK));

    public static final DeferredBlock<Block> ALCHEMICAL_BRASS_BLOCK = registerBlock("alchemical_brass_block",
            props -> BlockMetalTC.create(props, MapColor.GOLD));

    public static final DeferredBlock<Block> ALCHEMICAL_BRASS_ADVANCED_BLOCK = registerBlock("alchemical_brass_advanced_block",
            props -> BlockMetalTC.create(props, MapColor.GOLD));

    public static final DeferredBlock<Block> AMBER_BLOCK = registerBlock("amber_block",
            props -> new BlockTranslucent(props
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(0.5f, 2.5f)
                    .sound(SoundType.STONE)
                    .noOcclusion()));

    public static final DeferredBlock<Block> AMBER_BRICK = registerBlock("amber_brick",
            props -> new BlockTranslucent(props
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(0.5f, 2.5f)
                    .sound(SoundType.STONE)
                    .noOcclusion()));

    public static final DeferredBlock<Block> MATRIX_SPEED = registerBlock("matrix_speed",
            props -> BlockStoneTC.create(props, false));

    public static final DeferredBlock<Block> MATRIX_COST = registerBlock("matrix_cost",
            props -> BlockStoneTC.create(props, false));

    public static final DeferredBlock<Block> ARCANE_WORKBENCH = registerBlock("arcane_workbench",
            BlockArcaneWorkbench::new);

    public static final DeferredBlock<Block> ARCANE_WORKBENCH_CHARGER = registerBlock("arcane_workbench_charger",
            BlockArcaneWorkbenchCharger::create);

    public static final DeferredBlock<Block> CRUCIBLE = registerBlock("crucible",
            BlockCrucible::new);

    public static final DeferredBlock<Block> AMBER_ORE = registerBlock("amber_ore",
            BlockOreTC::createAmberOre);

    public static final DeferredBlock<Block> CINNABAR_ORE = registerBlock("cinnabar_ore",
            BlockOreTC::createCinnabarOre);

    public static final DeferredBlock<Block> QUARTZ_ORE = registerBlock("quartz_ore",
            BlockOreTC::createQuartzOre);

    public static final DeferredBlock<Block> DEEPSLATE_AMBER_ORE = registerBlock("deepslate_amber_ore",
            BlockOreTC::createDeepslateAmberOre);

    public static final DeferredBlock<Block> DEEPSLATE_CINNABAR_ORE = registerBlock("deepslate_cinnabar_ore",
            BlockOreTC::createDeepslateCinnabarOre);

    public static final DeferredBlock<Block> DEEPSLATE_QUARTZ_ORE = registerBlock("deepslate_quartz_ore",
            BlockOreTC::createDeepslateQuartzOre);

    public static final DeferredBlock<Block> CRYSTAL_AIR = registerBlock("crystal_air",
            props -> new BlockCrystalTC(props, Aspect.AIR));

    public static final DeferredBlock<Block> CRYSTAL_FIRE = registerBlock("crystal_fire",
            props -> new BlockCrystalTC(props, Aspect.FIRE));

    public static final DeferredBlock<Block> CRYSTAL_WATER = registerBlock("crystal_water",
            props -> new BlockCrystalTC(props, Aspect.WATER));

    public static final DeferredBlock<Block> CRYSTAL_EARTH = registerBlock("crystal_earth",
            props -> new BlockCrystalTC(props, Aspect.EARTH));

    public static final DeferredBlock<Block> CRYSTAL_ORDER = registerBlock("crystal_order",
            props -> new BlockCrystalTC(props, Aspect.ORDER));

    public static final DeferredBlock<Block> CRYSTAL_ENTROPY = registerBlock("crystal_entropy",
            props -> new BlockCrystalTC(props, Aspect.ENTROPY));

    public static final DeferredBlock<Block> CRYSTAL_FLUX = registerBlock("crystal_flux",
            props -> new BlockCrystalTC(props, Aspect.FLUX));

    public static final DeferredBlock<Block> GRASS_AMBIENT = registerBlock("grass_ambient",
            BlockGrassAmbient::new);

    public static final DeferredBlock<Block> SHIMMERLEAF = registerBlock("shimmerleaf",
            BlockShimmerleaf::new);

    public static final DeferredBlock<Block> CINDERPEARL = registerBlock("cinderpearl",
            BlockCinderpearl::new);

    public static final DeferredBlock<Block> VISHROOM = registerBlock("vishroom",
            BlockVishroom::new);

    public static final DeferredBlock<Block> GREATWOOD_LEAVES = registerBlock("greatwood_leaves",
            BlockLeavesTC::createGreatwood);

    public static final DeferredBlock<Block> SILVERWOOD_LEAVES = registerBlock("silverwood_leaves",
            BlockLeavesTC::createSilverwood);

    public static final DeferredBlock<Block> GREATWOOD_SAPLING = registerBlock("greatwood_sapling",
            BlockSaplingTC::createGreatwood);

    public static final DeferredBlock<Block> SILVERWOOD_SAPLING = registerBlock("silverwood_sapling",
            BlockSaplingTC::createSilverwood);

    public static final DeferredBlock<Block> POTTED_GREATWOOD_SAPLING = registerBlockNoItem("potted_greatwood_sapling",
            props -> new net.minecraft.world.level.block.FlowerPotBlock(
                    () -> (net.minecraft.world.level.block.FlowerPotBlock) net.minecraft.world.level.block.Blocks.FLOWER_POT,
                    GREATWOOD_SAPLING, props.instabreak().noOcclusion()));
    public static final DeferredBlock<Block> POTTED_SILVERWOOD_SAPLING = registerBlockNoItem("potted_silverwood_sapling",
            props -> new net.minecraft.world.level.block.FlowerPotBlock(
                    () -> (net.minecraft.world.level.block.FlowerPotBlock) net.minecraft.world.level.block.Blocks.FLOWER_POT,
                    SILVERWOOD_SAPLING, props.instabreak().noOcclusion()));
    public static final DeferredBlock<Block> POTTED_SHIMMERLEAF = registerBlockNoItem("potted_shimmerleaf",
            props -> new net.minecraft.world.level.block.FlowerPotBlock(
                    () -> (net.minecraft.world.level.block.FlowerPotBlock) net.minecraft.world.level.block.Blocks.FLOWER_POT,
                    SHIMMERLEAF, props.instabreak().noOcclusion()));
    public static final DeferredBlock<Block> POTTED_CINDERPEARL = registerBlockNoItem("potted_cinderpearl",
            props -> new net.minecraft.world.level.block.FlowerPotBlock(
                    () -> (net.minecraft.world.level.block.FlowerPotBlock) net.minecraft.world.level.block.Blocks.FLOWER_POT,
                    CINDERPEARL, props.instabreak().noOcclusion()));
    public static final DeferredBlock<Block> POTTED_VISHROOM = registerBlockNoItem("potted_vishroom",
            props -> new net.minecraft.world.level.block.FlowerPotBlock(
                    () -> (net.minecraft.world.level.block.FlowerPotBlock) net.minecraft.world.level.block.Blocks.FLOWER_POT,
                    VISHROOM, props.instabreak().noOcclusion()));

    public static final DeferredBlock<Block> PEDESTAL_ARCANE = registerBlock("pedestal_arcane",
            BlockPedestal::createArcane);

    public static final DeferredBlock<Block> PEDESTAL_ANCIENT = registerBlock("pedestal_ancient",
            BlockPedestal::createAncient);

    public static final DeferredBlock<Block> PEDESTAL_ELDRITCH = registerBlock("pedestal_eldritch",
            BlockPedestal::createEldritch);

    public static final DeferredBlock<Block> TABLE_WOOD = registerBlock("table_wood",
            BlockTable::createWooden);

    public static final DeferredBlock<Block> TABLE_STONE = registerBlock("table_stone",
            BlockTable::createStone);

    public static final DeferredBlock<Block> CANDLE_WHITE = registerBlock("candle_white",
            props -> BlockCandle.create(props, DyeColor.WHITE));
    public static final DeferredBlock<Block> CANDLE_ORANGE = registerBlock("candle_orange",
            props -> BlockCandle.create(props, DyeColor.ORANGE));
    public static final DeferredBlock<Block> CANDLE_MAGENTA = registerBlock("candle_magenta",
            props -> BlockCandle.create(props, DyeColor.MAGENTA));
    public static final DeferredBlock<Block> CANDLE_LIGHT_BLUE = registerBlock("candle_light_blue",
            props -> BlockCandle.create(props, DyeColor.LIGHT_BLUE));
    public static final DeferredBlock<Block> CANDLE_YELLOW = registerBlock("candle_yellow",
            props -> BlockCandle.create(props, DyeColor.YELLOW));
    public static final DeferredBlock<Block> CANDLE_LIME = registerBlock("candle_lime",
            props -> BlockCandle.create(props, DyeColor.LIME));
    public static final DeferredBlock<Block> CANDLE_PINK = registerBlock("candle_pink",
            props -> BlockCandle.create(props, DyeColor.PINK));
    public static final DeferredBlock<Block> CANDLE_GRAY = registerBlock("candle_gray",
            props -> BlockCandle.create(props, DyeColor.GRAY));
    public static final DeferredBlock<Block> CANDLE_LIGHT_GRAY = registerBlock("candle_light_gray",
            props -> BlockCandle.create(props, DyeColor.LIGHT_GRAY));
    public static final DeferredBlock<Block> CANDLE_CYAN = registerBlock("candle_cyan",
            props -> BlockCandle.create(props, DyeColor.CYAN));
    public static final DeferredBlock<Block> CANDLE_PURPLE = registerBlock("candle_purple",
            props -> BlockCandle.create(props, DyeColor.PURPLE));
    public static final DeferredBlock<Block> CANDLE_BLUE = registerBlock("candle_blue",
            props -> BlockCandle.create(props, DyeColor.BLUE));
    public static final DeferredBlock<Block> CANDLE_BROWN = registerBlock("candle_brown",
            props -> BlockCandle.create(props, DyeColor.BROWN));
    public static final DeferredBlock<Block> CANDLE_GREEN = registerBlock("candle_green",
            props -> BlockCandle.create(props, DyeColor.GREEN));
    public static final DeferredBlock<Block> CANDLE_RED = registerBlock("candle_red",
            props -> BlockCandle.create(props, DyeColor.RED));
    public static final DeferredBlock<Block> CANDLE_BLACK = registerBlock("candle_black",
            props -> BlockCandle.create(props, DyeColor.BLACK));

    public static final DeferredBlock<Block> NITOR_WHITE = registerBlock("nitor_white",
            props -> BlockNitor.create(props, DyeColor.WHITE));
    public static final DeferredBlock<Block> NITOR_ORANGE = registerBlock("nitor_orange",
            props -> BlockNitor.create(props, DyeColor.ORANGE));
    public static final DeferredBlock<Block> NITOR_MAGENTA = registerBlock("nitor_magenta",
            props -> BlockNitor.create(props, DyeColor.MAGENTA));
    public static final DeferredBlock<Block> NITOR_LIGHT_BLUE = registerBlock("nitor_light_blue",
            props -> BlockNitor.create(props, DyeColor.LIGHT_BLUE));
    public static final DeferredBlock<Block> NITOR_YELLOW = registerBlock("nitor_yellow",
            props -> BlockNitor.create(props, DyeColor.YELLOW));
    public static final DeferredBlock<Block> NITOR_LIME = registerBlock("nitor_lime",
            props -> BlockNitor.create(props, DyeColor.LIME));
    public static final DeferredBlock<Block> NITOR_PINK = registerBlock("nitor_pink",
            props -> BlockNitor.create(props, DyeColor.PINK));
    public static final DeferredBlock<Block> NITOR_GRAY = registerBlock("nitor_gray",
            props -> BlockNitor.create(props, DyeColor.GRAY));
    public static final DeferredBlock<Block> NITOR_LIGHT_GRAY = registerBlock("nitor_light_gray",
            props -> BlockNitor.create(props, DyeColor.LIGHT_GRAY));
    public static final DeferredBlock<Block> NITOR_CYAN = registerBlock("nitor_cyan",
            props -> BlockNitor.create(props, DyeColor.CYAN));
    public static final DeferredBlock<Block> NITOR_PURPLE = registerBlock("nitor_purple",
            props -> BlockNitor.create(props, DyeColor.PURPLE));
    public static final DeferredBlock<Block> NITOR_BLUE = registerBlock("nitor_blue",
            props -> BlockNitor.create(props, DyeColor.BLUE));
    public static final DeferredBlock<Block> NITOR_BROWN = registerBlock("nitor_brown",
            props -> BlockNitor.create(props, DyeColor.BROWN));
    public static final DeferredBlock<Block> NITOR_GREEN = registerBlock("nitor_green",
            props -> BlockNitor.create(props, DyeColor.GREEN));
    public static final DeferredBlock<Block> NITOR_RED = registerBlock("nitor_red",
            props -> BlockNitor.create(props, DyeColor.RED));
    public static final DeferredBlock<Block> NITOR_BLACK = registerBlock("nitor_black",
            props -> BlockNitor.create(props, DyeColor.BLACK));

    public static final DeferredBlock<Block> JAR_NORMAL = registerBlock("jar_normal",
            BlockJar::createNormal, BlockJarItem::new);

    public static final DeferredBlock<Block> JAR_VOID = registerBlock("jar_void",
            BlockJar::createVoid, BlockJarItem::new);

    public static final DeferredBlock<Block> JAR_BRAIN = registerBlock("jar_brain",
            BlockJar::createBrain, BlockJarBrainItem::new);

    public static final DeferredBlock<Block> TUBE_NORMAL = registerBlock("tube_normal",
            BlockTube::createNormal);

    public static final DeferredBlock<Block> TUBE_RESTRICTED = registerBlock("tube_restricted",
            BlockTube::createRestricted);

    public static final DeferredBlock<Block> TUBE_FILTER = registerBlock("tube_filter",
            BlockTube::createFilter);

    public static final DeferredBlock<Block> TUBE_VALVE = registerBlock("tube_valve",
            BlockTube::createValve);

    public static final DeferredBlock<Block> TUBE_BUFFER = registerBlock("tube_buffer",
            BlockTube::createBuffer);

    public static final DeferredBlock<Block> TUBE_ONEWAY = registerBlock("tube_oneway",
            BlockTube::createOneway);

    public static final DeferredBlock<Block> RESEARCH_TABLE = registerBlock("research_table",
            BlockResearchTable::new);

    public static final DeferredBlock<Block> DECONSTRUCTION_TABLE = registerBlock("deconstruction_table",
            DeconstructBlock::new);

    public static final DeferredBlock<Block> INFUSION_MATRIX = registerBlock("infusion_matrix",
            BlockInfusionMatrix::new);

    public static final DeferredBlock<Block> FOCAL_MANIPULATOR = registerBlock("focal_manipulator",
            BlockFocalManipulator::new);

    public static final DeferredBlock<Block> THAUMATORIUM = registerBlock("thaumatorium",
            BlockThaumatorium::new);

    public static final DeferredBlock<Block> THAUMATORIUM_TOP = registerBlock("thaumatorium_top",
            BlockThaumatoriumTop::new);

    public static final DeferredBlock<Block> PATTERN_CRAFTER = registerBlock("pattern_crafter",
            BlockPatternCrafter::new);

    public static final DeferredBlock<Block> BELLOWS = registerBlock("bellows",
            BlockBellows::new);

    public static final DeferredBlock<Block> LAMP_ARCANE = registerBlock("lamp_arcane",
            BlockLamp::createArcane);

    public static final DeferredBlock<Block> LAMP_GROWTH = registerBlock("lamp_growth",
            BlockLamp::createGrowth);

    public static final DeferredBlock<Block> LAMP_FERTILITY = registerBlock("lamp_fertility",
            BlockLamp::createFertility);

    public static final DeferredBlock<Block> HUNGRY_CHEST = registerBlock("hungry_chest",
            BlockHungryChest::new,
            (block, props) -> new BlockHungryChestItem(block, props));

    public static final DeferredBlock<Block> MIRROR_ITEM = registerBlock("mirror_item",
            BlockMirror::createItem, MirrorBlockItem::new);

    public static final DeferredBlock<Block> MIRROR_ESSENTIA = registerBlock("mirror_essentia",
            BlockMirror::createPlayer, MirrorBlockItem::new);

    public static final DeferredBlock<Block> STABILIZER = registerBlock("stabilizer",
            BlockStabilizer::new);

    public static final DeferredBlock<Block> VIS_GENERATOR = registerBlock("vis_generator",
            BlockVisGenerator::new);

    public static final DeferredBlock<Block> CONDENSER = registerBlock("condenser",
            BlockCondenser::new);

    public static final DeferredBlock<Block> CONDENSER_LATTICE = registerBlock("condenser_lattice",
            BlockCondenserLattice::new);

    public static final DeferredBlock<Block> CONDENSER_LATTICE_DIRTY = registerBlock("condenser_lattice_dirty",
            BlockCondenserLattice.Dirty::new);

    public static final DeferredBlock<Block> ARCANE_EAR = registerBlock("arcane_ear",
            BlockArcaneEar::createPulse);

    public static final DeferredBlock<Block> ARCANE_EAR_TOGGLE = registerBlock("arcane_ear_toggle",
            BlockArcaneEar::createToggle);

    public static final DeferredBlock<Block> REDSTONE_RELAY = registerBlock("redstone_relay",
            BlockRedstoneRelay::new);

    public static final DeferredBlock<Block> ALEMBIC = registerBlock("alembic",
            BlockAlembic::new);

    public static final DeferredBlock<Block> SMELTER = registerBlock("smelter",
            BlockSmelter::createBasic);

    public static final DeferredBlock<Block> SMELTER_AUX = registerBlock("smelter_aux",
            BlockSmelterAux::new);

    public static final DeferredBlock<Block> SMELTER_THAUMIUM = registerBlock("smelter_thaumium",
            BlockSmelter::createThaumium);

    public static final DeferredBlock<Block> SMELTER_VOID = registerBlock("smelter_void",
            BlockSmelter::createVoid);

    public static final DeferredBlock<Block> SMELTER_VENT = registerBlock("smelter_vent",
            BlockSmelterVent::new);

    public static final DeferredBlock<Block> CENTRIFUGE = registerBlock("centrifuge",
            BlockCentrifuge::new);

    public static final DeferredBlock<Block> INFERNAL_FURNACE = registerBlock("infernal_furnace",
            BlockInfernalFurnace::new);

    public static final DeferredBlock<Block> ESSENTIA_RESERVOIR = registerBlock("essentia_reservoir",
            BlockEssentiaReservoir::new);

    public static final DeferredBlock<Block> ESSENTIA_INPUT = registerBlock("essentia_input",
            props -> new BlockEssentiaTransport(props, BlockEssentiaTransport.TransportType.INPUT));

    public static final DeferredBlock<Block> ESSENTIA_OUTPUT = registerBlock("essentia_output",
            props -> new BlockEssentiaTransport(props, BlockEssentiaTransport.TransportType.OUTPUT));

    public static final DeferredBlock<Block> SPA = registerBlock("spa",
            BlockSpa::new);

    public static final DeferredBlock<Block> FLUX_SCRUBBER = registerBlock("flux_scrubber",
            BlockFluxScrubber::new);

    public static final DeferredBlock<Block> GOLEM_BUILDER = registerBlock("golem_builder",
            BlockGolemBuilder::new);

    public static final DeferredBlock<Block> EFFECT_SAP = registerBlockNoItem("effect_sap",
            BlockEffect::createSap);

    public static final DeferredBlock<Block> EFFECT_SHOCK = registerBlockNoItem("effect_shock",
            BlockEffect::createShock);

    public static final DeferredBlock<Block> EFFECT_GLIMMER = registerBlockNoItem("effect_glimmer",
            BlockEffect::createGlimmer);

    public static final DeferredBlock<Block> FLESH_BLOCK = registerBlock("flesh_block",
            BlockFlesh::new);

    public static final DeferredBlock<Block> FLUX_GOO = registerBlockNoItem("flux_goo",
            BlockFluxGoo::new);

    public static final DeferredBlock<Block> FLUX_GAS = registerBlockNoItem("flux_gas",
            props -> new arcaneresearch.common.blocks.misc.BlockFluxGas(props, ModFluids.FLUX_GAS));

    public static final DeferredBlock<Block> TAINT_FIBRE = registerBlock("taint_fibre",
            BlockTaintFibre::new);

    public static final DeferredBlock<Block> TAINT_SOIL = registerBlock("taint_soil",
            props -> new BlockTaint(props, BlockTaint.TaintType.SOIL));

    public static final DeferredBlock<Block> TAINT_ROCK = registerBlock("taint_rock",
            props -> new BlockTaint(props, BlockTaint.TaintType.ROCK));

    public static final DeferredBlock<Block> TAINT_CRUST = registerBlock("taint_crust",
            props -> new BlockTaint(props, BlockTaint.TaintType.CRUST));

    public static final DeferredBlock<Block> TAINT_GEYSER = registerBlock("taint_geyser",
            props -> new BlockTaint(props, BlockTaint.TaintType.GEYSER));

    public static final DeferredBlock<Block> TAINT_LOG = registerBlock("taint_log",
            BlockTaintLog::new);

    public static final DeferredBlock<Block> TAINT_FEATURE = registerBlock("taint_feature",
            BlockTaintFeature::new);

    public static final DeferredBlock<Block> LIQUID_DEATH = registerBlockNoItem("liquid_death",
            props -> new BlockFluidDeath(props, ModFluids.LIQUID_DEATH));

    public static final DeferredBlock<Block> PURIFYING_FLUID = registerBlockNoItem("purifying_fluid",
            props -> new BlockFluidPure(props, ModFluids.PURIFYING_FLUID));

    public static final DeferredBlock<Block> LEVITATOR = registerBlock("levitator",
            BlockLevitator::new);

    public static final DeferredBlock<Block> RECHARGE_PEDESTAL = registerBlock("recharge_pedestal",
            BlockRechargePedestal::new);

    public static final DeferredBlock<Block> DIOPTRA = registerBlock("dioptra",
            BlockDioptra::new);

    public static final DeferredBlock<Block> VOID_SIPHON = registerBlock("void_siphon",
            BlockVoidSiphon::new);

    public static final DeferredBlock<Block> POTION_SPRAYER = registerBlock("potion_sprayer",
            BlockPotionSprayer::new);

    public static final DeferredBlock<Block> ACTIVATOR_RAIL = registerBlock("activator_rail",
            arcaneresearch.common.blocks.misc.BlockActivatorRailTC::new);

    public static final DeferredBlock<Block> VIS_BATTERY = registerBlock("vis_battery",
            BlockVisBattery::create);

    public static final DeferredBlock<Block> BRAIN_BOX = registerBlock("brain_box",
            BlockBrainBox::create);

    public static final DeferredBlock<Block> INLAY = registerBlock("inlay",
            BlockInlay::create);

    public static final DeferredBlock<Block> LOOT_CRATE_COMMON = registerBlock("loot_crate_common",
            BlockLoot::createCrateCommon);

    public static final DeferredBlock<Block> LOOT_CRATE_UNCOMMON = registerBlock("loot_crate_uncommon",
            BlockLoot::createCrateUncommon);

    public static final DeferredBlock<Block> LOOT_CRATE_RARE = registerBlock("loot_crate_rare",
            BlockLoot::createCrateRare);

    public static final DeferredBlock<Block> LOOT_URN_COMMON = registerBlock("loot_urn_common",
            BlockLoot::createUrnCommon);

    public static final DeferredBlock<Block> LOOT_URN_UNCOMMON = registerBlock("loot_urn_uncommon",
            BlockLoot::createUrnUncommon);

    public static final DeferredBlock<Block> LOOT_URN_RARE = registerBlock("loot_urn_rare",
            BlockLoot::createUrnRare);

    public static final DeferredBlock<Block> HOLE = registerBlockNoItem("hole",
            BlockHole::new);

    public static final DeferredBlock<Block> PLACEHOLDER_NETHERBRICK = registerBlock("placeholder_brick",
            props -> new BlockPlaceholder(props, BlockPlaceholder.PlaceholderType.NETHERBRICK));

    public static final DeferredBlock<Block> PLACEHOLDER_OBSIDIAN = registerBlock("placeholder_obsidian",
            props -> new BlockPlaceholder(props, BlockPlaceholder.PlaceholderType.OBSIDIAN));

    public static final DeferredBlock<Block> PLACEHOLDER_BARS = registerBlock("placeholder_bars",
            props -> new BlockPlaceholder(props, BlockPlaceholder.PlaceholderType.BARS));

    public static final DeferredBlock<Block> PLACEHOLDER_ANVIL = registerBlock("placeholder_anvil",
            props -> new BlockPlaceholder(props, BlockPlaceholder.PlaceholderType.ANVIL));

    public static final DeferredBlock<Block> PLACEHOLDER_CAULDRON = registerBlock("placeholder_cauldron",
            props -> new BlockPlaceholder(props, BlockPlaceholder.PlaceholderType.CAULDRON));

    public static final DeferredBlock<Block> PLACEHOLDER_TABLE = registerBlock("placeholder_table",
            props -> new BlockPlaceholder(props, BlockPlaceholder.PlaceholderType.TABLE));

    public static final DeferredBlock<Block> EVERFULL_URN = registerBlock("everfull_urn",
            BlockWaterJug::new);

    public static final DeferredBlock<Block> PAVING_STONE = registerBlock("paving_stone",
            BlockPavingStone::createNormal);

    public static final DeferredBlock<Block> PAVING_STONE_TRAVEL = registerBlock("paving_stone_travel",
            BlockPavingStone::createTravel);

    public static final DeferredBlock<Block> PAVING_STONE_BARRIER = registerBlock("paving_stone_barrier",
            BlockPavingStone::createBarrier);

    public static final DeferredBlock<Block> BARRIER = registerBlockNoItem("barrier",
            BlockBarrier::new);

    public static final DeferredBlock<Block> BANNER_WHITE = registerBlock("banner_white",
            BlockBannerTC::createWhite, BannerBlockItem::new);
    public static final DeferredBlock<Block> BANNER_ORANGE = registerBlock("banner_orange",
            BlockBannerTC::createOrange, BannerBlockItem::new);
    public static final DeferredBlock<Block> BANNER_MAGENTA = registerBlock("banner_magenta",
            BlockBannerTC::createMagenta, BannerBlockItem::new);
    public static final DeferredBlock<Block> BANNER_LIGHT_BLUE = registerBlock("banner_light_blue",
            BlockBannerTC::createLightBlue, BannerBlockItem::new);
    public static final DeferredBlock<Block> BANNER_YELLOW = registerBlock("banner_yellow",
            BlockBannerTC::createYellow, BannerBlockItem::new);
    public static final DeferredBlock<Block> BANNER_LIME = registerBlock("banner_lime",
            BlockBannerTC::createLime, BannerBlockItem::new);
    public static final DeferredBlock<Block> BANNER_PINK = registerBlock("banner_pink",
            BlockBannerTC::createPink, BannerBlockItem::new);
    public static final DeferredBlock<Block> BANNER_GRAY = registerBlock("banner_gray",
            BlockBannerTC::createGray, BannerBlockItem::new);
    public static final DeferredBlock<Block> BANNER_LIGHT_GRAY = registerBlock("banner_light_gray",
            BlockBannerTC::createLightGray, BannerBlockItem::new);
    public static final DeferredBlock<Block> BANNER_CYAN = registerBlock("banner_cyan",
            BlockBannerTC::createCyan, BannerBlockItem::new);
    public static final DeferredBlock<Block> BANNER_PURPLE = registerBlock("banner_purple",
            BlockBannerTC::createPurple, BannerBlockItem::new);
    public static final DeferredBlock<Block> BANNER_BLUE = registerBlock("banner_blue",
            BlockBannerTC::createBlue, BannerBlockItem::new);
    public static final DeferredBlock<Block> BANNER_BROWN = registerBlock("banner_brown",
            BlockBannerTC::createBrown, BannerBlockItem::new);
    public static final DeferredBlock<Block> BANNER_GREEN = registerBlock("banner_green",
            BlockBannerTC::createGreen, BannerBlockItem::new);
    public static final DeferredBlock<Block> BANNER_RED = registerBlock("banner_red",
            BlockBannerTC::createRed, BannerBlockItem::new);
    public static final DeferredBlock<Block> BANNER_BLACK = registerBlock("banner_black",
            BlockBannerTC::createBlack, BannerBlockItem::new);
    public static final DeferredBlock<Block> BANNER_CRIMSON_CULT = registerBlock("banner_crimson_cult",
            BlockBannerTC::createCrimsonCult, BannerBlockItem::new);

    private static <T extends Block> DeferredBlock<T> registerBlock(String name,
            java.util.function.Function<BlockBehaviour.Properties, T> block) {
        DeferredBlock<T> blockObj = BLOCKS.registerBlock(name, block);
        registerBlockItem(name, blockObj);
        return blockObj;
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name,
            java.util.function.Function<BlockBehaviour.Properties, T> block,
            java.util.function.BiFunction<T, net.minecraft.world.item.Item.Properties, BlockItem> itemFactory) {
        DeferredBlock<T> blockObj = BLOCKS.registerBlock(name, block);
        BLOCK_ITEMS.registerItem(name, props -> itemFactory.apply(blockObj.get(), props));
        return blockObj;
    }

    private static <T extends Block> DeferredBlock<T> registerBlockNoItem(String name,
            java.util.function.Function<BlockBehaviour.Properties, T> block) {
        return BLOCKS.registerBlock(name, block);
    }

    /**
     * Привязка саженцев и цветов к ванильному горшку: ПКМ растением
     * по пустому горшку ставит соответствующий potted-вариант.
     * Вызывать в commonSetup (карта горшка статическая, обе стороны).
     */
    public static void addFlowerPotPlants() {
        // NEOFORGE_PORT (26.x): FlowerPotBlock.addPlant(ResourceLocation, ...) was removed.
        // The potted-plant mapping is now registered by the NeoForge FlowerPotBlock
        // constructor (emptyPot + plant suppliers), which the POTTED_* registrations
        // above already use. Nothing left to do here.
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        BLOCK_ITEMS.registerSimpleBlockItem(block);
    }
}
