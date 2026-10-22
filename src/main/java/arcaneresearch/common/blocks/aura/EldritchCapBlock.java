package arcaneresearch.common.blocks.aura;
import arcaneresearch.common.tiles.aura.EldritchCapTile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.common.blocks.aura.AuraBlockEntities;

import javax.annotation.Nullable;

 



 
public class EldritchCapBlock extends BaseEntityBlock {
    public EldritchCapBlock(BlockBehaviour.Properties props) {
        super(props
                .strength(50.0F, 20000.0F)
                .sound(SoundType.STONE)
                .lightLevel(state -> 8)
                .noLootTable()
                .noOcclusion()
                
                
                
                
                .isValidSpawn((state, level, pos, type) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                
                .isViewBlocking((state, level, pos, nearPlaneBox) -> false));
    }

     




 
    @Override
    public RenderShape getRenderShape(BlockState state) {
        
        return RenderShape.INVISIBLE;
    }

      
    
    
    @Override
    protected int getLightDampening(BlockState state) {
        return 0;
    }

     



 
    @Override
    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moving) {
        super.affectNeighborsAfterRemoval(state, level, pos, moving);
        try {
            if (level.getBlockState(pos).is(state.getBlock())) {
                return;
            }
            Block altar = arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_ALTAR.get();
            Block obelisk = arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_OBELISK.get();
            Block body = arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_OBELISK_BODY.get();
            Block cap = arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_CAP.get();
            for (int x = -3; x <= 3; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -3; z <= 3; z++) {
                        net.minecraft.core.BlockPos p = pos.offset(x, y, z);
                        BlockState s = level.getBlockState(p);
                        if (s.getBlock() == altar || s.getBlock() == obelisk
                                || s.getBlock() == body || s.getBlock() == cap) {
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

     



 
    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction side) {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        try {
            return new EldritchCapTile(AuraBlockEntities.ELDRITCH_CAP.get(), pos, state);
        } catch (Exception e) {
            return null;
        }
    }
}
