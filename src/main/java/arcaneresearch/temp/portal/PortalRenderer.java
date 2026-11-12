package arcaneresearch.temp.portal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class PortalRenderer extends MobRenderer<PortalEntity, ModelPortalJava<PortalEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/entity/portal.png");
    private static final ResourceLocation GLOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/entity/glowportal.png");
    /** UV непрозрачного пикселя (золотая полоса, A=255); чёрный цвет вершины умножает его в чёрную заливку экрана.
     *  "Чёрные" пиксели плоскости текстуры прозрачны (A=0) — cutout отбросил бы quad. */
    private static final float SCREEN_U = 10.0F / 128F;
    private static final float SCREEN_V = 20.0F / 128F;

    public PortalRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelPortalJava<>(context.bakeLayer(ModelPortalJava.LAYER_LOCATION)), 0f);

        this.addLayer(new RenderLayer<PortalEntity, ModelPortalJava<PortalEntity>>(this) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                               PortalEntity entity, float limbSwing, float limbSwingAmount,
                               float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
                VertexConsumer consumer = bufferSource.getBuffer(RenderType.eyes(GLOW_TEXTURE));
                this.getParentModel().renderToBuffer(
                        poseStack, consumer, 0xF000F0,
                        LivingEntityRenderer.getOverlayCoords(entity, 0),
                        1f, 1f, 1f, 1f);
            }
        });
    }

    @Override
    public void render(PortalEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        // чёрный экран-заливка в плоскости портала (на него дальше будет рендериться обратная сторона портала):
        // бока на 1px уже золотой полосы с каждой стороны, высота — между золотыми кромками (пересчёт по анимации),
        // глубина плоская (z=0), растёт из центра при открытии.
        double[] contour = entity.getContourBounds(partialTicks);
        double baseY = Mth.lerp(partialTicks, entity.yo, entity.getY());
        double halfWidth = contour[2] - 1.0 / 16.0; // 25px - 1px = 24px с каждой стороны
        double y0 = contour[1] - baseY;
        double y1 = contour[0] - baseY;

        poseStack.pushPose();
        // тот же поворот, что и у модели (setupRotations: 180 - yaw)
        poseStack.mulPose(Axis.YP.rotationDegrees(180f - entity.getYRot()));

        // экран рисуется В ТОМ ЖЕ RenderType, что и модель (entityCutoutNoCull), и ДО модели:
        // отрисованная позже модель всегда проходит depth-test (LEQUAL на совпадающей глубине)
        // и отображается поверх экрана, с обеих сторон
        VertexConsumer screen = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        renderScreenQuad(screen, poseStack, halfWidth, y0, y1, packedLight);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    /** Плоский чёрный quad экрана (z=0); no-cull — виден с обеих сторон. */
    private static void renderScreenQuad(VertexConsumer consumer, PoseStack poseStack,
                                         double halfWidth, double y0, double y1, int packedLight) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        float x0 = (float) -halfWidth;
        float x1 = (float) halfWidth;
        float fy0 = (float) y0;
        float fy1 = (float) y1;
        float[][] corners = {{x0, fy0}, {x1, fy0}, {x1, fy1}, {x0, fy1}};
        for (float[] corner : corners) {
            consumer.vertex(matrix, corner[0], corner[1], 0f)
                    .color(0, 0, 0, 255)
                    .uv(SCREEN_U, SCREEN_V)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(packedLight)
                    .normal(normal, 0f, 0f, 1f)
                    .endVertex();
        }
    }

    protected void setupRotations(PortalEntity entity, PoseStack poseStack, float bob, float yBodyRot, float partialTicks) {
        // модель поворачивается по фактическому yaw сущности (yBodyRot у "noAi" сущности не обновляется)
        float yaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        super.setupRotations(entity, poseStack, bob, yaw, partialTicks);
    }

    @Override
    public ResourceLocation getTextureLocation(PortalEntity entity) {
        return TEXTURE;
    }
}