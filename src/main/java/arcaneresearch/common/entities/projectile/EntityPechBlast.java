package arcaneresearch.common.entities.projectile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import arcaneresearch.common.entities.monster.EntityPech;
import java.util.List;
public class EntityPechBlast extends ThrowableItemProjectile {
    int strength;
    int duration;
    boolean nightshade;
    public EntityPechBlast(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }
    public EntityPechBlast(EntityType<? extends EntityPechBlast> type, Level level, LivingEntity owner, int strength, int duration, boolean nightshade) {
        super(type, owner, level);
        this.strength = strength;
        this.duration = duration;
        this.nightshade = nightshade;
    }
    public EntityPechBlast(EntityType<? extends EntityPechBlast> type, Level level, double x, double y, double z, int strength, int duration, boolean nightshade) {
        super(type, x, y, z, level);
        this.strength = strength;
        this.duration = duration;
        this.nightshade = nightshade;
    }
    @Override
    protected Item getDefaultItem() {
        try {
            return arcaneresearch.common.items.wands.WandItems.WAND_PECH_BLAST.get();
        } catch (Exception e) {
            return Items.SNOWBALL;
        }
    }
    @Override
    public void tick() {
        super.tick();
        if (this.tickCount > 500) {
            this.discard();
        }
    }
    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!this.level().isClientSide) {
            List<Entity> list = this.level().getEntities(this.getOwner(), new AABB(this.getX() - 2.0D, this.getY() - 2.0D, this.getZ() - 2.0D,
                    this.getX() + 2.0D, this.getY() + 2.0D, this.getZ() + 2.0D));
            for (Entity e : list) {
                if (e instanceof EntityPech) {
                    continue;
                }
                if (e instanceof LivingEntity living) {
                    living.hurt(this.damageSources().thrown(this, this.getOwner()), this.strength + 2);
                    try {
                        if (this.nightshade) {
                            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 100 + this.duration * 40, this.strength));
                            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100 + this.duration * 40, this.strength + 1));
                            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100 + this.duration * 40, this.strength));
                        } else {
                            switch (this.random.nextInt(3)) {
                                case 0 ->
                                        living.addEffect(new MobEffectInstance(MobEffects.WITHER, 100 + this.duration * 40, this.strength));
                                case 1 ->
                                        living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100 + this.duration * 40, this.strength + 1));
                                default ->
                                        living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100 + this.duration * 40, this.strength));
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
            this.discard();
        }
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("strength", this.strength);
        tag.putInt("duration", this.duration);
        tag.putBoolean("nightshade", this.nightshade);
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.strength = tag.getInt("strength");
        this.duration = tag.getInt("duration");
        this.nightshade = tag.getBoolean("nightshade");
    }
}
