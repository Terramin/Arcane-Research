package arcaneresearch.common.world.maze;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MazeHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static ConcurrentHashMap<CellLoc, Short> labyrinth = new ConcurrentHashMap<>();

    private MazeHandler() {
    }

    private static ConcurrentHashMap<CellLoc, Short> map() {
        ConcurrentHashMap<CellLoc, Short> m = labyrinth;
        if (m == null) {
            m = new ConcurrentHashMap<>();
            labyrinth = m;
        }
        return m;
    }

    public static synchronized void put(CellLoc key, Cell cell) {
        if (key == null || cell == null) {
            return;
        }
        map().put(key, cell.pack());
    }

    public static synchronized void putRaw(CellLoc key, short cell) {
        if (key == null) {
            return;
        }
        map().put(key, cell);
    }

    public static synchronized Cell get(CellLoc key) {
        if (key == null) {
            return null;
        }
        Short value = map().get(key);
        return value == null ? null : new Cell(value);
    }

    public static synchronized short getRaw(CellLoc key) {
        if (key == null) {
            return 0;
        }
        Short value = map().get(key);
        return value == null ? 0 : value;
    }

    public static synchronized void remove(CellLoc key) {
        if (key == null) {
            return;
        }
        map().remove(key);
    }

    public static synchronized void clear() {
        map().clear();
    }

    private static void readNBT(CompoundTag nbt) {
        if (nbt == null) {
            return;
        }
        ListTag tagList = nbt.getListOrEmpty("cells");
        for (int a = 0; a < tagList.size(); a++) {
            CompoundTag cell = tagList.getCompoundOrEmpty(a);
            putRaw(new CellLoc(cell.getIntOr("x", 0), cell.getIntOr("z", 0)), cell.getShortOr("cell", (short) 0));
        }
    }

    private static CompoundTag writeNBT() {
        CompoundTag nbt = new CompoundTag();
        ListTag tagList = new ListTag();
        for (Map.Entry<CellLoc, Short> entry : map().entrySet()) {
            CellLoc loc = entry.getKey();
            Short value = entry.getValue();
            if (loc == null || value == null || value <= 0) {
                continue;
            }
            CompoundTag cell = new CompoundTag();
            cell.putInt("x", loc.x);
            cell.putInt("z", loc.z);
            cell.putShort("cell", value);
            tagList.add(cell);
        }
        nbt.put("cells", tagList);
        return nbt;
    }

    private static File mazeFile(ServerLevel level) {
        if (level == null) {
            return null;
        }
        return level.getServer().getWorldPath(LevelResource.ROOT).resolve("labyrinth.dat").toFile();
    }

    public static void loadMaze(ServerLevel level) {
        if (level == null) {
            return;
        }
        clear();
        try {
            File file = mazeFile(level);
            if (file == null || !file.exists()) {
                return;
            }
            CompoundTag root = net.minecraft.nbt.NbtIo.readCompressed(
                    file.toPath(), net.minecraft.nbt.NbtAccounter.unlimitedHeap());
            if (root != null && root.contains("Data")) {
                readNBT(root.getCompoundOrEmpty("Data"));
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to load labyrinth data", e);
        }
    }

    public static void saveMaze(ServerLevel level) {
        if (level == null) {
            return;
        }
        try {
            File file = mazeFile(level);
            if (file == null) {
                return;
            }
            CompoundTag root = new CompoundTag();
            root.put("Data", writeNBT());
            net.minecraft.nbt.NbtIo.writeCompressed(root, file.toPath());
        } catch (Exception e) {
            LOGGER.warn("Failed to save labyrinth data", e);
        }
    }

    public static boolean mazesInRange(int chunkX, int chunkZ, int w, int h) {
        ConcurrentHashMap<CellLoc, Short> m = map();
        for (int x = -w; x <= w; x++) {
            for (int z = -h; z <= h; z++) {
                if (m.get(new CellLoc(chunkX + x, chunkZ + z)) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void generateEldritch(LevelAccessor level, RandomSource random, int cx, int cz) {
        if (level == null || random == null) {
            return;
        }
        Cell cell = get(new CellLoc(cx, cz));
        if (cell == null) {
            return;
        }
        MazeRooms.build(level, random, cx, cz, MazeFeature.FLOOR_Y, cell);
    }
}