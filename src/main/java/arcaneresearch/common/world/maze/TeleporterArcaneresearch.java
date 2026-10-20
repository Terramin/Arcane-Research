package arcaneresearch.common.world.maze;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.ITeleporter;
import org.slf4j.Logger;
import arcaneresearch.common.blocks.aura.AuraBlocks;
import java.util.function.Function;
public class TeleporterArcaneresearch implements ITeleporter {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int ARRIVAL_CHUNK_X = 6;
    public static final int ARRIVAL_CHUNK_Z = 6;
    public static final int NEAR_RADIUS = 16;
    public static BlockPos arrival() {
        return new BlockPos(ARRIVAL_CHUNK_X * 16 + 8, WorldProviderOuter.MAZE_LEVEL + 1,
                ARRIVAL_CHUNK_Z * 16 + 8);
    }
    public static BlockPos arrivalPortalSlot() {
        BlockPos at = arrival();
        return new BlockPos(at.getX(), at.getY() + 1, at.getZ());
    }
    public static ServerLevel dimension(net.minecraft.server.MinecraftServer server) {
        return server.getLevel(MazeFeature.DIMENSION);
    }
    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw,
                              Function<Boolean, Entity> repositionEntity) {
        if (!(entity instanceof net.minecraft.server.level.ServerPlayer player)
                || entity instanceof net.minecraftforge.common.util.FakePlayer) {
            return entity;
        }
        try {
            entity = repositionEntity.apply(true);
            if (destWorld.dimension().equals(MazeFeature.DIMENSION)) {
                return placeInOuterLands(entity, currentWorld, destWorld, yaw);
            }
            return placeOnReturn(entity, destWorld, yaw);
        } catch (Exception e) {
            LOGGER.error("[Arcaneresearch] placeEntity failed, emergency platform", e);
            BlockPos at = arrival().offset(2, 0, 0);
            moveTo(destWorld, entity, at.getX() + 0.5D, at.getY(), at.getZ() + 0.5D, yaw);
            return entity;
        }
    }
    private static void moveTo(ServerLevel level, Entity entity, double x, double y, double z,
                               float yaw) {
        try {
            entity.moveTo(x, y, z, yaw, entity.getXRot());
            entity.setDeltaMovement(0.0D, 0.0D, 0.0D);
        } catch (Exception ignored) {
        }
        try {
            if (entity instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                level.getChunkSource().move(serverPlayer);
            }
        } catch (Exception e) {
            LOGGER.error("[Arcaneresearch] chunk reticket failed", e);
        }
        try {
            if (entity instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                serverPlayer.connection.teleport(x, y, z, yaw, entity.getXRot());
            }
        } catch (Exception e) {
            LOGGER.error("[Arcaneresearch] client teleport sync failed", e);
        }
    }
    private static Entity placeInOuterLands(Entity entity, ServerLevel currentWorld, ServerLevel destWorld,
                                            float yaw) {
        LOGGER.info("[Arcaneresearch] enter {} -> outer_lands at {}",
                currentWorld.dimension().location(), entity.blockPosition());
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
        BlockPos p = null;
        if (entity instanceof net.minecraft.server.level.ServerPlayer player) {
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
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int nx = near.getX();
        int nz = near.getZ();
        int minY = Math.max(level.getMinBuildHeight(), near.getY() - 8);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, near.getY() + 8);
        double best = -1.0D;
        BlockPos result = null;
        for (int x = nx - radius; x <= nx + radius; x++) {
            for (int z = nz - radius; z <= nz + radius; z++) {
                for (int y = minY; y <= maxY; y++) {
                    cursor.set(x, y, z);
                    if (!level.getBlockState(cursor).is(AuraBlocks.ELDRITCH_PORTAL.get())) {
                        continue;
                    }
                    double dist = (x + 0.5D - nx) * (x + 0.5D - nx)
                            + (z + 0.5D - nz) * (z + 0.5D - nz);
                    if (best < 0.0D || dist < best) {
                        best = dist;
                        result = new BlockPos(x, y, z);
                    }
                }
            }
        }
        return result;
    }
}
