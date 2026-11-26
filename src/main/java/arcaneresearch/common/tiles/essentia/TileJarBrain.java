package arcaneresearch.common.tiles.essentia;

import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Block entity for the jar that stores experience and animates a preserved brain.
 *
 * <p>The server owns XP collection and orb absorption; the client owns the visual
 * rotation, bob and ambient sound. Both sides pull nearby orbs through the shared
 * {@link JarBrainRules} helpers so the two views stay consistent.</p>
 */
public class TileJarBrain extends TileArcaneresearch {

    /** Default capacity; see {@link JarBrainRules#DEFAULT_MAX_XP}. */
    public static final int MAX_XP = JarBrainRules.DEFAULT_MAX_XP;

    /** Stored experience points, always clamped to {@link #xpMax} on load and tick. */
    public int xp = 0;
    /** Mutable XP limit; kept as a field so it can be adjusted per tile. */
    public int xpMax = MAX_XP;
    /** Remaining ticks during which orbs cannot be absorbed (set by shaking the jar). */
    public int eatDelay = 0;
    /** Current yaw of the brain, in radians. */
    public float brainRotation = 0;
    /** Yaw used by the renderer to interpolate the previous tick, in radians. */
    public float brainRotationPrev = 0;
    /** Current vertical offset of the brain. */
    public float brainY = 0;
    /** Vertical offset from the previous tick, used for interpolation. */
    public float brainYPrev = 0;

    private float bobPhase = 0;
    private long lastSigh = System.currentTimeMillis() + JarBrainRules.INITIAL_SIGH_DELAY;
    private float targetRotation = 0;
    private int tickCount = 0;

    @Nullable
    private AABB pullBox;
    @Nullable
    private AABB eatBox;

    /**
     * @param type registered block entity type
     * @param pos block position
     * @param state initial block state
     */
    public TileJarBrain(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** Convenience constructor using the registered jar-brain block entity type. */
    public TileJarBrain(BlockPos pos, BlockState state) {
        this(ModBlockEntities.JAR_BRAIN.get(), pos, state);
    }

    /**
     * Server half of the tick: clamps XP, applies the pause after shaking, pulls the
     * closest orb and absorbs every orb touching the jar's centre box.
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, TileJarBrain tile) {
        tile.tickCount++;
        tile.xp = JarBrainRules.clampXp(tile.xp, tile.xpMax);
        boolean mayEat = JarBrainRules.serverMayEat(tile.xp, tile.xpMax, tile.eatDelay);

        if (tile.eatDelay > 0) {
            tile.eatDelay--;
        } else if (mayEat) {
            AABB box = tile.pullBox;
            if (box == null) {
                JarBrainRules.Bounds bounds = JarBrainRules.pullBox(
                        tile.worldPosition.getX(), tile.worldPosition.getY(), tile.worldPosition.getZ());
                box = new AABB(bounds.minX(), bounds.minY(), bounds.minZ(),
                        bounds.maxX(), bounds.maxY(), bounds.maxZ());
                tile.pullBox = box;
            }
            List<ExperienceOrb> orbs = level.getEntitiesOfClass(ExperienceOrb.class, box);
            if (!orbs.isEmpty()) {
                double cx = pos.getX() + 0.5;
                double cy = pos.getY() + 0.5;
                double cz = pos.getZ() + 0.5;

                ExperienceOrb closest = null;
                double best = Double.MAX_VALUE;
                for (ExperienceOrb orb : orbs) {
                    double d = orb.distanceToSqr(cx, cy, cz);
                    if (d < best) {
                        best = d;
                        closest = orb;
                    }
                }

                if (closest != null) {
                    pullOrb(pos, closest);
                }

                AABB eat = tile.eatBox;
                if (eat == null) {
                    JarBrainRules.Bounds bounds = JarBrainRules.eatBox(cx, cy, cz);
                    eat = new AABB(bounds.minX(), bounds.minY(), bounds.minZ(),
                            bounds.maxX(), bounds.maxY(), bounds.maxZ());
                    tile.eatBox = eat;
                }

                boolean ate = false;
                for (ExperienceOrb orb : orbs) {
                    if (eat.intersects(orb.getBoundingBox())) {
                        tile.xp += orb.getValue();
                        // The original uses eo.playSound(...): orb position, entity category.
                        orb.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.1f,
                                (level.random.nextFloat() - level.random.nextFloat()) * 0.2f + 1.0f);
                        orb.discard();
                        ate = true;
                    }
                }
                if (ate) {
                    tile.xp = JarBrainRules.clampXp(tile.xp, tile.xpMax);
                    tile.syncTile(false);
                }
            }
        }
    }

    /**
     * Client half of the tick: pulls orbs locally, follows the nearest player when no
     * orb is nearby, plays the ambient brain sound, and advances the bob animation.
     */
    public static void clientTick(Level level, BlockPos pos, BlockState state, TileJarBrain tile) {
        tile.brainRotationPrev = tile.brainRotation;
        tile.brainYPrev = tile.brainY;

        // Like the original update(), the brain only watches a player (and sighs) when
        // there is no XP orb nearby and the jar is not full. The orb pull runs on both
        // sides in the original, so it is shared with serverTick via pullOrb().
        @Nullable ExperienceOrb orb = tile.xp < tile.xpMax ? tile.getClosestXPOrb() : null;
        boolean orbNearby = orb != null;
        if (orb != null && JarBrainRules.clientMayPull(tile.xp, tile.xpMax, tile.eatDelay, orbNearby)) {
            pullOrb(pos, orb);
        }
        Player watcher = null;
        if (JarBrainRules.clientMayWatch(tile.xp, tile.xpMax, orbNearby)) {
            watcher = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    6.0, e -> e instanceof Player && !e.isSpectator());
            if (watcher != null && tile.lastSigh < System.currentTimeMillis()) {
                level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        ModSounds.BRAIN.get(), SoundSource.AMBIENT, 0.15f,
                        0.8f + level.random.nextFloat() * 0.4f, false);
                tile.lastSigh = JarBrainRules.nextSigh(System.currentTimeMillis(),
                        () -> level.random.nextInt(JarBrainRules.SIGH_JITTER));
            }
        }

        if (watcher != null) {
            double dx = watcher.getX() - (pos.getX() + 0.5);
            double dz = watcher.getZ() - (pos.getZ() + 0.5);
            tile.targetRotation = (float) Math.atan2(dz, dx);
        } else {
            // The original keeps a persistent target angle and lets it drift while idle,
            // so the current rotation smoothly chases it instead of jumping.
            tile.targetRotation = JarBrainRules.idleTarget(tile.targetRotation);
        }

        tile.brainRotation = JarBrainRules.chaseTarget(
                tile.brainRotation, tile.targetRotation, JarBrainRules.ROTATION_SPEED);

        // Original: sin(player.ticksExisted / 14.0F) * 0.03F + 0.03F with a -0.8 offset;
        // the renderer applies the equivalent -0.77 base, so only the frequency matters.
        tile.bobPhase = JarBrainRules.advanceBob(tile.bobPhase);
        tile.brainY = JarBrainRules.bobY(tile.bobPhase);

        // The original decrements eatDelay in the shared update() on both sides.
        if (tile.eatDelay > 0) {
            tile.eatDelay--;
        }
    }

    /** Shared pull from the original update(): blocks 48-69. */
    private static void pullOrb(BlockPos pos, ExperienceOrb orb) {
        JarBrainRules.Pull pull = JarBrainRules.orbPull(
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                orb.getX(), orb.getY(), orb.getZ());
        Vec3 motion = orb.getDeltaMovement();
        orb.setDeltaMovement(motion.x + pull.x(), motion.y + pull.y(), motion.z + pull.z());
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        tag.putInt("XP", xp);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        xp = JarBrainRules.clampXp(tag.getInt("XP"), xpMax);
    }

    /**
     * Finds the nearest experience orb inside the cached enlarged search box.
     *
     * @return the closest orb, or {@code null} when the level is absent or the box is empty
     */
    @Nullable
    private ExperienceOrb getClosestXPOrb() {
        if (level == null) {
            return null;
        }
        AABB box = pullBox;
        if (box == null) {
            JarBrainRules.Bounds bounds = JarBrainRules.pullBox(
                    worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
            box = new AABB(bounds.minX(), bounds.minY(), bounds.minZ(),
                    bounds.maxX(), bounds.maxY(), bounds.maxZ());
            pullBox = box;
        }
        List<ExperienceOrb> ents = level.getEntitiesOfClass(ExperienceOrb.class, box);
        double best = Double.MAX_VALUE;
        ExperienceOrb closest = null;
        double cx = worldPosition.getX() + 0.5;
        double cy = worldPosition.getY() + 0.5;
        double cz = worldPosition.getZ() + 0.5;
        for (ExperienceOrb orb : ents) {
            double d = orb.distanceToSqr(cx, cy, cz);
            if (d < best) {
                best = d;
                closest = orb;
            }
        }
        return closest;
    }

    /**
     * Transfers stored XP directly to a player.
     *
     * @param player receiving player; no transfer happens when null
     * @param all when true, release all XP; otherwise only enough for the next level
     * @return the amount actually transferred
     */
    public int dispenseXP(Player player, boolean all) {
        if (level == null || xp <= 0 || player == null) return 0;
        int toDispense = JarBrainRules.dispenseAmount(xp, player.experienceLevel, all);
        if (toDispense > 0) {
            player.giveExperiencePoints(toDispense);
            xp -= toDispense;
            markDirtyAndSync();
            level.playSound(null, worldPosition, SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.BLOCKS, 0.5f, 1.0f);
        }
        return toDispense;
    }

    /** @return the stored experience points */
    public int getXP() {
        return xp;
    }

    /** @return stored XP as a fraction of {@link #xpMax}, in {@code [0, 1]} */
    public float getFillPercent() {
        return JarBrainRules.fillPercent(xp, xpMax);
    }

    /** @return comparator signal in {@code [0, 15]} derived from the stored XP */
    public int getComparatorOutput() {
        return JarBrainRules.comparatorOutput(xp, xpMax);
    }

    /** @return number of server ticks observed; used by diagnostics and tests */
    public int getTickCount() {
        return tickCount;
    }

    /** @param tickCount new diagnostic tick counter value */
    public void setTickCount(int tickCount) {
        this.tickCount = tickCount;
    }
}