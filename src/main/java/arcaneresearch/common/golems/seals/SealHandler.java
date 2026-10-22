package arcaneresearch.common.golems.seals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import arcaneresearch.api.golems.seals.ISeal;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.api.golems.seals.SealPos;
import arcaneresearch.api.golems.tasks.Task;
import arcaneresearch.common.golems.tasks.TaskHandler;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.misc.PacketSealToClient;
import arcaneresearch.common.world.aura.AuraHandler;

import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SealHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SealHandler.class);

    public static LinkedHashMap<String, ISeal> types = new LinkedHashMap<>();

    public static ConcurrentHashMap<String, ConcurrentHashMap<SealPos, SealEntity>> sealEntities = new ConcurrentHashMap<>();

    private static int tickCount = 0;

    
    
    
    public static com.mojang.serialization.DynamicOps<net.minecraft.nbt.Tag> nbtOps() {
        net.minecraft.server.MinecraftServer server =
                net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        return server != null
                ? server.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE)
                : net.minecraft.nbt.NbtOps.INSTANCE;
    }

    private static String getDimKey(ResourceKey<Level> dim) {
        return dim.identifier().toString();
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
        if (types.containsKey(seal.getKey())) {
            LOGGER.error("Attempting to register Seal [{}] twice. Ignoring.", seal.getKey());
        } else {
            types.put(seal.getKey(), seal);
        }
    }

    public static String[] getRegisteredSeals() {
        return types.keySet().toArray(new String[0]);
    }

    public static ISeal getSeal(String key) {
        return types.get(key);
    }

    public static CopyOnWriteArrayList<SealEntity> getSealsInRange(Level level, BlockPos source, int range) {
        CopyOnWriteArrayList<SealEntity> out = new CopyOnWriteArrayList<>();
        ConcurrentHashMap<SealPos, SealEntity> dimSeals = sealEntities.get(getDimKey(level.dimension()));

        if (dimSeals != null && !dimSeals.isEmpty()) {
            int rangeSq = range * range;
            for (SealEntity se : dimSeals.values()) {
                if (se.getSeal() != null && se.getSealPos() != null) {
                    if (se.sealPos.pos.distSqr(source) <= rangeSq) {
                        out.add(se);
                    }
                }
            }
        }

        return out;
    }

    public static CopyOnWriteArrayList<SealEntity> getSealsInChunk(Level level, ChunkPos chunk) {
        CopyOnWriteArrayList<SealEntity> out = new CopyOnWriteArrayList<>();
        ConcurrentHashMap<SealPos, SealEntity> dimSeals = sealEntities.get(getDimKey(level.dimension()));

        if (dimSeals != null && !dimSeals.isEmpty()) {
            for (SealEntity se : dimSeals.values()) {
                if (se.getSeal() != null && se.getSealPos() != null) {
                    
        ChunkPos sealChunk = ChunkPos.containing(se.sealPos.pos);
                    if (sealChunk.equals(chunk)) {
                        out.add(se);
                    }
                }
            }
        }

        return out;
    }

    public static ISealEntity getSealEntity(ResourceKey<Level> dim, SealPos pos) {
        if (pos == null) return null;

        String dimKey = getDimKey(dim);
        sealEntities.computeIfAbsent(dimKey, k -> new ConcurrentHashMap<>());

        return sealEntities.get(dimKey).get(pos);
    }

    public static boolean addSealEntity(Level level, BlockPos pos, Direction face, ISeal seal, Player player) {
        String dimKey = getDimKey(level.dimension());
        sealEntities.computeIfAbsent(dimKey, k -> new ConcurrentHashMap<>());

        ConcurrentHashMap<SealPos, SealEntity> dimSeals = sealEntities.get(dimKey);
        SealPos sealPos = new SealPos(pos, face);

        if (dimSeals.containsKey(sealPos)) {
            return false;
        }

        SealEntity sealEntity = new SealEntity(level, sealPos, seal);
        sealEntity.setOwner(player.getUUID().toString());
        dimSeals.put(sealPos, sealEntity);

        if (!level.isClientSide()) {
            sealEntity.syncToClient(level);
            markChunkAsDirty(level.dimension(), pos);
            forceChunkSave(level, pos);
        }

        return true;
    }

    public static boolean addSealEntity(Level level, SealEntity seal) {
        if (level == null || seal == null) return false;

        String dimKey = getDimKey(level.dimension());
        sealEntities.computeIfAbsent(dimKey, k -> new ConcurrentHashMap<>());

        ConcurrentHashMap<SealPos, SealEntity> dimSeals = sealEntities.get(dimKey);

        if (dimSeals.containsKey(seal.getSealPos())) {
            return false;
        }

        dimSeals.put(seal.getSealPos(), seal);

        if (!level.isClientSide()) {
            seal.syncToClient(level);
            markChunkAsDirty(level.dimension(), seal.getSealPos().pos);
        }

        return true;
    }

    private static void forceChunkSave(Level level, BlockPos pos) {
        try {
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                net.minecraft.world.level.chunk.LevelChunk chunk =
                        serverLevel.getChunkAt(pos);
                if (chunk != null) {
                    
                    chunk.markUnsaved();
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void removeSealEntity(Level level, SealPos pos, boolean quiet) {
        if (level == null || pos == null || pos.pos == null || pos.face == null) {
            return;
        }
        String dimKey = getDimKey(level.dimension());
        sealEntities.computeIfAbsent(dimKey, k -> new ConcurrentHashMap<>());

        ConcurrentHashMap<SealPos, SealEntity> dimSeals = sealEntities.get(dimKey);
        SealEntity seal = dimSeals.remove(pos);
        boolean removed = seal != null;

        try {
            if (!level.isClientSide() && seal != null && seal.seal != null) {
                seal.seal.onRemoval(level, pos.pos, pos.face);
            }

            if (!quiet && seal != null && seal.getSeal() != null && !level.isClientSide()) {
                ItemStack sealStack = ItemSealPlacer.getSealStack(seal.getSeal().getKey());
                if (!sealStack.isEmpty()) {
                    double x = pos.pos.getX() + 0.5 + pos.face.getStepX() / 1.7;
                    double y = pos.pos.getY() + 0.5 + pos.face.getStepY() / 1.7;
                    double z = pos.pos.getZ() + 0.5 + pos.face.getStepZ() / 1.7;
                    level.addFreshEntity(new ItemEntity(level, x, y, z, sealStack));
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Removing invalid seal at {}", pos.pos);
        }

        ConcurrentHashMap<Integer, Task> tasks = null;
        try {
            tasks = TaskHandler.getTasks(level.dimension());
        } catch (Exception e) {
            LOGGER.warn("Failed to suspend tasks for removed seal at {}", pos.pos);
        }
        if (tasks != null) {
            try {
                for (Task task : tasks.values()) {
                    if (task != null && task.getSealPos() != null && task.getSealPos().equals(pos)) {
                        task.setSuspended(true);
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to suspend tasks for removed seal at {}", pos.pos);
            }
        }

        
        
        
        
        if (!removed) {
            return;
        }

        if (!level.isClientSide() && !quiet && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            try {
                SealEntity removedSeal = new SealEntity(level, pos, null);
                
                
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayersInDimension(
                        serverLevel, new arcaneresearch.common.lib.network.misc.PacketSealToClient(removedSeal));
            } catch (Exception e) {
                LOGGER.warn("Failed to sync removed seal at {}", pos.pos);
            }
        }

        if (!quiet) {
            try {
                markChunkAsDirty(level.dimension(), pos.pos);
                if (!level.isClientSide()) {
                    forceChunkSave(level, pos.pos);
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to mark chunk dirty for removed seal at {}", pos.pos);
            }
        }
    }

    public static void tickSealEntities(Level level) {
        String dimKey = getDimKey(level.dimension());
        sealEntities.computeIfAbsent(dimKey, k -> new ConcurrentHashMap<>());

        ConcurrentHashMap<SealPos, SealEntity> dimSeals = sealEntities.get(dimKey);
        tickCount++;

        for (SealEntity sealEntity : dimSeals.values()) {
            if (level.isLoaded(sealEntity.sealPos.pos)) {
                try {
                    boolean shouldTick = true;

                    if (tickCount % 20 == 0 && !sealEntity.seal.canPlaceAt(level, sealEntity.sealPos.pos, sealEntity.sealPos.face)) {
                        removeSealEntity(level, sealEntity.sealPos, false);
                        shouldTick = false;
                    }

                    if (shouldTick) {
                        sealEntity.tickSealEntity(level);
                    }
                } catch (Exception e) {
                    LOGGER.error("Error ticking seal at {}, removing", sealEntity.sealPos.pos, e);
                    removeSealEntity(level, sealEntity.sealPos, false);
                }
            }
        }
    }

    public static void markChunkAsDirty(ResourceKey<Level> dim, BlockPos pos) {
        ChunkPos chunkPos = ChunkPos.containing(pos);
        AuraHandler.markChunkDirty(dim, chunkPos);
    }

    public static void syncAllSealsToPlayer(ServerPlayer player) {
        ResourceKey<Level> dim = player.level().dimension();
        String dimKey = getDimKey(dim);
        ConcurrentHashMap<SealPos, SealEntity> dimSeals = sealEntities.get(dimKey);

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
        CopyOnWriteArrayList<SealEntity> seals = getSealsInRange(player.level(), center, range);

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
