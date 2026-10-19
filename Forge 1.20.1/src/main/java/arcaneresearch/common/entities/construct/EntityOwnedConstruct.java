package arcaneresearch.common.entities.construct;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Team;
import arcaneresearch.init.ModSounds;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
public abstract class EntityOwnedConstruct extends PathfinderMob implements OwnableEntity {
    private static final EntityDataAccessor<Byte> DATA_FLAGS =
            SynchedEntityData.defineId(EntityOwnedConstruct.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
            SynchedEntityData.defineId(EntityOwnedConstruct.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final byte FLAG_OWNED = 0x04;
    private boolean validSpawn = false;
    protected EntityOwnedConstruct(EntityType<? extends EntityOwnedConstruct> type, Level level) {
        super(type, level);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS, (byte) 0);
        this.entityData.define(DATA_OWNER_UUID, Optional.empty());
    }
    public boolean isOwned() {
        return (this.entityData.get(DATA_FLAGS) & FLAG_OWNED) != 0;
    }
    public void setOwned(boolean owned) {
        byte flags = this.entityData.get(DATA_FLAGS);
        if (owned) {
            this.entityData.set(DATA_FLAGS, (byte) (flags | FLAG_OWNED));
        } else {
            this.entityData.set(DATA_FLAGS, (byte) (flags & ~FLAG_OWNED));
        }
    }
    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNER_UUID).orElse(null);
    }
    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_OWNER_UUID, Optional.ofNullable(uuid));
    }
    @Nullable
    @Override
    public LivingEntity getOwner() {
        try {
            UUID uuid = getOwnerUUID();
            return uuid == null ? null : level().getPlayerByUUID(uuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    public boolean isOwner(LivingEntity entity) {
        return entity == getOwner();
    }
    @Nullable
    @Override
    public Team getTeam() {
        if (isOwned()) {
            LivingEntity owner = getOwner();
            if (owner != null) {
                return owner.getTeam();
            }
        }
        return super.getTeam();
    }
    @Override
    public boolean isAlliedTo(Entity other) {
        if (isOwned()) {
            LivingEntity owner = getOwner();
            if (other == owner) {
                return true;
            }
            if (owner != null) {
                return owner.isAlliedTo(other);
            }
        }
        return super.isAlliedTo(other);
    }
    public void setValidSpawn() {
        this.validSpawn = true;
    }
    public boolean hasValidSpawn() {
        return this.validSpawn;
    }
    @Override
    public void tick() {
        super.tick();
        if (getTarget() != null && isAlliedTo(getTarget())) {
            setTarget(null);
        }
        if (!level().isClientSide && !validSpawn) {
            discard();
        }
    }
    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }
    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }
    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CLACK.get();
    }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.CLACK.get();
    }
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.TOOL.get();
    }
    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }
    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (isRemoved()) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level().isClientSide && !isOwner(player)) {
            player.displayClientMessage(Component.translatable("tc.notowned"), true);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }
    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide && level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_SHOWDEATHMESSAGES)
                && hasCustomName() && getOwner() instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(getCombatTracker().getDeathMessage());
        }
        super.die(source);
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("ValidSpawn", validSpawn);
        UUID uuid = getOwnerUUID();
        if (uuid != null) {
            tag.putUUID("Owner", uuid);
        }
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        validSpawn = tag.getBoolean("ValidSpawn") || tag.getBoolean("v");
        if (tag.hasUUID("Owner")) {
            setOwnerUUID(tag.getUUID("Owner"));
            setOwned(true);
        } else if (tag.contains("OwnerUUID", 8)) {
            String uuidStr = tag.getString("OwnerUUID");
            if (!uuidStr.isEmpty()) {
                try {
                    setOwnerUUID(UUID.fromString(uuidStr));
                    setOwned(true);
                } catch (Throwable e) {
                    setOwned(false);
                }
            }
        }
    }
}
