package arcaneresearch.common.world.maze;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.io.File;
import net.minecraft.util.RandomSource;
import java.util.concurrent.ConcurrentHashMap;

 




 
public final class MazeHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static ConcurrentHashMap<CellLoc, Short> labyrinth = new ConcurrentHashMap<>();

    private MazeHandler() {
    }

    public static synchronized void put(CellLoc key, Cell cell) {
        labyrinth.put(key, cell.pack());
    }

    public static synchronized void putRaw(CellLoc key, short cell) {
        labyrinth.put(key, cell);
    }

    public static synchronized Cell get(CellLoc key) {
        return labyrinth.containsKey(key) ? new Cell(labyrinth.get(key)) : null;
    }

    public static synchronized short getRaw(CellLoc key) {
        return labyrinth.containsKey(key) ? labyrinth.get(key) : 0;
    }

    public static synchronized void remove(CellLoc key) {
        labyrinth.remove(key);
    }

    public static synchronized void clear() {
        labyrinth.clear();
    }

    private static void readNBT(CompoundTag nbt) {
        ListTag tagList = nbt.getListOrEmpty("cells");
        for (int a = 0; a < tagList.size(); a++) {
            CompoundTag cell = tagList.getCompoundOrEmpty(a);
            putRaw(new CellLoc(cell.getIntOr("x", 0), cell.getIntOr("z", 0)), cell.getShortOr("cell", (short) 0));
        }
    }

    private static CompoundTag writeNBT() {
        CompoundTag nbt = new CompoundTag();
        ListTag tagList = new ListTag();
        for (CellLoc loc : labyrinth.keySet()) {
            short value = getRaw(loc);
            if (value > 0) {
                CompoundTag cell = new CompoundTag();
                cell.putInt("x", loc.x);
                cell.putInt("z", loc.z);
                cell.putShort("cell", value);
                tagList.add(cell);
            }
        }
        nbt.put("cells", tagList);
        return nbt;
    }

    private static File mazeFile(ServerLevel level) {
        return level.getServer().getWorldPath(LevelResource.ROOT).resolve("labyrinth.dat").toFile();
    }

    public static void loadMaze(ServerLevel level) {
        
        
        clear();
        try {
            java.nio.file.Path file = mazeFile(level).toPath();
            if (java.nio.file.Files.exists(file)) {
                CompoundTag root = net.minecraft.nbt.NbtIo.readCompressed(
                        file, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
                if (root != null && root.contains("Data")) {
                    readNBT(root.getCompoundOrEmpty("Data"));
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to load labyrinth data", e);
        }
    }

    public static void saveMaze(ServerLevel level) {
        try {
            CompoundTag root = new CompoundTag();
            root.put("Data", writeNBT());
            
            net.minecraft.nbt.NbtIo.writeCompressed(root, mazeFile(level).toPath());
        } catch (Exception e) {
            LOGGER.warn("Failed to save labyrinth data", e);
        }
    }

    public static boolean mazesInRange(int chunkX, int chunkZ, int w, int h) {
        for (int x = -w; x <= w; x++) {
            for (int z = -h; z <= h; z++) {
                if (get(new CellLoc(chunkX + x, chunkZ + z)) != null) {
                    return true;
                }
            }
        }
        return false;
    }

     






 
    public static void generateEldritch(LevelAccessor level, RandomSource random, int cx, int cz) {
        Cell cell = get(new CellLoc(cx, cz));
        if (cell == null) {
            return;
        }
        MazeRooms.build(level, random, cx, cz, MazeFeature.FLOOR_Y, cell);
    }
}
