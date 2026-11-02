package arcaneresearch.common.lib.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class HexUtils {

    private static final int[][] NEIGHBOURS = {
            {1, 0}, {1, -1}, {0, -1}, {-1, 0}, {-1, 1}, {0, 1}
    };

    public static int getDistance(Hex a1, Hex a2) {
        return (Math.abs(a1.q - a2.q) + Math.abs(a1.r - a2.r)
                + Math.abs(a1.q + a1.r - a2.q - a2.r)) / 2;
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
        if (radius <= 0) return new ArrayList<>(0);

        Hex h = new Hex(0, 0);
        for (int k = 0; k < radius; k++) {
            h = h.getNeighbour(4);
        }

        ArrayList<Hex> ring = new ArrayList<>(6 * radius);
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < radius; j++) {
                ring.add(h);
                h = h.getNeighbour(i);
            }
        }
        return ring;
    }

    public static ArrayList<Hex> distributeRingRandomly(int radius, int entries, Random random) {
        if (entries <= 0 || random == null) return new ArrayList<>(0);

        ArrayList<Hex> ring = getRing(radius);
        if (ring.isEmpty()) return new ArrayList<>(0);

        ArrayList<Hex> results = new ArrayList<>(entries);
        float spacing = ring.size() / (float) entries;
        float pos = random.nextInt(ring.size());

        for (int i = 0; i < entries; i++) {
            results.add(ring.get((int) pos));
            pos += spacing;
            if (pos >= ring.size()) {
                pos -= ring.size();
            }
        }
        return results;
    }

    public static HashMap<String, Hex> generateHexes(int radius) {
        if (radius <= 0) radius = 0;

        HashMap<String, Hex> results = new HashMap<>(3 * radius * (radius + 1) + 1);
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

    public static class Hex {
        public int q;
        public int r;

        public Hex(int q, int r) {
            this.q = q;
            this.r = r;
        }

        public static Hex fromString(String hs) {
            if (hs == null) return null;
            String[] ss = hs.split(":");
            if (ss.length != 2) return null;
            try {
                int q = Integer.parseInt(ss[0]);
                int r = Integer.parseInt(ss[1]);
                return new Hex(q, r);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        public CubicHex toCubicHex() {
            return new CubicHex(q, r, -q - r);
        }

        public Pixel toPixel(int size) {
            return new Pixel(size * 1.5 * q, size * Math.sqrt(3.0) * (r + q / 2.0));
        }

        public Hex getNeighbour(int direction) {
            if (direction < 0 || direction >= NEIGHBOURS.length) {
                return new Hex(q, r);
            }
            int[] d = NEIGHBOURS[direction];
            return new Hex(q + d[0], r + d[1]);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Hex other)) return false;
            return q == other.q && r == other.r;
        }

        @Override
        public int hashCode() {
            return Objects.hash(q, r);
        }

        @Override
        public String toString() {
            return q + ":" + r;
        }
    }

    public static class CubicHex {
        public int x;
        public int y;
        public int z;

        public CubicHex(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Hex toHex() {
            return new Hex(x, z);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CubicHex other)) return false;
            return x == other.x && y == other.y && z == other.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }
    }

    public static class Pixel {
        public double x;
        public double y;

        public Pixel(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public Hex toHex(int size) {
            if (size == 0) return new Hex(0, 0);
            double qq = 0.6666666666666666 * x / size;
            double rr = (0.3333333333333333 * Math.sqrt(3.0) * -y - 0.3333333333333333 * x) / size;
            return HexUtils.getRoundedHex(qq, rr);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pixel other)) return false;
            return Double.compare(x, other.x) == 0 && Double.compare(y, other.y) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
}