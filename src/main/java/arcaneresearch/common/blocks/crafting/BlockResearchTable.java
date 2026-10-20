package arcaneresearch.common.blocks.crafting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import arcaneresearch.common.tiles.crafting.TileResearchTable;
import javax.annotation.Nullable;
public class BlockResearchTable extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0.0, 12.0, 0.0, 16.0, 16.0, 16.0),
            Block.box(1.0, 0.0, 1.0, 4.0, 12.0, 4.0),
            Block.box(12.0, 0.0, 1.0, 15.0, 12.0, 4.0),
            Block.box(1.0, 0.0, 12.0, 4.0, 12.0, 15.0),
            Block.box(12.0, 0.0, 12.0, 15.0, 12.0, 15.0)
    );
    public BlockResearchTable() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.0f)
                .sound(SoundType.WOOD)
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileResearchTable tile) {
            NetworkHooks.openScreen((ServerPlayer) player, tile, pos);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileResearchTable tile) {
            tile.setChanged();
            tile.markDirtyAndSync();
        }
    }
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TileResearchTable tile) {
                tile.dropContents();
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) == 0) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileResearchTable table && table.hasResearchData() && level.isClientSide) {
                double xx = random.nextGaussian() / 2.0;
                double zz = random.nextGaussian() / 2.0;
                double yy = 1.5 + random.nextFloat();
                int a = 40 + random.nextInt(20);
                net.minecraft.client.multiplayer.ClientLevel clientLevel =
                        (net.minecraft.client.multiplayer.ClientLevel) level;
                arcaneresearch.client.fx.particles.FXGeneric fb = new arcaneresearch.client.fx.particles.FXGeneric(
                        clientLevel,
                        pos.getX() + 0.5 + xx, pos.getY() + yy, pos.getZ() + 0.5 + zz,
                        -xx / a, -(yy - 0.85) / a, -zz / a);
                fb.setMaxAge(a);
                fb.setColor(0.5f + random.nextFloat() * 0.5f,
                        0.5f + random.nextFloat() * 0.5f,
                        0.5f + random.nextFloat() * 0.5f);
                fb.setAlphaKeyframes(0.0f, 0.25f, 0.5f, 0.75f, 0.0f);
                fb.setParticles(384 + random.nextInt(16), 1, 1);
                fb.setScaleKeyframes((0.8f + random.nextFloat() * 0.3f) * 0.1f, 0.3f * 0.1f);
                fb.setLayer(0);
                net.minecraft.client.Minecraft.getInstance().particleEngine.add(fb);
            }
        }
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileResearchTable(pos, state);
    }
}
