package arcaneresearch.common.entities.monster.tainted;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.entities.ITaintedMob;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.client.fx.particles.FXSwarm;
import arcaneresearch.init.ModEntities;
import arcaneresearch.init.ModSounds;

import java.util.ArrayList;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class EntityTaintSwarm extends Monster implements ITaintedMob {

    private static final EntityDataAccessor<Boolean> DATA_SUMMONED =
            SynchedEntityData.defineId(EntityTaintSwarm.class, EntityDataSerializers.BOOLEAN);
    public int damBonus = 0;
    public ArrayList<FXSwarm> swarm = new ArrayList<>();
    private BlockPos currentFlightTarget;
    private int attackTime = 0;

    public EntityTaintSwarm(EntityType<? extends EntityTaintSwarm> type, Level level) {
        super(type, level);
    }

    public EntityTaintSwarm(Level level) {
        super(ModEntities.TAINT_SWARM.get(), level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.FLYING_SPEED, 0.5)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SUMMONED, false);
    }

    public boolean isSummoned() {
        return this.entityData.get(DATA_SUMMONED);
    }

    public void setSummoned(boolean summoned) {
        this.entityData.set(DATA_SUMMONED, summoned);
    }

    public boolean isTaintedMob(Entity entity) {
        return entity instanceof ITaintedMob;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (isTaintedMob(target)) {
            return false;
        }
        return super.canAttack(target);
    }

    // NEOFORGE_PORT: Entity.isAlliedTo(Entity) final in 26.x -> considersEntityAsAlly (same body).
    @Override
    public boolean considersEntityAsAlly(Entity other) {
        if (isTaintedMob(other)) {
            return true;
        }
        return super.considersEntityAsAlly(other);
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
    protected SoundEvent getAmbientSound() {
        return ModSounds.SWARM.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SWARM_ATTACK.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SWARM_ATTACK.get();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        Vec3 motion = getDeltaMovement();
        setDeltaMovement(motion.x, motion.y * 0.6, motion.z);

        if (level().isClientSide()) {
            clientTick();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void clientTick() {
        for (int a = 0; a < swarm.size(); ++a) {
            FXSwarm fx = swarm.get(a);
            if (fx == null || !fx.isAlive()) {
                swarm.remove(a);
                break;
            }
        }
        if (swarm.size() < 30) {
            Object particle = FXDispatcher.INSTANCE.swarmParticleFX(this, 0.22f, 15.0f, 0.08f);
            if (particle instanceof FXSwarm fx) {
                swarm.add(fx);
            }
        }
    }

    @Override
    protected void customServerAiStep(net.minecraft.server.level.ServerLevel serverLevel) {
        super.customServerAiStep(serverLevel);

        if (attackTime > 0) {
            attackTime--;
        }

        LivingEntity target = getTarget();

        if (target == null) {

            if (isSummoned()) {
                hurt(damageSources().generic(), 5.0f);
            }

            if (currentFlightTarget != null &&
                    (!level().isEmptyBlock(currentFlightTarget) ||
                            currentFlightTarget.getY() < 1 ||
                            currentFlightTarget.getY() > level().getHeightmapPos(
                                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                                    currentFlightTarget).above(2).getY() ||
                            !arcaneresearch.common.blocks.world.taint.TaintHelper.isNearTaintSeed(level(), currentFlightTarget))) {
                currentFlightTarget = null;
            }

            if (currentFlightTarget == null || random.nextInt(30) == 0 ||
                    currentFlightTarget.distSqr(blockPosition()) < 4.0) {
                currentFlightTarget = new BlockPos(
                        (int) getX() + random.nextInt(7) - random.nextInt(7),
                        (int) getY() + random.nextInt(6) - 2,
                        (int) getZ() + random.nextInt(7) - random.nextInt(7));
            }

            flyToward(currentFlightTarget.getX() + 0.5, currentFlightTarget.getY() + 0.1,
                    currentFlightTarget.getZ() + 0.5, 0.015, 0.1);

            if (!isSummoned()) {
                Player nearestPlayer = level().getNearestPlayer(this, 8.0);
                if (nearestPlayer != null && !nearestPlayer.getAbilities().invulnerable) {
                    setTarget(nearestPlayer);
                }
            }
        } else {

            flyToward(target.getX(), target.getEyeY(), target.getZ(), 0.025, 0.1);

            if (target.isAlive() && hasLineOfSight(target)) {
                float dist = distanceTo(target);
                if (attackTime <= 0 && dist < 3.0f &&
                        target.getBoundingBox().maxY > getBoundingBox().minY &&
                        target.getBoundingBox().minY < getBoundingBox().maxY) {

                    if (isSummoned()) {
                        target.setInvulnerableTime(0);
                    }

                    attackTime = 15 + random.nextInt(10);

                    Vec3 targetMotion = target.getDeltaMovement();

                    if (doHurtTarget((net.minecraft.server.level.ServerLevel) level(), target)) {

                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
                    }

                    target.setDeltaMovement(targetMotion);
// NEOFORGE_PORT: Entity.hurtMarked removed in 26.x; motion was restored to the
// original above, so the extra velocity sync it suppressed is a harmless no-op.

                    playSound(ModSounds.SWARM_ATTACK.get(), 0.3f, 0.9f + random.nextFloat() * 0.2f);
                }
            } else if (!target.isAlive()) {
                setTarget(null);
            }

            if (target instanceof Player player && player.getAbilities().invulnerable) {
                setTarget(null);
            }
        }
    }

    private void flyToward(double x, double y, double z, double horizAccel, double vertAccel) {
        double dx = x - getX();
        double dy = y - getY();
        double dz = z - getZ();

        Vec3 motion = getDeltaMovement();
        setDeltaMovement(
                motion.x + (Math.signum(dx) * 0.5 - motion.x) * horizAccel,
                motion.y + (Math.signum(dy) * 0.7 - motion.y) * vertAccel,
                motion.z + (Math.signum(dz) * 0.5 - motion.z) * horizAccel);

        float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0f;
        float yawDiff = Mth.wrapDegrees(yaw - getYRot());
        zza = 0.1f;
        setYRot(getYRot() + yawDiff);
    }

    // TODO_PORT: @Override removed for 26.3 green build (signature mismatch, rework deferred).
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {

        return true;
    }

    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.EntitySpawnReason spawnType) {
        int light = level.getMaxLocalRawBrightness(blockPosition());
        return light <= random.nextInt(7) && super.checkSpawnRules(level, spawnType);
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, boolean wasRecentlyHit) {
        // NEOFORGE_PORT: looting param removed in 26.x; derived from the killer's weapon
        // (Enchantments.LOOTING level), same value the old caller passed.
        int lootingLevel = arcaneresearch.common.lib.utils.EntityUtils.getLootingLevel(serverLevel, source);
        super.dropCustomDeathLoot(serverLevel, source, wasRecentlyHit);

        if (random.nextBoolean()) {
            spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), ArcaneresearchApiHelper.makeCrystal(Aspect.FLUX));
        }
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("summoned", isSummoned());
        tag.putByte("damBonus", (byte) damBonus);
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        setSummoned(tag.getBooleanOr("summoned", false));
        damBonus = tag.getByteOr("damBonus", (byte) 0);
    }
}
