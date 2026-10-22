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
import net.minecraft.world.level.block.state.properties.EnumProperty; 
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class BlockPillarTC extends Block {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 8, 16);
    private static final VoxelShape COLLISION_SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16),
            Block.box(0, 16, 0, 16, 32, 16));

    public BlockPillarTC(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.defaultBlockState()
                        .setValue(FACING, Direction.NORTH)
        );
    }

    public static BlockPillarTC create(net.minecraft.world.level.block.state.BlockBehaviour.Properties props) {

        return new BlockPillarTC(
                props
                        .mapColor(MapColor.STONE)
                        .strength(2.5f, 10.0f)
                        .sound(SoundType.STONE)
        );
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {

        return PushReaction.IMMOVEABLE;
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

        Block self = this;
        Block stone = null;
        try {
            if (self == arcaneresearch.init.ModBlocks.ARCANE_PILLAR.get()) {
                stone = arcaneresearch.init.ModBlocks.ARCANE_STONE.get();
            } else if (self == arcaneresearch.init.ModBlocks.ANCIENT_PILLAR.get()) {
                stone = arcaneresearch.init.ModBlocks.ANCIENT_STONE.get();
            } else if (self == arcaneresearch.init.ModBlocks.ELDRITCH_PILLAR.get()) {
                stone = arcaneresearch.init.ModBlocks.ELDRITCH_STONE_TILE.get();
            }
        } catch (Exception ignored) {
        }
        if (stone != null) {
            return List.of(new ItemStack(stone, 2));
        }
        return super.getDrops(state, params);
    }
}
