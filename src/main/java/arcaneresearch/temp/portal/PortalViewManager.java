package arcaneresearch.temp.portal;

import arcaneresearch.Arcaneresearch;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

/**
 * Рендер вида "через" портал (схема ImmersivePortals).
 *
 * Проход выполняется ВНУТРИ основного renderLevel — на стадии AFTER_SKY основного прохода
 * вызывается вложенный levelRenderer.renderLevel с виртуальной камерой в СВОЙ фреймбуфер.
 *
 * Виртуальная камера = камера игрока, ПЕРЕНЕСЁННАЯ через портал: позиция смещается на вектор
 * (точка выхода - плоскость портала), направление взгляда СОХРАНЯЕТСЯ. Именно перенос, а не
 * отражение: телепорт сохраняет yaw игрока (PortalEntity#teleportPlayer), поэтому "вид в окно"
 * бесшовно стыкуется с картинкой после прохода и не зеркалится. Кроме того, при переносе
 * ориентация вида совпадает с ориентацией основной камеры — а UV-проекция экрана портала
 * (PortalRenderer#computeViewUvs) строится по матрицам ОСНОВНОЙ камеры, так что содержимое
 * фреймбуфера вида соответствует экрану 1:1: луч через пиксель портала в основном виде
 * продолжается тем же пикселем во фреймбуфере вида.
 *
 * Боковое смещение камеры ВИДА ограничено (MAX_LATERAL_OFFSET) с проверкой на блоки: иначе,
 * когда игрок стоит далеко сбоку от портала, перенесённая камера оказывается внутри геометрии
 * у точки выхода — вид рендерится изнутри блоков, backface'ы отброшены и сквозь стены
 * "просвечивают" фантомные block entities и порталы, а картинка скачет при обходе портала.
 * При ограничении UV-проекция экрана всё равно строится фактической камерой вида (с переносом
 * содержимого через портал), поэтому искажения (сдвиг/сдвиг) не возникает — портал
 * с ограниченной камерой работает как "камера у точки выхода", а не как точное окно.
 *
 * Клиппинг за плоскостью выхода не нужен: выход портала — не "зеркальная" поверхность,
 * а точка в загруженной зоне (окно), лучи через экран портала проходят через "виртуальное
 * окно" в плоскости выхода и показывают содержимое зоны за ним. Клиппинг разрезал бы вид
 * ровно пополам произвольной вертикальной плоскостью.
 *
 * Работает для порталов в том же измерении (чанки у точки выхода должны быть загружены на клиенте);
 * для других измерений портал остаётся чёрным экраном.
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class PortalViewManager {
    private static final double MAX_VIEW_DISTANCE = 64.0D;
    /** Допуск выхода центра портала за пределы экрана (в NDC) — quad за краем экрана ещё рендерится. */
    private static final double MAX_SCREEN_EXTENT = 1.5D;
    /** Виртуальная камера не встаёт ближе этого к плоскости выхода (срез near-plane). */
    private static final double MIN_CAMERA_OFFSET = 0.075D;
    /** Максимальное боковое смещение виртуальной камеры от оси точки выхода. */
    private static final double MAX_LATERAL_OFFSET = 1.0D;
    /** Доли бокового смещения при попытке выйти из блока (тесная зона выхода). */
    private static final double[] LATERAL_FALLBACK = {1.0D, 0.5D, 0.0D};

    private static RenderTarget framebuffer;
    private static PortalEntity activePortal;
    private static Vec3 activeViewCameraPos;
    private static boolean renderingView;

    private PortalViewManager() {
    }

    /** true во время прохода вида: рендереры портала не рисуют экран (иначе — цикл обратной связи). */
    public static boolean isRenderingView() {
        return renderingView;
    }

    /** Отладочный доступ: id активного портала. */
    public static long getActivePortalId() {
        return activePortal == null ? -1L : activePortal.getId();
    }

    /** Рендерится ли вид для конкретного портала. */
    public static boolean hasViewFor(PortalEntity entity) {
        return activePortal == entity;
    }

    /** Позиция виртуальной камеры активного вида (null — вид не рендерился). */
    @org.jetbrains.annotations.Nullable
    public static Vec3 getActiveViewCameraPos() {
        return activeViewCameraPos;
    }

    /** GL-id цветовой текстуры фреймбуфера вида (0 — нет). */
    public static int getTextureId() {
        return framebuffer == null ? 0 : framebuffer.getColorTextureId();
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) {
            return;
        }
        if (renderingView) {
            // вложенный проход вида: мир уже рендерится обычным образом во фреймбуфер вида
            return;
        }
        renderPortalView(event);
    }

    private static void renderPortalView(RenderLevelStageEvent event) {
        activePortal = null;
        activeViewCameraPos = null;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        // в Fabulous-режиме renderLevel сам переключает фреймбуферы — вложенный проход сломал бы картинку
        if (mc.options.graphicsMode().get() == GraphicsStatus.FABULOUS) {
            return;
        }
        Camera camera = event.getCamera();
        if (camera.getEntity() == null) {
            return;
        }
        float partialTick = event.getPartialTick();

        PortalEntity portal = selectPortal(mc, camera, partialTick);
        if (portal == null) {
            return;
        }
        Vec3 exitBase = portal.getExitBase();
        Vec3 normal = portal.getFacingNormal(partialTick);

        // виртуальная камера: перенос позиции игрока через портал (взгляд сохраняется);
        // боковое смещение ограничено и выводится из блоков — иначе камера попадает
        // внутрь геометрии у точки выхода и вид "просвечивает" фантомами
        Vec3 camPos = buildViewCameraPos(mc, portal, camera, partialTick, exitBase, normal);

        ensureFramebuffer(mc);

        renderingView = true;
        // вложенный проход портит глобальное состояние рендера — сохраняем матрицу поворота
        // вида основного прохода (RenderSystem.setInverseViewRotationMatrix не восстанавливается
        // ванильным кодом после AFTER_SKY)
        Matrix3f mainInverseViewRotation = new Matrix3f(RenderSystem.getInverseViewRotationMatrix());
        try {
            PortalCamera portalCamera = new PortalCamera();
            portalCamera.setView(camPos.x, camPos.y, camPos.z, camera.getYRot(), camera.getXRot(),
                    mc.level, camera.getEntity(), partialTick);

            // ориентация вида = ориентация основной камеры (перенос без поворота) —
            // тогда фреймбуфер вида соответствует экрану 1:1, как его считает computeViewUvs
            PoseStack viewRotPose = new PoseStack();
            viewRotPose.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
            viewRotPose.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
            RenderSystem.setInverseViewRotationMatrix(new Matrix3f(viewRotPose.last().normal()).invert());

            framebuffer.bindWrite(true);
            // bindWrite НЕ очищает фреймбуфер — очищаем явно; маски записи на этот момент могут
            // быть не дефолтными (после рендера неба основного прохода), приводим их к обычным
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.depthMask(true);
            RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager._clearDepth(1.0D);
            RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

            // вложенный renderLevel наследует modelview-состояние основного прохода (там уже стоит
            // поворот главной камеры) — сбрасываем стек в identity, иначе вложенный мир и сущности
            // отрисуются с двойным поворотом
            PoseStack modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.pushPose();
            modelViewStack.setIdentity();
            RenderSystem.applyModelViewMatrix();

            // сущности/тайловые сущности ориентируются на камеру dispatcher'а — даём им виртуальную камеру
            mc.getEntityRenderDispatcher().prepare(mc.level, portalCamera, mc.crosshairPickEntity);
            mc.getBlockEntityRenderDispatcher().prepare(mc.level, portalCamera, mc.hitResult);
            try {
                mc.levelRenderer.prepareCullFrustum(viewRotPose, portalCamera.getPosition(), event.getProjectionMatrix());
                mc.getProfiler().push("portalView");
                try {
                    // та же матрица проекции, что у основного прохода — соответствие экрану 1:1
                    mc.levelRenderer.renderLevel(viewRotPose, partialTick, 0L, false,
                            portalCamera, mc.gameRenderer, mc.gameRenderer.lightTexture(), event.getProjectionMatrix());
                } finally {
                    mc.getProfiler().pop();
                }
            } finally {
                modelViewStack.popPose();
                RenderSystem.applyModelViewMatrix();
            }

            activePortal = portal;
            activeViewCameraPos = camPos;
        } finally {
            renderingView = false;
            RenderSystem.setInverseViewRotationMatrix(mainInverseViewRotation);
            mc.getMainRenderTarget().bindWrite(true);
            // вложенный renderLevel перезаписал состояние основного прохода — восстанавливаем
            mc.levelRenderer.prepareCullFrustum(event.getPoseStack(), camera.getPosition(), event.getProjectionMatrix());
            mc.getEntityRenderDispatcher().prepare(mc.level, camera, mc.crosshairPickEntity);
            mc.getBlockEntityRenderDispatcher().prepare(mc.level, camera, mc.hitResult);
        }
    }

    /**
     * Позиция виртуальной камеры вида: перенос позиции игрока через портал
     * (взгляд сохраняется), при этом:
     * <ul>
     *   <li>высота глаз сохраняется — камера остаётся в проверенной воздухом колонке точки выхода;</li>
     *   <li>боковое (горизонтальное, поперёк нормали) смещение ограничено MAX_LATERAL_OFFSET —
     *       иначе камера "гуляет" вокруг точки выхода вслед за игроком и попадает в геометрии;</li>
     *   <li>составляющая вдоль нормали сохраняется, но не меньше MIN_CAMERA_OFFSET —
     *       камера не встаёт точно на плоскость выхода (near-plane срезает геометрию у выхода).</li>
     * </ul>
     * Если камера всё же попала в блок (тесная зона выхода) — боковое смещение ужимается
     * к оси выхода, пока блок не освободится.
     */
    private static Vec3 buildViewCameraPos(Minecraft mc, PortalEntity portal, Camera camera,
                                           float partialTick, Vec3 exitBase, Vec3 normal) {
        Vec3 offset = camera.getPosition().subtract(portal.getPortalPlanePos(partialTick));
        double along = offset.dot(normal);
        double vertical = offset.y;
        // боковое смещение: всё, кроме составляющей вдоль нормали и вертикали
        Vec3 flat = offset.subtract(normal.scale(along)).subtract(0.0D, vertical, 0.0D);
        if (flat.lengthSqr() > MAX_LATERAL_OFFSET * MAX_LATERAL_OFFSET) {
            flat = flat.normalize().scale(MAX_LATERAL_OFFSET);
        }
        double alongSafe = Math.abs(along) < MIN_CAMERA_OFFSET
                ? (along >= 0.0D ? MIN_CAMERA_OFFSET : -MIN_CAMERA_OFFSET)
                : along;

        Vec3 eyeOffset = new Vec3(0.0D, vertical, 0.0D);
        Vec3 alongOffset = normal.scale(alongSafe);
        for (double k : LATERAL_FALLBACK) {
            Vec3 candidate = exitBase.add(flat.scale(k)).add(eyeOffset).add(alongOffset);
            if (!isSolidBlock(mc, candidate)) {
                return candidate;
            }
        }
        return exitBase.add(eyeOffset).add(alongOffset);
    }

    /** true, если блок в точке непрозрачный (камера изнутри такого блока рендерит "просвет"). */
    private static boolean isSolidBlock(Minecraft mc, Vec3 pos) {
        BlockPos blockPos = BlockPos.containing(pos.x, pos.y, pos.z);
        return mc.level.getBlockState(blockPos).isSolidRender(mc.level, blockPos);
    }

    /** Выбор портала: ближайший, попадающий на экран, с доступным видом. */
    private static PortalEntity selectPortal(Minecraft mc, Camera camera, float partialTick) {
        PoseStack pose = new PoseStack();
        pose.mulPoseMatrix(new Matrix4f(RenderSystem.getProjectionMatrix()));
        pose.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        pose.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
        Vec3 camPos = camera.getPosition();
        pose.translate(-camPos.x, -camPos.y, -camPos.z);
        Matrix4f projView = pose.last().pose();

        PortalEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof PortalEntity portal)) {
                continue;
            }
            if (!portal.canRenderView(partialTick)) {
                continue;
            }
            // попадание на экран проверяем по углам экрана портала (границы контура):
            // у близкого и широкого портала ЦЕНТР может быть далеко за краем экрана,
            // хотя квад виден — проверка по центру теряла вид ("потеря" картинки)
            if (!isPortalOnScreen(portal, partialTick, projView)) {
                continue;
            }
            Vec3 center = portal.getPortalPlanePos(partialTick).add(0.0D, 2.0D, 0.0D);
            double dist = camPos.distanceTo(center);
            if (dist > MAX_VIEW_DISTANCE || dist >= bestDist) {
                continue;
            }
            best = portal;
            bestDist = dist;
        }
        return best;
    }

    /** true, если хотя бы один угол экрана портала в пределах экрана (NDC) и перед камерой. */
    private static boolean isPortalOnScreen(PortalEntity portal, float partialTick, Matrix4f projView) {
        double[] contour = portal.getContourBounds(partialTick);
        double ey = Mth.lerp((double) partialTick, portal.yo, portal.getY());
        double ex = Mth.lerp((double) partialTick, portal.xo, portal.getX());
        double ez = Mth.lerp((double) partialTick, portal.zo, portal.getZ());
        Matrix4f rot = new Matrix4f().rotationY((float) Math.toRadians(180f - portal.getPortalYaw(partialTick)));

        float[][] corners = {
                {-(float) contour[2], (float) (contour[1] - ey)},
                {(float) contour[2], (float) (contour[1] - ey)},
                {(float) contour[2], (float) (contour[0] - ey)},
                {-(float) contour[2], (float) (contour[0] - ey)}
        };
        for (float[] corner : corners) {
            Vector3f local = new Vector3f(corner[0], corner[1], 0f);
            rot.transformPosition(local);
            // projView уже содержит перенос -camPos, поэтому подаём МИРОВЫЕ координаты
            Vector4f clip = new Vector4f(
                    (float) (ex + local.x),
                    (float) (ey + local.y),
                    (float) (ez + local.z),
                    1.0F);
            projView.transform(clip);
            if (clip.w <= 0.0F) {
                continue;
            }
            float sx = clip.x / clip.w;
            float sy = clip.y / clip.w;
            if (Math.abs(sx) <= MAX_SCREEN_EXTENT && Math.abs(sy) <= MAX_SCREEN_EXTENT) {
                return true;
            }
        }
        return false;
    }

    private static void ensureFramebuffer(Minecraft mc) {
        int width = mc.getWindow().getWidth();
        int height = mc.getWindow().getHeight();
        if (framebuffer == null) {
            framebuffer = new TextureTarget(width, height, true, Minecraft.ON_OSX);
        } else if (framebuffer.width != width || framebuffer.height != height) {
            framebuffer.resize(width, height, Minecraft.ON_OSX);
        }
    }
}
