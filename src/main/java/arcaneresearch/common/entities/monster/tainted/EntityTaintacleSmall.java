package arcaneresearch.common.entities.monster.tainted;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import arcaneresearch.init.ModEntities;

public class EntityTaintacleSmall extends EntityTaintacle {

    private int lifetime = 200;

    public EntityTaintacleSmall(EntityType<? extends EntityTaintacleSmall> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.length = 6;
    }

    public EntityTaintacleSmall(Level level) {
        super(ModEntities.TAINTACLE_SMALL.get(), level);
        this.xpReward = 0;
        this.length = 6;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EntityTaintacle.createAttributes()
                .add(Attributes.MAX_HEALTH, 5.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            if (lifetime-- <= 0) {
                hurt(damageSources().magic(), 10.0f);
            }
        }
    }

    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level, net.minecraft.world.entity.EntitySpawnReason spawnType) {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, boolean wasRecentlyHit) {
        
        
        int looting = arcaneresearch.common.lib.utils.EntityUtils.getLootingLevel(serverLevel, source);

    }

    @Override
    protected void spawnSmallTentacle(net.minecraft.world.entity.Entity target) {

    }
}
