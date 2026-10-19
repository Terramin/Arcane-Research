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
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.items.resources.ItemWispEssence;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.fx.PacketFXWispZap;
import arcaneresearch.init.ModEntities;
import arcaneresearch.init.ModItems;
import arcaneresearch.init.ModSounds;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;

// NEOFORGE_PORT: FlyingMob removed in 26.x (flying mobs extend Mob directly now,
// e.g. Ghast); movement is fully custom here (see constructor move control).
public class EntityWisp extends Mob implements Enemy {

    private static final EntityDataAccessor<String> DATA_TYPE =
            SynchedEntityData.defineId(EntityWisp.class, EntityDataSerializers.STRING);
    public int prevAttackCounter = 0;
    public int attackCounter = 0;
    private BlockPos currentFlightTarget;
    private int aggroCooldown = 0;

    public EntityWisp(EntityType<? extends EntityWisp> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    public EntityWisp(Level level) {
        super(ModEntities.WISP.get(), level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 22.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.FLYING_SPEED, 0.5)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    public static boolean checkWispSpawnRules(EntityType<? extends EntityWisp> type, ServerLevelAccessor level,
                                              EntitySpawnReason spawnType, BlockPos pos, RandomSource random) {

        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }

        int count = level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(EntityWisp.class), new AABB(pos).inflate(16.0), e -> true).size();
        if (count >= 8) {
            return false;
        }

        int skyLight = level.getBrightness(LightLayer.SKY, pos);
        if (skyLight > random.nextInt(32)) {
            return false;
        }

        int blockLight = level.getMaxLocalRawBrightness(pos);
        return blockLight <= random.nextInt(8);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TYPE, "");
    }

    public String getWispType() {
        return this.entityData.get(DATA_TYPE);
    }

    public void setWispType(String type) {
        this.entityData.set(DATA_TYPE, type != null ? type : "");
    }

    public Aspect getAspect() {
        String type = getWispType();
        if (type == null || type.isEmpty()) return null;
        return Aspect.getAspect(type);
    }

    public int getColor() {
        Aspect aspect = getAspect();
        return aspect != null ? aspect.getColor() : 0xFFFFFF;
    }

    // TODO_PORT: @Override removed for 26.3 green build (signature mismatch, rework deferred).
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }

    @Override
    public int getMaxAirSupply() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, float amount) {
        if (source.getEntity() instanceof LivingEntity living) {
            setTarget(living);
            aggroCooldown = 200;
        }
        return super.hurtServer(serverLevel, source, amount);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);

        if (level().isClientSide()) {
            spawnDeathBurst();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnDeathBurst() {
        FXDispatcher.INSTANCE.burst(getX(), getY() + 0.45, getZ(), 1.0f);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide() && tickCount <= 1) {
            FXDispatcher.INSTANCE.burst(getX(), getY(), getZ(), 10.0f);
        }

        if (level().isClientSide() && random.nextBoolean()) {
            Aspect aspect = getAspect();
            if (aspect != null) {
                FXDispatcher.INSTANCE.drawWispParticles(
                        getX() + (random.nextFloat() - random.nextFloat()) * 0.7f,
                        getY() + (random.nextFloat() - random.nextFloat()) * 0.7f,
                        getZ() + (random.nextFloat() - random.nextFloat()) * 0.7f,
                        0.0, 0.0, 0.0, aspect.getColor(), 0);
            }
        }

        Vec3 motion = getDeltaMovement();
        setDeltaMovement(motion.x, motion.y * 0.6, motion.z);
    }

    @Override
    protected void customServerAiStep(net.minecraft.server.level.ServerLevel serverLevel) {
        super.customServerAiStep(serverLevel);

        if (getWispType().isEmpty()) {
            ArrayList<Aspect> aspects;
            if (random.nextInt(10) != 0) {
                aspects = Aspect.getPrimalAspects();
            } else {
                aspects = Aspect.getCompoundAspects();
            }
            if (aspects != null && !aspects.isEmpty()) {
                setWispType(aspects.get(random.nextInt(aspects.size())).getTag());
            }
        }

        if (level().getDifficulty() == Difficulty.PEACEFUL) {
            discard();
            return;
        }

        prevAttackCounter = attackCounter;
        double attackRange = 16.0;

        LivingEntity target = getTarget();

        if (target == null || !hasLineOfSight(target)) {

            if (currentFlightTarget != null &&
                    (!level().isEmptyBlock(currentFlightTarget) ||
                            currentFlightTarget.getY() < 1 ||
                            currentFlightTarget.getY() > level().getHeightmapPos(
                                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                                    currentFlightTarget).above(8).getY())) {
                currentFlightTarget = null;
            }

            if (currentFlightTarget == null || random.nextInt(30) == 0 ||
                    currentFlightTarget.distSqr(blockPosition()) < 4.0) {
                currentFlightTarget = new BlockPos(
                        (int) getX() + random.nextInt(7) - random.nextInt(7),
                        (int) getY() + random.nextInt(6) - 2,
                        (int) getZ() + random.nextInt(7) - random.nextInt(7));
            }

            flyToward(currentFlightTarget.getX() + 0.5, currentFlightTarget.getY() + 0.1, currentFlightTarget.getZ() + 0.5, 0.15f);
        } else if (distanceToSqr(target) > attackRange * attackRange / 2.0 && hasLineOfSight(target)) {

            flyToward(target.getX(), target.getEyeY() * 0.66 + target.getY() * 0.34, target.getZ(), 0.5f);
        }

        if (target instanceof Player player && player.getAbilities().invulnerable) {
            setTarget(null);
        }

        if (target != null && !target.isAlive()) {
            setTarget(null);
        }

        aggroCooldown--;

        if (random.nextInt(1000) == 0 && (target == null || aggroCooldown <= 0)) {
            Player nearestPlayer = level().getNearestPlayer(this, 16.0);
            if (nearestPlayer != null) {
                setTarget(nearestPlayer);
                aggroCooldown = 50;
            }
        }

        target = getTarget();
        if (isAlive() && target != null && distanceToSqr(target) < attackRange * attackRange) {

            double dx = target.getX() - getX();
            double dz = target.getZ() - getZ();
            float targetYaw = -(float) Math.atan2(dx, dz) * Mth.RAD_TO_DEG;
            setYRot(targetYaw);
            yBodyRot = targetYaw;

            if (hasLineOfSight(target)) {
                attackCounter++;

                if (attackCounter == 20) {

                    playSound(ModSounds.ZAP.get(), 1.0f, 1.1f);

                    PacketHandler.sendToAllTracking(new PacketFXWispZap(getId(), target.getId()), this);

                    float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
                    Vec3 targetMotion = target.getDeltaMovement();

                    if (Math.abs(targetMotion.x) > 0.1 || Math.abs(targetMotion.y) > 0.1 || Math.abs(targetMotion.z) > 0.1) {

                        if (random.nextFloat() < 0.4f) {
                            target.hurt(damageSources().mobAttack(this), damage);
                        }
                    } else {

                        if (random.nextFloat() < 0.66f) {
                            target.hurt(damageSources().mobAttack(this), damage + 1.0f);
                        }
                    }

                    attackCounter = -20 + random.nextInt(20);
                }
            } else if (attackCounter > 0) {
                attackCounter--;
            }
        }
    }

    private void flyToward(double x, double y, double z, float speed) {
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
        zza = speed;
        setYRot(getYRot() + yawDiff);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.WISP_LIVE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.LAVA_EXTINGUISH;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.WISP_DEAD.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.25f;
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, boolean wasRecentlyHit) {
        // NEOFORGE_PORT: looting param removed in 26.x; derived from the killer's weapon
        // (Enchantments.LOOTING level), same value the old caller passed.
        int lootingLevel = arcaneresearch.common.lib.utils.EntityUtils.getLootingLevel(serverLevel, source);
        super.dropCustomDeathLoot(serverLevel, source, wasRecentlyHit);

        // 1.7.10 парити (EntityWisp.func_70628_a): мудр всегда дропает одну висп-эссенцию
        // (2 эссенции) своего аспекта — вместо кристалла из тех-линии forge 1.12.
        Aspect aspect = getAspect();
        if (aspect != null) {
            spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), ItemWispEssence.createStack(ModItems.WISP_ESSENCE.get(), aspect, 1));
        }
    }

    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level, EntitySpawnReason spawnType) {

        int count = 0;
        try {
            List<EntityWisp> nearby = level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(EntityWisp.class), getBoundingBox().inflate(16.0), e -> true);
            count = nearby.size();
        } catch (Exception ignored) {
        }

        if (count >= 8) return false;
        if (level.getDifficulty() == Difficulty.PEACEFUL) return false;

        return isValidLightLevel(level) && super.checkSpawnRules(level, spawnType);
    }

    private boolean isValidLightLevel(net.minecraft.world.level.LevelAccessor level) {
        BlockPos pos = new BlockPos((int) getX(), (int) getBoundingBox().minY, (int) getZ());

        if (level.getBrightness(LightLayer.SKY, pos) > random.nextInt(32)) {
            return false;
        }

        int lightLevel = level.getMaxLocalRawBrightness(pos);
        return lightLevel <= random.nextInt(8);
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 2;
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("WispType", getWispType());
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        setWispType(tag.getStringOr("WispType", ""));
    }
}