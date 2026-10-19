package arcaneresearch.common.blocks.world.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.common.entities.monster.EntityThaumicSlime;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModEffects;
import arcaneresearch.init.ModEntities;
import arcaneresearch.init.ModSounds;

public class BlockFluxGoo extends Block implements ITaintBlock {

    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 7);

    protected static final VoxelShape[] SHAPES = new VoxelShape[]{
            Block.box(0, 0, 0, 16, 2, 16),
            Block.box(0, 0, 0, 16, 4, 16),
            Block.box(0, 0, 0, 16, 6, 16),
            Block.box(0, 0, 0, 16, 8, 16),
            Block.box(0, 0, 0, 16, 10, 16),
            Block.box(0, 0, 0, 16, 12, 16),
            Block.box(0, 0, 0, 16, 14, 16),
            Block.box(0, 0, 0, 16, 16, 16)
    };

    public BlockFluxGoo(BlockBehaviour.Properties props) {
        super(props
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(-1.0f, 100.0f)
                .noOcclusion()
                .pushReaction(PushReaction.POPPED)
                .randomTicks()
                .noCollision()
                .sound(ModSounds.GORE_TYPE));
        registerDefaultState(stateDefinition.any().setValue(LEVEL, 7));
    }

    public static BlockState withLevel(Block block, int level) {
        return block.defaultBlockState().setValue(LEVEL, Math.min(7, Math.max(0, level)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(LEVEL)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                net.minecraft.world.entity.InsideBlockEffectApplier applier, boolean runningEffects) {
        int gooLevel = state.getValue(LEVEL);

        if (entity instanceof EntityThaumicSlime slime) {
            if (slime.getSize() < gooLevel && level.getRandom().nextBoolean()) {
                slime.setSize(slime.getSize() + 1, true);
                if (gooLevel > 1) {
                    level.setBlock(pos, state.setValue(LEVEL, gooLevel - 1), 2);
                } else {
                    level.removeBlock(pos, false);
                }
            }
            return;
        }

        float slowFactor = 1.0f - (gooLevel / 8.0f);
        Vec3 motion = entity.getDeltaMovement();
        entity.setDeltaMovement(motion.x * slowFactor, motion.y, motion.z * slowFactor);

        if (entity instanceof LivingEntity living) {
            MobEffectInstance effect = new MobEffectInstance(
                    ModEffects.VIS_EXHAUST,
                    600,
                    gooLevel / 3,
                    true,
                    true
            );
            // TODO_PORT: incurable effect (getCurativeItems gone in 26.3); rework deferred.
            living.addEffect(effect);
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int gooLevel = state.getValue(LEVEL);

        if (gooLevel >= 2 && gooLevel < 6 && level.isEmptyBlock(pos.above()) && random.nextInt(50) == 0) {
            level.removeBlock(pos, false);
            EntityThaumicSlime slime = new EntityThaumicSlime(ModEntities.THAUMIC_SLIME.get(), level);
            slime.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            slime.setYRot(0.0f); slime.setXRot(0.0f);;
            slime.setSize(1, true);
            level.addFreshEntity(slime);
            level.playSound(null, pos, ModSounds.GORE.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            return;
        }

        if (gooLevel >= 6 && level.isEmptyBlock(pos.above()) && random.nextInt(50) == 0) {
            level.removeBlock(pos, false);
            EntityThaumicSlime slime = new EntityThaumicSlime(ModEntities.THAUMIC_SLIME.get(), level);
            slime.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            slime.setYRot(0.0f); slime.setXRot(0.0f);;
            slime.setSize(2, true);
            level.addFreshEntity(slime);
            level.playSound(null, pos, ModSounds.GORE.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            return;
        }

        if (gooLevel > 0 && random.nextInt(3) == 0) {
            Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            BlockPos side = pos.relative(dir);
            BlockState sideState = level.getBlockState(side);
            if ((sideState.isAir() || sideState.canBeReplaced())
                    && !(sideState.is(this))) {
                level.setBlock(side, state.setValue(LEVEL, gooLevel - 1), 3);
            }
        }

        if (random.nextInt(4) == 0) {
            if (gooLevel == 0) {

                if (random.nextBoolean()) {
                    AuraHelper.polluteAura(level, pos, 1.0f, true);
                    level.removeBlock(pos, false);
                } else {

                    level.setBlock(pos, ModBlocks.TAINT_FIBRE.get().defaultBlockState(), 3);
                }
            } else {

                level.setBlock(pos, state.setValue(LEVEL, gooLevel - 1), 2);
                AuraHelper.polluteAura(level, pos, 1.0f, true);
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader level,
                                net.minecraft.world.level.ScheduledTickAccess tickAccess, BlockPos pos,
                                Direction direction, BlockPos neighborPos, BlockState neighborState,
                                net.minecraft.util.RandomSource random) {

        if (direction == Direction.DOWN && neighborState.isAir()) {

            tickAccess.scheduleTick(pos, this, 5);
        }
        return state;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {

        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);

        if (belowState.isAir()) {

            level.setBlock(below, state, 3);
            level.removeBlock(pos, false);
        } else if (belowState.is(this)) {

            int currentLevel = state.getValue(LEVEL);
            int belowLevel = belowState.getValue(LEVEL);
            int totalLevel = currentLevel + belowLevel + 2;

            if (totalLevel <= 7) {
                level.setBlock(below, belowState.setValue(LEVEL, totalLevel), 3);
                level.removeBlock(pos, false);
            } else {
                level.setBlock(below, belowState.setValue(LEVEL, 7), 3);
                int remaining = totalLevel - 8;
                if (remaining > 0) {
                    level.setBlock(pos, state.setValue(LEVEL, remaining), 3);
                } else {
                    level.removeBlock(pos, false);
                }
            }
        }
    }

    @Override
    public boolean canBeReplaced(BlockState state, net.minecraft.world.item.context.BlockPlaceContext context) {
        return state.getValue(LEVEL) < 4;
    }

    @Override
    public void die(Level level, BlockPos pos, BlockState state) {
        level.removeBlock(pos, false);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int meta = state.getValue(LEVEL);
        if (random.nextInt(44) <= meta) {
            level.addParticle(ParticleTypes.CRIMSON_SPORE,
                    pos.getX() + random.nextFloat(),
                    pos.getY() + 0.125 * meta,
                    pos.getZ() + random.nextFloat(),
                    0.0, 0.01, 0.0);
        }
    }
}
