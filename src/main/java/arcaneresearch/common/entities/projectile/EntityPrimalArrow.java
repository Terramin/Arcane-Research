package arcaneresearch.common.entities.projectile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import arcaneresearch.common.items.tools.ItemPrimalArrow;
import arcaneresearch.init.ModEntities;
import arcaneresearch.init.ModItems;
public class EntityPrimalArrow extends AbstractArrow {
    private static final EntityDataAccessor<Integer> PRIMAL_TYPE =
            SynchedEntityData.defineId(EntityPrimalArrow.class, EntityDataSerializers.INT);
    public EntityPrimalArrow(EntityType<? extends EntityPrimalArrow> type, Level level) {
        super(type, level);
    }
    public EntityPrimalArrow(Level level, LivingEntity owner, float chargeFactor, int type) {
        super(ModEntities.PRIMAL_ARROW.get(), owner, level);
        this.setPrimalType(type);
        this.setBaseDamage(2.1D * damageMult(type));
        this.shootFromRotation(owner, owner.getXRot(), owner.getYRot(), 0.0F, chargeFactor * 1.5F, 1.0F);
    }
    public static double damageMult(int type) {
        return switch (type) {
            case ItemPrimalArrow.EARTH -> 1.5D;
            case ItemPrimalArrow.ORDER, ItemPrimalArrow.ENTROPY -> 0.8D;
            default -> 1.0D;
        };
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PRIMAL_TYPE, 0);
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
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("PrimalType", this.getPrimalType());
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        try {
            this.setPrimalType(tag.getInt("PrimalType"));
        } catch (Exception ignored) {
        }
    }
    @Override
    protected ItemStack getPickupItem() {
        return switch (this.getPrimalType()) {
            case ItemPrimalArrow.FIRE -> new ItemStack(ModItems.PRIMAL_ARROW_FIRE.get());
            case ItemPrimalArrow.WATER -> new ItemStack(ModItems.PRIMAL_ARROW_WATER.get());
            case ItemPrimalArrow.EARTH -> new ItemStack(ModItems.PRIMAL_ARROW_EARTH.get());
            case ItemPrimalArrow.ORDER -> new ItemStack(ModItems.PRIMAL_ARROW_ORDER.get());
            case ItemPrimalArrow.ENTROPY -> new ItemStack(ModItems.PRIMAL_ARROW_ENTROPY.get());
            default -> new ItemStack(ModItems.PRIMAL_ARROW_AIR.get());
        };
    }
    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.inGround && this.inGroundTime >= 100) {
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
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 4));
            } else if (type == ItemPrimalArrow.ORDER) {
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 4));
            } else if (type == ItemPrimalArrow.ENTROPY) {
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0));
            }
            if (fire > 0 && !(target instanceof EnderMan)) {
                target.setSecondsOnFire(fire);
            }
        }
        super.onHitEntity(result);
    }
}
