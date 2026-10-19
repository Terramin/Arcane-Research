package arcaneresearch.common.world.aura;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class AuraThreadManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuraThreadManager.class);
    private static final Map<ResourceKey<Level>, AuraThread> auraThreads = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Thread> threads = new ConcurrentHashMap<>();
    public static void startThread(ResourceKey<Level> dimension) {
        if (auraThreads.containsKey(dimension)) {
            AuraThread existing = auraThreads.get(dimension);
            if (!existing.isStopped()) {
                return;
            }
            removeThread(dimension);
        }
        if (AuraHandler.getAuraWorld(dimension) == null) {
            LOGGER.debug("Not starting aura thread for {} - no aura world exists", dimension.location());
            return;
        }
        LOGGER.info("Starting aura thread for dimension {}", dimension.location());
        AuraThread auraThread = new AuraThread(dimension);
        Thread thread = new Thread(auraThread, "Arcaneresearch-Aura-" + dimension.location().toString().replace(':', '-'));
        thread.setDaemon(true);
        thread.start();
        auraThreads.put(dimension, auraThread);
        threads.put(dimension, thread);
    }
    public static void stopThread(ResourceKey<Level> dimension) {
        AuraThread auraThread = auraThreads.get(dimension);
        if (auraThread != null) {
            LOGGER.info("Stopping aura thread for dimension {}", dimension.location());
            auraThread.stop();
        }
        Thread thread = threads.get(dimension);
        if (thread != null) {
            try {
                thread.interrupt();
                thread.join(2000);
            } catch (InterruptedException e) {
                LOGGER.warn("Interrupted while waiting for aura thread to stop");
            }
        }
        removeThread(dimension);
    }
    public static void removeThread(ResourceKey<Level> dimension) {
        auraThreads.remove(dimension);
        threads.remove(dimension);
    }
    public static void stopAllThreads() {
        LOGGER.info("Stopping all aura threads ({} active)", auraThreads.size());
        for (AuraThread auraThread : auraThreads.values()) {
            auraThread.stop();
        }
        for (Thread thread : threads.values()) {
            try {
                thread.interrupt();
            } catch (Exception e) {
            }
        }
        for (Map.Entry<ResourceKey<Level>, Thread> entry : threads.entrySet()) {
            try {
                entry.getValue().join(1000);
            } catch (InterruptedException e) {
                LOGGER.warn("Interrupted while waiting for aura thread {} to stop", entry.getKey().location());
            }
        }
        auraThreads.clear();
        threads.clear();
    }
    public static boolean hasThread(ResourceKey<Level> dimension) {
        AuraThread thread = auraThreads.get(dimension);
        return thread != null && !thread.isStopped();
    }
    public static AuraThread getThread(ResourceKey<Level> dimension) {
        return auraThreads.get(dimension);
    }
    public static void onLevelLoad(ServerLevel level) {
        ResourceKey<Level> dimension = level.dimension();
        AuraHandler.addAuraWorld(dimension);
        startThread(dimension);
    }
    public static void onLevelUnload(ServerLevel level) {
        ResourceKey<Level> dimension = level.dimension();
        stopThread(dimension);
        AuraHandler.removeAuraWorld(dimension);
    }
}
