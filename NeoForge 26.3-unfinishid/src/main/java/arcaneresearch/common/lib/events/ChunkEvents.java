package arcaneresearch.common.lib.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.level.ChunkDataEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.common.golems.seals.SealEntity;
import arcaneresearch.common.golems.seals.SealHandler;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.misc.PacketSealToClient;

@EventBusSubscriber(modid = Arcaneresearch.MODID)
public class ChunkEvents {

    private static final String ARCANERESEARCH_DATA_KEY = "Arcaneresearch";
    private static final String SEALS_KEY = "seals";

    // NEOFORGE_PORT: chunk seal persistence moved to the SEAL_DATA chunk
    // attachment (ChunkDataEvent data is an immutable record in 26.x).
    // Same content as the Forge "Arcaneresearch"/"seals" NBT.
    public record SealChunkData(java.util.List<CompoundTag> seals) {
        public SealChunkData() {
            this(new java.util.ArrayList<>());
        }

        public static final com.mojang.serialization.MapCodec<SealChunkData> CODEC =
                CompoundTag.CODEC.listOf().optionalFieldOf(SEALS_KEY, java.util.List.of())
                        .xmap(SealChunkData::new, SealChunkData::seals);
    }

    // NEOFORGE_PORT: the Forge handler wrote "Arcaneresearch"/"seals" into the mutable
    // ChunkDataEvent.Save NBT (event.getData().put(...)). 26.x serializes chunk attachments
    // before that event fires and setData() re-dirties the chunk, so the snapshot is taken
    // on the server tick instead (ServerEvents#writeChunkAttachments, see the note there).
    // Writing it here made ChunkMap.saveAllChunks() loop until the heap was exhausted.

    @SubscribeEvent
    public static void onChunkLoad(ChunkDataEvent.Load event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            SealChunkData data = event.getChunk().getData(
                    arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities.SEAL_DATA.get());

            for (CompoundTag sealNbt : data.seals()) {
                try {
                    SealEntity seal = new SealEntity();
                    seal.readNBT(sealNbt);

                    if (seal.getSeal() != null && seal.getSealPos() != null) {

                        if (!SealHandler.addSealEntity(level, seal)) {
                            SealHandler.removeSealEntity(level, seal.getSealPos(), true);
                            SealHandler.addSealEntity(level, seal);
                        }
                    }
                } catch (Exception e) {
                    Arcaneresearch.LOGGER.error("Failed to load seal from chunk data", e);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onChunkWatch(ChunkWatchEvent.Watch event) {
        ServerPlayer player = event.getPlayer();
        Level level = player.level();
        ChunkPos chunkPos = event.getPos();

        for (ISealEntity seal : SealHandler.getSealsInChunk(level, chunkPos)) {
            if (seal.getSeal() != null) {
                PacketHandler.sendToPlayer(new PacketSealToClient(seal), player);
            }
        }
    }

    @SubscribeEvent
    public static void onChunkUnWatch(ChunkWatchEvent.UnWatch event) {

    }
}
