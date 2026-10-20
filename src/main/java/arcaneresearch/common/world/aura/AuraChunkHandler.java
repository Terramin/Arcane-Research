package arcaneresearch.common.world.aura;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.config.ModConfig;
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AuraChunkHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuraChunkHandler.class);
    private static final String TAG_ARCANERESEARCH = "arcaneresearch";
    private static final String TAG_AURA = "aura";
    private static final String TAG_BASE = "base";
    private static final String TAG_VIS = "vis";
    private static final String TAG_FLUX = "flux";
    @SubscribeEvent
    public static void onChunkDataLoad(ChunkDataEvent.Load event) {
        if (event.getLevel() == null || event.getLevel().isClientSide()) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        CompoundTag data = event.getData();
        ResourceKey<Level> dimension = level.dimension();
        ChunkPos chunkPos = chunk.getPos();
        if (data.contains(TAG_ARCANERESEARCH)) {
            CompoundTag tcData = data.getCompound(TAG_ARCANERESEARCH);
            if (tcData.contains(TAG_AURA)) {
                CompoundTag auraData = tcData.getCompound(TAG_AURA);
                short base = auraData.getShort(TAG_BASE);
                float vis = auraData.getFloat(TAG_VIS);
                float flux = auraData.getFloat(TAG_FLUX);
                AuraHandler.addAuraChunk(dimension, chunk, base, vis, flux);
                return;
            }
        }
        if (data.contains("Arcaneresearch")) {
            CompoundTag tcData = data.getCompound("Arcaneresearch");
            if (tcData.contains("base")) {
                short base = tcData.getShort("base");
                float vis = tcData.contains("vis") ? tcData.getFloat("vis") : 0.0f;
                float flux = tcData.contains("flux") ? tcData.getFloat("flux") : 0.0f;
                AuraHandler.addAuraChunk(dimension, chunk, base, vis, flux);
                return;
            }
        }
    }
    @SubscribeEvent
    public static void onChunkDataSave(ChunkDataEvent.Save event) {
        if (event.getLevel() == null || event.getLevel().isClientSide()) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        ResourceKey<Level> dimension = level.dimension();
        ChunkPos chunkPos = event.getChunk().getPos();
        AuraChunk auraChunk = AuraHandler.getAuraChunk(dimension, chunkPos.x, chunkPos.z);
        if (auraChunk == null) {
            return;
        }
        CompoundTag data = event.getData();
        CompoundTag tcData = data.contains(TAG_ARCANERESEARCH) ?
                data.getCompound(TAG_ARCANERESEARCH) : new CompoundTag();
        CompoundTag auraData = new CompoundTag();
        auraData.putShort(TAG_BASE, auraChunk.getBase());
        auraData.putFloat(TAG_VIS, auraChunk.getVis());
        auraData.putFloat(TAG_FLUX, auraChunk.getFlux());
        tcData.put(TAG_AURA, auraData);
        data.put(TAG_ARCANERESEARCH, tcData);
    }
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() == null || event.getLevel().isClientSide()) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        if (!ModConfig.generateAura) {
            return;
        }
        ResourceKey<Level> dimension = level.dimension();
        ChunkPos chunkPos = chunk.getPos();
        AuraChunk existingAura = AuraHandler.getAuraChunk(dimension, chunkPos.x, chunkPos.z);
        if (existingAura != null && existingAura.getBase() > 0) {
            return;
        }
        generateChunkAura(level, chunk);
    }
    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() == null || event.getLevel().isClientSide()) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        ResourceKey<Level> dimension = level.dimension();
        ChunkPos chunkPos = event.getChunk().getPos();
        AuraHandler.removeAuraChunk(dimension, chunkPos.x, chunkPos.z);
    }
    private static void generateChunkAura(ServerLevel level, LevelChunk chunk) {
        RandomSource random = level.random;
        AuraHandler.generateAura(chunk, random);
        ResourceKey<Level> dimension = level.dimension();
        ChunkPos chunkPos = chunk.getPos();
        if (!AuraHandler.dirtyChunks.containsKey(dimension)) {
            AuraHandler.dirtyChunks.put(dimension, new java.util.concurrent.CopyOnWriteArrayList<>());
        }
        AuraHandler.dirtyChunks.get(dimension).add(chunkPos);
    }
}
