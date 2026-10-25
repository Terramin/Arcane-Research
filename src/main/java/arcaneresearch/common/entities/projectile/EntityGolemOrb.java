package arcaneresearch.common.entities.projectile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.init.ModEntities;
public class EntityGolemOrb extends ThrowableProjectile {
    private static final EntityDataAccessor<Boolean> DATA_RED =
            SynchedEntityData.defineId(EntityGolemOrb.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_TARGET_ID =
            SynchedEntityData.defineId(EntityGolemOrb.class, EntityDataSerializers.INT);
    private LivingEntity target = null;
    public EntityGolemOrb(EntityType<? extends EntityGolemOrb> type, Level level) {
        super(type, level);
    }
    public EntityGolemOrb(Level level, LivingEntity owner, LivingEntity target, boolean red) {
        super(ModEntities.GOLEM_ORB.get(), owner, level);
        this.target = target;
        entityData.set(DATA_RED, red);
        entityData.set(DATA_TARGET_ID, target.getId());
    }
    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_RED, false);
        entityData.define(DATA_TARGET_ID, -1);
    }
    public boolean isRed() {
        return entityData.get(DATA_RED);
    }
    @Override
    protected float getGravity() {
        return 0.0f;
    }
    @Override
    public void tick() {
        super.tick();
        if (target == null) {
            int targetId = entityData.get(DATA_TARGET_ID);
            if (targetId >= 0) {
                Entity e = level().getEntity(targetId);
                if (e instanceof LivingEntity le) {
                    target = le;
                }
            }
        }
        int maxLife = isRed() ? 240 : 160;
        if (tickCount > maxLife) {
            discard();
            return;
        }
        if (target != null && target.isAlive()) {
            double dist = Math.sqrt(distanceToSqr(target));
            double dx = target.getX() - getX();
            double dy = target.getY() + target.getBbHeight() * 0.6 - getY();
            double dz = target.getZ() - getZ();
            double accel = 0.2;
            dx = dx / dist * accel;
            dy = dy / dist * accel;
            dz = dz / dist * accel;
            Vec3 motion = getDeltaMovement();
            setDeltaMovement(
                    Mth.clamp(motion.x + dx, -0.25, 0.25),
                    Mth.clamp(motion.y + dy, -0.25, 0.25),
                    Mth.clamp(motion.z + dz, -0.25, 0.25)
            );
        }
    }
    @Override
    protected void onHit(HitResult result) {
        if (result.getType() == HitResult.Type.ENTITY) {
            onHitEntity((EntityHitResult) result);
        }
        playSound(arcaneresearch.init.ModSounds.SHOCK.get(), 1.0f, 1.0f + (random.nextFloat() - random.nextFloat()) * 0.2f);
        if (level().isClientSide) {
            spawnHitFX();
        }
        discard();
    }
    @OnlyIn(Dist.CLIENT)
    private void spawnHitFX() {
        FXDispatcher.INSTANCE.burst(getX(), getY(), getZ(), 1.0f);
    }
    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!level().isClientSide) {
            Entity owner = getOwner();
            if (owner instanceof LivingEntity livingOwner) {
                double attackDamage = livingOwner.getAttributeValue(Attributes.ATTACK_DAMAGE);
                float damage = (float) (attackDamage * (isRed() ? 1.0 : 0.6));
                DamageSource source = level().damageSources().indirectMagic(this, owner);
                result.getEntity().hurt(source, damage);
            }
        }
    }
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        }
        Entity attacker = source.getEntity();
        if (attacker != null) {
            Vec3 look = attacker.getLookAngle();
            setDeltaMovement(look.scale(0.9));
            playSound(arcaneresearch.init.ModSounds.ZAP.get(), 1.0f, 1.0f + (random.nextFloat() - random.nextFloat()) * 0.2f);
            return true;
        }
        return false;
    }
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Red", isRed());
        if (target != null) {
            tag.putInt("TargetId", target.getId());
        }
    }
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_RED, tag.getBoolean("Red"));
        if (tag.contains("TargetId")) {
            entityData.set(DATA_TARGET_ID, tag.getInt("TargetId"));
        }
    }
}
