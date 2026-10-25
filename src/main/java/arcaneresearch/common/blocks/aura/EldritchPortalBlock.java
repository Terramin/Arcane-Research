package arcaneresearch.common.blocks.aura;
import arcaneresearch.common.tiles.aura.EldritchPortalTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.common.blocks.aura.AuraBlockEntities;
import arcaneresearch.init.ModBlocks;
import javax.annotation.Nullable;
public class EldritchPortalBlock extends BaseEntityBlock {
    public EldritchPortalBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(-1.0F, 3600000.0F)
                .sound(SoundType.GLASS)
                .lightLevel(state -> 15)
                .noLootTable()
                .noCollission());
    }
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.empty();
    }
    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block block,
                                BlockPos neighborPos, boolean moved) {
        super.neighborChanged(state, level, pos, block, neighborPos, moved);
        if (!isFrame(level.getBlockState(pos.above()))
                || !isFrame(level.getBlockState(pos.below()))) {
            level.removeBlock(pos, false);
        }
    }
    public static boolean isFrame(BlockState state) {
        try {
            if (state.is(ModBlocks.ELDRITCH_STONE_TILE.get())) {
                return true;
            }
            if (state.is(arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_ALTAR.get())) {
                return true;
            }
            if (state.is(ModBlocks.ELDRITCH_PILLAR.get())) {
                return true;
            }
            if (state.is(arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_CAP.get())) {
                return true;
            }
            return state.is(arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_OBELISK.get());
        } catch (Exception e) {
            return false;
        }
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        try {
            return new EldritchPortalTile(AuraBlockEntities.ELDRITCH_PORTAL.get(), pos, state);
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
            if (level.isClientSide) {
                return createTickerHelper(type, AuraBlockEntities.ELDRITCH_PORTAL.get(),
                        EldritchPortalTile::clientTick);
            }
            return createTickerHelper(type, AuraBlockEntities.ELDRITCH_PORTAL.get(),
                    EldritchPortalTile::serverTick);
        } catch (Exception e) {
            return null;
        }
    }
}
