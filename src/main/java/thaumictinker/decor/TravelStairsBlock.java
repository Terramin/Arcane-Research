package thaumictinker.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

 


 
public class TravelStairsBlock extends StairBlock {

    public TravelStairsBlock(BlockState baseState, Properties properties) {
        super(baseState, properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        TravelEffect.apply(level, pos, entity);
        super.stepOn(level, pos, state, entity);
    }
}
