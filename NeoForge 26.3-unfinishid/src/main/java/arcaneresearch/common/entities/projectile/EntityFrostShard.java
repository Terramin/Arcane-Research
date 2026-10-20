package arcaneresearch.common.entities.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import arcaneresearch.client.fx.WandFX;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;

public class EntityFrostShard extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(EntityFrostShard.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Byte> DATA_FROST = SynchedEntityData.defineId(EntityFrostShard.class, EntityDataSerializers.BYTE);

    public double bounce = 0.5D;
    public int bounceLimit = 3;
    public boolean fragile = false;

    public EntityFrostShard(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public EntityFrostShard(EntityType<? extends EntityFrostShard> type, Level level, LivingEntity owner, float scatter) {
        super(type, level);
        setOwner(owner);
        // NEOFORGE_PORT (26.3): Forge super(type, owner, level) also placed the projectile at the owner's eyes.
        setPos(owner.getX(), owner.getEyeY() - 0.1F, owner.getZ());
        setItem(new ItemStack(getDefaultItem()));
        this.shootFromRotation(owner, owner.getXRot(), owner.getYRot(), 0.0F, 1.6F, scatter);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_DAMAGE, 0.0F);
        builder.define(DATA_FROST, (byte) 0);
    }

    public float getDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    public void setDamage(float damage) {
        this.entityData.set(DATA_DAMAGE, damage);
    }

    public int getFrosty() {
        return this.entityData.get(DATA_FROST);
    }

    public void setFrosty(int frosty) {
        this.entityData.set(DATA_FROST, (byte) frosty);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SNOWBALL;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide() && getFrosty() > 0) {
            float s = getDamage() / 10.0F;
            for (int a = 0; a < getFrosty(); a++) {
                WandFX.sparkle(this.level(),
                        (float) this.getX() - s + this.getRandom().nextFloat() * s * 2.0F,
                        (float) this.getY() - s + this.getRandom().nextFloat() * s * 2.0F,
                        (float) this.getZ() - s + this.getRandom().nextFloat() * s * 2.0F,
                        0.4F, 6, 0.005F);
            }
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        // NEOFORGE_PORT: 1:1 from Forge; MOVEMENT_SLOWDOWN -> SLOWNESS holder,
        // random field -> getRandom(), isClientSide field -> method.
        boolean destroyed = false;
        if (hit instanceof EntityHitResult entityHit) {
            int ox = net.minecraft.util.Mth.floor(this.getX()) - net.minecraft.util.Mth.floor(entityHit.getEntity().getX());
            int oy = net.minecraft.util.Mth.floor(this.getY()) - net.minecraft.util.Mth.floor(entityHit.getEntity().getY());
            int oz = net.minecraft.util.Mth.floor(this.getZ()) - net.minecraft.util.Mth.floor(entityHit.getEntity().getZ());
            var motion = this.getDeltaMovement();
            double mx = oz != 0 ? motion.z * -1.0D : motion.x;
            double my = motion.y;
            double mz = ox != 0 ? motion.x * -1.0D : motion.z;
            if (oy != 0) {
                my *= -0.9D;
            }
            this.setDeltaMovement(mx * 0.15D, my * 0.15D, mz * 0.15D);
            crackParticles((int) getDamage());
            if (!this.level().isClientSide()) {
                double keepX = entityHit.getEntity().getDeltaMovement().x;
                double keepY = entityHit.getEntity().getDeltaMovement().y;
                double keepZ = entityHit.getEntity().getDeltaMovement().z;
                // NEOFORGE_PORT (26.3): Entity.invulnerableTime is private -> get/setInvulnerableTime
                // (same field, any entity, as in Forge).
                if (this.fragile && entityHit.getEntity().getInvulnerableTime() > 0
                        && this.getRandom().nextInt(10) < this.tickCount + 3) {
                    entityHit.getEntity().setInvulnerableTime(0);
                }
                Entity owner = this.getOwner();
                entityHit.getEntity().hurt(this.damageSources().thrown(this, owner != null ? owner : this), getDamage());
                if (entityHit.getEntity() instanceof LivingEntity living && getFrosty() > 0) {
                    living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, getFrosty() - 1));
                }
                if (this.fragile) {
                    this.playSound(net.minecraft.sounds.SoundEvents.GLASS_BREAK, 0.3F,
                            1.2F / (this.getRandom().nextFloat() * 0.2F + 0.9F));
                    entityHit.getEntity().setDeltaMovement(
                            keepX + (entityHit.getEntity().getDeltaMovement().x - keepX) / 10.0D,
                            keepY + (entityHit.getEntity().getDeltaMovement().y - keepY) / 10.0D,
                            keepZ + (entityHit.getEntity().getDeltaMovement().z - keepZ) / 10.0D);
                }
            }
            if (this.fragile) {
                destroyed = true;
            }
        } else if (hit instanceof BlockHitResult blockHit) {
            var motion = this.getDeltaMovement();
            switch (blockHit.getDirection().getAxis()) {
                case X -> this.setDeltaMovement(motion.multiply(-1.0D, 1.0D, 1.0D));
                case Z -> this.setDeltaMovement(motion.multiply(1.0D, 1.0D, -1.0D));
                case Y -> this.setDeltaMovement(motion.multiply(1.0D, -0.9D, 1.0D));
            }
            try {
                var state = this.level().getBlockState(blockHit.getBlockPos());
                this.playSound(state.getSoundType().getBreakSound(), 0.3F,
                        1.2F / (this.getRandom().nextFloat() * 0.2F + 0.9F));
            } catch (Exception ignored) {
            }
            crackParticles((int) getDamage());
        }
        this.setDeltaMovement(this.getDeltaMovement().scale(this.bounce));
        double speed = this.getDeltaMovement().length();
        // NEOFORGE_PORT (26.3): ThrowableProjectile.tick now moves the shard TO the hit point before
        // onHit and applies no motion afterwards. Forge nudged back from the pre-move position
        // (== xOld) and then moved by the bounced motion in the same tick; reproduce that end
        // position (nudging from the hit point lands inside the block and re-hits every tick).
        double nx = this.xOld + this.getDeltaMovement().x;
        double ny = this.yOld + this.getDeltaMovement().y;
        double nz = this.zOld + this.getDeltaMovement().z;
        if (speed > 1.0E-4D) {
            nx -= this.getDeltaMovement().x / speed * 0.05D;
            ny -= this.getDeltaMovement().y / speed * 0.05D;
            nz -= this.getDeltaMovement().z / speed * 0.05D;
        }
        this.setPos(nx, ny, nz);
        if (this.bounceLimit-- <= 0) {
            destroyed = true;
        }
        if (destroyed) {
            this.playSound(net.minecraft.sounds.SoundEvents.GLASS_BREAK, 0.3F,
                    1.2F / (this.getRandom().nextFloat() * 0.2F + 0.9F));
            shatterParticles((int) (8.0F * getDamage()));
            this.discard();
        }
    }

    private void crackParticles(int count) {
        if (!this.level().isClientSide()) {
            return;
        }
        for (int a = 0; a < count; a++) {
            this.level().addParticle(new net.minecraft.core.particles.BlockParticleOption(
                            net.minecraft.core.particles.ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.ICE.defaultBlockState()),
                    this.getX(), this.getY(), this.getZ(),
                    4.0D * (this.getRandom().nextFloat() - 0.5D), 0.5D,
                    (this.getRandom().nextFloat() - 0.5D) * 4.0D);
        }
    }

    private void shatterParticles(int count) {
        crackParticles(count);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("damage", getDamage());
        tag.putBoolean("fragile", this.fragile);
        tag.putInt("frost", getFrosty());
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        setDamage(tag.getFloatOr("damage", 0.0F));
        this.fragile = tag.getBooleanOr("fragile", false);
        setFrosty(tag.getIntOr("frost", 0));
    }
}
