package arcaneresearch.common.entities.monster.boss;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.api.entities.IEldritchMob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.entities.projectile.EntityGolemOrb;
import arcaneresearch.init.ModEntities;
import arcaneresearch.init.ModSounds;
import javax.annotation.Nullable;
public class EntityEldritchGolem extends EntityArcaneresearchBoss implements IEldritchMob, RangedAttackMob {
    private static final EntityDataAccessor<Boolean> DATA_HEADLESS =
            SynchedEntityData.defineId(EntityEldritchGolem.class, EntityDataSerializers.BOOLEAN);
    private int beamCharge = 0;
    private boolean chargingBeam = false;
    private int attackTimer = 0;
    private int arcing = 0;
    private int arcX;
    private int arcY;
    private int arcZ;
    public EntityEldritchGolem(EntityType<? extends EntityEldritchGolem> type, Level level) {
        super(type, level);
        this.xpReward = 50;
    }
    public EntityEldritchGolem(Level level) {
        this(ModEntities.ELDRITCH_GOLEM.get(), level);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return EntityArcaneresearchBoss.createBossAttributes()
                .add(Attributes.MAX_HEALTH, 400.0)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ARMOR, 6.0);
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_HEADLESS, false);
    }
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, false));
        this.goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.8));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
    public boolean isHeadless() {
        return this.entityData.get(DATA_HEADLESS);
    }
    public void setHeadless(boolean headless) {
        this.entityData.set(DATA_HEADLESS, headless);
    }
    private void makeHeadless() {
        this.goalSelector.addGoal(2, new arcaneresearch.common.entities.ai.combat.AILongRangeAttack(this, 3.0, 1.0, 5, 5, 24.0f));
    }
    @Override
    public float getEyeHeight(net.minecraft.world.entity.Pose pose) {
        return isHeadless() ? 3.33f : 3.0f;
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("headless", isHeadless());
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setHeadless(tag.getBoolean("headless"));
        if (isHeadless()) {
            makeHeadless();
        }
    }
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        spawnTimer = 100;
        return super.finalizeSpawn(level, difficulty, spawnType, spawnData, tag);
    }
    @Override
    public void generateName() {
        int t = arcaneresearch.common.entities.monster.mods.ChampionModifier.getChampionId(this);
        if (t >= 0 && t < arcaneresearch.common.entities.monster.mods.ChampionModifier.MODS.length) {
            setCustomName(net.minecraft.network.chat.Component.literal(String.format(
                    net.minecraft.network.chat.Component.translatable("entity.arcaneresearch.eldritch_golem.name.custom").getString(),
                    arcaneresearch.common.entities.monster.mods.ChampionModifier.MODS[t].getModNameLocalized())));
        }
    }
    @Override
    public void aiStep() {
        super.aiStep();
        if (attackTimer > 0) {
            --attackTimer;
        }
        Vec3 motion = getDeltaMovement();
        if (motion.x * motion.x + motion.z * motion.z > 2.5E-7 && random.nextInt(5) == 0) {
            BlockState bs = level().getBlockState(blockPosition());
            if (!bs.isAir()) {
                level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, bs),
                        getX() + (random.nextFloat() - 0.5) * getBbWidth(),
                        getBoundingBox().minY + 0.1,
                        getZ() + (random.nextFloat() - 0.5) * getBbWidth(),
                        4.0 * (random.nextFloat() - 0.5), 0.5, (random.nextFloat() - 0.5) * 4.0);
            }
        }
        if (!level().isClientSide) {
            BlockPos pos = blockPosition();
            BlockState bs = level().getBlockState(pos);
            float hardness = bs.getDestroySpeed(level(), pos);
            if (hardness >= 0.0f && hardness <= 0.15f) {
                level().destroyBlock(pos, true);
            }
        }
    }
    @Override
    public void tick() {
        if (spawnTimer == 150) {
            level().broadcastEntityEvent(this, (byte) 18);
        }
        if (spawnTimer > 0) {
            heal(2.0f);
        }
        super.tick();
        if (!level().isClientSide) {
            if (isHeadless() && beamCharge <= 0) {
                chargingBeam = true;
            }
            if (isHeadless() && chargingBeam) {
                ++beamCharge;
                level().broadcastEntityEvent(this, (byte) 19);
                if (beamCharge == 150) {
                    chargingBeam = false;
                }
            }
        }
        if (level().isClientSide && isHeadless()) {
            headlessClientTick();
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void headlessClientTick() {
        setXRot(0.0f);
        float yawRad = -yBodyRot * Mth.DEG_TO_RAD - Mth.PI;
        float lookX = Mth.sin(yawRad);
        float lookZ = Mth.cos(yawRad);
        if (random.nextInt(20) == 0) {
            float a = (random.nextFloat() - random.nextFloat()) / 3.0f;
            float b = (random.nextFloat() - random.nextFloat()) / 3.0f;
            FXDispatcher.INSTANCE.spark((float) (getX() + lookX + a),
                    (float) getY() + getEyeHeight() - 0.75f, (float) (getZ() + lookZ + b),
                    3.0f, 0.65f + random.nextFloat() * 0.1f, 1.0f, 1.0f, 0.8f);
        }
        FXDispatcher.INSTANCE.drawVentParticles((float) getX() + lookX * 0.4f,
                (float) getY() + getEyeHeight() - 1.25f, (float) getZ() + lookZ * 0.4f,
                0.0, 0.001, 0.0, 5592405, 4.0f);
        if (arcing > 0) {
            FXDispatcher.INSTANCE.arcLightning(getX(), getY() + getBbHeight() / 2.0f, getZ(),
                    arcX + 0.5, arcY + 1, arcZ + 0.5,
                    0.65f + random.nextFloat() * 0.1f, 1.0f, 1.0f, 1.0f - arcing / 10.0f);
            --arcing;
        }
    }
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide && amount > getHealth() && !isHeadless()) {
            setHeadless(true);
            spawnTimer = 100;
            double xx = Mth.cos(yBodyRot * Mth.DEG_TO_RAD) * 0.75f;
            double zz = Mth.sin(yBodyRot * Mth.DEG_TO_RAD) * 0.75f;
            level().explode(this, getX() + xx, getY() + getEyeHeight(), getZ() + zz, 2.0f, Level.ExplosionInteraction.NONE);
            makeHeadless();
            return false;
        }
        return super.hurt(source, amount);
    }
    @Override
    public boolean doHurtTarget(Entity target) {
        if (attackTimer > 0) {
            return false;
        }
        attackTimer = 10;
        level().broadcastEntityEvent(this, (byte) 4);
        float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.75f;
        boolean hit = target.hurt(damageSources().mobAttack(this), damage);
        if (hit) {
            target.setDeltaMovement(target.getDeltaMovement().add(0, 0.2, 0));
            if (isHeadless() && target instanceof LivingEntity) {
                float knockX = -Mth.sin(getYRot() * Mth.DEG_TO_RAD) * 1.5f;
                float knockZ = Mth.cos(getYRot() * Mth.DEG_TO_RAD) * 1.5f;
                target.setDeltaMovement(target.getDeltaMovement().add(knockX, 0.1, knockZ));
            }
        }
        return hit;
    }
    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (hasLineOfSight(target) && !chargingBeam && beamCharge > 0) {
            beamCharge -= 15 + random.nextInt(5);
            getLookControl().setLookAt(target, 30.0f, 30.0f);
            Vec3 look = getLookAngle();
            EntityGolemOrb orb = new EntityGolemOrb(level(), this, target, false);
            orb.setPos(orb.getX() + look.x, orb.getY(), orb.getZ() + look.z);
            double dx = target.getX() + target.getDeltaMovement().x - getX();
            double dy = target.getY() - getY() - target.getBbHeight() / 2.0;
            double dz = target.getZ() + target.getDeltaMovement().z - getZ();
            orb.shoot(dx, dy, dz, 0.66f, 5.0f);
            playSound(ModSounds.EG_ATTACK.get(), 1.0f, 1.0f + random.nextFloat() * 0.1f);
            level().addFreshEntity(orb);
        }
    }
    @Override
    public void handleEntityEvent(byte id) {
        if (id == 4) {
            attackTimer = 10;
            playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0f, 1.0f);
        } else if (id == 18) {
            spawnTimer = 150;
        } else if (id == 19) {
            if (arcing == 0) {
                float radius = 2.0f + random.nextFloat() * 2.0f;
                double radians = Math.toRadians(random.nextInt(360));
                int bx = net.minecraft.util.Mth.floor(getX() + radius * Math.cos(radians));
                int by = net.minecraft.util.Mth.floor(getY());
                int bz = net.minecraft.util.Mth.floor(getZ() + radius * Math.sin(radians));
                net.minecraft.core.BlockPos bp = new net.minecraft.core.BlockPos(bx, by, bz);
                for (int c = 0; c < 5 && level().isEmptyBlock(bp); ++c, --by) {
                    bp = bp.below();
                }
                by = bp.getY();
                if (level().isEmptyBlock(bp.above()) && !level().isEmptyBlock(bp)) {
                    arcX = bx;
                    arcY = by;
                    arcZ = bz;
                    arcing = 8 + random.nextInt(5);
                    playSound(arcaneresearch.init.ModSounds.JACOBS.get(), 0.8f,
                            1.0f + (random.nextFloat() - random.nextFloat()) * 0.05f);
                }
            }
        } else {
            super.handleEntityEvent(id);
        }
    }
    public int getAttackTimer() {
        return attackTimer;
    }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }
    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.IRON_GOLEM_STEP, 1.0f, 1.0f);
    }
    @Override
    public boolean fireImmune() {
        return true;
    }
}
