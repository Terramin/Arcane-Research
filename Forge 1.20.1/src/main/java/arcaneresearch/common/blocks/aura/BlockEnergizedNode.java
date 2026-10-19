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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.common.tiles.aura.TileNodeEnergized;
import javax.annotation.Nullable;
public class BlockEnergizedNode extends BaseEntityBlock {
    private static final VoxelShape TARGET = Block.box(5.0D, 5.0D, 5.0D, 11.0D, 11.0D, 11.0D);
    public BlockEnergizedNode() {
        super(BlockBehaviour.Properties.of()
                .strength(-1.0F, 6000000.0F)
                .sound(SoundType.WOOL)
                .noLootTable()
                .noOcclusion()
                .pushReaction(PushReaction.BLOCK));
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
            if (level.isClientSide) {
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
