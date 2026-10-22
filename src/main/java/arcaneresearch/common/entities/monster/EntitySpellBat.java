package arcaneresearch.common.entities.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import arcaneresearch.api.casters.FocusEffect;
import arcaneresearch.api.casters.FocusEngine;
import arcaneresearch.api.casters.FocusPackage;
import arcaneresearch.api.casters.Trajectory;
import arcaneresearch.common.lib.utils.EntityUtils;
import arcaneresearch.init.ModEntities;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;

public class EntitySpellBat extends Monster implements net.neoforged.neoforge.entity.IEntityWithComplexSpawn {
    

    private static final EntityDataAccessor<Boolean> DATA_FRIENDLY =
            SynchedEntityData.defineId(EntitySpellBat.class, EntityDataSerializers.BOOLEAN);
    public LivingEntity owner;
    public int damBonus = 0;
    public int color = 0xFFFFFF;
    private BlockPos currentFlightTarget;
    private UUID ownerUUID;
    private FocusPackage focusPackage;
    private int attackTime;
    private FocusEffect[] effects;

    public EntitySpellBat(EntityType<? extends EntitySpellBat> type, Level level) {
        super(type, level);
    }

    public EntitySpellBat(Level level) {
        this(ModEntities.SPELL_BAT.get(), level);
    }

    public EntitySpellBat(Level level, FocusPackage pack, boolean friendly) {
        this(ModEntities.SPELL_BAT.get(), level);
        this.focusPackage = pack;
        setOwner(pack.getCaster());
        setFriendly(friendly);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 5.0)
                .add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FRIENDLY, false);
    }

    public boolean isFriendly() {
        return this.entityData.get(DATA_FRIENDLY);
    }

    public void setFriendly(boolean friendly) {
        this.entityData.set(DATA_FRIENDLY, friendly);
    }

    @Nullable
    public LivingEntity getOwner() {
        if (owner == null && ownerUUID != null && level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(ownerUUID);
            if (entity instanceof LivingEntity living) {
                owner = living;
            }
        }
        return owner;
    }

    public void setOwner(@Nullable LivingEntity owner) {
        this.owner = owner;
        this.ownerUUID = owner != null ? owner.getUUID() : null;
    }

    public FocusPackage getFocusPackage() {
        return focusPackage;
    }

    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0f;
    }

    @Override
    protected float getSoundVolume() {
        return 0.1f;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 0.95f;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BAT_DEATH;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public net.minecraft.world.scores.PlayerTeam getTeam() {
        LivingEntity owner = getOwner();
        if (owner != null) {
            return owner.getTeam();
        }
        return super.getTeam();
    }

    
    @Override
    public boolean considersEntityAsAlly(Entity other) {
        LivingEntity owner = getOwner();
        if (other == owner) {
            return true;
        }
        if (owner != null) {
            return owner.isAlliedTo(other) || other.isAlliedTo(owner);
        }
        return super.considersEntityAsAlly(other);
    }

    @Override
    public void tick() {
        super.tick();

        setDeltaMovement(getDeltaMovement().multiply(1.0, 0.6, 1.0));

        if (!level().isClientSide() && (tickCount > 600 || getOwner() == null)) {
            discard();
            return;
        }

        if (isAlive() && level().isClientSide() && focusPackage != null) {
            if (effects == null) {
                effects = focusPackage.getFocusEffects();
                if (effects.length > 0) {
                    int r = 0;
                    int g = 0;
                    int b = 0;
                    for (FocusEffect ef : effects) {
                        java.awt.Color c = new java.awt.Color(FocusEngine.getElementColor(ef.getKey()));
                        r += c.getRed();
                        g += c.getGreen();
                        b += c.getBlue();
                    }
                    color = new java.awt.Color(r / effects.length, g / effects.length, b / effects.length).getRGB();
                }
            }
            if (effects != null && effects.length > 0) {
                FocusEffect eff = effects[random.nextInt(effects.length)];
                eff.renderParticleFX(level(),
                        getX() + random.nextGaussian() * 0.125,
                        getY() + getBbHeight() / 2.0 + random.nextGaussian() * 0.125,
                        getZ() + random.nextGaussian() * 0.125,
                        0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected void customServerAiStep(net.minecraft.server.level.ServerLevel serverLevel) {
        super.customServerAiStep(serverLevel);

        if (attackTime > 0) {
            --attackTime;
        }

        if (getTarget() == null) {

            if (currentFlightTarget != null &&
                    (!level().isEmptyBlock(currentFlightTarget) || currentFlightTarget.getY() < 1)) {
                currentFlightTarget = null;
            }

            if (currentFlightTarget == null || random.nextInt(30) == 0 ||
                    currentFlightTarget.distToCenterSqr(getX(), getY(), getZ()) < 4.0) {
                currentFlightTarget = new BlockPos(
                        (int) getX() + random.nextInt(7) - random.nextInt(7),
                        (int) getY() + random.nextInt(6) - 2,
                        (int) getZ() + random.nextInt(7) - random.nextInt(7));
            }

            moveTowardsTarget(currentFlightTarget.getX() + 0.5,
                    currentFlightTarget.getY() + 0.1,
                    currentFlightTarget.getZ() + 0.5);
        } else {

            moveTowardsTarget(getTarget().getX(),
                    getTarget().getY() + getTarget().getEyeHeight() * 0.66,
                    getTarget().getZ());
        }

        Vec3 motion = getDeltaMovement();
        float yaw = (float) (Mth.atan2(motion.z, motion.x) * Mth.RAD_TO_DEG) - 90.0f;
        float yawDiff = Mth.wrapDegrees(yaw - getYRot());
        zza = 0.5f;
        setYRot(getYRot() + yawDiff);

        if (getTarget() == null) {
            setTarget(findTargetToAttack());
        } else if (getTarget().isAlive()) {
            float dist = distanceTo(getTarget());
            if (isAlive() && hasLineOfSight(getTarget())) {
                attackEntity(getTarget(), dist);
            }
        } else {
            setTarget(null);
        }

        if (!isFriendly() && getTarget() instanceof Player player && player.getAbilities().invulnerable) {
            setTarget(null);
        }
    }

    private void moveTowardsTarget(double x, double y, double z) {
        double dx = x - getX();
        double dy = y - getY();
        double dz = z - getZ();

        Vec3 delta = getDeltaMovement();
        setDeltaMovement(
                delta.x + (Math.signum(dx) * 0.5 - delta.x) * 0.1,
                delta.y + (Math.signum(dy) * 0.7 - delta.y) * 0.1,
                delta.z + (Math.signum(dz) * 0.5 - delta.z) * 0.1
        );
    }

    protected void attackEntity(Entity target, float distance) {
        if (attackTime <= 0 && distance < Math.max(2.5f, target.getBbWidth() * 1.1f) &&
                target.getBoundingBox().maxY > getBoundingBox().minY &&
                target.getBoundingBox().minY < getBoundingBox().maxY) {

            attackTime = 40;

            if (!level().isClientSide()) {

                if (focusPackage != null && getOwner() != null) {
                    Vec3 start = position();
                    Vec3 hitVec = target.position().add(0.0, target.getBbHeight() / 2.0f, 0.0);
                    Trajectory trajectory = new Trajectory(start, hitVec.subtract(start));
                    EntityHitResult hitResult = new EntityHitResult(target, hitVec);

                    FocusEngine.runFocusPackage(
                            focusPackage.copy(getOwner()),
                            new Trajectory[]{trajectory},
                            new HitResult[]{hitResult}
                    );
                }

                setHealth(getHealth() - 1.0f);
            }

            playSound(SoundEvents.BAT_HURT, 0.5f, 0.9f + random.nextFloat() * 0.2f);
        }
    }

    @Nullable
    protected LivingEntity findTargetToAttack() {

        double range = 12.0;
        AABB searchBox = getBoundingBox().inflate(range);
        List<LivingEntity> list = level().getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(LivingEntity.class), searchBox, e -> e != this && e.isAlive() && !e.isSpectator());

        double d = Double.MAX_VALUE;
        LivingEntity ret = null;
        LivingEntity owner = getOwner();

        for (LivingEntity e : list) {
            if (isFriendly()) {
                if (!EntityUtils.isFriendly(owner, e)) {
                    continue;
                }
            } else {
                if (EntityUtils.isFriendly(owner, e)) {
                    continue;
                }
                if (considersEntityAsAlly(e)) {
                    continue;
                }
            }
            double ed = distanceToSqr(e);
            if (ed >= d) {
                continue;
            }
            d = ed;
            ret = e;
        }

        return ret;
    }

    @Override
    protected void pushEntities() {
        if (!isFriendly()) {
            super.pushEntities();
        }
    }

    
    

    
    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {

    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    
    
    @Override
    public boolean isAffectedByFluids() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUUID != null) {
            tag.store("OwnerUUID", net.minecraft.core.UUIDUtil.CODEC, ownerUUID);
        }
        tag.putBoolean("friendly", isFriendly());
        if (focusPackage != null) {
            tag.store("pack", net.minecraft.nbt.CompoundTag.CODEC, focusPackage.serialize());
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        
        ownerUUID = tag.read("OwnerUUID", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
        setFriendly(tag.getBooleanOr("friendly", false));
        if (tag.read("pack", net.minecraft.nbt.CompoundTag.CODEC).isPresent()) {
            focusPackage = new FocusPackage();
            try {
                focusPackage.deserialize(tag.read("pack", net.minecraft.nbt.CompoundTag.CODEC).orElse(new net.minecraft.nbt.CompoundTag()));
            } catch (Exception ignored) {
            }
        }
    }

    
    

    @Override
    public void writeSpawnData(net.minecraft.network.RegistryFriendlyByteBuf buffer) {
        if (focusPackage != null) {
            buffer.writeNbt(focusPackage.serialize());
        } else {
            buffer.writeNbt(new net.minecraft.nbt.CompoundTag());
        }
    }

    @Override
    public void readSpawnData(net.minecraft.network.RegistryFriendlyByteBuf buffer) {
        try {
            net.minecraft.nbt.CompoundTag tag = buffer.readNbt();
            if (tag != null && !tag.isEmpty()) {
                focusPackage = new FocusPackage();
                focusPackage.deserialize(tag);
            }
        } catch (Exception ignored) {
        }
    }
}
