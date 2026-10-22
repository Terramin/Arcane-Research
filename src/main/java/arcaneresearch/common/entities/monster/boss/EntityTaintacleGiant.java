package arcaneresearch.common.entities.monster.boss;

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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import arcaneresearch.api.entities.IEldritchMob;
import arcaneresearch.api.entities.ITaintedMob;
import arcaneresearch.common.entities.monster.tainted.EntityTaintacle;
import arcaneresearch.init.ModEntities;
import arcaneresearch.init.ModItems;

import javax.annotation.Nullable;
import java.util.List;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;

public class EntityTaintacleGiant extends EntityTaintacle implements ITaintedMob, IEldritchMob {

    private static final EntityDataAccessor<Integer> DATA_ANGER =
            SynchedEntityData.defineId(EntityTaintacleGiant.class, EntityDataSerializers.INT);

    protected final ServerBossEvent bossEvent;

    public EntityTaintacleGiant(EntityType<? extends EntityTaintacleGiant> type, Level level) {
        super(type, level);
        this.xpReward = 20;
        this.length = 14;
        this.bossEvent = new ServerBossEvent(this.getUUID(), getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
        this.bossEvent.setDarkenScreen(true);
    }

    public EntityTaintacleGiant(Level level) {
        this(ModEntities.TAINTACLE_GIANT.get(), level);
    }

    public static AttributeSupplier.Builder createAttributes() {

        return EntityTaintacle.createAttributes()
                .add(Attributes.MAX_HEALTH, 175.0)
                .add(Attributes.ATTACK_DAMAGE, 9.0);
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
    public void tick() {
        super.tick();

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
    protected void customServerAiStep(net.minecraft.server.level.ServerLevel serverLevel) {
        super.customServerAiStep(serverLevel);
        this.bossEvent.setProgress(getHealth() / getMaxHealth());
    }

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, float amount) {
        if (!level().isClientSide()) {

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
                        player.sendOverlayMessage(Component.literal(getDisplayName().getString() + " ")
                                        .append(Component.translatable("tc.boss.enrage")));
                    }
                }
                amount = 35.0f;
            }
        }
        return super.hurtServer(serverLevel, source, amount);
    }

    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level, EntitySpawnReason spawnType) {
        return false;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnData) {

        if (arcaneresearch.common.entities.monster.mods.ChampionModifier.getChampionId(this) < 0) {
            arcaneresearch.common.entities.monster.mods.ChampionModifier.setChampionId(this,
                    level.getRandom().nextInt(
                            arcaneresearch.common.entities.monster.mods.ChampionModifier.MODS.length));
        }
        this.bossEvent.setName(getDisplayName());
        return spawnData;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
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
    public boolean isNoAi() {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, boolean wasRecentlyHit) {
        
        
        int looting = arcaneresearch.common.lib.utils.EntityUtils.getLootingLevel(serverLevel, source);

        AABB searchBox = getBoundingBox().inflate(48.0);
        List<EntityTaintacleGiant> others = level().getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(EntityTaintacleGiant.class), searchBox, e -> e != this);

        if (others.isEmpty()) {
            spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), new ItemStack(ModItems.PRIMORDIAL_PEARL.get()));
        }
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Anger", getAnger());
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        
        
        setAnger(tag.getIntOr("Anger", getAnger()));

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
