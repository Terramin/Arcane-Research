package arcaneresearch.common.blocks.basic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import arcaneresearch.common.blocks.BlockTC;

public class BlockPlanksTC extends BlockTC {

    public BlockPlanksTC(Properties properties) {
        super(properties);
    }

    public static BlockPlanksTC createGreatwood(net.minecraft.world.level.block.state.BlockBehaviour.Properties props) {
        return new BlockPlanksTC(
                props
                        .mapColor(MapColor.COLOR_BROWN)
                        .strength(2.0f, 2.0f)
                        .sound(SoundType.WOOD)
        );
    }

    public static BlockPlanksTC createSilverwood(net.minecraft.world.level.block.state.BlockBehaviour.Properties props) {
        return new BlockPlanksTC(
                props
                        .mapColor(MapColor.QUARTZ)
                        .strength(2.0f, 2.0f)
                        .sound(SoundType.WOOD)
        );
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 20;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }
}
