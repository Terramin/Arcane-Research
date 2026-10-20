package arcaneresearch.common.blocks.world.ore;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class BlockOreTC extends DropExperienceBlock {

    public BlockOreTC(Properties properties) {
        super(UniformInt.of(1, 4), properties);
    }

    public static BlockOreTC createAmberOre(BlockBehaviour.Properties props) {
        return new BlockOreTC(props
                .mapColor(MapColor.STONE)
                .strength(5.0f, 25.0f)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
    }

    public static BlockOreTC createCinnabarOre(BlockBehaviour.Properties props) {
        return new BlockOreTC(props
                .mapColor(MapColor.STONE)
                .strength(5.0f, 25.0f)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
    }

    public static BlockOreTC createQuartzOre(BlockBehaviour.Properties props) {
        return new BlockOreTC(props
                .mapColor(MapColor.STONE)
                .strength(5.0f, 25.0f)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
    }

    public static BlockOreTC createDeepslateAmberOre(BlockBehaviour.Properties props) {
        return new BlockOreTC(props
                .mapColor(MapColor.DEEPSLATE)
                .strength(3.0f, 6.0f)
                .sound(SoundType.DEEPSLATE)
                .requiresCorrectToolForDrops());
    }

    public static BlockOreTC createDeepslateCinnabarOre(BlockBehaviour.Properties props) {
        return new BlockOreTC(props
                .mapColor(MapColor.DEEPSLATE)
                .strength(3.5f, 6.0f)
                .sound(SoundType.DEEPSLATE)
                .requiresCorrectToolForDrops());
    }

    public static BlockOreTC createDeepslateQuartzOre(BlockBehaviour.Properties props) {
        return new BlockOreTC(props
                .mapColor(MapColor.DEEPSLATE)
                .strength(4.5f, 6.0f)
                .sound(SoundType.DEEPSLATE)
                .requiresCorrectToolForDrops());
    }
}
