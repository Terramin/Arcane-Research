package arcaneresearch.common.entities.monster;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
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
                                        EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnData) {

        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, spawnData);

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

    // NEOFORGE_PORT: 26.x shouldDropLoot takes the server level.
    @Override
    protected boolean shouldDropLoot(net.minecraft.server.level.ServerLevel level) {
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
        // NEOFORGE_PORT: 1:1 from Forge. 26.x swaps: Entity.moveTo(x,y,z,yaw,pitch) gone ->
        // setPos + setRot; getCurrentDifficultyAt lives on ServerLevelAccessor;
        // MobSpawnType -> EntitySpawnReason; RULE_DOMOBLOOT -> MOB_DROPS (ServerLevel rules);
        // lastHurtByPlayerTime -> getLastHurtByPlayerMemoryTime();
        // getExperienceReward(ServerLevel, attacker) (mirrors vanilla dropExperience gating).
        if (!level().isClientSide()) {
            EntityEldritchCrab crab = new EntityEldritchCrab(level());
            // NEOFORGE_PORT (26.3): moveTo(x,y,z,yaw,pitch) -> snapTo (same body incl. old pos/rot).
            crab.snapTo(getX(), getY() + getEyeHeight(), getZ(), getYRot(), getXRot());
            crab.setHelm(true);
            crab.finalizeSpawn((net.minecraft.world.level.ServerLevelAccessor) level(),
                    ((net.minecraft.world.level.ServerLevelAccessor) level()).getCurrentDifficultyAt(blockPosition()),
                    EntitySpawnReason.CONVERSION, null);
            level().addFreshEntity(crab);

            if (level() instanceof net.minecraft.server.level.ServerLevel serverLevel
                    && (getLastHurtByPlayerMemoryTime() > 0 || isAlwaysExperienceDropper())
                    && serverLevel.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.MOB_DROPS)) {
                int xp = getExperienceReward(serverLevel, getLastHurtByPlayer());
                net.minecraft.world.entity.ExperienceOrb.award(serverLevel, position(), xp);
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
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level, EntitySpawnReason spawnType) {

        List<EntityInhabitedZombie> nearby = level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(EntityInhabitedZombie.class), new AABB(getX() - 32, getY() - 16, getZ() - 32,
                        getX() + 32, getY() + 16, getZ() + 32), e -> true);
        return nearby.isEmpty() && super.checkSpawnRules(level, spawnType);
    }
}
