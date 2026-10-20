package arcaneresearch.common.entities.projectile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import arcaneresearch.api.blocks.BlocksTC;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.lib.utils.BlockUtils;
import arcaneresearch.common.lib.utils.EntityUtils;
import java.util.List;
public class EntityPrimalOrb extends ThrowableItemProjectile {
    int count;
    boolean seeker;
    int ownerId;
    public EntityPrimalOrb(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }
    public EntityPrimalOrb(EntityType<? extends EntityPrimalOrb> type, Level level, LivingEntity owner, boolean seeker) {
        super(type, owner, level);
        var v = owner.getLookAngle();
        this.setPos(owner.getX() + v.x / 2.0D, owner.getEyeY() + v.y / 2.0D, owner.getZ() + v.z / 2.0D);
        this.seeker = seeker;
        this.ownerId = owner.getId();
    }
    @Override
    protected Item getDefaultItem() {
        return Items.SNOWBALL;
    }
    @Override
    protected float getGravity() {
        return 0.001F;
    }
    @OnlyIn(Dist.CLIENT)
    private void trailMote(double x, double y, double z, float r, float g, float b, float scale) {
        try {
            FXDispatcher.INSTANCE.drawGenericParticles(x, y, z, 0.0D, 0.0D, 0.0D,
                    r, g, b, 0.8F, false, 320, 16, 1, 12, 0, scale, 0.0F, 0);
        } catch (Exception ignored) {
        }
    }
    @Override
    public void tick() {
        this.count++;
        if (this.isInWater()) {
            this.onHit(new net.minecraft.world.phys.BlockHitResult(this.position(), this.getDirection(),
                    this.blockPosition(), false));
            return;
        }
        if (this.level().isClientSide) {
            for (int a = 0; a < 6; a++) {
                trailMote(
                        this.getX() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F,
                        this.getY() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F,
                        this.getZ() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F,
                        0.8F, 0.6F, 1.0F, 0.5F);
            }
            trailMote(
                    this.getX() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F,
                    this.getY() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F,
                    this.getZ() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F,
                    0.5F, 0.2F, 0.7F, 0.6F);
        }
        if (this.tickCount > 20) {
            if (!this.seeker) {
                java.util.Random rr = new java.util.Random(this.getId() + this.count);
                this.setDeltaMovement(this.getDeltaMovement().add(
                        (rr.nextFloat() - rr.nextFloat()) * 0.01F,
                        (rr.nextFloat() - rr.nextFloat()) * 0.01F,
                        (rr.nextFloat() - rr.nextFloat()) * 0.01F));
            } else if (!this.level().isClientSide) {
                List<LivingEntity> targets = EntityUtils.getEntitiesInRange(this.level(), this.blockPosition(),
                        16.0D, LivingEntity.class, e -> e.getId() != this.ownerId && e.isAlive());
                double best = Double.MAX_VALUE;
                Entity locked = null;
                for (Entity e : targets) {
                    double dd = this.distanceToSqr(e);
                    if (dd < best) {
                        best = dd;
                        locked = e;
                    }
                }
                if (locked != null && best > 1.0E-4D) {
                    double dx = (locked.getX() - this.getX()) / best;
                    double dy = (locked.getBoundingBox().minY + locked.getBbHeight() * 0.9D - this.getY()) / best;
                    double dz = (locked.getZ() - this.getZ()) / best;
                    double accel = 0.2D;
                    this.setDeltaMovement(
                            Mth.clamp((float) (this.getDeltaMovement().x + dx * accel), -0.2F, 0.2F),
                            Mth.clamp((float) (this.getDeltaMovement().y + dy * accel), -0.2F, 0.2F),
                            Mth.clamp((float) (this.getDeltaMovement().z + dz * accel), -0.2F, 0.2F));
                }
            }
        }
        super.tick();
        if (this.tickCount > 5000) {
            this.discard();
        }
    }
    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (this.level().isClientSide) {
            for (int a = 0; a < 6; a++) {
                for (int b = 0; b < 6; b++) {
                    float fx = (this.random.nextFloat() - this.random.nextFloat()) * 0.5F;
                    float fy = (this.random.nextFloat() - this.random.nextFloat()) * 0.5F;
                    float fz = (this.random.nextFloat() - this.random.nextFloat()) * 0.5F;
                    trailMote(this.getX() + fx, this.getY() + fy, this.getZ() + fz,
                            0.7F, 0.4F, 1.0F, 0.8F);
                }
            }
            return;
        }
        boolean inWater = this.isInWater();
        this.level().explode(null, this.getX(), this.getY(), this.getZ(), inWater ? 4.0F : 2.0F, true,
                Level.ExplosionInteraction.MOB);
        if (!this.seeker && this.random.nextInt(100) <= (inWater ? 10.0F : 1.0F)) {
            taintSplosion();
        }
        this.discard();
    }
    public void taintSplosion() {
        net.minecraft.world.level.block.Block fibre;
        try {
            fibre = BlocksTC.taintFibre.get();
        } catch (Exception e) {
            return;
        }
        int x = (int) this.getX();
        int y = (int) this.getY();
        int z = (int) this.getZ();
        for (int a = 0; a < 15; a++) {
            int xx = x + (int) (this.random.nextFloat() - this.random.nextFloat() * 6.0F);
            int zz = z + (int) (this.random.nextFloat() - this.random.nextFloat() * 6.0F);
            int yy = y + (int) (this.random.nextFloat() - this.random.nextFloat() * 6.0F);
            BlockPos bp = new BlockPos(xx, yy, zz);
            if (this.level().isEmptyBlock(bp) && BlockUtils.isAdjacentToSolidBlock(this.level(), bp)) {
                this.level().setBlockAndUpdate(bp, fibre.defaultBlockState());
            }
        }
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("seeker", this.seeker);
        tag.putInt("ownerId", this.ownerId);
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.seeker = tag.getBoolean("seeker");
        this.ownerId = tag.getInt("ownerId");
    }
}
