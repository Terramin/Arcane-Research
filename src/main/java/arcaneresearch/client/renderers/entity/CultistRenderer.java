package arcaneresearch.client.renderers.entity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.entities.monster.cult.EntityCultist;
import arcaneresearch.common.entities.monster.cult.EntityCultistCleric;
import java.util.Random;
@OnlyIn(Dist.CLIENT)
public class CultistRenderer extends HumanoidMobRenderer<EntityCultist, HumanoidModel<EntityCultist>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/entity/cultist.png");
    private static final ResourceLocation TETHER_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/misc/wispy.png");
    public CultistRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
    }
    private static void quad(VertexConsumer consumer, Matrix4f matrix,
                             float x0, float y0, float z0, float u0,
                             float x1, float y1, float z1, float u1,
                             float x2, float y2, float z2, float u2,
                             float x3, float y3, float z3, float u3,
                             float r, float g, float b) {
        consumer.vertex(matrix, x0, y0, z0).color(r, g, b, 0.8F).uv(u0, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0)
                .normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, x1, y1, z1).color(r, g, b, 0.8F).uv(u1, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0)
                .normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, x2, y2, z2).color(r, g, b, 0.8F).uv(u2, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0)
                .normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, x3, y3, z3).color(r, g, b, 0.8F).uv(u3, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0)
                .normal(0, 1, 0).endVertex();
    }
    @Override
    public ResourceLocation getTextureLocation(EntityCultist entity) {
        return TEXTURE;
    }
    @Override
    public void render(EntityCultist entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float bob = 0.0F;
        boolean ritualist = entity instanceof EntityCultistCleric cleric && cleric.isRitualist();
        if (ritualist) {
            int seed = new Random(entity.getId()).nextInt(1000);
            float time = entity.tickCount + partialTicks + seed;
            bob = Mth.sin(time / 9.0F) * 0.1F + 0.21F;
            poseStack.translate(0.0, bob, 0.0);
        }
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        if (ritualist && entity instanceof EntityCultistCleric cleric) {
            BlockPos home = cleric.getRestrictCenter();
            drawTether(poseStack, buffer, entity.getX(), entity.getY(), entity.getZ(),
                    0.0, entity.getEyeHeight() * 1.2, 0.0,
                    home.getX() + 0.5 - entity.getX(),
                    home.getY() + 1.5 - bob - entity.getY(),
                    home.getZ() + 0.5 - entity.getZ(),
                    partialTicks, 1114129, -0.03F,
                    Math.min(entity.tickCount, 10) / 10.0F, 0.25F);
        }
        poseStack.popPose();
    }
    private void drawTether(PoseStack poseStack, MultiBufferSource buffer,
                            double wx, double wy, double wz,
                            double sx, double sy, double sz,
                            double ex, double ey, double ez,
                            float partialTicks, int color, float speed,
                            float distance, float width) {
        double dx = ex - sx;
        double dy = ey - sy;
        double dz = ez - sz;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        float time = System.nanoTime() / 30000000.0F;
        double dcx = sx - ex;
        double dcy = sy - ey;
        double dcz = sz - ez;
        float blocks = Math.round(dist);
        float length = blocks * 6.0F;
        int segments = Math.max(1, (int) (length * distance));
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TETHER_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        for (int pass = 0; pass < 2; pass++) {
            float[] ax = new float[segments + 1];
            float[] ay = new float[segments + 1];
            float[] az = new float[segments + 1];
            float[] au = new float[segments + 1];
            for (int i = 0; i <= segments; i++) {
                float f11 = i / length;
                float f12 = 1.0F - Math.abs(i - length / 2.0F) / (length / 2.0F);
                double wobx = dcx + Math.sin((wz % 16.0 + dist * (1.0F - f11) * 6.0F - time % 32767.0F / 5.0F) / 4.0) * 0.5 * f12;
                double woby = dcy + Math.sin((wx % 16.0 + dist * (1.0F - f11) * 6.0F - time % 32767.0F / 5.0F) / 3.0) * 0.5 * f12;
                double wobz = dcz + Math.sin((wy % 16.0 + dist * (1.0F - f11) * 6.0F - time % 32767.0F / 5.0F) / 2.0) * 0.5 * f12;
                ax[i] = (float) (ex + wobx * f11);
                ay[i] = (float) (ey + woby * f11);
                az[i] = (float) (ez + wobz * f11);
                au[i] = (1.0F - f11) * (float) dist - time * speed;
            }
            for (int i = 1; i <= segments; i++) {
                float px0, py0, pz0, qx0, qy0, qz0, px1, py1, pz1, qx1, qy1, qz1;
                if (pass == 0) {
                    px0 = ax[i - 1];
                    py0 = ay[i - 1] - width;
                    pz0 = az[i - 1];
                    qx0 = ax[i - 1];
                    qy0 = ay[i - 1] + width;
                    qz0 = az[i - 1];
                    px1 = ax[i];
                    py1 = ay[i] - width;
                    pz1 = az[i];
                    qx1 = ax[i];
                    qy1 = ay[i] + width;
                    qz1 = az[i];
                } else {
                    px0 = ax[i - 1] - width;
                    py0 = ay[i - 1];
                    pz0 = az[i - 1];
                    qx0 = ax[i - 1] + width;
                    qy0 = ay[i - 1];
                    qz0 = az[i - 1];
                    px1 = ax[i] - width;
                    py1 = ay[i];
                    pz1 = az[i];
                    qx1 = ax[i] + width;
                    qy1 = ay[i];
                    qz1 = az[i];
                }
                quad(consumer, matrix, px0, py0, pz0, au[i - 1], qx0, qy0, qz0, au[i - 1],
                        qx1, qy1, qz1, au[i], px1, py1, pz1, au[i], r, g, b);
            }
        }
    }
}
