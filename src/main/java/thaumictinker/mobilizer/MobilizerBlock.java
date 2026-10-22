package thaumictinker.mobilizer;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

 

 
public class MobilizerBlock extends Block implements net.minecraft.world.level.block.EntityBlock {

    public MobilizerBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MobilizerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != MobilizerFeature.MOBILIZER_ENTITY) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> MobilizerBlockEntity.tick(tickLevel, pos, tickState, (MobilizerBlockEntity) entity);
    }
}
