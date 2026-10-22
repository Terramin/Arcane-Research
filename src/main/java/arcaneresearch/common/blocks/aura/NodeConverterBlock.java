package arcaneresearch.common.blocks.aura;
import arcaneresearch.client.renderers.tile.NodeConverterRenderer;
import arcaneresearch.common.tiles.aura.NodeConverterTile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.common.blocks.aura.AuraBlockEntities;

import javax.annotation.Nullable;

 




 
public class NodeConverterBlock extends BaseEntityBlock {
    public NodeConverterBlock(BlockBehaviour.Properties props) {
        super(props
                .strength(3.0F, 25.0F)
                .sound(SoundType.STONE)
                
                
                .noOcclusion());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        
        return RenderShape.INVISIBLE;
    }

    
    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, net.minecraft.world.level.BlockGetter level,
                                    BlockPos pos) {
        return 1.0F;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block block,
                                net.minecraft.world.level.redstone.Orientation orientation, boolean moved) {
        super.neighborChanged(state, level, pos, block, orientation, moved);
        if (level.getBlockEntity(pos) instanceof NodeConverterTile tile) {
            tile.checkStatus();
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        try {
            return new NodeConverterTile(AuraBlockEntities.NODE_CONVERTER.get(), pos, state);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        try {
            if (level.isClientSide()) {
                return createTickerHelper(type, AuraBlockEntities.NODE_CONVERTER.get(),
                        NodeConverterTile::clientTick);
            }
            return createTickerHelper(type, AuraBlockEntities.NODE_CONVERTER.get(),
                    NodeConverterTile::serverTick);
        } catch (Exception e) {
            return null;
        }
    }
}
