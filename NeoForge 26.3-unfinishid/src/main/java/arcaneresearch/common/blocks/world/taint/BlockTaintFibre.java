package arcaneresearch.common.blocks.world.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.init.ModEffects;
import arcaneresearch.init.ModSounds;

public class BlockTaintFibre extends Block implements ITaintBlock {

    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    public static final BooleanProperty HAS_CRYSTAL = BooleanProperty.create("has_crystal");

    protected static final VoxelShape SHAPE_DOWN = Block.box(0, 0, 0, 16, 1, 16);
    protected static final VoxelShape SHAPE_UP = Block.box(0, 15, 0, 16, 16, 16);
    protected static final VoxelShape SHAPE_NORTH = Block.box(0, 0, 0, 16, 16, 1);
    protected static final VoxelShape SHAPE_SOUTH = Block.box(0, 0, 15, 16, 16, 16);
    protected static final VoxelShape SHAPE_WEST = Block.box(0, 0, 0, 1, 16, 16);
    protected static final VoxelShape SHAPE_EAST = Block.box(15, 0, 0, 16, 16, 16);

    public BlockTaintFibre(BlockBehaviour.Properties props) {
        super(props
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(1.0f)
                .noOcclusion()
                .pushReaction(PushReaction.POPPED)
                .randomTicks()
                .ignitedByLava()
                .sound(ModSounds.GORE_TYPE));

        registerDefaultState(stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false)
                .setValue(HAS_CRYSTAL, false));
    }

    private static boolean canConnectTo(BlockGetter level, BlockPos pos, Direction face) {
        BlockPos adjacentPos = pos.relative(face);
        BlockState adjacentState = level.getBlockState(adjacentPos);
        if (adjacentState.getBlock() instanceof BlockTaintFibre
                || adjacentState.getBlock() instanceof BlockTaintFeature) {
            return false;
        }
        if (level instanceof LevelReader reader) {
            return adjacentState.isFaceSturdy(reader, adjacentPos, face.getOpposite());
        }
        return false;
    }

    public static int growthStage(BlockGetter level, BlockPos pos) {
        boolean down = canConnectTo(level, pos, Direction.DOWN);
        boolean up = canConnectTo(level, pos, Direction.UP);
        int growth = 0;
        java.util.Random rand = new java.util.Random(pos.asLong());
        int q = rand.nextInt(50);
        if (down) {
            if (q < 4) {
                growth = 1;
            } else if (q == 4 || q == 5) {
                growth = 2;
            } else if (q == 6) {
                growth = 3;
            }
        }
        if (up && q > 47) {
            growth = 4;
        }
        return growth;
    }

    private static boolean hasGrowth(BlockGetter level, BlockPos pos) {
        return growthStage(level, pos) != 0;
    }

    public static boolean isOnlyAdjacentToTaint(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos adjacent = pos.relative(dir);
            if (level.isEmptyBlock(adjacent)) {
                continue;
            }
            BlockState adjacentState = level.getBlockState(adjacent);
            if (!(adjacentState.getBlock() instanceof ITaintBlock)
                    && adjacentState.isFaceSturdy(level, adjacent, dir.getOpposite())) {
                return false;
            }
        }
        return true;
    }

    public static boolean isHemmedByTaint(Level level, BlockPos pos) {
        int c = 0;
        for (Direction dir : Direction.values()) {
            BlockPos adjacent = pos.relative(dir);
            BlockState adjacentState = level.getBlockState(adjacent);
            if (adjacentState.getBlock() instanceof ITaintBlock) {
                ++c;
            } else if (level.isEmptyBlock(adjacent)) {
                --c;
            } else if (adjacentState.getFluidState().isEmpty()
                    && !adjacentState.isFaceSturdy(level, adjacent, dir.getOpposite())) {
                --c;
            }
        }
        return c > 0;
    }

    public static int getAdjacentTaint(BlockGetter level, BlockPos pos) {
        int count = 0;
        for (Direction dir : Direction.values()) {
            if (!(level.getBlockState(pos.relative(dir)).getBlock() instanceof ITaintBlock)) {
                ++count;
            }
        }
        return count;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, HAS_CRYSTAL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = Shapes.empty();

        if (state.getValue(DOWN)) shape = Shapes.or(shape, SHAPE_DOWN);
        if (state.getValue(UP)) shape = Shapes.or(shape, SHAPE_UP);
        if (state.getValue(NORTH)) shape = Shapes.or(shape, SHAPE_NORTH);
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, SHAPE_SOUTH);
        if (state.getValue(WEST)) shape = Shapes.or(shape, SHAPE_WEST);
        if (state.getValue(EAST)) shape = Shapes.or(shape, SHAPE_EAST);

        if (shape.isEmpty()) {
            shape = SHAPE_DOWN;
        }

        return shape;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {

        return getShape(state, level, pos, context);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return getStateWithConnections(context.getLevel(), context.getClickedPos());
    }

    @Override
    public BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader level,
                                net.minecraft.world.level.ScheduledTickAccess tickAccess, BlockPos pos,
                                Direction direction, BlockPos neighborPos, BlockState neighborState,
                                net.minecraft.util.RandomSource random) {
        return getStateWithConnections(level, pos);
    }

    private BlockState getStateWithConnections(net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        boolean down = canConnectTo(level, pos, Direction.DOWN);
        boolean up = canConnectTo(level, pos, Direction.UP);
        boolean north = canConnectTo(level, pos, Direction.NORTH);
        boolean south = canConnectTo(level, pos, Direction.SOUTH);
        boolean west = canConnectTo(level, pos, Direction.WEST);
        boolean east = canConnectTo(level, pos, Direction.EAST);

        boolean hasCrystal = false;
        if (down) {
            hasCrystal = new java.util.Random(pos.asLong()).nextInt(50) == 6;
        }

        return defaultBlockState()
                .setValue(DOWN, down)
                .setValue(UP, up)
                .setValue(NORTH, north)
                .setValue(SOUTH, south)
                .setValue(WEST, west)
                .setValue(EAST, east)
                .setValue(HAS_CRYSTAL, hasCrystal);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                net.minecraft.world.entity.InsideBlockEffectApplier applier, boolean runningEffects) {

        if (entity.xo == entity.getX() && entity.zo == entity.getZ()) {
            return;
        }

        if (!level.isClientSide() && entity instanceof LivingEntity living) {
            if (!living.isInvertedHealAndHarm()) {
                if (level.getRandom().nextInt(750) == 0) {
                    living.addEffect(new MobEffectInstance(ModEffects.FLUX_TAINT, 200, 0, false, true));
                }
            }
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {

        if (!hasGrowth(level, pos) && isOnlyAdjacentToTaint(level, pos)) {
            die(level, pos, state);
            return;
        }

        if (!TaintHelper.isNearTaintSeed(level, pos)) {
            die(level, pos, state);
            return;
        }

        TaintHelper.spreadFibres(level, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block neighborBlock,
                                net.minecraft.world.level.redstone.Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);

        if (!level.isClientSide() && !hasGrowth(level, pos) && isOnlyAdjacentToTaint(level, pos)) {
            level.removeBlock(pos, false);
        }
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int eventId, int eventParam) {

        if (eventId == 1 && level.isClientSide()) {
            level.playSound(null, pos, SoundEvents.CHORUS_FLOWER_DEATH, SoundSource.BLOCKS,
                    0.1f, 0.9f + level.getRandom().nextFloat() * 0.2f);
            return true;
        }
        return super.triggerEvent(state, level, pos, eventId, eventParam);
    }

    @Override
    public void die(Level level, BlockPos pos, BlockState state) {
        level.removeBlock(pos, false);
    }

    @Override
    public void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, tool, dropExperience);

        if (state.getValue(HAS_CRYSTAL)) {
            int fortune = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                    level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FORTUNE), tool);
            if (level.getRandom().nextInt(5) <= fortune) {
                popResource(level, pos, ArcaneresearchApiHelper.makeCrystal(Aspect.FLUX));
            }

            AuraHelper.polluteAura(level, pos, 1.0f, true);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // TODO_PORT: @Override removed for 26.3 green build (signature mismatch, rework deferred).
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {

        int growth = growthStage(level, pos);
        if (growth == 3) {
            return 12;
        }
        if (growth == 2 || growth == 4) {
            return 6;
        }
        return 0;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 3;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 3;
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return true;
    }
}
