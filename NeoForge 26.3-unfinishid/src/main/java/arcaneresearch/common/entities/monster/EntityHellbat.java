package arcaneresearch.common.entities.monster;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;

public class EntityHellbat extends EntityFireBat {
    private static final EntityDataAccessor<Byte> DATA_HELLBAT_FLAGS =
            SynchedEntityData.defineId(EntityHellbat.class, EntityDataSerializers.BYTE);

    private static final int FLAG_SUMMONED = 1;
    private static final int FLAG_DEVIL = 2;
    private static final int FLAG_EXPLOSIVE = 3;
    private static final int FLAG_VAMPIRE = 4;

    public EntityHellbat(EntityType<? extends EntityFireBat> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_HELLBAT_FLAGS, (byte) 0);
    }

    private boolean getHellFlag(int bit) {
        return (this.entityData.get(DATA_HELLBAT_FLAGS) & (1 << bit)) != 0;
    }

    private void setHellFlag(int bit, boolean value) {
        byte b = this.entityData.get(DATA_HELLBAT_FLAGS);
        this.entityData.set(DATA_HELLBAT_FLAGS, (byte) (value ? b | (1 << bit) : b & ~(1 << bit)));
    }

    public boolean getIsSummoned() {
        return getHellFlag(FLAG_SUMMONED);
    }

    public void setIsSummoned(boolean summoned) {
        setHellFlag(FLAG_SUMMONED, summoned);
        updateHellbatAttack();
    }

    public boolean getIsDevil() {
        return getHellFlag(FLAG_DEVIL);
    }

    public void setIsDevil(boolean devil) {
        setHellFlag(FLAG_DEVIL, devil);
        var health = this.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.setBaseValue(devil ? 15.0D : 5.0D);
        }
        updateHellbatAttack();
    }

    public boolean getIsExplosive() {
        return getHellFlag(FLAG_EXPLOSIVE);
    }

    public void setIsExplosive(boolean explosive) {
        setHellFlag(FLAG_EXPLOSIVE, explosive);
    }

    public boolean getIsVampire() {
        return getHellFlag(FLAG_VAMPIRE);
    }

    public void setIsVampire(boolean vampire) {
        setHellFlag(FLAG_VAMPIRE, vampire);
    }

    public void updateHellbatAttack() {
        var atk = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) {
            atk.setBaseValue(getIsSummoned() ? (getIsDevil() ? 3 : 2) + this.damBonus : 1.0D);
        }
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return !getIsSummoned() && this.owner == null;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {

            if (!getIsVampire()) {
                this.level().addParticle(ParticleTypes.SMOKE,
                        this.getX() + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.3D,
                        this.getY() + this.getRandom().nextFloat() * 0.5D,
                        this.getZ() + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.3D,
                        0.0D, 0.05D, 0.0D);
                this.level().addParticle(ParticleTypes.FLAME,
                        this.getX() + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.3D,
                        this.getY() + this.getRandom().nextFloat() * 0.5D,
                        this.getZ() + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.3D,
                        0.0D, 0.05D, 0.0D);
            }
            if (getIsExplosive()) {
                arcaneresearch.client.fx.WandFX.sparkle(this.level(),
                        (float) this.getX(), (float) this.getY(), (float) this.getZ(),
                        1.0F, 0xFFAA33, 0.02F);
            }
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.server.level.ServerLevel serverLevel, Entity victim) {
        boolean hit = victim.level() instanceof net.minecraft.server.level.ServerLevel _sl ? victim.hurtServer(_sl, this.damageSources().mobAttack(this),
                (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE)) : false;
        if (hit) {
            victim.setRemainingFireTicks((getIsSummoned() ? 4 : 2) * 20);
            if (getIsVampire() && this.owner instanceof Player player && !player.isDeadOrDying()) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 26, 1));
            }
        }
        return hit;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide() && !getIsDevil()
                && (getIsExplosive() || this.getRandom().nextInt(10) == 0)) {
            this.level().explode(this, this.getX(), this.getY(), this.getZ(),
                    1.5F + (getIsExplosive() ? this.damBonus * 0.33F : 0.0F), false,
                    Level.ExplosionInteraction.NONE);
        }
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, boolean recentlyHit) {
        // NEOFORGE_PORT: looting param removed in 26.x; derived from the killer's weapon
        // (Enchantments.LOOTING level), same value the old caller passed.
        int looting = arcaneresearch.common.lib.utils.EntityUtils.getLootingLevel(serverLevel, source);
        super.dropCustomDeathLoot(serverLevel, source, recentlyHit);
        if (!getIsSummoned()) {
            spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), new ItemStack(Items.GUNPOWDER), this.getBbHeight() / 2.0F);
        }
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("HellbatFlags", this.entityData.get(DATA_HELLBAT_FLAGS));
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_HELLBAT_FLAGS, tag.getByteOr("HellbatFlags", (byte) 0));
        updateHellbatAttack();
    }
}
