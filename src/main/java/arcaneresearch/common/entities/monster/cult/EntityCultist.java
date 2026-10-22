package arcaneresearch.common.entities.monster.cult;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.entities.monster.boss.EntityCultistLeader;
import arcaneresearch.init.ModEntities;

import javax.annotation.Nullable;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class EntityCultist extends Monster {

    private BlockPos homePos;
    private int homeDistance;

    public EntityCultist(EntityType<? extends EntityCultist> type, Level level) {
        super(type, level);
        this.xpReward = 10;
        ((GroundPathNavigation) getNavigation()).setCanOpenDoors(true);

        setDropChance(EquipmentSlot.HEAD, 0.05f);
        setDropChance(EquipmentSlot.CHEST, 0.05f);
        setDropChance(EquipmentSlot.LEGS, 0.05f);
        setDropChance(EquipmentSlot.FEET, 0.05f);
    }

    public EntityCultist(Level level) {
        this(ModEntities.CULTIST.get(), level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MAX_HEALTH, 20.0);
    }

    @Override
    protected void registerGoals() {

    }

    @Override
    public boolean canPickUpLoot() {
        return false;
    }

    protected void setLoot(DifficultyInstance difficulty) {

    }

    protected void setEnchantmentBasedOnDifficulty(DifficultyInstance difficulty) {

    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnData) {

        spawnData = super.finalizeSpawn(level, difficulty, spawnType, spawnData);
        setLoot(difficulty);
        setEnchantmentBasedOnDifficulty(difficulty);
        return spawnData;
    }

    public void setHomePos(BlockPos pos, int distance) {
        this.homePos = pos;
        this.homeDistance = distance;
        setHomeTo(pos, distance);
    }

    public BlockPos getHomePos() {
        return homePos;
    }

    public int getHomeDistance() {
        return homeDistance;
    }

    public boolean hasHome() {
        return homePos != null && homeDistance > 0;
    }

    @Override
    
    protected boolean considersEntityAsAlly(Entity entity) {

        if (entity instanceof EntityCultist) {
            return true;
        }

        if (entity instanceof EntityCultistLeader) {
            return true;
        }
        return super.considersEntityAsAlly(entity);
    }

    @Override
    public boolean canAttack(net.minecraft.world.entity.LivingEntity target) {

        if (target instanceof EntityCultist || target instanceof EntityCultistLeader) {
            return false;
        }
        return super.canAttack(target);
    }

    public void spawnExplosionParticle() {
        if (level().isClientSide()) {
            spawnSpawnFX();
        } else {
            level().broadcastEntityEvent(this, (byte) 20);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnSpawnFX() {
        for (int i = 0; i < 20; ++i) {
            double d0 = random.nextGaussian() * 0.05;
            double d2 = random.nextGaussian() * 0.05;
            double d3 = random.nextGaussian() * 0.05;
            double d4 = 2.0;
            FXDispatcher.INSTANCE.cultistSpawn(
                    getX() + random.nextFloat() * getBbWidth() * 2.0f - getBbWidth() + d0 * d4,
                    getY() + random.nextFloat() * getBbHeight() + d2 * d4,
                    getZ() + random.nextFloat() * getBbWidth() * 2.0f - getBbWidth() + d3 * d4,
                    d0, d2, d3);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 20) {
            spawnExplosionParticle();
        } else {
            super.handleEntityEvent(id);
        }
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
    }
}
