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
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
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
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Arcaneresearch.MODID);
    public static final DeferredRegister<Item> BLOCK_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Arcaneresearch.MODID);
    public static final RegistryObject<Block> ARCANE_STONE = registerBlock("arcane_stone",
            () -> BlockStoneTC.create(true));
    public static final RegistryObject<Block> ARCANE_STONE_BRICK = registerBlock("arcane_stone_brick",
            () -> BlockStoneTC.create(true));
    public static final RegistryObject<Block> ANCIENT_STONE = registerBlock("ancient_stone",
            () -> BlockStoneTC.create(true));
    public static final RegistryObject<Block> ANCIENT_STONE_TILE = registerBlock("ancient_stone_tile",
            () -> BlockStoneTC.create(false));
    public static final RegistryObject<Block> ANCIENT_STONE_ROCK = registerBlock("ancient_rock",
            () -> BlockStoneTC.createUnbreakable());
    public static final RegistryObject<Block> ANCIENT_STONE_GLYPHED = registerBlock("ancient_stone_glyphed",
            () -> BlockStoneTC.create(false));
    public static final RegistryObject<Block> OBSIDIAN_TILE = registerBlock("obsidian_tile",
            () -> new Block(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                    .requiresCorrectToolForDrops()
                    .strength(50.0F, 1200.0F)));
    public static final RegistryObject<Block> ANCIENT_STONE_DOORWAY = registerBlock("ancient_pedestal_doorway",
            () -> BlockStoneTC.createUnbreakable());
    public static final RegistryObject<Block> ELDRITCH_STONE_TILE = registerBlock("eldritch_stone_tile",
            () -> BlockStoneTC.createReinforced());
    public static final RegistryObject<Block> ELDRITCH_STONE = registerBlock("eldritch_stone",
            () -> BlockStoneTC.create(true));
    public static final RegistryObject<Block> POROUS_STONE = registerBlock("porous_stone",
            () -> new BlockStonePorous(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(1.0f, 5.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> ARCANE_STONE_STAIRS = registerBlock("arcane_stone_stairs",
            () -> new StairBlock(() -> ARCANE_STONE.get().defaultBlockState(),
                    BlockBehaviour.Properties.copy(ARCANE_STONE.get())));
    public static final RegistryObject<Block> ARCANE_STONE_BRICK_STAIRS = registerBlock("arcane_stone_brick_stairs",
            () -> new StairBlock(() -> ARCANE_STONE_BRICK.get().defaultBlockState(),
                    BlockBehaviour.Properties.copy(ARCANE_STONE_BRICK.get())));
    public static final RegistryObject<Block> ANCIENT_STONE_STAIRS = registerBlock("ancient_stone_stairs",
            () -> new StairBlock(() -> ANCIENT_STONE.get().defaultBlockState(),
                    BlockBehaviour.Properties.copy(ANCIENT_STONE.get())));
    public static final RegistryObject<Block> ARCANE_STONE_SLAB = registerBlock("arcane_stone_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0f, 10.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> ARCANE_STONE_BRICK_SLAB = registerBlock("arcane_stone_brick_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0f, 10.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> ANCIENT_STONE_SLAB = registerBlock("ancient_stone_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0f, 10.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> ELDRITCH_STONE_SLAB = registerBlock("eldritch_stone_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0f, 10.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> ARCANE_PILLAR = registerBlock("arcane_pillar",
            BlockPillarTC::create);
    public static final RegistryObject<Block> ANCIENT_PILLAR = registerBlock("ancient_pillar",
            BlockPillarTC::create);
    public static final RegistryObject<Block> ELDRITCH_PILLAR = registerBlock("eldritch_pillar",
            () -> new BlockPillarTC(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(15.0f, 1000.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> GREATWOOD_LOG = registerBlock("greatwood_log",
            BlockLogTC::createGreatwood);
    public static final RegistryObject<Block> SILVERWOOD_LOG = registerBlock("silverwood_log",
            BlockLogTC::createSilverwood);
    public static final RegistryObject<Block> GREATWOOD_PLANKS = registerBlock("greatwood_planks",
            BlockPlanksTC::createGreatwood);
    public static final RegistryObject<Block> SILVERWOOD_PLANKS = registerBlock("silverwood_planks",
            BlockPlanksTC::createSilverwood);
    public static final RegistryObject<Block> GREATWOOD_STAIRS = registerBlock("greatwood_stairs",
            () -> new BlockStairsTC(() -> GREATWOOD_PLANKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.copy(GREATWOOD_PLANKS.get()), true));
    public static final RegistryObject<Block> SILVERWOOD_STAIRS = registerBlock("silverwood_stairs",
            () -> new BlockStairsTC(() -> SILVERWOOD_PLANKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.copy(SILVERWOOD_PLANKS.get()), true));
    public static final RegistryObject<Block> GREATWOOD_SLAB = registerBlock("greatwood_slab",
            () -> new BlockSlabTC(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(1.2f, 2.0f)
                    .sound(SoundType.WOOD), true));
    public static final RegistryObject<Block> SILVERWOOD_SLAB = registerBlock("silverwood_slab",
            () -> new BlockSlabTC(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .strength(1.0f, 2.0f)
                    .sound(SoundType.WOOD), true));
    public static final RegistryObject<Block> BRASS_BLOCK = registerBlock("brass_block",
            () -> BlockMetalTC.create(MapColor.GOLD));
    public static final RegistryObject<Block> THAUMIUM_BLOCK = registerBlock("thaumium_block",
            () -> BlockMetalTC.create(MapColor.COLOR_PURPLE));
    public static final RegistryObject<Block> VOID_METAL_BLOCK = registerBlock("void_metal_block",
            () -> BlockMetalTC.create(MapColor.COLOR_BLACK));
    public static final RegistryObject<Block> ALCHEMICAL_BRASS_BLOCK = registerBlock("alchemical_brass_block",
            () -> BlockMetalTC.create(MapColor.GOLD));
    public static final RegistryObject<Block> ALCHEMICAL_BRASS_ADVANCED_BLOCK = registerBlock("alchemical_brass_advanced_block",
            () -> BlockMetalTC.create(MapColor.GOLD));
    public static final RegistryObject<Block> AMBER_BLOCK = registerBlock("amber_block",
            () -> new BlockTranslucent(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(0.5f, 2.5f)
                    .sound(SoundType.STONE)
                    .noOcclusion()));
    public static final RegistryObject<Block> AMBER_BRICK = registerBlock("amber_brick",
            () -> new BlockTranslucent(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(0.5f, 2.5f)
                    .sound(SoundType.STONE)
                    .noOcclusion()));
    public static final RegistryObject<Block> MATRIX_SPEED = registerBlock("matrix_speed",
            () -> BlockStoneTC.create(false));
    public static final RegistryObject<Block> MATRIX_COST = registerBlock("matrix_cost",
            () -> BlockStoneTC.create(false));
    public static final RegistryObject<Block> ARCANE_WORKBENCH = registerBlock("arcane_workbench",
            BlockArcaneWorkbench::new);
    public static final RegistryObject<Block> ARCANE_WORKBENCH_CHARGER = registerBlock("arcane_workbench_charger",
            BlockArcaneWorkbenchCharger::create);
    public static final RegistryObject<Block> CRUCIBLE = registerBlock("crucible",
            BlockCrucible::new);
    public static final RegistryObject<Block> AMBER_ORE = registerBlock("amber_ore",
            BlockOreTC::createAmberOre);
    public static final RegistryObject<Block> CINNABAR_ORE = registerBlock("cinnabar_ore",
            BlockOreTC::createCinnabarOre);
    public static final RegistryObject<Block> QUARTZ_ORE = registerBlock("quartz_ore",
            BlockOreTC::createQuartzOre);
    public static final RegistryObject<Block> DEEPSLATE_AMBER_ORE = registerBlock("deepslate_amber_ore",
            BlockOreTC::createDeepslateAmberOre);
    public static final RegistryObject<Block> DEEPSLATE_CINNABAR_ORE = registerBlock("deepslate_cinnabar_ore",
            BlockOreTC::createDeepslateCinnabarOre);
    public static final RegistryObject<Block> DEEPSLATE_QUARTZ_ORE = registerBlock("deepslate_quartz_ore",
            BlockOreTC::createDeepslateQuartzOre);
    public static final RegistryObject<Block> CRYSTAL_AIR = registerBlock("crystal_air",
            () -> new BlockCrystalTC(Aspect.AIR));
    public static final RegistryObject<Block> CRYSTAL_FIRE = registerBlock("crystal_fire",
            () -> new BlockCrystalTC(Aspect.FIRE));
    public static final RegistryObject<Block> CRYSTAL_WATER = registerBlock("crystal_water",
            () -> new BlockCrystalTC(Aspect.WATER));
    public static final RegistryObject<Block> CRYSTAL_EARTH = registerBlock("crystal_earth",
            () -> new BlockCrystalTC(Aspect.EARTH));
    public static final RegistryObject<Block> CRYSTAL_ORDER = registerBlock("crystal_order",
            () -> new BlockCrystalTC(Aspect.ORDER));
    public static final RegistryObject<Block> CRYSTAL_ENTROPY = registerBlock("crystal_entropy",
            () -> new BlockCrystalTC(Aspect.ENTROPY));
    public static final RegistryObject<Block> CRYSTAL_FLUX = registerBlock("crystal_flux",
            () -> new BlockCrystalTC(Aspect.FLUX));
    public static final RegistryObject<Block> GRASS_AMBIENT = registerBlock("grass_ambient",
            BlockGrassAmbient::new);
    public static final RegistryObject<Block> SHIMMERLEAF = registerBlock("shimmerleaf",
            BlockShimmerleaf::new);
    public static final RegistryObject<Block> CINDERPEARL = registerBlock("cinderpearl",
            BlockCinderpearl::new);
    public static final RegistryObject<Block> VISHROOM = registerBlock("vishroom",
            BlockVishroom::new);
    public static final RegistryObject<Block> GREATWOOD_LEAVES = registerBlock("greatwood_leaves",
            BlockLeavesTC::createGreatwood);
    public static final RegistryObject<Block> SILVERWOOD_LEAVES = registerBlock("silverwood_leaves",
            BlockLeavesTC::createSilverwood);
    public static final RegistryObject<Block> GREATWOOD_SAPLING = registerBlock("greatwood_sapling",
            BlockSaplingTC::createGreatwood);
    public static final RegistryObject<Block> SILVERWOOD_SAPLING = registerBlock("silverwood_sapling",
            BlockSaplingTC::createSilverwood);
    public static final RegistryObject<Block> POTTED_GREATWOOD_SAPLING = registerBlockNoItem("potted_greatwood_sapling",
            () -> new net.minecraft.world.level.block.FlowerPotBlock(
                    () -> (net.minecraft.world.level.block.FlowerPotBlock) net.minecraft.world.level.block.Blocks.FLOWER_POT,
                    GREATWOOD_SAPLING, BlockBehaviour.Properties.of().instabreak().noOcclusion()));
    public static final RegistryObject<Block> POTTED_SILVERWOOD_SAPLING = registerBlockNoItem("potted_silverwood_sapling",
            () -> new net.minecraft.world.level.block.FlowerPotBlock(
                    () -> (net.minecraft.world.level.block.FlowerPotBlock) net.minecraft.world.level.block.Blocks.FLOWER_POT,
                    SILVERWOOD_SAPLING, BlockBehaviour.Properties.of().instabreak().noOcclusion()));
    public static final RegistryObject<Block> POTTED_SHIMMERLEAF = registerBlockNoItem("potted_shimmerleaf",
            () -> new net.minecraft.world.level.block.FlowerPotBlock(
                    () -> (net.minecraft.world.level.block.FlowerPotBlock) net.minecraft.world.level.block.Blocks.FLOWER_POT,
                    SHIMMERLEAF, BlockBehaviour.Properties.of().instabreak().noOcclusion()));
    public static final RegistryObject<Block> POTTED_CINDERPEARL = registerBlockNoItem("potted_cinderpearl",
            () -> new net.minecraft.world.level.block.FlowerPotBlock(
                    () -> (net.minecraft.world.level.block.FlowerPotBlock) net.minecraft.world.level.block.Blocks.FLOWER_POT,
                    CINDERPEARL, BlockBehaviour.Properties.of().instabreak().noOcclusion()));
    public static final RegistryObject<Block> POTTED_VISHROOM = registerBlockNoItem("potted_vishroom",
            () -> new net.minecraft.world.level.block.FlowerPotBlock(
                    () -> (net.minecraft.world.level.block.FlowerPotBlock) net.minecraft.world.level.block.Blocks.FLOWER_POT,
                    VISHROOM, BlockBehaviour.Properties.of().instabreak().noOcclusion()));
    public static final RegistryObject<Block> PEDESTAL_ARCANE = registerBlock("pedestal_arcane",
            BlockPedestal::createArcane);
    public static final RegistryObject<Block> PEDESTAL_ANCIENT = registerBlock("pedestal_ancient",
            BlockPedestal::createAncient);
    public static final RegistryObject<Block> PEDESTAL_ELDRITCH = registerBlock("pedestal_eldritch",
            BlockPedestal::createEldritch);
    public static final RegistryObject<Block> TABLE_WOOD = registerBlock("table_wood",
            BlockTable::createWooden);
    public static final RegistryObject<Block> TABLE_STONE = registerBlock("table_stone",
            BlockTable::createStone);
    public static final RegistryObject<Block> CANDLE_WHITE = registerBlock("candle_white",
            () -> BlockCandle.create(DyeColor.WHITE));
    public static final RegistryObject<Block> CANDLE_ORANGE = registerBlock("candle_orange",
            () -> BlockCandle.create(DyeColor.ORANGE));
    public static final RegistryObject<Block> CANDLE_MAGENTA = registerBlock("candle_magenta",
            () -> BlockCandle.create(DyeColor.MAGENTA));
    public static final RegistryObject<Block> CANDLE_LIGHT_BLUE = registerBlock("candle_light_blue",
            () -> BlockCandle.create(DyeColor.LIGHT_BLUE));
    public static final RegistryObject<Block> CANDLE_YELLOW = registerBlock("candle_yellow",
            () -> BlockCandle.create(DyeColor.YELLOW));
    public static final RegistryObject<Block> CANDLE_LIME = registerBlock("candle_lime",
            () -> BlockCandle.create(DyeColor.LIME));
    public static final RegistryObject<Block> CANDLE_PINK = registerBlock("candle_pink",
            () -> BlockCandle.create(DyeColor.PINK));
    public static final RegistryObject<Block> CANDLE_GRAY = registerBlock("candle_gray",
            () -> BlockCandle.create(DyeColor.GRAY));
    public static final RegistryObject<Block> CANDLE_LIGHT_GRAY = registerBlock("candle_light_gray",
            () -> BlockCandle.create(DyeColor.LIGHT_GRAY));
    public static final RegistryObject<Block> CANDLE_CYAN = registerBlock("candle_cyan",
            () -> BlockCandle.create(DyeColor.CYAN));
    public static final RegistryObject<Block> CANDLE_PURPLE = registerBlock("candle_purple",
            () -> BlockCandle.create(DyeColor.PURPLE));
    public static final RegistryObject<Block> CANDLE_BLUE = registerBlock("candle_blue",
            () -> BlockCandle.create(DyeColor.BLUE));
    public static final RegistryObject<Block> CANDLE_BROWN = registerBlock("candle_brown",
            () -> BlockCandle.create(DyeColor.BROWN));
    public static final RegistryObject<Block> CANDLE_GREEN = registerBlock("candle_green",
            () -> BlockCandle.create(DyeColor.GREEN));
    public static final RegistryObject<Block> CANDLE_RED = registerBlock("candle_red",
            () -> BlockCandle.create(DyeColor.RED));
    public static final RegistryObject<Block> CANDLE_BLACK = registerBlock("candle_black",
            () -> BlockCandle.create(DyeColor.BLACK));
    public static final RegistryObject<Block> NITOR_WHITE = registerBlock("nitor_white",
            () -> BlockNitor.create(DyeColor.WHITE));
    public static final RegistryObject<Block> NITOR_ORANGE = registerBlock("nitor_orange",
            () -> BlockNitor.create(DyeColor.ORANGE));
    public static final RegistryObject<Block> NITOR_MAGENTA = registerBlock("nitor_magenta",
            () -> BlockNitor.create(DyeColor.MAGENTA));
    public static final RegistryObject<Block> NITOR_LIGHT_BLUE = registerBlock("nitor_light_blue",
            () -> BlockNitor.create(DyeColor.LIGHT_BLUE));
    public static final RegistryObject<Block> NITOR_YELLOW = registerBlock("nitor_yellow",
            () -> BlockNitor.create(DyeColor.YELLOW));
    public static final RegistryObject<Block> NITOR_LIME = registerBlock("nitor_lime",
            () -> BlockNitor.create(DyeColor.LIME));
    public static final RegistryObject<Block> NITOR_PINK = registerBlock("nitor_pink",
            () -> BlockNitor.create(DyeColor.PINK));
    public static final RegistryObject<Block> NITOR_GRAY = registerBlock("nitor_gray",
            () -> BlockNitor.create(DyeColor.GRAY));
    public static final RegistryObject<Block> NITOR_LIGHT_GRAY = registerBlock("nitor_light_gray",
            () -> BlockNitor.create(DyeColor.LIGHT_GRAY));
    public static final RegistryObject<Block> NITOR_CYAN = registerBlock("nitor_cyan",
            () -> BlockNitor.create(DyeColor.CYAN));
    public static final RegistryObject<Block> NITOR_PURPLE = registerBlock("nitor_purple",
            () -> BlockNitor.create(DyeColor.PURPLE));
    public static final RegistryObject<Block> NITOR_BLUE = registerBlock("nitor_blue",
            () -> BlockNitor.create(DyeColor.BLUE));
    public static final RegistryObject<Block> NITOR_BROWN = registerBlock("nitor_brown",
            () -> BlockNitor.create(DyeColor.BROWN));
    public static final RegistryObject<Block> NITOR_GREEN = registerBlock("nitor_green",
            () -> BlockNitor.create(DyeColor.GREEN));
    public static final RegistryObject<Block> NITOR_RED = registerBlock("nitor_red",
            () -> BlockNitor.create(DyeColor.RED));
    public static final RegistryObject<Block> NITOR_BLACK = registerBlock("nitor_black",
            () -> BlockNitor.create(DyeColor.BLACK));
    public static final RegistryObject<Block> JAR_NORMAL = registerBlock("jar_normal",
            BlockJar::createNormal, BlockJarItem::new);
    public static final RegistryObject<Block> JAR_VOID = registerBlock("jar_void",
            BlockJar::createVoid, BlockJarItem::new);
    public static final RegistryObject<Block> JAR_BRAIN = registerBlock("jar_brain",
            BlockJar::createBrain, BlockJarBrainItem::new);
    public static final RegistryObject<Block> TUBE_NORMAL = registerBlock("tube_normal",
            BlockTube::createNormal);
    public static final RegistryObject<Block> TUBE_RESTRICTED = registerBlock("tube_restricted",
            BlockTube::createRestricted);
    public static final RegistryObject<Block> TUBE_FILTER = registerBlock("tube_filter",
            BlockTube::createFilter);
    public static final RegistryObject<Block> TUBE_VALVE = registerBlock("tube_valve",
            BlockTube::createValve);
    public static final RegistryObject<Block> TUBE_BUFFER = registerBlock("tube_buffer",
            BlockTube::createBuffer);
    public static final RegistryObject<Block> TUBE_ONEWAY = registerBlock("tube_oneway",
            BlockTube::createOneway);
    public static final RegistryObject<Block> RESEARCH_TABLE = registerBlock("research_table",
            BlockResearchTable::new);
    public static final RegistryObject<Block> DECONSTRUCTION_TABLE = registerBlock("deconstruction_table",
            DeconstructBlock::new);
    public static final RegistryObject<Block> INFUSION_MATRIX = registerBlock("infusion_matrix",
            BlockInfusionMatrix::new);
    public static final RegistryObject<Block> FOCAL_MANIPULATOR = registerBlock("focal_manipulator",
            BlockFocalManipulator::new);
    public static final RegistryObject<Block> THAUMATORIUM = registerBlock("thaumatorium",
            BlockThaumatorium::new);
    public static final RegistryObject<Block> THAUMATORIUM_TOP = registerBlock("thaumatorium_top",
            BlockThaumatoriumTop::new);
    public static final RegistryObject<Block> PATTERN_CRAFTER = registerBlock("pattern_crafter",
            BlockPatternCrafter::new);
    public static final RegistryObject<Block> BELLOWS = registerBlock("bellows",
            BlockBellows::new);
    public static final RegistryObject<Block> LAMP_ARCANE = registerBlock("lamp_arcane",
            BlockLamp::createArcane);
    public static final RegistryObject<Block> LAMP_GROWTH = registerBlock("lamp_growth",
            BlockLamp::createGrowth);
    public static final RegistryObject<Block> LAMP_FERTILITY = registerBlock("lamp_fertility",
            BlockLamp::createFertility);
    public static final RegistryObject<Block> HUNGRY_CHEST = registerBlock("hungry_chest",
            BlockHungryChest::new,
            block -> new BlockHungryChestItem(block, new net.minecraft.world.item.Item.Properties()));
    public static final RegistryObject<Block> MIRROR_ITEM = registerBlock("mirror_item",
            BlockMirror::createItem, MirrorBlockItem::new);
    public static final RegistryObject<Block> MIRROR_ESSENTIA = registerBlock("mirror_essentia",
            BlockMirror::createPlayer, MirrorBlockItem::new);
    public static final RegistryObject<Block> STABILIZER = registerBlock("stabilizer",
            BlockStabilizer::new);
    public static final RegistryObject<Block> VIS_GENERATOR = registerBlock("vis_generator",
            BlockVisGenerator::new);
    public static final RegistryObject<Block> CONDENSER = registerBlock("condenser",
            BlockCondenser::new);
    public static final RegistryObject<Block> CONDENSER_LATTICE = registerBlock("condenser_lattice",
            BlockCondenserLattice::new);
    public static final RegistryObject<Block> CONDENSER_LATTICE_DIRTY = registerBlock("condenser_lattice_dirty",
            BlockCondenserLattice.Dirty::new);
    public static final RegistryObject<Block> ARCANE_EAR = registerBlock("arcane_ear",
            BlockArcaneEar::createPulse);
    public static final RegistryObject<Block> ARCANE_EAR_TOGGLE = registerBlock("arcane_ear_toggle",
            BlockArcaneEar::createToggle);
    public static final RegistryObject<Block> REDSTONE_RELAY = registerBlock("redstone_relay",
            BlockRedstoneRelay::new);
    public static final RegistryObject<Block> ALEMBIC = registerBlock("alembic",
            BlockAlembic::new);
    public static final RegistryObject<Block> SMELTER = registerBlock("smelter",
            BlockSmelter::createBasic);
    public static final RegistryObject<Block> SMELTER_AUX = registerBlock("smelter_aux",
            BlockSmelterAux::new);
    public static final RegistryObject<Block> SMELTER_THAUMIUM = registerBlock("smelter_thaumium",
            BlockSmelter::createThaumium);
    public static final RegistryObject<Block> SMELTER_VOID = registerBlock("smelter_void",
            BlockSmelter::createVoid);
    public static final RegistryObject<Block> SMELTER_VENT = registerBlock("smelter_vent",
            BlockSmelterVent::new);
    public static final RegistryObject<Block> CENTRIFUGE = registerBlock("centrifuge",
            BlockCentrifuge::new);
    public static final RegistryObject<Block> INFERNAL_FURNACE = registerBlock("infernal_furnace",
            BlockInfernalFurnace::new);
    public static final RegistryObject<Block> ESSENTIA_RESERVOIR = registerBlock("essentia_reservoir",
            BlockEssentiaReservoir::new);
    public static final RegistryObject<Block> ESSENTIA_INPUT = registerBlock("essentia_input",
            () -> new BlockEssentiaTransport(BlockEssentiaTransport.TransportType.INPUT));
    public static final RegistryObject<Block> ESSENTIA_OUTPUT = registerBlock("essentia_output",
            () -> new BlockEssentiaTransport(BlockEssentiaTransport.TransportType.OUTPUT));
    public static final RegistryObject<Block> SPA = registerBlock("spa",
            BlockSpa::new);
    public static final RegistryObject<Block> FLUX_SCRUBBER = registerBlock("flux_scrubber",
            BlockFluxScrubber::new);
    public static final RegistryObject<Block> GOLEM_BUILDER = registerBlock("golem_builder",
            BlockGolemBuilder::new);
    public static final RegistryObject<Block> EFFECT_SAP = registerBlockNoItem("effect_sap",
            BlockEffect::createSap);
    public static final RegistryObject<Block> EFFECT_SHOCK = registerBlockNoItem("effect_shock",
            BlockEffect::createShock);
    public static final RegistryObject<Block> EFFECT_GLIMMER = registerBlockNoItem("effect_glimmer",
            BlockEffect::createGlimmer);
    public static final RegistryObject<Block> FLESH_BLOCK = registerBlock("flesh_block",
            BlockFlesh::new);
    public static final RegistryObject<Block> FLUX_GOO = registerBlockNoItem("flux_goo",
            BlockFluxGoo::new);
    public static final RegistryObject<Block> FLUX_GAS = registerBlockNoItem("flux_gas",
            () -> new arcaneresearch.common.blocks.misc.BlockFluxGas(ModFluids.FLUX_GAS));
    public static final RegistryObject<Block> TAINT_FIBRE = registerBlock("taint_fibre",
            BlockTaintFibre::new);
    public static final RegistryObject<Block> TAINT_SOIL = registerBlock("taint_soil",
            () -> new BlockTaint(BlockTaint.TaintType.SOIL));
    public static final RegistryObject<Block> TAINT_ROCK = registerBlock("taint_rock",
            () -> new BlockTaint(BlockTaint.TaintType.ROCK));
    public static final RegistryObject<Block> TAINT_CRUST = registerBlock("taint_crust",
            () -> new BlockTaint(BlockTaint.TaintType.CRUST));
    public static final RegistryObject<Block> TAINT_GEYSER = registerBlock("taint_geyser",
            () -> new BlockTaint(BlockTaint.TaintType.GEYSER));
    public static final RegistryObject<Block> TAINT_LOG = registerBlock("taint_log",
            BlockTaintLog::new);
    public static final RegistryObject<Block> TAINT_FEATURE = registerBlock("taint_feature",
            BlockTaintFeature::new);
    public static final RegistryObject<Block> LIQUID_DEATH = registerBlockNoItem("liquid_death",
            () -> new BlockFluidDeath(ModFluids.LIQUID_DEATH));
    public static final RegistryObject<Block> PURIFYING_FLUID = registerBlockNoItem("purifying_fluid",
            () -> new BlockFluidPure(ModFluids.PURIFYING_FLUID));
    public static final RegistryObject<Block> LEVITATOR = registerBlock("levitator",
            BlockLevitator::new);
    public static final RegistryObject<Block> RECHARGE_PEDESTAL = registerBlock("recharge_pedestal",
            BlockRechargePedestal::new);
    public static final RegistryObject<Block> DIOPTRA = registerBlock("dioptra",
            BlockDioptra::new);
    public static final RegistryObject<Block> VOID_SIPHON = registerBlock("void_siphon",
            BlockVoidSiphon::new);
    public static final RegistryObject<Block> POTION_SPRAYER = registerBlock("potion_sprayer",
            BlockPotionSprayer::new);
    public static final RegistryObject<Block> ACTIVATOR_RAIL = registerBlock("activator_rail",
            arcaneresearch.common.blocks.misc.BlockActivatorRailTC::new);
    public static final RegistryObject<Block> VIS_BATTERY = registerBlock("vis_battery",
            BlockVisBattery::create);
    public static final RegistryObject<Block> BRAIN_BOX = registerBlock("brain_box",
            BlockBrainBox::create);
    public static final RegistryObject<Block> INLAY = registerBlock("inlay",
            BlockInlay::create);
    public static final RegistryObject<Block> LOOT_CRATE_COMMON = registerBlock("loot_crate_common",
            BlockLoot::createCrateCommon);
    public static final RegistryObject<Block> LOOT_CRATE_UNCOMMON = registerBlock("loot_crate_uncommon",
            BlockLoot::createCrateUncommon);
    public static final RegistryObject<Block> LOOT_CRATE_RARE = registerBlock("loot_crate_rare",
            BlockLoot::createCrateRare);
    public static final RegistryObject<Block> LOOT_URN_COMMON = registerBlock("loot_urn_common",
            BlockLoot::createUrnCommon);
    public static final RegistryObject<Block> LOOT_URN_UNCOMMON = registerBlock("loot_urn_uncommon",
            BlockLoot::createUrnUncommon);
    public static final RegistryObject<Block> LOOT_URN_RARE = registerBlock("loot_urn_rare",
            BlockLoot::createUrnRare);
    public static final RegistryObject<Block> HOLE = registerBlockNoItem("hole",
            BlockHole::new);
    public static final RegistryObject<Block> PLACEHOLDER_NETHERBRICK = registerBlock("placeholder_brick",
            () -> new BlockPlaceholder(BlockPlaceholder.PlaceholderType.NETHERBRICK));
    public static final RegistryObject<Block> PLACEHOLDER_OBSIDIAN = registerBlock("placeholder_obsidian",
            () -> new BlockPlaceholder(BlockPlaceholder.PlaceholderType.OBSIDIAN));
    public static final RegistryObject<Block> PLACEHOLDER_BARS = registerBlock("placeholder_bars",
            () -> new BlockPlaceholder(BlockPlaceholder.PlaceholderType.BARS));
    public static final RegistryObject<Block> PLACEHOLDER_ANVIL = registerBlock("placeholder_anvil",
            () -> new BlockPlaceholder(BlockPlaceholder.PlaceholderType.ANVIL));
    public static final RegistryObject<Block> PLACEHOLDER_CAULDRON = registerBlock("placeholder_cauldron",
            () -> new BlockPlaceholder(BlockPlaceholder.PlaceholderType.CAULDRON));
    public static final RegistryObject<Block> PLACEHOLDER_TABLE = registerBlock("placeholder_table",
            () -> new BlockPlaceholder(BlockPlaceholder.PlaceholderType.TABLE));
    public static final RegistryObject<Block> EVERFULL_URN = registerBlock("everfull_urn",
            BlockWaterJug::new);
    public static final RegistryObject<Block> PAVING_STONE = registerBlock("paving_stone",
            BlockPavingStone::createNormal);
    public static final RegistryObject<Block> PAVING_STONE_TRAVEL = registerBlock("paving_stone_travel",
            BlockPavingStone::createTravel);
    public static final RegistryObject<Block> PAVING_STONE_BARRIER = registerBlock("paving_stone_barrier",
            BlockPavingStone::createBarrier);
    public static final RegistryObject<Block> BARRIER = registerBlockNoItem("barrier",
            BlockBarrier::new);
    public static final RegistryObject<Block> BANNER_WHITE = registerBlock("banner_white",
            BlockBannerTC::createWhite, BannerBlockItem::new);
    public static final RegistryObject<Block> BANNER_ORANGE = registerBlock("banner_orange",
            BlockBannerTC::createOrange, BannerBlockItem::new);
    public static final RegistryObject<Block> BANNER_MAGENTA = registerBlock("banner_magenta",
            BlockBannerTC::createMagenta, BannerBlockItem::new);
    public static final RegistryObject<Block> BANNER_LIGHT_BLUE = registerBlock("banner_light_blue",
            BlockBannerTC::createLightBlue, BannerBlockItem::new);
    public static final RegistryObject<Block> BANNER_YELLOW = registerBlock("banner_yellow",
            BlockBannerTC::createYellow, BannerBlockItem::new);
    public static final RegistryObject<Block> BANNER_LIME = registerBlock("banner_lime",
            BlockBannerTC::createLime, BannerBlockItem::new);
    public static final RegistryObject<Block> BANNER_PINK = registerBlock("banner_pink",
            BlockBannerTC::createPink, BannerBlockItem::new);
    public static final RegistryObject<Block> BANNER_GRAY = registerBlock("banner_gray",
            BlockBannerTC::createGray, BannerBlockItem::new);
    public static final RegistryObject<Block> BANNER_LIGHT_GRAY = registerBlock("banner_light_gray",
            BlockBannerTC::createLightGray, BannerBlockItem::new);
    public static final RegistryObject<Block> BANNER_CYAN = registerBlock("banner_cyan",
            BlockBannerTC::createCyan, BannerBlockItem::new);
    public static final RegistryObject<Block> BANNER_PURPLE = registerBlock("banner_purple",
            BlockBannerTC::createPurple, BannerBlockItem::new);
    public static final RegistryObject<Block> BANNER_BLUE = registerBlock("banner_blue",
            BlockBannerTC::createBlue, BannerBlockItem::new);
    public static final RegistryObject<Block> BANNER_BROWN = registerBlock("banner_brown",
            BlockBannerTC::createBrown, BannerBlockItem::new);
    public static final RegistryObject<Block> BANNER_GREEN = registerBlock("banner_green",
            BlockBannerTC::createGreen, BannerBlockItem::new);
    public static final RegistryObject<Block> BANNER_RED = registerBlock("banner_red",
            BlockBannerTC::createRed, BannerBlockItem::new);
    public static final RegistryObject<Block> BANNER_BLACK = registerBlock("banner_black",
            BlockBannerTC::createBlack, BannerBlockItem::new);
    public static final RegistryObject<Block> BANNER_CRIMSON_CULT = registerBlock("banner_crimson_cult",
            BlockBannerTC::createCrimsonCult, BannerBlockItem::new);
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> blockObj = BLOCKS.register(name, block);
        registerBlockItem(name, blockObj);
        return blockObj;
    }
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block,
                                                                     java.util.function.Function<T, BlockItem> itemFactory) {
        RegistryObject<T> blockObj = BLOCKS.register(name, block);
        BLOCK_ITEMS.register(name, () -> itemFactory.apply(blockObj.get()));
        return blockObj;
    }
    private static <T extends Block> RegistryObject<T> registerBlockNoItem(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }
    public static void addFlowerPotPlants() {
        try {
            net.minecraft.world.level.block.FlowerPotBlock pot =
                    (net.minecraft.world.level.block.FlowerPotBlock) net.minecraft.world.level.block.Blocks.FLOWER_POT;
            pot.addPlant(new net.minecraft.resources.ResourceLocation(Arcaneresearch.MODID, "greatwood_sapling"),
                    POTTED_GREATWOOD_SAPLING);
            pot.addPlant(new net.minecraft.resources.ResourceLocation(Arcaneresearch.MODID, "silverwood_sapling"),
                    POTTED_SILVERWOOD_SAPLING);
            pot.addPlant(new net.minecraft.resources.ResourceLocation(Arcaneresearch.MODID, "shimmerleaf"),
                    POTTED_SHIMMERLEAF);
            pot.addPlant(new net.minecraft.resources.ResourceLocation(Arcaneresearch.MODID, "cinderpearl"),
                    POTTED_CINDERPEARL);
            pot.addPlant(new net.minecraft.resources.ResourceLocation(Arcaneresearch.MODID, "vishroom"),
                    POTTED_VISHROOM);
        } catch (Exception e) {
            Arcaneresearch.LOGGER.warn("Failed to register Arcaneresearch flower pot plants", e);
        }
    }
    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        BLOCK_ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
