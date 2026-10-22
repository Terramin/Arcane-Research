package arcaneresearch.common.blocks.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty; 
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.common.tiles.crafting.TilePatternCrafter;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModSounds;

import javax.annotation.Nullable;

public class BlockPatternCrafter extends Block implements EntityBlock {

    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    public BlockPatternCrafter(BlockBehaviour.Properties props) {
        super(props
                .mapColor(MapColor.METAL)
                .strength(2.0f)
                .sound(SoundType.METAL)
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ENABLED, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ENABLED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {

        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection())
                .setValue(ENABLED, true);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    
    

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TilePatternCrafter crafter) {

            Vec3 rel = hit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
            if (!isLeverHit(rel, state.getValue(FACING))) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide()) {
                crafter.cycle();
                level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        ModSounds.KEY.get(), SoundSource.BLOCKS, 0.5f, 1.0f);
            }
            return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
        }
        return InteractionResult.PASS;
    }

    private boolean isLeverHit(Vec3 hit, Direction facing) {
        if (hit.y < 0.125 || hit.y > 0.375) return false;
        return switch (facing) {
            case NORTH -> hit.z >= 0.75 && hit.z <= 0.875 && hit.x >= 0.375 && hit.x <= 0.625;
            case SOUTH -> hit.z >= 0.125 && hit.z <= 0.25 && hit.x >= 0.375 && hit.x <= 0.625;
            case WEST -> hit.x >= 0.75 && hit.x <= 0.875 && hit.z >= 0.375 && hit.z <= 0.625;
            case EAST -> hit.x >= 0.125 && hit.x <= 0.25 && hit.z >= 0.375 && hit.z <= 0.625;
            default -> false;
        };
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block block,
                                net.minecraft.world.level.redstone.Orientation orientation, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, orientation, isMoving);

        boolean powered = level.hasNeighborSignal(pos);
        if (powered != !state.getValue(ENABLED)) {
            level.setBlock(pos, state.setValue(ENABLED, !powered), 3);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TilePatternCrafter(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (type == ModBlockEntities.PATTERN_CRAFTER.get()) {
            if (level.isClientSide()) {
                return (lvl, pos, st, be) -> TilePatternCrafter.clientTick(lvl, pos, st, (TilePatternCrafter) be);
            } else {
                return (lvl, pos, st, be) -> TilePatternCrafter.serverTick(lvl, pos, st, (TilePatternCrafter) be);
            }
        }
        return null;
    }
}
