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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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

    /** Квад экрана выталкивается к камере, если плоскость портала ближе этого к камере
     *  (больше near-plane главной камеры 0.05 — иначе квад срезается и видна дыра). */
    private static final double QUAD_PUSH_MARGIN = 0.1D;
    /** Порог "угол за камерой" (clip.w <= 0 — позади плоскости камеры). */
    private static final float BEHIND_CAMERA_EPS = 1.0E-4F;
    /** Заменитель w для UV углов за камерой: конечное значение вместо Infinity/NaN. */
    private static final float UV_W_EPS = 1.0E-3F;

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
        // Во вложенном проходе вида портал не рисуется вовсе (ни рамка, ни свечение):
        // экран в проходе вида недоступен (его фреймбуфер — цель рендера), и рамка со
        // "сквозной" серединой выглядела бы фантомным порталом внутри вида.
        if (PortalViewManager.isRenderingView()) {
            return;
        }
        // экран в плоскости портала (на него рендерится вид "через" портал):
        // бока на 1px уже золотой полосы с каждой стороны, высота — между золотыми кромками (пересчёт по анимации),
        // глубина плоская (z=0), растёт из центра при открытии.
        double[] contour = entity.getContourBounds(partialTicks);
        double baseY = Mth.lerp(partialTicks, entity.yo, entity.getY());
        double halfWidth = contour[2] - 1.0 / 16.0; // 25px - 1px = 24px с каждой стороны
        double y0 = contour[1] - baseY;
        double y1 = contour[0] - baseY;
        float yaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());

        // Экран рисуется только когда есть вид через портал. Заглушка — ОТСУТСТВИЕ экрана:
        // сквозь рамку видно мир за порталом.
        float[] viewUvs = computeViewUvs(entity, partialTicks, halfWidth, y0, y1);

        if (viewUvs != null) {
            poseStack.pushPose();
            // тот же поворот, что и у модели (setupRotations: 180 - yaw)
            poseStack.mulPose(Axis.YP.rotationDegrees(180f - yaw));

            // у самой плоскости портала near-plane главной камеры (0.05) режет квад экрана
            // (дыра в изображении); выталкиваем квад к камере.
            // UV-карта от положения квада не зависит — она считается по лучам через пиксели.
            pushQuadTowardCamera(entity, partialTicks, poseStack);

            // вид через портал: UV углов проецируются матрицей виртуальной камеры
            renderViewQuad(buffer.getBuffer(PORTAL_VIEW), poseStack, halfWidth, y0, y1, viewUvs);

            poseStack.popPose();
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    /**
     * UV-координаты углов экрана во фреймбуфере вида портала. Вид рендерится с той же матрицей
     * проекции и ориентацией, что и основной проход, и с камерой ВИДА (перенесённой через
     * портал, боковое смещение ограничено). Содержимое точки экрана q — перенос через портал
     * q' = q + (выход - плоскость), поэтому UV угла = проекция q' камерой вида: картинка
     * на экране согласована с фактической камерой вида и не искажается (без ограничения
     * смещения камера вида = перенесённая камера игрока, и соответствие экрану 1:1).
     * null — вид не для этого портала или весь экран за камерой. Отдельные углы за камерой
     * допустимы: GPU сам отсечёт квад по near-plane, UV таких вершин в шейдинг не попадут —
     * важно лишь, чтобы их значения были конечными.
     */
    private static float[] computeViewUvs(PortalEntity entity, float partialTicks,
                                          double halfWidth, double y0, double y1) {
        if (!PortalViewManager.hasViewFor(entity)) {
            return null;
        }
        Vec3 viewCamPos = PortalViewManager.getActiveViewCameraPos();
        if (viewCamPos == null) {
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
        // проекция точки содержимого q' = q + (exit - plane) камерой вида:
        // перенос в матрицу сворачиваем как -(viewCamPos - (exit - plane))
        Vec3 planePos = entity.getPortalPlanePos(partialTicks);
        Vec3 exitBase = entity.getExitBase();
        pose.translate(
                (float) (exitBase.x - planePos.x - viewCamPos.x),
                (float) (exitBase.y - planePos.y - viewCamPos.y),
                (float) (exitBase.z - planePos.z - viewCamPos.z));
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
        int behind = 0;
        for (int i = 0; i < corners.length; i++) {
            Vector3f local = new Vector3f(corners[i][0], corners[i][1], 0f);
            rot.transformPosition(local);
            // projView содержит перенос фактической камеры вида, подаём мировые координаты угла
            Vector4f clip = new Vector4f(
                    (float) (ex + local.x),
                    (float) (ey + local.y),
                    (float) (ez + local.z),
                    1.0F);
            projView.transform(clip);
            if (clip.w <= BEHIND_CAMERA_EPS) {
                behind++;
                // угол за камерой: GPU отсечёт его по near-plane; UV заменяем конечным,
                // иначе Infinity/NaN при клиппинге интерполируется в NaN и портит соседей
                float w = clip.w < 0.0F ? -UV_W_EPS : UV_W_EPS;
                uvs[i * 2] = clip.x / w * 0.5F + 0.5F;
                uvs[i * 2 + 1] = clip.y / w * 0.5F + 0.5F;
                continue;
            }
            uvs[i * 2] = clip.x / clip.w * 0.5F + 0.5F;
            uvs[i * 2 + 1] = clip.y / clip.w * 0.5F + 0.5F;
        }
        if (behind == corners.length) {
            return null; // весь экран за камерой
        }
        return uvs;
    }

    /**
     * Сдвиг квада экрана к камере, когда плоскость портала ближе QUAD_PUSH_MARGIN:
     * near-plane главной камеры (0.05) вплотную срезает квад в плоскости портала,
     * оставляя дыру. Сдвиг выполняется по локальной Z pose (после поворота 180-yaw
     * локальный +Z направлен ПРОТИВ нормали портала: (sin yaw, 0, -cos yaw)).
     * UV-проекция считается по лучам через пиксели, поэтому от сдвига квада не меняется.
     */
    private static void pushQuadTowardCamera(PortalEntity entity, float partialTicks, PoseStack poseStack) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (camera.getEntity() == null) {
            return;
        }
        Vec3 normal = entity.getFacingNormal(partialTicks);
        double along = camera.getPosition().subtract(entity.getPortalPlanePos(partialTicks)).dot(normal);
        double absAlong = Math.abs(along);
        if (absAlong >= QUAD_PUSH_MARGIN) {
            return;
        }
        double push = (QUAD_PUSH_MARGIN - absAlong) * (along >= 0.0D ? 1.0D : -1.0D);
        // локальная Z = -нормаль, значит сдвиг "к камере" (по нормали) — это локальный -Z * sign
        poseStack.translate(0.0F, 0.0F, (float) (-push));
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