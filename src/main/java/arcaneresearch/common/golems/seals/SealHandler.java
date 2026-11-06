package arcaneresearch.common.golems.seals;

import arcaneresearch.api.golems.seals.ISeal;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.api.golems.seals.SealPos;
import arcaneresearch.api.golems.tasks.Task;
import arcaneresearch.common.golems.tasks.TaskHandler;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.misc.PacketSealToClient;
import arcaneresearch.common.world.aura.AuraHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SealHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SealHandler.class);
    private static final int SEAL_RECHECK_PERIOD = 20;

    public static LinkedHashMap<String, ISeal> types = new LinkedHashMap<>();
    public static ConcurrentHashMap<String, ConcurrentHashMap<SealPos, SealEntity>> sealEntities = new ConcurrentHashMap<>();

    private static String getDimKey(ResourceKey<Level> dim) {
        return dim.location().toString();
    }

    private static ConcurrentHashMap<SealPos, SealEntity> getOrCreateDimMap(ResourceKey<Level> dim) {
        return sealEntities.computeIfAbsent(getDimKey(dim), k -> new ConcurrentHashMap<>());
    }

    public static void registerDefaultSeals() {
        LOGGER.info("Registering Arcaneresearch seals...");
        registerSeal(new SealPickup());
        registerSeal(new SealEmpty());
        registerSeal(new SealFill());
        registerSeal(new SealProvide());
        registerSeal(new SealGuard());
        registerSeal(new SealButcher());
        registerSeal(new SealHarvest());
        registerSeal(new SealLumber());
        registerSeal(new SealBreaker());
        registerSeal(new SealBreakerAdvanced());
        registerSeal(new SealPickupAdvanced());
        registerSeal(new SealEmptyAdvanced());
        registerSeal(new SealFillAdvanced());
        registerSeal(new SealGuardAdvanced());
        registerSeal(new SealStock());
        registerSeal(new SealUse());
        LOGGER.info("Registered {} seals", types.size());
    }

    public static void registerSeal(ISeal seal) {
        if (seal == null) {
            LOGGER.error("Attempting to register null Seal. Ignoring.");
            return;
        }
        if (types.containsKey(seal.getKey())) {
            LOGGER.error("Attempting to register Seal [{}] twice. Ignoring.", seal.getKey());
            return;
        }
        types.put(seal.getKey(), seal);
    }

    public static String[] getRegisteredSeals() {
        return types.keySet().toArray(new String[0]);
    }

    public static ISeal getSeal(String key) {
        return types.get(key);
    }

    /** Snapshot list of seals whose positions are within {@code range} of {@code source}. Local to the caller and only iterated, so a plain ArrayList suffices. */
    public static List<SealEntity> getSealsInRange(Level level, BlockPos source, int range) {
        ConcurrentHashMap<SealPos, SealEntity> dimSeals =
                sealEntities.get(getDimKey(level.dimension()));
        List<SealEntity> out = new ArrayList<>();
        if (dimSeals == null || dimSeals.isEmpty()) {
            return out;
        }
        long rangeSq = (long) range * range;
        for (SealEntity se : dimSeals.values()) {
            SealPos sp = se.getSealPos();
            if (sp != null && se.getSeal() != null && sp.pos.distSqr(source) <= rangeSq) {
                out.add(se);
            }
        }
        return out;
    }

    public static List<SealEntity> getSealsInChunk(Level level, ChunkPos chunk) {
        ConcurrentHashMap<SealPos, SealEntity> dimSeals =
                sealEntities.get(getDimKey(level.dimension()));
        List<SealEntity> out = new ArrayList<>();
        if (dimSeals == null || dimSeals.isEmpty()) {
            return out;
        }
        for (SealEntity se : dimSeals.values()) {
            SealPos sp = se.getSealPos();
            if (sp == null || se.getSeal() == null) {
                continue;
            }
            if (new ChunkPos(sp.pos).equals(chunk)) {
                out.add(se);
            }
        }
        return out;
    }

    public static ISealEntity getSealEntity(ResourceKey<Level> dim, SealPos pos) {
        if (dim == null || pos == null) {
            return null;
        }
        ConcurrentHashMap<SealPos, SealEntity> dimSeals = sealEntities.get(getDimKey(dim));
        return dimSeals == null ? null : dimSeals.get(pos);
    }

    public static boolean addSealEntity(Level level, BlockPos pos, Direction face, ISeal seal, Player player) {
        if (level == null || pos == null || face == null || seal == null || player == null) {
            return false;
        }
        ConcurrentHashMap<SealPos, SealEntity> dimSeals = getOrCreateDimMap(level.dimension());
        SealPos sealPos = new SealPos(pos, face);
        SealEntity sealEntity = new SealEntity(level, sealPos, seal);
        sealEntity.setOwner(player.getUUID().toString());
        if (dimSeals.putIfAbsent(sealPos, sealEntity) != null) {
            return false;
        }
        if (!level.isClientSide) {
            sealEntity.syncToClient(level);
            markChunkAsDirty(level.dimension(), pos);
            forceChunkSave(level, pos);
        }
        return true;
    }

    public static boolean addSealEntity(Level level, SealEntity seal) {
        if (level == null || seal == null) {
            return false;
        }
        SealPos sealPos = seal.getSealPos();
        if (sealPos == null) {
            return false;
        }
        ConcurrentHashMap<SealPos, SealEntity> dimSeals = getOrCreateDimMap(level.dimension());
        if (dimSeals.putIfAbsent(sealPos, seal) != null) {
            return false;
        }
        if (!level.isClientSide) {
            seal.syncToClient(level);
            markChunkAsDirty(level.dimension(), sealPos.pos);
            forceChunkSave(level, sealPos.pos);
        }
        return true;
    }

    private static void forceChunkSave(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        try {
            LevelChunk chunk = serverLevel.getChunkAt(pos);
            if (chunk != null) {
                chunk.setUnsaved(true);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to force chunk save at {}", pos, e);
        }
    }

    public static void removeSealEntity(Level level, SealPos pos, boolean quiet) {
        if (level == null || pos == null || pos.pos == null || pos.face == null) {
            return;
        }
        ConcurrentHashMap<SealPos, SealEntity> dimSeals = getOrCreateDimMap(level.dimension());
        SealEntity seal = dimSeals.remove(pos);
        if (seal == null) {
            return;
        }
        ISeal sealLogic = seal.getSeal();

        if (!level.isClientSide && sealLogic != null) {
            try {
                sealLogic.onRemoval(level, pos.pos, pos.face);
            } catch (Exception e) {
                LOGGER.warn("onRemoval failed for seal at {}", pos.pos, e);
            }
        }

        if (!quiet && sealLogic != null && !level.isClientSide) {
            ItemStack sealStack = ItemSealPlacer.getSealStack(sealLogic.getKey());
            if (!sealStack.isEmpty()) {
                double x = pos.pos.getX() + 0.5 + pos.face.getStepX() / 1.7;
                double y = pos.pos.getY() + 0.5 + pos.face.getStepY() / 1.7;
                double z = pos.pos.getZ() + 0.5 + pos.face.getStepZ() / 1.7;
                level.addFreshEntity(new ItemEntity(level, x, y, z, sealStack));
            }
        }

        try {
            ConcurrentHashMap<Integer, Task> tasks = TaskHandler.getTasks(level.dimension());
            for (Task task : tasks.values()) {
                if (task != null && pos.equals(task.getSealPos())) {
                    task.setSuspended(true);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to suspend tasks for removed seal at {}", pos.pos, e);
        }

        if (!level.isClientSide && !quiet) {
            try {
                SealEntity removedSeal = new SealEntity(level, pos, null);
                PacketHandler.sendToDimension(new PacketSealToClient(removedSeal), level.dimension());
            } catch (Exception e) {
                LOGGER.warn("Failed to sync removed seal at {}", pos.pos, e);
            }
            try {
                markChunkAsDirty(level.dimension(), pos.pos);
                forceChunkSave(level, pos.pos);
            } catch (Exception e) {
                LOGGER.warn("Failed to mark chunk dirty for removed seal at {}", pos.pos, e);
            }
        }
    }

    public static void tickSealEntities(Level level) {
        ConcurrentHashMap<SealPos, SealEntity> dimSeals = getOrCreateDimMap(level.dimension());
        long gameTime = level.getGameTime();
        for (SealEntity sealEntity : dimSeals.values()) {
            if (!level.isLoaded(sealEntity.sealPos.pos)) {
                continue;
            }
            ISeal logic = sealEntity.getSeal();
            if (logic == null) {
                continue;
            }
            try {
                if (gameTime % SEAL_RECHECK_PERIOD == 0
                        && !logic.canPlaceAt(level, sealEntity.sealPos.pos, sealEntity.sealPos.face)) {
                    removeSealEntity(level, sealEntity.sealPos, false);
                    continue;
                }
                sealEntity.tickSealEntity(level);
            } catch (Exception e) {
                LOGGER.error("Error ticking seal at {}, removing", sealEntity.sealPos.pos, e);
                removeSealEntity(level, sealEntity.sealPos, false);
            }
        }
    }

    public static void markChunkAsDirty(ResourceKey<Level> dim, BlockPos pos) {
        AuraHandler.markChunkDirty(dim, new ChunkPos(pos));
    }

    public static void syncAllSealsToPlayer(ServerPlayer player) {
        if (player == null) {
            return;
        }
        ResourceKey<Level> dim = player.level().dimension();
        ConcurrentHashMap<SealPos, SealEntity> dimSeals = sealEntities.get(getDimKey(dim));
        if (dimSeals == null || dimSeals.isEmpty()) {
            return;
        }
        LOGGER.debug("Syncing {} seals to player {}", dimSeals.size(), player.getName().getString());
        for (SealEntity sealEntity : dimSeals.values()) {
            if (sealEntity.getSeal() != null) {
                PacketHandler.sendToPlayer(new PacketSealToClient(sealEntity), player);
            }
        }
    }

    public static void syncSealsInRangeToPlayer(ServerPlayer player, BlockPos center, int range) {
        if (player == null) {
            return;
        }
        List<SealEntity> seals = getSealsInRange(player.level(), center, range);
        for (SealEntity sealEntity : seals) {
            if (sealEntity.getSeal() != null) {
                PacketHandler.sendToPlayer(new PacketSealToClient(sealEntity), player);
            }
        }
    }

    public static void clearDimension(ResourceKey<Level> dim) {
        sealEntities.remove(getDimKey(dim));
    }
}