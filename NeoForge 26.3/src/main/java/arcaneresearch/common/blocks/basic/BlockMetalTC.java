package arcaneresearch.common.blocks.basic;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import arcaneresearch.common.blocks.BlockTC;

public class BlockMetalTC extends BlockTC {

    public BlockMetalTC(Properties properties) {
        super(properties);
    }

    public static BlockMetalTC create(net.minecraft.world.level.block.state.BlockBehaviour.Properties props) {
        return new BlockMetalTC(
                props
                        .mapColor(MapColor.METAL)
                        .strength(5.0f, 6.0f)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
        );
    }

    public static BlockMetalTC create(net.minecraft.world.level.block.state.BlockBehaviour.Properties props, MapColor color) {
        return new BlockMetalTC(
                props
                        .mapColor(color)
                        .strength(5.0f, 6.0f)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
        );
    }
}
