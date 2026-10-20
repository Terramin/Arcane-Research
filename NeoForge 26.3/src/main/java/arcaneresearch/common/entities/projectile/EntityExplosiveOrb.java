package arcaneresearch.common.entities.projectile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class EntityExplosiveOrb extends ThrowableItemProjectile {
    public float strength = 1.0F;
    public boolean onFire = false;

    public EntityExplosiveOrb(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public EntityExplosiveOrb(EntityType<? extends EntityExplosiveOrb> type, Level level, LivingEntity owner) {
        super(type, level);
        setOwner(owner);
        // NEOFORGE_PORT (26.3): Forge super(type, owner, level) also placed the projectile at the owner's eyes.
        setPos(owner.getX(), owner.getEyeY() - 0.1F, owner.getZ());
        setItem(new ItemStack(getDefaultItem()));
    }

    @Override
    protected Item getDefaultItem() {
        return Items.FIRE_CHARGE;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {

            for (int a = 0; a < 2; a++) {
                arcaneresearch.client.fx.WandFX.flameMote(this.level(),
                        this.xOld + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.3F,
                        this.yOld + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.3F,
                        this.zOld + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.3F,
                        0.0D, 0.0D, 0.0D, 2.0F);
            }
        }
        if (this.tickCount > 500) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (this.level().isClientSide()) {
            for (int a = 0; a < 8; a++) {
                arcaneresearch.client.fx.WandFX.flameMote(this.level(),
                        this.getX() + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.5F,
                        this.getY() + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.5F,
                        this.getZ() + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.5F,
                        (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.15D,
                        (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.15D,
                        (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.15D, 2.0F);
            }
        }
        if (!this.level().isClientSide()) {
            if (hit instanceof EntityHitResult entityHit) {
                Entity owner = this.getOwner();
                entityHit.getEntity().hurt(this.damageSources().thrown(this,
                        owner != null ? owner : this), this.strength * 1.5F);
            }
            this.level().explode((net.minecraft.world.entity.Entity) null, this.getX(), this.getY(), this.getZ(), this.strength, this.onFire,
                    Level.ExplosionInteraction.MOB);
        }
        this.discard();
    }
}
