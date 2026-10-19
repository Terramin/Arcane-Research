package arcaneresearch.common.blocks;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public interface IBlockEnabled {
    BooleanProperty ENABLED = BlockStateProperties.ENABLED;
}
