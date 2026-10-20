package arcaneresearch.common.world.maze;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MazePersistence {
    private MazePersistence() {
    }
    private static boolean isOuter(ServerLevel level) {
        return level.dimension().equals(MazeFeature.DIMENSION);
    }
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !isOuter(level)) {
            return;
        }
        if (!MazeHandler.labyrinth.isEmpty()) {
            return;
        }
        MazeHandler.loadMaze(level);
    }
    @SubscribeEvent
    public static void onLevelSave(LevelEvent.Save event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !isOuter(level)) {
            return;
        }
        MazeHandler.saveMaze(level);
    }
    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !isOuter(level)) {
            return;
        }
        MazeHandler.saveMaze(level);
        MazeHandler.clear();
    }
}
