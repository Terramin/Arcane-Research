package arcaneresearch.common.world.maze;

import net.minecraft.server.level.ServerLevel;

public class MazeThread implements Runnable {
    private static final int MAX_GENERATION_ATTEMPTS = 1000;

    private final int x;
    private final int z;
    private final int w;
    private final int h;
    private long seed;

    public MazeThread(int x, int z, int w, int h, long seed) {
        this.x = x;
        this.z = z;
        this.w = w;
        this.h = h;
        this.seed = seed;
    }

    public static void generate(ServerLevel level, int chunkX, int chunkZ, int w, int h) {
        if (level == null) {
            return;
        }
        new MazeThread(chunkX, chunkZ, w, h, level.getSeed()).run();
    }

    public static void queue(int chunkX, int chunkZ, int w, int h, long seed) {
        Thread thread = new Thread(new MazeThread(chunkX, chunkZ, w, h, seed),
                "Arcaneresearch-Maze-" + chunkX + "," + chunkZ);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        CellLoc[] markers = {
                new CellLoc(x, z),
                new CellLoc(x - w, z - h),
                new CellLoc(x + w, z + h),
                new CellLoc(x - w, z + h),
                new CellLoc(x + w, z - h)
        };
        for (CellLoc m : markers) {
            MazeHandler.putRaw(m, (short) 0);
        }

        MazeGenerator gen = new MazeGenerator(w, h, seed++);
        int attempts = 0;
        while (!gen.generate()) {
            if (++attempts >= MAX_GENERATION_ATTEMPTS) {
                for (CellLoc m : markers) {
                    clearMarker(m);
                }
                return;
            }
            gen = new MazeGenerator(w, h, seed++);
        }

        int col = x - 1 - w / 2;
        int row = z - 1 - h / 2;
        int[][] grid = gen.grid;
        if (grid != null) {
            for (int a = 0; a < w; a++) {
                for (int b = 0; b < h; b++) {
                    int v = grid[b][a];
                    if (v > 0) {
                        MazeHandler.putRaw(new CellLoc(a + col, b + row), (short) v);
                    }
                }
            }
        }

        for (CellLoc m : markers) {
            clearMarker(m);
        }
    }

    private static void clearMarker(CellLoc loc) {
        if (MazeHandler.getRaw(loc) == 0) {
            MazeHandler.remove(loc);
        }
    }
}