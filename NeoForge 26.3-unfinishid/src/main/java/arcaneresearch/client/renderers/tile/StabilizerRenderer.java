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
import arcaneresearch.client.renderers.entity.TurretObjModel;
import arcaneresearch.common.tiles.aura.StabilizerTile;

/**
 * Node stabilizer renderer, ported from TC4 TileNodeStabilizerRenderer.
 * Same lock + four pistons OBJ as the converter (shared geometry in TC4),
 * pistons driven by count/100, glow 50+170*count/37, red pistons and pink
 * bubble for the advanced lattice (lock 2), white bubble otherwise.
 * NEOFORGE_PORT (26.3 render-state rework): Forge renderDevice() body 1:1
 * via submitCustomGeometry.
 */
public class StabilizerRenderer implements BlockEntityRenderer<StabilizerTile, StabilizerRenderer.State> {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID,
            "models/block/node_converter.obj");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID,
            "textures/models/node_stabilizer.png");
    private static final Identifier TEXTURE_OVER = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID,
            "textures/models/node_stabilizer_over.png");
    private static final Identifier BUBBLE = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID,
            "textures/misc/node_bubble.png");

    public static class State extends BlockEntityRenderState {
        public boolean hasLevel;
        public int count;
        public int lock;
        public int playerTickCount;
    }

    public StabilizerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(StabilizerTile tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.hasLevel = tile.getLevel() != null;
        state.count = tile.count;
        state.lock = tile.getLock();
        state.playerTickCount = Minecraft.getInstance().player != null
                ? Minecraft.getInstance().player.tickCount : 0;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        if (!state.hasLevel) {
            return;
        }
        renderDevice(poseStack, collector, state.count, state.lock, state.lightCoords,
                state.playerTickCount, true);
    }

    public static void renderDevice(PoseStack poseStack, SubmitNodeCollector collector,
                                    int count, int lock, int packedLight, int playerTickCount) {
        renderDevice(poseStack, collector, count, lock, packedLight, playerTickCount, true);
    }

    /**
     * @param bubble РїСѓР·С‹СЂСЊ РЅР°Рґ РґРµРІР°Р№СЃРѕРј (РІ РјРёСЂРµ вЂ” РґР°, РЅР° РїСЂРµРґРјРµС‚Рµ вЂ” РЅРµС‚).
     */
    public static void renderDevice(PoseStack poseStack, SubmitNodeCollector collector,
                                    int count, int lock, int packedLight, int playerTickCount,
                                    boolean bubble) {
        TurretObjModel model = TurretObjModel.load(MODEL);
        if (model == null) {
            return;
        }
        float v = Math.max(0, count) / 100.0F;
        float[] glow = lock == 2
                ? new float[]{1.0F, 0.2F, 0.2F}
                : new float[]{1.0F, 1.0F, 1.0F};
        int glowLight = glowLight(count, playerTickCount);
        int overlay = OverlayTexture.NO_OVERLAY;

        poseStack.pushPose();
        poseStack.translate(0.5, 1.0, 0.5);
        poseStack.rotate(Axis.XP.rotationDegrees(90.0F));

        RenderType baseType = TCRenderTypes.objSolid(TEXTURE);
        emitObj(poseStack, collector, baseType, model, "lock", packedLight, overlay, 1, 1, 1, 1);

        RenderType overType = TCRenderTypes.objSolid(TEXTURE_OVER);
        for (int a = 0; a < 4; a++) {
            poseStack.pushPose();
            poseStack.rotate(Axis.ZP.rotationDegrees(90.0F * a));
            poseStack.rotate(Axis.YP.rotationDegrees(45.0F));
            poseStack.translate(0.0, 0.0, v);
            emitObj(poseStack, collector, baseType, model, "piston", packedLight, overlay, 1, 1, 1, 1);
            emitObj(poseStack, collector, overType, model, "piston", glowLight, overlay,
                    glow[0], glow[1], glow[2], 1);
            poseStack.popPose();
        }
        poseStack.popPose();

        if (bubble && count > 0) {
            float alphaPulse = Mth.sin(playerTickCount / 8.0F) * 0.1F + 0.5F;
            float alpha = count / 37.0F * alphaPulse;
            int color = lock == 1 ? 0xFFFFFF : 16729156;
            float cr = ((color >> 16) & 0xFF) / 255.0F;
            float cg = ((color >> 8) & 0xFF) / 255.0F;
            float cb = (color & 0xFF) / 255.0F;
            RenderType bubbleType = TCRenderTypes.additiveTextured(BUBBLE);
            int fullLight = 0xF000F0;
            poseStack.pushPose();
            poseStack.translate(0.5, 1.5, 0.5);
            poseStack.rotate(Minecraft.getInstance().gameRenderer.mainCamera().rotation());
            float half = 0.9F;
            collector.submitCustomGeometry(poseStack, bubbleType, (pose, consumer) -> {
                // NEOFORGE_PORT: same vert/UV order as UtilsFX.renderFacingQuad 1x1f0.
                quad(consumer, pose, -half, -half, 0, 1, 1, cr, cg, cb, alpha, fullLight);
                quad(consumer, pose, -half, half, 0, 1, 0, cr, cg, cb, alpha, fullLight);
                quad(consumer, pose, half, half, 0, 0, 0, cr, cg, cb, alpha, fullLight);
                quad(consumer, pose, half, -half, 0, 0, 1, cr, cg, cb, alpha, fullLight);
            });
            poseStack.popPose();
        }
    }

    private static void emitObj(PoseStack poseStack, SubmitNodeCollector collector, RenderType type,
                                TurretObjModel model, String group, int light, int overlay,
                                float r, float g, float b, float a) {
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            PoseStack work = new PoseStack();
            work.last().pose().set(pose.pose());
            work.last().normal().set(pose.normal());
            model.renderGroup(work, consumer, group, light, overlay, r, g, b, a);
        });
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                             float x, float y, float z, float u, float v,
                             float r, float g, float b, float a, int light) {
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose, 0, 0, 1);
    }

    private static int glowLight(int count, int playerTickCount) {
        float scale = Mth.sin(playerTickCount / 3.0F) * 0.1F + 0.9F;
        int glow = 50 + (int) (170.0F * Math.max(0, count) / 37.0F * scale);
        // NEOFORGE_PORT: LightTexture removed in 26.x; same packing.
        return (Math.min(15, Math.max(0, glow >> 4)) & 0xF) << 4;
    }
}