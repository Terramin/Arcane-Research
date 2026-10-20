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
import net.minecraft.world.level.Level;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import arcaneresearch.common.tiles.essentia.TileSmelter;
import arcaneresearch.init.ModBlockEntities;
import javax.annotation.Nullable;
public class BlockSmelter extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private final int smelterType;
    public BlockSmelter() {
        this(0);
    }
    public BlockSmelter(int smelterType) {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(2.0f, 20.0f)
                .sound(SoundType.METAL)
                .lightLevel(state -> state.getValue(LIT) ? 13 : 0));
        this.smelterType = Math.max(0, Math.min(2, smelterType));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false));
    }
    public static BlockSmelter createBasic() {
        return new BlockSmelter(0);
    }
    public static BlockSmelter createThaumium() {
        return new BlockSmelter(1);
    }
    public static BlockSmelter createVoid() {
        return new BlockSmelter(2);
    }
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
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileSmelter smelter && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, smelter, pos);
        }
        return InteractionResult.CONSUME;
    }
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof TileSmelter smelter) {
            smelter.checkNeighbours();
        }
    }
    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        if (id == 1 && level.isClientSide) {
            return clientTriggerEvent(level, pos, param);
        }
        return super.triggerEvent(state, level, pos, id, param);
    }
    @OnlyIn(Dist.CLIENT)
    private boolean clientTriggerEvent(Level level, BlockPos pos, int param) {
        net.minecraft.core.Direction dir =
                net.minecraft.core.Direction.from3DDataValue(param);
        level.playLocalSound(pos.getX() + 0.5 + dir.getStepX(),
                pos.getY() + 0.5, pos.getZ() + 0.5 + dir.getStepZ(),
                net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH,
                net.minecraft.sounds.SoundSource.BLOCKS,
                0.25f, 2.6f + (level.random.nextFloat() - level.random.nextFloat()) * 0.8f, false);
        for (int a = 0; a < 4; a++) {
            arcaneresearch.client.fx.FXDispatcher.INSTANCE.drawVentParticles(
                    pos.getX() + 0.5 + dir.getStepX(),
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5 + dir.getStepZ(),
                    0.1f - level.random.nextFloat() * 0.2f,
                    0.1f - level.random.nextFloat() * 0.2f,
                    0.1f - level.random.nextFloat() * 0.2f,
                    11184810);
        }
        return true;
    }
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TileSmelter smelter) {
                for (int i = 0; i < smelter.getContainerSize(); i++) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), smelter.getItem(i));
                }
                if (smelter.vis > 0) {
                    arcaneresearch.api.aura.AuraHelper.polluteAura(level, pos, smelter.vis, true);
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
    @Override
    @OnlyIn(Dist.CLIENT)
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
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
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
            if (!level.isClientSide) {
                return (lvl, pos, st, be) -> TileSmelter.serverTick(lvl, pos, st, (TileSmelter) be);
            }
        }
        return null;
    }
}
