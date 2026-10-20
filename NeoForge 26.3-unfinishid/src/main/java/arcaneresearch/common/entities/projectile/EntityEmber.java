package arcaneresearch.common.entities.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;

public class EntityEmber extends ThrowableItemProjectile {
    public int duration = 20;
    public int firey = 0;
    public float damage = 1.0F;

    public EntityEmber(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public EntityEmber(EntityType<? extends EntityEmber> type, Level level, LivingEntity owner, float scatter) {
        super(type, level);
        setOwner(owner);
        // NEOFORGE_PORT (26.3): Forge super(type, owner, level) also placed the ember at the owner's eyes.
        setPos(owner.getX(), owner.getEyeY() - 0.1F, owner.getZ());
        setItem(new ItemStack(getDefaultItem()));
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
        if (this.level().isClientSide()) {

            arcaneresearch.client.fx.WandFX.flameMote(this.level(),
                    this.getX() + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.2F,
                    this.getY() + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.2F,
                    this.getZ() + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.2F,
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
        if (this.level().isClientSide()) {
            for (int a = 0; a < 4; a++) {
                arcaneresearch.client.fx.WandFX.flameMote(this.level(),
                        this.getX() + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.3F,
                        this.getY() + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.3F,
                        this.getZ() + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.3F,
                        (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.1D,
                        (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.1D,
                        (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.1D, 1.5F);
            }
        }
        if (!this.level().isClientSide()) {
            if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living) {
                // NEOFORGE_PORT (26.3): Entity.hurt is void -> hurtServer keeps the boolean, so fire is
                // only applied when the hit landed; setSecondsOnFire -> igniteForSeconds (no shortening).
                if (!living.isDeadOrDying() && this.level() instanceof net.minecraft.server.level.ServerLevel sl
                        && living.hurtServer(sl, this.damageSources().indirectMagic(this, this.getOwner() instanceof LivingEntity o ? o : this), this.damage)) {
                    living.igniteForSeconds(3 + this.firey);
                }
            } else if (hit instanceof BlockHitResult blockHit) {
                if (this.getRandom().nextFloat() < 0.025F * this.firey) {
                    boolean grief = true;
                    if (this.getOwner() instanceof LivingEntity) {
                        grief = !(this.level() instanceof net.minecraft.server.level.ServerLevel sl) || sl.getGameRules().get(GameRules.MOB_GRIEFING);
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

    // NEOFORGE_PORT: 26.x canBeCollidedWith takes the colliding entity.
    @Override
    public boolean canBeCollidedWith(net.minecraft.world.entity.Entity other) {
        return false;
    }

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, float amount) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("damage", this.damage);
        tag.putInt("firey", this.firey);
        tag.putInt("duration", this.duration);
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        this.damage = tag.getFloatOr("damage", 0.0F);
        this.firey = tag.getIntOr("firey", 0);
        this.duration = tag.getIntOr("duration", 0);
    }
}
