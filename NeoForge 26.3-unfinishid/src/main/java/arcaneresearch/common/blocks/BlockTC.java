package arcaneresearch.common.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public class BlockTC extends Block {

    public BlockTC(Properties properties) {
        super(properties);
    }

    public static Properties defaultStoneProperties() {
        return Properties.of()
                .mapColor(MapColor.STONE)
                .strength(2.0f, 10.0f)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops();
    }

    public static Properties defaultWoodProperties() {
        return Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.0f, 3.0f)
                .sound(SoundType.WOOD);
    }

    public static Properties defaultMetalProperties() {
        return Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0f, 6.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops();
    }

    public static Properties defaultGlassProperties() {
        return Properties.of()
                .mapColor(MapColor.NONE)
                .strength(0.3f)
                .sound(SoundType.GLASS)
                .noOcclusion();
    }
}
