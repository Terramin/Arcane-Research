package thaumictinker.client.attractor;

 



 
public final class AttractorGeometry {

    @FunctionalInterface
    public interface ModelSink {
        void vertex(float x, float y, float z, float u, float v);
    }

    private static final float TEXTURE_SIZE = 64.0F;
    private static final float HALF_PI = 1.570796F;
    private static final int[][] QUADS = {
            {5, 1, 2, 6},
            {0, 4, 7, 3},
            {5, 4, 0, 1},
            {2, 3, 7, 6},
            {1, 0, 3, 2},
            {4, 5, 6, 7}
    };

    private record Part(int u, int v, float x, float y, float z, int width, int height, int depth,
                        float pointX, float pointY, float pointZ, float rotation) {
    }

    private static final Part[] PARTS = {
            new Part(0, 0, 0, 0, 0, 14, 2, 14, -7, 22, -7, 0),
            new Part(0, 16, 0, 0, 0, 4, 13, 4, -2, 9, -2, 0),
            new Part(28, 19, 0, -2, 0, 6, 14, 0, 3, 10, -3, -HALF_PI),
            new Part(28, 33, 0, 0, 0, 6, 14, 0, -3, 8, 3, HALF_PI),
            new Part(40, 19, 0, 0, 0, 6, 14, 0, 3, 8, 3, (float) Math.PI),
            new Part(40, 33, 0, 0, 0, 6, 14, 0, -3, 8, -3, 0),
            new Part(28, 49, 0, 0, 0, 6, 0, 6, -3, 8, -3, 0)
    };

    private AttractorGeometry() {
    }

    public static void model(ModelSink sink) {
        for (Part part : PARTS) {
            box(sink, part);
        }
    }

    public static void ring(ModelSink sink, int frame, int frames) {
        float minV = frame / (float) frames;
        float maxV = (frame + 1) / (float) frames;
        sink.vertex(-0.5F, 0.5F, 0.0F, 1.0F, maxV);
        sink.vertex(0.5F, 0.5F, 0.0F, 0.0F, maxV);
        sink.vertex(0.5F, -0.5F, 0.0F, 0.0F, minV);
        sink.vertex(-0.5F, -0.5F, 0.0F, 1.0F, minV);
    }

    private static float[] place(Part part, float x, float y, float z) {
        double cos = Math.cos(part.rotation());
        double sin = Math.sin(part.rotation());
        float rotatedX = (float) (x * cos + z * sin);
        float rotatedZ = (float) (-x * sin + z * cos);
        float modelX = rotatedX + part.pointX();
        float modelY = y + part.pointY();
        float modelZ = rotatedZ + part.pointZ();
        return new float[] {0.5F + modelX / 16.0F, 1.5F - modelY / 16.0F, 0.5F - modelZ / 16.0F};
    }

    private static void box(ModelSink sink, Part part) {
        float x1 = part.x();
        float x2 = part.x() + part.width();
        float y1 = part.y();
        float y2 = part.y() + part.height();
        float z1 = part.z();
        float z2 = part.z() + part.depth();
        float[][] corners = {
                {x1, y1, z1}, {x2, y1, z1}, {x2, y2, z1}, {x1, y2, z1},
                {x1, y1, z2}, {x2, y1, z2}, {x2, y2, z2}, {x1, y2, z2}
        };
        float[][] placed = new float[8][];
        for (int index = 0; index < 8; index++) {
            placed[index] = place(part, corners[index][0], corners[index][1], corners[index][2]);
        }
        float[] center = place(part, part.x() + part.width() * 0.5F, part.y() + part.height() * 0.5F,
                part.z() + part.depth() * 0.5F);
        int u = part.u();
        int v = part.v();
        int width = part.width();
        int height = part.height();
        int depth = part.depth();
        int[][] regions = {
                {u + depth + width, v + depth, u + depth + width + depth, v + depth + height},
                {u, v + depth, u + depth, v + depth + height},
                {u + depth, v, u + depth + width, v + depth},
                {u + depth + width, v + depth, u + depth + width + width, v},
                {u + depth, v + depth, u + depth + width, v + depth + height},
                {u + depth + width + depth, v + depth, u + depth + width + depth + width, v + depth + height}
        };
        for (int quad = 0; quad < QUADS.length; quad++) {
            int[] indices = QUADS[quad];
            int[] region = regions[quad];
            float[][] uvs = {
                    {region[2] / TEXTURE_SIZE, region[1] / TEXTURE_SIZE},
                    {region[0] / TEXTURE_SIZE, region[1] / TEXTURE_SIZE},
                    {region[0] / TEXTURE_SIZE, region[3] / TEXTURE_SIZE},
                    {region[2] / TEXTURE_SIZE, region[3] / TEXTURE_SIZE}
            };
            float[] first = placed[indices[0]];
            float[] second = placed[indices[1]];
            float[] third = placed[indices[2]];
            float[] fourth = placed[indices[3]];
            float firstX = second[0] - first[0];
            float firstY = second[1] - first[1];
            float firstZ = second[2] - first[2];
            float secondX = fourth[0] - first[0];
            float secondY = fourth[1] - first[1];
            float secondZ = fourth[2] - first[2];
            float normalX = firstY * secondZ - firstZ * secondY;
            float normalY = firstZ * secondX - firstX * secondZ;
            float normalZ = firstX * secondY - firstY * secondX;
            float middleX = (first[0] + third[0]) * 0.5F - center[0];
            float middleY = (first[1] + third[1]) * 0.5F - center[1];
            float middleZ = (first[2] + third[2]) * 0.5F - center[2];
            boolean outward = normalX * middleX + normalY * middleY + normalZ * middleZ >= -1.0E-6F;
            for (int step = 0; step < 4; step++) {
                int vertex = outward ? step : (4 - step) % 4;
                float[] corner = placed[indices[vertex]];
                sink.vertex(corner[0], corner[1], corner[2], uvs[vertex][0], uvs[vertex][1]);
            }
        }
    }
}
