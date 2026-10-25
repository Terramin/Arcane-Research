package arcaneresearch.common.entities.projectile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import arcaneresearch.api.casters.FocusEffect;
import arcaneresearch.api.casters.FocusEngine;
import arcaneresearch.api.casters.FocusPackage;
import arcaneresearch.api.casters.Trajectory;
import arcaneresearch.init.ModEntities;
import javax.annotation.Nullable;
import java.util.List;
public class EntityFocusMine extends ThrowableProjectile implements IEntityAdditionalSpawnData {
    private static final EntityDataAccessor<Boolean> DATA_ARMED =
            SynchedEntityData.defineId(EntityFocusMine.class, EntityDataSerializers.BOOLEAN);
    private FocusPackage focusPackage;
    private boolean targetFriendly;
    private int armingCountdown = 40;
    public EntityFocusMine(EntityType<? extends EntityFocusMine> type, Level level) {
        super(type, level);
    }
    public EntityFocusMine(Level level, LivingEntity owner) {
        super(ModEntities.FOCUS_MINE.get(), owner, level);
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
    protected void defineSynchedData() {
        this.entityData.define(DATA_ARMED, false);
    }
    public boolean isArmed() {
        return this.entityData.get(DATA_ARMED);
    }
    public void setArmed(boolean armed) {
        this.entityData.set(DATA_ARMED, armed);
    }
    @Override
    protected float getGravity() {
        return 0.01f;
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
        if (!level().isClientSide && getOwner() == null) {
            discard();
            return;
        }
        if (isAlive() && isArmed()) {
            if (armingCountdown > 0) {
                armingCountdown--;
                return;
            }
            if (tickCount % 5 == 0) {
                if (level().isClientSide) {
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
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("armed", isArmed());
        tag.putBoolean("friendly", targetFriendly);
        if (focusPackage != null) {
            tag.put("pack", focusPackage.serialize());
        }
    }
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        targetFriendly = tag.getBoolean("friendly");
        setArmed(tag.getBoolean("armed"));
        if (isArmed()) {
            armingCountdown = 0;
        }
        if (tag.contains("pack")) {
            focusPackage = new FocusPackage();
            focusPackage.deserialize(tag.getCompound("pack"));
            focusPackage.world = level();
        }
    }
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
    @Override
    public void writeSpawnData(FriendlyByteBuf buf) {
        buf.writeNbt(focusPackage != null ? focusPackage.serialize() : new CompoundTag());
    }
    @Override
    public void readSpawnData(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        if (tag != null && !tag.isEmpty()) {
            try {
                focusPackage = new FocusPackage();
                focusPackage.deserialize(tag);
                focusPackage.world = level();
            } catch (Exception e) {
                focusPackage = null;
            }
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
