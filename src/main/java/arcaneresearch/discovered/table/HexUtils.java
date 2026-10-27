package arcaneresearch.discovered.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class HexUtils {
    static final int[][] NEIGHBOURS = {{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {-1, 1}, {0, 1}};
    private static final double SQRT3 = Math.sqrt(3.0D);
    private static final double HEX_WIDTH_FACTOR = 1.5D;

    public static class Hex {
        public int q = 0;
        public int r = 0;

        public Hex(int q, int r) {
            this.q = q;
            this.r = r;
        }

        public CubicHex toCubicHex() {
            return new CubicHex(this.q, this.r, -this.q - this.r);
        }

        public Pixel toPixel(int size) {
            return new Pixel(HEX_WIDTH_FACTOR * size * this.q,
                    SQRT3 * size * (this.r + this.q / 2.0D));
        }

        public Hex getNeighbour(int direction) {
            int[] d = NEIGHBOURS[direction];
            return new Hex(this.q + d[0], this.r + d[1]);
        }

        public boolean equals(Hex h) {
            return h != null && h.q == this.q && h.r == this.r;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Hex h && h.q == this.q && h.r == this.r;
        }

        @Override
        public int hashCode() {
            return q * 31 + r;
        }

        @Override
        public String toString() {
            return this.q + ":" + this.r;
        }
    }

    public static class CubicHex {
        public int x = 0;
        public int y = 0;
        public int z = 0;

        public CubicHex(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Hex toHex() {
            return new Hex(this.x, this.z);
        }
    }

    public static class Pixel {
        public double x = 0.0D;
        public double y = 0.0D;

        public Pixel(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public Hex toHex(int size) {
            if (size == 0) {
                return new Hex(0, 0);
            }
            double qq = (2.0D / 3.0D) * this.x / size;
            double rr = ((1.0D / 3.0D) * SQRT3 * -this.y - (1.0D / 3.0D) * this.x) / size;
            return HexUtils.getRoundedHex(qq, rr);
        }
    }

    public static int getDistance(Hex a1, Hex a2) {
        if (a1 == null || a2 == null) {
            return Integer.MAX_VALUE;
        }
        int dq = a1.q - a2.q;
        int dr = a1.r - a2.r;
        return (Math.abs(dq) + Math.abs(dr) + Math.abs(dq + dr)) / 2;
    }

    public static Hex getRoundedHex(double qq, double rr) {
        return getRoundedCubicHex(qq, rr, -qq - rr).toHex();
    }

    public static CubicHex getRoundedCubicHex(double xx, double yy, double zz) {
        int rx = (int) Math.round(xx);
        int ry = (int) Math.round(yy);
        int rz = (int) Math.round(zz);
        double xDiff = Math.abs(rx - xx);
        double yDiff = Math.abs(ry - yy);
        double zDiff = Math.abs(rz - zz);
        if (xDiff > yDiff && xDiff > zDiff) {
            rx = -ry - rz;
        } else if (yDiff > zDiff) {
            ry = -rx - rz;
        } else {
            rz = -rx - ry;
        }
        return new CubicHex(rx, ry, rz);
    }

    public static ArrayList<Hex> getRing(int radius) {
        if (radius <= 0) {
            return new ArrayList<>(0);
        }
        Hex h = new Hex(0, 0);
        for (int k = 0; k < radius; k++) {
            h = h.getNeighbour(4);
        }
        ArrayList<Hex> ring = new ArrayList<>(Math.min(6 * radius, 4096));
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < radius; j++) {
                ring.add(h);
                h = h.getNeighbour(i);
            }
        }
        return ring;
    }

    public static ArrayList<Hex> distributeRingRandomly(int radius, int entries, Random random) {
        if (entries <= 0 || random == null) {
            return new ArrayList<>(0);
        }
        ArrayList<Hex> ring = getRing(radius);
        int size = ring.size();
        if (size == 0) {
            return new ArrayList<>(0);
        }
        random.nextInt(size);
        int spacing = size / entries;
        ArrayList<Hex> results = new ArrayList<>(Math.min(entries, size));
        for (int i = 0; i < entries; i++) {
            results.add(ring.get(i * spacing));
        }
        return results;
    }

    public static HashMap<String, Hex> generateHexes(int radius) {
        int rr = Math.max(0, radius);
        long expected = 1L + 3L * rr * (rr + 1L);
        int cap = (int) Math.min(expected * 4L / 3L + 1L, 4096L);
        HashMap<String, Hex> results = new HashMap<>(cap);
        Hex h = new Hex(0, 0);
        results.put(h.toString(), h);
        for (int k = 0; k < radius; k++) {
            h = h.getNeighbour(4);
            Hex hd = new Hex(h.q, h.r);
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j <= k; j++) {
                    results.put(hd.toString(), hd);
                    hd = hd.getNeighbour(i);
                }
            }
        }
        return results;
    }
}