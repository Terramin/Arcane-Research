package arcaneresearch.common.entities;

import arcaneresearch.api.portals.IPortal;
import arcaneresearch.api.portals.IPortalShape;
import arcaneresearch.api.portals.PortalApi;
import arcaneresearch.api.portals.PortalDestination;
import arcaneresearch.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityPortal extends PathfinderMob implements IPortal, IPortalShape {
    private static final int DEFAULT_LIFETIME = 600;
    private static final int OPEN_ANIMATION_TICKS = 20;
    private static final int FADE_TICKS = 40;
    private static final int TARGET_TICKET_DISTANCE = 3;
    private static final int TICKET_REFRESH_TICKS = 20;
    private static final float WIDTH = PortalDestination.DEFAULT_WIDTH;
    private static final float HEIGHT = PortalDestination.DEFAULT_HEIGHT;
    private static final double MODEL_RENDER_BASE = 1.501;
    private static final double CONTOUR_HALF_WIDTH = 25.0 / 16.0;
    private static final EntityDataAccessor<BlockPos> DATA_TELEPORT_TARGET =
            SynchedEntityData.defineId(EntityPortal.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<String> DATA_TELEPORT_DIM =
            SynchedEntityData.defineId(EntityPortal.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_CLOSING =
            SynchedEntityData.defineId(EntityPortal.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_LIFETIME =
            SynchedEntityData.defineId(EntityPortal.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FADE =
            SynchedEntityData.defineId(EntityPortal.class, EntityDataSerializers.INT);
    private float portalWidth = WIDTH;
    private float portalHeight = HEIGHT;
    private int lifetime = DEFAULT_LIFETIME;
    private boolean closing;
    private int closeTicks;
    private float closeFromFraction = 1f;
    private int fadeTicks = -1;
    private int clientCloseStartTick = -1;
    private float clientCloseFromFraction = 1f;

    public EntityPortal(PlayMessages.SpawnEntity packet, Level level) {
        this(ModEntities.PORTAL.get(), level);
    }

    public EntityPortal(EntityType<EntityPortal> type, Level level) {
        super(type, level);
        setMaxUpStep(0f);
        xpReward = 0;
        setNoAi(true);
        setPersistenceRequired();
        super.setNoGravity(true);
        this.moveControl = new FlyingMoveControl(this, 10, true);
    }

    @Override
    public void setTeleportTarget(BlockPos pos) {
        this.entityData.set(DATA_TELEPORT_TARGET, pos == null ? BlockPos.ZERO : pos.immutable());
    }

    @Override
    public void setTeleportDimension(ResourceKey<Level> dimension) {
        ResourceKey<Level> key = dimension == null ? Level.OVERWORLD : dimension;
        this.entityData.set(DATA_TELEPORT_DIM, key.location().toString());
    }

    @Override
    public ResourceKey<Level> getTeleportDimension() {
        ResourceLocation dim = PortalApi.parseResourceLocation(this.entityData.get(DATA_TELEPORT_DIM));
        return dim == null ? Level.OVERWORLD : ResourceKey.create(Registries.DIMENSION, dim);
    }

    @Override
    public BlockPos getTeleportTarget() {
        return this.entityData.get(DATA_TELEPORT_TARGET);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TELEPORT_TARGET, BlockPos.ZERO);
        this.entityData.define(DATA_TELEPORT_DIM, Level.OVERWORLD.location().toString());
        this.entityData.define(DATA_CLOSING, false);
        this.entityData.define(DATA_LIFETIME, DEFAULT_LIFETIME);
        this.entityData.define(DATA_FADE, -1);
    }

    @Override
    public void setSize(float width, float height) {
        this.portalWidth = width;
        this.portalHeight = height;
    }

    @Override
    public void startClosing() {
        if (this.closing) return;
        this.closing = true;
        this.closeTicks = 0;
        this.fadeTicks = -1;
        this.closeFromFraction = openProgress(0f);
        this.entityData.set(DATA_CLOSING, true);
    }

    @Override
    public boolean isClosing() {
        return this.entityData.get(DATA_CLOSING);
    }

    @Override
    public int getDisplayedLifetime() {
        return this.entityData.get(DATA_LIFETIME);
    }

    @Override
    public float getFadeAlpha(float partialTicks) {
        int fade = this.entityData.get(DATA_FADE);
        if (fade < 0) {
            return 1f;
        }
        float f = Math.min((fade + partialTicks) / (float) FADE_TICKS, 1f);
        return 1f - f * f * (3f - 2f * f);
    }

    private static float ease(float f) {
        return f * f * (3f - 2f * f);
    }

    private float openProgress(float partialTicks) {
        float f = Math.min((this.tickCount + partialTicks) / (float) OPEN_ANIMATION_TICKS, 1f);
        return ease(f);
    }

    @Override
    public float getOpenFraction(float partialTicks) {
        boolean closing = isClosing();
        if (!closing) {
            return openProgress(partialTicks);
        }
        float elapsed;
        float fromFraction;
        if (this.level().isClientSide) {
            if (this.clientCloseStartTick < 0) {
                this.clientCloseStartTick = this.tickCount;
                this.clientCloseFromFraction = openProgress(0f);
            }
            elapsed = (this.tickCount - this.clientCloseStartTick) + partialTicks;
            fromFraction = this.clientCloseFromFraction;
        } else {
            elapsed = this.closeTicks + partialTicks;
            fromFraction = this.closeFromFraction;
        }
        float f = ease(Math.min(elapsed / (float) OPEN_ANIMATION_TICKS, 1f));
        return fromFraction * (1f - f);
    }

    @Override
    public Vec3 getPortalPlanePos(float partialTicks) {
        return new Vec3(
                Mth.lerp(partialTicks, this.xo, this.getX()),
                Mth.lerp(partialTicks, this.yo, this.getY()),
                Mth.lerp(partialTicks, this.zo, this.getZ()));
    }

    @Override
    public float getPortalYaw(float partialTicks) {
        return Mth.rotLerp(partialTicks, this.yRotO, this.getYRot());
    }

    @Override
    public Vec3 getFacingNormal(float partialTicks) {
        double rad = Math.toRadians(getPortalYaw(partialTicks));
        return new Vec3(-Math.sin(rad), 0.0D, Math.cos(rad));
    }

    @Override
    public double[] getContourBounds(float partialTicks) {
        float slide = (1f - getOpenFraction(partialTicks)) * OPEN_SLIDE_PX;
        double topEdge = MODEL_RENDER_BASE + (36.0 - slide) / 16.0;
        double bottomEdge = MODEL_RENDER_BASE + (slide - 19.0) / 16.0;
        double topY = getY() + Math.max(topEdge, bottomEdge);
        double bottomY = getY() + Math.min(topEdge, bottomEdge);
        return new double[]{topY, bottomY, CONTOUR_HALF_WIDTH};
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    public @NotNull MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pose) {
        return EntityDimensions.scalable(portalWidth, portalHeight);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, @NotNull DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(@NotNull Entity entity) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public void setNoGravity(boolean ignored) {
        super.setNoGravity(true);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        super.setNoGravity(true);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (!level().isClientSide) {
            if (this.tickCount % TICKET_REFRESH_TICKS == 0) {
                holdTargetChunkTicket();
            }
            if (this.closing) {
                if (this.fadeTicks >= 0) {
                    this.fadeTicks++;
                    this.entityData.set(DATA_FADE, this.fadeTicks);
                    if (this.fadeTicks >= FADE_TICKS) {
                        this.discard();
                        return;
                    }
                } else {
                    this.closeTicks++;
                    if (this.closeTicks >= OPEN_ANIMATION_TICKS) {
                        this.fadeTicks = 0;
                        this.entityData.set(DATA_FADE, 0);
                    }
                }
            } else if (this.tickCount >= OPEN_ANIMATION_TICKS) {
                if (--this.lifetime <= 0) {
                    startClosing();
                }
            }
            if (this.entityData.get(DATA_LIFETIME) != this.lifetime) {
                this.entityData.set(DATA_LIFETIME, this.lifetime);
            }
        }
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        return handleInteraction(player);
    }

    @Override
    public @NotNull InteractionResult interactAt(@NotNull Player player, @NotNull Vec3 hitVec, @NotNull InteractionHand hand) {
        return handleInteraction(player);
    }

    private InteractionResult handleInteraction(Player player) {
        if (level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            if (this.closing) {
                return InteractionResult.SUCCESS;
            }
            if (player.isShiftKeyDown()) {
                startClosing();
            } else {
                PortalApi.teleport(serverPlayer, this);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Nullable
    private ServerLevel resolveTargetLevel() {
        return PortalApi.resolveTargetLevel(this);
    }

    private void holdTargetChunkTicket() {
        ServerLevel target = resolveTargetLevel();
        if (target == null) {
            return;
        }
        target.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(getTeleportTarget()), TARGET_TICKET_DISTANCE, getTeleportTarget());
    }

    private void releaseTargetChunkTicket() {
        ServerLevel target = resolveTargetLevel();
        if (target == null) {
            return;
        }
        target.getChunkSource().removeRegionTicket(TicketType.PORTAL, new ChunkPos(getTeleportTarget()), TARGET_TICKET_DISTANCE, getTeleportTarget());
    }

    @Override
    public void remove(@NotNull RemovalReason reason) {
        releaseTargetChunkTicket();
        super.remove(reason);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        PortalApi.writeDestination(tag, getTeleportTarget(), getTeleportDimension(), portalWidth, portalHeight);
        tag.putInt(PortalApi.TAG_TIMER, lifetime);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        PortalDestination destination = PortalApi.readDestination(tag);
        if (destination != null) {
            setTeleportTarget(destination.pos());
            setTeleportDimension(destination.dimension());
            portalWidth = destination.width();
            portalHeight = destination.height();
        }
        if (tag.contains(PortalApi.TAG_TIMER)) {
            lifetime = tag.getInt(PortalApi.TAG_TIMER);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }
}
