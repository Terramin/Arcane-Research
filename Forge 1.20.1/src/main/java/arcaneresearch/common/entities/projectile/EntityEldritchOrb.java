package arcaneresearch.common.entities.projectile;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import arcaneresearch.init.ModEntities;
import java.util.List;
public class EntityEldritchOrb extends ThrowableProjectile {
    public EntityEldritchOrb(EntityType<? extends EntityEldritchOrb> type, Level level) {
        super(type, level);
    }
    public EntityEldritchOrb(Level level, LivingEntity owner) {
        super(ModEntities.ELDRITCH_ORB.get(), owner, level);
        shootFromRotation(owner, owner.getXRot(), owner.getYRot(), -5.0f, 0.75f, 0.0f);
    }
    @Override
    protected void defineSynchedData() {
    }
    @Override
    protected float getGravity() {
        return 0.0f;
    }
    @Override
    public void tick() {
        super.tick();
        if (tickCount > 100) {
            discard();
            return;
        }
        if (level().isClientSide) {
            for (int i = 0; i < 2; i++) {
                level().addParticle(ParticleTypes.PORTAL,
                        getX() + (random.nextDouble() - 0.5) * 0.5,
                        getY() + (random.nextDouble() - 0.5) * 0.5,
                        getZ() + (random.nextDouble() - 0.5) * 0.5,
                        (random.nextDouble() - 0.5) * 0.2,
                        (random.nextDouble() - 0.5) * 0.2,
                        (random.nextDouble() - 0.5) * 0.2);
            }
        }
    }
    @Override
    protected void onHit(HitResult result) {
        if (!level().isClientSide) {
            Entity owner = getOwner();
            AABB aoe = getBoundingBox().inflate(2.0);
            List<Entity> entities = level().getEntities(owner, aoe);
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity living) {
                    if (living.isInvertedHealAndHarm()) continue;
                    float damage = 4.0f;
                    if (owner instanceof LivingEntity livingOwner) {
                        damage = (float) (livingOwner.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.666);
                    }
                    DamageSource source = level().damageSources().indirectMagic(this, owner);
                    living.hurt(source, damage);
                    try {
                        living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 0));
                    } catch (Exception ignored) {
                    }
                }
            }
            playSound(SoundEvents.LAVA_EXTINGUISH, 0.5f, 2.6f + (random.nextFloat() - random.nextFloat()) * 0.8f);
            discard();
        }
        if (level().isClientSide) {
            for (int i = 0; i < 20; i++) {
                level().addParticle(ParticleTypes.PORTAL,
                        getX(), getY(), getZ(),
                        (random.nextDouble() - 0.5) * 0.5,
                        (random.nextDouble() - 0.5) * 0.5,
                        (random.nextDouble() - 0.5) * 0.5);
            }
        }
    }
}
