package arcaneresearch.common.blocks.misc;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
public class BlockActivatorRailTC extends PoweredRailBlock {
    public BlockActivatorRailTC() {
        super(BlockBehaviour.Properties.of()
                        .strength(0.7f)
                        .sound(SoundType.METAL)
                        .noOcclusion(),
                false);
    }
}
