package arcaneresearch.temp.portal;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.function.IntSupplier;

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

    /** Доступ к protected-состояниям RenderStateShard для сборки собственного RenderType. */
    private static class Shards extends RenderStateShard {
        private Shards() {
            super("portal_shards", () -> {
            }, () -> {
            });
        }

        static final RenderStateShard.ShaderStateShard SHADER_POSITION_TEX = POSITION_TEX_SHADER;
        static final RenderStateShard.CullStateShard SHARD_NO_CULL = NO_CULL;
    }

    /** Текстура-шард: биндит цветовую текстуру фреймбуфера вида портала (не ResourceLocation). */
    private static class FbTextureShard extends RenderStateShard.EmptyTextureStateShard {
        private FbTextureShard(IntSupplier textureId) {
            super(() -> {
                int id = textureId.getAsInt();
                if (id != 0) {
                    // clamp по краям: проецированные UV могут выходить за границы фреймбуфера
                    GlStateManager._texParameter(3553, 10242, 33071); // GL_TEXTURE_2D, WRAP_S, CLAMP_TO_EDGE
                    GlStateManager._texParameter(3553, 10243, 33071); // GL_TEXTURE_2D, WRAP_T, CLAMP_TO_EDGE
                }
                RenderSystem.setShaderTexture(0, id);
            }, () -> {
            });
        }
    }

    /**
     * RenderType экрана с видом через портал: шейдер position_tex — без alpha-теста
     * (альфа фреймбуфера непредсказуема) и без смешивания; no-cull — виден с обеих сторон;
     * depth-test LEQUAL по умолчанию, как у модели.
     */
    private static final RenderType PORTAL_VIEW = RenderType.create(
            "portal_view", com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS,
            256, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(Shards.SHADER_POSITION_TEX)
                    .setTextureState(new FbTextureShard(PortalViewManager::getTextureId))
                    .setCullState(Shards.SHARD_NO_CULL)
                    .createCompositeState(false));
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
        // экран в плоскости портала (на него рендерится вид "через" портал):
        // бока на 1px уже золотой полосы с каждой стороны, высота — между золотыми кромками (пересчёт по анимации),
        // глубина плоская (z=0), растёт из центра при открытии.
        double[] contour = entity.getContourBounds(partialTicks);
        double baseY = Mth.lerp(partialTicks, entity.yo, entity.getY());
        double halfWidth = contour[2] - 1.0 / 16.0; // 25px - 1px = 24px с каждой стороны
        double y0 = contour[1] - baseY;
        double y1 = contour[0] - baseY;
        float yaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());

        // во время offscreen-прохода экран не рисуется (иначе — цикл обратной связи в фреймбуфер вида)
        if (!PortalViewManager.isRenderingView()) {
            poseStack.pushPose();
            // тот же поворот, что и у модели (setupRotations: 180 - yaw)
            poseStack.mulPose(Axis.YP.rotationDegrees(180f - yaw));

            float[] viewUvs = computeViewUvs(entity, partialTicks, halfWidth, y0, y1);
            VertexConsumer screen = buffer.getBuffer(viewUvs != null ? PORTAL_VIEW : RenderType.entityCutoutNoCull(TEXTURE));
            if (viewUvs != null) {
                // вид через портал: UV углов проецируются матрицей виртуальной камеры
                renderViewQuad(screen, poseStack, halfWidth, y0, y1, viewUvs);
            } else {
                // чёрный экран-заглушка (портал в другом измерении / вид недоступен)
                renderScreenQuad(screen, poseStack, halfWidth, y0, y1, packedLight);
            }

            poseStack.popPose();
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    /**
     * UV-координаты углов экрана во фреймбуфере вида портала. Вид рендерится с той же матрицей
     * проекции, что и основной проход, и с камерой, преобразованной через портал, поэтому
     * экранное соответствие 1:1: UV угла = его экранные координаты в основном проходе
     * (проекция мирового угла матрицей глобальной проекции и поворота главной камеры).
     * null — вид не для этого портала или угол за камерой (проекция некорректна).
     */
    private static float[] computeViewUvs(PortalEntity entity, float partialTicks,
                                          double halfWidth, double y0, double y1) {
        if (!PortalViewManager.hasViewFor(entity)) {
            return null;
        }
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (camera.getEntity() == null) {
            return null;
        }
        Matrix4f projection = RenderSystem.getProjectionMatrix();
        if (projection == null) {
            return null;
        }
        PoseStack pose = new PoseStack();
        pose.mulPoseMatrix(new Matrix4f(projection));
        pose.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        pose.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
        Vec3 camPos = camera.getPosition();
        pose.translate(-camPos.x, -camPos.y, -camPos.z);
        Matrix4f projView = pose.last().pose();

        double ex = Mth.lerp((double) partialTicks, entity.xo, entity.getX());
        double ey = Mth.lerp((double) partialTicks, entity.yo, entity.getY());
        double ez = Mth.lerp((double) partialTicks, entity.zo, entity.getZ());
        Matrix4f rot = new Matrix4f().rotationY((float) Math.toRadians(180f - entity.getPortalYaw(partialTicks)));

        float[][] corners = {
                {-(float) halfWidth, (float) y0},
                {(float) halfWidth, (float) y0},
                {(float) halfWidth, (float) y1},
                {-(float) halfWidth, (float) y1}
        };
        float[] uvs = new float[8];
        for (int i = 0; i < corners.length; i++) {
            Vector3f local = new Vector3f(corners[i][0], corners[i][1], 0f);
            rot.transformPosition(local);
            // projView уже содержит перенос -camPos, подаём мировые координаты угла
            Vector4f clip = new Vector4f(
                    (float) (ex + local.x),
                    (float) (ey + local.y),
                    (float) (ez + local.z),
                    1.0F);
            projView.transform(clip);
            if (clip.w <= 1.0E-4F) {
                return null; // угол за камерой
            }
            uvs[i * 2] = clip.x / clip.w * 0.5F + 0.5F;
            uvs[i * 2 + 1] = clip.y / clip.w * 0.5F + 0.5F;
        }
        return uvs;
    }

    /** Quad экрана с видом через портал (формат POSITION_TEX — без цвета/света: картинка уже освещена). */
    private static void renderViewQuad(VertexConsumer consumer, PoseStack poseStack,
                                       double halfWidth, double y0, double y1, float[] uvs) {
        Matrix4f matrix = poseStack.last().pose();
        float x0 = (float) -halfWidth;
        float x1 = (float) halfWidth;
        float fy0 = (float) y0;
        float fy1 = (float) y1;
        float[][] corners = {{x0, fy0}, {x1, fy0}, {x1, fy1}, {x0, fy1}};
        for (int i = 0; i < corners.length; i++) {
            consumer.vertex(matrix, corners[i][0], corners[i][1], 0f)
                    .uv(uvs[i * 2], uvs[i * 2 + 1])
                    .endVertex();
        }
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