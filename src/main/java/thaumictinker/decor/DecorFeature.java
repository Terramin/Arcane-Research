package thaumictinker.decor;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

import arcaneresearch.init.ModBlocks;
import thaumictinker.registry.TtRegistry;

 





 
public final class DecorFeature {

    public static final TravelSlabBlock TRAVEL_SLAB = TtRegistry.blockWithItem("travel_slab",
            new TravelSlabBlock(stone(BlockBehaviour.Properties.of(), 0.8F, 10.0F)));
    public static TravelStairsBlock TRAVEL_STAIRS;
    public static final WardSlabBlock WARD_SLAB = TtRegistry.blockWithItem("ward_slab",
            new WardSlabBlock(stone(BlockBehaviour.Properties.of(), 0.8F, 10.0F)));
    public static final BlockEntityType<WardedSlabBlockEntity> WARDED_SLAB =
            TtRegistry.blockEntity("warded_slab",
                    BlockEntityType.Builder.of(WardedSlabBlockEntity::new, WARD_SLAB).build(null));

    private DecorFeature() {
    }

    public static void load() {
        BlockState baseState;
        try {
            baseState = ModBlocks.PAVING_STONE_TRAVEL.get().defaultBlockState();
            thaumictinker.ThaumicTinker.LOGGER.info("DecorFeature: paving_stone_travel resolved OK");
        } catch (Throwable t) {
            thaumictinker.ThaumicTinker.LOGGER.warn("DecorFeature: paving_stone_travel NOT resolved ({}), falling back to stone", t.toString());
            baseState = net.minecraft.world.level.block.Blocks.STONE.defaultBlockState();
        }
        if (TRAVEL_STAIRS == null) {
            TRAVEL_STAIRS = TtRegistry.blockWithItem("travel_stairs",
                    new TravelStairsBlock(baseState, stone(BlockBehaviour.Properties.of(), 2.0F, 10.0F)));
        }
    }

    private static BlockBehaviour.Properties stone(BlockBehaviour.Properties properties, float hardness, float resistance) {
        return properties
                .mapColor(MapColor.STONE)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops()
                .strength(hardness, resistance)
                .sound(SoundType.STONE);
    }
}
