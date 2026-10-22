package arcaneresearch.common.entities.projectile;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import arcaneresearch.common.items.tools.ItemPrimalArrow;
import arcaneresearch.init.ModEntities;

 



 
public class EntityPrimalArrow extends AbstractArrow {

    private static final EntityDataAccessor<Integer> PRIMAL_TYPE =
            SynchedEntityData.defineId(EntityPrimalArrow.class, EntityDataSerializers.INT);

     
 
    public int punchLevel = 0;

    public EntityPrimalArrow(EntityType<? extends EntityPrimalArrow> type, Level level) {
        super(type, level);
    }

    public EntityPrimalArrow(Level level, LivingEntity owner, float chargeFactor, int type) {
        
        
        super(ModEntities.PRIMAL_ARROW.get(), owner, level, pickupStackFor(type), null);
        this.setPrimalType(type);
        
        
        this.setBaseDamage(2.1D * damageMult(type));
        
        this.shootFromRotation(owner, owner.getXRot(), owner.getYRot(), 0.0F, chargeFactor * 1.5F, 1.0F);
    }

    private static ItemStack pickupStackFor(int type) {
        return switch (type) {
            case ItemPrimalArrow.FIRE -> new ItemStack(arcaneresearch.init.ModItems.PRIMAL_ARROW_FIRE.get());
            case ItemPrimalArrow.WATER -> new ItemStack(arcaneresearch.init.ModItems.PRIMAL_ARROW_WATER.get());
            case ItemPrimalArrow.EARTH -> new ItemStack(arcaneresearch.init.ModItems.PRIMAL_ARROW_EARTH.get());
            case ItemPrimalArrow.ORDER -> new ItemStack(arcaneresearch.init.ModItems.PRIMAL_ARROW_ORDER.get());
            case ItemPrimalArrow.ENTROPY -> new ItemStack(arcaneresearch.init.ModItems.PRIMAL_ARROW_ENTROPY.get());
            default -> new ItemStack(arcaneresearch.init.ModItems.PRIMAL_ARROW_AIR.get());
        };
    }

    public static double damageMult(int type) {
        return switch (type) {
            case ItemPrimalArrow.EARTH -> 1.5D;
            case ItemPrimalArrow.ORDER, ItemPrimalArrow.ENTROPY -> 0.8D;
            default -> 1.0D;
        };
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PRIMAL_TYPE, 0);
    }

    public int getPrimalType() {
        try {
            return this.entityData.get(PRIMAL_TYPE);
        } catch (Exception e) {
            return 0;
        }
    }

    public void setPrimalType(int type) {
        this.entityData.set(PRIMAL_TYPE, Math.max(0, Math.min(5, type)));
    }

      
    public int getTicksInGround() {
        
        return this.inGroundTime;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("PrimalType", this.getPrimalType());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        try {
            this.setPrimalType(tag.getIntOr("PrimalType", 0));
        } catch (Exception ignored) {
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return pickupStackFor(this.getPrimalType());
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return pickupStackFor(this.getPrimalType());
    }

    @Override
    public void tick() {
        super.tick();
        
        if (!this.level().isClientSide() && this.isInGround() && this.inGroundTime >= 100) {
            this.discard();
        }
    }

     



 
    @Override
    protected void onHitEntity(EntityHitResult result) {
        int type = this.getPrimalType();
        if (result.getEntity() instanceof LivingEntity target) {
            int fire = 0;
            if (type == ItemPrimalArrow.FIRE) {
                fire += 5;
            } else if (this.isOnFire() && type != ItemPrimalArrow.WATER) {
                fire += 5;
            }
            if (type == ItemPrimalArrow.WATER) {
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 4));
            } else if (type == ItemPrimalArrow.ORDER) {
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 4));
            } else if (type == ItemPrimalArrow.ENTROPY) {
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0));
            }
            
            if (fire > 0 && !(target instanceof Enderman)) {
                target.igniteForSeconds((float) fire);
            }
        }
        super.onHitEntity(result);
    }

    
    
    
    @Override
    protected void doKnockback(LivingEntity mob, net.minecraft.world.damagesource.DamageSource damageSource) {
        super.doKnockback(mob, damageSource);
        if (punchLevel > 0) {
            double resist = Math.max(0.0D, 1.0D - mob.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE));
            net.minecraft.world.phys.Vec3 push = this.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize()
                    .scale((double) punchLevel * 0.6D * resist);
            if (push.lengthSqr() > 0.0D) {
                mob.push(push.x, 0.1D, push.z);
            }
        }
    }
}
