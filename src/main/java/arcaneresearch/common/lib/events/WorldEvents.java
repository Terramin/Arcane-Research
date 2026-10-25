package arcaneresearch.common.lib.events;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.level.NoteBlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.blocks.world.taint.TaintHelper;
import arcaneresearch.common.golems.seals.SealHandler;
import arcaneresearch.common.world.aura.AuraHandler;
import java.util.ArrayList;
import java.util.HashMap;
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WorldEvents {
    public static final WorldEvents INSTANCE = new WorldEvents();
    public static final HashMap<String, ArrayList<NoteBlockData>> noteBlockEvents = new HashMap<>();
    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            AuraHandler.addAuraWorld(serverLevel.dimension());
            Arcaneresearch.LOGGER.debug("Loaded aura for dimension: {}", serverLevel.dimension().location());
        }
    }
    @SubscribeEvent
    public static void onWorldSave(LevelEvent.Save event) {
    }
    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            String dimKey = serverLevel.dimension().location().toString();
            SealHandler.clearDimension(serverLevel.dimension());
            TaintHelper.clearDimension(serverLevel.dimension());
            AuraHandler.removeAuraWorld(serverLevel.dimension());
            noteBlockEvents.remove(dimKey);
            Arcaneresearch.LOGGER.debug("Unloaded aura for dimension: {}", dimKey);
        }
    }
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof Level level)) return;
        if (level.isClientSide()) return;
        if (event.getEntity() instanceof Player player) {
            BlockPos pos = event.getPos();
            if (isNearActiveBoss(level, player, pos)) {
                event.setCanceled(true);
            }
        }
    }
    private static boolean isNearActiveBoss(Level level, Player player, BlockPos pos) {
        return false;
    }
    @SubscribeEvent
    public static void onNoteBlockPlay(NoteBlockEvent.Play event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        String dimKey = serverLevel.dimension().location().toString();
        noteBlockEvents.computeIfAbsent(dimKey, k -> new ArrayList<>());
        ArrayList<NoteBlockData> list = noteBlockEvents.get(dimKey);
        BlockPos pos = event.getPos();
        NoteBlockData data = new NoteBlockData(
                pos.getX(), pos.getY(), pos.getZ(),
                event.getInstrument().ordinal(),
                event.getVanillaNoteId()
        );
        list.add(data);
        while (list.size() > 100) {
            list.remove(0);
        }
    }
    public static ArrayList<NoteBlockData> getAndClearNoteBlockEvents(String dimKey) {
        ArrayList<NoteBlockData> events = noteBlockEvents.remove(dimKey);
        return events != null ? events : new ArrayList<>();
    }
    public static class NoteBlockData {
        public final int x, y, z;
        public final int instrument;
        public final int note;
        public NoteBlockData(int x, int y, int z, int instrument, int note) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.instrument = instrument;
            this.note = note;
        }
    }
}
