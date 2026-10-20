package arcaneresearch.common.blocks;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public interface IBlockFacing {
    EnumProperty<Direction> FACING = BlockStateProperties.FACING;
}
