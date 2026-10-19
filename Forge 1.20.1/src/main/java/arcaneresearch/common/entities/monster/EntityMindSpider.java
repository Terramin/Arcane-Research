package arcaneresearch.common.entities.monster;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import arcaneresearch.init.ModEntities;
import javax.annotation.Nullable;
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
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_HARMLESS, false);
        this.entityData.define(DATA_VIEWER, "");
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
    @Override
    protected float getStandingEyeHeight(net.minecraft.world.entity.Pose pose, net.minecraft.world.entity.EntityDimensions dimensions) {
        return 0.45f;
    }
    @Override
    protected float getSoundVolume() {
        return super.getSoundVolume() * 0.7f;
    }
    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount > lifeSpan) {
            discard();
        }
    }
    @Override
    public int getExperienceReward() {
        return isHarmless() ? 0 : super.getExperienceReward();
    }
    @Override
    public boolean doHurtTarget(Entity target) {
        if (isHarmless()) {
            return false;
        }
        return super.doHurtTarget(target);
    }
    @Override
    public boolean isNoGravity() {
        return false;
    }
    @Override
    protected boolean shouldDropLoot() {
        return false;
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Harmless", isHarmless());
        tag.putString("Viewer", getViewer());
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setHarmless(tag.getBoolean("Harmless"));
        setViewer(tag.getString("Viewer"));
    }
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        return spawnData;
    }
}
