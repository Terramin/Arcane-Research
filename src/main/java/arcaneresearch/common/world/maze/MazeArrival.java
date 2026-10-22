package arcaneresearch.common.world.maze;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.status.ChunkStatus;
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
        long t0 = System.nanoTime();
        MazeFeature.ensureMaze(level);
        LOGGER.info("[Arcaneresearch] ensureMaze done in {}ms, cells={}",
                (System.nanoTime() - t0) / 1_000_000L, MazeHandler.labyrinth.size());
        t0 = System.nanoTime();
        
        
        forceLoad(level, arrivalChunkX(), arrivalChunkZ(), 1);
        buildNeighborhood(level);
        LOGGER.info("[Arcaneresearch] arrival neighborhood built in {}ms",
                (System.nanoTime() - t0) / 1_000_000L);
        if (!isArrivalRoomReady(level)) {
            LOGGER.warn("[Arcaneresearch] arrival room missing after canonical build, retrying once");
            buildNeighborhood(level);
        }
        if (!isArrivalRoomReady(level)) {
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
        int ax = arrivalChunkX();
        int az = arrivalChunkZ();
        buildRoom(level, ax, az);
        buildRoom(level, ax + 1, az);
        buildRoom(level, ax - 1, az);
        buildRoom(level, ax, az + 1);
        buildRoom(level, ax, az - 1);
    }

    public static void buildRoom(ServerLevel level, int cx, int cz) {
        try {
            MazeHandler.generateEldritch(level, level.getRandom(), cx, cz);
        } catch (Exception e) {
            LOGGER.error("[Arcaneresearch] buildRoom failed at {},{}", cx, cz, e);
        }
    }

      
    public static void forceLoad(ServerLevel level, int cx, int cz, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                try {
                    level.getChunk(cx + dx, cz + dz, ChunkStatus.FULL, true);
                } catch (Exception e) {
                    LOGGER.error("[Arcaneresearch] forceLoad failed at {},{}",
                            cx + dx, cz + dz, e);
                }
            }
        }
    }

     


 
    public static boolean isArrivalRoomReady(ServerLevel level) {
        int y = WorldProviderOuter.MAZE_LEVEL;
        int cx = arrivalChunkX() * 16 + 8;
        int cz = arrivalChunkZ() * 16 + 8;
        if (!level.getBlockState(new BlockPos(cx, y, cz)).isSolid()) {
            return false;
        }
        if (!level.getBlockState(new BlockPos(cx + 2, y, cz)).isSolid()) {
            return false;
        }
        
        for (int dx = -8; dx <= 8; dx += 16) {
            for (int dz = -8; dz <= 8; dz += 16) {
                if (!level.getBlockState(new BlockPos(cx + dx, y + 2, cz + dz)).isSolid()) {
                    return false;
                }
            }
        }
        return isPortal(level, TeleporterArcaneresearch.arrivalPortalSlot());
    }

     



 
    public static BlockPos findRoomSpawn(ServerLevel level) {
        BlockPos slot = TeleporterArcaneresearch.arrivalPortalSlot();
        int floorY = WorldProviderOuter.MAZE_LEVEL + 1;
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                BlockPos feet = new BlockPos(slot.getX() + dx, floorY, slot.getZ() + dz);
                try {
                    if (!level.getBlockState(feet).isAir()
                            || !level.getBlockState(feet.above()).isAir()
                            || !level.getBlockState(feet.below()).isSolid()
                            || !hasCeiling(level, feet)) {
                        continue;
                    }
                } catch (Exception ignored) {
                    continue;
                }
                double dist = dx * dx + dz * dz;
                if (dist < bestDist) {
                    bestDist = dist;
                    best = feet;
                }
            }
        }
        if (best != null) {
            LOGGER.info("[Arcaneresearch] room spawn at {}", best);
        }
        return best;
    }

      
    public static boolean hasCeiling(ServerLevel level, BlockPos feet) {
        for (int dy = 2; dy <= 5; dy++) {
            if (level.getBlockState(feet.above(dy)).isSolid()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPortal(ServerLevel level, BlockPos pos) {
        try {
            return level.getBlockState(pos).is(AuraBlocks.ELDRITCH_PORTAL.get());
        } catch (Exception e) {
            return false;
        }
    }

     


 
    private static BlockPos fallbackPad(ServerLevel level) {
        BlockPos at = TeleporterArcaneresearch.arrival().offset(2, 0, 0);
        try {
            StructureHelper.set(level, at.below(),
                    StructureHelper.state("arcaneresearch:eldritch_stone_tile"));
            StructureHelper.set(level, at, Blocks.AIR.defaultBlockState());
            StructureHelper.set(level, at.above(), Blocks.AIR.defaultBlockState());
        } catch (Exception ignored) {
        }
        return at;
    }
}
