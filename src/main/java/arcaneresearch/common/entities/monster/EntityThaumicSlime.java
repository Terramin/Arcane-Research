package arcaneresearch.common.entities.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.entities.ITaintedMob;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.init.ModEntities;
import arcaneresearch.init.ModSounds;

import javax.annotation.Nullable;

public class EntityThaumicSlime extends Slime implements ITaintedMob {

    private int launched = 10;
    private int spitCounter = 100;
    private boolean wasOnGroundLastTick = false;

    public EntityThaumicSlime(EntityType<? extends EntityThaumicSlime> type, Level level) {
        super(type, level);
        int size = 1 << (1 + random.nextInt(3));
        setSize(size, true);
    }

    public EntityThaumicSlime(Level level) {
        super(ModEntities.THAUMIC_SLIME.get(), level);
        int size = 1 << (1 + random.nextInt(3));
        setSize(size, true);
    }

    public EntityThaumicSlime(Level level, LivingEntity shooter, LivingEntity target) {
        super(ModEntities.THAUMIC_SLIME.get(), level);
        setSize(1, true);
        launched = 10;

        double startY = (shooter.getBoundingBox().minY + shooter.getBoundingBox().maxY) / 2.0;

        double dx = target.getX() - shooter.getX();
        double dy = target.getBoundingBox().minY + target.getBbHeight() / 3.0f - startY;
        double dz = target.getZ() - shooter.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        if (dist >= 1.0E-7) {
            float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0f;
            float pitch = (float) (-Mth.atan2(dy, dist) * Mth.RAD_TO_DEG);

            double normalizedX = dx / dist;
            double normalizedZ = dz / dist;

            setPos(shooter.getX() + normalizedX, startY, shooter.getZ() + normalizedZ);
            setYRot(yaw);
            setXRot(pitch);
            yRotO = yaw;
            xRotO = pitch;

            float arc = (float) dist * 0.2f;
            shoot(dx, dy + arc, dz, 1.5f, 1.0f);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    public static boolean checkThaumicSlimeSpawnRules(EntityType<? extends EntityThaumicSlime> type, ServerLevelAccessor level,
                                                      EntitySpawnReason spawnType, BlockPos pos, RandomSource random) {

        return false;
    }

    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        double dist = Math.sqrt(x * x + y * y + z * z);
        x /= dist;
        y /= dist;
        z /= dist;

        x += random.nextGaussian() * 0.0075 * inaccuracy;
        y += random.nextGaussian() * 0.0075 * inaccuracy;
        z += random.nextGaussian() * 0.0075 * inaccuracy;

        x *= velocity;
        y *= velocity;
        z *= velocity;

        setDeltaMovement(x, y, z);

        double horizDist = Math.sqrt(x * x + z * z);
        setYRot((float) (Mth.atan2(x, z) * Mth.RAD_TO_DEG));
        setXRot((float) (Mth.atan2(y, horizDist) * Mth.RAD_TO_DEG));
        yRotO = getYRot();
        xRotO = getXRot();
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnData) {

        int sizeIndex = random.nextInt(3);
        if (sizeIndex < 2 && random.nextFloat() < 0.5f * difficulty.getSpecialMultiplier()) {
            sizeIndex++;
        }
        int size = 1 << sizeIndex;
        setSize(size, true);

        return super.finalizeSpawn(level, difficulty, spawnType, spawnData);
    }

    
    
    @Override
    public void setSize(int size, boolean resetHealth) {
        super.setSize(size, resetHealth);
        this.xpReward = size + 2;
    }

    @Override
    public void tick() {
        int size = getSize();

        if (onGround() && !wasOnGroundLastTick) {
            wasOnGroundLastTick = true;

            if (level().isClientSide()) {
                spawnSlimeJumpFX(size * 2, size);
            }

            playSound(getJumpSound(), getSoundVolume(),
                    (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f * 0.8f);
        } else if (!onGround()) {
            wasOnGroundLastTick = false;
        }

        super.tick();

        if (level().isClientSide()) {
            if (launched > 0) {
                launched--;
                spawnSlimeJumpFX(size * (launched + 1), size);
            }
        }

        if (!level().isClientSide() && isAlive()) {
            Player target = level().getNearestPlayer(this, 16.0);
            if (target != null) {
                if (spitCounter > 0) {
                    spitCounter--;
                }

                lookAt(target, 10.0f, 20.0f);

                if (distanceTo(target) > 4.0f && spitCounter <= 0 && getSize() > 2) {
                    spitCounter = 101;

                    EntityThaumicSlime spitSlime = new EntityThaumicSlime(level(), this, target);
                    level().addFreshEntity(spitSlime);

                    playSound(ModSounds.GORE.get(), 1.0f,
                            (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f * 0.8f);

                    setSize(getSize() - 1, true);
                }
            }
        }
    }

    private void spawnSlimeJumpFX(int count, int size) {
        for (int k = 0; k < count; k++) {
            FXDispatcher.INSTANCE.slimeJumpFX(this, size);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        int size = getSize();

        if (!level().isClientSide() && size > 1 && isDeadOrDying()) {
            for (int k = 0; k < size; k++) {
                float offsetX = (k % 2 - 0.5f) * size / 4.0f;
                float offsetZ = (k / 2 - 0.5f) * size / 4.0f;

                EntityThaumicSlime smallSlime = new EntityThaumicSlime(ModEntities.THAUMIC_SLIME.get(), level());
                smallSlime.setSize(1, true);
                
                smallSlime.snapTo(getX() + offsetX, getY() + 0.5, getZ() + offsetZ,
                        random.nextFloat() * 360.0f, 0.0f);
                level().addFreshEntity(smallSlime);
            }
        }

        super.remove(reason);
    }

    @Override
    protected float getAttackDamage() {
        return getSize() + 1;
    }

    
    @Override
    protected boolean canDealDamage() {
        return true;
    }

    @Override
    protected void dealDamage(LivingEntity target) {
        int size = getSize();
        int effectiveSize = launched > 0 ? size + 2 : size;

        if (isAlive() && hasLineOfSight(target) &&
                distanceToSqr(target) < 0.6 * effectiveSize * 0.6 * effectiveSize) {

            
            
            DamageSource attackSource = damageSources().mobAttack(this);
            if (target.level() instanceof net.minecraft.server.level.ServerLevel _sl
                    && target.hurtServer(_sl, attackSource, getAttackDamage())) {
                playSound(SoundEvents.SLIME_ATTACK, 1.0f,
                        (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f);
                net.minecraft.world.item.enchantment.EnchantmentHelper.doPostAttackEffects(_sl, target, attackSource);
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, boolean wasRecentlyHit) {
        
        
        int lootingLevel = arcaneresearch.common.lib.utils.EntityUtils.getLootingLevel(serverLevel, source);
        super.dropCustomDeathLoot(serverLevel, source, wasRecentlyHit);

        if (getSize() > 1) {
            spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), ArcaneresearchApiHelper.makeCrystal(Aspect.FLUX), getBbHeight() / 2.0f);
        }
    }

    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level, EntitySpawnReason spawnType) {

        return false;
    }
}