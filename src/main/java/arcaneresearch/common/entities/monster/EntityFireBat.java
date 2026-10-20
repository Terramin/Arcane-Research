package arcaneresearch.common.entities.monster;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.init.ModEntities;
public class EntityFireBat extends Monster {
    private static final EntityDataAccessor<Boolean> DATA_HANGING =
            SynchedEntityData.defineId(EntityFireBat.class, EntityDataSerializers.BOOLEAN);
    public LivingEntity owner;
    public int damBonus = 0;
    private BlockPos currentFlightTarget;
    private int attackTime = 0;
    public EntityFireBat(EntityType<? extends EntityFireBat> type, Level level) {
        super(type, level);
        this.setIsBatHanging(true);
    }
    public EntityFireBat(Level level) {
        super(ModEntities.FIRE_BAT.get(), level);
        this.setIsBatHanging(true);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 5.0)
                .add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.FLYING_SPEED, 0.5)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }
    public static boolean checkFireBatSpawnRules(EntityType<? extends EntityFireBat> type, ServerLevelAccessor level,
                                                 MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        int light = level.getMaxLocalRawBrightness(pos);
        return light <= random.nextInt(7);
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_HANGING, false);
    }
    @Override
    public boolean fireImmune() {
        return true;
    }
    public boolean getIsBatHanging() {
        return this.entityData.get(DATA_HANGING);
    }
    public void setIsBatHanging(boolean hanging) {
        this.entityData.set(DATA_HANGING, hanging);
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
        return (getIsBatHanging() && random.nextInt(4) != 0) ? null : SoundEvents.BAT_AMBIENT;
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
    public void aiStep() {
        if (isInWaterOrRain()) {
            hurt(damageSources().drown(), 1.0f);
        }
        super.aiStep();
    }
    @Override
    public void tick() {
        super.tick();
        if (getIsBatHanging()) {
            setDeltaMovement(Vec3.ZERO);
            setPos(getX(), Mth.floor(getY()) + 1.0 - getBbHeight(), getZ());
        } else {
            Vec3 motion = getDeltaMovement();
            setDeltaMovement(motion.x, motion.y * 0.6, motion.z);
        }
    }
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (attackTime > 0) {
            attackTime--;
        }
        BlockPos blockPos = blockPosition();
        BlockPos abovePos = blockPos.above();
        if (getIsBatHanging()) {
            if (!level().getBlockState(abovePos).isRedstoneConductor(level(), abovePos)) {
                setIsBatHanging(false);
                level().levelEvent(null, 1025, blockPos, 0);
            } else {
                if (random.nextInt(200) == 0) {
                    setYHeadRot(random.nextInt(360));
                }
                Player nearbyPlayer = level().getNearestPlayer(this, 4.0);
                if (nearbyPlayer != null) {
                    setIsBatHanging(false);
                    level().levelEvent(null, 1025, blockPos, 0);
                }
            }
        } else if (getTarget() == null) {
            if (currentFlightTarget != null &&
                    (!level().isEmptyBlock(currentFlightTarget) || currentFlightTarget.getY() < 1)) {
                currentFlightTarget = null;
            }
            if (currentFlightTarget == null || random.nextInt(30) == 0 ||
                    currentFlightTarget.distSqr(blockPos) < 4.0) {
                currentFlightTarget = new BlockPos(
                        (int) getX() + random.nextInt(7) - random.nextInt(7),
                        (int) getY() + random.nextInt(6) - 2,
                        (int) getZ() + random.nextInt(7) - random.nextInt(7));
            }
            flyToward(currentFlightTarget.getX() + 0.5, currentFlightTarget.getY() + 0.1, currentFlightTarget.getZ() + 0.5);
            if (random.nextInt(100) == 0 && level().getBlockState(abovePos).isRedstoneConductor(level(), abovePos)) {
                setIsBatHanging(true);
            }
            setTarget(level().getNearestPlayer(this, 12.0));
        } else {
            LivingEntity target = getTarget();
            flyToward(target.getX(), target.getEyeY() * 0.66 + target.getY() * 0.34, target.getZ());
            if (target.isAlive() && hasLineOfSight(target)) {
                float dist = distanceTo(target);
                if (attackTime <= 0 && dist < Math.max(2.5f, target.getBbWidth() * 1.1f) &&
                        target.getBoundingBox().maxY > getBoundingBox().minY &&
                        target.getBoundingBox().minY < getBoundingBox().maxY) {
                    attackTime = 20 + random.nextInt(20);
                    if (random.nextInt(10) == 0) {
                        target.invulnerableTime = 0;
                        level().explode(this, getX(), getY(), getZ(), 1.5f, Level.ExplosionInteraction.MOB);
                        discard();
                    } else {
                        playSound(SoundEvents.BAT_HURT, 0.5f, 0.9f + random.nextFloat() * 0.2f);
                        doHurtTarget(target);
                    }
                }
            } else if (!target.isAlive()) {
                setTarget(null);
            }
            if (target instanceof Player player && player.getAbilities().invulnerable) {
                setTarget(null);
            }
        }
    }
    private void flyToward(double x, double y, double z) {
        double dx = x - getX();
        double dy = y - getY();
        double dz = z - getZ();
        Vec3 motion = getDeltaMovement();
        setDeltaMovement(
                motion.x + (Math.signum(dx) * 0.5 - motion.x) * 0.1,
                motion.y + (Math.signum(dy) * 0.7 - motion.y) * 0.1,
                motion.z + (Math.signum(dz) * 0.5 - motion.z) * 0.1);
        float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0f;
        float yawDiff = Mth.wrapDegrees(yaw - getYRot());
        zza = 0.5f;
        setYRot(getYRot() + yawDiff);
    }
    @Override
    protected boolean shouldDespawnInPeaceful() {
        return this.owner == null;
    }
    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }
    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source) || source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE) ||
                source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)) {
            return false;
        }
        if (!level().isClientSide && getIsBatHanging()) {
            setIsBatHanging(false);
        }
        return super.hurt(source, amount);
    }
    @Override
    protected void dropCustomDeathLoot(DamageSource source, int lootingLevel, boolean wasRecentlyHit) {
        super.dropCustomDeathLoot(source, lootingLevel, wasRecentlyHit);
        if (random.nextInt(3) == 0) {
            spawnAtLocation(new ItemStack(Items.GUNPOWDER));
        }
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("BatHanging", getIsBatHanging());
        tag.putByte("DamBonus", (byte) damBonus);
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setIsBatHanging(tag.getBoolean("BatHanging"));
        damBonus = tag.getByte("DamBonus");
    }
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level, net.minecraft.world.entity.MobSpawnType spawnType) {
        BlockPos pos = blockPosition();
        int light = level.getMaxLocalRawBrightness(pos);
        return light <= random.nextInt(7) && super.checkSpawnRules(level, spawnType);
    }
}
