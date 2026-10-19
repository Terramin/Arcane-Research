package arcaneresearch.common.entities.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.api.casters.*;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.init.ModEntities;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class EntityFocusCloud extends Entity {
    // NEOFORGE_PORT: Entity.hurtServer abstract in 26.3; this entity is unhurtable (was: no hurt override).
    @Override
    public final boolean hurtServer(net.minecraft.server.level.ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float damage) {
        if (!isInvulnerableToBase(source)) {
            markHurt();
        }
        return false;
    }

    // TODO_PORT (packet wave): IEntityAdditionalSpawnData removed in NeoForge; extra spawn data synced via pairing payload (see below).

    private static final EntityDataAccessor<Float> DATA_RADIUS =
            SynchedEntityData.defineId(EntityFocusCloud.class, EntityDataSerializers.FLOAT);

    private static final Map<Long, Long> COOLDOWN_MAP = new HashMap<>();

    private FocusPackage focusPackage;
    private LivingEntity owner;
    private UUID ownerUUID;
    private int duration;

    public EntityFocusCloud(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public EntityFocusCloud(FocusPackage pack, Trajectory trajectory, float radius, int duration) {
        super(ModEntities.FOCUS_CLOUD.get(), pack.world);

        this.focusPackage = pack;
        this.duration = duration;
        this.noPhysics = true;

        setPos(trajectory.source.x, trajectory.source.y, trajectory.source.z);
        setRadius(radius);

        setOwner(pack.getCaster());
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(DATA_RADIUS, 0.5f);
    }

    public float getRadius() {
        return this.entityData.get(DATA_RADIUS);
    }

    public void setRadius(float radius) {
        if (!level().isClientSide()) {
            this.entityData.set(DATA_RADIUS, radius);
        }

        double x = getX();
        double y = getY();
        double z = getZ();
        setBoundingBox(new AABB(x - radius, y - 0.25, z - radius, x + radius, y + 0.25, z + radius));
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Nullable
    public LivingEntity getOwner() {
        if (owner == null && ownerUUID != null && level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(ownerUUID);
            if (entity instanceof LivingEntity living) {
                owner = living;
            }
        }
        return owner;
    }

    public void setOwner(@Nullable LivingEntity owner) {
        this.owner = owner;
        this.ownerUUID = owner != null ? owner.getUUID() : null;
    }

    @Override
    public void tick() {
        super.tick();

        float radius = getRadius();
        int durationTicks = duration * 20;

        if (!level().isClientSide() && (tickCount > durationTicks || getOwner() == null)) {
            discard();
            return;
        }

        if (isAlive()) {
            if (level().isClientSide()) {

                renderCloudParticles(radius);
            } else {

                if (tickCount % 5 == 0) {
                    applyEffects(radius);
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void renderCloudParticles(float radius) {
        if (focusPackage == null) {
            return;
        }
        java.util.List<IFocusElement> elements = focusPackage.getEffects();
        if (elements == null || elements.isEmpty()) {
            return;
        }
        java.util.ArrayList<FocusEffect> effects = new java.util.ArrayList<>();
        for (IFocusElement el : elements) {
            if (el instanceof FocusEffect effect) {
                effects.add(effect);
            }
        }
        if (effects.isEmpty()) {
            return;
        }
        for (int a = 0; a < radius; ++a) {
            FocusEffect eff = effects.get(random.nextInt(effects.size()));
            FXDispatcher.INSTANCE.drawFocusCloudParticle(
                    getX() + random.nextGaussian() * radius / 2.0 * 0.85,
                    getY() + random.nextGaussian() * radius / 2.0 * 0.85,
                    getZ() + random.nextGaussian() * radius / 2.0 * 0.85,
                    random.nextGaussian() * 0.01, random.nextGaussian() * 0.01, random.nextGaussian() * 0.01,
                    FocusEngine.getElementColor(eff.getKey()));
            eff.renderParticleFX(level(),
                    getX() + random.nextGaussian() * radius / 2.0,
                    getY() + random.nextGaussian() * radius / 2.0,
                    getZ() + random.nextGaussian() * radius / 2.0,
                    random.nextGaussian() * 0.01, random.nextGaussian() * 0.01, random.nextGaussian() * 0.01);
        }
    }

    private void applyEffects(float radius) {
        if (focusPackage == null) return;

        long currentTime = System.currentTimeMillis();
        java.util.ArrayList<Trajectory> trajectories = new java.util.ArrayList<>();
        java.util.ArrayList<net.minecraft.world.phys.HitResult> targets = new java.util.ArrayList<>();

        Vec3 center = position();
        AABB scanBox = new AABB(center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius);
        List<Entity> list = level().getEntities(this, scanBox, e -> !e.isRemoved());

        for (Entity e : list) {
            if (!e.isAlive()) {
                continue;
            }
            if (e instanceof EntityFocusCloud other) {

                Vec3 v = other.position().subtract(center);
                other.move(net.minecraft.world.entity.MoverType.SELF,
                        new Vec3(v.x / 50.0, v.y / 50.0, v.z / 50.0));
                arcaneresearch.common.lib.utils.EntityUtils.pushOutOfBlocks(other);
                continue;
            }
            if (!(e instanceof LivingEntity)) {
                continue;
            }
            long entityKey = e.getId();
            if (COOLDOWN_MAP.containsKey(entityKey) && COOLDOWN_MAP.get(entityKey) > currentTime) {
                continue;
            }
            COOLDOWN_MAP.put(entityKey, currentTime + 2000L);

            Vec3 epv = e.position().add(0.0, e.getBbHeight() / 2.0f, 0.0);
            Trajectory trajectory = new Trajectory(center, epv.subtract(center));
            EntityHitResult hit = new EntityHitResult(e, epv);
            trajectories.add(trajectory);
            targets.add(hit);
        }

        for (int i = 0; i < (int) radius; i++) {
            Vec3 randomDir = new Vec3(
                    random.nextGaussian(),
                    random.nextGaussian(),
                    random.nextGaussian()
            ).normalize();

            var blockHit = level().clip(new net.minecraft.world.level.ClipContext(
                    center,
                    center.add(randomDir.scale(radius)),
                    net.minecraft.world.level.ClipContext.Block.OUTLINE,
                    net.minecraft.world.level.ClipContext.Fluid.NONE,
                    this));

            if (blockHit != null && blockHit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
                long blockKey = blockHit.getBlockPos().asLong();

                if (COOLDOWN_MAP.containsKey(blockKey)) {
                    if (COOLDOWN_MAP.get(blockKey) <= currentTime) {
                        COOLDOWN_MAP.remove(blockKey);
                    } else {
                        continue;
                    }
                }
                COOLDOWN_MAP.put(blockKey, currentTime + 2000L);

                Trajectory trajectory = new Trajectory(center, randomDir);
                trajectories.add(trajectory);
                targets.add(blockHit);
            }
        }

        if (!targets.isEmpty()) {
            LivingEntity thrower = getOwner();
            FocusPackage copy = thrower != null ? focusPackage.copy(thrower) : focusPackage;
            Trajectory[] tra = trajectories.toArray(new Trajectory[0]);
            net.minecraft.world.phys.HitResult[] tgt = targets.toArray(
                    new net.minecraft.world.phys.HitResult[0]);
            arcaneresearch.common.lib.events.ServerEvents.addRunnableServer(level(),
                    () -> FocusEngine.runFocusPackage(copy, tra, tgt), 0);
        }

        if (tickCount % 100 == 0) {
            COOLDOWN_MAP.entrySet().removeIf(entry -> entry.getValue() <= currentTime);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput tag) {
        tag.putInt("Age", tickCount);
        tag.putInt("Duration", duration);
        tag.putFloat("Radius", getRadius());
        if (ownerUUID != null) {
            tag.store("OwnerUUID", net.minecraft.core.UUIDUtil.CODEC, ownerUUID);
        }
        if (focusPackage != null) {
            tag.store("pack", net.minecraft.nbt.CompoundTag.CODEC, focusPackage.serialize());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput tag) {
        tickCount = tag.getIntOr("Age", 0);
        duration = tag.getIntOr("Duration", 0);
        setRadius(tag.getFloatOr("Radius", 0.0F));
        if (tag.read("OwnerUUID", net.minecraft.core.UUIDUtil.CODEC).isPresent()) {
            // NEOFORGE_PORT: getUUID removed -> CODEC read (write side already stores via CODEC).
            ownerUUID = tag.read("OwnerUUID", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
        }
        if (tag.read("pack", net.minecraft.nbt.CompoundTag.CODEC).isPresent()) {
            focusPackage = new FocusPackage();
            focusPackage.deserialize(tag.read("pack", net.minecraft.nbt.CompoundTag.CODEC).orElse(new net.minecraft.nbt.CompoundTag()));
            focusPackage.world = level();
        }
    }

    // NEOFORGE_PORT (26.3-native spawn sync, variant B): Forge IEntityAdditionalSpawnData
    // (FocusPackage NBT only) ported 1:1 to a pairing payload (PacketFocusPackToClient,
    // bundled with spawn). Radius/duration/owner need no spawn packet: radius already
    // synced, duration and owner are server-side only (discard/applyEffects server branch).
    @Override
    public void sendPairingData(net.minecraft.server.level.ServerPlayer player,
                                java.util.function.Consumer<net.minecraft.network.protocol.common.custom.CustomPacketPayload> bundle) {
        super.sendPairingData(player, bundle);
        if (focusPackage != null) {
            bundle.accept(new arcaneresearch.common.lib.network.entity.PacketFocusPackToClient(
                    getId(), focusPackage.serialize()));
        }
    }



    public FocusPackage getFocusPackage() {
        return focusPackage;
    }

    public void setFocusPackage(FocusPackage pack) {
        this.focusPackage = pack;
    }
}
