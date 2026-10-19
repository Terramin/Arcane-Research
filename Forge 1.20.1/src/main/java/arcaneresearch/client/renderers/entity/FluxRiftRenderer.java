package arcaneresearch.client.renderers.entity;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import arcaneresearch.common.entities.EntityFluxRift;
import arcaneresearch.common.lib.utils.EntityUtils;
import java.util.List;
@OnlyIn(Dist.CLIENT)
public class FluxRiftRenderer extends EntityRenderer<EntityFluxRift> {
    private static final ResourceLocation RIFT_TEXTURE =
            new ResourceLocation("textures/entity/end_portal.png");
    private static final RenderType RIFT_HALO = makeRiftType("tc_rift_halo", true, false);
    private static final RenderType RIFT_HALO_NOFOG = makeRiftType("tc_rift_halo_nodepth", true, true);
    private static final RenderType RIFT_CORE = makeRiftType("tc_rift_core", false, false);
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
    private static RenderType makeRiftType(String name, boolean additive, boolean noDepthTest) {
        RenderStateShard.TransparencyStateShard transparency;
        if (additive) {
            transparency = new RenderStateShard.TransparencyStateShard("rift_additive",
                    () -> {
                        RenderSystem.enableBlend();
                        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                                GlStateManager.DestFactor.ONE);
                    },
                    () -> {
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                    });
        } else {
            transparency = new RenderStateShard.TransparencyStateShard("rift_translucent",
                    () -> {
                        RenderSystem.enableBlend();
                        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                    },
                    () -> {
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                    });
        }
        return RenderType.create(name,
                com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS,
                256, false, false,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(
                                GameRenderer::getRendertypeEndPortalShader))
                        .setTextureState(RenderStateShard.MultiTextureStateShard.builder()
                                .add(TheEndPortalRenderer.END_SKY_LOCATION, false, false)
                                .add(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false)
                                .build())
                        .setTransparencyState(transparency)
                        .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false))
                        .setCullState(new RenderStateShard.CullStateShard(false))
                        .setDepthTestState(new RenderStateShard.DepthTestStateShard(
                                "rift_depth", noDepthTest ? GL11.GL_ALWAYS : GL11.GL_LEQUAL))
                        .createCompositeState(false));
    }
    @Override
    public ResourceLocation getTextureLocation(EntityFluxRift entity) {
        return RIFT_TEXTURE;
    }
    @Override
    public void render(EntityFluxRift entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        List<Vec3> points = entity.getPoints();
        List<Float> widths = entity.getPointWidths();
        if (points == null || points.size() <= 2 || widths == null || widths.size() <= 2) {
            return;
        }
        poseStack.pushPose();
        float stability = entity.getRiftStability();
        float stab = Mth.clamp(1.0F - stability / 50.0F, 0.0F, 1.5F);
        float time = entity.tickCount + partialTicks;
        boolean goggles = Minecraft.getInstance().player != null
                && EntityUtils.hasGoggles(Minecraft.getInstance().player);
        Matrix4f matrix = new Matrix4f(poseStack.last().pose());
        java.util.concurrent.Future<float[]>[] jobs = new java.util.concurrent.Future[4];
        for (int q = 0; q <= 3; q++) {
            final float widthScale = (q < 3) ? (1.25f + 0.5f * q) : 1.0f;
            jobs[q] = MESH_POOL.submit(() ->
                    buildTube(points, widths, time, stab, widthScale, matrix));
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
            RenderType type = (q < 3) ? ((q == 0 && goggles) ? RIFT_HALO_NOFOG : RIFT_HALO) : RIFT_CORE;
            VertexConsumer vertexConsumer = buffer.getBuffer(type);
            for (int i = 0; i < verts.length; i += 3) {
                vertexConsumer.vertex(matrix, verts[i], verts[i + 1], verts[i + 2]).endVertex();
            }
        }
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
    private static float[] buildTube(List<Vec3> points, List<Float> widths,
                                     float time, float stab, float widthScale,
                                     Matrix4f matrix) {
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
