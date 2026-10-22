package thaumictinker.elements;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraftforge.event.ForgeEventFactory;

 




 
public class ImbuedFireBlock extends BaseFireBlock {

    public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;
    public static final BooleanProperty UP = PipeBlock.UP;

    private final int tickDelay;
    private final FireTransformation transformation;

    public ImbuedFireBlock(Properties properties, int tickDelay, FireTransformation transformation) {
        super(properties, 1.0F);
        this.tickDelay = tickDelay;
        this.transformation = transformation;
        registerDefaultState(stateDefinition.any()
                .setValue(AGE, 0)
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false));
    }

    public FireTransformation transformation() {
        return transformation;
    }

    public boolean isTarget(LevelReader level, BlockPos pos) {
        return level.hasChunkAt(pos) && transformation.result(level, pos, level.getBlockState(pos)) != null;
    }

    public boolean isNeighborTarget(LevelReader level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (isTarget(level, pos.relative(direction))) {
                return true;
            }
        }
        return false;
    }

    public BlockState fireState(LevelReader level, BlockPos pos, int age) {
        BlockState state = defaultBlockState().setValue(AGE, age);
        BlockPos below = pos.below();
        if (level.getBlockState(below).isFaceSturdy(level, below, Direction.UP) || isTarget(level, below)) {
            return state;
        }
        for (Direction direction : Direction.values()) {
            BooleanProperty property = PipeBlock.PROPERTY_BY_DIRECTION.get(direction);
            if (property != null) {
                state = state.setValue(property, isTarget(level, pos.relative(direction)));
            }
        }
        return state;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return fireState(context.getLevel(), context.getClickedPos(), 0);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState neighbourState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        return canSurvive(state, level, pos) ? fireState(level, pos, state.getValue(AGE)) : Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP) || isNeighborTarget(level, pos);
    }

    @Override
    protected boolean canBurn(BlockState state) {
        return ((FireBlock) Blocks.FIRE).getBurnOdds(state) > 0;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (oldState.is(this)) {
            return;
        }
        if (level.dimension() == Level.OVERWORLD || level.dimension() == Level.NETHER) {
            Optional<PortalShape> shape = PortalShape.findEmptyPortalShape(level, pos, Direction.Axis.X);
            shape = ForgeEventFactory.onTrySpawnPortal(level, pos, shape);
            if (shape.isPresent()) {
                shape.get().createPortalBlocks();
                return;
            }
        }
        level.scheduleTick(pos, this, tickDelay + level.getRandom().nextInt(10));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, NORTH, EAST, SOUTH, WEST, UP);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            return;
        }
        int age = state.getValue(AGE);
        boolean removed = false;
        if (isRainingNear(level, pos)) {
            level.removeBlock(pos, false);
            removed = true;
        } else if (!isNeighborTarget(level, pos)) {
            level.removeBlock(pos, false);
            removed = true;
        } else if (random.nextInt(20) == 0) {
            transformNeighbors(level, pos, random);
        }
        if (!removed) {
            if (age < 15) {
                int newAge = age + random.nextInt(3) / 2;
                if (newAge != age) {
                    level.setBlock(pos, state.setValue(AGE, newAge), 260);
                }
            }
            level.scheduleTick(pos, this, tickDelay + random.nextInt(3));
        }
        spread(level, pos, random, age);
    }

    public void transformNeighbors(ServerLevel level, BlockPos pos, RandomSource random) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.relative(direction);
            if (!level.hasChunkAt(neighbor)) {
                continue;
            }
            BlockState neighborState = level.getBlockState(neighbor);
            BlockState blockstate = transformation.result(level, neighbor, neighborState);
            if (blockstate == null) {
                continue;
            }
            if (random.nextInt(transformation.decayChance(level, neighbor, neighborState)) == 0) {
                level.setBlock(neighbor, blockstate, 3);
            } else {
                level.setBlock(neighbor, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    private void spread(ServerLevel level, BlockPos pos, RandomSource random, int age) {
        BlockPos.MutableBlockPos candidate = new BlockPos.MutableBlockPos();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = -1; dy <= 2; dy++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    candidate.setWithOffset(pos, dx, dy, dz);
                    if (!level.hasChunkAt(candidate) || !level.isEmptyBlock(candidate) || !isNeighborTarget(level, candidate)) {
                        continue;
                    }
                    int chance = (100 + 70) / (age + 30) + 70;
                    if (chance > 0 && random.nextInt(100) <= chance && !isRainingNear(level, candidate)) {
                        int newAge = Math.min(15, age + random.nextInt(5) / 4);
                        level.setBlock(candidate.immutable(), fireState(level, candidate, newAge), 3);
                    }
                }
            }
        }
    }

    private static boolean isRainingNear(Level level, BlockPos pos) {
        return level.isRainingAt(pos)
                || level.isRainingAt(pos.west())
                || level.isRainingAt(pos.east())
                || level.isRainingAt(pos.north())
                || level.isRainingAt(pos.south());
    }
}
