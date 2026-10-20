package arcaneresearch.common.blocks.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import arcaneresearch.common.tiles.essentia.TileSmelter;
import arcaneresearch.init.ModBlockEntities;

import javax.annotation.Nullable;

public class BlockSmelter extends Block implements EntityBlock {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    private final int smelterType;

    public BlockSmelter(BlockBehaviour.Properties props) {
        this(props, 0);
    }

    public BlockSmelter(BlockBehaviour.Properties props, int smelterType) {

        super(props
                .mapColor(MapColor.METAL)
                .strength(2.0f, 20.0f)
                .sound(SoundType.METAL)
                .lightLevel(state -> state.getValue(LIT) ? 13 : 0));
        this.smelterType = Math.max(0, Math.min(2, smelterType));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false));
    }

    public static BlockSmelter createBasic(BlockBehaviour.Properties props) {
        return new BlockSmelter(props, 0);
    }

    public static BlockSmelter createThaumium(BlockBehaviour.Properties props) {
        return new BlockSmelter(props, 1);
    }

    public static BlockSmelter createVoid(BlockBehaviour.Properties props) {
        return new BlockSmelter(props, 2);
    }

    // NEOFORGE_PORT: 26.x uses EnumProperty<Direction> instead of DirectionProperty.
    public static final net.minecraft.world.level.block.state.properties.EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(LIT, false);
    }

    @Override
    // NEOFORGE_PORT: Block.use(state, level, pos, player, hand, hit) removed in 26.x
    // -> useWithoutItem (hand unused).

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileSmelter smelter && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(smelter, buf -> buf.writeBlockPos(pos));
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                net.minecraft.world.level.redstone.Orientation orientation, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, orientation, isMoving);

        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileSmelter smelter) {
            smelter.checkNeighbours();
        }
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        if (id == 1 && level.isClientSide()) {
            return clientTriggerEvent(level, pos, param);
        }
        return super.triggerEvent(state, level, pos, id, param);
    }

    // NEOFORGE_PORT: Forge 1:1 — клиентский код вынесен в @OnlyIn-метод
    // (dist-безопасность: сервер не грузит client.fx.FXDispatcher).
    @OnlyIn(Dist.CLIENT)
    private boolean clientTriggerEvent(Level level, BlockPos pos, int param) {
        net.minecraft.core.Direction dir =
                net.minecraft.core.Direction.from3DDataValue(param);
        level.playLocalSound(pos.getX() + 0.5 + dir.getStepX(),
                pos.getY() + 0.5, pos.getZ() + 0.5 + dir.getStepZ(),
                net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH,
                net.minecraft.sounds.SoundSource.BLOCKS,
                0.25f, 2.6f + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.8f, false);
        for (int a = 0; a < 4; a++) {
            arcaneresearch.client.fx.FXDispatcher.INSTANCE.drawVentParticles(
                    pos.getX() + 0.5 + dir.getStepX(),
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5 + dir.getStepZ(),
                    0.1f - level.getRandom().nextFloat() * 0.2f,
                    0.1f - level.getRandom().nextFloat() * 0.2f,
                    0.1f - level.getRandom().nextFloat() * 0.2f,
                    11184810);
        }
        return true;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        // NEOFORGE_PORT: Block.onRemove removed in 26.x -> affectNeighborsAfterRemoval
        // (server-side removal hook with the old state + isMoving; the new state is
        // already in the chunk: level.getBlockState(pos)). Inventory drops are automatic
        // now (BlockEntity.preRemoveSideEffects default drops Container contents).
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
        if (!state.is(level.getBlockState(pos).getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            // NEOFORGE_PORT: inventory dropped by preRemoveSideEffects default; vis pollution kept.
            if (blockEntity instanceof TileSmelter smelter) {

                if (smelter.vis > 0) {
                    arcaneresearch.api.aura.AuraHelper.polluteAura(level, pos, smelter.vis, true);
                }
            }

        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) {
            Direction facing = state.getValue(FACING);
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.2 + random.nextFloat() * 0.3;
            double z = pos.getZ() + 0.5;

            double offset = 0.52;
            double spread = random.nextFloat() * 0.5 - 0.25;

            switch (facing) {
                case WEST -> {
                    level.addParticle(ParticleTypes.SMOKE, x - offset, y, z + spread, 0.0, 0.0, 0.0);
                    level.addParticle(ParticleTypes.FLAME, x - offset, y, z + spread, 0.0, 0.0, 0.0);
                }
                case EAST -> {
                    level.addParticle(ParticleTypes.SMOKE, x + offset, y, z + spread, 0.0, 0.0, 0.0);
                    level.addParticle(ParticleTypes.FLAME, x + offset, y, z + spread, 0.0, 0.0, 0.0);
                }
                case NORTH -> {
                    level.addParticle(ParticleTypes.SMOKE, x + spread, y, z - offset, 0.0, 0.0, 0.0);
                    level.addParticle(ParticleTypes.FLAME, x + spread, y, z - offset, 0.0, 0.0, 0.0);
                }
                case SOUTH -> {
                    level.addParticle(ParticleTypes.SMOKE, x + spread, y, z + offset, 0.0, 0.0, 0.0);
                    level.addParticle(ParticleTypes.FLAME, x + spread, y, z + offset, 0.0, 0.0, 0.0);
                }
            }
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos,
                                     net.minecraft.core.Direction direction) {

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileSmelter smelter) {
            int slots = smelter.getContainerSize();
            if (slots <= 0) {
                return 0;
            }
            float fill = 0.0f;
            for (int i = 0; i < slots; i++) {
                ItemStack stack = smelter.getItem(i);
                if (!stack.isEmpty()) {
                    fill += (float) stack.getCount() / stack.getMaxStackSize();
                }
            }
            return net.minecraft.util.Mth.floor(fill / slots * 14.0f) + (fill > 0 ? 1 : 0);
        }
        return 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        TileSmelter tile = new TileSmelter(pos, state);
        tile.setSmelterType(smelterType);
        return tile;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.SMELTER.get()) {
            if (!level.isClientSide()) {
                return (lvl, pos, st, be) -> TileSmelter.serverTick(lvl, pos, st, (TileSmelter) be);
            }
        }
        return null;
    }
}