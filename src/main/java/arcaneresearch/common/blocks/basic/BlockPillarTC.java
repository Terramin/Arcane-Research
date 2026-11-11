package arcaneresearch.common.blocks.basic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.List;

public class BlockPillarTC extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    // Geometry of the pillar model in its unrotated (facing=NORTH) orientation.
    // The pillar is NOT symmetric in X/Z (the curved top leans toward the +X/+Z
    // corner), so the shape must be rotated per FACING to follow the blockstate
    // "y" rotation of the model.
    private static final double[][] COLLISION_BOXES = {
            {0, 0, 0, 16, 8, 16},       // lower platform
            {2, 8, 2, 14, 16, 14},      // shaft
            {4, 16, 4, 15, 27, 15},     // upper shaft
            {11, 26, 11, 17, 33, 17},   // curved top cap
    };

    private static final VoxelShape SELECTION_SHAPE = Block.box(0, 0, 0, 16, 8, 16);

    private static final VoxelShape COLLISION_NORTH;
    private static final VoxelShape COLLISION_SOUTH;
    private static final VoxelShape COLLISION_EAST;
    private static final VoxelShape COLLISION_WEST;

    static {
        COLLISION_NORTH = buildCollisionShape(Direction.NORTH);
        COLLISION_SOUTH = buildCollisionShape(Direction.SOUTH);
        COLLISION_EAST = buildCollisionShape(Direction.EAST);
        COLLISION_WEST = buildCollisionShape(Direction.WEST);
    }

    private static VoxelShape buildCollisionShape(Direction facing) {
        VoxelShape result = Shapes.empty();
        for (double[] b : COLLISION_BOXES) {
            double[] r = rotateBox(b, facing);
            result = Shapes.or(result, Block.box(r[0], r[1], r[2], r[3], r[4], r[5]));
        }
        return result;
    }

    // Rotate a corner-referenced box (in 0..16 shape units) about the block's
    // vertical center axis, matching the blockstate "y" rotation applied to the
    // model (clockwise when viewed from above, i.e. north -> east for 90 deg).
    private static double[] rotateBox(double[] b, Direction facing) {
        double x1 = b[0], y1 = b[1], z1 = b[2];
        double x2 = b[3], y2 = b[4], z2 = b[5];
        switch (facing) {
            case NORTH:
                return new double[]{x1, y1, z1, x2, y2, z2};
            case EAST:
                return new double[]{16 - z2, y1, x1, 16 - z1, y2, x2};
            case SOUTH:
                return new double[]{16 - x2, y1, 16 - z2, 16 - x1, y2, 16 - z1};
            case WEST:
                return new double[]{z1, y1, 16 - x2, z2, y2, 16 - x1};
            default:
                return new double[]{x1, y1, z1, x2, y2, z2};
        }
    }

    public BlockPillarTC(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.defaultBlockState()
                        .setValue(FACING, Direction.NORTH)
        );
    }
    public static BlockPillarTC create() {
        return new BlockPillarTC(
                Properties.of()
                        .mapColor(MapColor.STONE)
                        .strength(2.5f, 10.0f)
                        .sound(SoundType.STONE)
        );
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SELECTION_SHAPE;
    }
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        switch (facing) {
            case SOUTH: return COLLISION_SOUTH;
            case EAST: return COLLISION_EAST;
            case WEST: return COLLISION_WEST;
            default: return COLLISION_NORTH;
        }
    }
    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    @Override
    public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder params) {
        Block stone;
        if (this == arcaneresearch.init.ModBlocks.ARCANE_PILLAR.get()) {
            stone = arcaneresearch.init.ModBlocks.ARCANE_STONE.get();
        } else if (this == arcaneresearch.init.ModBlocks.ANCIENT_PILLAR.get()) {
            stone = arcaneresearch.init.ModBlocks.ANCIENT_STONE.get();
        } else if (this == arcaneresearch.init.ModBlocks.ELDRITCH_PILLAR.get()) {
            stone = arcaneresearch.init.ModBlocks.ELDRITCH_STONE_TILE.get();
        } else {
            return super.getDrops(state, params);
        }
        return List.of(new ItemStack(stone, 2));
    }
}
