package arcaneresearch.common.world.maze;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import arcaneresearch.common.blocks.aura.AuraBlocks;

 
















 
public class TeleporterArcaneresearch {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int ARRIVAL_CHUNK_X = 6;
    public static final int ARRIVAL_CHUNK_Z = 6;
      
    public static final int NEAR_RADIUS = 16;

    private TeleporterArcaneresearch() {
    }

    private static final BlockPos ARRIVAL_POS =
            new BlockPos(ARRIVAL_CHUNK_X * 16 + 8, WorldProviderOuter.MAZE_LEVEL + 1,
                    ARRIVAL_CHUNK_Z * 16 + 8);
    private static final BlockPos ARRIVAL_PORTAL_SLOT = ARRIVAL_POS.above();

    public static BlockPos arrival() {
        return ARRIVAL_POS;
    }

    public static BlockPos arrivalPortalSlot() {
        return ARRIVAL_PORTAL_SLOT;
    }

    public static ServerLevel dimension(net.minecraft.server.MinecraftServer server) {
        if (server == null) {
            return null;
        }
        return server.getLevel(MazeFeature.DIMENSION);
    }

    
    
    
    
    
    
    public static boolean teleportPlayer(ServerPlayer player, ServerLevel destWorld) {
        if (player instanceof net.neoforged.neoforge.common.util.FakePlayer) {
            return false;
        }
        float yaw = player.getYRot();
        try {
            BlockPos target;
            if (destWorld.dimension().equals(MazeFeature.DIMENSION)) {
                target = placeInOuterLands(player, destWorld);
            } else {
                target = placeOnReturn(player, destWorld);
            }
            Vec3 at = new Vec3(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D);
            player.teleport(new TeleportTransition(destWorld, at, Vec3.ZERO, yaw, player.getXRot(),
                    TeleportTransition.PLACE_PORTAL_TICKET));
            return true;
        } catch (Exception e) {
            
            
            LOGGER.error("[Arcaneresearch] teleport failed, emergency platform", e);
            BlockPos at = arrival().offset(2, 0, 0);
            player.teleport(new TeleportTransition(destWorld,
                    new Vec3(at.getX() + 0.5D, at.getY(), at.getZ() + 0.5D), Vec3.ZERO,
                    yaw, player.getXRot(), TeleportTransition.PLACE_PORTAL_TICKET));
            return true;
        }
    }

      
    private static BlockPos placeInOuterLands(ServerPlayer player, ServerLevel destWorld) {
        
        LOGGER.info("[Arcaneresearch] enter outer_lands from {} at {}",
                player.level().dimension().identifier(), player.blockPosition());
        
        
        
        
        long t0 = System.nanoTime();
        BlockPos spawn = MazeArrival.ensureArrivalRoom(destWorld);
        LOGGER.info("[Arcaneresearch] arrival ensured in {}ms, spawn {}",
                (System.nanoTime() - t0) / 1_000_000L, spawn);
        LOGGER.info("[Arcaneresearch] spawned at {} below={} feet={} head={}",
                spawn, destWorld.getBlockState(spawn.below()), destWorld.getBlockState(spawn),
                destWorld.getBlockState(spawn.above()));
        return spawn;
    }

     



 
    private static BlockPos placeOnReturn(ServerPlayer player, ServerLevel destWorld) {
        BlockPos p = null;
        try {
            
            ServerPlayer.RespawnConfig cfg = player.getRespawnConfig();
            if (cfg != null && cfg.respawnData() != null && cfg.respawnData().globalPos() != null) {
                BlockPos rp = cfg.respawnData().globalPos().pos();
                if (rp != null) {
                    p = rp.immutable();
                }
            }
        } catch (Exception ignored) {
        }
        if (p == null) {
            
            p = destWorld.getRespawnData().pos();
        }
        p = freeSpot(destWorld, p);
        LOGGER.info("[Arcaneresearch] return to personal spawn {}", p);
        return p;
    }

      
    private static BlockPos freeSpot(ServerLevel level, BlockPos pos) {
        if (level == null || pos == null) {
            return pos;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(
                pos.getX(), pos.getY(), pos.getZ());
        for (int i = 0; i < 6; i++) {
            if (level.getBlockState(cursor).isAir()
                    && level.getBlockState(cursor.move(0, 1, 0)).isAir()) {
                cursor.move(0, -1, 0);
                return cursor.immutable();
            }
            cursor.move(0, 1, 0);
        }
        return pos;
    }

      
    public static BlockPos findPortalNear(ServerLevel level, BlockPos near, int radius) {
        if (level == null || near == null) {
            return null;
        }
        Block portalBlock = AuraBlocks.ELDRITCH_PORTAL.get();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int nx = near.getX();
        int nz = near.getZ();

        int minY = Math.max(level.dimensionType().minY(), near.getY() - 8);
        int maxY = Math.min(level.dimensionType().minY() + level.dimensionType().height() - 1, near.getY() + 8);
        if (minY > maxY) {
            return null;
        }
        double best = -1.0D;
        BlockPos result = null;
        for (int x = nx - radius; x <= nx + radius; x++) {
            double dx = x + 0.5D - nx;
            double dx2 = dx * dx;
            for (int z = nz - radius; z <= nz + radius; z++) {
                double dz = z + 0.5D - nz;
                double horizDist = dx2 + dz * dz;
                if (best >= 0.0D && horizDist >= best) {
                    continue;
                }
                for (int y = minY; y <= maxY; y++) {
                    cursor.set(x, y, z);
                    if (!level.getBlockState(cursor).is(portalBlock)) {
                        continue;
                    }
                    if (best < 0.0D || horizDist < best) {
                        best = horizDist;
                        result = new BlockPos(x, y, z);
                    }
                }
            }
        }
        return result;
    }
}
