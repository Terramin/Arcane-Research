package arcaneresearch.common.entities.monster;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import arcaneresearch.init.ModEntities;

import javax.annotation.Nullable;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;

public class EntityMindSpider extends Spider {

    private static final EntityDataAccessor<Boolean> DATA_HARMLESS =
            SynchedEntityData.defineId(EntityMindSpider.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> DATA_VIEWER =
            SynchedEntityData.defineId(EntityMindSpider.class, EntityDataSerializers.STRING);

    private int lifeSpan = Integer.MAX_VALUE;

    public EntityMindSpider(EntityType<? extends EntityMindSpider> type, Level level) {
        super(type, level);
        this.xpReward = 1;
    }

    public EntityMindSpider(Level level) {
        super(ModEntities.MIND_SPIDER.get(), level);
        this.xpReward = 1;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Spider.createAttributes()
                .add(Attributes.MAX_HEALTH, 1.0)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_HARMLESS, false);
        builder.define(DATA_VIEWER, "");
    }

    public String getViewer() {
        return this.entityData.get(DATA_VIEWER);
    }

    public void setViewer(String player) {
        this.entityData.set(DATA_VIEWER, player != null ? player : "");
    }

    public boolean isHarmless() {
        return this.entityData.get(DATA_HARMLESS);
    }

    public void setHarmless(boolean harmless) {
        if (harmless) {
            this.lifeSpan = 1200;
        }
        this.entityData.set(DATA_HARMLESS, harmless);
    }
    // NEOFORGE_PORT: getStandingEyeHeight removed in 26.3 (eye height via EntityDimensions.withEyeHeight).

    @Override
    protected float getSoundVolume() {
        return super.getSoundVolume() * 0.7f;
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide() && tickCount > lifeSpan) {
            discard();
        }
    }

    // NEOFORGE_PORT: getExperienceReward() is final in 26.x; harmless zero-XP
    // goes through the protected getBaseExperienceReward hook instead.
    @Override
    protected int getBaseExperienceReward(net.minecraft.server.level.ServerLevel level) {
        return isHarmless() ? 0 : super.getBaseExperienceReward(level);
    }

    @Override
    public boolean doHurtTarget(net.minecraft.server.level.ServerLevel serverLevel, Entity target) {
        if (isHarmless()) {
            return false;
        }
        return super.doHurtTarget(serverLevel, target);
    }

    @Override
    public boolean isNoGravity() {
        return false;
    }

    // NEOFORGE_PORT: 26.x shouldDropLoot takes the server level.
    @Override
    protected boolean shouldDropLoot(net.minecraft.server.level.ServerLevel level) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Harmless", isHarmless());
        tag.putString("Viewer", getViewer());
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        setHarmless(tag.getBooleanOr("Harmless", false));
        setViewer(tag.getStringOr("Viewer", ""));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnData) {

        return spawnData;
    }
}
