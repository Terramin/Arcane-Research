package arcaneresearch.common.blocks.aura;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.common.blocks.basic.BlockStairsTC;
import java.util.function.Supplier;
public class EldritchStairsBlock extends BlockStairsTC {
    public EldritchStairsBlock(Supplier<BlockState> baseState) {
        super(baseState,
                BlockBehaviour.Properties.of()
                        .strength(-1.0F, 3600000.0F)
                        .sound(SoundType.STONE)
                        .noLootTable(),
                false);
    }
}
