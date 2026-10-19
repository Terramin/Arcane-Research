package arcaneresearch.common.entities.projectile;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.init.ModEntities;
import java.util.List;
public class EntityHomingShard extends ThrowableProjectile {
    private static final EntityDataAccessor<Byte> DATA_STRENGTH =
            SynchedEntityData.defineId(EntityHomingShard.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_TARGET_ID =
            SynchedEntityData.defineId(EntityHomingShard.class, EntityDataSerializers.INT);
    private Class<? extends LivingEntity> targetClass = null;
    private boolean persistent = false;
    private LivingEntity target = null;
    public EntityHomingShard(EntityType<? extends EntityHomingShard> type, Level level) {
        super(type, level);
    }
    public EntityHomingShard(Level level, LivingEntity owner, LivingEntity target, int strength, boolean persistent) {
        super(ModEntities.HOMING_SHARD.get(), owner, level);
        this.target = target;
        this.targetClass = target.getClass();
        this.persistent = persistent;
        setStrength(strength);
        entityData.set(DATA_TARGET_ID, target.getId());
        Vec3 look = owner.getLookAngle();
        setPos(owner.getX() + look.x / 2.0,
                owner.getEyeY() + look.y / 2.0,
                owner.getZ() + look.z / 2.0);
        float speed = 0.5f;
        float yaw = owner.getYRot() + (random.nextFloat() - random.nextFloat()) * 60.0f;
        float pitch = owner.getXRot() + (random.nextFloat() - random.nextFloat()) * 60.0f;
        double mx = -Mth.sin(yaw * Mth.DEG_TO_RAD) * Mth.cos(pitch * Mth.DEG_TO_RAD) * speed;
        double mz = Mth.cos(yaw * Mth.DEG_TO_RAD) * Mth.cos(pitch * Mth.DEG_TO_RAD) * speed;
        double my = -Mth.sin(pitch * Mth.DEG_TO_RAD) * speed;
        setDeltaMovement(mx, my, mz);
    }
    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_STRENGTH, (byte) 0);
        entityData.define(DATA_TARGET_ID, -1);
    }
    public int getStrength() {
        return entityData.get(DATA_STRENGTH);
    }
    public void setStrength(int strength) {
        entityData.set(DATA_STRENGTH, (byte) strength);
    }
    @Override
    protected float getGravity() {
        return 0.0f;
    }
    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            level().addParticle(ParticleTypes.WITCH,
                    getX(), getY(), getZ(),
                    (random.nextFloat() - 0.5) * 0.1,
                    (random.nextFloat() - 0.5) * 0.1,
                    (random.nextFloat() - 0.5) * 0.1);
        }
        if (!level().isClientSide) {
            if (target == null) {
                int targetId = entityData.get(DATA_TARGET_ID);
                if (targetId >= 0) {
                    Entity e = level().getEntity(targetId);
                    if (e instanceof LivingEntity le) {
                        target = le;
                    }
                }
            }
            if (persistent && (target == null || !target.isAlive() || distanceToSqr(target) > 1250.0)) {
                findNewTarget();
            }
            if (target == null || !target.isAlive()) {
                level().broadcastEntityEvent(this, (byte) 16);
                discard();
                return;
            }
        }
        if (tickCount > 300) {
            if (!level().isClientSide) {
                level().broadcastEntityEvent(this, (byte) 16);
            }
            discard();
            return;
        }
        if (tickCount % 20 == 0 && target != null && target.isAlive()) {
            double dist = distanceTo(target);
            if (dist > 0) {
                double dx = (target.getX() - getX()) / dist;
                double dy = (target.getY() + target.getBbHeight() * 0.6 - getY()) / dist;
                double dz = (target.getZ() - getZ()) / dist;
                setDeltaMovement(dx, dy, dz);
            }
        }
        setDeltaMovement(getDeltaMovement().scale(0.85));
    }
    private void findNewTarget() {
        if (targetClass == null) return;
        AABB searchBox = getBoundingBox().inflate(16.0);
        List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, searchBox,
                e -> targetClass.isInstance(e) && e.isAlive() && (getOwner() == null || e.getId() != getOwner().getId()));
        if (!entities.isEmpty()) {
            target = entities.get(0);
            entityData.set(DATA_TARGET_ID, target.getId());
        }
    }
    @Override
    public void handleEntityEvent(byte id) {
        if (id == 16) {
            spawnHitFX();
        }
        super.handleEntityEvent(id);
    }
    @OnlyIn(Dist.CLIENT)
    private void spawnHitFX() {
        FXDispatcher.INSTANCE.burst(getX(), getY(), getZ(), 0.3f);
    }
    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!level().isClientSide) {
            Entity hit = result.getEntity();
            Entity owner = getOwner();
            if (owner != null && hit == owner) return;
            float damage = 1.0f + getStrength() * 0.5f;
            DamageSource source = level().damageSources().indirectMagic(this, owner);
            hit.hurt(source, damage);
            playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f + (random.nextFloat() - random.nextFloat()) * 0.2f);
            level().broadcastEntityEvent(this, (byte) 16);
            discard();
        }
    }
    @Override
    protected void onHitBlock(BlockHitResult result) {
        Vec3 motion = getDeltaMovement();
        switch (result.getDirection().getAxis()) {
            case X -> setDeltaMovement(motion.x * -0.8, motion.y, motion.z);
            case Y -> setDeltaMovement(motion.x, motion.y * -0.8, motion.z);
            case Z -> setDeltaMovement(motion.x, motion.y, motion.z * -0.8);
        }
    }
    @Override
    protected void onHit(HitResult result) {
        if (result.getType() == HitResult.Type.ENTITY) {
            onHitEntity((EntityHitResult) result);
        } else if (result.getType() == HitResult.Type.BLOCK) {
            onHitBlock((BlockHitResult) result);
        }
    }
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("Strength", (byte) getStrength());
        tag.putBoolean("Persistent", persistent);
        if (target != null) {
            tag.putInt("TargetId", target.getId());
        }
        if (targetClass != null) {
            tag.putString("TargetClass", targetClass.getName());
        }
    }
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setStrength(tag.getByte("Strength"));
        persistent = tag.getBoolean("Persistent");
        if (tag.contains("TargetId")) {
            entityData.set(DATA_TARGET_ID, tag.getInt("TargetId"));
        }
    }
}
