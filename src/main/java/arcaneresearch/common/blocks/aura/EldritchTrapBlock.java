package arcaneresearch.common.blocks.aura;
import arcaneresearch.common.tiles.aura.EldritchTrapTile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.common.blocks.aura.AuraBlockEntities;

import javax.annotation.Nullable;

 


 
public class EldritchTrapBlock extends BaseEntityBlock {
    public EldritchTrapBlock(BlockBehaviour.Properties props) {
        super(props
                .strength(-1.0F, 3600000.0F)
                .sound(SoundType.STONE)
                .noLootTable());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        try {
            return new EldritchTrapTile(AuraBlockEntities.ELDRITCH_TRAP.get(), pos, state);
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
                return null;
            }
            return createTickerHelper(type, AuraBlockEntities.ELDRITCH_TRAP.get(),
                    EldritchTrapTile::serverTick);
        } catch (Exception e) {
            return null;
        }
    }
}
