package arcaneresearch.common.entities.monster;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import arcaneresearch.init.ModEntities;
import arcaneresearch.init.ModItems;
import arcaneresearch.init.ModSounds;
import javax.annotation.Nullable;
import java.util.List;
public class EntityInhabitedZombie extends Zombie {
    public EntityInhabitedZombie(EntityType<? extends EntityInhabitedZombie> type, Level level) {
        super(type, level);
    }
    public EntityInhabitedZombie(Level level) {
        super(ModEntities.INHABITED_ZOMBIE.get(), level);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0);
    }
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnData, @Nullable net.minecraft.nbt.CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, spawnData, tag);
        float armorChance = (level.getDifficulty() == Difficulty.HARD) ? 0.9f : 0.6f;
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.CRIMSON_PLATE_HELM.get()));
        if (random.nextFloat() <= armorChance) {
            setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.CRIMSON_PLATE_CHEST.get()));
        }
        if (random.nextFloat() <= armorChance) {
            setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.CRIMSON_PLATE_LEGS.get()));
        }
        return result;
    }
    @Override
    protected boolean shouldDropLoot() {
        return false;
    }
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CRAB_TALK.get();
    }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GENERIC_HURT;
    }
    @Override
    protected void tickDeath() {
        if (!level().isClientSide) {
            EntityEldritchCrab crab = new EntityEldritchCrab(level());
            crab.moveTo(getX(), getY() + getEyeHeight(), getZ(), getYRot(), getXRot());
            crab.setHelm(true);
            crab.finalizeSpawn((net.minecraft.world.level.ServerLevelAccessor) level(),
                    level().getCurrentDifficultyAt(blockPosition()),
                    net.minecraft.world.entity.MobSpawnType.CONVERSION, null, null);
            level().addFreshEntity(crab);
            if ((lastHurtByPlayerTime > 0 || isAlwaysExperienceDropper()) &&
                    level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT)) {
                int xp = getExperienceReward();
                net.minecraft.world.entity.ExperienceOrb.award((net.minecraft.server.level.ServerLevel) level(), position(), xp);
            }
        }
        for (int k = 0; k < 20; k++) {
            double dx = random.nextGaussian() * 0.02;
            double dy = random.nextGaussian() * 0.02;
            double dz = random.nextGaussian() * 0.02;
            level().addParticle(ParticleTypes.POOF,
                    getX() + random.nextFloat() * getBbWidth() * 2.0f - getBbWidth(),
                    getY() + random.nextFloat() * getBbHeight(),
                    getZ() + random.nextFloat() * getBbWidth() * 2.0f - getBbWidth(),
                    dx, dy, dz);
        }
        discard();
    }
    @Override
    public void die(DamageSource source) {
    }
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level, MobSpawnType spawnType) {
        List<EntityInhabitedZombie> nearby = level.getEntitiesOfClass(EntityInhabitedZombie.class,
                new AABB(getX() - 32, getY() - 16, getZ() - 32,
                        getX() + 32, getY() + 16, getZ() + 32));
        return nearby.isEmpty() && super.checkSpawnRules(level, spawnType);
    }
}
