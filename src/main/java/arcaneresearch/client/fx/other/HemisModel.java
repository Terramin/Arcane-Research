package arcaneresearch.client.fx.other;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier; 
import net.neoforged.api.distmarker.Dist;
import org.joml.Matrix4f;
import arcaneresearch.Arcaneresearch;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ARGB;

public final class HemisModel {

    private static final Identifier MODEL =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "models/obj/hemis.obj");

    private static float[] verts;
    private static boolean tried = false;

    private HemisModel() {
    }

    public static synchronized float[] get() {
        if (verts == null && !tried) {
            tried = true;
            verts = load();
        }
        return verts;
    }

    private static float[] load() {
        try {
            List<float[]> v = new ArrayList<>();
            List<float[]> vt = new ArrayList<>();
            List<float[]> out = new ArrayList<>();
            var resource = Minecraft.getInstance().getResourceManager().getResource(MODEL);
            if (resource.isEmpty()) {
                return null;
            }
            try (InputStream in = resource.get().open();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("v ")) {
                        String[] p = line.split("\\s+");
                        v.add(new float[]{Float.parseFloat(p[1]), Float.parseFloat(p[2]), Float.parseFloat(p[3])});
                    } else if (line.startsWith("vt ")) {
                        String[] p = line.split("\\s+");
                        vt.add(new float[]{Float.parseFloat(p[1]), Float.parseFloat(p[2])});
                    } else if (line.startsWith("f ")) {
                        String[] p = line.split("\\s+");
                        if (p.length != 4) {
                            continue;
                        }
                        for (int i = 1; i <= 3; i++) {
                            String[] idx = p[i].split("/");
                            int vi = Integer.parseInt(idx[0]) - 1;
                            int ti = Integer.parseInt(idx[1]) - 1;
                            float[] vv = v.get(vi);
                            float[] tt = vt.get(ti);
                            out.add(new float[]{vv[0], vv[1], vv[2], tt[0], tt[1]});
                        }
                    }
                }
            }
            float[] flat = new float[out.size() * 5];
            for (int i = 0; i < out.size(); i++) {
                float[] q = out.get(i);
                System.arraycopy(q, 0, flat, i * 5, 5);
            }
            return flat.length == 0 ? null : flat;
        } catch (Exception ex) {
            return null;
        }
    }

    public static void render(PoseStack poseStack, VertexConsumer consumer,
                              float r, float g, float b, float a) {
        float[] mesh = get();
        if (mesh == null) {
            return;
        }
        Matrix4f matrix = poseStack.last().pose();
        
        
        
        int argb = net.minecraft.util.ARGB.color((int) ((a) * 255.0F), (int) ((r) * 255.0F), (int) ((g) * 255.0F), (int) ((b) * 255.0F));
        for (int i = 0; i + 14 < mesh.length; i += 15) {
            for (int k = 0; k < 4; k++) {
                int o = Math.min(k, 2) * 5;
                consumer.addVertex(matrix, mesh[i + o], mesh[i + o + 1], mesh[i + o + 2])
                        .setUv(mesh[i + o + 3], mesh[i + o + 4])
                        .setColor(argb)
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(0xF000F0)
                        .setNormal(0.0F, 1.0F, 0.0F);
            }
        }
    }
}