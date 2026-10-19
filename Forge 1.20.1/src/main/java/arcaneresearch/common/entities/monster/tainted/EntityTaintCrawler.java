package arcaneresearch.common.entities.monster.tainted;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.entities.ITaintedMob;
import arcaneresearch.common.blocks.world.taint.BlockTaintFibre;
import arcaneresearch.common.blocks.world.taint.ITaintBlock;
import arcaneresearch.common.lib.utils.BlockUtils;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModEffects;
import arcaneresearch.init.ModEntities;
public class EntityTaintCrawler extends Monster implements ITaintedMob {
    private BlockPos lastPos = BlockPos.ZERO;
    public EntityTaintCrawler(EntityType<? extends EntityTaintCrawler> type, Level level) {
        super(type, level);
        this.xpReward = 3;
    }
    public EntityTaintCrawler(Level level) {
        super(ModEntities.TAINT_CRAWLER.get(), level);
        this.xpReward = 3;
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.275)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, EntityTaintCrawler.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
    @Override
    protected float getStandingEyeHeight(net.minecraft.world.entity.Pose pose, net.minecraft.world.entity.EntityDimensions dimensions) {
        return 0.1f;
    }
    @Override
    public float getVoicePitch() {
        return 0.7f;
    }
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SILVERFISH_AMBIENT;
    }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SILVERFISH_HURT;
    }
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SILVERFISH_DEATH;
    }
    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.SILVERFISH_STEP, 0.15f, 1.0f);
    }
    @Override
    public MobType getMobType() {
        return MobType.ARTHROPOD;
    }
    public boolean isTaintedMob(Entity entity) {
        if (entity instanceof arcaneresearch.api.entities.ITaintedMob) return true;
        return entity instanceof EntityTaintCrawler ||
                entity instanceof EntityTaintSwarm ||
                entity instanceof EntityTaintacle;
    }
    @Override
    public boolean canAttack(net.minecraft.world.entity.LivingEntity target) {
        if (isTaintedMob(target)) {
            return false;
        }
        return super.canAttack(target);
    }
    @Override
    public boolean isAlliedTo(Entity other) {
        if (isTaintedMob(other)) {
            return true;
        }
        return super.isAlliedTo(other);
    }
    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        return level.noCollision(this);
    }
    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }
    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && isAlive() && tickCount % 40 == 0 && !lastPos.equals(blockPosition())) {
            lastPos = blockPosition();
            BlockPos pos = blockPosition();
            BlockState bs = level().getBlockState(pos);
            if (!bs.is(BlockTags.LEAVES)
                    && bs.getFluidState().isEmpty()
                    && !(bs.getBlock() instanceof ITaintBlock)
                    && (level().isEmptyBlock(pos) || bs.canBeReplaced()
                    || bs.getBlock() instanceof net.minecraft.world.level.block.FlowerBlock
                    || bs.getBlock() instanceof IPlantable)
                    && BlockUtils.isAdjacentToSolidBlock(level(), pos)
                    && !BlockTaintFibre.isOnlyAdjacentToTaint(level(), pos)) {
                level().setBlockAndUpdate(pos, ModBlocks.TAINT_FIBRE.get().defaultBlockState());
            }
        }
    }
    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            if (target instanceof net.minecraft.world.entity.LivingEntity living) {
                int duration = 0;
                Difficulty difficulty = level().getDifficulty();
                if (difficulty == Difficulty.NORMAL) {
                    duration = 3;
                } else if (difficulty == Difficulty.HARD) {
                    duration = 6;
                }
                if (duration > 0 && random.nextInt(duration + 1) > 2) {
                    living.addEffect(new MobEffectInstance(ModEffects.FLUX_TAINT.get(), duration * 20, 0));
                }
            }
            return true;
        }
        return false;
    }
    @Override
    protected void dropCustomDeathLoot(DamageSource source, int lootingLevel, boolean wasRecentlyHit) {
        super.dropCustomDeathLoot(source, lootingLevel, wasRecentlyHit);
        if (random.nextInt(8) == 0) {
            this.spawnAtLocation(ArcaneresearchApiHelper.makeCrystal(Aspect.FLUX), getBbHeight() / 2.0f);
        }
    }
}
