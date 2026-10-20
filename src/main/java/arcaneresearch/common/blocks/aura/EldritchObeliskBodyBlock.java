package arcaneresearch.common.blocks.aura;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
public class EldritchObeliskBodyBlock extends Block {
    public EldritchObeliskBodyBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(-1.0F, 3600000.0F)
                .sound(SoundType.STONE)
                .lightLevel(state -> 8)
                .noLootTable()
                .noOcclusion());
    }
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        super.onRemove(state, level, pos, newState, moving);
        if (level.isClientSide) {
            return;
        }
        try {
            if (newState.is(this)) {
                return;
            }
            Block altar = arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_ALTAR.get();
            Block base = arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_OBELISK.get();
            Block cap = arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_CAP.get();
            for (int x = -3; x <= 3; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -3; z <= 3; z++) {
                        BlockPos p = pos.offset(x, y, z);
                        BlockState s = level.getBlockState(p);
                        if (s.getBlock() == this || s.getBlock() == base
                                || s.getBlock() == altar || s.getBlock() == cap) {
                            level.removeBlock(p, false);
                        }
                    }
                }
            }
            level.explode(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 1.0F,
                    Level.ExplosionInteraction.NONE);
        } catch (Exception ignored) {
        }
    }
}
