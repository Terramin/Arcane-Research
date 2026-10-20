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
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import org.joml.Vector3f;
import arcaneresearch.common.tiles.crafting.TileVoidSiphon;

// NEOFORGE_PORT (26.3 render-state rework): Forge renderVoidBox() body 1:1
// (two nested end-portal boxes; custom GL types -> vanilla endPortal()).
public class VoidSiphonRenderer implements BlockEntityRenderer<TileVoidSiphon, VoidSiphonRenderer.State> {

    public static class State extends BlockEntityRenderState {
        public boolean enabled = true;
    }

    public VoidSiphonRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileVoidSiphon tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        boolean enabled = true;
        if (tile.getLevel() != null) {
            BlockState blockState =
                    tile.getLevel().getBlockState(tile.getBlockPos());
            if (blockState.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.ENABLED)
                    && !blockState.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.ENABLED)) {
                enabled = false;
            }
        }
        state.enabled = enabled;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        if (!state.enabled) {
            return;
        }

        renderVoidBox(poseStack, collector, 0.875f, 0.126f, 0.2f, 0.2f, 0.2f, true);

        renderVoidBox(poseStack, collector, 0.3125f, 0.26f, 0.25f, 0.5f, 0.25f, false);
    }

    private void renderVoidBox(PoseStack poseStack, SubmitNodeCollector collector,
                               float height, float offset,
                               float sx, float sy, float sz, boolean alwaysFlat) {
        poseStack.pushPose();
        poseStack.translate(0.5, height, 0.5);

        // NEOFORGE_PORT: custom void types gone; vanilla endPortal() shader
        // (same portal look; additive/translucent split approximated by one pass).
        RenderType type = RenderTypes.endPortal();

        for (Direction face : Direction.values()) {
            poseStack.pushPose();

            org.joml.Vector3f axis = new org.joml.Vector3f(
                    -face.getStepY(), face.getStepX(), -face.getStepZ());
            poseStack.rotate(Axis.of(axis).rotationDegrees(90.0f));

            if (face.getStepZ() < 0) {
                poseStack.translate(0, 0, offset);
                poseStack.rotate(Axis.YP.rotationDegrees(180.0f));
            } else {
                poseStack.translate(0, 0, -offset);
            }

            if (alwaysFlat || face.getStepZ() != 0) {
                poseStack.rotate(Axis.ZN.rotationDegrees(90.0f));
            }

            poseStack.scale(sx, sy, sz);

            float half = 0.5f;
            collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
                portalVert(consumer, pose, -half, -half);
                portalVert(consumer, pose, half, -half);
                portalVert(consumer, pose, half, half);
                portalVert(consumer, pose, -half, half);
            });

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static void portalVert(VertexConsumer consumer, PoseStack.Pose pose, float x, float y) {
        // NEOFORGE_PORT: Forge СЃР»Р°Р» С‚РѕР»СЊРєРѕ РїРѕР·РёС†РёРё (POSITION-С„РѕСЂРјР°С‚ РїРѕСЂС‚Р°Р»Р°).
        consumer.addVertex(pose, x, y, 0);
    }
}