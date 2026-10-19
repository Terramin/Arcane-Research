package arcaneresearch.common.lib.events;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.common.golems.seals.SealEntity;
import arcaneresearch.common.golems.seals.SealHandler;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.misc.PacketSealToClient;
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChunkEvents {
    private static final String ARCANERESEARCH_DATA_KEY = "Arcaneresearch";
    private static final String SEALS_KEY = "seals";
    @SubscribeEvent
    public static void onChunkSave(ChunkDataEvent.Save event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            ChunkPos chunkPos = event.getChunk().getPos();
            CompoundTag arcaneresearchData = new CompoundTag();
            ListTag sealList = new ListTag();
            for (ISealEntity seal : SealHandler.getSealsInChunk(level, chunkPos)) {
                if (seal.getSeal() != null) {
                    CompoundTag sealNbt = seal.writeNBT();
                    sealList.add(sealNbt);
                }
            }
            if (!sealList.isEmpty()) {
                arcaneresearchData.put(SEALS_KEY, sealList);
            }
            if (!arcaneresearchData.isEmpty()) {
                event.getData().put(ARCANERESEARCH_DATA_KEY, arcaneresearchData);
            }
        }
    }
    @SubscribeEvent
    public static void onChunkLoad(ChunkDataEvent.Load event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            CompoundTag data = event.getData();
            if (data.contains(ARCANERESEARCH_DATA_KEY, Tag.TAG_COMPOUND)) {
                CompoundTag arcaneresearchData = data.getCompound(ARCANERESEARCH_DATA_KEY);
                if (arcaneresearchData.contains(SEALS_KEY, Tag.TAG_LIST)) {
                    ListTag sealList = arcaneresearchData.getList(SEALS_KEY, Tag.TAG_COMPOUND);
                    for (int i = 0; i < sealList.size(); i++) {
                        CompoundTag sealNbt = sealList.getCompound(i);
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
