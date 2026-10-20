package arcaneresearch.common.blocks.basic;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.tiles.misc.TileBarrierStone;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModBlocks;

import javax.annotation.Nullable;
import java.util.List;

public class BlockPavingStone extends Block implements EntityBlock {

    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);
    private final Type type;

    public BlockPavingStone(Properties properties, Type type) {
        super(properties);
        this.type = type;
    }

    public static BlockPavingStone createNormal(BlockBehaviour.Properties props) {
        return new BlockPavingStone(
                props
                        .mapColor(MapColor.STONE)
                        .strength(2.5f)
                        .sound(SoundType.STONE),
                Type.NORMAL
        );
    }

    public static BlockPavingStone createTravel(BlockBehaviour.Properties props) {
        return new BlockPavingStone(
                props
                        .mapColor(MapColor.STONE)
                        .strength(2.5f)
                        .sound(SoundType.STONE),
                Type.TRAVEL
        );
    }

    public static BlockPavingStone createBarrier(BlockBehaviour.Properties props) {
        return new BlockPavingStone(
                props
                        .mapColor(MapColor.STONE)
                        .strength(2.5f)
                        .sound(SoundType.STONE)
                        .randomTicks(),
                Type.BARRIER
        );
    }

    public Type getType() {
        return type;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {

        if (!level.isClientSide() && type == Type.TRAVEL && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 1, false, false));
            living.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 40, 0, false, false));
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (type == Type.BARRIER) {

            if (level.hasNeighborSignal(pos)) {

                for (int i = 0; i < 4; i++) {
                    FXDispatcher.INSTANCE.blockRunes(
                            pos.getX(), pos.getY() + 0.7f, pos.getZ(),
                            0.2f + random.nextFloat() * 0.4f,
                            random.nextFloat() * 0.3f,
                            0.8f + random.nextFloat() * 0.2f,
                            20, -0.02f
                    );
                }
            } else {

                AABB area = new AABB(pos).inflate(1.0);
                List<Entity> entities = level.getEntities(null, area);

                for (Entity entity : entities) {
                    if (entity instanceof LivingEntity && !(entity instanceof Player)) {

                        FXDispatcher.INSTANCE.blockRunes(
                                pos.getX(), pos.getY() + 0.6f + random.nextFloat() * Math.max(0.8f, entity.getEyeHeight()),
                                pos.getZ(),
                                0.6f + random.nextFloat() * 0.4f,
                                0.0f,
                                0.3f + random.nextFloat() * 0.7f,
                                20, 0.0f
                        );
                        break;
                    }
                }

                if (entities.stream().noneMatch(e -> e instanceof LivingEntity && !(e instanceof Player))) {
                    BlockState above1 = level.getBlockState(pos.above(1));
                    BlockState above2 = level.getBlockState(pos.above(2));

                    if (above1.is(ModBlocks.BARRIER.get()) || above2.is(ModBlocks.BARRIER.get())) {
                        for (int i = 0; i < 6; i++) {
                            FXDispatcher.INSTANCE.blockRunes(
                                    pos.getX(), pos.getY() + 0.7f, pos.getZ(),
                                    0.9f + random.nextFloat() * 0.1f,
                                    random.nextFloat() * 0.3f,
                                    random.nextFloat() * 0.3f,
                                    24, -0.02f
                            );
                        }
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (type == Type.BARRIER) {
            return new TileBarrierStone(pos, state);
        }
        return null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (type == Type.BARRIER && blockEntityType == ModBlockEntities.BARRIER_STONE.get()) {
            if (!level.isClientSide()) {
                return (lvl, pos, st, be) -> TileBarrierStone.serverTick(lvl, pos, st, (TileBarrierStone) be);
            }
        }
        return null;
    }

    public enum Type {
        NORMAL,
        TRAVEL,
        BARRIER
    }
}