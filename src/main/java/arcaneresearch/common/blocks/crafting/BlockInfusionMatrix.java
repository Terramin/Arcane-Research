package arcaneresearch.common.blocks.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.common.lib.events.EssentiaHandler;
import arcaneresearch.common.tiles.crafting.TileInfusionMatrix;
import arcaneresearch.init.ModBlockEntities;

import javax.annotation.Nullable;

public class BlockInfusionMatrix extends Block implements EntityBlock {

    private static final VoxelShape SHAPE = Shapes.block();

    public BlockInfusionMatrix(BlockBehaviour.Properties props) {
        super(props
                .mapColor(MapColor.STONE)
                .strength(5.0f, 100.0f)
                .sound(SoundType.STONE)
                .noOcclusion()
                .lightLevel(state -> 5));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {

        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileInfusionMatrix(pos, state);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        
        
        
        
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);

        if (!level.isClientSide() && !state.is(level.getBlockState(pos).getBlock())) {
            EssentiaHandler.refreshSources(level, pos);
        }

    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.INFUSION_MATRIX.get()) {
            if (level.isClientSide()) {
                return (lvl, pos, st, be) -> TileInfusionMatrix.clientTick(lvl, pos, st, (TileInfusionMatrix) be);
            } else {
                return (lvl, pos, st, be) -> TileInfusionMatrix.serverTick(lvl, pos, st, (TileInfusionMatrix) be);
            }
        }
        return null;
    }
}
