package thaumictinker.client.geom;

import org.joml.Matrix4f;
import org.joml.Vector3f;

 



 
public final class SpinningCubesGeometry {

    public static final int CUBES = 12;
    public static final int TRAILS = 5;
    public static final float CUBE_SIZE = 1.0F / 16.0F;

    private static final float MODIFIER = 6.0F;
    private static final float ROTATION_MODIFIER = 0.25F;
    private static final float RADIUS_BASE = 0.7F;
    private static final float RADIUS_MOD = 0.1F;
    private static final float TRAIL_DELAY = 0.75F;

    @FunctionalInterface
    public interface ModelQuadSink {
        void vertex(float x, float y, float z, float u, float v);
    }

    private SpinningCubesGeometry() {
    }

    public static void build(double ticks, ModelQuadSink solid, ModelQuadSink trail) {
        for (int layer = 0; layer <= TRAILS; layer++) {
            layer(ticks - TRAIL_DELAY * layer, layer == 0 ? solid : trail);
        }
    }

    private static void layer(double ticks, ModelQuadSink sink) {
        float degreesPerCube = 360 / CUBES;
        Matrix4f matrix = new Matrix4f();
        Vector3f axis = new Vector3f();
        for (int index = 0; index < CUBES; index++) {
            float degrees = (float) (ticks / ROTATION_MODIFIER % 360.0 + degreesPerCube * index);
            float radians = (float) Math.toRadians(degrees);
            float radiusX = (float) (RADIUS_BASE + RADIUS_MOD * Math.sin(ticks / MODIFIER));
            float radiusZ = (float) (RADIUS_BASE + RADIUS_MOD * Math.cos(ticks / MODIFIER));
            float x = radiusX * (float) Math.cos(radians);
            float z = radiusZ * (float) Math.sin(radians);
            float y = (float) Math.cos((ticks + 50 * index) / 5.0F) / 10.0F;
            axis.set(
                    (float) Math.sin(ticks * ROTATION_MODIFIER) / 2.0F,
                    (float) Math.max(0.6F, Math.sin(ticks * 0.1F) / 2.0F + 0.5F),
                    (float) Math.cos(ticks * ROTATION_MODIFIER) / 2.0F
            ).normalize();
            matrix.identity()
                    .translate(-0.025F, 0.85F, -0.025F)
                    .translate(x, y, z)
                    .rotate(radians, axis);
            cube(matrix, sink);
        }
    }

    private static void cube(Matrix4f matrix, ModelQuadSink sink) {
        float s = CUBE_SIZE;
        quad(matrix, sink, 0, s, s, s, s, s, s, s, 0, 0, s, 0);
        quad(matrix, sink, 0, 0, 0, s, 0, 0, s, 0, s, 0, 0, s);
        quad(matrix, sink, 0, 0, s, s, 0, s, s, s, s, 0, s, s);
        quad(matrix, sink, s, 0, 0, 0, 0, 0, 0, s, 0, s, s, 0);
        quad(matrix, sink, s, 0, s, s, 0, 0, s, s, 0, s, s, s);
        quad(matrix, sink, 0, 0, 0, 0, 0, s, 0, s, s, 0, s, 0);
    }

    private static void quad(Matrix4f matrix, ModelQuadSink sink,
                             float x0, float y0, float z0, float x1, float y1, float z1,
                             float x2, float y2, float z2, float x3, float y3, float z3) {
        Vector3f point = new Vector3f();
        vertex(matrix, sink, point.set(x0, y0, z0));
        vertex(matrix, sink, point.set(x1, y1, z1));
        vertex(matrix, sink, point.set(x2, y2, z2));
        vertex(matrix, sink, point.set(x3, y3, z3));
    }

    private static void vertex(Matrix4f matrix, ModelQuadSink sink, Vector3f point) {
        matrix.transformPosition(point);
        sink.vertex(point.x, point.y, point.z, 0.5F, 0.5F);
    }
}
