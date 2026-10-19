package arcaneresearch.common.world.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuraThread implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuraThread.class);
    private static final long INTERVAL = 1000L;
    private static final float[] PHASE_VIS_TABLE = {0.25f, 0.15f, 0.1f, 0.05f, 0.0f, 0.05f, 0.1f, 0.15f};
    private static final float[] PHASE_MAX_TABLE = {0.15f, 0.05f, 0.0f, -0.05f, -0.15f, -0.05f, 0.0f, 0.05f};
    private final ResourceKey<Level> dimension;
    private final Random rand;
    private volatile boolean stop = false;
    private float phaseVis = 0.0f;
    private float phaseFlux = 0.0f;
    private float phaseMax = 0.0f;
    private long lastWorldTime = 0L;

    public AuraThread(ResourceKey<Level> dimension) {
        this.dimension = dimension;
        this.rand = new Random(System.currentTimeMillis());
    }

    @Override
    public void run() {
        LOGGER.info("Starting aura thread for dimension {}", dimension.identifier());

        while (!stop) {

            if (AuraHandler.getAuraWorld(dimension) == null) {
                LOGGER.warn("No aura world found for dimension {}!", dimension.identifier());
                break;
            }

            long startTime = System.currentTimeMillis();

            try {
                processAuras();
            } catch (Exception e) {
                LOGGER.error("Error processing auras in dimension {}", dimension.identifier(), e);
            }

            long executionTime = System.currentTimeMillis() - startTime;

            if (executionTime > INTERVAL) {
                LOGGER.warn("Aura processing took {}ms longer than normal in dimension {}",
                        executionTime - INTERVAL, dimension.identifier());
            }

            try {
                Thread.sleep(Math.max(1L, INTERVAL - executionTime));
            } catch (InterruptedException e) {

                break;
            }
        }

        LOGGER.info("Stopping aura thread for dimension {}", dimension.identifier());

        try {
            AuraThreadManager.removeThread(dimension);
        } catch (Exception e) {
            LOGGER.error("Error removing aura thread for dimension {}", dimension.identifier(), e);
        }
    }

    private void processAuras() {
        AuraWorld auraWorld = AuraHandler.getAuraWorld(dimension);
        if (auraWorld == null) {
            stop();
            return;
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }

        ServerLevel level = server.getLevel(dimension);
        if (level == null) {
            return;
        }

        long worldTime = level.getLevelData().getGameTime();
        if (lastWorldTime != worldTime) {
            lastWorldTime = worldTime;
            updateMoonPhaseModifiers(level);

            for (AuraChunk auraChunk : auraWorld.getAuraChunks().values()) {
                processAuraChunk(auraWorld, auraChunk, level);
            }
        }
    }

    private void updateMoonPhaseModifiers(ServerLevel level) {
        // NEOFORGE_PORT: ServerLevel.getMoonPhase gone in 26.x -> vanilla formula
        // (dayTime / 24000 % 8, same as ScanSky).
        int moonPhase = (int) (level.getLevelData().getGameTime() / 24000L % 8L);
        phaseVis = PHASE_VIS_TABLE[moonPhase];
        phaseMax = 1.0f + PHASE_MAX_TABLE[moonPhase];
        phaseFlux = 0.25f - phaseVis;
    }

    private void processAuraChunk(AuraWorld auraWorld, AuraChunk auraChunk, ServerLevel level) {

        List<Integer> directions = Arrays.asList(0, 1, 2, 3);
        Collections.shuffle(directions, rand);

        ChunkPos loc = auraChunk.getLoc();
        if (loc == null) return;

        int x = loc.x();
        int z = loc.z();

        float base = auraChunk.getBase() * phaseMax;
        boolean dirty = false;

        float currentVis = auraChunk.getVis();
        float currentFlux = auraChunk.getFlux();

        AuraChunk neighbourVisChunk = null;
        AuraChunk neighbourFluxChunk = null;
        float lowestVis = Float.MAX_VALUE;
        float lowestFlux = Float.MAX_VALUE;

        for (Integer dirIndex : directions) {
            Direction dir = Direction.from2DDataValue(dirIndex);
            AuraChunk neighbor = auraWorld.getAuraChunkAt(x + dir.getStepX(), z + dir.getStepZ());

            if (neighbor != null) {
                float neighborBase = neighbor.getBase() * phaseMax;

                if ((neighbourVisChunk == null || lowestVis > neighbor.getVis())
                        && neighbor.getVis() + neighbor.getFlux() < neighborBase) {
                    neighbourVisChunk = neighbor;
                    lowestVis = neighbor.getVis();
                }

                if (neighbourFluxChunk == null || lowestFlux > neighbor.getFlux()) {
                    neighbourFluxChunk = neighbor;
                    lowestFlux = neighbor.getFlux();
                }
            }
        }

        if (neighbourVisChunk != null && lowestVis < currentVis && lowestVis / currentVis < 0.75) {
            float transfer = Math.min(currentVis - lowestVis, 1.0f);
            currentVis -= transfer;
            neighbourVisChunk.setVis(lowestVis + transfer);
            dirty = true;
            markChunkAsDirty(neighbourVisChunk);
        }

        float fluxThreshold = Math.max(5.0f, auraChunk.getBase() / 10.0f);
        if (neighbourFluxChunk != null && currentFlux > fluxThreshold && lowestFlux < currentFlux / 1.75) {
            float transfer = Math.min(currentFlux - lowestFlux, 1.0f);
            currentFlux -= transfer;
            neighbourFluxChunk.setFlux(lowestFlux + transfer);
            dirty = true;
            markChunkAsDirty(neighbourFluxChunk);
        }

        if (currentVis + currentFlux < base) {
            float regen = Math.min(base - currentVis + currentFlux, phaseVis);
            currentVis += regen;
            dirty = true;
        } else if (currentVis > base * 1.25 && rand.nextFloat() < 0.1) {
            currentFlux += phaseFlux;
            currentVis -= phaseFlux;
            dirty = true;
        } else if (currentVis <= base * 0.1 && currentVis >= currentFlux && rand.nextFloat() < 0.1) {
            currentFlux += phaseFlux;
            dirty = true;
        }

        if (dirty) {
            auraChunk.setVis(currentVis);
            auraChunk.setFlux(currentFlux);
            markChunkAsDirty(auraChunk);
        }

        if (currentFlux > base * 0.75 && rand.nextFloat() < currentFlux / 5000.0f) {
            BlockPos riftPos = new BlockPos(x * 16, 0, z * 16);
            AuraHandler.riftTrigger.put(dimension, riftPos);
        }
    }

    private void markChunkAsDirty(AuraChunk chunk) {
        if (chunk == null || chunk.isModified()) {
            return;
        }

        ChunkPos pos = chunk.getLoc();
        if (pos == null) return;

        CopyOnWriteArrayList<ChunkPos> dirtyList = AuraHandler.dirtyChunks
                .computeIfAbsent(dimension, k -> new CopyOnWriteArrayList<>());

        if (!dirtyList.contains(pos)) {
            dirtyList.add(pos);
        }
    }

    public void stop() {
        stop = true;
    }

    public boolean isStopped() {
        return stop;
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }
}
