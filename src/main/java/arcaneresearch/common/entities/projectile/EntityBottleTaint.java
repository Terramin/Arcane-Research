package arcaneresearch.common.entities.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import arcaneresearch.api.entities.ITaintedMob;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModEffects;
import arcaneresearch.init.ModEntities;

import java.util.List;

public class EntityBottleTaint extends ThrowableProjectile {

    public EntityBottleTaint(EntityType<? extends EntityBottleTaint> type, Level level) {
        super(type, level);
    }

    public EntityBottleTaint(Level level, LivingEntity owner) {
        super(ModEntities.BOTTLE_TAINT.get(), level);
        setOwner(owner);
        setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
    }

    public EntityBottleTaint(Level level, double x, double y, double z) {
        super(ModEntities.BOTTLE_TAINT.get(), x, y, z, level);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {

    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            spawnBreakFX();
        }
        super.handleEntityEvent(id);
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnBreakFX() {
        for (int a = 0; a < 100; ++a) {
            FXDispatcher.INSTANCE.taintsplosionFX(this);
        }
        FXDispatcher.INSTANCE.bottleTaintBreak(getX(), getY(), getZ());
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (!level().isClientSide()) {

            AABB searchBox = new AABB(getX(), getY(), getZ(), getX(), getY(), getZ()).inflate(5.0, 5.0, 5.0);
            // NEOFORGE_PORT (26.3): 2-arg getEntitiesOfClass keeps the Forge NO_SPECTATORS filter.
            List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, searchBox);

            for (LivingEntity entity : entities) {

                if (!(entity instanceof ITaintedMob) && !entity.isInvertedHealAndHarm()) {

                    entity.addEffect(new MobEffectInstance(ModEffects.FLUX_TAINT, 100, 0, false, true));
                }
            }

            for (int a = 0; a < 10; a++) {
                int xx = (int) ((random.nextFloat() - random.nextFloat()) * 4.0f);
                int zz = (int) ((random.nextFloat() - random.nextFloat()) * 4.0f);
                BlockPos pos = blockPosition().offset(xx, 0, zz);

                if (random.nextBoolean()) {

                    if (canPlaceFluxGoo(pos)) {
                        placeFluxGoo(pos);
                    } else {
                        BlockPos belowPos = pos.below();
                        if (canPlaceFluxGoo(belowPos)) {
                            placeFluxGoo(belowPos);
                        }
                    }
                }
            }

            level().broadcastEntityEvent(this, (byte) 3);
            discard();
        }
    }

    private boolean canPlaceFluxGoo(BlockPos pos) {
        BlockState stateBelow = level().getBlockState(pos.below());
        BlockState stateAt = level().getBlockState(pos);

        return stateBelow.isSolidRender() &&
                (stateAt.isAir() || stateAt.canBeReplaced());
    }

    private void placeFluxGoo(BlockPos pos) {
        level().setBlockAndUpdate(pos, ModBlocks.FLUX_GOO.get().defaultBlockState());
    }

    // NEOFORGE_PORT: Forge getGravity() (float) -> 26.x getDefaultGravity() (double), same value.
    @Override
    protected double getDefaultGravity() {
        return 0.03;
    }
}
