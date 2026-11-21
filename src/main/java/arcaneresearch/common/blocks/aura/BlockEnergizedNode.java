package arcaneresearch.common.blocks.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.common.tiles.aura.NodeConverterTile;
import arcaneresearch.common.tiles.aura.StabilizerTile;
import arcaneresearch.common.tiles.aura.TileNodeEnergized;
import arcaneresearch.common.tiles.aura.TransducerTile;
import javax.annotation.Nullable;

public class BlockEnergizedNode extends BaseEntityBlock {
    private static final VoxelShape TARGET = Block.box(5.0D, 5.0D, 5.0D, 11.0D, 11.0D, 11.0D);

    public BlockEnergizedNode(BlockBehaviour.Properties props) {
        super(props
                .strength(-1.0F, 6000000.0F)
                .sound(SoundType.WOOL)
                .noLootTable()
                .noOcclusion()
                .pushReaction(PushReaction.IMMOVEABLE));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return TARGET;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                        CollisionContext context) {
        return Shapes.empty();
    }
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                Block neighborBlock, Orientation orientation, boolean moved) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, moved);
        if (level.isClientSide()) return;
        if (!(level.getBlockEntity(pos) instanceof TileNodeEnergized)) return;

        BlockPos belowPos = pos.below();
        BlockEntity belowTE = level.getBlockEntity(belowPos);
        if (level.hasNeighborSignal(belowPos) || !(belowTE instanceof StabilizerTile)) {
            explodify(level, pos);
            return;
        }

        BlockEntity aboveTE = level.getBlockEntity(pos.above());
        if (!(aboveTE instanceof TransducerTile) && !(aboveTE instanceof NodeConverterTile)) {
            explodify(level, pos);
        }
    }

    public static void explodify(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) return;

        level.removeBlock(pos, false);
        level.explode(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                3.0F, Level.ExplosionInteraction.BLOCK);

        Block goo = arcaneresearch.init.ModBlocks.FLUX_GOO.get();
        Block gas = arcaneresearch.init.ModBlocks.FLUX_GAS.get();

        for (int a = 0; a < 50; a++) {
            BlockPos scatter = pos.offset(
                    level.getRandom().nextInt(8) - level.getRandom().nextInt(8),
                    level.getRandom().nextInt(8) - level.getRandom().nextInt(8),
                    level.getRandom().nextInt(8) - level.getRandom().nextInt(8));

            if (!level.isEmptyBlock(scatter)) continue;

            Block target = scatter.getY() < pos.getY() ? goo : gas;
            level.setBlock(scatter, target.defaultBlockState(), 3);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        try {
            return new TileNodeEnergized(AuraBlockEntities.ENERGIZED_NODE.get(), pos, state);
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
                return createTickerHelper(type, AuraBlockEntities.ENERGIZED_NODE.get(),
                        TileNodeEnergized::clientTick);
            }
            return createTickerHelper(type, AuraBlockEntities.ENERGIZED_NODE.get(),
                    TileNodeEnergized::serverTick);
        } catch (Exception e) {
            return null;
        }
    }
}
