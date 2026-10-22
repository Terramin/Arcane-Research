package thaumictinker.client.geom;

 










 
public final class WingsGeometry {

      
    public interface Sink {
        void vertex(float x, float y, float z, float u, float v,
                    float nx, float ny, float nz, int argb, int light);
    }

    public static final float LEFT_PIVOT_X = -2.0F;
    public static final float RIGHT_PIVOT_X = 2.0F;
    public static final float PIVOT_Y = 1.0F;
    public static final float PIVOT_Z = 2.0F;
    public static final float LEFT_REST_ANGLE = -0.6108652F;
    public static final float RIGHT_REST_ANGLE = 0.4468043F;
    public static final float FLAP_AMPLITUDE = (float) (Math.PI / 180.0 * 15.0);
    public static final float PIXEL = 1.0F / 16.0F;

    private static final float TEXTURE_WIDTH = 64.0F;
    private static final float TEXTURE_HEIGHT = 32.0F;
    private static final int TEXTURE_U = 36;
    private static final int TEXTURE_V = -12;
    private static final int HEIGHT = 7;
    private static final int DEPTH = 12;

    private static final int[][] QUADS = {
        {5, 1, 2, 6},
        {0, 4, 7, 3}
    };
    private static final float[][] NORMALS = {
        {1.0F, 0.0F, 0.0F},
        {-1.0F, 0.0F, 0.0F}
    };

    private WingsGeometry() {
    }

    public static float leftAngle(boolean flying, float ageInTicks) {
        return flying ? (float) ((Math.sin(ageInTicks) + 1.0) * FLAP_AMPLITUDE + LEFT_REST_ANGLE) : LEFT_REST_ANGLE;
    }

    public static float rightAngle(boolean flying, float ageInTicks) {
        return flying ? -leftAngle(true, ageInTicks) : RIGHT_REST_ANGLE;
    }

    public static void leftWing(Sink sink, int argb, int light) {
        box(sink, 0.0F, argb, light);
    }

    public static void rightWing(Sink sink, int argb, int light) {
        box(sink, 0.1F, argb, light);
    }

    private static void box(Sink sink, float offsetX, int argb, int light) {
        float[][] corners = {
            {offsetX, 0, 0},
            {offsetX, 0, 0},
            {offsetX, HEIGHT, 0},
            {offsetX, HEIGHT, 0},
            {offsetX, 0, DEPTH},
            {offsetX, 0, DEPTH},
            {offsetX, HEIGHT, DEPTH},
            {offsetX, HEIGHT, DEPTH}
        };
        int[][] regions = {
            {TEXTURE_U + DEPTH, TEXTURE_V + DEPTH, TEXTURE_U + DEPTH + DEPTH, TEXTURE_V + DEPTH + HEIGHT},
            {TEXTURE_U, TEXTURE_V + DEPTH, TEXTURE_U + DEPTH, TEXTURE_V + DEPTH + HEIGHT}
        };
        for (int quad = 0; quad < QUADS.length; quad++) {
            int[] indices = QUADS[quad];
            int[] region = regions[quad];
            float[][] uvs = {
                {region[2] / TEXTURE_WIDTH, region[1] / TEXTURE_HEIGHT},
                {region[0] / TEXTURE_WIDTH, region[1] / TEXTURE_HEIGHT},
                {region[0] / TEXTURE_WIDTH, region[3] / TEXTURE_HEIGHT},
                {region[2] / TEXTURE_WIDTH, region[3] / TEXTURE_HEIGHT}
            };
            float[] first = corners[indices[0]];
            float[] second = corners[indices[1]];
            float[] fourth = corners[indices[3]];
            float firstY = second[1] - first[1];
            float firstZ = second[2] - first[2];
            float secondY = fourth[1] - first[1];
            float secondZ = fourth[2] - first[2];
            float normalX = firstY * secondZ - firstZ * secondY;
            boolean outward = normalX * NORMALS[quad][0] >= 0.0F;
            float nx = NORMALS[quad][0];
            float ny = NORMALS[quad][1];
            float nz = NORMALS[quad][2];
            for (int step = 0; step < 4; step++) {
                int vertex = outward ? step : (4 - step) % 4;
                float[] corner = corners[indices[vertex]];
                sink.vertex(corner[0] * PIXEL, corner[1] * PIXEL, corner[2] * PIXEL,
                        uvs[vertex][0], uvs[vertex][1], nx, ny, nz, argb, light);
            }
        }
    }
}
