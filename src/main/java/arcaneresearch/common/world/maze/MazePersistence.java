package arcaneresearch.common.world.maze;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import arcaneresearch.Arcaneresearch;

@EventBusSubscriber(modid = Arcaneresearch.MODID)
public final class MazePersistence {
    private MazePersistence() {
    }

    private static ServerLevel outerLevel(LevelEvent event) {
        if (event == null) {
            return null;
        }
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return null;
        }
        ResourceKey<Level> mazeDim = MazeFeature.DIMENSION;
        return mazeDim.equals(level.dimension()) ? level : null;
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        ServerLevel level = outerLevel(event);
        if (level == null) {
            return;
        }
        if (MazeHandler.labyrinth != null && !MazeHandler.labyrinth.isEmpty()) {
            return;
        }
        try {
            MazeHandler.loadMaze(level);
        } catch (Exception e) {
            Arcaneresearch.LOGGER.error("[Arcaneresearch] maze load failed for {}", level.dimension().identifier(), e);
        }
    }

    @SubscribeEvent
    public static void onLevelSave(LevelEvent.Save event) {
        ServerLevel level = outerLevel(event);
        if (level == null) {
            return;
        }
        try {
            MazeHandler.saveMaze(level);
        } catch (Exception e) {
            Arcaneresearch.LOGGER.error("[Arcaneresearch] maze save failed for {}", level.dimension().identifier(), e);
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        ServerLevel level = outerLevel(event);
        if (level == null) {
            return;
        }
        try {
            MazeHandler.saveMaze(level);
        } catch (Exception e) {
            Arcaneresearch.LOGGER.error("[Arcaneresearch] maze unload-save failed for {}",
                    level.dimension().identifier(), e);
        } finally {
            try {
                MazeHandler.clear();
            } catch (Exception e) {
                Arcaneresearch.LOGGER.error("[Arcaneresearch] maze clear failed", e);
            }
        }
    }
}