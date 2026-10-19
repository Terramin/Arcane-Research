package arcaneresearch.common.entities.monster.tainted;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.entities.ITaintedMob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.blocks.world.taint.ITaintBlock;
import arcaneresearch.common.lib.utils.BlockUtils;
import arcaneresearch.common.world.biomes.BiomeHandler;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModEntities;
import arcaneresearch.init.ModSounds;
public class EntityTaintacle extends Monster implements ITaintedMob {
    public float flailIntensity = 1.0f;
    protected int length = 10;
    public EntityTaintacle(EntityType<? extends EntityTaintacle> type, Level level) {
        super(type, level);
        this.xpReward = 8;
    }
    public EntityTaintacle(Level level) {
        super(ModEntities.TAINTACLE.get(), level);
        this.xpReward = 8;
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 50.0)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
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
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (level.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) {
            return false;
        }
        net.minecraft.core.BlockPos pos = blockPosition();
        boolean onTaint = level.getBlockState(pos).getBlock() instanceof ITaintBlock
                || level.getBlockState(pos.below()).getBlock() instanceof ITaintBlock;
        return onTaint && level.noCollision(this);
    }
    @Override
    public boolean isAlliedTo(Entity other) {
        if (isTaintedMob(other)) {
            return true;
        }
        return super.isAlliedTo(other);
    }
    @Override
    public void move(MoverType type, Vec3 movement) {
        double clampedX = 0;
        double clampedY = movement.y > 0 ? 0 : movement.y;
        double clampedZ = 0;
        super.move(type, new Vec3(clampedX, clampedY, clampedZ));
    }
    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount % 20 == 0) {
            boolean onTaint = level().getBlockState(blockPosition()).getBlock() instanceof ITaintBlock
                    || level().getBlockState(blockPosition().below()).getBlock() instanceof ITaintBlock;
            if (!onTaint) {
                hurt(damageSources().starve(), 1.0f);
            }
            if (!(this instanceof EntityTaintacleSmall) && tickCount % 40 == 0) {
                LivingEntity target = getTarget();
                if (target != null) {
                    double distSq = distanceToSqr(target);
                    if (distSq > 16.0 && distSq < 256.0 && hasLineOfSight(target)) {
                        spawnSmallTentacle(target);
                    }
                }
            }
        }
        if (level().isClientSide) {
            clientTick();
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void clientTick() {
        if (flailIntensity > 1.0f) {
            flailIntensity -= 0.01f;
        }
        if (tickCount < getBbHeight() * 10.0f && onGround()) {
            FXDispatcher.INSTANCE.tentacleAriseFX(this);
        }
    }
    protected void spawnSmallTentacle(Entity target) {
        boolean eldritch = level().getBiome(target.blockPosition()).unwrapKey()
                .map(k -> k.equals(BiomeHandler.ELDRITCH)).orElse(false);
        boolean onTaint = level().getBlockState(target.blockPosition()).getBlock() instanceof ITaintBlock
                || level().getBlockState(target.blockPosition().below()).getBlock() instanceof ITaintBlock;
        if (!eldritch && !onTaint) return;
        EntityTaintacleSmall smallTentacle = new EntityTaintacleSmall(level());
        smallTentacle.moveTo(
                target.getX() + random.nextFloat() - random.nextFloat(),
                target.getY(),
                target.getZ() + random.nextFloat() - random.nextFloat(),
                0.0f, 0.0f);
        level().addFreshEntity(smallTentacle);
        if (eldritch && level().isEmptyBlock(target.blockPosition())
                && BlockUtils.isAdjacentToSolidBlock(level(), target.blockPosition())
                && ModBlocks.TAINT_FIBRE != null && ModBlocks.TAINT_FIBRE.get() != null) {
            level().setBlockAndUpdate(target.blockPosition(), ModBlocks.TAINT_FIBRE.get().defaultBlockState());
        }
        playSound(ModSounds.TENTACLE.get(), getSoundVolume(), getVoicePitch());
    }
    public void faceEntity(Entity target, float maxYawChange) {
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        float targetYaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0f;
        setYRot(rotlerp(getYRot(), targetYaw, maxYawChange));
    }
    private float rotlerp(float current, float target, float maxChange) {
        float diff = Mth.wrapDegrees(target - current);
        if (diff > maxChange) diff = maxChange;
        if (diff < -maxChange) diff = -maxChange;
        return current + diff;
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
    public int getLength() {
        return this.length;
    }
    @Override
    public void handleEntityEvent(byte id) {
        if (id == 16) {
            flailIntensity = 3.0f;
        } else {
            super.handleEntityEvent(id);
        }
    }
    @Override
    public boolean doHurtTarget(Entity target) {
        level().broadcastEntityEvent(this, (byte) 16);
        playSound(ModSounds.TENTACLE.get(), getSoundVolume(), getVoicePitch());
        return super.doHurtTarget(target);
    }
    @Override
    protected void dropCustomDeathLoot(DamageSource source, int lootingLevel, boolean wasRecentlyHit) {
        super.dropCustomDeathLoot(source, lootingLevel, wasRecentlyHit);
        spawnAtLocation(ArcaneresearchApiHelper.makeCrystal(Aspect.FLUX));
    }
}
