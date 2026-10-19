package arcaneresearch.common.blocks;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public interface IBlockFacingHorizontal {
    EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
}
