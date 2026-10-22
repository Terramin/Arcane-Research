package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier; 
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TurretObjModel {

    private static final Map<String, TurretObjModel> CACHE = new ConcurrentHashMap<>();
    private final LinkedHashMap<String, List<Tri>> groups = new LinkedHashMap<>();

    public static TurretObjModel load(Identifier loc) {
        return load(loc, true);
    }

    
    
    
    
    
    public static TurretObjModel load(Identifier loc, boolean flipV) {
        return CACHE.computeIfAbsent(loc + "|flipV=" + flipV, k -> parse(loc, flipV));
    }

    private static TurretObjModel parse(Identifier loc) {
        return parse(loc, true);
    }

    private static TurretObjModel parse(Identifier loc, boolean flipV) {
        TurretObjModel model = new TurretObjModel();
        try {
            Resource res = Minecraft.getInstance().getResourceManager().getResourceOrThrow(loc);
            List<float[]> verts = new ArrayList<>();
            List<float[]> uvs = new ArrayList<>();
            String current = "default";
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
                    } else if (line.startsWith("g ") || line.startsWith("o ")) {
                        current = line.substring(2).trim().split("\\s+")[0];
                        model.groups.computeIfAbsent(current, k -> new ArrayList<>());
                    } else if (line.startsWith("f ")) {
                        String[] p = line.split("\\s+");

                        for (int i = 2; i < p.length - 1; i++) {
                            Tri tri = new Tri();
                            fillVert(tri, 0, p[1], verts, uvs, flipV);
                            fillVert(tri, 1, p[i], verts, uvs, flipV);
                            fillVert(tri, 2, p[i + 1], verts, uvs, flipV);
                            computeFlatNormal(tri);
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

    private static void fillVert(Tri tri, int slot, String token,
                                 List<float[]> verts, List<float[]> uvs, boolean flipV) {
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

        tri.data[o + 4] = flipV ? 1.0f - vt : vt;
    }

    private static void computeFlatNormal(Tri tri) {
        float[] d = tri.data;
        float ax = d[8] - d[0], ay = d[9] - d[1], az = d[10] - d[2];
        float bx = d[16] - d[0], by = d[17] - d[1], bz = d[18] - d[2];
        float nx = ay * bz - az * by;
        float ny = az * bx - ax * bz;
        float nz = ax * by - ay * bx;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len < 1e-6f) {
            nx = 0;
            ny = 1;
            nz = 0;
        } else {
            nx /= len;
            ny /= len;
            nz /= len;
        }
        for (int i = 0; i < 3; i++) {
            d[i * 8 + 5] = nx;
            d[i * 8 + 6] = ny;
            d[i * 8 + 7] = nz;
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
        renderGroup(poseStack, consumer, group, packedLight, packedOverlay, r, g, b, a, 0.0F, 1.0F);
    }

     








 
    public void renderGroup(PoseStack poseStack, VertexConsumer consumer, String group,
                            int packedLight, int packedOverlay,
                            float r, float g, float b, float a,
                            float vOffset, float vScale) {
        List<Tri> tris = groups.get(group);
        if (tris == null || tris.isEmpty()) {
            return;
        }
        
        
        
        PoseStack.Pose pose = poseStack.last();
        for (Tri tri : tris) {
            float[] d = tri.data;
            for (int i = 0; i < 4; i++) {
                int o = Math.min(i, 2) * 8;
                consumer.addVertex(pose, d[o], d[o + 1], d[o + 2])
                        .setColor(r, g, b, a)
                        .setUv(d[o + 3], vOffset + d[o + 4] * vScale)
                        .setOverlay(packedOverlay)
                        .setLight(packedLight)
                        .setNormal(pose, d[o + 5], d[o + 6], d[o + 7]);
            }
        }
    }

    private static class Tri {
        final float[] data = new float[3 * 8];
    }
}