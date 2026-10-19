package arcaneresearch.common.blocks.world.ore;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
public class BlockOreTC extends DropExperienceBlock {
    public BlockOreTC(Properties properties) {
        super(properties, UniformInt.of(1, 4));
    }
    public static BlockOreTC createAmberOre() {
        return new BlockOreTC(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(5.0f, 25.0f)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
    }
    public static BlockOreTC createCinnabarOre() {
        return new BlockOreTC(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(5.0f, 25.0f)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
    }
    public static BlockOreTC createQuartzOre() {
        return new BlockOreTC(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(5.0f, 25.0f)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
    }
    public static BlockOreTC createDeepslateAmberOre() {
        return new BlockOreTC(BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .strength(3.0f, 6.0f)
                .sound(SoundType.DEEPSLATE)
                .requiresCorrectToolForDrops());
    }
    public static BlockOreTC createDeepslateCinnabarOre() {
        return new BlockOreTC(BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .strength(3.5f, 6.0f)
                .sound(SoundType.DEEPSLATE)
                .requiresCorrectToolForDrops());
    }
    public static BlockOreTC createDeepslateQuartzOre() {
        return new BlockOreTC(BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .strength(4.5f, 6.0f)
                .sound(SoundType.DEEPSLATE)
                .requiresCorrectToolForDrops());
    }
}
