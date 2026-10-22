package arcaneresearch.discovered.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

 






 
public class HexUtils {

    static final int[][] NEIGHBOURS = {{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {-1, 1}, {0, 1}};

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
            return new Pixel(size * 1.5D * this.q, size * Math.sqrt(3.0D) * (this.r + this.q / 2.0D));
        }

        public Hex getNeighbour(int direction) {
            int[] d = NEIGHBOURS[direction];
            return new Hex(this.q + d[0], this.r + d[1]);
        }

        public boolean equals(Hex h) {
            return (h.q == this.q && h.r == this.r);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Hex && equals((Hex) o);
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
            double qq = 0.6666666666666666D * this.x / size;
            double rr = (0.3333333333333333D * Math.sqrt(3.0D) * -this.y - 0.3333333333333333D * this.x) / size;
            return HexUtils.getRoundedHex(qq, rr);
        }
    }

    public static int getDistance(Hex a1, Hex a2) {
        return (Math.abs(a1.q - a2.q) + Math.abs(a1.r - a2.r) + Math.abs(a1.q + a1.r - a2.q - a2.r)) / 2;
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
        Hex h = new Hex(0, 0);
        for (int k = 0; k < radius; k++) {
            h = h.getNeighbour(4);
        }

        ArrayList<Hex> ring = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < radius; j++) {
                ring.add(h);
                h = h.getNeighbour(i);
            }
        }

        return ring;
    }

    public static ArrayList<Hex> distributeRingRandomly(int radius, int entries, Random random) {
        ArrayList<Hex> ring = getRing(radius);
        ArrayList<Hex> results = new ArrayList<>();
        float spacing = ring.size() / entries;
        int start = random.nextInt(ring.size());
        float pos = 0.0F;
        for (int i = 0; i < entries; i++) {
            results.add(ring.get(Math.round(pos)));
            pos += spacing;
        }

        return results;
    }

    public static HashMap<String, Hex> generateHexes(int radius) {
        HashMap<String, Hex> results = new HashMap<>();
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
