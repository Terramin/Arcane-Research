package arcaneresearch.common.world.maze;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ITeleporter;
import org.slf4j.Logger;
import arcaneresearch.common.blocks.aura.AuraBlocks;

import java.util.function.Function;

public class TeleporterArcaneresearch implements ITeleporter {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final int ARRIVAL_CHUNK_X = 6;
    public static final int ARRIVAL_CHUNK_Z = 6;
    public static final int NEAR_RADIUS = 16;

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

    public static ServerLevel dimension(MinecraftServer server) {
        if (server == null) {
            return null;
        }
        return server.getLevel(MazeFeature.DIMENSION);
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw,
                              Function<Boolean, Entity> repositionEntity) {
        if (entity == null || destWorld == null || repositionEntity == null) {
            return entity;
        }
        if (!(entity instanceof ServerPlayer) || entity instanceof FakePlayer) {
            return entity;
        }

        Entity repositioned;
        try {
            repositioned = repositionEntity.apply(true);
        } catch (Exception e) {
            LOGGER.error("[Arcaneresearch] repositionEntity failed", e);
            return entity;
        }
        if (repositioned == null) {
            return entity;
        }

        try {
            if (destWorld.dimension().equals(MazeFeature.DIMENSION)) {
                return placeInOuterLands(repositioned, currentWorld, destWorld, yaw);
            }
            return placeOnReturn(repositioned, destWorld, yaw);
        } catch (Exception e) {
            LOGGER.error("[Arcaneresearch] placeEntity failed, emergency platform", e);
            BlockPos at = ARRIVAL_POS.offset(2, 0, 0);
            moveTo(destWorld, repositioned, at.getX() + 0.5D, at.getY(), at.getZ() + 0.5D, yaw);
            return repositioned;
        }
    }

    private static void moveTo(ServerLevel level, Entity entity, double x, double y, double z, float yaw) {
        if (level == null || entity == null) {
            return;
        }
        try {
            entity.moveTo(x, y, z, yaw, entity.getXRot());
            entity.setDeltaMovement(0.0D, 0.0D, 0.0D);
        } catch (Exception ignored) {
        }
        if (entity instanceof ServerPlayer sp) {
            try {
                level.getChunkSource().move(sp);
            } catch (Exception e) {
                LOGGER.error("[Arcaneresearch] chunk reticket failed", e);
            }
            try {
                sp.connection.teleport(x, y, z, yaw, entity.getXRot());
            } catch (Exception e) {
                LOGGER.error("[Arcaneresearch] client teleport sync failed", e);
            }
        }
    }

    private static Entity placeInOuterLands(Entity entity, ServerLevel currentWorld, ServerLevel destWorld,
                                            float yaw) {
        if (currentWorld != null) {
            LOGGER.info("[Arcaneresearch] enter {} -> outer_lands at {}",
                    currentWorld.dimension().location(), entity.blockPosition());
        }
        long t0 = System.nanoTime();
        BlockPos spawn = MazeArrival.ensureArrivalRoom(destWorld);
        LOGGER.info("[Arcaneresearch] arrival ensured in {}ms, spawn {}",
                (System.nanoTime() - t0) / 1_000_000L, spawn);
        moveTo(destWorld, entity, spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D, yaw);
        LOGGER.info("[Arcaneresearch] spawned at {} below={} feet={} head={}",
                spawn, destWorld.getBlockState(spawn.below()), destWorld.getBlockState(spawn),
                destWorld.getBlockState(spawn.above()));
        return entity;
    }

    private static Entity placeOnReturn(Entity entity, ServerLevel destWorld, float yaw) {
        if (destWorld == null) {
            return entity;
        }
        BlockPos p = null;
        if (entity instanceof ServerPlayer player) {
            try {
                BlockPos respawn = player.getRespawnPosition();
                if (respawn != null) {
                    p = respawn.immutable();
                }
            } catch (Exception ignored) {
            }
        }
        if (p == null) {
            p = destWorld.getSharedSpawnPos();
        }
        p = freeSpot(destWorld, p);
        LOGGER.info("[Arcaneresearch] return to personal spawn {}", p);
        moveTo(destWorld, entity, p.getX() + 0.5D, p.getY(), p.getZ() + 0.5D, yaw);
        return entity;
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
        int minY = Math.max(level.getMinBuildHeight(), near.getY() - 8);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, near.getY() + 8);
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