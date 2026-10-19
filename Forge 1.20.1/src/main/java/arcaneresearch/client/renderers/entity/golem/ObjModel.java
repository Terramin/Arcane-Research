package arcaneresearch.client.renderers.entity.golem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import arcaneresearch.Arcaneresearch;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
@OnlyIn(Dist.CLIENT)
public class ObjModel {
    private static final Map<ResourceLocation, ObjModel> CACHE = new ConcurrentHashMap<>();
    private final LinkedHashMap<String, List<Tri>> groups = new LinkedHashMap<>();
    public static ObjModel load(ResourceLocation loc) {
        return CACHE.computeIfAbsent(loc, ObjModel::parse);
    }
    private static ObjModel parse(ResourceLocation loc) {
        ObjModel model = new ObjModel();
        try {
            Resource res = Minecraft.getInstance().getResourceManager().getResourceOrThrow(loc);
            List<float[]> verts = new ArrayList<>();
            List<float[]> uvs = new ArrayList<>();
            List<float[]> normals = new ArrayList<>();
            String current = "Default";
            model.groups.put(current, new ArrayList<>());
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(res.open(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    if (line.startsWith("v ")) {
                        String[] p = line.split("\\s+");
                        verts.add(new float[]{Float.parseFloat(p[1]), Float.parseFloat(p[2]), Float.parseFloat(p[3])});
                    } else if (line.startsWith("vt ")) {
                        String[] p = line.split("\\s+");
                        uvs.add(new float[]{Float.parseFloat(p[1]), Float.parseFloat(p[2])});
                    } else if (line.startsWith("vn ")) {
                        String[] p = line.split("\\s+");
                        normals.add(new float[]{Float.parseFloat(p[1]), Float.parseFloat(p[2]), Float.parseFloat(p[3])});
                    } else if (line.startsWith("g ") || line.startsWith("o ")) {
                        current = line.substring(2).trim().split("\\s+")[0];
                        model.groups.computeIfAbsent(current, k -> new ArrayList<>());
                    } else if (line.startsWith("f ")) {
                        String[] p = line.split("\\s+");
                        for (int i = 2; i < p.length - 1; i++) {
                            Tri tri = new Tri();
                            boolean n0 = fillVert(tri, 0, p[1], verts, uvs, normals);
                            boolean n1 = fillVert(tri, 1, p[i], verts, uvs, normals);
                            boolean n2 = fillVert(tri, 2, p[i + 1], verts, uvs, normals);
                            if (!(n0 && n1 && n2)) {
                                applyFlatNormal(tri);
                            }
                            model.groups.get(current).add(tri);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Arcaneresearch.LOGGER.error("Failed to load OBJ model {}", loc, e);
            return null;
        }
        return model;
    }
    private static int resolve(int idx, int size) {
        if (idx > 0) {
            return idx - 1;
        }
        return size + idx;
    }
    private static boolean fillVert(Tri tri, int slot, String token,
                                    List<float[]> verts, List<float[]> uvs, List<float[]> normals) {
        String[] parts = token.split("/", -1);
        int o = slot * 8;
        float[] v = verts.get(resolve(Integer.parseInt(parts[0]), verts.size()));
        tri.data[o] = v[0];
        tri.data[o + 1] = v[1];
        tri.data[o + 2] = v[2];
        float u = 0, vt = 0;
        if (parts.length > 1 && !parts[1].isEmpty() && !uvs.isEmpty()) {
            float[] t = uvs.get(resolve(Integer.parseInt(parts[1]), uvs.size()));
            u = t[0];
            vt = t[1];
        }
        tri.data[o + 3] = u;
        tri.data[o + 4] = 1.0f - vt;
        if (parts.length > 2 && !parts[2].isEmpty() && !normals.isEmpty()) {
            float[] n = normals.get(resolve(Integer.parseInt(parts[2]), normals.size()));
            tri.data[o + 5] = n[0];
            tri.data[o + 6] = n[1];
            tri.data[o + 7] = n[2];
            return true;
        }
        tri.data[o + 5] = 0.0f;
        tri.data[o + 6] = 1.0f;
        tri.data[o + 7] = 0.0f;
        return false;
    }
    private static void applyFlatNormal(Tri tri) {
        float[] d = tri.data;
        float ax = d[8] - d[0];
        float ay = d[9] - d[1];
        float az = d[10] - d[2];
        float bx = d[16] - d[0];
        float by = d[17] - d[1];
        float bz = d[18] - d[2];
        float nx = ay * bz - az * by;
        float ny = az * bx - ax * bz;
        float nz = ax * by - ay * bx;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len < 1.0E-4f) {
            nx = 0.0f;
            ny = 1.0f;
            nz = 0.0f;
        } else {
            nx /= len;
            ny /= len;
            nz /= len;
        }
        for (int i = 0; i < 3; i++) {
            int o = i * 8;
            d[o + 5] = nx;
            d[o + 6] = ny;
            d[o + 7] = nz;
        }
    }
    public Set<String> getGroupNames() {
        return groups.keySet();
    }
    public boolean hasGroup(String name) {
        return groups.containsKey(name);
    }
    public void renderGroup(PoseStack poseStack, VertexConsumer consumer, String group,
                            int packedLight, int packedOverlay,
                            float r, float g, float b, float a) {
        List<Tri> tris = groups.get(group);
        if (tris == null || tris.isEmpty()) {
            return;
        }
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        for (Tri tri : tris) {
            float[] d = tri.data;
            for (int i = 0; i < 3; i++) {
                int o = i * 8;
                consumer.vertex(pose, d[o], d[o + 1], d[o + 2])
                        .color(r, g, b, a)
                        .uv(d[o + 3], d[o + 4])
                        .overlayCoords(packedOverlay)
                        .uv2(packedLight)
                        .normal(normal, d[o + 5], d[o + 6], d[o + 7])
                        .endVertex();
            }
        }
    }
    private static class Tri {
        final float[] data = new float[3 * 8];
    }
}
