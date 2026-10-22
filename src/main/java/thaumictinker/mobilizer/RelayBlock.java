package thaumictinker.mobilizer;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

 

 
public class RelayBlock extends Block implements net.minecraft.world.level.block.EntityBlock {

    public RelayBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RelayBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != MobilizerFeature.RELAY_ENTITY) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> RelayBlockEntity.tick(tickLevel, pos, tickState, (RelayBlockEntity) entity);
    }
}
