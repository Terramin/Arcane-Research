package arcaneresearch.common.entities.monster.boss;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import arcaneresearch.api.entities.IEldritchMob;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;

public abstract class EntityArcaneresearchBoss extends Monster {

    private static final EntityDataAccessor<Integer> DATA_ANGER =
            SynchedEntityData.defineId(EntityArcaneresearchBoss.class, EntityDataSerializers.INT);

    protected final ServerBossEvent bossEvent;
    private final Map<Integer, Integer> aggro = new HashMap<>();
    protected int spawnTimer = 0;

    private BlockPos homePos;
    private int homeDistance;

    public EntityArcaneresearchBoss(EntityType<? extends EntityArcaneresearchBoss> type, Level level) {
        super(type, level);
        this.xpReward = 50;
        this.bossEvent = new ServerBossEvent(this.getUUID(), getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
        this.bossEvent.setDarkenScreen(true);
    }

    public static AttributeSupplier.Builder createBossAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ANGER, 0);
    }

    public int getAnger() {
        return this.entityData.get(DATA_ANGER);
    }

    public void setAnger(int anger) {
        this.entityData.set(DATA_ANGER, anger);
    }

    public int getSpawnTimer() {
        return spawnTimer;
    }

    public void setHomePos(BlockPos pos, int distance) {
        this.homePos = pos;
        this.homeDistance = distance;
        setHomeTo(pos, distance);
    }

    public boolean hasHome() {
        return homePos != null && homeDistance > 0;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    protected void customServerAiStep(net.minecraft.server.level.ServerLevel serverLevel) {
        if (spawnTimer == 0) {
            super.customServerAiStep(serverLevel);
        }

        if (getTarget() != null && !getTarget().isAlive()) {
            setTarget(null);
        }

        this.bossEvent.setProgress(getHealth() / getMaxHealth());
    }

    @Override
    public void tick() {
        super.tick();

        if (spawnTimer > 0) {
            --spawnTimer;
        }

        if (getAnger() > 0) {
            setAnger(getAnger() - 1);
        }

        if (level().isClientSide() && random.nextInt(15) == 0 && getAnger() > 0) {
            double dx = random.nextGaussian() * 0.02;
            double dy = random.nextGaussian() * 0.02;
            double dz = random.nextGaussian() * 0.02;
            level().addParticle(ParticleTypes.ANGRY_VILLAGER,
                    getX() + random.nextFloat() * getBbWidth() - getBbWidth() / 2.0,
                    getBoundingBox().minY + getBbHeight() + random.nextFloat() * 0.5,
                    getZ() + random.nextFloat() * getBbWidth() - getBbWidth() / 2.0,
                    dx, dy, dz);
        }

        if (!level().isClientSide() && tickCount % 30 == 0) {
            heal(1.0f);
        }
    }

    @Override
    public boolean isInvulnerableTo(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source) {
        return super.isInvulnerableTo(serverLevel, source) || spawnTimer > 0;
    }

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, float amount) {
        if (!level().isClientSide()) {

            if (source.getEntity() instanceof LivingEntity living) {
                int targetId = living.getId();
                int currentAggro = aggro.getOrDefault(targetId, 0);
                aggro.put(targetId, currentAggro + (int) amount);
            }

            if (amount > 35.0f) {
                if (getAnger() == 0) {

                    try {
                        addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, (int) (amount / 15.0f)));
                        addEffect(new MobEffectInstance(MobEffects.STRENGTH, 200, (int) (amount / 10.0f)));
                        addEffect(new MobEffectInstance(MobEffects.HASTE, 200, (int) (amount / 40.0f)));
                    } catch (Exception ignored) {
                    }
                    setAnger(200);

                    if (source.getEntity() instanceof Player player) {
                        player.sendOverlayMessage(Component.translatable("tc.boss.enrage", getDisplayName()));
                    }
                }
                amount = 35.0f;
            }
        }
        return super.hurtServer(serverLevel, source, amount);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnData) {

        setHomeTo(blockPosition(), 24);
        generateName();
        this.bossEvent.setName(getDisplayName());
        return spawnData;
    }

    public void generateName() {

    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }

    @Override
    public void makeStuckInBlock(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.phys.Vec3 motion) {

    }

    @Override
    public boolean canPickUpLoot() {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, boolean wasRecentlyHit) {
        // NEOFORGE_PORT: looting param removed in 26.x; derived from the killer's weapon
        // (Enchantments.LOOTING level), same value the old caller passed.
        int lootingLevel = arcaneresearch.common.lib.utils.EntityUtils.getLootingLevel(serverLevel, source);
        super.dropCustomDeathLoot(serverLevel, source, wasRecentlyHit);
        spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), new ItemStack(arcaneresearch.init.ModItems.PRIMORDIAL_PEARL.get()));
        spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), new ItemStack(arcaneresearch.init.ModItems.LOOT_BAG_RARE.get()));
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean isPushable() {
        // NEOFORGE_PORT: isInvulnerableTo(DamageSource) -> (ServerLevel, DamageSource).
        if (!(level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return super.isPushable();
        }
        return !isInvulnerableTo(serverLevel, damageSources().starve());
    }

    @Override
    // NEOFORGE_PORT: Entity.isAlliedTo(Entity) final in 26.x -> considersEntityAsAlly (same body).
    protected boolean considersEntityAsAlly(Entity entity) {
        if (entity instanceof IEldritchMob) {
            return true;
        }
        return super.considersEntityAsAlly(entity);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        if (homePos != null && homeDistance > 0) {
            tag.putInt("HomeD", homeDistance);
            tag.putInt("HomeX", homePos.getX());
            tag.putInt("HomeY", homePos.getY());
            tag.putInt("HomeZ", homePos.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        if (tag.getInt("HomeD").isPresent()) {
            setHomePos(new BlockPos(
                            tag.getIntOr("HomeX", 0),
                            tag.getIntOr("HomeY", 0),
                            tag.getIntOr("HomeZ", 0)),
                    tag.getIntOr("HomeD", 0));
        }

        if (hasCustomName()) {
            this.bossEvent.setName(getDisplayName());
        }
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(getDisplayName());
    }
}
