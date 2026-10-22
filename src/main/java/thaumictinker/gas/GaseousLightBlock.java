package thaumictinker.gas;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import arcaneresearch.client.fx.FXDispatcher;

 

 
public class GaseousLightBlock extends SpreadingGasBlock {

    public static final int LIGHT = 12;

    public GaseousLightBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() < 0.0075F) {
            FXDispatcher.INSTANCE.sparkle(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F,
                    1.0F, 1.0F, 1.0F);
        }
    }
}
