package arcaneresearch.client.renderers.tile;
import arcaneresearch.common.tiles.aura.NodeConverterTile;

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

/**
 * Node converter renderer, ported from TC4 TileNodeConverterRenderer.
 * OBJ lock plus four count-driven pistons, glow overlay tinted by status
 * (green idle, orange charging, red energized).
 * NEOFORGE_PORT (26.3 render-state rework): Forge renderDevice() body 1:1
 * via submitCustomGeometry (PoseStack-bridge emission).
 */
public class NodeConverterRenderer implements BlockEntityRenderer<NodeConverterTile, NodeConverterRenderer.State> {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID,
            "models/block/node_converter.obj");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID,
            "textures/models/node_converter.png");
    private static final Identifier TEXTURE_OVER = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID,
            "textures/models/node_converter_over.png");

    public static class State extends BlockEntityRenderState {
        public boolean hasLevel;
        public int count;
        public int status;
    }

    public NodeConverterRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(NodeConverterTile tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.hasLevel = tile.getLevel() != null;
        state.count = tile.count;
        state.status = tile.status;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        if (!state.hasLevel) {
            return;
        }
        renderDevice(poseStack, collector, state.count, state.status, state.lightCoords);
    }

    public static void renderDevice(PoseStack poseStack, SubmitNodeCollector collector,
                                    int count, int status, int packedLight) {
        renderDevice(poseStack, collector, count, status, packedLight, OverlayTexture.NO_OVERLAY);
    }

    public static void renderDevice(PoseStack poseStack, SubmitNodeCollector collector,
                                    int count, int status, int packedLight, int packedOverlay) {
        TurretObjModel model = TurretObjModel.load(MODEL);
        if (model == null) {
            return;
        }
        float v = Math.min(50, Math.max(0, count)) / 137.0F;
        float[] glow = statusColor(status);
        int glowLight = glowLightForPiston(v, 0);
        int overlay = packedOverlay;

        poseStack.pushPose();
        poseStack.translate(0.5, 1.0, 0.5);
        poseStack.rotate(Axis.XP.rotationDegrees(90.0F));

        RenderType baseType = TCRenderTypes.objSolid(TEXTURE);
        RenderType overType = TCRenderTypes.objSolid(TEXTURE_OVER);
        emitGroup(poseStack, collector, baseType,
                (work, consumer) -> model.renderGroup(work, consumer, "lock", packedLight, overlay, 1.0F, 1.0F, 1.0F, 1.0F));
        emitGroup(poseStack, collector, overType,
                (work, consumer) -> model.renderGroup(work, consumer, "lock", glowLight, overlay,
                        glow[0], glow[1], glow[2], 1.0F));

        for (int a = 0; a < 4; a++) {
            poseStack.pushPose();
            poseStack.rotate(Axis.ZP.rotationDegrees(90.0F * a));
            poseStack.rotate(Axis.YP.rotationDegrees(45.0F));
            poseStack.translate(0.0, 0.0, v);
            final int pistonGlow = glowLightForPiston(v, a);
            emitGroup(poseStack, collector, baseType,
                    (work, consumer) -> model.renderGroup(work, consumer, "piston", packedLight, overlay,
                            1.0F, 1.0F, 1.0F, 1.0F));
            emitGroup(poseStack, collector, overType,
                    (work, consumer) -> model.renderGroup(work, consumer, "piston", pistonGlow, overlay,
                            glow[0], glow[1], glow[2], 1.0F));
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void emitGroup(PoseStack poseStack, SubmitNodeCollector collector, RenderType type,
                                  GroupDraw draw) {
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            PoseStack work = new PoseStack();
            work.last().pose().set(pose.pose());
            work.last().normal().set(pose.normal());
            draw.draw(work, consumer);
        });
    }

    @FunctionalInterface
    private interface GroupDraw {
        void draw(PoseStack poseStack, VertexConsumer consumer);
    }

    private static float[] statusColor(int status) {
        if (status == 2) {
            return new float[]{1.0F, 0.0F, 0.3F};
        }
        if (status == 1) {
            return new float[]{1.0F, 0.6F, 0.1F};
        }
        return new float[]{0.5F, 1.0F, 0.5F};
    }

    private static int glowLightForPiston(float v, int offset) {
        int ticks = 0;
        try {
            if (Minecraft.getInstance().player != null) {
                ticks = Minecraft.getInstance().player.tickCount + offset * 5;
            }
        } catch (Exception ignored) {
        }
        float scale = Mth.sin(ticks / 3.0F) * 0.1F + 0.9F;
        int glow = 50 + (int) (170.0F * v * 2.5F * scale);
        return packLight(Math.min(15, Math.max(0, glow >> 4)), 0);
    }

    // NEOFORGE_PORT: LightTexture removed in 26.x; same packing
    // (block << 4 | sky << 20, as in old LightTexture.pack).
    private static int packLight(int blockLight, int skyLight) {
        return (blockLight & 0xF) << 4 | (skyLight & 0xF) << 20;
    }
}