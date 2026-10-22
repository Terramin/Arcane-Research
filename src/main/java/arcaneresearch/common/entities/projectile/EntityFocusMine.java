package arcaneresearch.common.entities.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.api.casters.FocusEffect;
import arcaneresearch.api.casters.FocusEngine;
import arcaneresearch.api.casters.FocusPackage;
import arcaneresearch.api.casters.Trajectory;
import arcaneresearch.init.ModEntities;

import javax.annotation.Nullable;
import java.util.List;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;

public class EntityFocusMine extends ThrowableProjectile {
    

    private static final EntityDataAccessor<Boolean> DATA_ARMED =
            SynchedEntityData.defineId(EntityFocusMine.class, EntityDataSerializers.BOOLEAN);

    private FocusPackage focusPackage;
    private boolean targetFriendly;
    private int armingCountdown = 40;

    public EntityFocusMine(EntityType<? extends EntityFocusMine> type, Level level) {
        super(type, level);
    }

    public EntityFocusMine(Level level, LivingEntity owner) {
        super(ModEntities.FOCUS_MINE.get(), level);
        setOwner(owner);
        setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
    }

    public EntityFocusMine(FocusPackage pack, Trajectory trajectory, boolean targetFriendly) {
        super(ModEntities.FOCUS_MINE.get(), pack.world);

        this.focusPackage = pack;
        this.targetFriendly = targetFriendly;

        LivingEntity owner = findOwner(pack);
        if (owner != null) {
            setOwner(owner);
        }

        setPos(trajectory.source.x, trajectory.source.y, trajectory.source.z);

        shoot(trajectory.direction.x, trajectory.direction.y, trajectory.direction.z, 0.0f, 0.0f);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(DATA_ARMED, false);
    }

    public boolean isArmed() {
        return this.entityData.get(DATA_ARMED);
    }

    public void setArmed(boolean armed) {
        this.entityData.set(DATA_ARMED, armed);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.01;
    }

    @Override
    protected void onHit(HitResult result) {

        if (result != null && getOwner() != null) {
            setArmed(true);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {

    }

    @Override
    public void tick() {
        super.tick();

        if (arcaneresearch.common.lib.utils.EntityUtils.pushOutOfBlocks(this)) {
            setDeltaMovement(getDeltaMovement().scale(0.25));
        }

        if (tickCount > 1200) {
            discard();
            return;
        }

        if (!level().isClientSide() && getOwner() == null) {
            discard();
            return;
        }

        if (isAlive() && isArmed()) {

            if (armingCountdown > 0) {
                armingCountdown--;
                return;
            }

            if (tickCount % 5 == 0) {
                if (level().isClientSide()) {
                    renderMineParticles();
                } else {
                    checkForTargets();
                }
            }
        }
    }

    private void renderMineParticles() {
        if (focusPackage == null) {
            return;
        }
        FocusEffect[] effects = focusPackage.getFocusEffects();
        if (effects == null || effects.length == 0) {
            return;
        }
        FocusEffect eff = effects[random.nextInt(effects.length)];
        eff.renderParticleFX(level(),
                getX() + random.nextGaussian() * 0.1,
                getY() + random.nextGaussian() * 0.1,
                getZ() + random.nextGaussian() * 0.1,
                random.nextGaussian() * 0.01,
                random.nextGaussian() * 0.01,
                random.nextGaussian() * 0.01);
    }

    private void checkForTargets() {
        if (focusPackage == null) return;

        List<LivingEntity> list = arcaneresearch.common.lib.utils.EntityUtils.getEntitiesInRange(
                level(), blockPosition(), 1.0, LivingEntity.class);
        int d = 0;
        for (LivingEntity e : list) {
            if (!e.isAlive()) {
                continue;
            }
            if (targetFriendly) {
                if (!arcaneresearch.common.lib.utils.EntityUtils.isFriendly(focusPackage.getCaster(), e)) {
                    continue;
                }
            } else if (arcaneresearch.common.lib.utils.EntityUtils.isFriendly(focusPackage.getCaster(), e)) {
                continue;
            }
            Vec3 epv = e.position().add(0.0, e.getBbHeight() / 2.0f, 0.0);
            Vec3 start = position();
            Trajectory trajectory = new Trajectory(start, epv.subtract(start).normalize());
            EntityHitResult ray = new EntityHitResult(e, epv);
            LivingEntity thrower = focusPackage.getCaster();
            FocusPackage copy = thrower != null ? focusPackage.copy(thrower) : focusPackage;
            arcaneresearch.common.lib.events.ServerEvents.addRunnableServer(level(),
                    () -> FocusEngine.runFocusPackage(copy,
                            new Trajectory[]{trajectory}, new HitResult[]{ray}), d++);
        }
        if (d > 0) {
            discard();
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("armed", isArmed());
        tag.putBoolean("friendly", targetFriendly);
        if (focusPackage != null) {
            tag.store("pack", net.minecraft.nbt.CompoundTag.CODEC, focusPackage.serialize());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        targetFriendly = tag.getBooleanOr("friendly", false);
        setArmed(tag.getBooleanOr("armed", false));
        if (isArmed()) {
            armingCountdown = 0;
        }
        if (tag.read("pack", net.minecraft.nbt.CompoundTag.CODEC).isPresent()) {
            focusPackage = new FocusPackage();
            focusPackage.deserialize(tag.read("pack", net.minecraft.nbt.CompoundTag.CODEC).orElse(new net.minecraft.nbt.CompoundTag()));
            focusPackage.world = level();
        }
    }

    
    
    
    
    @Override
    public void sendPairingData(net.minecraft.server.level.ServerPlayer player,
                                java.util.function.Consumer<net.minecraft.network.protocol.common.custom.CustomPacketPayload> bundle) {
        super.sendPairingData(player, bundle);
        if (focusPackage != null) {
            bundle.accept(new arcaneresearch.common.lib.network.entity.PacketFocusPackToClient(
                    getId(), focusPackage.serialize()));
        }
    }



    @Nullable
    private LivingEntity findOwner(FocusPackage pack) {
        return pack.getCaster();
    }

    public FocusPackage getFocusPackage() {
        return focusPackage;
    }

    public void setFocusPackage(FocusPackage pack) {
        this.focusPackage = pack;
    }

    public boolean isTargetingFriendly() {
        return targetFriendly;
    }
}
