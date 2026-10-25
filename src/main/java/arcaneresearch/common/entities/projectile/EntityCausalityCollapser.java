package arcaneresearch.common.entities.projectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.entities.EntityFluxRift;
import arcaneresearch.init.ModEntities;
import java.util.List;
public class EntityCausalityCollapser extends ThrowableProjectile {
    public EntityCausalityCollapser(EntityType<? extends EntityCausalityCollapser> type, Level level) {
        super(type, level);
    }
    public EntityCausalityCollapser(Level level, LivingEntity owner) {
        super(ModEntities.CAUSALITY_COLLAPSER.get(), owner, level);
    }
    public EntityCausalityCollapser(Level level, double x, double y, double z) {
        super(ModEntities.CAUSALITY_COLLAPSER.get(), x, y, z, level);
    }
    @Override
    protected void defineSynchedData() {
    }
    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        super.shoot(x, y, z, 0.8f, inaccuracy);
    }
    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            spawnCollapserFX();
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void spawnCollapserFX() {
        for (double i = 0.0; i < 3.0; ++i) {
            double coeff = i / 3.0;
            FXDispatcher.INSTANCE.drawAlumentum(
                    (float) (xOld + (getX() - xOld) * coeff),
                    (float) (yOld + (getY() - yOld) * coeff) + getBbHeight() / 2.0f,
                    (float) (zOld + (getZ() - zOld) * coeff),
                    0.0125f * (random.nextFloat() - 0.5f), 0.0125f * (random.nextFloat() - 0.5f),
                    0.0125f * (random.nextFloat() - 0.5f),
                    0.8f + random.nextFloat() * 0.2f, 0.3f + random.nextFloat() * 0.1f,
                    random.nextFloat() * 0.1f, 0.5f, 4.0f);
            FXDispatcher.INSTANCE.drawGenericParticles(
                    getX() + random.nextGaussian() * 0.2, getY() + random.nextGaussian() * 0.2,
                    getZ() + random.nextGaussian() * 0.2, 0.0, 0.0, 0.0,
                    1.0f, 1.0f, 1.0f, 0.7f, false, 448, 8, 1, 8, 0, 0.3f, 0.0f, 1);
        }
    }
    @Override
    protected void onHit(HitResult result) {
        if (!level().isClientSide) {
            level().explode(this, getX(), getY(), getZ(), 2.0f, Level.ExplosionInteraction.TNT);
            List<EntityFluxRift> rifts = level().getEntitiesOfClass(EntityFluxRift.class,
                    getBoundingBox().inflate(3.0));
            for (EntityFluxRift rift : rifts) {
                rift.setCollapsing(true);
            }
            discard();
        }
    }
    @Override
    protected float getGravity() {
        return 0.03f;
    }
}
