package arcaneresearch.common.world.aura;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.common.lib.utils.PosXY;
import arcaneresearch.common.world.biomes.BiomeHandler;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
public class AuraHandler {
    public static final int AURA_CEILING = 500;
    public static final ConcurrentHashMap<ResourceKey<Level>, CopyOnWriteArrayList<ChunkPos>> dirtyChunks = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<ResourceKey<Level>, BlockPos> riftTrigger = new ConcurrentHashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(AuraHandler.class);
    private static final ConcurrentHashMap<ResourceKey<Level>, AuraWorld> auras = new ConcurrentHashMap<>();
    public static AuraWorld getAuraWorld(ResourceKey<Level> dimension) {
        return auras.get(dimension);
    }
    public static AuraChunk getAuraChunk(ResourceKey<Level> dimension, int x, int z) {
        AuraWorld world = auras.get(dimension);
        if (world == null) {
            addAuraWorld(dimension);
            world = auras.get(dimension);
        }
        return world != null ? world.getAuraChunkAt(x, z) : null;
    }
    public static void addAuraWorld(ResourceKey<Level> dimension) {
        if (!auras.containsKey(dimension)) {
            auras.put(dimension, new AuraWorld(dimension));
            LOGGER.info("Creating aura cache for dimension {}", dimension.location());
        }
    }
    public static void removeAuraWorld(ResourceKey<Level> dimension) {
        auras.remove(dimension);
        LOGGER.info("Removing aura cache for dimension {}", dimension.location());
    }
    public static void addAuraChunk(ResourceKey<Level> dimension, LevelChunk chunk, short base, float vis, float flux) {
        AuraWorld aw = auras.computeIfAbsent(dimension, AuraWorld::new);
        aw.setAuraChunk(new PosXY(chunk.getPos().x, chunk.getPos().z), new AuraChunk(chunk, base, vis, flux));
    }
    public static void removeAuraChunk(ResourceKey<Level> dimension, int x, int z) {
        AuraWorld aw = auras.get(dimension);
        if (aw != null) {
            aw.removeAuraChunk(x, z);
        }
    }
    public static float getTotalAura(Level level, BlockPos pos) {
        AuraChunk ac = getAuraChunk(level.dimension(), pos.getX() >> 4, pos.getZ() >> 4);
        return ac != null ? (ac.getVis() + ac.getFlux()) : 0.0f;
    }
    public static float getFluxSaturation(Level level, BlockPos pos) {
        AuraChunk ac = getAuraChunk(level.dimension(), pos.getX() >> 4, pos.getZ() >> 4);
        if (ac != null && ac.getBase() > 0) {
            return ac.getFlux() / ac.getBase();
        }
        return 0.0f;
    }
    public static float getVis(Level level, BlockPos pos) {
        AuraChunk ac = getAuraChunk(level.dimension(), pos.getX() >> 4, pos.getZ() >> 4);
        return ac != null ? ac.getVis() : 0.0f;
    }
    public static float getFlux(Level level, BlockPos pos) {
        AuraChunk ac = getAuraChunk(level.dimension(), pos.getX() >> 4, pos.getZ() >> 4);
        return ac != null ? ac.getFlux() : 0.0f;
    }
    public static int getAuraBase(Level level, BlockPos pos) {
        AuraChunk ac = getAuraChunk(level.dimension(), pos.getX() >> 4, pos.getZ() >> 4);
        return ac != null ? ac.getBase() : 0;
    }
    public static boolean shouldPreserveAura(Level level, Player player, BlockPos pos) {
        int base = getAuraBase(level, pos);
        if (base <= 0) return false;
        float visRatio = getVis(level, pos) / base;
        if (visRatio >= 0.1f) return false;
        if (player == null) return true;
        return ArcaneresearchCapabilities.isResearchComplete(player, "AURAPRESERVE");
    }
    public static void addVis(Level level, BlockPos pos, float amount) {
        if (amount <= 0.0f) return;
        try {
            AuraChunk ac = getAuraChunk(level.dimension(), pos.getX() >> 4, pos.getZ() >> 4);
            modifyVisInChunk(ac, amount, true);
        } catch (Exception ignored) {
        }
    }
    public static void addFlux(Level level, BlockPos pos, float amount) {
        if (amount <= 0.0f) return;
        try {
            AuraChunk ac = getAuraChunk(level.dimension(), pos.getX() >> 4, pos.getZ() >> 4);
            modifyFluxInChunk(ac, amount, true);
        } catch (Exception ignored) {
        }
    }
    public static float drainVis(Level level, BlockPos pos, float amount, boolean simulate) {
        try {
            AuraChunk ac = getAuraChunk(level.dimension(), pos.getX() >> 4, pos.getZ() >> 4);
            if (ac == null) return 0.0f;
            float available = ac.getVis();
            if (amount > available) {
                amount = available;
            }
            if (modifyVisInChunk(ac, -amount, !simulate)) {
                return amount;
            }
        } catch (Exception ignored) {
        }
        return 0.0f;
    }
    public static float drainFlux(Level level, BlockPos pos, float amount, boolean simulate) {
        try {
            AuraChunk ac = getAuraChunk(level.dimension(), pos.getX() >> 4, pos.getZ() >> 4);
            if (ac == null) return 0.0f;
            float available = ac.getFlux();
            if (amount > available) {
                amount = available;
            }
            if (modifyFluxInChunk(ac, -amount, !simulate)) {
                return amount;
            }
        } catch (Exception ignored) {
        }
        return 0.0f;
    }
    public static boolean modifyVisInChunk(AuraChunk ac, float amount, boolean apply) {
        if (ac == null) return false;
        if (apply) {
            ac.setVis(Math.max(0.0f, ac.getVis() + amount));
        }
        return true;
    }
    private static boolean modifyFluxInChunk(AuraChunk ac, float amount, boolean apply) {
        if (ac == null) return false;
        if (apply) {
            ac.setFlux(Math.max(0.0f, ac.getFlux() + amount));
        }
        return true;
    }
    public static void generateAura(LevelChunk chunk, RandomSource rand) {
        Level level = chunk.getLevel();
        BlockPos center = new BlockPos(chunk.getPos().x * 16 + 8, 50, chunk.getPos().z * 16 + 8);
        Holder<Biome> centerBiome = level.getBiome(center);
        if (BiomeHandler.isBiomeBlacklisted(centerBiome)) {
            return;
        }
        float life = getBiomeAuraModifier(level, center);
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = center.relative(dir, 16);
            life += getBiomeAuraModifier(level, neighborPos);
        }
        life /= 5.0f;
        float noise = (float) (1.0 + rand.nextGaussian() * 0.1);
        short base = (short) (life * AURA_CEILING * noise);
        base = (short) Mth.clamp(base, 0, AURA_CEILING);
        addAuraChunk(level.dimension(), chunk, base, base, 0.0f);
    }
    private static float getBiomeAuraModifier(Level level, BlockPos pos) {
        Holder<Biome> biome = level.getBiome(pos);
        return BiomeHandler.getAuraModifier(biome);
    }
    public static void markChunkDirty(ResourceKey<Level> dimension, ChunkPos chunkPos) {
        if (!dirtyChunks.containsKey(dimension)) {
            dirtyChunks.put(dimension, new CopyOnWriteArrayList<>());
        }
        dirtyChunks.get(dimension).add(chunkPos);
    }
}
