package arcaneresearch.common.lib.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class StreamPathHelper {

    private static final double[] ARC_TS = {0.25, 0.5, 0.75};

    private StreamPathHelper() {
    }

    public static List<Vec3> findPath(Level level, Vec3 start, Vec3 end) {
        if (start == null || end == null) {
            return new ArrayList<>(0);
        }

        List<Vec3> straight = new ArrayList<>(2);
        straight.add(start);
        straight.add(end);

        if (level == null) return straight;

        if (isClear(level, start, end)) {
            double dist = start.distanceTo(end);
            if (dist <= 3.0) return straight;
            return arcedPath(start, end, dist);
        }

        double top = Math.max(start.y, end.y);
        for (int h = 1; h <= 12; h++) {
            double y = top + h;
            Vec3 elbow1 = new Vec3(start.x, y, start.z);
            Vec3 elbow2 = new Vec3(end.x, y, end.z);
            if (isClear(level, start, elbow1)
                    && isClear(level, elbow1, elbow2)
                    && isClear(level, elbow2, end)) {
                List<Vec3> path = new ArrayList<>(4);
                path.add(start);
                path.add(elbow1);
                path.add(elbow2);
                path.add(end);
                return smoothCorners(path);
            }
        }
        return straight;
    }

    private static List<Vec3> arcedPath(Vec3 start, Vec3 end, double dist) {
        double h = Math.min((dist - 2.0) * 0.3, 5.0);
        double cx = (start.x + end.x) * 0.5;
        double cz = (start.z + end.z) * 0.5;
        double cy = Math.max(start.y, end.y) + h * 2.0;

        List<Vec3> path = new ArrayList<>(5);
        path.add(start);
        for (double t : ARC_TS) {
            double u = 1.0 - t;
            path.add(new Vec3(
                    u * u * start.x + 2.0 * u * t * cx + t * t * end.x,
                    u * u * start.y + 2.0 * u * t * cy + t * t * end.y,
                    u * u * start.z + 2.0 * u * t * cz + t * t * end.z));
        }
        path.add(end);
        return smoothCorners(path);
    }

    private static List<Vec3> smoothCorners(List<Vec3> raw) {
        if (raw == null || raw.size() < 3) return raw;

        int n = raw.size();
        List<Vec3> out = new ArrayList<>(n * 3);
        out.add(raw.get(0));

        for (int i = 1; i < n - 1; i++) {
            Vec3 prev = raw.get(i - 1);
            Vec3 cur = raw.get(i);
            Vec3 next = raw.get(i + 1);
            if (prev == null || cur == null || next == null) {
                out.add(cur);
                continue;
            }

            double d1x = cur.x - prev.x;
            double d1y = cur.y - prev.y;
            double d1z = cur.z - prev.z;
            double d2x = next.x - cur.x;
            double d2y = next.y - cur.y;
            double d2z = next.z - cur.z;

            double l1 = Math.sqrt(d1x * d1x + d1y * d1y + d1z * d1z);
            double l2 = Math.sqrt(d2x * d2x + d2y * d2y + d2z * d2z);
            if (l1 < 1.0e-6 || l2 < 1.0e-6) {
                out.add(cur);
                continue;
            }

            double dot = (d1x * d2x + d1y * d2y + d1z * d2z) / (l1 * l2);
            if (dot > 0.995) {
                out.add(cur);
                continue;
            }

            double r = Math.min(0.75, Math.min(l1, l2) * 0.45);
            Vec3 a = new Vec3(
                    cur.x - d1x / l1 * r,
                    cur.y - d1y / l1 * r,
                    cur.z - d1z / l1 * r);
            Vec3 b = new Vec3(
                    cur.x + d2x / l2 * r,
                    cur.y + d2y / l2 * r,
                    cur.z + d2z / l2 * r);

            out.add(a);
            for (int k = 1; k <= 3; k++) {
                double t = k / 4.0;
                double u = 1.0 - t;
                out.add(new Vec3(
                        u * u * a.x + 2.0 * u * t * cur.x + t * t * b.x,
                        u * u * a.y + 2.0 * u * t * cur.y + t * t * b.y,
                        u * u * a.z + 2.0 * u * t * cur.z + t * t * b.z));
            }
            out.add(b);
        }
        out.add(raw.get(n - 1));
        return out;
    }

    public static int travelTicks(Level level, Vec3 start, Vec3 end) {
        return travelTicks(findPath(level, start, end));
    }

    public static int travelTicks(List<Vec3> path) {
        if (path == null) return 1;
        return Math.max(1, (int) (pathLength(path) * 21.0));
    }

    public static double pathLength(List<Vec3> path) {
        if (path == null || path.size() < 2) return 0.0;

        double len = 0.0;
        int n = path.size() - 1;
        for (int i = 0; i < n; i++) {
            Vec3 a = path.get(i);
            Vec3 b = path.get(i + 1);
            if (a == null || b == null) continue;
            len += a.distanceTo(b);
        }
        return len;
    }

    public static double headDistance(double pathLen, int duration, int elapsed) {
        double headTime = duration * 0.75;
        double t = headTime <= 0.0 ? 1.0 : Math.min(1.0, elapsed / headTime);
        double s = t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
        return pathLen * s;
    }

    public static Blockage firstBlockedCell(Level level, List<Vec3> path) {
        if (level == null || path == null || path.size() < 2) return null;

        Vec3 first = path.get(0);
        Vec3 last = path.get(path.size() - 1);
        if (first == null || last == null) return null;

        BlockPos startCell = BlockPos.containing(first.x, first.y, first.z);
        BlockPos endCell = BlockPos.containing(last.x, last.y, last.z);

        double walked = 0.0;
        BlockPos.MutableBlockPos cell = new BlockPos.MutableBlockPos();
        int n = path.size() - 1;

        for (int i = 0; i < n; i++) {
            Vec3 a = path.get(i);
            Vec3 b = path.get(i + 1);
            if (a == null || b == null) continue;

            double seg = a.distanceTo(b);
            int steps = Math.max(1, (int) (seg * 4.0));
            double dx = b.x - a.x;
            double dy = b.y - a.y;
            double dz = b.z - a.z;

            for (int s = 1; s < steps; s++) {
                double t = (double) s / (double) steps;
                cell.set(
                        Mth.floor(a.x + dx * t),
                        Mth.floor(a.y + dy * t),
                        Mth.floor(a.z + dz * t));
                if (cell.equals(startCell) || cell.equals(endCell)) continue;
                if (!level.getBlockState(cell).getCollisionShape(level, cell).isEmpty()) {
                    return new Blockage(cell.immutable(), walked + seg * t);
                }
            }
            walked += seg;
        }
        return null;
    }

    private static boolean isClear(Level level, Vec3 start, Vec3 end) {
        if (level == null || start == null || end == null) return true;

        double dist = start.distanceTo(end);
        if (dist < 1.0e-6) return true;

        BlockPos startCell = BlockPos.containing(start.x, start.y, start.z);
        BlockPos endCell = BlockPos.containing(end.x, end.y, end.z);

        int steps = Math.max(1, (int) (dist * 4.0));
        double dx = end.x - start.x;
        double dy = end.y - start.y;
        double dz = end.z - start.z;

        BlockPos.MutableBlockPos cell = new BlockPos.MutableBlockPos();
        for (int i = 1; i < steps; i++) {
            double t = (double) i / (double) steps;
            cell.set(
                    Mth.floor(start.x + dx * t),
                    Mth.floor(start.y + dy * t),
                    Mth.floor(start.z + dz * t));
            if (cell.equals(startCell) || cell.equals(endCell)) continue;
            if (!level.getBlockState(cell).getCollisionShape(level, cell).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public record Blockage(BlockPos cell, double dist) {
    }
}