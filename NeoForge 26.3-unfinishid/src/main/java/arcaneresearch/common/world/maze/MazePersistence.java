package arcaneresearch.common.world.maze;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import arcaneresearch.Arcaneresearch;

/**
 * Персистентность лабиринта, порт TC4 MazeHandler.loadMaze/saveMaze.
 * Без этого раскладка жила только в памяти до рестарта сервера.
 * Лежит в arcaneresearch.arcaneresearch.common.world.maze по просьбе портирования.
 */
@EventBusSubscriber(modid = Arcaneresearch.MODID)
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
        // Чистим статичную карту: иначе новый мир в той же сессии строил бы
        // лабиринт по чужому сиду (ensureMaze видит непустую карту и молчит).
        MazeHandler.clear();
    }
}
