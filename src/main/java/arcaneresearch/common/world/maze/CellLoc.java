package arcaneresearch.common.world.maze;

public class CellLoc implements Comparable<CellLoc> {
    public int x;
    public int z;

    public CellLoc() {
    }

    public CellLoc(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public CellLoc(CellLoc c) {
        if (c != null) {
            this.x = c.x;
            this.z = c.z;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CellLoc other)) {
            return false;
        }
        return this.x == other.x && this.z == other.z;
    }

    @Override
    public int hashCode() {
        return this.x * 1664525 + this.z * 1013904223;
    }

    @Override
    public int compareTo(CellLoc c) {
        int zCmp = Integer.compare(this.z, c.z);
        return zCmp != 0 ? zCmp : Integer.compare(this.x, c.x);
    }

    public void set(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public float getDistanceSquared(int x, int z) {
        float f = (float) this.x - x;
        float f2 = (float) this.z - z;
        return f * f + f2 * f2;
    }

    public float getDistanceSquaredToChunkCoordinates(CellLoc c) {
        if (c == null) {
            return Float.POSITIVE_INFINITY;
        }
        return getDistanceSquared(c.x, c.z);
    }

    @Override
    public String toString() {
        return "Pos{x=" + this.x + ", z=" + this.z + '}';
    }
}