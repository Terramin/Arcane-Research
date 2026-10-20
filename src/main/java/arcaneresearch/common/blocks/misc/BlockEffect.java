package arcaneresearch.common.blocks.misc;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.entities.IEldritchMob;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.init.ModSounds;
public class BlockEffect extends Block {
    private final EffectType effectType;
    public BlockEffect(EffectType type) {
        super(BlockBehaviour.Properties.of()
                .replaceable()
                .noCollission()
                .instabreak()
                .explosionResistance(999.0f)
                .noLootTable()
                .air()
                .lightLevel(state -> type == EffectType.GLIMMER ? 15 : 7)
                .randomTicks()
                .pushReaction(PushReaction.DESTROY));
        this.effectType = type;
    }
    public static BlockEffect createSap() {
        return new BlockEffect(EffectType.SAP);
    }
    public static BlockEffect createShock() {
        return new BlockEffect(EffectType.SHOCK);
    }
    public static BlockEffect createGlimmer() {
        return new BlockEffect(EffectType.GLIMMER);
    }
    public EffectType getEffectType() {
        return effectType;
    }
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide) return;
        if (effectType == EffectType.SHOCK) {
            if (entity instanceof LivingEntity living) {
                entity.hurt(level.damageSources().magic(), 1.0f);
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0, true, true));
            }
            if (level.random.nextInt(100) == 0) {
                level.removeBlock(pos, false);
            }
        } else if (effectType == EffectType.SAP) {
            if (entity instanceof IEldritchMob) return;
            if (entity instanceof LivingEntity living && !living.hasEffect(MobEffects.WITHER)) {
                living.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0, true, true));
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, true, true));
                living.addEffect(new MobEffectInstance(MobEffects.HUNGER, 40, 1, true, true));
            }
        }
    }
    @Override
    public void randomTick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, RandomSource random) {
        if (effectType != EffectType.GLIMMER) {
            level.removeBlock(pos, false);
        }
    }
    @Override
    public void tick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, RandomSource random) {
        randomTick(state, level, pos, random);
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (effectType != EffectType.GLIMMER) {
            float h = random.nextFloat() * 0.33f;
            if (effectType == EffectType.SHOCK) {
                FXDispatcher.INSTANCE.spark(pos.getX() + random.nextFloat(), pos.getY() + 0.1515f + h / 2.0f,
                        pos.getZ() + random.nextFloat(), 3.0f + h * 6.0f,
                        0.65f + random.nextFloat() * 0.1f, 1.0f, 1.0f, 0.8f);
            } else {
                FXDispatcher.INSTANCE.spark(pos.getX() + random.nextFloat(), pos.getY() + 0.1515f + h / 2.0f,
                        pos.getZ() + random.nextFloat(), 3.0f + h * 6.0f,
                        0.3f - random.nextFloat() * 0.1f, 0.0f, 0.5f + random.nextFloat() * 0.2f, 1.0f);
            }
            if (random.nextInt(50) == 0 && ModSounds.JACOBS.get() != null) {
                level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), ModSounds.JACOBS.get(),
                        SoundSource.AMBIENT, 0.25f, 1.0f + (random.nextFloat() - random.nextFloat()) * 0.2f, false);
            }
        }
    }
    public enum EffectType {
        SAP,
        SHOCK,
        GLIMMER
    }
}
