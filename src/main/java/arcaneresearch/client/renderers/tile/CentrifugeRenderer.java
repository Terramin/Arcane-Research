package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.block.CentrifugeModel;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.tiles.essentia.TileCentrifuge;



public class CentrifugeRenderer implements BlockEntityRenderer<TileCentrifuge, CentrifugeRenderer.State> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/centrifuge.png");

    public static class State extends BlockEntityRenderState {
        public float spinRotation;
    }

    private final CentrifugeModel model;

    public CentrifugeRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new CentrifugeModel(context.bakeLayer(CentrifugeModel.LAYER_LOCATION));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileCentrifuge tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.spinRotation = (float) Math.toRadians(tile.rotation);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        RenderType type = TCRenderTypes.objSolid(TEXTURE);
        int light = state.lightCoords;
        
        
        final float spin = state.spinRotation;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.scale(0.0625f, 0.0625f, 0.0625f);
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            PoseStack work = new PoseStack();
            work.last().pose().set(pose.pose());
            work.last().normal().set(pose.normal());
            model.setSpinRotation(spin);
            model.renderStaticParts(work, consumer, light, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
            model.renderSpinnyBit(work, consumer, light, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        });
        poseStack.popPose();
    }
}