package arcaneresearch.temp.portal;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
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

@OnlyIn(Dist.CLIENT)
public class PortalRenderer extends MobRenderer<PortalEntity, ModelPortalJava<PortalEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/entity/portal.png");
    private static final ResourceLocation GLOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/entity/glowportal.png");

    /** Квад волны выталкивается к камере, если плоскость портала ближе этого к камере
     *  (больше near-plane главной камеры 0.05 — иначе квад срезается и волна пропадает). */
    private static final double QUAD_PUSH_MARGIN = 0.1D;
    /** Шаг проекции для перспективного масштаба искажения (мировые единицы). */
    private static final float SCALE_PROBE_STEP = 0.25F;
    /** Мировая единица = столько NDC; упаковка масштаба в байт цвета (см. шейдер). */
    private static final float MAX_SCALE_PACKED = 2.0F;

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
        // прозрачный экран с кольцевой волной (модель рамки рисуется следом, поверх)
        if (PortalWaveFx.isActive()) {
            // копия фона снимается до отрисовки первого портала в кадре
            PortalWaveFx.ensureBackgroundCopied();
            drawWaveRing(entity, partialTicks, poseStack, buffer);
        }
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    /**
     * Квад волны в плоскости портала (границы — внутренние кромки золотой полосы рамки,
     * пересчёт по анимации открытия). Квад полностью прозрачный: рисуется только кольцо
     * волны, расходящееся от центра раз в секунду. UV хранит локальные координаты от
     * центра волны (мировые единицы), цвет — экранные направление и перспективный
     * масштаб искажения для этого угла (шейдер сэмплирует копию фона со смещением).
     */
    private static void drawWaveRing(PortalEntity entity, float partialTicks, PoseStack poseStack,
                                     MultiBufferSource buffer) {
        double[] contour = entity.getContourBounds(partialTicks);
        double baseY = Mth.lerp((double) partialTicks, entity.yo, entity.getY());
        double halfWidth = contour[2];
        double y0 = contour[1] - baseY;
        double y1 = contour[0] - baseY;
        if (y1 - y0 < 0.05D) {
            return; // портал ещё не раскрылся
        }
        double centerY = (y0 + y1) * 0.5;
        float yaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (camera.getEntity() == null) {
            return;
        }
        Vec3 camPos = camera.getPosition();
        Vec3 planePos = entity.getPortalPlanePos(partialTicks);

        // мировые координаты -> clip (проекция + поворот камеры + перенос)
        PoseStack projViewPose = new PoseStack();
        projViewPose.mulPoseMatrix(new Matrix4f(RenderSystem.getProjectionMatrix()));
        projViewPose.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        projViewPose.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
        projViewPose.translate((float) -camPos.x, (float) -camPos.y, (float) -camPos.z);
        Matrix4f projView = projViewPose.last().pose();

        // мировые направления -> пространство камеры (только поворот)
        PoseStack worldToCamPose = new PoseStack();
        worldToCamPose.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        worldToCamPose.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
        Matrix4f worldToCam = worldToCamPose.last().pose();

        // поворот портала: локальные координаты -> мировые
        Matrix4f portalRot = new Matrix4f().rotationY((float) Math.toRadians(180f - yaw));

        poseStack.pushPose();
        // тот же поворот, что и у модели (setupRotations: 180 - yaw)
        poseStack.mulPose(Axis.YP.rotationDegrees(180f - yaw));
        // выталкивание квада к камере у near-plane (позиция квада не влияет на UV/цвет)
        pushQuadTowardCamera(entity, partialTicks, poseStack);

        VertexConsumer buf = buffer.getBuffer(PortalWaveFx.getWaveType());
        Matrix4f matrix = poseStack.last().pose();

        float fx0 = (float) -halfWidth;
        float fx1 = (float) halfWidth;
        float fy0 = (float) y0;
        float fy1 = (float) y1;
        float[][] corners = {
                {fx0, fy0},
                {fx1, fy0},
                {fx1, fy1},
                {fx0, fy1}
        };
        for (float[] corner : corners) {
            // радиальный луч из центра волны (в плоскости портала, мировые единицы)
            float rx = corner[0];
            float ry = corner[1] - (float) centerY;
            float len = (float) Math.sqrt(rx * rx + ry * ry);
            Vector3f localDir = len > 1.0E-4F ? new Vector3f(rx / len, ry / len, 0f) : new Vector3f(0f, 1f, 0f);

            Vector3f worldDir = new Vector3f(localDir);
            portalRot.transformPosition(worldDir);

            // направление луча на экране (NDC)
            Vector3f camDir = new Vector3f(worldDir);
            worldToCam.transformDirection(camDir);
            Vector4f clipDir = new Vector4f(camDir.x, camDir.y, camDir.z, 0f);
            projView.transform(clipDir);
            float dirX = 0f;
            float dirY = 1f;
            if (Math.abs(clipDir.w) > 1.0E-4F) {
                dirX = clipDir.x / clipDir.w;
                dirY = clipDir.y / clipDir.w;
                float l = (float) Math.sqrt(dirX * dirX + dirY * dirY);
                if (l > 1.0E-5F) {
                    dirX /= l;
                    dirY /= l;
                } else {
                    dirX = 0f;
                    dirY = 1f;
                }
            }

            // перспективный масштаб: NDC-сдвиг на мировую единицу у этого угла
            Vector3f cornerWorld = new Vector3f(corner[0], corner[1], 0f);
            portalRot.transformPosition(cornerWorld);
            Vector3f p0 = new Vector3f(
                    (float) planePos.x + cornerWorld.x,
                    (float) planePos.y + cornerWorld.y,
                    (float) planePos.z + cornerWorld.z);
            Vector3f p1 = new Vector3f(p0).add(
                    worldDir.x * SCALE_PROBE_STEP,
                    worldDir.y * SCALE_PROBE_STEP,
                    worldDir.z * SCALE_PROBE_STEP);
            Vector4f clipP0 = new Vector4f(p0.x, p0.y, p0.z, 1f);
            Vector4f clipP1 = new Vector4f(p1.x, p1.y, p1.z, 1f);
            projView.transform(clipP0);
            projView.transform(clipP1);
            float scale = 0f;
            if (clipP0.w > 1.0E-4F && clipP1.w > 1.0E-4F) {
                float ndcDx = clipP1.x / clipP1.w - clipP0.x / clipP0.w;
                float ndcDy = clipP1.y / clipP1.w - clipP0.y / clipP0.w;
                scale = (float) Math.sqrt(ndcDx * ndcDx + ndcDy * ndcDy) / SCALE_PROBE_STEP;
            }

            buf.vertex(matrix, corner[0], corner[1], 0f)
                    .color(dirX * 0.5f + 0.5f, dirY * 0.5f + 0.5f,
                            Mth.clamp(scale / MAX_SCALE_PACKED, 0f, 1f), 1f)
                    .uv(corner[0], corner[1] - (float) centerY)
                    .endVertex();
        }

        poseStack.popPose();
    }

    /**
     * Сдвиг квада к камере, когда плоскость портала ближе QUAD_PUSH_MARGIN:
     * near-plane главной камеры (0.05) вплотную срезает квад в плоскости портала.
     * Сдвиг по локальной Z pose (после поворота 180-yaw локальный +Z направлен
     * ПРОТИВ нормали портала). На UV/цвет волны не влияет.
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
