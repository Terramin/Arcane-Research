package arcaneresearch.wards;

import arcaneresearch.client.wards.WardRenderHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Ward: connected texture UVs match TC4 bit layout")
class WardRenderMappingTest {
    private static final BlockPos ORIGIN = new BlockPos(10, 20, 30);

    @SuppressWarnings("unchecked")
    private static float[][][] faces() throws Exception {
        Field field = WardRenderHelper.class.getDeclaredField("FACES");
        field.setAccessible(true);
        return (float[][][]) field.get(null);
    }

    @Test
    void everyFacePlacesBit0AtUvZeroZeroAndCornerBitsAtTheirUvCorners() throws Exception {
        float[][][] faces = faces();
        assertNotNull(faces);
        for (Direction face : Direction.values()) {
            BlockPos[] bits = capturedBits(face);
            Map<String, float[]> corners = corners(faces[face.ordinal()]);
            assertArrayEquals(corner(bits[0], face), corners.get("0,0"),
                    "bit0 / uv(0,0) on " + face);
            assertArrayEquals(corner(bits[2], face), corners.get("1,0"),
                    "bit2 / uv(1,0) on " + face);
            assertArrayEquals(corner(bits[7], face), corners.get("1,1"),
                    "bit7 / uv(1,1) on " + face);
            assertArrayEquals(corner(bits[5], face), corners.get("0,1"),
                    "bit5 / uv(0,1) on " + face);
        }
    }

    /** Records the neighbour offsets that TC4 assigns to bits 0..7. */
    private static BlockPos[] capturedBits(Direction face) {
        BlockPos[] bits = new BlockPos[8];
        int[] index = {0};
        WardRenderHelper.connectionBits(ORIGIN, face, pos -> {
            if (index[0] < bits.length) {
                bits[index[0]++] = new BlockPos(pos.getX() - ORIGIN.getX(),
                        pos.getY() - ORIGIN.getY(), pos.getZ() - ORIGIN.getZ());
            }
            return false;
        });
        return bits;
    }

    private static Map<String, float[]> corners(float[][] face) {
        Map<String, float[]> out = new HashMap<>();
        for (float[] vertex : face) {
            out.put((int) vertex[3] + "," + (int) vertex[4],
                    new float[]{vertex[0], vertex[1], vertex[2]});
        }
        return out;
    }

    /** Converts a neighbouring block offset into the local corner of this face. */
    private static float[] corner(BlockPos neighbour, Direction face) {
        return new float[]{
                axis(neighbour.getX(), face.getAxis() == Direction.Axis.X, face.getStepX()),
                axis(neighbour.getY(), face.getAxis() == Direction.Axis.Y, face.getStepY()),
                axis(neighbour.getZ(), face.getAxis() == Direction.Axis.Z, face.getStepZ())};
    }

    private static float axis(int offset, boolean faceAxis, int step) {
        if (faceAxis) {
            return step > 0 ? 1.0F : 0.0F;
        }
        return offset > 0 ? 1.0F : 0.0F;
    }
}
