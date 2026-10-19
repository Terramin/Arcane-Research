package arcaneresearch.common.blocks.aura;
import arcaneresearch.common.tiles.aura.EldritchCrabTile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty; // NEOFORGE_PORT: EnumProperty<Direction> folded into EnumProperty<Direction>
import arcaneresearch.common.blocks.aura.AuraBlockEntities;

import javax.annotation.Nullable;

/**
 * Крабовый спавнер, порт TC4 blockEldritch meta 9 (TileEldritchCrabSpawner).
 * Нерушимый, смотрит в сторону размещения. Лежит в arcaneresearch.arcaneresearch.common.blocks.aura.
 */
public class EldritchCrabBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class, 
            Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);

    public EldritchCrabBlock(BlockBehaviour.Properties props) {
        super(props
                .strength(15.0F, 30.0F)
                .sound(SoundType.STONE)
                .lightLevel(state -> 4)
                .noLootTable());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        try {
            Direction facing = context.getHorizontalDirection().getOpposite();
            return this.defaultBlockState().setValue(FACING, facing);
        } catch (Exception e) {
            return this.defaultBlockState();
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
                            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        try {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof EldritchCrabTile crab && state.hasProperty(FACING)) {
                crab.setFacing(state.getValue(FACING));
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        try {
            if (!level.isClientSide()) {
                return super.triggerEvent(state, level, pos, id, param);
            }
            BlockEntity tile = level.getBlockEntity(pos);
            if (id == 1 && tile instanceof EldritchCrabTile crab) {
                crab.onVentEvent();
                return true;
            }
        } catch (Exception ignored) {
        }
        return super.triggerEvent(state, level, pos, id, param);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        try {
            return new EldritchCrabTile(AuraBlockEntities.ELDRITCH_CRAB.get(), pos, state);
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
            if (level.isClientSide()) {
                return createTickerHelper(type, AuraBlockEntities.ELDRITCH_CRAB.get(),
                        EldritchCrabTile::clientTick);
            }
            return createTickerHelper(type, AuraBlockEntities.ELDRITCH_CRAB.get(),
                    EldritchCrabTile::serverTick);
        } catch (Exception e) {
            return null;
        }
    }
}
