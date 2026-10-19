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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.models.block.ResearchTableModel;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.blocks.crafting.BlockResearchTable;
import arcaneresearch.common.tiles.crafting.TileResearchTable;

import java.awt.*;

// NEOFORGE_PORT (26.3 render-state rework, verified in 26.3 jars):
// Forge draw logic (facing rotation, alchemy-tinted scroll, inkwell, 2D quill)
// via submitCustomGeometry bridge + submitModelPart; quill as textured quad
// (UtilsFX.renderItemIn2D lost its texture arg in the port).
public class ResearchTableRenderer implements BlockEntityRenderer<TileResearchTable, ResearchTableRenderer.State> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/block/research_table_model.png");
    private static final Identifier QUILL_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/research/quill.png");

    public static class State extends BlockEntityRenderState {
        public Direction facing = Direction.NORTH;
        public boolean hasResearchData;
        public boolean hasScribeTools;
    }

    private final ResearchTableModel tableModel;

    public ResearchTableRenderer(BlockEntityRendererProvider.Context context) {
        this.tableModel = new ResearchTableModel(context.bakeLayer(ResearchTableModel.LAYER_LOCATION));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileResearchTable tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.facing = getFacing(tile.getBlockState());
        state.hasResearchData = tile.hasResearchData();
        state.hasScribeTools = tile.hasScribeTools();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        Direction facing = state.facing;

        poseStack.pushPose();

        poseStack.translate(0.5, 1.0, 0.5);

        poseStack.rotate(Axis.XP.rotationDegrees(180));

        applyRotation(poseStack, facing);

        RenderType cutoutType = TCRenderTypes.objSolid(TEXTURE);
        int light = state.lightCoords;

        if (state.hasResearchData) {
            Color color = new Color(Aspect.ALCHEMY.getColor());
            float r = color.getRed() / 255f;
            float g = color.getGreen() / 255f;
            float b = color.getBlue() / 255f;
            collector.submitCustomGeometry(poseStack, cutoutType, (pose, consumer) -> {
                PoseStack work = bridge(pose);
                tableModel.renderScroll(work, consumer, light, OverlayTexture.NO_OVERLAY, r, g, b, 1.0f);
            });
        }

        if (state.hasScribeTools) {
            collector.submitCustomGeometry(poseStack, cutoutType, (pose, consumer) -> {
                PoseStack work = bridge(pose);
                tableModel.renderInkwell(work, consumer, light, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
            });

            poseStack.pushPose();

            poseStack.rotate(Axis.XP.rotationDegrees(180));
            poseStack.translate(-0.3, 0.05, -0.2);
            poseStack.rotate(Axis.YP.rotationDegrees(60));

            poseStack.translate(0.25f, 0.25f, 0.0f);
            poseStack.rotate(Axis.ZP.rotationDegrees(180));
            poseStack.translate(-0.25f, -0.25f, 0.0f);
            poseStack.scale(0.5f, 0.5f, 0.5f);

            RenderType quillType = TCRenderTypes.translucentPortal(QUILL_TEXTURE);
            // Forge parity: UtilsFX.renderItemIn2D -> renderTextureIn3D builds a
            // thick double-sided item (front normal +Z at z=0, back normal -Z at
            // z=-thickness, U-mirrored). A single front-only quad is backface-culled
            // by ENTITY_TRANSLUCENT (cull=true), so the quill vanishes from behind
            // and flickers at grazing/close camera angles. Restore both faces.
            float quillThickness = 0.0625f;
            collector.submitCustomGeometry(poseStack, quillType, (pose, consumer) -> {
                quad(consumer, pose, 0, 0, 0, 1, 0, light, 0, 0, 1);
                quad(consumer, pose, 1, 0, 0, 0, 0, light, 0, 0, 1);
                quad(consumer, pose, 1, 1, 0, 0, 1, light, 0, 0, 1);
                quad(consumer, pose, 0, 1, 0, 1, 1, light, 0, 0, 1);
                quad(consumer, pose, 0, 1, -quillThickness, 1, 1, light, 0, 0, -1);
                quad(consumer, pose, 1, 1, -quillThickness, 0, 1, light, 0, 0, -1);
                quad(consumer, pose, 1, 0, -quillThickness, 0, 0, light, 0, 0, -1);
                quad(consumer, pose, 0, 0, -quillThickness, 1, 0, light, 0, 0, -1);
            });

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static PoseStack bridge(PoseStack.Pose pose) {
        PoseStack work = new PoseStack();
        work.last().pose().set(pose.pose());
        work.last().normal().set(pose.normal());
        return work;
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                             float x, float y, float z, float u, float v, int light,
                             float nx, float ny, float nz) {
        consumer.addVertex(pose, x, y, z)
                .setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    private void applyRotation(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case EAST -> poseStack.rotate(Axis.YP.rotationDegrees(90));
            case WEST -> poseStack.rotate(Axis.YP.rotationDegrees(270));
            case SOUTH -> poseStack.rotate(Axis.YP.rotationDegrees(180));
            default -> {
            }
        }
    }

    private Direction getFacing(BlockState state) {
        if (state.hasProperty(BlockResearchTable.FACING)) {
            return state.getValue(BlockResearchTable.FACING);
        }
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        return Direction.NORTH;
    }
}