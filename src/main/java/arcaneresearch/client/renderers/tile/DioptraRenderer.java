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
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.tiles.devices.TileDioptra;

import java.awt.*;

import java.awt.*;

// NEOFORGE_PORT (26.3 render-state rework): Forge render() body 1:1
// (vis wave grid + side panels).
public class DioptraRenderer implements BlockEntityRenderer<TileDioptra, DioptraRenderer.State> {

    private static final Identifier GRID_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/gridblock.png");
    private static final Identifier SIDE_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/dioptra_side.png");

    public static class State extends BlockEntityRenderState {
        public boolean hasLevel;
        public boolean showingVis;
        public float ticks;
        // NEOFORGE_PORT: 13x13 grid stashed (level reads forbidden in submit).
        public int[] grid = new int[13 * 13];
        public int counter;
    }

    public DioptraRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileDioptra tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.hasLevel = tile.getLevel() != null;
        Minecraft mc = Minecraft.getInstance();
        state.ticks = (mc.player != null ? mc.player.tickCount : 0) + partialTicks;
        state.showingVis = tile.isDisplayingVis();
        for (int a = 0; a < 13; a++) {
            for (int b = 0; b < 13; b++) {
                state.grid[a * 13 + b] = tile.getGridValue(a, b);
            }
        }
        state.counter = tile.counter;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        if (!state.hasLevel) {
            return;
        }
        float ticks = state.ticks;
        float rc, gc, bc;

        if (state.showingVis) {

            rc = Mth.sin(ticks / 12.0f) * 0.05f + 0.85f;
            gc = Mth.sin(ticks / 11.0f) * 0.05f + 0.9f;
            bc = Mth.sin(ticks / 10.0f) * 0.05f + 0.95f;
        } else {

            rc = Mth.sin(ticks / 12.0f) * 0.05f + 0.85f;
            gc = Mth.sin(ticks / 11.0f) * 0.05f + 0.45f;
            bc = Mth.sin(ticks / 10.0f) * 0.05f + 0.95f;
        }

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        renderGrid(state, poseStack, collector, rc, gc, bc, ticks);

        renderSidePanels(poseStack, collector, rc, gc, bc, state.lightCoords);

        poseStack.popPose();
    }

    private void renderGrid(State state, PoseStack poseStack, SubmitNodeCollector collector,
                            float r, float g, float b, float ticks) {

        poseStack.pushPose();
        poseStack.translate(-0.495, 0.501, -0.495);
        poseStack.scale(0.99f, 1.0f, 0.99f);

        RenderType type = TCRenderTypes.additiveTextured(GRID_TEXTURE);

        // NEOFORGE_PORT: same 12x12 quads + skirts as Forge, one submit per quad.
        for (int a = 0; a < 12; a++) {
            for (int bb = 0; bb < 12; bb++) {

                float h00 = gridAt(state, a, bb) / 96.0f;
                float h10 = gridAt(state, a + 1, bb) / 96.0f;
                float h11 = gridAt(state, a + 1, bb + 1) / 96.0f;
                float h01 = gridAt(state, a, bb + 1) / 96.0f;

                float x0 = a / 12.0f;
                float x1 = (a + 1) / 12.0f;
                float z0 = bb / 12.0f;
                float z1 = (bb + 1) / 12.0f;

                double d3 = a - 6;
                double d4 = bb - 6;
                double dis = Math.sqrt(d3 * d3 + d4 * d4);
                float wave = Mth.sin((float) ((state.counter - dis * 10.0) / 8.0));

                int brightness = (int) (200.0f + wave * 15.0f);
                int light = packLight(Math.round(brightness / 16.0f), 0);

                Color c = new Color(r * 0.8f, g, b);
                float cr = c.getRed() / 255.0f;
                float cg = c.getGreen() / 255.0f;
                float cb = c.getBlue() / 255.0f;
                float alpha = 0.9f;

                emitQuad(poseStack, collector, type,
                        x0, h01, z1, 0, 0, x1, h11, z1, 1, 0,
                        x1, h10, z0, 1, 1, x0, h00, z0, 0, 1,
                        cr, cg, cb, alpha, light);

                if (a == 0) {
                    emitQuad(poseStack, collector, type,
                            0, 0, z1, 0, 0, 0, h01, z1, 1, 0,
                            0, h00, z0, 1, 1, 0, 0, z0, 0, 1,
                            cr, cg, cb, 0f, alpha, alpha, 0f, light);
                }
                if (a == 11) {
                    emitQuad(poseStack, collector, type,
                            1, 0, z0, 0, 1, 1, h10, z0, 1, 1,
                            1, h11, z1, 1, 0, 1, 0, z1, 0, 0,
                            cr, cg, cb, 0f, alpha, alpha, 0f, light);
                }
                if (bb == 0) {
                    emitQuad(poseStack, collector, type,
                            x0, h00, 0, 0, 0, x1, h10, 0, 1, 0,
                            x1, 0, 0, 1, 1, x0, 0, 0, 0, 1,
                            cr, cg, cb, alpha, alpha, 0f, 0f, light);
                }
                if (bb == 11) {
                    emitQuad(poseStack, collector, type,
                            x0, 0, 1, 0, 1, x1, 0, 1, 1, 1,
                            x1, h11, 1, 1, 0, x0, h01, 1, 0, 0,
                            cr, cg, cb, 0f, 0f, alpha, alpha, light);
                }
            }
        }

        poseStack.popPose();
    }

    private static int gridAt(State state, int a, int b) {
        return state.grid[a * 13 + b];
    }

    // NEOFORGE_PORT: LightTexture removed in 26.x; same packing (block << 4 | sky << 20).
    private static int packLight(int blockLight, int skyLight) {
        return (blockLight & 0xF) << 4 | (skyLight & 0xF) << 20;
    }

    private static void emitQuad(PoseStack poseStack, SubmitNodeCollector collector, RenderType type,
                                 float x1, float y1, float z1, float u1, float v1,
                                 float x2, float y2, float z2, float u2, float v2,
                                 float x3, float y3, float z3, float u3, float v3,
                                 float x4, float y4, float z4, float u4, float v4,
                                 float r, float g, float b,
                                 float a1, float a2, float a3, float a4, int light) {
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            quad(consumer, pose, x1, y1, z1, u1, v1, r, g, b, a1, light);
            quad(consumer, pose, x2, y2, z2, u2, v2, r, g, b, a2, light);
            quad(consumer, pose, x3, y3, z3, u3, v3, r, g, b, a3, light);
            quad(consumer, pose, x4, y4, z4, u4, v4, r, g, b, a4, light);
        });
    }

    private static void emitQuad(PoseStack poseStack, SubmitNodeCollector collector, RenderType type,
                                 float x1, float y1, float z1, float u1, float v1,
                                 float x2, float y2, float z2, float u2, float v2,
                                 float x3, float y3, float z3, float u3, float v3,
                                 float x4, float y4, float z4, float u4, float v4,
                                 float r, float g, float b, float a, int light) {
        emitQuad(poseStack, collector, type,
                x1, y1, z1, u1, v1, x2, y2, z2, u2, v2,
                x3, y3, z3, u3, v3, x4, y4, z4, u4, v4,
                r, g, b, a, a, a, a, light);
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                             float x, float y, float z, float u, float v,
                             float r, float g, float b, float a, int light) {
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose, 0, 0, 1);
    }

    private void renderSidePanels(PoseStack poseStack, SubmitNodeCollector collector,
                                  float r, float g, float b, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0, 1.0, 0);
        poseStack.rotate(Axis.ZP.rotationDegrees(270));

        RenderType type = TCRenderTypes.additiveTexturedNoCull(SIDE_TEXTURE);
        int fullLight = packLight(14, 0);

        for (int q = 0; q < 4; q++) {
            poseStack.pushPose();
            poseStack.rotate(Axis.XP.rotationDegrees(90.0f * q));
            poseStack.translate(0, 0, -0.5);

            renderCenteredQuad(poseStack, collector, type, 1.0f, r, g, b, 0.8f, fullLight);

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private void renderCenteredQuad(PoseStack poseStack, SubmitNodeCollector collector, RenderType type,
                                    float size, float r, float g, float b, float a, int light) {
        float half = size / 2.0f;
        emitQuad(poseStack, collector, type,
                -half, -half, 0, 0, 1,
                half, -half, 0, 1, 1,
                half, half, 0, 1, 0,
                -half, half, 0, 0, 0,
                r, g, b, a, light);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    // NEOFORGE_PORT: render bounds moved from BlockEntity to the renderer in 26.x
    // (IBlockEntityRendererExtension); forward the tile's Forge-parity culling box.
    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(TileDioptra blockEntity) {
        return blockEntity.getRenderBoundingBox();
    }
}
