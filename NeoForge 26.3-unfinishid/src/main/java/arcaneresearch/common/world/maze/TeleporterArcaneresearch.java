package arcaneresearch.common.world.maze;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import arcaneresearch.common.blocks.aura.AuraBlocks;

/**
 * Teleporter into/out of the Outer Lands, ported from TC4 TeleporterArcaneresearch
 * (arcaneresearch.common.lib.world.dim.TeleporterArcaneresearch).
 *
 * Специально БЕЗ широкого поиска портала по миру: перебор 257x257x61 клеток
 * вешал серверный поток на десятки секунд. Вместо этого:
 * - вход во Внешние земли всегда идёт в детерминированную портал-комнату
 *   (чанк прибытия 6,6 — туда MazeGenerator всегда кладёт комнату feature 1);
 * - комнату строит ТОЛЬКО MazeArrival.ensureArrivalRoom (канонический
 *   портал-зал + комнаты соседей, идемпотентно — одна и та же комната
 *   при каждом заходе);
 * - точка возврата в верхний мир запоминается в persistent-данных сущности
 *   в момент входа и используется при выходе;
 * - запасные варианты — маленький локальный поиск (радиус 16) и спавн мира.
 *
 * Точка прибытия — центр лабиринта MazeThread (портал-комната GenPortal),
 * высота — WorldProviderOuter.MAZE_LEVEL.
 */
public class TeleporterArcaneresearch {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int ARRIVAL_CHUNK_X = 6;
    public static final int ARRIVAL_CHUNK_Z = 6;
    /** Малый радиус запасного поиска — дёшево и не вешает поток. */
    public static final int NEAR_RADIUS = 16;

    private TeleporterArcaneresearch() {
    }

    public static BlockPos arrival() {
        return new BlockPos(ARRIVAL_CHUNK_X * 16 + 8, WorldProviderOuter.MAZE_LEVEL + 1,
                ARRIVAL_CHUNK_Z * 16 + 8);
    }

    /** Слот портала в портал-комнате: центр чанка прибытия, y = FLOOR+2. */
    public static BlockPos arrivalPortalSlot() {
        BlockPos at = arrival();
        return new BlockPos(at.getX(), at.getY() + 1, at.getZ());
    }

    public static ServerLevel dimension(net.minecraft.server.MinecraftServer server) {
        return server.getLevel(MazeFeature.DIMENSION);
    }

    // NEOFORGE_PORT: ITeleporter/changeDimension removed in 26.x; dimension travel is
    // ServerPlayer.teleport(TeleportTransition) (vanilla handles registration, chunk
    // tickets and client sync). The TC4 arrival/return logic below only computes the
    // TARGET position now; callers pass it into a TeleportTransition.
    // Портал — только для настоящих игроков: мобы, предметы, снаряды
    // и фейковые игроки через него не проходят.
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
            // Никогда не роняем телепорт исключением и никогда не оставляем
            // игрока в пустоте: уводим на аварийную точку у комнаты прибытия.
            LOGGER.error("[Arcaneresearch] teleport failed, emergency platform", e);
            BlockPos at = arrival().offset(2, 0, 0);
            player.teleport(new TeleportTransition(destWorld,
                    new Vec3(at.getX() + 0.5D, at.getY(), at.getZ() + 0.5D), Vec3.ZERO,
                    yaw, player.getXRot(), TeleportTransition.PLACE_PORTAL_TICKET));
            return true;
        }
    }

    /** Вход во Внешние земли: единая комната прибытия (возврат — на спавн). */
    private static BlockPos placeInOuterLands(ServerPlayer player, ServerLevel destWorld) {
        // NEOFORGE_PORT: ResourceKey.location() -> identifier() in 26.x.
        LOGGER.info("[Arcaneresearch] enter outer_lands from {} at {}",
                player.level().dimension().identifier(), player.blockPosition());
        // Комнату строит ТОЛЬКО MazeArrival.ensureArrivalRoom: канонический
        // портал-зал TC4 + комнаты соседей, идемпотентно. Здесь больше нет
        // своей геометрии (площадок/аварийных коробок): они и давали
        // «другую комнату» при повторных заходах и запечатывали коридоры.
        long t0 = System.nanoTime();
        BlockPos spawn = MazeArrival.ensureArrivalRoom(destWorld);
        LOGGER.info("[Arcaneresearch] arrival ensured in {}ms, spawn {}",
                (System.nanoTime() - t0) / 1_000_000L, spawn);
        LOGGER.info("[Arcaneresearch] spawned at {} below={} feet={} head={}",
                spawn, destWorld.getBlockState(spawn.below()), destWorld.getBlockState(spawn),
                destWorld.getBlockState(spawn.above()));
        return spawn;
    }

    /**
     * Выход из Внешних земель — на персональную точку спавна игрока
     * (кровать; нет точки — спавн мира). Точка правится freeSpot вверх,
     * если занята.
     */
    private static BlockPos placeOnReturn(ServerPlayer player, ServerLevel destWorld) {
        BlockPos p = null;
        try {
            // NEOFORGE_PORT: player.getRespawnPosition() -> getRespawnConfig().respawnData().
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
            // NEOFORGE_PORT: ServerLevel.getSharedSpawnPos gone -> level respawn data pos.
            p = destWorld.getRespawnData().pos();
        }
        p = freeSpot(destWorld, p);
        LOGGER.info("[Arcaneresearch] return to personal spawn {}", p);
        return p;
    }

    /** Подняться вверх, пока не будет свободно (не более 6 блоков). */
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

    /** Дёшевый локальный поиск портала: куб 2r+1, не более ±8 по Y. */
    public static BlockPos findPortalNear(ServerLevel level, BlockPos near, int radius) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int nx = near.getX();
        int nz = near.getZ();
        // NEOFORGE_PORT: getMin/MaxBuildHeight gone -> dimensionType minY/height.
        int minY = Math.max(level.dimensionType().minY(), near.getY() - 8);
        int maxY = Math.min(level.dimensionType().minY() + level.dimensionType().height() - 1, near.getY() + 8);
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
