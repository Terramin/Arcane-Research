package arcaneresearch.common.entities.projectile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
public class EntityEmber extends ThrowableItemProjectile {
    public int duration = 20;
    public int firey = 0;
    public float damage = 1.0F;
    public EntityEmber(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }
    public EntityEmber(EntityType<? extends EntityEmber> type, Level level, LivingEntity owner, float scatter) {
        super(type, owner, level);
        this.shootFromRotation(owner, owner.getXRot(), owner.getYRot(), 0.0F, this.getGravityVelocity(), scatter);
    }
    protected float getGravityVelocity() {
        return 1.0F;
    }
    @Override
    protected Item getDefaultItem() {
        return Items.FIRE_CHARGE;
    }
    @Override
    public void tick() {
        if (this.level().isClientSide) {
            arcaneresearch.client.fx.WandFX.flameMote(this.level(),
                    this.getX() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F,
                    this.getY() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F,
                    this.getZ() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F,
                    -this.getDeltaMovement().x * 0.1D,
                    -this.getDeltaMovement().y * 0.1D,
                    -this.getDeltaMovement().z * 0.1D, 1.2F);
        }
        if (this.tickCount > this.duration) {
            this.discard();
            return;
        }
        double damp = this.duration <= 20 ? 0.95D : 0.975D;
        this.setDeltaMovement(this.getDeltaMovement().scale(damp));
        if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.66D));
        }
        super.tick();
    }
    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (this.level().isClientSide) {
            for (int a = 0; a < 4; a++) {
                arcaneresearch.client.fx.WandFX.flameMote(this.level(),
                        this.getX() + (this.random.nextFloat() - this.random.nextFloat()) * 0.3F,
                        this.getY() + (this.random.nextFloat() - this.random.nextFloat()) * 0.3F,
                        this.getZ() + (this.random.nextFloat() - this.random.nextFloat()) * 0.3F,
                        (this.random.nextFloat() - this.random.nextFloat()) * 0.1D,
                        (this.random.nextFloat() - this.random.nextFloat()) * 0.1D,
                        (this.random.nextFloat() - this.random.nextFloat()) * 0.1D, 1.5F);
            }
        }
        if (!this.level().isClientSide) {
            if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living) {
                if (!living.isDeadOrDying()
                        && living.hurt(this.damageSources().indirectMagic(this, this.getOwner() instanceof LivingEntity o ? o : this), this.damage)) {
                    living.setSecondsOnFire(3 + this.firey);
                }
            } else if (hit instanceof BlockHitResult blockHit) {
                if (this.random.nextFloat() < 0.025F * this.firey) {
                    boolean grief = true;
                    if (this.getOwner() instanceof LivingEntity) {
                        grief = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
                    }
                    if (grief) {
                        BlockPos at = blockHit.getBlockPos().relative(blockHit.getDirection());
                        if (this.level().isEmptyBlock(at)) {
                            this.level().setBlockAndUpdate(at, Blocks.FIRE.defaultBlockState());
                        }
                    }
                }
            }
        }
        this.discard();
    }
    @Override
    public boolean canBeCollidedWith() {
        return false;
    }
    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("damage", this.damage);
        tag.putInt("firey", this.firey);
        tag.putInt("duration", this.duration);
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.damage = tag.getFloat("damage");
        this.firey = tag.getInt("firey");
        this.duration = tag.getInt("duration");
    }
}
