package arcaneresearch.common.blocks.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty; // NEOFORGE_PORT: EnumProperty<Direction> folded into EnumProperty<Direction>
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.common.tiles.devices.TileInfernalFurnace;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModBlocks;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class BlockInfernalFurnace extends Block implements EntityBlock {

    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 8, 16);
    public static boolean ignore = false;

    public BlockInfernalFurnace(BlockBehaviour.Properties props) {
        super(props
                .mapColor(MapColor.NETHER)
                .strength(5.0f, 100.0f)
                .sound(SoundType.STONE)
                .lightLevel(state -> 14)
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }

    public static void destroyFurnace(Level level, BlockPos pos, BlockState state, BlockPos startpos) {
        if (ignore || level.isClientSide()) {
            return;
        }
        ignore = true;
        try {
            for (int a = -1; a <= 1; ++a) {
                for (int b = -1; b <= 1; ++b) {
                    for (int c = -1; c <= 1; ++c) {
                        BlockPos p = pos.offset(a, b, c);
                        if (p.equals(startpos)) {
                            continue;
                        }
                        BlockState bs = level.getBlockState(p);
                        if (bs.is(ModBlocks.PLACEHOLDER_NETHERBRICK.get())) {
                            level.setBlock(p, Blocks.NETHER_BRICKS.defaultBlockState(), 3);
                        }
                        if (bs.is(ModBlocks.PLACEHOLDER_OBSIDIAN.get())) {
                            level.setBlock(p, Blocks.OBSIDIAN.defaultBlockState(), 3);
                        }
                    }
                }
            }
            Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.NORTH;
            BlockPos mouthPos = pos.relative(facing.getOpposite());
            if (level.isEmptyBlock(mouthPos)) {
                level.setBlock(mouthPos, Blocks.IRON_BARS.defaultBlockState(), 3);
            }
            level.setBlock(pos, Blocks.LAVA.defaultBlockState(), 3);
        } finally {
            ignore = false;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rot) {

        return state;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {

        return RenderShape.MODEL;
    }

    @Override
    // NEOFORGE_PORT: Block.use(state, level, pos, player, hand, hit) removed in 26.x
    // -> useItemOn (uses held item/hand).

    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileInfernalFurnace furnace) {
            ItemStack heldItem = player.getItemInHand(hand);
            if (!heldItem.isEmpty()) {

                ItemStack remaining = furnace.addItemsToInventory(heldItem.copy());
                if (remaining.isEmpty() || remaining.getCount() < heldItem.getCount()) {
                    if (!player.getAbilities().instabuild) {
                        heldItem.setCount(remaining.isEmpty() ? 0 : remaining.getCount());
                    }
                    return InteractionResult.CONSUME;
                }
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {

        return Collections.emptyList();
    }

    @Override
    public ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        // Колесо по горнилу в креативе отдаёт стройблок (обсидиан),
        // а не саму печь: центр встал на место лавы/жерла.
        return new ItemStack(Blocks.OBSIDIAN);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        // NEOFORGE_PORT: Block.onRemove removed in 26.x -> affectNeighborsAfterRemoval
        // (server-side removal hook with the old state + isMoving; the new state is
        // already in the chunk: level.getBlockState(pos)). Inventory drops are automatic
        // now (BlockEntity.preRemoveSideEffects default drops Container contents).
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
        if (!state.is(level.getBlockState(pos).getBlock())) {
            // NEOFORGE_PORT: inventory dropped by preRemoveSideEffects default; multiblock teardown kept.
            destroyFurnace(level, pos, state, pos);

        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Direction facing = state.getValue(FACING);
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.1;
        double z = pos.getZ() + 0.5;

        Direction opposite = facing.getOpposite();
        double offsetX = opposite.getStepX() * 0.6;
        double offsetZ = opposite.getStepZ() * 0.6;

        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.LARGE_SMOKE,
                    x + offsetX + (random.nextDouble() - 0.5) * 0.3,
                    y + 0.5 + random.nextDouble() * 0.3,
                    z + offsetZ + (random.nextDouble() - 0.5) * 0.3,
                    0, 0.02, 0);
        }

        if (random.nextInt(2) == 0) {
            level.addParticle(ParticleTypes.FLAME,
                    x + offsetX + (random.nextDouble() - 0.5) * 0.2,
                    y + 0.3 + random.nextDouble() * 0.2,
                    z + offsetZ + (random.nextDouble() - 0.5) * 0.2,
                    0, 0.01, 0);
        }

        if (random.nextInt(10) == 0) {
            level.addParticle(ParticleTypes.DRIPPING_LAVA,
                    x + (random.nextDouble() - 0.5) * 0.5,
                    y,
                    z + (random.nextDouble() - 0.5) * 0.5,
                    0, 0, 0);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                net.minecraft.world.entity.InsideBlockEffectApplier applier, boolean runningEffects) {
        if (entity.getX() < pos.getX() + 0.3) {
            entity.setDeltaMovement(entity.getDeltaMovement().add(0.0001, 0, 0));
        }
        if (entity.getX() > pos.getX() + 0.7) {
            entity.setDeltaMovement(entity.getDeltaMovement().add(-0.0001, 0, 0));
        }
        if (entity.getZ() < pos.getZ() + 0.3) {
            entity.setDeltaMovement(entity.getDeltaMovement().add(0, 0, 0.0001));
        }
        if (entity.getZ() > pos.getZ() + 0.7) {
            entity.setDeltaMovement(entity.getDeltaMovement().add(0, 0, -0.0001));
        }
        if (!level.isClientSide() && entity.tickCount % 10 == 0) {
            if (entity instanceof ItemEntity itemEntity) {
                itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().x, 0.025, itemEntity.getDeltaMovement().z);
                if (itemEntity.onGround()) {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof TileInfernalFurnace furnace) {
                        itemEntity.setItem(furnace.addItemsToInventory(itemEntity.getItem()));
                    }
                }
            } else if (entity instanceof LivingEntity living && !living.fireImmune()) {
                DamageSources sources = level.damageSources();
                entity.hurt(sources.lava(), 3.0F);
                // NEOFORGE_PORT (26.3): setSecondsOnFire(10) == igniteForSeconds(10): only raises
                // the fire timer and applies Fire Protection (BURNING_TIME), unlike setRemainingFireTicks.
                entity.igniteForSeconds(10.0F);
            }
        }
        super.entityInside(state, level, pos, entity, applier, runningEffects);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileInfernalFurnace(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.INFERNAL_FURNACE.get()) {
            if (level.isClientSide()) {
                return null;
            } else {
                return (lvl, pos, st, be) -> TileInfernalFurnace.serverTick(lvl, pos, st, (TileInfernalFurnace) be);
            }
        }
        return null;
    }
}
