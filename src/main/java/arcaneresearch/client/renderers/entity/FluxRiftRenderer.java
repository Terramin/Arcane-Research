package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.common.entities.EntityFluxRift;
import arcaneresearch.common.lib.utils.EntityUtils;

import java.util.List;






public class FluxRiftRenderer extends EntityRenderer<EntityFluxRift, FluxRiftRenderer.State> {

    public static class State extends EntityRenderState {
        public List<Vec3> points;
        public List<Float> widths;
        public float stability;
        public float time;
        public boolean goggles;
    }

    private static final Identifier RIFT_TEXTURE =
            Identifier.fromNamespaceAndPath("minecraft", "textures/entity/end_portal/end_portal.png"); 

    private static final java.util.concurrent.ExecutorService MESH_POOL =
            java.util.concurrent.Executors.newFixedThreadPool(
                    Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
                    runnable -> {
                        Thread thread = new Thread(runnable, "TC-RiftMesh");
                        thread.setDaemon(true);
                        return thread;
                    });

    public FluxRiftRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
        this.shadowStrength = 0.0F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityFluxRift entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.points = entity.getPoints();
        state.widths = entity.getPointWidths();
        state.stability = entity.getRiftStability();
        state.time = entity.tickCount + partialTicks;
        state.goggles = Minecraft.getInstance().player != null
                && EntityUtils.hasGoggles(Minecraft.getInstance().player);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        List<Vec3> points = state.points;
        List<Float> widths = state.widths;
        if (points == null || points.size() <= 2 || widths == null || widths.size() <= 2) {
            return;
        }
        poseStack.pushPose();

        float stab = Mth.clamp(1.0F - state.stability / 50.0F, 0.0F, 1.5F);
        float time = state.time;

        java.util.concurrent.Future<float[]>[] jobs = new java.util.concurrent.Future[4];
        for (int q = 0; q <= 3; q++) {
            final float widthScale = (q < 3) ? (1.25F + 0.5F * q) : 1.0F;
            jobs[q] = MESH_POOL.submit(() -> buildTube(points, widths, time, stab, widthScale));
        }
        for (int q = 0; q <= 3; q++) {
            float[] verts;
            try {
                verts = jobs[q].get();
            } catch (Exception e) {
                continue;
            }
            if (verts == null || verts.length == 0) {
                continue;
            }
            RenderType type = (q < 3) ? RenderTypes.entityTranslucentEmissive(RIFT_TEXTURE)
                    : RenderTypes.entityTranslucent(RIFT_TEXTURE);
            collector.submitCustomGeometry(poseStack, type,
                    (pose, consumer) -> emitTube(pose, consumer, verts));
        }

        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    private static void emitTube(PoseStack.Pose pose, VertexConsumer consumer, float[] verts) {
        for (int i = 0; i + 11 < verts.length; i += 12) {
            for (int k = 0; k < 4; k++) {
                consumer.addVertex(pose, verts[i + k * 3], verts[i + k * 3 + 1], verts[i + k * 3 + 2])
                        .setColor(1.0F, 1.0F, 1.0F, 1.0F)
                        .setUv(0.0F, 0.0F)
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(0xF000F0)
                        .setNormal(pose, 0.0F, 1.0F, 0.0F);
            }
        }
    }

    private static float[] buildTube(List<Vec3> points, List<Float> widths,
                                     float time, float stab, float widthScale) {
        int n = points.size();
        float[] wx = new float[n];
        float[] wy = new float[n];
        float[] wz = new float[n];
        float[] wr = new float[n];
        for (int a = 0; a < n; a++) {
            float var = time;
            if (a > n / 2) {
                var -= a * 10;
            } else if (a < n / 2) {
                var += a * 10;
            }
            Vec3 p = points.get(a);
            wx[a] = (float) (p.x + Math.sin(var / 50.0f) * 0.1 * stab);
            wy[a] = (float) (p.y + Math.sin(var / 60.0f) * 0.1 * stab);
            wz[a] = (float) (p.z + Math.sin(var / 70.0f) * 0.1 * stab);
            wr[a] = widths.get(a) * (1.0f - (float) Math.sin(var / 8.0f) * 0.1f * stab) * widthScale;
        }

        final int sides = 6;
        float[][][] ring = new float[n][sides][3];
        for (int i = 0; i < n; i++) {
            int p0 = Math.max(0, i - 1);
            int p1 = Math.min(n - 1, i + 1);
            float tx = wx[p1] - wx[p0];
            float ty = wy[p1] - wy[p0];
            float tz = wz[p1] - wz[p0];
            float tlen = Mth.sqrt(tx * tx + ty * ty + tz * tz);
            if (tlen < 1.0e-4f) {
                tx = 0.0f;
                ty = 1.0f;
                tz = 0.0f;
                tlen = 1.0f;
            }
            tx /= tlen;
            ty /= tlen;
            tz /= tlen;

            float px = -tz;
            float py = 0.0f;
            float pz2 = tx;
            if (Math.abs(ty) > 0.99f) {
                px = 1.0f;
                py = 0.0f;
                pz2 = 0.0f;
            }
            float plen = Mth.sqrt(px * px + py * py + pz2 * pz2);
            px /= plen;
            py /= plen;
            pz2 /= plen;
            float qx = ty * pz2 - tz * py;
            float qy = tz * px - tx * pz2;
            float qz = tx * py - ty * px;
            for (int k = 0; k < sides; k++) {
                float ang = k * Mth.TWO_PI / sides;
                float co = Mth.cos(ang);
                float si = Mth.sin(ang);
                ring[i][k][0] = wx[i] + (px * co + qx * si) * wr[i];
                ring[i][k][1] = wy[i] + (py * co + qy * si) * wr[i];
                ring[i][k][2] = wz[i] + (pz2 * co + qz * si) * wr[i];
            }
        }

        float[] out = new float[(n - 1) * sides * 4 * 3];
        int o = 0;
        for (int i = 0; i < n - 1; i++) {
            for (int k = 0; k < sides; k++) {
                int k2 = (k + 1) % sides;
                out[o++] = ring[i][k][0];
                out[o++] = ring[i][k][1];
                out[o++] = ring[i][k][2];
                out[o++] = ring[i][k2][0];
                out[o++] = ring[i][k2][1];
                out[o++] = ring[i][k2][2];
                out[o++] = ring[i + 1][k2][0];
                out[o++] = ring[i + 1][k2][1];
                out[o++] = ring[i + 1][k2][2];
                out[o++] = ring[i + 1][k][0];
                out[o++] = ring[i + 1][k][1];
                out[o++] = ring[i + 1][k][2];
            }
        }
        return out;
    }
}