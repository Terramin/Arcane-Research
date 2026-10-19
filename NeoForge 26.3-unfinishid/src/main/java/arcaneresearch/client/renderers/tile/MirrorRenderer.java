package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.tiles.devices.TileMirror;

// NEOFORGE_PORT (26.3 render-state rework): Forge renderMirror() body 1:1
// (frame, active tunnel portal, inactive pane) via submitCustomGeometry.
public class MirrorRenderer implements BlockEntityRenderer<TileMirror, MirrorRenderer.State> {

    private static final Identifier TUNNEL_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/tunnel.png");
    private static final Identifier PARTICLEFIELD_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/particlefield.png");
    private static final Identifier INACTIVE_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/block/mirrorpane.png");
    private static final Identifier ACTIVE_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/block/mirrorpanetrans.png");
    private static final Identifier FRAME_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/block/mirrorframe.png");
    private static final Identifier FRAME2_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/block/mirrorframe2.png");

    public static class State extends BlockEntityRenderState {
        public net.minecraft.core.BlockPos mirrorPos;
        public net.minecraft.world.level.Level level;
        public BlockState mirrorState;
        public boolean linked;
    }

    public MirrorRenderer(BlockEntityRendererProvider.Context context) {
    }

    static Identifier frame2Texture() {
        return FRAME2_TEXTURE;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileMirror tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.mirrorPos = tile.getBlockPos();
        state.level = tile.getLevel();
        state.mirrorState = tile.getBlockState();
        state.linked = tile.linked;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        renderMirror(state.mirrorPos, state.level, state.mirrorState,
                state.linked, FRAME_TEXTURE, poseStack, collector, state.lightCoords);
    }

    public static void renderMirror(net.minecraft.core.BlockPos pos,
                                    net.minecraft.world.level.Level level,
                                    BlockState state, boolean linked,
                                    Identifier frameTexture,
                                    PoseStack poseStack,
                                    SubmitNodeCollector collector, int packedLight) {
        Direction facing = getFacing(state);

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);
        applyRotation(poseStack, facing);

        renderFrame(poseStack, collector, frameTexture, packedLight);

        if (linked && isPlayerNearby(pos, level)) {
            renderActivePortal(poseStack, collector, pos, facing, packedLight);
        } else {
            renderInactivePane(poseStack, collector, packedLight);
        }

        poseStack.popPose();
    }

    /** Глубина рамки: выдавливание наружу, к зрителю (передняя грань ближе стекла). */
    private static final float FRAME_THICKNESS = 0.1f;

    private static void renderFrame(PoseStack poseStack, SubmitNodeCollector collector,
                                    Identifier texture, int packedLight) {
        poseStack.pushPose();
        // Та же конструкция 3D-модели из плоской текстуры, что у печатей
        // (SealRenderer) и пера стола (renderItemIn2D): передняя и задняя
        // грани + потэксельные стенки по периметру. Рисует в пространстве
        // 0..1, поэтому сдвигаем центр блока в угол, а переднюю грань
        // выносим наружу к зрителю (к стеклу).
        // Хитбокс блока — плита 2/16 у опорной стороны (локально z -0.5..-0.375).
        // Рамка целиком внутри плиты: задняя грань почти вровень с опорой
        // (-0.499, запас от z-fighting со стеной), передняя на -0.399.
        // Альфа текстур чистая 0/255, поэтому cutout: прозрачная середина
        // отбрасывается и не пишет глубину — стекло и портал за рамкой
        // (z -0.4375/-0.4475) остаются видны, а золото корректно их перекрывает.
        poseStack.translate(-0.5f, -0.5f, -0.499f + FRAME_THICKNESS);

        // Forge entityCutout(texture) 1:1 -> raw ENTITY_CUTOUT (memoized per
        // texture, RenderType identity kept for batching; raw vanilla
        // pipelines need no RegisterRenderPipelinesEvent registration).
        RenderType type = TCRenderTypes.objSolid(texture);
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            // Forge UtilsFX.renderTextureIn3D(poseStack, vc, 1, 1, 0, 0,
            // 16, 16, FRAME_THICKNESS, packedLight) 1:1, inlined: UtilsFX
            // takes a pose-less VertexConsumer, so it cannot run inside the
            // submitCustomGeometry lambda (pose transform would be lost).
            renderFrameGeometry(pose, consumer, packedLight);
        });

        poseStack.popPose();
    }

    private static void renderFrameGeometry(PoseStack.Pose pose, VertexConsumer consumer,
                                            int packedLight) {
        float t = FRAME_THICKNESS;
        frameQuad(consumer, pose, packedLight,
                0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F,
                1.0F, 0.0F, 0.0F, 0.0F, 0.0F,
                1.0F, 1.0F, 0.0F, 0.0F, 1.0F,
                0.0F, 1.0F, 0.0F, 1.0F, 1.0F);
        frameQuad(consumer, pose, packedLight,
                0.0F, 1.0F, -t, 1.0F, 1.0F, 0.0F, 0.0F, -1.0F,
                1.0F, 1.0F, -t, 0.0F, 1.0F,
                1.0F, 0.0F, -t, 0.0F, 0.0F,
                0.0F, 0.0F, -t, 1.0F, 0.0F);
        int width = 16, height = 16;
        float f5 = 0.5F * (1.0F - 0.0F) / width;
        float f6 = 0.5F * (0.0F - 1.0F) / height;
        for (int k = 0; k < width; k++) {
            float f7 = k / (float) width;
            float f8 = 1.0F + (0.0F - 1.0F) * f7 - f5;
            frameQuad(consumer, pose, packedLight,
                    f7, 0.0F, -t, f8, 0.0F, -1.0F, 0.0F, 0.0F,
                    f7, 0.0F, 0.0F, f8, 0.0F,
                    f7, 1.0F, 0.0F, f8, 1.0F,
                    f7, 1.0F, -t, f8, 1.0F);
        }
        for (int k = 0; k < width; k++) {
            float f7 = k / (float) width;
            float f8 = 1.0F + (0.0F - 1.0F) * f7 - f5;
            float f9 = f7 + 1.0F / width;
            frameQuad(consumer, pose, packedLight,
                    f9, 1.0F, -t, f8, 1.0F, 1.0F, 0.0F, 0.0F,
                    f9, 1.0F, 0.0F, f8, 1.0F,
                    f9, 0.0F, 0.0F, f8, 0.0F,
                    f9, 0.0F, -t, f8, 0.0F);
        }
        for (int k = 0; k < height; k++) {
            float f7 = k / (float) height;
            float f8 = 0.0F + (1.0F - 0.0F) * f7 - f6;
            float f9 = f7 + 1.0F / height;
            frameQuad(consumer, pose, packedLight,
                    0.0F, f9, 0.0F, 1.0F, f8, 0.0F, 1.0F, 0.0F,
                    1.0F, f9, 0.0F, 0.0F, f8,
                    1.0F, f9, -t, 0.0F, f8,
                    0.0F, f9, -t, 1.0F, f8);
        }
        for (int k = 0; k < height; k++) {
            float f7 = k / (float) height;
            float f8 = 0.0F + (1.0F - 0.0F) * f7 - f6;
            frameQuad(consumer, pose, packedLight,
                    1.0F, f7, 0.0F, 0.0F, f8, 0.0F, -1.0F, 0.0F,
                    0.0F, f7, 0.0F, 1.0F, f8,
                    0.0F, f7, -t, 1.0F, f8,
                    1.0F, f7, -t, 0.0F, f8);
        }
    }

    private static void frameQuad(VertexConsumer consumer, PoseStack.Pose pose, int packedLight,
                                  float x1, float y1, float z1, float u1, float v1,
                                  float nx, float ny, float nz,
                                  float x2, float y2, float z2, float u2, float v2,
                                  float x3, float y3, float z3, float u3, float v3,
                                  float x4, float y4, float z4, float u4, float v4) {
        consumer.addVertex(pose, x1, y1, z1)
                .setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                .setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x2, y2, z2)
                .setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u2, v2)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                .setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x3, y3, z3)
                .setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u3, v3)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                .setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x4, y4, z4)
                .setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u4, v4)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                .setNormal(pose, nx, ny, nz);
    }

    private static boolean isPlayerNearby(net.minecraft.core.BlockPos pos,
                                          net.minecraft.world.level.Level level) {
        if (level == null) return false;
        return Minecraft.getInstance().player != null &&
                Minecraft.getInstance().player.distanceToSqr(
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5) < 1024.0;
    }

    private static void renderActivePortal(PoseStack poseStack, SubmitNodeCollector collector,
                                           net.minecraft.core.BlockPos pos, Direction facing,
                                           int packedLight) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.gameRenderer.mainCamera() == null) {
            return;
        }
        net.minecraft.world.phys.Vec3 camPos =
                mc.gameRenderer.mainCamera().position();
        net.minecraft.world.entity.Entity view = mc.getCameraEntity();
        net.minecraft.world.phys.Vec3 playPos =
                // NEOFORGE_PORT: Entity.position(float) removed -> position().
                view != null ? view.position() : camPos;

        double[] camLocal = toLocal(facing, pos,
                camPos.x, camPos.y, camPos.z);
        double[] playLocal = toLocal(facing, pos,
                playPos.x, playPos.y, playPos.z);

        final double planeZ = -0.4475;
        double f6 = planeZ - camLocal[2];
        if (Math.abs(f6) < 1.0e-4) {
            f6 = f6 < 0.0 ? -1.0e-4 : 1.0e-4;
        }

        float time = (System.currentTimeMillis() % 700000L) / 250000.0f;
        java.util.Random random = new java.util.Random(31100L);
        float half = 0.5f - 0.1875f;

        for (int i = 0; i < 16; ++i) {
            float f2 = 16 - i;
            float f3 = 0.0625f;
            float f4 = 1.0f / (f2 + 1.0f);
            Identifier tex = PARTICLEFIELD_TEXTURE;
            boolean additive = true;
            if (i == 0) {
                tex = TUNNEL_TEXTURE;
                f4 = 0.1f;
                f2 = 65.0f;
                f3 = 0.125f;
                additive = false;
            }
            if (i == 1) {
                f3 = 0.5f;
            }

            float r = random.nextFloat() * 0.5f + 0.1f;
            float g = random.nextFloat() * 0.5f + 0.4f;
            float b = random.nextFloat() * 0.5f + 0.5f;
            if (i == 0) {
                r = (g = (b = 1.0f));
            }

            RenderType layer = TCRenderTypes.mirrorLayer(tex, additive);
            double shift = f2 / f6;
            double rot = Math.toRadians((i * i * 4321 + i * 9) * 2.0);
            double cos = Math.cos(rot);
            double sin = Math.sin(rot);
            float[][] corners = {
                    {-half, -half}, {-half, half}, {half, half}, {half, -half}
            };
            float[] quad = new float[4 * 5];
            for (int k = 0; k < 4; k++) {
                float[] corner = corners[k];
                double u = corner[0] + camLocal[0] * shift - playLocal[0];
                double v = corner[1] + camLocal[1] * shift - playLocal[1];
                u -= 0.5;
                v -= 0.5;
                double ru = u * cos - v * sin;
                double rv = u * sin + v * cos;
                u = ru + 0.5;
                v = rv + 0.5;
                u *= f3;
                v *= f3;
                v += time;
                quad[k * 5] = corner[0];
                quad[k * 5 + 1] = corner[1];
                quad[k * 5 + 2] = (float) u;
                quad[k * 5 + 3] = (float) v;
                quad[k * 5 + 4] = 0.0f;
            }
            final float fr = r * f4, fg = g * f4, fb = b * f4;
            collector.submitCustomGeometry(poseStack, layer, (pose, consumer) -> {
                for (int k = 0; k < 4; k++) {
                    consumer.addVertex(pose, quad[k * 5], quad[k * 5 + 1], (float) planeZ)
                            .setUv(quad[k * 5 + 2], quad[k * 5 + 3])
                            .setColor(fr, fg, fb, 1.0f)
                            .setOverlay(OverlayTexture.NO_OVERLAY)
                            .setLight(packedLight)
                            .setNormal(pose, 0, 0, 1);
                }
            });
        }

        renderOverlay(poseStack, collector, ACTIVE_TEXTURE, packedLight);
    }

    private static double[] toLocal(Direction facing, net.minecraft.core.BlockPos pos,
                                    double x, double y, double z) {
        double dx = x - (pos.getX() + 0.5);
        double dy = y - (pos.getY() + 0.5);
        double dz = z - (pos.getZ() + 0.5);
        return switch (facing) {
            case NORTH -> new double[]{-dx, dy, -dz};
            case WEST -> new double[]{dz, dy, -dx};
            case EAST -> new double[]{-dz, dy, dx};
            case DOWN -> new double[]{dx, dz, -dy};
            case UP -> new double[]{dx, -dz, dy};
            default -> new double[]{dx, dy, dz};
        };
    }

    private static void renderInactivePane(PoseStack poseStack, SubmitNodeCollector collector, int packedLight) {
        renderOverlay(poseStack, collector, INACTIVE_TEXTURE, packedLight);
    }

    private static void renderOverlay(PoseStack poseStack, SubmitNodeCollector collector,
                                      Identifier texture, int packedLight) {
        poseStack.pushPose();
        // Стекло ровно на половине хитбокса (-0.4375).
        poseStack.translate(0, 0, -0.4375f);

        RenderType type = TCRenderTypes.translucentPortal(texture);
        float size = 0.35f;
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            quad(consumer, pose, -size, -size, 0, 0, 0, packedLight, 1, 1, 1, 1);
            quad(consumer, pose, size, -size, 0, 1, 0, packedLight, 1, 1, 1, 1);
            quad(consumer, pose, size, size, 0, 1, 1, packedLight, 1, 1, 1, 1);
            quad(consumer, pose, -size, size, 0, 0, 1, packedLight, 1, 1, 1, 1);
        });

        poseStack.popPose();
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                             float x, float y, float z, float u, float v, int light,
                             float r, float g, float b, float a) {
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose, 0, 0, 1);
    }

    private static void applyRotation(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case DOWN -> poseStack.rotate(Axis.XP.rotationDegrees(90));
            case UP -> poseStack.rotate(Axis.XP.rotationDegrees(-90));
            case NORTH -> poseStack.rotate(Axis.YP.rotationDegrees(180));
            case SOUTH -> {
            }
            case WEST -> poseStack.rotate(Axis.YP.rotationDegrees(-90));
            case EAST -> poseStack.rotate(Axis.YP.rotationDegrees(90));
        }
    }

    private static Direction getFacing(BlockState state) {

        if (state.hasProperty(arcaneresearch.common.blocks.devices.BlockMirror.FACING)) {
            return state.getValue(arcaneresearch.common.blocks.devices.BlockMirror.FACING);
        }
        return Direction.NORTH;
    }
}