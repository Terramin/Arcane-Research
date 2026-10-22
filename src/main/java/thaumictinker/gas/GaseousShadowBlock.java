package thaumictinker.gas;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import arcaneresearch.client.fx.FXDispatcher;

 


 
public class GaseousShadowBlock extends SpreadingGasBlock {

    public GaseousShadowBlock(Properties properties) {
        super(properties);
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 15;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() < 0.0075F) {
            FXDispatcher.INSTANCE.sparkle(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F,
                    0.25F, 0.0F, 0.3F);
        }
    }
}
