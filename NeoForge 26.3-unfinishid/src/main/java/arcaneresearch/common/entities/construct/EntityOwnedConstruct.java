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
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;

public abstract class EntityOwnedConstruct extends PathfinderMob implements OwnableEntity {

    private static final EntityDataAccessor<Byte> DATA_FLAGS =
            SynchedEntityData.defineId(EntityOwnedConstruct.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Optional<net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.LivingEntity>>> DATA_OWNER_UUID =
            // NEOFORGE_PORT: OPTIONAL_UUID removed in 26.3 -> OPTIONAL_LIVING_ENTITY_REFERENCE.
            SynchedEntityData.defineId(EntityOwnedConstruct.class, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE);

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
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLAGS, (byte) 0);
        builder.define(DATA_OWNER_UUID, Optional.empty());
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
    // NEOFORGE_PORT: OwnableEntity.getOwnerUUID gone in 26.3 (only getOwnerReference); kept as helper.
    public UUID getOwnerUUID() {
        var ref = this.entityData.get(DATA_OWNER_UUID);
        return ref.map(net.minecraft.world.entity.EntityReference::getUUID).orElse(null);
    }

    // NEOFORGE_PORT: OwnableEntity.getOwnerReference() abstract in 26.x (verified in 26.3 jar).
    @Override
    public net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.LivingEntity> getOwnerReference() {
        return this.entityData.get(DATA_OWNER_UUID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_OWNER_UUID, Optional.ofNullable(uuid)
                .map(net.minecraft.world.entity.EntityReference::<net.minecraft.world.entity.LivingEntity>of));
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
    public net.minecraft.world.scores.PlayerTeam getTeam() {
        if (isOwned()) {
            LivingEntity owner = getOwner();
            if (owner != null) {
                return owner.getTeam();
            }
        }
        return super.getTeam();
    }

    // NEOFORGE_PORT: Entity.isAlliedTo(Entity) final in 26.x -> considersEntityAsAlly (same body).
    @Override
    public boolean considersEntityAsAlly(Entity other) {
        if (isOwned()) {
            LivingEntity owner = getOwner();
            if (other == owner) {
                return true;
            }
            if (owner != null) {
                return owner.isAlliedTo(other);
            }
        }
        return super.considersEntityAsAlly(other);
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

        if (getTarget() != null && considersEntityAsAlly(getTarget())) {
            setTarget(null);
        }

        if (!level().isClientSide() && !validSpawn) {
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

        if (!level().isClientSide() && !isOwner(player)) {
            player.sendOverlayMessage(Component.translatable("tc.notowned"));
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void die(DamageSource source) {
        // NEOFORGE_PORT: Level.getGameRules removed in 26.x -> ServerLevel (Thaumaturge oracle).
        if (!level().isClientSide() && level() instanceof net.minecraft.server.level.ServerLevel serverLevel
                && serverLevel.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.SHOW_DEATH_MESSAGES)
                && hasCustomName() && getOwner() instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(getCombatTracker().getDeathMessage());
        }
        super.die(source);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("ValidSpawn", validSpawn);

        UUID uuid = getOwnerUUID();
        if (uuid != null) {
            // NEOFORGE_PORT: ValueOutput.putUUID gone in 26.3 -> codec store.
            tag.store("Owner", net.minecraft.core.UUIDUtil.CODEC, uuid);
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        validSpawn = tag.getBooleanOr("ValidSpawn", false) || tag.getBooleanOr("v", false);

        // NEOFORGE_PORT: ValueInput UUID/contains helpers gone in 26.3 -> codec read.
        java.util.Optional<UUID> owner = tag.read("Owner", net.minecraft.core.UUIDUtil.CODEC);
        if (owner.isPresent()) {
            setOwnerUUID(owner.get());
            setOwned(true);
        } else {
            String uuidStr = tag.getStringOr("OwnerUUID", "");
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
