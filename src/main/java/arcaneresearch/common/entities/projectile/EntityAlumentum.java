package arcaneresearch.common.entities.projectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.init.ModEntities;
public class EntityAlumentum extends ThrowableProjectile {
    public EntityAlumentum(EntityType<? extends EntityAlumentum> type, Level level) {
        super(type, level);
    }
    public EntityAlumentum(Level level, LivingEntity owner) {
        super(ModEntities.ALUMENTUM.get(), owner, level);
    }
    public EntityAlumentum(Level level, double x, double y, double z) {
        super(ModEntities.ALUMENTUM.get(), x, y, z, level);
    }
    @Override
    protected void defineSynchedData() {
    }
    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        super.shoot(x, y, z, arcaneresearch.common.items.consumables.AlumentumRules.VELOCITY, inaccuracy);
    }
    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            spawnAlumentumFX();
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void spawnAlumentumFX() {
        for (int i = 0; i < arcaneresearch.common.items.consumables.AlumentumRules.FX_SAMPLES; ++i) {
            double coeff = arcaneresearch.common.items.consumables.AlumentumRules.trailCoefficient(i);
            FXDispatcher.INSTANCE.drawAlumentum(
                    (float) (xOld + (getX() - xOld) * coeff),
                    (float) (yOld + (getY() - yOld) * coeff) + getBbHeight() / 2.0f,
                    (float) (zOld + (getZ() - zOld) * coeff),
                    arcaneresearch.common.items.consumables.AlumentumRules.trailJitter(random.nextFloat()),
                    arcaneresearch.common.items.consumables.AlumentumRules.trailJitter(random.nextFloat()),
                    arcaneresearch.common.items.consumables.AlumentumRules.trailJitter(random.nextFloat()),
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
        if (!level().isClientSide) {
            // The original passes isFlaming=true; the 6-argument overload always
            // passes false, so use the explicit overload to keep the fire effect.
            level().explode(this, getX(), getY(), getZ(),
                    arcaneresearch.common.items.consumables.AlumentumRules.EXPLOSION_RADIUS,
                    arcaneresearch.common.items.consumables.AlumentumRules.EXPLOSION_FLAMING,
                    Level.ExplosionInteraction.BLOCK);
            discard();
        }
    }
    @Override
    protected float getGravity() {
        return arcaneresearch.common.items.consumables.AlumentumRules.GRAVITY;
    }
}
