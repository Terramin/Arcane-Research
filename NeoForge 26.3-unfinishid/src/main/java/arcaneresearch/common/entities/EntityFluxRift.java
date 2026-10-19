package arcaneresearch.common.entities;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.api.casters.FocusEngine;
import arcaneresearch.api.casters.FocusMediumRoot;
import arcaneresearch.api.casters.FocusPackage;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.blocks.world.taint.TaintHelper;
import arcaneresearch.common.entities.monster.EntityWisp;
import arcaneresearch.common.entities.monster.tainted.EntityTaintSeedPrime;
import arcaneresearch.common.items.casters.foci.FocusEffectFlux;
import arcaneresearch.common.items.casters.foci.FocusMediumCloud;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.fx.PacketFXBlockBamf;
import arcaneresearch.common.lib.utils.RandomItemChooser;
import arcaneresearch.init.ModEffects;
import arcaneresearch.init.ModEntities;
import arcaneresearch.init.ModItems;
import arcaneresearch.init.ModSounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;

public class EntityFluxRift extends Entity {
    // NEOFORGE_PORT: Entity.hurtServer abstract in 26.3; this entity is unhurtable (was: no hurt override).
    @Override
    public final boolean hurtServer(net.minecraft.server.level.ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float damage) {
        if (!isInvulnerableToBase(source)) {
            markHurt();
        }
        return false;
    }


    private static final EntityDataAccessor<Integer> DATA_SEED =
            SynchedEntityData.defineId(EntityFluxRift.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SIZE =
            SynchedEntityData.defineId(EntityFluxRift.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_STABILITY =
            SynchedEntityData.defineId(EntityFluxRift.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_COLLAPSE =
            SynchedEntityData.defineId(EntityFluxRift.class, EntityDataSerializers.BOOLEAN);
    private static final ArrayList<RandomItemChooser.Item> RIFT_EVENTS = new ArrayList<>();

    static {

        RIFT_EVENTS.add(new FluxEventEntry(0, 50, 5, true));
        RIFT_EVENTS.add(new FluxEventEntry(1, 10, 0, false));
        RIFT_EVENTS.add(new FluxEventEntry(2, 20, 10, true));
        RIFT_EVENTS.add(new FluxEventEntry(3, 20, 10, true));
        RIFT_EVENTS.add(new FluxEventEntry(4, 1, 0, true));
    }

    public ArrayList<Vec3> points = new ArrayList<>();
    public ArrayList<Float> pointsWidth = new ArrayList<>();
    /**
     * РђС‚РѕРјР°СЂРЅС‹Р№ СЃРЅР°РїС€РѕС‚ С„РѕСЂРјС‹: РїРµСЂРµСЃС‡С‘С‚ СЃРѕР±РёСЂР°РµС‚ РќРћР’Р«Р• СЃРїРёСЃРєРё Рё РїСѓР±Р»РёРєСѓРµС‚
     * РёС… РѕРґРЅРѕР№ volatile-Р·Р°РїРёСЃСЊСЋ. Р РµРЅРґРµСЂ (РІ С‚.С‡. РІРѕСЂРєРµСЂС‹ РјРµС€РёРЅРіР°) Р±РµСЂС‘С‚
     * СЃСЃС‹Р»РєСѓ РѕРґРёРЅ СЂР°Р· Р·Р° РєР°РґСЂ Рё РІРёРґРёС‚ РІСЃРµРіРґР° С†РµР»СЊРЅС‹Р№ РЅР°Р±РѕСЂ вЂ” РЅРёРєР°РєРёС…
     * ConcurrentModification Рё СЂРІР°РЅС‹С… С‚РѕС‡РµРє. In-place clear/add Р±РѕР»СЊС€Рµ
     * РЅРµ РёСЃРїРѕР»СЊР·СѓРµС‚СЃСЏ.
     */
    public volatile List<Vec3> pointsView = List.of();
    public volatile List<Float> pointsWidthView = List.of();
    private int maxSize = 0;
    private int lastSize = -1;

    public EntityFluxRift(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public EntityFluxRift(Level level) {
        this(ModEntities.FLUX_RIFT.get(), level);
    }

    public static void createRift(Level level, BlockPos pos) {
        if (level.isClientSide()) return;

        pos = pos.offset(level.getRandom().nextInt(16), 0, level.getRandom().nextInt(16));
        BlockPos spawnPos;
        if (!level.dimensionType().hasSkyLight()) {

            spawnPos = new BlockPos(pos.getX(), 10, pos.getZ());
            while (!level.getBlockState(spawnPos).isAir()) {
                spawnPos = spawnPos.above(level.getRandom().nextInt(5) + 1);
                if (spawnPos.getY() > level.getMaxY() - 5) {
                    return;
                }
            }
        } else {
            spawnPos = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, pos);
        }

        if (spawnPos.getY() >= level.getMaxY() - 4) return;

        List<EntityFluxRift> nearbyRifts = level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(EntityFluxRift.class), new AABB(spawnPos).inflate(32.0), e -> true);
        if (!nearbyRifts.isEmpty()) return;

        float flux = AuraHelper.getFlux(level, spawnPos);
        double size = Math.sqrt(flux * 3.0f);

        if (size > 5) {
            EntityFluxRift rift = new EntityFluxRift(level);
            rift.setRiftSeed(level.getRandom().nextInt());
            rift.teleportTo(spawnPos.getX() + 0.5, spawnPos.getY() + 0.5, spawnPos.getZ() + 0.5);
            rift.setYRot(level.getRandom().nextFloat() * 360.0f); rift.setXRot(0.0f);;

            if (level.addFreshEntity(rift)) {
                rift.setRiftSize((int) size);
                AuraHelper.drainFlux(level, spawnPos, (float) size, false);

                List<Player> targets = level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(Player.class), new AABB(spawnPos).inflate(32.0), e -> true);
                for (Player target : targets) {
                    if (!ArcaneresearchCapabilities.isResearchKnown(target, "f_toomuchflux")) {
                        target.sendOverlayMessage(Component.translatable("tc.fluxevent.3")
                                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
                        ArcaneresearchApi.internalMethods.completeResearch(target, "f_toomuchflux");
                    }
                }
            }
        }
    }

    public List<Vec3> getPoints() {
        return pointsView;
    }

    public List<Float> getPointWidths() {
        return pointsWidthView;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(DATA_SEED, 0);
        builder.define(DATA_SIZE, 5);
        builder.define(DATA_STABILITY, 0.0f);
        builder.define(DATA_COLLAPSE, false);
    }

    public int getRiftSeed() {
        return this.entityData.get(DATA_SEED);
    }

    public void setRiftSeed(int seed) {
        this.entityData.set(DATA_SEED, seed);
    }

    public int getRiftSize() {
        return this.entityData.get(DATA_SIZE);
    }

    public void setRiftSize(int size) {
        this.entityData.set(DATA_SIZE, Math.max(1, size));
        recalculateShape();
    }

    public float getRiftStability() {
        return this.entityData.get(DATA_STABILITY);
    }

    public void setRiftStability(float stability) {
        this.entityData.set(DATA_STABILITY, Mth.clamp(stability, -100.0f, 100.0f));
    }

    public boolean isCollapsing() {
        return this.entityData.get(DATA_COLLAPSE);
    }

    public void setCollapsing(boolean collapse) {
        if (collapse) {
            this.maxSize = getRiftSize();
        }
        this.entityData.set(DATA_COLLAPSE, collapse);
    }

    public void addStability(float amount) {
        setRiftStability(getRiftStability() + amount);
    }

    public EnumStability getStability() {
        float stability = getRiftStability();
        if (stability > 50.0f) return EnumStability.VERY_STABLE;
        if (stability >= 0.0f) return EnumStability.STABLE;
        if (stability > -25.0f) return EnumStability.UNSTABLE;
        return EnumStability.VERY_UNSTABLE;
    }

    private void recalculateShape() {
        // РЎС‚СЂРѕРёРј РІ Р»РѕРєР°Р»СЊРЅС‹Рµ СЃРїРёСЃРєРё Рё РїСѓР±Р»РёРєСѓРµРј Р°С‚РѕРјР°СЂРЅРѕ: С‡РёС‚Р°С‚РµР»Рё РІСЃРµРіРґР°
        // РІРёРґСЏС‚ Р»РёР±Рѕ СЃС‚Р°СЂС‹Р№, Р»РёР±Рѕ РЅРѕРІС‹Р№ РЅР°Р±РѕСЂ С†РµР»РёРєРѕРј, РЅРѕ РЅРµ РїРѕР»СѓСЃРѕР±СЂР°РЅРЅС‹Р№.
        ArrayList<Vec3> np = new ArrayList<>();
        ArrayList<Float> nw = new ArrayList<>();
        calcSteps(np, nw, new Random(getRiftSeed()));
        this.points = np;
        this.pointsWidth = nw;
        this.pointsView = java.util.Collections.unmodifiableList(np);
        this.pointsWidthView = java.util.Collections.unmodifiableList(nw);
        lastSize = getRiftSize();

        if (points.isEmpty()) {
            setBoundingBox(new AABB(getX() - 1, getY() - 1, getZ() - 1,
                    getX() + 1, getY() + 1, getZ() + 1));
            return;
        }

        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;

        for (Vec3 v : points) {
            minX = Math.min(minX, v.x);
            maxX = Math.max(maxX, v.x);
            minY = Math.min(minY, v.y);
            maxY = Math.max(maxY, v.y);
            minZ = Math.min(minZ, v.z);
            maxZ = Math.max(maxZ, v.z);
        }

        setBoundingBox(new AABB(
                getX() + minX, getY() + minY, getZ() + minZ,
                getX() + maxX, getY() + maxY, getZ() + maxZ));
    }

    private void calcSteps(ArrayList<Vec3> pp, ArrayList<Float> ww, Random rr) {
        pp.clear();
        ww.clear();

        Vec3 right = new Vec3(rr.nextGaussian(), rr.nextGaussian(), rr.nextGaussian()).normalize();
        Vec3 left = right.scale(-1.0);
        Vec3 lr = Vec3.ZERO;
        Vec3 ll = Vec3.ZERO;

        int steps = Mth.ceil(getRiftSize() / 3.0f);
        float girth = getRiftSize() / 300.0f;
        double angle = 0.33;
        float dec = girth / steps;

        for (int a = 0; a < steps; ++a) {
            girth -= dec;

            right = rotateVec(right, (float) (rr.nextGaussian() * angle), (float) (rr.nextGaussian() * angle));
            lr = lr.add(right.scale(0.2));
            pp.add(new Vec3(lr.x, lr.y, lr.z));
            ww.add(girth);

            left = rotateVec(left, (float) (rr.nextGaussian() * angle), (float) (rr.nextGaussian() * angle));
            ll = ll.add(left.scale(0.2));
            pp.add(0, new Vec3(ll.x, ll.y, ll.z));
            ww.add(0, girth);
        }

        lr = lr.add(right.scale(0.1));
        pp.add(new Vec3(lr.x, lr.y, lr.z));
        ww.add(0.0f);

        ll = ll.add(left.scale(0.1));
        pp.add(0, new Vec3(ll.x, ll.y, ll.z));
        ww.add(0, 0.0f);
    }

    private Vec3 rotateVec(Vec3 vec, float pitch, float yaw) {

        double cosPitch = Math.cos(pitch);
        double sinPitch = Math.sin(pitch);
        double y1 = vec.y * cosPitch + vec.z * sinPitch;
        double z1 = vec.z * cosPitch - vec.y * sinPitch;
        double cosYaw = Math.cos(yaw);
        double sinYaw = Math.sin(yaw);
        double x2 = vec.x * cosYaw + z1 * sinYaw;
        double z2 = z1 * cosYaw - vec.x * sinYaw;
        return new Vec3(x2, y1, z2);
    }

    @Override
    public void tick() {
        super.tick();

        if (lastSize != getRiftSize()) {
            recalculateShape();
        }

        if (!level().isClientSide()) {
            serverTick();
        } else {
            clientTick();
        }
    }

    private void serverTick() {

        if (getRiftSeed() == 0) {
            setRiftSeed(random.nextInt());
        }

        if (!points.isEmpty() && points.size() > 1) {
            int pi = random.nextInt(points.size() - 1);
            Vec3 v1 = points.get(pi).add(getX(), getY(), getZ());
            Vec3 v2 = points.get(pi + 1).add(getX(), getY(), getZ());

            BlockHitResult hit = level().clip(new ClipContext(v1, v2,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = hit.getBlockPos();
                BlockState state = level().getBlockState(pos);

                if (!state.isAir() && state.getDestroySpeed(level(), pos) >= 0.0f
                        && !state.getCollisionShape(level(), pos).isEmpty()) {

                    level().levelEvent(2001, pos, Block.getId(state));
                    level().destroyBlock(pos, false);
                }
            }

            List<Entity> entities = level().getEntities(this,
                    new AABB(v1.x - 0.5, v1.y - 0.5, v1.z - 0.5,
                            v1.x + 0.5, v1.y + 0.5, v1.z + 0.5));
            for (Entity e : entities) {
                if (e.isAlive()) {
                    if (e instanceof Player player && player.isCreative()) {
                        continue;
                    }
                    e.hurt(damageSources().fellOutOfWorld(), 2.0f);
                    if (e instanceof ItemEntity) {
                        e.discard();
                    }
                }
            }
        }

        if (points.size() < 3 && !isCollapsing()) {
            setCollapsing(true);
        }

        if (isCollapsing()) {
            setRiftSize(getRiftSize() - 1);

            if (random.nextBoolean()) {
                AuraHelper.addVis(level(), blockPosition(), 1.0f);
            } else {
                AuraHelper.polluteAura(level(), blockPosition(), 1.0f, false);
            }

            if (random.nextInt(10) == 0) {
                float strength = random.nextFloat() / 2.0f;
                double ex = getX() + random.nextGaussian() * 2.0;
                double ey = getY() + random.nextGaussian() * 2.0;
                double ez = getZ() + random.nextGaussian() * 2.0;
                level().explode(this, ex, ey, ez, strength, false,
                        Level.ExplosionInteraction.NONE);
            }

            if (getRiftSize() <= 1) {
                completeCollapse();
                return;
            }
        }

        if (tickCount % 120 == 0) {
            setRiftStability(getRiftStability() - 0.2f);
        }

        if (tickCount % 600 == getId() % 600) {

            float flux = AuraHelper.getFlux(level(), blockPosition());
            double sizeThreshold = Math.sqrt(getRiftSize() * 2);
            if (flux >= sizeThreshold && getRiftSize() < 100 && getStability() != EnumStability.VERY_STABLE) {
                AuraHelper.drainFlux(level(), blockPosition(), (float) sizeThreshold, false);
                setRiftSize(getRiftSize() + 1);
            }

            if (getRiftStability() < 0.0f && random.nextInt(1000) < Math.abs(getRiftStability()) + getRiftSize()) {
                executeRiftEvent();
            }
        }

        if (!isRemoved() && tickCount % 300 == 0) {
            playSound(ModSounds.EVIL_PORTAL.get(),
                    (float) (0.15 + random.nextGaussian() * 0.066),
                    (float) (0.75 + random.nextGaussian() * 0.1));
        }
    }

    private void clientTick() {

        if (!points.isEmpty() && points.size() > 2) {
            if (!isCollapsing() && getRiftStability() < 0.0f && random.nextInt(150) < Math.abs(getRiftStability())) {
                int pi = 1 + random.nextInt(points.size() - 2);
                Vec3 v = points.get(pi).add(getX(), getY(), getZ());
                FXDispatcher.INSTANCE.drawCurlyWisp(v.x, v.y, v.z, 0.0, 0.0, 0.0,
                        0.1f + pointsWidth.get(pi) * 3.0f, 1.0f, 1.0f, 1.0f, 0.25f, null, 1, 0, 0);
            }

            if (isCollapsing()) {
                int pi = 1 + random.nextInt(points.size() - 2);
                Vec3 v = points.get(pi).add(getX(), getY(), getZ());
                FXDispatcher.INSTANCE.drawCurlyWisp(v.x, v.y, v.z, 0.0, 0.0, 0.0,
                        0.1f + pointsWidth.get(pi) * 3.0f, 1.0f,
                        0.3f + random.nextFloat() * 0.1f, 0.3f + random.nextFloat() * 0.1f,
                        0.4f, null, 1, 0, 0);
            }
        }
    }

    private void executeRiftEvent() {
        RandomItemChooser ric = new RandomItemChooser();
        FluxEventEntry ei = (FluxEventEntry) ric.chooseOnWeight(RIFT_EVENTS);
        if (ei == null) {
            return;
        }
        if (!ei.nearTaintAllowed && TaintHelper.isNearTaintSeed(level(), blockPosition())) {
            return;
        }
        boolean didit = false;
        switch (ei.event) {
            case 0: {
                EntityWisp wisp = new EntityWisp(level());
                wisp.teleportTo(getX() + random.nextGaussian() * 5.0, getY() + random.nextGaussian() * 5.0, getZ() + random.nextGaussian() * 5.0);
                wisp.setYRot(0.0f); wisp.setXRot(0.0f);;
                if (random.nextInt(5) == 0) {
                    wisp.setWispType(Aspect.FLUX.getTag());
                }
                if (wisp.checkSpawnRules(level(), EntitySpawnReason.EVENT) && level().addFreshEntity(wisp)) {
                    didit = true;
                    break;
                }
                break;
            }
            case 1: {
                EntityTaintSeedPrime seed = new EntityTaintSeedPrime(level());
                seed.teleportTo((int) (getX() + random.nextGaussian() * 5.0) + 0.5, (int) (getY() + random.nextGaussian() * 5.0), (int) (getZ() + random.nextGaussian() * 5.0) + 0.5);
                seed.setYRot(random.nextFloat() * 360.0f); seed.setXRot(0.0f);;

                if (seed.checkSpawnRules(level(), EntitySpawnReason.EVENT) && level().addFreshEntity(seed)) {
                    didit = true;
                    seed.boost = getRiftSize();
                    AuraHelper.polluteAura(level(), blockPosition(), getRiftSize() / 2.0f, true);
                    discard();
                    break;
                }
                break;
            }
            case 2: {
                List<LivingEntity> targets = level().getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(LivingEntity.class), getBoundingBox().inflate(16.0), e -> true);
                if (targets != null && !targets.isEmpty()) {
                    for (LivingEntity target : targets) {
                        didit = true;
                        if (target instanceof Player player) {
                            player.sendOverlayMessage(Component.translatable("tc.fluxevent.2")
                                    .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
                        }
                        MobEffectInstance pe = new MobEffectInstance(
                                ModEffects.INFECTIOUS_VIS_EXHAUST, 3000, 2);
                        // TODO_PORT: incurable effect (getCurativeItems gone in 26.3); rework deferred.
                        try {
                            target.addEffect(pe);
                        } catch (Exception ignored) {
                        }
                    }
                    break;
                }
                break;
            }
            case 3: {
                Player target = level().getNearestPlayer(getX(), getY(), getZ(), 16.0, e -> true);
                if (target != null) {

                    FocusPackage p = new FocusPackage(target);
                    FocusMediumRoot root = new FocusMediumRoot();
                    root.setupFromCasterToTarget(target, target, 0.5);
                    p.addNode(root);
                    FocusMediumCloud cloud = new FocusMediumCloud();
                    cloud.initialize();
                    cloud.getSetting("radius").setValue(Mth.nextInt(random,
                            1, 3));
                    cloud.getSetting("duration").setValue(Mth.nextInt(random,
                            Math.min(getRiftSize() / 2, 30), Math.min(getRiftSize(), 120)));
                    p.addNode(cloud);
                    p.addNode(new FocusEffectFlux());
                    FocusEngine.castFocusPackage(target, p, true);
                    break;
                }
                break;
            }
            case 4: {
                setCollapsing(true);
                break;
            }
        }
        if (didit) {
            setRiftStability(getRiftStability() + ei.cost);
        }
    }

    private void completeCollapse() {
        int qq = (int) Math.sqrt(maxSize);

        if (random.nextInt(100) < qq) {
            ItemStack pearl = new ItemStack(ModItems.PRIMORDIAL_PEARL.get());
            pearl.setDamageValue(4 + random.nextInt(4));
            spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), pearl);
        }

        for (int a = 0; a < qq; a++) {
            spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), new ItemStack(ModItems.VOID_SEED.get()));
        }

        if (level() instanceof ServerLevel serverLevel) {
            PacketHandler.sendToAllTrackingChunk(
                    new PacketFXBlockBamf(getX(), getY(), getZ(), 0, true, true, null),
                    serverLevel, blockPosition());
        } else {
            level().addParticle(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(), getZ(), 0, 0, 0);
        }

        List<LivingEntity> nearby = level().getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(LivingEntity.class), getBoundingBox().inflate(32.0), e -> true);

        switch (getStability()) {
            case VERY_UNSTABLE:
                for (LivingEntity e : nearby) {
                    int duration = (int) ((1.0 - e.distanceToSqr(this) / 32.0) * 120.0);
                    if (duration > 0) {
                        e.addEffect(new MobEffectInstance(ModEffects.FLUX_TAINT, duration * 20, 0));
                    }
                }
            case UNSTABLE:
                for (LivingEntity e : nearby) {
                    int duration = (int) ((1.0 - e.distanceToSqr(this) / 32.0) * 300.0);
                    if (duration > 0) {
                        e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration * 20, 0));
                    }
                }
            case STABLE:
                for (LivingEntity e : nearby) {
                    if (e instanceof Player player) {
                        int warp = (int) ((1.0 - e.distanceToSqr(this) / 32.0) * 25.0);
                        if (warp <= 0) {
                            continue;
                        }
                        ArcaneresearchApi.internalMethods.addWarpToPlayer(player, warp, IPlayerWarp.EnumWarpType.NORMAL);
                        ArcaneresearchApi.internalMethods.addWarpToPlayer(player, warp, IPlayerWarp.EnumWarpType.TEMPORARY);
                    }
                }
                break;
            case VERY_STABLE:
            default:

                break;
        }

        discard();
    }

    @Override
    public void move(MoverType type, Vec3 movement) {

    }

    @Override
    public void push(double x, double y, double z) {

    }

    // TODO_PORT: @Override removed for 26.3 green build (signature mismatch, rework deferred).
    public void setSecondsOnFire(int seconds) {

    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput tag) {
        maxSize = tag.getIntOr("MaxSize", 0);
        setRiftSize(tag.getIntOr("RiftSize", 0));
        setRiftSeed(tag.getIntOr("RiftSeed", 0));
        setRiftStability(tag.getFloatOr("Stability", 0.0F));

        // TODO_PORT: ValueInput.contains gone in 26.3; nested default covers legacy key.
        setCollapsing(tag.getBooleanOr("collapse", tag.getBooleanOr("Collapse", false)));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput tag) {
        tag.putInt("MaxSize", maxSize);
        tag.putInt("RiftSize", getRiftSize());
        tag.putInt("RiftSeed", getRiftSeed());
        tag.putFloat("Stability", getRiftStability());
        tag.putBoolean("collapse", isCollapsing());
    }

    public enum EnumStability {
        VERY_STABLE,
        STABLE,
        UNSTABLE,
        VERY_UNSTABLE
    }

    static class FluxEventEntry implements RandomItemChooser.Item {
        final int event;
        final double weight;
        final int cost;
        final boolean nearTaintAllowed;

        FluxEventEntry(int event, int weight, int cost, boolean nearTaintAllowed) {
            this.event = event;
            this.weight = weight;
            this.cost = cost;
            this.nearTaintAllowed = nearTaintAllowed;
        }

        @Override
        public double getWeight() {
            return weight;
        }
    }
}