package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.IEssentiaTransport;
import arcaneresearch.client.lib.UtilsFX;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.tiles.essentia.TileAlembic;

import java.awt.*;

// NEOFORGE_PORT (26.3 render-state rework): Forge render() body 1:1
// (aspect label + transport nozzles).
public class AlembicRenderer implements BlockEntityRenderer<TileAlembic, AlembicRenderer.State> {

    private static final Identifier LABEL_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/label.png");
    private static final Identifier BORE_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/bore.png");

    public static class State extends BlockEntityRenderState {
        public Aspect filter;
        public int facing;
        // NEOFORGE_PORT: transport topology stashed (level reads forbidden in submit).
        public boolean[] nozzleDir = new boolean[4];
    }

    private final arcaneresearch.client.models.block.BellowsNozzleModel nozzleModel;

    public AlembicRenderer(BlockEntityRendererProvider.Context context) {
        this.nozzleModel = new arcaneresearch.client.models.block.BellowsNozzleModel(
                context.bakeLayer(arcaneresearch.client.models.block.BellowsNozzleModel.LAYER_LOCATION));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileAlembic tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.filter = tile.getAspectFilter();
        state.facing = tile.getFacing();
        if (tile.getLevel() != null) {
            int i = 0;
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                boolean out = false;
                if (tile.canOutputTo(dir)) {
                    BlockEntity te = tile.getLevel().getBlockEntity(tile.getBlockPos().relative(dir));
                    if (te instanceof IEssentiaTransport transport) {
                        out = transport.canInputFrom(dir.getOpposite());
                    }
                }
                state.nozzleDir[i++] = out;
            }
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        if (state.filter != null) {
            renderLabel(state.filter, state.facing, poseStack, collector, state.lightCoords);
        }

        int i = 0;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (state.nozzleDir[i++]) {
                renderNozzle(dir, poseStack, collector, state.lightCoords);
            }
        }
    }

    private void renderLabel(Aspect aspect, int facing, PoseStack poseStack,
                             SubmitNodeCollector collector, int packedLight) {

        poseStack.pushPose();

        poseStack.translate(0.5, 0.0, 0.5);

        switch (facing) {
            case 2 -> poseStack.rotate(Axis.YP.rotationDegrees(0));
            case 3 -> poseStack.rotate(Axis.YP.rotationDegrees(180));
            case 4 -> poseStack.rotate(Axis.YP.rotationDegrees(90));
            case 5 -> poseStack.rotate(Axis.YP.rotationDegrees(270));
        }

        poseStack.translate(0, 0.5, -0.376);

        RenderType labelType = TCRenderTypes.translucentPortal(LABEL_TEXTURE);
        float size = 0.22f;
        collector.submitCustomGeometry(poseStack, labelType, (pose, consumer) -> {
            quad(consumer, pose, -size, -size, 0, 0, 1, packedLight, 1, 1, 1, 1);
            quad(consumer, pose, size, -size, 0, 1, 1, packedLight, 1, 1, 1, 1);
            quad(consumer, pose, size, size, 0, 1, 0, packedLight, 1, 1, 1, 1);
            quad(consumer, pose, -size, size, 0, 0, 0, packedLight, 1, 1, 1, 1);
        });

        poseStack.translate(0, 0, -0.001);

        Color color = new Color(aspect.getColor());
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;

        // NEOFORGE_PORT (Forge 1:1): aspect bg stripped via the shared helper.
        RenderType iconType = TCRenderTypes.translucentPortal(UtilsFX.strippedAspectTexture(aspect.getImage()));
        float iconSize = 0.15f;
        collector.submitCustomGeometry(poseStack, iconType, (pose, consumer) -> {
            quad(consumer, pose, -iconSize, -iconSize, 0, 0, 1, 0x00F000F0, r, g, b, 1);
            quad(consumer, pose, iconSize, -iconSize, 0, 1, 1, 0x00F000F0, r, g, b, 1);
            quad(consumer, pose, iconSize, iconSize, 0, 1, 0, 0x00F000F0, r, g, b, 1);
            quad(consumer, pose, -iconSize, iconSize, 0, 0, 0, 0x00F000F0, r, g, b, 1);
        });

        poseStack.popPose();
    }

    private void renderNozzle(Direction dir, PoseStack poseStack,
                              SubmitNodeCollector collector, int packedLight) {

        poseStack.pushPose();

        poseStack.translate(0.5, 0.0, 0.5);

        switch (dir) {
            case DOWN -> {
                poseStack.translate(-0.5, 0.5, 0.0);
                poseStack.rotate(Axis.ZP.rotationDegrees(-90));
            }
            case UP -> {
                poseStack.translate(0.5, 0.5, 0.0);
                poseStack.rotate(Axis.ZP.rotationDegrees(90));
            }
            case NORTH -> poseStack.rotate(Axis.YP.rotationDegrees(90));
            case SOUTH -> poseStack.rotate(Axis.YP.rotationDegrees(270));
            case WEST -> poseStack.rotate(Axis.YP.rotationDegrees(180));
            case EAST -> {
            }
            default -> {
            }
        }

        RenderType nozzleType = TCRenderTypes.objSolid(BORE_TEXTURE);
        collector.submitCustomGeometry(poseStack, nozzleType, (pose, consumer) -> {
            PoseStack work = new PoseStack();
            work.last().pose().set(pose.pose());
            work.last().normal().set(pose.normal());
            nozzleModel.renderToBuffer(work, consumer, packedLight,
                    OverlayTexture.NO_OVERLAY,
                    1.0F, 1.0F, 1.0F, 1.0F);
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
}