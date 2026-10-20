package arcaneresearch.common.tiles.essentia;

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
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;

import javax.annotation.Nullable;
import java.util.List;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileJarBrain extends TileArcaneresearch {

    public static final int MAX_XP = 2000;

    public int xp = 0;

    public int eatDelay = 0;

    public float brainRotation = 0;
    public float brainRotationPrev = 0;
    public float brainY = 0;
    public float brainYPrev = 0;
    private float bobPhase = 0;
    private long lastSigh = 0;

    private int tickCount = 0;

    public TileJarBrain(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileJarBrain(BlockPos pos, BlockState state) {
        this(ModBlockEntities.JAR_BRAIN.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileJarBrain tile) {
        tile.tickCount++;

        if (tile.xp > MAX_XP) {
            tile.xp = MAX_XP;
        }
        if (tile.xp < MAX_XP) {
            ExperienceOrb closest = tile.getClosestXPOrb();
            if (closest != null && tile.eatDelay == 0) {
                double dx = (pos.getX() + 0.5 - closest.getX()) / 25.0;
                double dy = (pos.getY() + 0.5 - closest.getY()) / 25.0;
                double dz = (pos.getZ() + 0.5 - closest.getZ()) / 25.0;
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                double pull = 1.0 - dist;
                if (pull > 0.0) {
                    pull *= pull;
                    net.minecraft.world.phys.Vec3 motion = closest.getDeltaMovement();
                    closest.setDeltaMovement(
                            motion.x + dx / dist * pull * 0.3,
                            motion.y + dy / dist * pull * 0.5,
                            motion.z + dz / dist * pull * 0.3);
                }
            }
        }

        if (tile.eatDelay > 0) {
            tile.eatDelay--;
        } else if (tile.xp < MAX_XP) {

            List<ExperienceOrb> ents = level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(ExperienceOrb.class), new AABB(
                    pos.getX() - 0.1, pos.getY() - 0.1, pos.getZ() - 0.1,
                    pos.getX() + 1.1, pos.getY() + 1.1, pos.getZ() + 1.1), e -> true);
            if (!ents.isEmpty()) {
                for (ExperienceOrb orb : ents) {
                    tile.xp += orb.getValue();
                    level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP,
                            SoundSource.BLOCKS, 0.1f,
                            (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2f + 1.0f);
                    orb.discard();
                }
                if (tile.xp > MAX_XP) {
                    tile.xp = MAX_XP;
                }
                tile.syncTile(false);
                tile.setChanged();
            }
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TileJarBrain tile) {

        tile.brainRotationPrev = tile.brainRotation;
        tile.brainYPrev = tile.brainY;

        Player watcher = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                6.0, e -> e instanceof Player);
        if (watcher != null) {
            double dx = watcher.getX() - (pos.getX() + 0.5);
            double dz = watcher.getZ() - (pos.getZ() + 0.5);
            float target = (float) Math.atan2(dz, dx);
            float delta = target - tile.brainRotation;
            while (delta >= Math.PI) delta -= Math.PI * 2;
            while (delta < -Math.PI) delta += Math.PI * 2;
            tile.brainRotation += delta * 0.04f;
            long now = System.currentTimeMillis();
            if (tile.lastSigh < now) {
                level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        arcaneresearch.init.ModSounds.BRAIN.get(), SoundSource.AMBIENT, 0.15f,
                        0.8f + level.getRandom().nextFloat() * 0.4f, false);
                tile.lastSigh = now + 5000L + level.getRandom().nextInt(25000);
            }
        } else {
            tile.brainRotation += 0.01f;
        }

        tile.bobPhase += 0.05f;
        tile.brainY = (float) Math.sin(tile.bobPhase) * 0.03f;
    }

    @Override
    protected void writeSyncNBT(ValueOutput tag) {
        super.writeSyncNBT(tag);
        tag.putInt("XP", xp);
    }

    @Override
    protected void readSyncNBT(ValueInput tag) {
        super.readSyncNBT(tag);
        xp = tag.getIntOr("XP", 0);
    }

    @Nullable
    private ExperienceOrb getClosestXPOrb() {
        if (level == null) {
            return null;
        }
        AABB box = new AABB(worldPosition).inflate(8.0);
        List<ExperienceOrb> ents = level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(ExperienceOrb.class), box, e -> true);
        double best = Double.MAX_VALUE;
        ExperienceOrb closest = null;
        for (ExperienceOrb orb : ents) {
            double d = orb.distanceToSqr(worldPosition.getX() + 0.5,
                    worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);
            if (d < best) {
                best = d;
                closest = orb;
            }
        }
        return closest;
    }

    public int dispenseXP(Player player, boolean all) {
        if (level == null || xp <= 0) return 0;

        int toDispense;
        if (all) {
            toDispense = xp;
        } else {

            int currentLevel = player.experienceLevel;
            int xpForNextLevel = getXpNeededForLevel(currentLevel + 1) - getXpNeededForLevel(currentLevel);
            toDispense = Math.min(xp, xpForNextLevel);
        }

        if (toDispense > 0) {
            player.giveExperiencePoints(toDispense);
            xp -= toDispense;
            markDirtyAndSync();

            level.playSound(null, worldPosition, SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.BLOCKS, 0.5f, 1.0f);
        }

        return toDispense;
    }

    private int getXpNeededForLevel(int level) {
        if (level <= 16) {
            return level * level + 6 * level;
        } else if (level <= 31) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        }
    }

    public int getXP() {
        return xp;
    }

    public float getFillPercent() {
        return (float) xp / MAX_XP;
    }

    public int getComparatorOutput() {
        if (xp == 0) return 0;
        return 1 + (int) ((xp / (float) MAX_XP) * 14);
    }
}
