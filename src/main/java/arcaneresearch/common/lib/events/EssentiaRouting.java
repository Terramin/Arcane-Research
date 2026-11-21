package arcaneresearch.common.lib.events;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/** Pure search/sort rules used by the essentia transport network. */
public final class EssentiaRouting {

    private EssentiaRouting() {
    }

    /** Offsets visited while looking for essentia containers, in scan order. */
    public static List<int[]> offsets(char axis, int stepX, int stepY, int stepZ,
                                      int range, boolean wholeArea) {
        List<int[]> result = new ArrayList<>();
        int start = wholeArea ? -range : 0;
        for (int aa = -range; aa <= range; aa++) {
            for (int bb = -range; bb <= range; bb++) {
                for (int cc = start; cc < range; cc++) {
                    if (aa == 0 && bb == 0 && cc == 0) {
                        continue;
                    }
                    int dx, dy, dz;
                    if (axis == 'Y') {
                        dx = aa;
                        dy = cc * stepY;
                        dz = bb;
                    } else if (axis == 'Z') {
                        dx = aa;
                        dy = bb;
                        dz = cc * stepZ;
                    } else {
                        dx = cc * stepX;
                        dy = aa;
                        dz = bb;
                    }
                    result.add(new int[]{dx, dy, dz});
                }
            }
        }
        return result;
    }

    /** Sorts candidates by squared distance to the given origin (nearest first). */
    public static <T> void sortNearestFirst(List<T> candidates, Function<T, int[]> position,
                                            int baseX, int baseY, int baseZ) {
        candidates.sort(Comparator.comparingDouble(candidate -> {
            int[] pos = position.apply(candidate);
            double dx = pos[0] - baseX;
            double dy = pos[1] - baseY;
            double dz = pos[2] - baseZ;
            return dx * dx + dy * dy + dz * dz;
        }));
    }
}
