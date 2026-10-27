package arcaneresearch.common.world.maze;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.slf4j.Logger;
import arcaneresearch.common.blocks.aura.AuraBlocks;
import arcaneresearch.common.world.structures.StructureHelper;

public final class MazeArrival {
    private static final Logger LOGGER = LogUtils.getLogger();

    private MazeArrival() {
    }

    public static int arrivalChunkX() {
        return TeleporterArcaneresearch.ARRIVAL_CHUNK_X;
    }

    public static int arrivalChunkZ() {
        return TeleporterArcaneresearch.ARRIVAL_CHUNK_Z;
    }

    public static BlockPos ensureArrivalRoom(ServerLevel level) {
        if (level == null) {
            return BlockPos.ZERO;
        }
        long t0 = System.nanoTime();
        MazeFeature.ensureMaze(level);
        var labyrinth = MazeHandler.labyrinth;
        LOGGER.info("[Arcaneresearch] ensureMaze done in {}ms, cells={}",
                (System.nanoTime() - t0) / 1_000_000L, labyrinth == null ? 0 : labyrinth.size());
        t0 = System.nanoTime();
        forceLoad(level, arrivalChunkX(), arrivalChunkZ(), 1);
        buildNeighborhood(level);
        LOGGER.info("[Arcaneresearch] arrival neighborhood built in {}ms",
                (System.nanoTime() - t0) / 1_000_000L);

        boolean ready = isArrivalRoomReady(level);
        if (!ready) {
            LOGGER.warn("[Arcaneresearch] arrival room missing after canonical build, retrying once");
            buildNeighborhood(level);
            ready = isArrivalRoomReady(level);
        }
        if (!ready) {
            LOGGER.error("[Arcaneresearch] arrival room FAILED, using minimal pad spawn");
            return fallbackPad(level);
        }
        try {
            MazeHandler.saveMaze(level);
        } catch (Exception e) {
            LOGGER.warn("[Arcaneresearch] saveMaze after arrival failed", e);
        }
        BlockPos spawn = findRoomSpawn(level);
        if (spawn == null) {
            LOGGER.warn("[Arcaneresearch] no free spot in arrival room, using pad spawn");
            return fallbackPad(level);
        }
        LOGGER.info("[Arcaneresearch] arrival spawn {}", spawn);
        return spawn;
    }

    public static void buildNeighborhood(ServerLevel level) {
        if (level == null) {
            return;
        }
        int ax = arrivalChunkX();
        int az = arrivalChunkZ();
        buildRoom(level, ax, az);
        buildRoom(level, ax + 1, az);
        buildRoom(level, ax - 1, az);
        buildRoom(level, ax, az + 1);
        buildRoom(level, ax, az - 1);
    }

    public static void buildRoom(ServerLevel level, int cx, int cz) {
        if (level == null) {
            return;
        }
        try {
            MazeHandler.generateEldritch(level, level.random, cx, cz);
        } catch (Exception e) {
            LOGGER.error("[Arcaneresearch] buildRoom failed at {},{}", cx, cz, e);
        }
    }

    public static void forceLoad(ServerLevel level, int cx, int cz, int radius) {
        if (level == null) {
            return;
        }
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int tx = cx + dx;
                int tz = cz + dz;
                try {
                    level.getChunk(tx, tz, ChunkStatus.FULL, true);
                } catch (Exception e) {
                    LOGGER.error("[Arcaneresearch] forceLoad failed at {},{}", tx, tz, e);
                }
            }
        }
    }

    public static boolean isArrivalRoomReady(ServerLevel level) {
        if (level == null) {
            return false;
        }
        int y = WorldProviderOuter.MAZE_LEVEL;
        int cx = arrivalChunkX() * 16 + 8;
        int cz = arrivalChunkZ() * 16 + 8;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        cursor.set(cx, y, cz);
        if (!level.getBlockState(cursor).canOcclude()) {
            return false;
        }
        cursor.set(cx + 2, y, cz);
        if (!level.getBlockState(cursor).canOcclude()) {
            return false;
        }
        for (int dx = -8; dx <= 8; dx += 16) {
            for (int dz = -8; dz <= 8; dz += 16) {
                cursor.set(cx + dx, y + 2, cz + dz);
                if (!level.getBlockState(cursor).canOcclude()) {
                    return false;
                }
            }
        }
        return isPortal(level, TeleporterArcaneresearch.arrivalPortalSlot());
    }

    public static BlockPos findRoomSpawn(ServerLevel level) {
        if (level == null) {
            return null;
        }
        BlockPos slot = TeleporterArcaneresearch.arrivalPortalSlot();
        int floorY = WorldProviderOuter.MAZE_LEVEL + 1;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                int fx = slot.getX() + dx;
                int fz = slot.getZ() + dz;
                try {
                    cursor.set(fx, floorY, fz);
                    if (!level.getBlockState(cursor).isAir()) {
                        continue;
                    }
                    cursor.set(fx, floorY + 1, fz);
                    if (!level.getBlockState(cursor).isAir()) {
                        continue;
                    }
                    cursor.set(fx, floorY - 1, fz);
                    if (!level.getBlockState(cursor).canOcclude()) {
                        continue;
                    }
                    if (!hasCeiling(level, fx, floorY, fz, cursor)) {
                        continue;
                    }
                } catch (Exception ignored) {
                    continue;
                }
                double dist = dx * dx + dz * dz;
                if (dist < bestDist) {
                    bestDist = dist;
                    best = new BlockPos(fx, floorY, fz);
                }
            }
        }
        if (best != null) {
            LOGGER.info("[Arcaneresearch] room spawn at {}", best);
        }
        return best;
    }

    public static boolean hasCeiling(ServerLevel level, BlockPos feet) {
        if (level == null || feet == null) {
            return false;
        }
        return hasCeiling(level, feet.getX(), feet.getY(), feet.getZ(),
                new BlockPos.MutableBlockPos());
    }

    private static boolean hasCeiling(ServerLevel level, int fx, int fy, int fz,
                                      BlockPos.MutableBlockPos cursor) {
        for (int dy = 2; dy <= 5; dy++) {
            cursor.set(fx, fy + dy, fz);
            if (level.getBlockState(cursor).canOcclude()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPortal(ServerLevel level, BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }
        var portal = AuraBlocks.ELDRITCH_PORTAL.get();
        try {
            return level.getBlockState(pos).is(portal);
        } catch (Exception e) {
            return false;
        }
    }

    private static BlockPos fallbackPad(ServerLevel level) {
        BlockPos at = TeleporterArcaneresearch.arrival().offset(2, 0, 0);
        if (level == null) {
            return at;
        }
        try {
            BlockState pad = StructureHelper.state("arcaneresearch:eldritch_stone_tile");
            StructureHelper.set(level, at.below(), pad);
            StructureHelper.set(level, at, Blocks.AIR.defaultBlockState());
            StructureHelper.set(level, at.above(), Blocks.AIR.defaultBlockState());
        } catch (Exception ignored) {
        }
        return at;
    }
}