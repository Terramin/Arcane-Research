package arcaneresearch.common.entities.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.api.casters.*;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.init.ModEntities;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class EntityFocusProjectile extends ThrowableProjectile {
    // (spawn sync: see sendPairingData below)

    public static final int SPECIAL_NONE = 0;

    public static final int SPECIAL_BOUNCY = 1;

    public static final int SPECIAL_SEEKING_HOSTILE = 2;

    public static final int SPECIAL_SEEKING_FRIENDLY = 3;

    private static final EntityDataAccessor<Integer> DATA_SPECIAL =
            SynchedEntityData.defineId(EntityFocusProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_OWNER_ID =
            SynchedEntityData.defineId(EntityFocusProjectile.class, EntityDataSerializers.INT);

    private FocusPackage focusPackage;
    private Entity seekTarget;
    private boolean firstParticle = true;

    public EntityFocusProjectile(EntityType<? extends EntityFocusProjectile> type, Level level) {
        super(type, level);
    }

    public EntityFocusProjectile(Level level, LivingEntity owner) {
        super(ModEntities.FOCUS_PROJECTILE.get(), level);
        setOwner(owner);
        setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
    }

    public EntityFocusProjectile(FocusPackage pack, float speed, Trajectory trajectory, int special) {
        super(ModEntities.FOCUS_PROJECTILE.get(), pack.world);

        this.focusPackage = pack;

        LivingEntity owner = pack.getCaster();
        if (owner != null) {
            setOwner(owner);
            setOwnerId(owner.getId());
        }

        double offsetScale = owner != null ? owner.getBbWidth() * 2.1 : 0.5;
        Vec3 startPos = trajectory.source.add(trajectory.direction.scale(offsetScale));
        setPos(startPos.x, startPos.y, startPos.z);

        Vec3 dir = trajectory.direction.normalize();
        shoot(dir.x, dir.y, dir.z, speed, 0.0f);

        setSpecial(special);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(DATA_SPECIAL, 0);
        builder.define(DATA_OWNER_ID, 0);
    }

    public int getSpecial() {
        return this.entityData.get(DATA_SPECIAL);
    }

    public void setSpecial(int special) {
        this.entityData.set(DATA_SPECIAL, special);
    }

    public int getOwnerId() {
        return this.entityData.get(DATA_OWNER_ID);
    }

    public void setOwnerId(int id) {
        this.entityData.set(DATA_OWNER_ID, id);
    }

    @Override
    protected double getDefaultGravity() {
        return getSpecial() > 1 ? 0.005 : 0.01;
    }

    @Override
    protected void onHit(HitResult result) {
        if (result == null) return;

        int special = getSpecial();

        if (special == SPECIAL_BOUNCY && result.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) result;
            BlockState state = level().getBlockState(blockHit.getBlockPos());

            VoxelShape collisionShape = state.getCollisionShape(level(), blockHit.getBlockPos());
            AABB collision = collisionShape.isEmpty()
                    ? AABB.ofSize(Vec3.ZERO, 0, 0, 0)
                    : collisionShape.bounds();

            if (!collision.equals(AABB.ofSize(Vec3.ZERO, 0, 0, 0))) {

                Vec3 motion = getDeltaMovement();

                switch (blockHit.getDirection().getAxis()) {
                    case X -> setDeltaMovement(motion.x * -0.9, motion.y * 0.9, motion.z * 0.9);
                    case Y -> setDeltaMovement(motion.x * 0.9, motion.y * -0.9, motion.z * 0.9);
                    case Z -> setDeltaMovement(motion.x * 0.9, motion.y * 0.9, motion.z * -0.9);
                }

                Vec3 newMotion = getDeltaMovement();
                double len = newMotion.length();
                // NEOFORGE_PORT (26.3): ThrowableProjectile.tick now moves the projectile TO the hit point
                // before onHit and applies no motion afterwards. Forge nudged back from the pre-move
                // position (== xOld) and then moved by the bounced motion in the same tick; reproduce that
                // end position (nudging from the hit point lands inside the block and re-hits every tick).
                double nx = xOld + newMotion.x;
                double ny = yOld + newMotion.y;
                double nz = zOld + newMotion.z;
                if (len > 0) {
                    nx -= newMotion.x / len * 0.05;
                    ny -= newMotion.y / len * 0.05;
                    nz -= newMotion.z / len * 0.05;
                }
                setPos(nx, ny, nz);

                if (!level().isClientSide()) {
                    // NEOFORGE_PORT: LEASH_KNOT_PLACE was folded into LEAD_TIED in 26.x (vanilla knot uses it).
                    playSound(net.minecraft.sounds.SoundEvents.LEAD_TIED, 0.25f, 1.0f);
                }
                if (newMotion.length() < 0.2) {
                    if (!level().isClientSide()) {
                        discard();
                    }
                }
                return;
            }
        }

        if (!level().isClientSide()) {
            executeFocusPackage(result);
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {

    }

    @Override
    protected void onHitBlock(BlockHitResult result) {

    }

    private void executeFocusPackage(HitResult hit) {
        if (focusPackage == null) return;

        Vec3 prevPos = new Vec3(xOld, yOld, zOld);
        Vec3 motion = getDeltaMovement().normalize();

        Entity owner = getOwner();
        FocusPackage copy = (owner instanceof LivingEntity living) ? focusPackage.copy(living) : focusPackage;
        FocusEngine.runFocusPackage(copy,
                new Trajectory[]{new Trajectory(prevPos, motion)},
                new HitResult[]{hit});
    }

    @Override
    public void tick() {
        super.tick();

        if (tickCount > 1200) {
            discard();
            return;
        }

        if (!level().isClientSide() && getOwner() == null) {
            discard();
            return;
        }

        firstParticle = true;

        int special = getSpecial();
        if (special == SPECIAL_SEEKING_HOSTILE || special == SPECIAL_SEEKING_FRIENDLY) {
            updateSeeking(special == SPECIAL_SEEKING_FRIENDLY);
        }

        if (level().isClientSide() && focusPackage != null) {
            renderParticleTrail();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void renderParticleTrail() {
        List<IFocusElement> elements = focusPackage.getEffects();
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
        for (double i = 0.0; i < 1.0; i += 0.33) {
            double coeff = i;
            FocusEffect eff = effects.get(random.nextInt(effects.size()));
            float scale = 1.0f;
            java.awt.Color c1 = new java.awt.Color(FocusEngine.getElementColor(eff.getKey()));
            FXDispatcher.INSTANCE.drawFireMote(
                    (float) (xOld + (getX() - xOld) * coeff),
                    (float) (yOld + (getY() - yOld) * coeff) + getBbHeight() / 2.0f,
                    (float) (zOld + (getZ() - zOld) * coeff),
                    0.0125f * (random.nextFloat() - 0.5f) * scale,
                    0.0125f * (random.nextFloat() - 0.5f) * scale,
                    0.0125f * (random.nextFloat() - 0.5f) * scale,
                    c1.getRed() / 255.0f, c1.getGreen() / 255.0f, c1.getBlue() / 255.0f,
                    0.5f, 7.0f * scale);
            if (firstParticle) {
                firstParticle = false;
                eff.renderParticleFX(level(),
                        xOld + (getX() - xOld) * coeff + random.nextGaussian() * 0.1,
                        yOld + (getY() - yOld) * coeff + getBbHeight() / 2.0f + random.nextGaussian() * 0.1,
                        zOld + (getZ() - zOld) * coeff + random.nextGaussian() * 0.1,
                        random.nextGaussian() * 0.01, random.nextGaussian() * 0.01, random.nextGaussian() * 0.01);
            }
        }
    }

    private void updateSeeking(boolean seekFriendly) {

        if (seekTarget == null && tickCount % 5 == 0) {
            seekTarget = findSeekTarget(seekFriendly);
        }

        if (seekTarget != null) {

            if (seekTarget.isRemoved() || !canSee(seekTarget)
                    || (tickCount % 5 == 0 && !arcaneresearch.common.lib.utils.EntityUtils
                    .isInFlightCone(this, seekTarget, 1.75f, 16.0))) {
                seekTarget = null;
            } else {
                Vec3 targetPos = seekTarget.getBoundingBox().getCenter();
                Vec3 toTarget = targetPos.subtract(position()).normalize();

                Vec3 motion = getDeltaMovement();
                double speed = motion.length();
                Vec3 newDir = motion.normalize().add(toTarget.scale(0.275)).normalize();

                setDeltaMovement(newDir.scale(speed));
            }
        }
    }

    @Nullable
    private Entity findSeekTarget(boolean seekFriendly) {
        double range = 16.0;
        AABB searchBox = getBoundingBox().inflate(range);

        Predicate<Entity> filter = e -> {
            if (!(e instanceof LivingEntity living)) return false;
            if (e.isRemoved()) return false;
            if (e == getOwner()) return false;

            if (!arcaneresearch.common.lib.utils.EntityUtils.isInFlightCone(this, e, 1.75f, 16.0)) return false;
            if (!canSee(e)) return false;

            boolean isFriendly = isFriendlyTo(living);
            return seekFriendly ? isFriendly : !isFriendly;
        };

        List<Entity> entities = level().getEntities(this, searchBox, filter);

        Entity closest = null;
        double closestDist = range * range;

        for (Entity e : entities) {
            double dist = distanceToSqr(e);
            if (dist < closestDist) {
                closestDist = dist;
                closest = e;
            }
        }

        return closest;
    }

    private boolean isFriendlyTo(LivingEntity target) {
        return arcaneresearch.common.lib.utils.EntityUtils.isFriendly(getOwner(), target);
    }

    private boolean canSee(Entity target) {
        Vec3 start = getEyePosition();
        Vec3 end = target.getEyePosition();
        return level().clip(new net.minecraft.world.level.ClipContext(
                start, end,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                this)).getType() == HitResult.Type.MISS;
    }

    @Override
    public Vec3 getLookAngle() {
        return getDeltaMovement().normalize();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("special", getSpecial());
        if (focusPackage != null) {
            tag.store("pack", net.minecraft.nbt.CompoundTag.CODEC, focusPackage.serialize());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        setSpecial(tag.getIntOr("special", 0));
        if (tag.read("pack", net.minecraft.nbt.CompoundTag.CODEC).isPresent()) {
            focusPackage = new FocusPackage();
            focusPackage.deserialize(tag.read("pack", net.minecraft.nbt.CompoundTag.CODEC).orElse(new net.minecraft.nbt.CompoundTag()));
            focusPackage.world = level();
        }
        if (getOwner() != null) {
            setOwnerId(getOwner().getId());
        }
    }

    // NEOFORGE_PORT (26.3-native spawn sync, variant B): Forge IEntityAdditionalSpawnData
    // (FocusPackage NBT via writeSpawnData/readSpawnData) ported 1:1 to a pairing payload
    // (PacketFocusPackToClient, bundled with spawn); EntityDataSerializers has no NBT
    // serializer in 26.3, so variant A (SynchedEntityData) is impossible.
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
}
