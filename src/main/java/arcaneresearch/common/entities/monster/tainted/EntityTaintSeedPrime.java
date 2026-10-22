package arcaneresearch.common.entities.monster.tainted;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import arcaneresearch.init.ModEntities;

public class EntityTaintSeedPrime extends EntityTaintSeed {

    public EntityTaintSeedPrime(EntityType<? extends EntityTaintSeedPrime> type, Level level) {
        super(type, level);
        this.xpReward = 12;
    }

    public EntityTaintSeedPrime(Level level) {
        this(ModEntities.TAINT_SEED_PRIME.get(), level);
    }

    public static AttributeSupplier.Builder createAttributes() {

        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 150.0)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    @Override
    protected int getArea() {
        return 2;
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, boolean wasRecentlyHit) {
        
        
        int looting = arcaneresearch.common.lib.utils.EntityUtils.getLootingLevel(serverLevel, source);

        spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), arcaneresearch.api.ArcaneresearchApiHelper.makeCrystal(arcaneresearch.api.aspects.Aspect.FLUX));
        if (random.nextBoolean()) {
            spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), arcaneresearch.api.ArcaneresearchApiHelper.makeCrystal(arcaneresearch.api.aspects.Aspect.FLUX));
        }
        if (random.nextBoolean()) {
            spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), arcaneresearch.api.ArcaneresearchApiHelper.makeCrystal(arcaneresearch.api.aspects.Aspect.FLUX));
        }
    }
}
