package arcaneresearch.common.blocks.misc;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.common.tiles.misc.TileBarrierStone;
import arcaneresearch.init.ModBlocks;
public class BlockBarrier extends Block {
    private static final VoxelShape EMPTY = Shapes.empty();
    private static final VoxelShape FULL = Shapes.block();
    public BlockBarrier() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.NONE)
                .strength(-1.0f)
                .noLootTable()
                .noOcclusion()
                .noCollission()
                .replaceable()
                .pushReaction(PushReaction.BLOCK));
    }
    private static boolean isRiddenByPlayer(Entity entity) {
        for (Entity passenger : entity.getPassengers()) {
            if (passenger instanceof Player || isRiddenByPlayer(passenger)) {
                return true;
            }
        }
        return false;
    }
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return EMPTY;
    }
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext) {
            Entity entity = entityContext.getEntity();
            if (entity instanceof Player) {
                return EMPTY;
            }
            if (entity instanceof LivingEntity) {
                if (isRiddenByPlayer(entity)) {
                    return EMPTY;
                }
                if (!isBarrierActive(level, pos)) {
                    return EMPTY;
                }
                return FULL;
            }
        }
        return EMPTY;
    }
    private boolean isBarrierActive(BlockGetter level, BlockPos pos) {
        for (int i = 1; i <= 2; i++) {
            BlockPos below = pos.below(i);
            BlockState state = level.getBlockState(below);
            if (state.is(ModBlocks.PAVING_STONE_BARRIER.get())) {
                BlockEntity be = level.getBlockEntity(below);
                if (be instanceof TileBarrierStone barrierStone) {
                    return !barrierStone.isGettingPower();
                }
            }
        }
        return true;
    }
    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState,
                                  LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (facing == Direction.DOWN) {
            BlockState below = level.getBlockState(currentPos.below());
            if (!below.is(ModBlocks.PAVING_STONE_BARRIER.get()) && !below.is(this)) {
                return Blocks.AIR.defaultBlockState();
            }
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        BlockState below1 = level.getBlockState(pos.below(1));
        if (!below1.is(ModBlocks.PAVING_STONE_BARRIER.get()) && !below1.is(this)) {
            level.removeBlock(pos, false);
        }
    }
    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }
    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0f;
    }
}
