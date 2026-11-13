package arcaneresearch.temp.portal;

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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;

public class PortalEntity extends PathfinderMob {
    private static final int DEFAULT_LIFETIME = 600; // 30 секунд
    private static final int OPEN_ANIMATION_TICKS = 20;
    private static final float WIDTH = 2.4F;
    private static final float HEIGHT = 4.2F;
    /** Сдвиг частей модели от стандартного состояния к центру портала в закрытом виде (в пикселях модели).
     *  27.5px — ровно столько, чтобы в закрытом виде рамки сошлись в центре портала,
     *  а внутренние кромки их золотых полос совпали (36-27.5 = 27.5-19 = 8.5). */
    public static final float OPEN_SLIDE_PX = 27.5F;
    /** Смещение модели живой сущности при отрисовке (LivingEntityRenderer). */
    private static final double MODEL_RENDER_BASE = 1.501;
    /** Цель телепорта — синхронизируется на клиент. */
    private static final EntityDataAccessor<BlockPos> DATA_TELEPORT_TARGET =
            SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.BLOCK_POS);
    /** Измерение цели телепорта — строка вида "namespace:path" (у ResourceKey нет сериализатора). */
    private static final EntityDataAccessor<String> DATA_TELEPORT_DIM =
            SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.STRING);
    private float portalWidth = WIDTH;
    private float portalHeight = HEIGHT;
    private int lifetime = DEFAULT_LIFETIME;

    public PortalEntity(PlayMessages.SpawnEntity packet, Level level) {
        this(ModEntities.PORTAL.get(), level);
    }

    public PortalEntity(EntityType<PortalEntity> type, Level level) {
        super(type, level);
        setMaxUpStep(0f);
        xpReward = 0;
        setNoAi(true);
        setPersistenceRequired();
        super.setNoGravity(true);
        this.moveControl = new FlyingMoveControl(this, 10, true);
    }

    public void setTeleportTarget(BlockPos pos) {
        this.entityData.set(DATA_TELEPORT_TARGET, pos == null ? BlockPos.ZERO : pos.immutable());
    }

    public void setTeleportDimension(ResourceKey<Level> dimension) {
        ResourceKey<Level> key = dimension == null ? Level.OVERWORLD : dimension;
        this.entityData.set(DATA_TELEPORT_DIM, key.location().toString());
    }

    /** Ключ измерения цели — парсится из синхронизированной строки. */
    public ResourceKey<Level> getTeleportDimension() {
        ResourceLocation dim = parseResourceLocation(this.entityData.get(DATA_TELEPORT_DIM));
        return dim == null ? Level.OVERWORLD : ResourceKey.create(Registries.DIMENSION, dim);
    }

    public BlockPos getTeleportTarget() {
        return this.entityData.get(DATA_TELEPORT_TARGET);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TELEPORT_TARGET, BlockPos.ZERO);
        this.entityData.define(DATA_TELEPORT_DIM, Level.OVERWORLD.location().toString());
    }

    public void setSize(float width, float height) {
        this.portalWidth = width;
        this.portalHeight = height;
    }

    /**
     * Прогресс открытия портала: 0 — части модели у центра (закрыт), 1 — полностью открыт.
     * Основан на tickCount, поэтому одинаково работает на сервере и на клиенте.
     */
    public float getOpenFraction(float partialTicks) {
        float f = Math.min((this.tickCount + partialTicks) / (float) OPEN_ANIMATION_TICKS, 1f);
        // плавное ускорение/замедление (smoothstep)
        return f * f * (3f - 2f * f);
    }

    /** Позиция плоскости портала (интерполяция по partialTicks) — плоскость проходит через центр сущности. */
    public Vec3 getPortalPlanePos(float partialTicks) {
        return new Vec3(
                Mth.lerp((double) partialTicks, this.xo, this.getX()),
                Mth.lerp((double) partialTicks, this.yo, this.getY()),
                Mth.lerp((double) partialTicks, this.zo, this.getZ()));
    }

    /** Фактический yaw портала (интерполяция по partialTicks). */
    public float getPortalYaw(float partialTicks) {
        return Mth.rotLerp(partialTicks, this.yRotO, this.getYRot());
    }

    /** Нормаль плоскости портала (горизонтальное направление "лица" портала). */
    public Vec3 getFacingNormal(float partialTicks) {
        double rad = Math.toRadians(getPortalYaw(partialTicks));
        return new Vec3(-Math.sin(rad), 0.0D, Math.cos(rad));
    }

    /** Границы области волны — по внутренней кромке ЗОЛОТОЙ полосы рамки (мировые координаты):
     *  [0]=верх, [1]=низ, [2]=полуширина.
     *  Золотая полоса (box2, UV(0,16), 50px): верхняя рамка — модельные Y (slide-38)..(slide-36),
     *  нижняя рамка — (19-slide)..(21-slide). В закрытом виде кромки сходятся в центре (36-27.5 = 27.5-19 = 8.5). */
    public double[] getContourBounds(float partialTicks) {
        float slide = (1f - getOpenFraction(partialTicks)) * OPEN_SLIDE_PX;
        double topEdge = MODEL_RENDER_BASE + (36.0 - slide) / 16.0;    // нижняя кромка золотой полосы верхней рамки
        double bottomEdge = MODEL_RENDER_BASE + (slide - 19.0) / 16.0; // верхняя кромка золотой полосы нижней рамки
        double topY = getY() + Math.max(topEdge, bottomEdge);
        double bottomY = getY() + Math.min(topEdge, bottomEdge);
        return new double[]{topY, bottomY, 25.0 / 16.0};
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable((float) portalWidth, (float) portalHeight);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
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
    protected void doPush(Entity entity) {
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
            // время существования не убывает, пока портал не открыт полностью
            if (this.tickCount >= OPEN_ANIMATION_TICKS) {
                if (--this.lifetime <= 0) {
                    this.discard();
                }
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        return handleInteraction(player);
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 hitVec, InteractionHand hand) {
        return handleInteraction(player);
    }

    private InteractionResult handleInteraction(Player player) {
        if (level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            teleportPlayer(serverPlayer);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private void teleportPlayer(ServerPlayer player) {
        ServerLevel targetLevel = resolveTargetLevel(player);
        BlockPos safe = getTeleportTarget().above();

        for (int i = 0; i < 8; i++) {
            if (targetLevel.getBlockState(safe).isAir() && targetLevel.getBlockState(safe.above()).isAir()) {
                break;
            }
            safe = safe.above();
        }

        double x = safe.getX() + 0.5;
        double y = safe.getY();
        double z = safe.getZ() + 0.5;

        if (player.isPassenger()) {
            player.stopRiding();
        }

        player.teleportTo(targetLevel, x, y, z, player.getYRot(), player.getXRot());
        player.setDeltaMovement(0, 0, 0);
        player.resetFallDistance();
    }

    private ServerLevel resolveTargetLevel(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server != null) {
            ServerLevel target = server.getLevel(getTeleportDimension());
            if (target != null) {
                return target;
            }
        }
        return player.serverLevel();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        BlockPos target = getTeleportTarget();
        tag.putInt("Pos_X", target.getX());
        tag.putInt("Pos_Y", target.getY());
        tag.putInt("Pos_Z", target.getZ());
        tag.putString("Dim", getTeleportDimension().location().toString());
        tag.putInt("Timer", lifetime);
        tag.putFloat("Width", portalWidth);
        tag.putFloat("Height", portalHeight);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Pos_X") && tag.contains("Pos_Y") && tag.contains("Pos_Z")) {
            setTeleportTarget(new BlockPos(tag.getInt("Pos_X"), tag.getInt("Pos_Y"), tag.getInt("Pos_Z")));
        }
        if (tag.contains("Dim")) {
            ResourceLocation dim = parseResourceLocation(tag.getString("Dim"));
            if (dim != null) {
                setTeleportDimension(ResourceKey.create(Registries.DIMENSION, dim));
            }
        }
        if (tag.contains("Timer")) {
            lifetime = tag.getInt("Timer");
        }
        if (tag.contains("Width")) {
            portalWidth = tag.getFloat("Width");
        }
        if (tag.contains("Height")) {
            portalHeight = tag.getFloat("Height");
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    private static ResourceLocation parseResourceLocation(String value) {
        if (value == null || value.isEmpty()) return null;
        int sep = value.indexOf(':');
        if (sep >= 0) {
            return ResourceLocation.fromNamespaceAndPath(value.substring(0, sep), value.substring(sep + 1));
        }
        return ResourceLocation.fromNamespaceAndPath("minecraft", value);
    }
}