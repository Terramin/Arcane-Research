package arcaneresearch.common.world.maze;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.minecraft.util.RandomSource;
public class MazeGenerator {
    int width = 0;
    int height = 0;
    long seed = 0L;
    RandomSource rand = null;
    public int[][] grid;
    public static final int N = 1;
    public static final int S = 2;
    public static final int E = 4;
    public static final int W = 8;
    public static final int A = 16;
    public static final int B = 32;
    public static int getOPP(int in) {
        switch (in) {
            case 1:
                return 2;
            case 2:
                return 1;
            case 4:
                return 8;
            case 8:
                return 4;
            default:
                return -99;
        }
    }
    public static int getDX(int in) {
        switch (in) {
            case 1:
                return 0;
            case 2:
                return 0;
            case 4:
                return 1;
            case 8:
                return -1;
            default:
                return -99;
        }
    }
    public static int getDY(int in) {
        switch (in) {
            case 1:
                return -1;
            case 2:
                return 1;
            case 4:
                return 0;
            case 8:
                return 0;
            default:
                return -99;
        }
    }
    public MazeGenerator(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.seed = seed;
        this.rand = RandomSource.create(seed);
        this.grid = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                this.grid[y][x] = 0;
            }
        }
    }
    private static class Loc {
        int x;
        int y;
        Loc(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    public boolean generate() {
        int bx = 0;
        int by = 0;
        switch (this.rand.nextInt(4)) {
            case 0:
                bx = 0;
                by = 0;
                break;
            case 1:
                bx = this.width - 2;
                by = this.height - 2;
                break;
            case 2:
                bx = this.width - 2;
                by = 0;
                break;
            case 3:
                bx = 0;
                by = this.height - 2;
                break;
            default:
                break;
        }
        this.grid[by][bx] = 512;
        this.grid[by][bx + 1] = 768;
        this.grid[by + 1][bx] = 1024;
        this.grid[by + 1][bx + 1] = 1280;
        int px = 1 + this.width / 2;
        int py = 1 + this.height / 2;
        this.grid[py][px] = 256;
        ArrayList<Loc> cells = new ArrayList<>();
        int l = (this.width + this.height) / 4;
        for (int z = 0; z < l; z++) {
            int w = 1 + this.rand.nextInt(3);
            if (w > 2) {
                l--;
            }
            int qq = this.rand.nextInt(this.width - w);
            int ww = this.rand.nextInt(this.height - w);
            for (int a = qq; a < qq + w; a++) {
                for (int b = ww; b < ww + w; b++) {
                    if (this.grid[b][a] == 0) {
                        this.grid[b][a] = -1;
                    }
                }
            }
        }
        List<Integer> directions = Arrays.asList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(4), Integer.valueOf(8));
        java.util.Collections.shuffle(directions, new java.util.Random(this.rand.nextLong()));
        int xx = px + getDX(directions.get(0).intValue());
        int yy = py + getDY(directions.get(0).intValue());
        this.grid[py][px] = this.grid[py][px] | directions.get(0).intValue();
        if (this.grid[yy][xx] < 0) {
            this.grid[yy][xx] = 0;
        }
        this.grid[yy][xx] = this.grid[yy][xx] | getOPP(directions.get(0).intValue());
        cells.add(new Loc(xx, yy));
        boolean success = false;
        while (!cells.isEmpty()) {
            int index = getNextIndex(cells.size());
            int x = cells.get(index).x;
            int y = cells.get(index).y;
            java.util.Collections.shuffle(directions, new java.util.Random(this.rand.nextLong()));
            boolean carved = false;
            for (Iterator<Integer> iterator = directions.iterator(); iterator.hasNext();) {
                int dir = iterator.next().intValue();
                int nx = x + getDX(dir);
                int ny = y + getDY(dir);
                if (0 < nx && nx < this.width - 1 && 0 < ny && ny < this.height - 1) {
                    if (this.grid[ny][nx] == 0) {
                        this.grid[y][x] = this.grid[y][x] | dir;
                        this.grid[ny][nx] = this.grid[ny][nx] | getOPP(dir);
                        cells.add(new Loc(nx, ny));
                        carved = true;
                    }
                    if (carved) {
                        success = true;
                        break;
                    }
                }
            }
            if (!carved) {
                cells.remove(index);
            }
        }
        if (!success) {
            return false;
        }
        for (int aa = 0; aa < this.height; aa++) {
            for (int bb = 0; bb < this.width; bb++) {
                if (this.grid[aa][bb] < 0) {
                    this.grid[aa][bb] = 0;
                }
            }
        }
        java.util.Collections.shuffle(directions, new java.util.Random(this.rand.nextLong()));
        for (Iterator<Integer> i$ = directions.iterator(); i$.hasNext();) {
            int dir = i$.next().intValue();
            int nx = px + getDX(dir);
            int ny = py + getDY(dir);
            if (0 < nx && nx < this.width - 1 && 0 < ny && ny < this.height - 1 && this.grid[ny][nx] > 0 && this.rand.nextBoolean()) {
                this.grid[ny][nx] = this.grid[ny][nx] | getOPP(dir);
                this.grid[py][px] = this.grid[py][px] | dir;
            }
        }
        java.util.Collections.shuffle(directions, new java.util.Random(this.rand.nextLong()));
        boolean connected = false;
        int ax;
        outer:
        for (ax = 0; ax < 2; ax++) {
            for (int ay = 0; ay < 2; ay++) {
                for (Iterator<Integer> iterator = directions.iterator(); iterator.hasNext();) {
                    int dir = iterator.next().intValue();
                    int nx = bx + ax + getDX(dir);
                    int ny = by + ay + getDY(dir);
                    if (0 < nx && nx < this.width - 1 && 0 < ny && ny < this.height - 1 && this.grid[ny][nx] > 0
                            && new Cell((short) this.grid[ny][nx]).feature == 0) {
                        this.grid[ny][nx] = this.grid[ny][nx] | getOPP(dir);
                        this.grid[by + ay][bx + ax] = this.grid[by + ay][bx + ax] | dir;
                        connected = true;
                        break outer;
                    }
                }
            }
        }
        if (!connected) {
            List<Integer> directions2 = Arrays.asList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(4), Integer.valueOf(8));
            java.util.Collections.shuffle(directions2, new java.util.Random(this.rand.nextLong()));
            success = false;
            outer2:
            for (int m = 0; m < 2; m++) {
                for (int ay = 0; ay < 2; ay++) {
                    for (Iterator<Integer> iterator = directions2.iterator(); iterator.hasNext();) {
                        int dir2 = iterator.next().intValue();
                        int qx = bx + m + getDX(dir2);
                        int qy = by + ay + getDY(dir2);
                        if (0 < qx && qx < this.width - 1 && 0 < qy && qy < this.height - 1 && this.grid[qy][qx] == 0) {
                            cells.add(new Loc(qx, qy));
                            while (!cells.isEmpty()) {
                                int index = getNextIndex(cells.size());
                                int x = cells.get(index).x;
                                int y = cells.get(index).y;
                                java.util.Collections.shuffle(directions, new java.util.Random(this.rand.nextLong()));
                                boolean carved = false;
                                for (Iterator<Integer> iterator1 = directions.iterator(); iterator1.hasNext();) {
                                    int dir = iterator1.next().intValue();
                                    int nx = x + getDX(dir);
                                    int ny = y + getDY(dir);
                                    if (0 < nx && nx < this.width - 1 && 0 < ny && ny < this.height - 1) {
                                        if (this.grid[ny][nx] == 0) {
                                            this.grid[y][x] = this.grid[y][x] | dir;
                                            this.grid[y][x] = this.grid[y][x] | 0x6300;
                                            this.grid[ny][nx] = this.grid[ny][nx] | getOPP(dir);
                                            this.grid[ny][nx] = this.grid[ny][nx] | 0x6300;
                                            cells.add(new Loc(nx, ny));
                                            carved = true;
                                        } else if (new Cell((short) this.grid[ny][nx]).feature == 0) {
                                            this.grid[y][x] = this.grid[y][x] | dir;
                                            this.grid[ny][nx] = this.grid[ny][nx] | getOPP(dir);
                                            this.grid[qy][qx] = this.grid[qy][qx] | getOPP(dir2);
                                            this.grid[by + ay][bx + m] = this.grid[by + ay][bx + m] | dir2;
                                            success = true;
                                            break outer2;
                                        }
                                        if (carved) {
                                            break;
                                        }
                                    }
                                }
                                if (!carved) {
                                    cells.remove(index);
                                }
                            }
                        }
                    }
                }
            }
            if (!success) {
                return false;
            }
        }
        for (int i = 0; i < this.height; i++) {
            for (int bb = 0; bb < this.width; bb++) {
                Cell cell = new Cell((short) this.grid[i][bb]);
                if (cell.feature == 99) {
                    cell.feature = 0;
                    this.grid[i][bb] = cell.pack();
                }
            }
        }
        ArrayList<CellLoc> deadEndsloc = new ArrayList<>();
        for (int k = 0; k < this.height; k++) {
            for (int bb = 0; bb < this.width; bb++) {
                Cell cell = new Cell((short) this.grid[k][bb]);
                int exits = (cell.north ? 1 : 0) + (cell.south ? 1 : 0) + (cell.east ? 1 : 0) + (cell.west ? 1 : 0);
                if (exits == 1 && cell.feature == 0) {
                    deadEndsloc.add(new CellLoc(k, bb));
                }
            }
        }
        if (deadEndsloc.size() == 0) {
            return false;
        }
        int r = this.rand.nextInt(deadEndsloc.size());
        CellLoc ll = deadEndsloc.get(r);
        Cell c = new Cell((short) this.grid[ll.x][ll.z]);
        c.feature = 6;
        this.grid[ll.x][ll.z] = c.pack();
        deadEndsloc.remove(r);
        if (deadEndsloc.size() > 0) {
            int count = 0;
            while (count < deadEndsloc.size() * 0.75D) {
                int m = this.rand.nextInt(deadEndsloc.size());
                CellLoc cellLoc = deadEndsloc.get(m);
                Cell cell = new Cell((short) this.grid[cellLoc.x][cellLoc.z]);
                if (cell.feature == 0) {
                    switch (this.rand.nextInt(5)) {
                        case 0:
                            cell.feature = 7;
                            break;
                        case 1:
                            cell.feature = 8;
                            break;
                        case 2:
                            cell.feature = 9;
                            break;
                        default:
                            cell.feature = 11;
                            break;
                    }
                    this.grid[cellLoc.x][cellLoc.z] = cell.pack();
                    deadEndsloc.remove(m);
                    count++;
                }
            }
        }
        for (int j = 0; j < this.height; j++) {
            for (int bb = 0; bb < this.width; bb++) {
                c = new Cell((short) this.grid[j][bb]);
                if (c.feature == 0 && (c.north || c.south || c.west || c.east) && this.rand.nextBoolean()) {
                    switch (this.rand.nextInt(10)) {
                        case 0:
                            c.feature = 8;
                            break;
                        case 1:
                        case 2:
                            c.feature = 10;
                            break;
                        case 3:
                            c.feature = 21;
                            break;
                        case 4:
                            c.feature = 22;
                            break;
                        case 5:
                            c.feature = 23;
                            break;
                        case 6:
                            c.feature = 24;
                            break;
                        default:
                            c.feature = 11;
                            break;
                    }
                    this.grid[j][bb] = c.pack();
                }
            }
        }
        return true;
    }
    private int getNextIndex(int ceil) {
        float r = this.rand.nextFloat();
        if (r <= 0.45F) {
            return ceil - 1;
        }
        if (r <= 0.9F) {
            return this.rand.nextInt(ceil);
        }
        return 0;
    }
}
