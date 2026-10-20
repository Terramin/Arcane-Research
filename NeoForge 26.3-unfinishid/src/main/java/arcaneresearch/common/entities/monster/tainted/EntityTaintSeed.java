package arcaneresearch.common.entities.monster.tainted;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.api.entities.ITaintedMob;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.blocks.world.taint.TaintHelper;
import arcaneresearch.common.world.aura.AuraHandler;
import arcaneresearch.init.ModEffects;
import arcaneresearch.init.ModEntities;
import arcaneresearch.init.ModSounds;

import java.util.List;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class EntityTaintSeed extends Monster implements ITaintedMob {

    public int boost = 0;
    public float attackAnim = 0.0f;
    private boolean firstRun = false;

    public EntityTaintSeed(EntityType<? extends EntityTaintSeed> type, Level level) {
        super(type, level);
        this.xpReward = 8;
    }

    public EntityTaintSeed(Level level) {
        this(ModEntities.TAINT_SEED.get(), level);
    }

    public static AttributeSupplier.Builder createAttributes() {

        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 75.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    protected int getArea() {
        return 1;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(net.minecraft.server.level.ServerLevel serverLevel, Entity target) {
        level().broadcastEntityEvent(this, (byte) 16);
        playSound(ModSounds.TENTACLE.get(), getSoundVolume(), getVoicePitch());
        return super.doHurtTarget(serverLevel, target);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 16) {
            attackAnim = 0.5f;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public boolean canAttack(LivingEntity target) {

        if (target instanceof ITaintedMob) {
            return false;
        }
        return super.canAttack(target);
    }

    @Override
    // NEOFORGE_PORT: Entity.isAlliedTo(Entity) final in 26.x -> considersEntityAsAlly (same body).
    protected boolean considersEntityAsAlly(Entity entity) {
        if (entity instanceof ITaintedMob) {
            return true;
        }
        return super.considersEntityAsAlly(entity);
    }

    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.EntitySpawnReason spawnType) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }

        double radius = arcaneresearch.common.config.ModConfig.taintSpreadArea * 0.8;
        List<EntityTaintSeed> nearby = level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(EntityTaintSeed.class), new AABB(getX() - radius, getY() - radius, getZ() - radius,
                        getX() + radius, getY() + radius, getZ() + radius), e -> true);

        return nearby.isEmpty() && checkNoEntityCollision(level) && !level.containsAnyLiquid(getBoundingBox());
    }

    private boolean checkNoEntityCollision(net.minecraft.world.level.LevelAccessor level) {
        return level.noCollision(this, getBoundingBox()) && level.getEntities(this, getBoundingBox()).isEmpty();
    }

    @Override
    public boolean isPickable() {

        return isAlive();
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {

            if (!firstRun || tickCount % 1200 == 0) {
                TaintHelper.removeTaintSeed(level(), blockPosition());
                TaintHelper.addTaintSeed(level(), blockPosition());
                firstRun = true;
            }

            if (isAlive()) {
                boolean tickFlag = tickCount % 20 == 0;

                if (boost > 0 || tickFlag) {

                    float mod = (boost > 0) ? 1.0f : getFluxSaturation();

                    if (boost > 0) {
                        --boost;
                    }

                    if (mod <= 0.0f) {

                        hurt(damageSources().starve(), 0.5f);

                        AuraHelper.polluteAura(level(), blockPosition(), 0.1f, false);
                    } else {

                        spreadTaint();
                    }
                }

                if (tickFlag) {

                    if (getTarget() != null &&
                            distanceToSqr(getTarget()) < getArea() * 256 &&
                            hasLineOfSight(getTarget())) {
                        spawnTentacles(getTarget());
                    }

                    List<LivingEntity> nearby = level().getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(LivingEntity.class), getBoundingBox().inflate(getArea() * 4.0), e -> e != this);

                    for (LivingEntity entity : nearby) {

                        entity.addEffect(new MobEffectInstance(ModEffects.FLUX_TAINT, 100, getArea() - 1, false, true));
                    }
                }
            }
        } else {
            clientTick();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void clientTick() {
        if (attackAnim > 0.0f) {
            attackAnim *= 0.75f;
        }
        if (attackAnim < 0.001f) {
            attackAnim = 0.0f;
        }

        float xx = 1.0f * Mth.sin(tickCount * 0.05f - 0.5f) / 5.0f;
        float zz = 1.0f * Mth.sin(tickCount * 0.06f - 0.5f) / 5.0f + hurtTime / 200.0f + attackAnim;
        if (random.nextFloat() < 0.033) {
            FXDispatcher.INSTANCE.drawLightningFlash((float) getX() + xx,
                    (float) getY() + getBbHeight() + 0.25f, (float) getZ() + zz,
                    0.7f, 0.1f, 0.9f, 0.5f, 1.5f + random.nextFloat());
        } else {
            FXDispatcher.INSTANCE.drawTaintParticles((float) getX() + xx,
                    (float) getY() + getBbHeight() + 0.25f, (float) getZ() + zz,
                    (float) random.nextGaussian() * 0.05f, 0.1f + 0.01f * random.nextFloat(),
                    (float) random.nextGaussian() * 0.05f, 2.0f);
        }
    }

    private float getFluxSaturation() {
        return AuraHandler.getFluxSaturation(level(), blockPosition());
    }

    private void spreadTaint() {
        int area = getArea();
        BlockPos spreadPos = blockPosition().offset(
                Mth.randomBetweenInclusive(random, -area * 3, area * 3),
                Mth.randomBetweenInclusive(random, -area, area),
                Mth.randomBetweenInclusive(random, -area * 3, area * 3));

        TaintHelper.spreadFibres(level(), spreadPos, true);
    }

    protected void spawnTentacles(Entity target) {

        boolean eldritch = level().getBiome(target.blockPosition()).unwrapKey()
                .map(k -> k.equals(arcaneresearch.common.world.biomes.BiomeHandler.ELDRITCH)).orElse(false);
        boolean onTaint = level().getBlockState(target.blockPosition()).getBlock()
                instanceof arcaneresearch.common.blocks.world.taint.ITaintBlock
                || level().getBlockState(target.blockPosition().below()).getBlock()
                instanceof arcaneresearch.common.blocks.world.taint.ITaintBlock;
        if (!eldritch && !onTaint) {
            return;
        }

        EntityTaintacleSmall tentacle = new EntityTaintacleSmall(level());
        // NEOFORGE_PORT (26.3): Entity.moveTo(x,y,z,yaw,pitch) -> snapTo (same body).
        tentacle.snapTo(target.getX() + random.nextFloat() - random.nextFloat(), target.getY(), target.getZ() + random.nextFloat() - random.nextFloat(), 0.0f, 0.0f);
        level().addFreshEntity(tentacle);

        if (eldritch && level().isEmptyBlock(target.blockPosition())
                && arcaneresearch.common.lib.utils.BlockUtils.isAdjacentToSolidBlock(level(), target.blockPosition())) {
            level().setBlockAndUpdate(target.blockPosition(),
                    arcaneresearch.init.ModBlocks.TAINT_FIBRE.get().defaultBlockState());
        }

        playSound(ModSounds.TENTACLE.get(), getSoundVolume(), getVoicePitch());
    }

    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.CHORUS_FLOWER_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 1.3f - getBbHeight() / 10.0f;
    }

    @Override
    protected float getSoundVolume() {
        return getBbHeight() / 8.0f;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.TENTACLE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.TENTACLE.get();
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, boolean wasRecentlyHit) {
        // NEOFORGE_PORT: looting param removed in 26.x; derived from the killer's weapon
        // (Enchantments.LOOTING level), same value the old caller passed.
        int looting = arcaneresearch.common.lib.utils.EntityUtils.getLootingLevel(serverLevel, source);

        spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), ArcaneresearchApiHelper.makeCrystal(Aspect.FLUX));
    }

    @Override
    public void die(DamageSource source) {
        TaintHelper.removeTaintSeed(level(), blockPosition());
        super.die(source);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(double x, double y, double z) {

    }

    @Override
    public void move(MoverType type, Vec3 movement) {

        if (movement.y > 0) {
            movement = new Vec3(0, 0, 0);
        } else {
            movement = new Vec3(0, movement.y, 0);
        }
        super.move(type, movement);
    }

    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("boost", boost);
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        boost = tag.getIntOr("boost", 0);
    }
}
