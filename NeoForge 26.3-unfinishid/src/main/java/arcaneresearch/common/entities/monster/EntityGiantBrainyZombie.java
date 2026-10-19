package arcaneresearch.common.entities.monster;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import arcaneresearch.init.ModEntities;

public class EntityGiantBrainyZombie extends EntityBrainyZombie {

    private static final EntityDataAccessor<Float> DATA_ANGER =
            SynchedEntityData.defineId(EntityGiantBrainyZombie.class, EntityDataSerializers.FLOAT);

    public EntityGiantBrainyZombie(EntityType<? extends EntityGiantBrainyZombie> type, Level level) {
        super(type, level);
        this.xpReward = 15;
    }

    public EntityGiantBrainyZombie(Level level) {
        super(ModEntities.GIANT_BRAINY_ZOMBIE.get(), level);
        this.xpReward = 15;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4f));
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ANGER, 1.0f);
    }

    public float getAnger() {
        return this.entityData.get(DATA_ANGER);
    }

    public void setAnger(float anger) {
        this.entityData.set(DATA_ANGER, anger);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (getAnger() > 1.0f) {
            setAnger(getAnger() - 0.002f);
            // NEOFORGE_PORT: Forge scaled dimensions on the fly via getDimensions override
            // (removed: final in LivingEntity). 26.x scales via the SCALE attribute instead.
            getAttribute(Attributes.SCALE).setBaseValue(getAnger());
            refreshDimensions();
        }

        if (!level().isClientSide()) {
            double baseDamage = 7.0 + (getAnger() - 1.0f) * 5.0;
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(baseDamage);
        }
    }

    // NEOFORGE_PORT: LivingEntity.getScale() is final (26.x drive scale through
    // the SCALE attribute instead); Forge getScale() == getAnger(), inlined above.

    // NEOFORGE_PORT: getDimensions is final in LivingEntity (26.x); dynamic anger
    // scale is applied through the SCALE attribute (see aiStep) instead.
    // NEOFORGE_PORT: getStandingEyeHeight removed in 26.3 (eye height via EntityDimensions.withEyeHeight).

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, float amount) {

        setAnger(Math.min(2.0f, getAnger() + 0.1f));
        refreshDimensions();
        return super.hurtServer(serverLevel, source, amount);
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, boolean wasRecentlyHit) {
        // NEOFORGE_PORT: looting param removed in 26.x; derived from the killer's weapon
        // (Enchantments.LOOTING level), same value the old caller passed.
        int looting = arcaneresearch.common.lib.utils.EntityUtils.getLootingLevel(serverLevel, source);

        for (int a = 0; a < 6; a++) {
            if (random.nextBoolean()) {
                spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), new ItemStack(Items.ROTTEN_FLESH, 2));
            }
        }
        for (int a = 0; a < 6; a++) {
            if (random.nextBoolean()) {
                spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), new ItemStack(Items.ROTTEN_FLESH, 2));
            }
        }
    }
}
