package thaumictinker.darkquartz;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

import thaumictinker.registry.TtRegistry;

 



 
public final class DarkQuartzFeature {

    public static final String RESEARCH = "DARK_QUARTZ";

    public static final Item DARK_QUARTZ = TtRegistry.itemWithTab("dark_quartz", () -> new Item(new Item.Properties()));
    public static final Block DARK_QUARTZ_BLOCK = TtRegistry.blockWithItem("dark_quartz_block", new Block(quartz()));
    public static final Block CHISELED_DARK_QUARTZ_BLOCK = TtRegistry.blockWithItem("chiseled_dark_quartz_block", new Block(quartz()));
    public static final RotatedPillarBlock DARK_QUARTZ_PILLAR = TtRegistry.blockWithItem("dark_quartz_pillar", new RotatedPillarBlock(quartz()));
    public static final SlabBlock DARK_QUARTZ_SLAB = TtRegistry.blockWithItem("dark_quartz_slab", new SlabBlock(quartz()));
    public static final StairBlock DARK_QUARTZ_STAIRS = TtRegistry.blockWithItem("dark_quartz_stairs",
            new StairBlock(DARK_QUARTZ_BLOCK.defaultBlockState(), quartz()));

    private DarkQuartzFeature() {
    }

    public static void load() {
    }

    private static BlockBehaviour.Properties quartz() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BLACK)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops()
                .strength(0.8F, 10.0F)
                .sound(SoundType.STONE);
    }
}
