package arcaneresearch.common.blocks.world.ore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.common.world.aura.AuraHandler;
import arcaneresearch.init.ModSounds;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
public class BlockCrystalTC extends Block {
    public static final IntegerProperty SIZE = IntegerProperty.create("size", 0, 3);
    public static final IntegerProperty GENERATION = IntegerProperty.create("gen", 1, 4);
    private static final VoxelShape SHAPE_FULL = Shapes.block();
    private static final VoxelShape SHAPE_UP = Block.box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_DOWN = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    private static final VoxelShape SHAPE_NORTH = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 8.0);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0.0, 0.0, 8.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_EAST = Block.box(8.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_WEST = Block.box(0.0, 0.0, 0.0, 8.0, 16.0, 16.0);
    private final Aspect aspect;
    public BlockCrystalTC(Aspect aspect) {
        super(BlockBehaviour.Properties.of()
                .mapColor(getColorForAspect(aspect))
                .strength(0.25f)
                .sound(ModSounds.CRYSTAL_TYPE)
                .lightLevel(state -> 1)
                .noOcclusion()
                .noCollission()
                .randomTicks());
        this.aspect = aspect;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(SIZE, 0)
                .setValue(GENERATION, 1));
    }
    private static MapColor getColorForAspect(Aspect aspect) {
        if (aspect == Aspect.AIR) return MapColor.COLOR_YELLOW;
        if (aspect == Aspect.FIRE) return MapColor.COLOR_RED;
        if (aspect == Aspect.WATER) return MapColor.COLOR_BLUE;
        if (aspect == Aspect.EARTH) return MapColor.COLOR_GREEN;
        if (aspect == Aspect.ORDER) return MapColor.QUARTZ;
        if (aspect == Aspect.ENTROPY) return MapColor.COLOR_GRAY;
        if (aspect == Aspect.FLUX) return MapColor.COLOR_PURPLE;
        return MapColor.QUARTZ;
    }
    public static BlockPos spreadCrystal(Level level, BlockPos pos, RandomSource random) {
        int x = pos.getX() + random.nextInt(3) - 1;
        int y = pos.getY() + random.nextInt(3) - 1;
        int z = pos.getZ() + random.nextInt(3) - 1;
        BlockPos targetPos = new BlockPos(x, y, z);
        if (targetPos.equals(pos)) {
            return null;
        }
        BlockState targetState = level.getBlockState(targetPos);
        FluidState fluid = level.getFluidState(targetPos);
        if (!fluid.isEmpty()) {
            return null;
        }
        if ((level.isEmptyBlock(targetPos) || targetState.canBeReplaced())
                && random.nextInt(16) == 0
                && isTouchingRock(level, targetPos)) {
            return targetPos;
        }
        return null;
    }
    public static boolean isTouchingRock(LevelReader level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (isValidSupport(neighborState, level, neighborPos, dir.getOpposite())) {
                return true;
            }
        }
        return false;
    }
    public static boolean isValidSupport(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        if (!state.isFaceSturdy(level, pos, face)) {
            return false;
        }
        return state.getSoundType() == net.minecraft.world.level.block.SoundType.STONE
                || state.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD)
                || state.is(net.minecraft.tags.BlockTags.BASE_STONE_NETHER);
    }
    public Aspect getAspect() {
        return aspect;
    }
    @Override
    public SoundType getSoundType(BlockState state) {
        return ModSounds.CRYSTAL_TYPE;
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SIZE, GENERATION);
    }
    public int getGrowth(BlockState state) {
        return state.getValue(SIZE);
    }
    public int getGeneration(BlockState state) {
        return state.getValue(GENERATION);
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int attachedCount = 0;
        Direction attachedDir = null;
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (isValidSupport(neighborState, level, neighborPos, dir.getOpposite())) {
                attachedCount++;
                attachedDir = dir;
            }
        }
        if (attachedCount > 1 || attachedDir == null) {
            return SHAPE_FULL;
        }
        return switch (attachedDir) {
            case UP -> SHAPE_UP;
            case DOWN -> SHAPE_DOWN;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
        };
    }
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return isTouchingRock(level, pos);
    }
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (!isTouchingRock(level, pos)) {
            return null;
        }
        return super.getStateForPlacement(context);
    }
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                Block neighborBlock, BlockPos neighborPos, boolean moving) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, moving);
        if (!level.isClientSide && !isTouchingRock(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int generation = getGeneration(state);
        if (random.nextInt(3 + generation) != 0) {
            return;
        }
        int threshold = 10;
        int growth = getGrowth(state);
        if (aspect != Aspect.FLUX) {
            if (AuraHelper.getVis(level, pos) <= threshold) {
                if (growth > 0) {
                    level.setBlock(pos, state.setValue(SIZE, growth - 1), 3);
                    AuraHelper.addVis(level, pos, threshold);
                } else if (isTouchingSelf(level, pos)) {
                    level.removeBlock(pos, false);
                    AuraHandler.addVis(level, pos, threshold);
                }
            } else if (AuraHelper.getVis(level, pos) > AuraHandler.getAuraBase(level, pos) + threshold) {
                if (growth < 3 && growth < 5 - generation + pos.asLong() % 3L) {
                    if (AuraHelper.drainVis(level, pos, threshold, false) > 0.0f) {
                        level.setBlock(pos, state.setValue(SIZE, growth + 1), 3);
                    }
                } else if (generation < 4) {
                    BlockPos spreadPos = spreadCrystal(level, pos, random);
                    if (spreadPos != null && AuraHelper.drainVis(level, pos, threshold, false) > 0.0f) {
                        if (random.nextInt(6) == 0) {
                            generation--;
                        }
                        level.setBlock(spreadPos, this.defaultBlockState().setValue(GENERATION, generation + 1), 3);
                    }
                }
            }
        } else {
            if (AuraHelper.getFlux(level, pos) <= threshold) {
                if (growth > 0) {
                    level.setBlock(pos, state.setValue(SIZE, growth - 1), 3);
                    AuraHelper.polluteAura(level, pos, threshold, false);
                } else if (isTouchingSelf(level, pos)) {
                    level.removeBlock(pos, false);
                    AuraHelper.polluteAura(level, pos, threshold, false);
                }
            } else if (AuraHelper.getFlux(level, pos) > AuraHandler.getAuraBase(level, pos) + threshold) {
                if (growth < 3 && growth < 5 - generation + pos.asLong() % 3L) {
                    if (AuraHelper.drainFlux(level, pos, threshold, false) > 0.0f) {
                        level.setBlock(pos, state.setValue(SIZE, growth + 1), 3);
                    }
                } else if (generation < 4) {
                    BlockPos spreadPos = spreadCrystal(level, pos, random);
                    if (spreadPos != null && AuraHelper.drainFlux(level, pos, threshold, false) > 0.0f) {
                        if (random.nextInt(6) == 0) {
                            generation--;
                        }
                        level.setBlock(spreadPos, this.defaultBlockState().setValue(GENERATION, generation + 1), 3);
                    }
                }
            }
        }
    }
    private boolean isTouchingSelf(LevelReader level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (level.getBlockState(pos.relative(dir)).getBlock() == this) {
                return true;
            }
        }
        return false;
    }
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>();
        int count = getGrowth(state) + 1;
        for (int i = 0; i < count; i++) {
            ItemStack crystal = ArcaneresearchApiHelper.makeCrystal(aspect);
            if (!crystal.isEmpty()) {
                drops.add(crystal);
            }
        }
        return drops;
    }
}
