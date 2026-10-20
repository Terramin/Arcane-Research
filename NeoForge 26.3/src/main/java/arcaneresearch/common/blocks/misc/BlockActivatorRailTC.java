package arcaneresearch.common.blocks.misc;

import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlockActivatorRailTC extends PoweredRailBlock {

    public BlockActivatorRailTC(BlockBehaviour.Properties props) {
        super(props
                        .strength(0.7f)
                        .sound(SoundType.METAL)
                        .noOcclusion(),
                false);
    }
}
