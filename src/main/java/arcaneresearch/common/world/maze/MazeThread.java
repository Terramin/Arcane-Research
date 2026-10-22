package arcaneresearch.common.world.maze;

import net.minecraft.server.level.ServerLevel;

 







 
public class MazeThread implements Runnable {
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
        
        MazeHandler.putRaw(new CellLoc(x, z), (short) 0);
        MazeHandler.putRaw(new CellLoc(x - w, z - h), (short) 0);
        MazeHandler.putRaw(new CellLoc(x + w, z + h), (short) 0);
        MazeHandler.putRaw(new CellLoc(x - w, z + h), (short) 0);
        MazeHandler.putRaw(new CellLoc(x + w, z - h), (short) 0);

        MazeGenerator gen = new MazeGenerator(w, h, seed++);
        while (!gen.generate()) {
            gen = new MazeGenerator(w, h, seed++);
        }

        
        
        
        int col = x - 1 - w / 2;
        int row = z - 1 - h / 2;
        for (int a = 0; a < w; a++) {
            for (int b = 0; b < h; b++) {
                if (gen.grid[b][a] > 0) {
                    MazeHandler.putRaw(new CellLoc(a + col, b + row), (short) gen.grid[b][a]);
                }
            }
        }

        
        clearMarker(new CellLoc(x, z));
        clearMarker(new CellLoc(x - w, z - h));
        clearMarker(new CellLoc(x + w, z + h));
        clearMarker(new CellLoc(x - w, z + h));
        clearMarker(new CellLoc(x + w, z - h));
    }

    private static void clearMarker(CellLoc loc) {
        if (MazeHandler.getRaw(loc) == 0) {
            MazeHandler.remove(loc);
        }
    }
}
