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

/**
 * Первородная стрела, порт TC4 {@code EntityPrimalArrow}: 6 стихийных видов
 * (0 air, 1 fire, 2 water, 3 earth, 4 order, 5 entropy), подбор запрещён,
 * в земле гаснет за 100 тиков. Тип едет entity data (и в NBT).
 */
public class EntityPrimalArrow extends AbstractArrow {

    private static final EntityDataAccessor<Integer> PRIMAL_TYPE =
            SynchedEntityData.defineId(EntityPrimalArrow.class, EntityDataSerializers.INT);

    // NEOFORGE_PORT: AbstractArrow.inGroundTime/inGround visibility changed in 26.x;
    // own ground-age counter keeps the TC4 100-tick fade 1:1 without touching internals.
    private int groundTicks = 0;

    /** NEOFORGE_PORT: AbstractArrow.setKnockback удалён в 26.x; сила отброса хранится
     * здесь и применяется в onHitEntity (тот же +1 за землю, панч лука — из событий). */
    public int punchLevel = 0;

    public EntityPrimalArrow(EntityType<? extends EntityPrimalArrow> type, Level level) {
        super(type, level);
    }

    public EntityPrimalArrow(Level level, LivingEntity owner, float chargeFactor, int type) {
        // NEOFORGE_PORT: AbstractArrow(type, owner, level, pickupStack, firedFrom) — pickup
        // stack per type (pickup stays DISALLOWED below, same as Forge getPickupItem use).
        super(ModEntities.PRIMAL_ARROW.get(), owner, level, pickupStackFor(type), null);
        this.setPrimalType(type);
        // Паритет TC4: база 2.1, земля x1.5, порядок/энтропия x0.8.
        // Множитель запекается сразу (иначе бонусы лука учлись бы дважды).
        this.setBaseDamage(2.1D * damageMult(type));
        // Паритет TC4: скорость f * dam * 1.5 (dam уже в chargeFactor).
        this.shootFromRotation(owner, owner.getXRot(), owner.getYRot(), 0.0F, chargeFactor * 1.5F, 1.0F);
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
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

    /** Тиков в земле (для затухания рендера). */
    public int getTicksInGround() {
        return this.groundTicks;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("PrimalType", this.getPrimalType());
        tag.putInt("GroundTicks", this.groundTicks);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        try {
            this.setPrimalType(tag.getIntOr("PrimalType", 0));
        } catch (Exception ignored) {
        }
        this.groundTicks = tag.getIntOr("GroundTicks", 0);
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
        // Паритет TC4: в земле гаснет за 100 тиков (не подбирается).
        if (!this.level().isClientSide()) {
            if (this.isInGround()) {
                this.groundTicks++;
                if (this.groundTicks >= 100) {
                    this.discard();
                }
            } else {
                this.groundTicks = 0;
            }
        }
    }

    /**
     * Паритет TC4 inflictDamage: огонь (огонь всегда горит, вода immune),
     * вода — замедление, порядок — слабость, энтропия — иссушение.
     * Урон считает ванилла (скорость x baseDamage + крит + knockback).
     */
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
            // NEOFORGE_PORT: Entity.setSecondsOnFire удалён -> igniteForSeconds (те же секунды).
            if (fire > 0 && !(target instanceof Enderman)) {
                target.igniteForSeconds((float) fire);
            }
            // NEOFORGE_PORT: отброс панча (setKnockback удалён): толчок по направлению
            // полёта, 0.6 за уровень — та же формула ванильного AbstractArrow.doKnockback.
            if (punchLevel > 0) {
                net.minecraft.world.phys.Vec3 motion = this.getDeltaMovement();
                double horiz = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
                if (horiz > 1.0E-4D) {
                    double strength = 0.6D * (double) punchLevel;
                    target.push(motion.x / horiz * strength, 0.1D, motion.z / horiz * strength);
                }
            }
        }
        super.onHitEntity(result);
    }
}
