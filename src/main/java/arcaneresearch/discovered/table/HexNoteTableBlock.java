package arcaneresearch.discovered.table;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
public class HexNoteTableBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
    private static VoxelShape shapeFor(Part part, Direction facing) {
        Direction eff = part == Part.SIDE ? facing.getOpposite() : facing;
        double[][] boxes = part == Part.MAIN
                ? new double[][]{
                        {0, 12, 0, 16, 16, 16},
                        {2, 0, 2, 6, 12, 6},
                        {2, 0, 10, 6, 12, 14},
                        {4, 2, 6, 16, 6, 10}}
                : new double[][]{
                        {0, 12, 0, 16, 16, 16},
                        {10, 0, 2, 14, 12, 6},
                        {10, 0, 10, 14, 12, 14},
                        {0, 2, 6, 12, 6, 10}};
        VoxelShape shape = Shapes.empty();
        for (double[] b : boxes) {
            shape = Shapes.or(shape, rotBox(b, eff));
        }
        return shape;
    }
    private static VoxelShape rotBox(double[] b, Direction facing) {
        double x1 = b[0], y1 = b[1], z1 = b[2], x2 = b[3], y2 = b[4], z2 = b[5];
        return switch (facing) {
            case EAST -> Block.box(x1, y1, z1, x2, y2, z2);
            case SOUTH -> Block.box(16.0 - z2, y1, x1, 16.0 - z1, y2, x2);
            case WEST -> Block.box(16.0 - x2, y1, 16.0 - z2, 16.0 - x1, y2, 16.0 - z1);
            case NORTH -> Block.box(z1, y1, 16.0 - x2, z2, y2, 16.0 - x1);
            default -> Block.box(x1, y1, z1, x2, y2, z2);
        };
    }
    public HexNoteTableBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.0F)
                .sound(SoundType.WOOD)
                .noOcclusion());
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, Part.MAIN));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }
    public static BlockPos mainPos(BlockPos pos, BlockState state) {
        return state.getValue(PART) == Part.MAIN ? pos : pos.relative(state.getValue(FACING));
    }
    private static BlockPos partnerPos(BlockPos pos, BlockState state) {
        return pos.relative(state.getValue(FACING));
    }
    private static boolean isPartner(BlockState state, BlockState neighbour) {
        if (!(neighbour.getBlock() instanceof HexNoteTableBlock)) {
            return false;
        }
        return neighbour.getValue(PART) != state.getValue(PART)
                && neighbour.getValue(FACING) == state.getValue(FACING).getOpposite();
    }
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState neighbour = level.getBlockState(partnerPos(pos, state));
        return isPartner(state, neighbour);
    }
    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState,
            LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        if (direction == state.getValue(FACING) && !isPartner(state, neighbourState)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return state;
    }
    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            BlockPos partner = partnerPos(pos, state);
            Block single = arcaneresearch.discovered.table.DiscoveredTableSetup.getSingleTable();
            if (level.getBlockState(partner).getBlock() instanceof HexNoteTableBlock
                    && single != null) {
                level.setBlock(partner, single.defaultBlockState(), 3);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state.getValue(PART), state.getValue(FACING));
    }
    @Override
    @net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos,
            net.minecraft.util.RandomSource random) {
        if (state.getValue(PART) != Part.MAIN) {
            return;
        }
        if (random.nextInt(5) == 0) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof HexNoteTableTile tile && level.isClientSide
                    && !tile.getItem(HexNoteTableTile.NOTE_SLOT).isEmpty()) {
                double xx = random.nextGaussian() / 2.0;
                double zz = random.nextGaussian() / 2.0;
                double yy = 1.5 + random.nextFloat();
                int a = 40 + random.nextInt(20);
                net.minecraft.client.multiplayer.ClientLevel clientLevel =
                        (net.minecraft.client.multiplayer.ClientLevel) level;
                arcaneresearch.client.fx.particles.FXGeneric fb =
                        new arcaneresearch.client.fx.particles.FXGeneric(clientLevel,
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
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockPos main = mainPos(pos, state);
        BlockEntity be = level.getBlockEntity(main);
        if (be instanceof HexNoteTableTile tile) {
            NetworkHooks.openScreen((ServerPlayer) player, tile, main);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (state.getValue(PART) == Part.MAIN
                    && level.getBlockEntity(pos) instanceof HexNoteTableTile tile) {
                tile.dropContents();
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == Part.MAIN ? new HexNoteTableTile(pos, state) : null;
    }
    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide || state.getValue(PART) != Part.MAIN) {
            return null;
        }
        return (BlockEntityTicker<T>) (BlockEntityTicker<HexNoteTableTile>) HexNoteTableTile::tick;
    }
    public enum Part implements StringRepresentable {
        MAIN("main"),
        SIDE("side");
        private final String name;
        Part(String name) {
            this.name = name;
        }
        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
