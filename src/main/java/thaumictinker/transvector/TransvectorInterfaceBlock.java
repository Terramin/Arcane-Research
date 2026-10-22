package thaumictinker.transvector;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import thaumictinker.camo.CamoBlock;

 

 
public class TransvectorInterfaceBlock extends CamoBlock {

    public TransvectorInterfaceBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TransvectorInterfaceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != TransvectorFeature.INTERFACE_ENTITY) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) ->
                TransvectorInterfaceBlockEntity.tick(tickLevel, pos, tickState, (TransvectorInterfaceBlockEntity) entity);
    }
}
