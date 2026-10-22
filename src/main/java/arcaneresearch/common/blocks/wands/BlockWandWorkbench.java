package arcaneresearch.common.blocks.wands;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import arcaneresearch.common.blocks.BlockTCDevice;
import arcaneresearch.common.tiles.crafting.TileFocalManipulator;

public class BlockWandWorkbench extends BlockTCDevice implements EntityBlock {
    public BlockWandWorkbench(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileFocalManipulator(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide() || !type.isValid(state)) {
            return null;
        }
        return (lvl, p, s, be) -> {
            if (be instanceof TileFocalManipulator manipulator) {
                TileFocalManipulator.serverTick(lvl, p, s, manipulator);
            }
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    
    
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {

        return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        
        
        
        
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
        if (!state.is(level.getBlockState(pos).getBlock())) {
            
            level.updateNeighbourForOutputSignal(pos, this);
        }

    }
}
