package arcaneresearch.common.entities.monster;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.api.entities.IEldritchMob;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.entities.monster.cult.EntityCultist;
import arcaneresearch.common.entities.projectile.EntityEldritchOrb;
import arcaneresearch.common.items.resources.ItemWispEssence;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.fx.PacketFXSonic;
import arcaneresearch.common.lib.network.misc.PacketMiscEvent;
import arcaneresearch.common.world.biomes.BiomeHandler;
import arcaneresearch.init.ModEntities;
import arcaneresearch.init.ModItems;
import arcaneresearch.init.ModSounds;
import javax.annotation.Nullable;
import java.util.List;
public class EntityEldritchGuardian extends Monster implements RangedAttackMob, IEldritchMob {
    public float armLiftL = 0.0f;
    public float armLiftR = 0.0f;
    private boolean lastBlast = false;
    private BlockPos homePos;
    private int homeDistance;
    public EntityEldritchGuardian(EntityType<? extends EntityEldritchGuardian> type, Level level) {
        super(type, level);
        this.xpReward = 20;
        ((GroundPathNavigation) getNavigation()).setCanOpenDoors(true);
    }
    public EntityEldritchGuardian(Level level) {
        this(ModEntities.ELDRITCH_GUARDIAN.get(), level);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 50.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new arcaneresearch.common.entities.ai.combat.AILongRangeAttack(this, 8.0, 1.0, 20, 40, 24.0f));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 0.8));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, EntityCultist.class, true));
    }
    @Override
    public int getArmorValue() {
        return 4;
    }
    @Override
    public boolean canPickUpLoot() {
        return false;
    }
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(net.minecraft.tags.DamageTypeTags.WITCH_RESISTANT_TO)) {
            amount /= 2.0f;
        }
        return super.hurt(source, amount);
    }
    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (armLiftL > 0.0f) {
                armLiftL -= 0.05f;
            }
            if (armLiftR > 0.0f) {
                armLiftR -= 0.05f;
            }
            spawnWispTrail();
        } else {
            if ((tickCount == 0 || tickCount % 100 == 0)
                    && level().getDifficulty() != Difficulty.EASY
                    && !isOuterLands()) {
                double range = level().getDifficulty() == Difficulty.HARD ? 576.0 : 256.0;
                for (Player player : level().players()) {
                    if (player.isAlive() && player.distanceToSqr(getX(), getY(), getZ()) < range
                            && player instanceof ServerPlayer serverPlayer) {
                        PacketHandler.sendToPlayer(new PacketMiscEvent(PacketMiscEvent.MIST_EVENT_SHORT), serverPlayer);
                    }
                }
            }
        }
    }
    private boolean isOuterLands() {
        try {
            return level().dimension().equals(
                    arcaneresearch.common.world.maze.MazeFeature.DIMENSION);
        } catch (Exception e) {
            return false;
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void spawnWispTrail() {
        float x = (float) (getX() + (random.nextFloat() - random.nextFloat()) * 0.2f);
        float z = (float) (getZ() + (random.nextFloat() - random.nextFloat()) * 0.2f);
        FXDispatcher.INSTANCE.wispFXEG(x, (float) (getY() + 0.22 * getBbHeight()), z, this);
    }
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) {
            int difficulty = level().getDifficulty().getId();
            if (getMainHandItem().isEmpty() && isOnFire() && random.nextFloat() < difficulty * 0.3f) {
                target.setSecondsOnFire(2 * difficulty);
            }
        }
        return hit;
    }
    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (random.nextFloat() > 0.15f) {
            EntityEldritchOrb orb = new EntityEldritchOrb(level(), this);
            lastBlast = !lastBlast;
            level().broadcastEntityEvent(this, (byte) (lastBlast ? 16 : 15));
            int rotation = lastBlast ? 90 : 180;
            double offsetX = Math.cos(Math.toRadians((getYRot() + rotation) % 360.0f)) * 0.5f;
            double offsetZ = Math.sin(Math.toRadians((getYRot() + rotation) % 360.0f)) * 0.5f;
            orb.setPos(orb.getX() - offsetX, orb.getY(), orb.getZ() - offsetZ);
            Vec3 targetVel = target.getDeltaMovement().scale(10.0);
            Vec3 direction = target.position().add(targetVel).subtract(position()).normalize();
            orb.shoot(direction.x, direction.y, direction.z, 1.1f, 2.0f);
            playSound(ModSounds.EG_ATTACK.get(), 2.0f, 1.0f + random.nextFloat() * 0.1f);
            level().addFreshEntity(orb);
        } else if (hasLineOfSight(target)) {
            if (!level().isClientSide) {
                PacketHandler.sendToAllTracking(new PacketFXSonic(this), this);
            }
            try {
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 400, 0));
            } catch (Exception ignored) {
            }
            if (target instanceof Player player) {
                ArcaneresearchApi.internalMethods.addWarpToPlayer(player, 1 + random.nextInt(3), IPlayerWarp.EnumWarpType.TEMPORARY);
            }
            playSound(ModSounds.EG_SCREECH.get(), 3.0f, 1.0f + random.nextFloat() * 0.1f);
        }
    }
    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case 15 -> armLiftL = 0.5f;
            case 16 -> armLiftR = 0.5f;
            case 17 -> {
                armLiftL = 0.9f;
                armLiftR = 0.9f;
            }
            default -> super.handleEntityEvent(id);
        }
    }
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.EG_IDLE.get();
    }
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.EG_DEATH.get();
    }
    @Override
    public int getAmbientSoundInterval() {
        return 500;
    }
    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }
    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }
    @Override
    public float getEyeHeight(net.minecraft.world.entity.Pose pose) {
        return 2.1f;
    }
    public void setHomePos(BlockPos pos, int distance) {
        this.homePos = pos;
        this.homeDistance = distance;
        restrictTo(pos, distance);
    }
    public boolean hasHome() {
        return homePos != null && homeDistance > 0;
    }
    @Override
    public boolean removeWhenFarAway(double distance) {
        return !hasHome();
    }
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level, MobSpawnType spawnType) {
        List<EntityEldritchGuardian> nearby = level.getEntitiesOfClass(EntityEldritchGuardian.class,
                new AABB(getX() - 32, getY() - 16, getZ() - 32,
                        getX() + 32, getY() + 16, getZ() + 32));
        return nearby.isEmpty() && super.checkSpawnRules(level, spawnType);
    }
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        spawnData = super.finalizeSpawn(level, difficulty, spawnType, spawnData, tag);
        try {
            boolean eldritch = level.getBiome(blockPosition()).unwrapKey()
                    .map(k -> k.equals(BiomeHandler.ELDRITCH)).orElse(false);
            if (eldritch) {
                int bonus = (int) getMaxHealth() / 2;
                setAbsorptionAmount(getAbsorptionAmount() + bonus);
            }
        } catch (Exception ignored) {
        }
        return spawnData;
    }
    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof IEldritchMob) {
            return true;
        }
        return super.isAlliedTo(entity);
    }
    @Override
    protected void dropCustomDeathLoot(DamageSource source, int lootingLevel, boolean wasRecentlyHit) {
        super.dropCustomDeathLoot(source, lootingLevel, wasRecentlyHit);
        if (this.random.nextBoolean()) {
            this.spawnAtLocation(ItemWispEssence.createStack(
                    ModItems.WISP_ESSENCE.get(), Aspect.UNDEAD, 1));
        }
        if (this.random.nextBoolean()) {
            this.spawnAtLocation(ItemWispEssence.createStack(
                    ModItems.WISP_ESSENCE.get(), Aspect.ELDRITCH, 1));
        }
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (homePos != null && homeDistance > 0) {
            tag.putInt("HomeD", homeDistance);
            tag.putInt("HomeX", homePos.getX());
            tag.putInt("HomeY", homePos.getY());
            tag.putInt("HomeZ", homePos.getZ());
        }
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("HomeD")) {
            setHomePos(new BlockPos(
                            tag.getInt("HomeX"),
                            tag.getInt("HomeY"),
                            tag.getInt("HomeZ")),
                    tag.getInt("HomeD"));
        }
    }
}
