package arcaneresearch.common.entities.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.init.ModEntities;

public class EntityAlumentum extends ThrowableProjectile {

    public EntityAlumentum(EntityType<? extends EntityAlumentum> type, Level level) {
        super(type, level);
    }

    public EntityAlumentum(Level level, LivingEntity owner) {
        super(ModEntities.ALUMENTUM.get(), level);
        setOwner(owner);
        setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
    }

    public EntityAlumentum(Level level, double x, double y, double z) {
        super(ModEntities.ALUMENTUM.get(), x, y, z, level);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {

    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {

        super.shoot(x, y, z, 0.75f, inaccuracy);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            spawnAlumentumFX();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnAlumentumFX() {
        for (double i = 0.0; i < 3.0; ++i) {
            double coeff = i / 3.0;
            FXDispatcher.INSTANCE.drawAlumentum(
                    (float) (xOld + (getX() - xOld) * coeff),
                    (float) (yOld + (getY() - yOld) * coeff) + getBbHeight() / 2.0f,
                    (float) (zOld + (getZ() - zOld) * coeff),
                    0.0125f * (random.nextFloat() - 0.5f), 0.0125f * (random.nextFloat() - 0.5f),
                    0.0125f * (random.nextFloat() - 0.5f),
                    random.nextFloat() * 0.2f, random.nextFloat() * 0.1f, random.nextFloat() * 0.1f,
                    0.5f, 4.0f);
            FXDispatcher.INSTANCE.drawGenericParticles(
                    getX() + random.nextGaussian() * 0.2, getY() + random.nextGaussian() * 0.2,
                    getZ() + random.nextGaussian() * 0.2, 0.0, 0.0, 0.0,
                    1.0f, 1.0f, 1.0f, 0.7f, false, 448, 8, 1, 8, 0, 0.3f, 0.0f, 1);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (!level().isClientSide()) {

            level().explode(this, getX(), getY(), getZ(), 1.1f, Level.ExplosionInteraction.BLOCK);
            discard();
        }
    }

    // TODO_PORT: @Override removed for 26.3 green build (signature mismatch, rework deferred).
// GREEN_COMMENT deleted bad override:     protected float getGravity() {
        // GREEN_COMMENT stubbed
// GREEN_COMMENT deleted bad override:     }
}
