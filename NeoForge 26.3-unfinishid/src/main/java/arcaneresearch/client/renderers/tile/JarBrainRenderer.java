package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.block.BrainModel;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.tiles.essentia.TileJarBrain;

// NEOFORGE_PORT (26.3 render-state rework): Forge render() body 1:1
// (rotating brain + brine fittings).
public class JarBrainRenderer implements BlockEntityRenderer<TileJarBrain, JarBrainRenderer.State> {

    private static final Identifier BRAIN_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/brain2.png");
    private static final Identifier BRINE_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/jarbrine.png");

    public static class State extends BlockEntityRenderState {
        public float rotation;
        public float yOffset;
    }

    private final BrainModel brainModel;
    private final arcaneresearch.client.models.block.JarModel fittings;

    public JarBrainRenderer(BlockEntityRendererProvider.Context context) {
        this.brainModel = new BrainModel(context.bakeLayer(BrainModel.LAYER_LOCATION));
        this.fittings = new arcaneresearch.client.models.block.JarModel(
                context.bakeLayer(arcaneresearch.client.models.block.JarModel.LAYER_LOCATION));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileJarBrain tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.rotation = Mth.lerp(partialTicks, tile.brainRotationPrev, tile.brainRotation);
        state.yOffset = Mth.lerp(partialTicks, tile.brainYPrev, tile.brainY);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        int light = state.lightCoords;

        poseStack.pushPose();

        poseStack.translate(0.5, 0.01, 0.5);
        poseStack.rotate(Axis.XP.rotationDegrees(180.0F));
        poseStack.translate(0.0F, -0.77F + state.yOffset, 0.0F);
        poseStack.rotate(Axis.YP.rotationDegrees(state.rotation));
        poseStack.rotate(Axis.YP.rotationDegrees(-90.0F));

        float scale = 0.4F;
        poseStack.scale(scale, scale, scale);

        RenderType brainType = TCRenderTypes.objSolid(BRAIN_TEXTURE);
        collector.submitCustomGeometry(poseStack, brainType, (pose, consumer) -> {
            PoseStack work = new PoseStack();
            work.last().pose().set(pose.pose());
            work.last().normal().set(pose.normal());
            brainModel.renderToBuffer(work, consumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        });

        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.5, 0.01, 0.5);
        poseStack.rotate(Axis.XP.rotationDegrees(180.0F));
        RenderType brineType = TCRenderTypes.translucentPortal(BRINE_TEXTURE);
        collector.submitCustomGeometry(poseStack, brineType, (pose, consumer) -> {
            PoseStack work = new PoseStack();
            work.last().pose().set(pose.pose());
            work.last().normal().set(pose.normal());
            fittings.renderBrine(work, consumer, light, OverlayTexture.NO_OVERLAY);
        });
        poseStack.popPose();
    }
}