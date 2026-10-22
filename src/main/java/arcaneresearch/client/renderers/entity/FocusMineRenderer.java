package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier; 
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.entity.GrapplerModel;
import arcaneresearch.common.entities.projectile.EntityFocusMine;



public class FocusMineRenderer extends EntityRenderer<EntityFocusMine, FocusMineRenderer.State> {

    public static class State extends EntityRenderState {
        public float pulse;
        public float yaw;
        public float pitch;
    }

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/mine.png");

    private final GrapplerModel model;

    public FocusMineRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
        this.model = new GrapplerModel(context.bakeLayer(GrapplerModel.LAYER_LOCATION));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityFocusMine entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.pulse = (entity.tickCount + partialTicks) % 8.0F / 8.0F;
        state.yaw = entity.yRotO + (entity.getYRot() - entity.yRotO) * partialTicks - 90.0F;
        state.pitch = entity.xRotO + (entity.getXRot() - entity.xRotO) * partialTicks;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        poseStack.pushPose();
        float red = 1.0F;
        float green = 1.0F - state.pulse;
        float blue = 1.0F - state.pulse;

        poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(state.yaw));
        poseStack.rotate(com.mojang.math.Axis.ZP.rotationDegrees(state.pitch));

        RenderType renderType = RenderTypes.entityCutout(TEXTURE);
        int light = 0xF000F0;
        collector.submitCustomGeometry(poseStack, renderType, (pose, consumer) -> {
            PoseStack work = new PoseStack();
            work.last().pose().set(pose.pose());
            work.last().normal().set(pose.normal());
            this.model.renderToBuffer(work, consumer, light,
                    OverlayTexture.NO_OVERLAY, red, green, blue, 1.0F);
        });
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}