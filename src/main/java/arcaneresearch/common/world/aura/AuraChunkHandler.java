package arcaneresearch.common.world.aura;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.event.level.ChunkDataEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.config.ModConfig;



public class AuraChunkHandler {

    public static void register() {
        NeoForge.EVENT_BUS.register(AuraChunkHandler.class);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(AuraChunkHandler.class);

    
    
    
    
    public record AuraChunkData(short base, float vis, float flux) {
        public AuraChunkData() {
            this((short) 0, 0.0f, 0.0f);
        }

        public static final com.mojang.serialization.MapCodec<AuraChunkData> CODEC =
                com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(inst -> inst.group(
                        com.mojang.serialization.Codec.SHORT.fieldOf("base").forGetter(AuraChunkData::base),
                        com.mojang.serialization.Codec.FLOAT.fieldOf("vis").forGetter(AuraChunkData::vis),
                        com.mojang.serialization.Codec.FLOAT.fieldOf("flux").forGetter(AuraChunkData::flux))
                        .apply(inst, AuraChunkData::new));
    }

    @SubscribeEvent
    public static void onChunkDataLoad(ChunkDataEvent.Load event) {
        
        
        
        
        
        LevelChunk chunk;
        if (event.getChunk() instanceof LevelChunk levelChunk) {
            chunk = levelChunk;
        } else if (event.getChunk() instanceof ImposterProtoChunk imposter) {
            chunk = imposter.getWrapped();
        } else {
            return;
        }

        net.minecraft.world.level.LevelAccessor eventLevel =
                event.getLevel() != null ? event.getLevel() : chunk.getLevel();
        if (eventLevel == null || eventLevel.isClientSide()) {
            return;
        }

        if (!(eventLevel instanceof ServerLevel level)) {
            return;
        }

        AuraChunkData data = event.getChunk().getData(
                arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities.AURA_DATA.get());
        ResourceKey<Level> dimension = level.dimension();

        if (data.base() != 0 || data.vis() != 0.0f || data.flux() != 0.0f) {
            AuraHandler.addAuraChunk(dimension, chunk, data.base(), data.vis(), data.flux);
        }
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

        AuraChunk existingAura = AuraHandler.getAuraChunk(dimension, chunkPos.x(), chunkPos.z());
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

        AuraHandler.removeAuraChunk(dimension, chunkPos.x(), chunkPos.z());
    }

    private static void generateChunkAura(ServerLevel level, LevelChunk chunk) {
        RandomSource random = level.getRandom();
        AuraHandler.generateAura(chunk, random);

        ResourceKey<Level> dimension = level.dimension();
        ChunkPos chunkPos = chunk.getPos();

        if (!AuraHandler.dirtyChunks.containsKey(dimension)) {
            AuraHandler.dirtyChunks.put(dimension, new java.util.concurrent.CopyOnWriteArrayList<>());
        }
        AuraHandler.dirtyChunks.get(dimension).add(chunkPos);
    }
}
