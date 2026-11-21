package arcaneresearch.client.renderers.entity;

import arcaneresearch.client.models.entity.PortalModel;
import arcaneresearch.common.entities.EntityPortal;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
public class PortalRenderer extends MobRenderer<EntityPortal, PortalRenderer.State, PortalModel> {

    public static class State extends LivingEntityRenderState {
        public float fadeAlpha = 1.0F;
        public double bbHeight;
        public float openFraction = 1.0F;
        public boolean closing;
        public int displayedLifetime;
    }

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("arcaneresearch", "textures/entity/portal.png");
    private static final Identifier GLOW_TEXTURE =
            Identifier.fromNamespaceAndPath("arcaneresearch", "textures/entity/glowportal.png");

    private static final RenderType BODY_TYPE = RenderTypes.entityCutout(TEXTURE);
    private static final RenderType BODY_FADE_TYPE = RenderTypes.entityTranslucent(TEXTURE);
    private static final RenderType GLOW_TYPE = RenderTypes.eyes(GLOW_TEXTURE);

    private static final int SECONDS_PER_TICK_GROUP = 20;

    public PortalRenderer(EntityRendererProvider.Context context) {
        super(context, new PortalModel(context.bakeLayer(PortalModel.LAYER_LOCATION)), 0f);

        this.addLayer(new RenderLayer<State, PortalModel>(this) {
            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                               State state, float yRot, float xRot) {
                float fade = state.fadeAlpha;
                if (fade <= 0.001F) {
                    return;
                }
                PortalModel model = getParentModel();
                model.setFadeAlpha(fade);
                collector.submitCustomGeometry(poseStack, GLOW_TYPE, (pose, consumer) -> {
                    PoseStack work = new PoseStack();
                    work.last().pose().set(pose.pose());
                    work.last().normal().set(pose.normal());
                    model.renderToBuffer(work, consumer, 0xF000F0, OverlayTexture.NO_OVERLAY,
                            1.0F, 1.0F, 1.0F, 1.0F);
                });
            }
        });
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityPortal entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.fadeAlpha = entity.getFadeAlpha(partialTicks);
        state.bbHeight = entity.getBbHeight();
        state.openFraction = entity.getOpenFraction(partialTicks);
        state.closing = entity.isClosing();
        state.displayedLifetime = entity.getDisplayedLifetime();
        state.bodyRot = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return TEXTURE;
    }

    @Override
    protected RenderType getRenderType(State state, boolean bodyVisible, boolean translucent, boolean glowing) {
        if (state.fadeAlpha < 0.999F) {
            return BODY_FADE_TYPE;
        }
        return BODY_TYPE;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        if (state.fadeAlpha <= 0.001F) {
            return;
        }
        super.submit(state, poseStack, collector, camera);
        renderLifetimeTimer(state, poseStack, collector, camera);
    }

    private void renderLifetimeTimer(State state, PoseStack poseStack, SubmitNodeCollector collector,
                                     CameraRenderState camera) {
        if (state.closing || state.openFraction < 0.999F) {
            return;
        }
        int lifetime = state.displayedLifetime;
        if (lifetime <= 0) {
            return;
        }

        String text = Integer.toString((lifetime + SECONDS_PER_TICK_GROUP - 1) / SECONDS_PER_TICK_GROUP);
        float scale = 0.025F;

        poseStack.pushPose();
        poseStack.translate(0.0D, state.bbHeight + 0.3D, 0.0D);
        poseStack.rotate(camera.orientation);
        poseStack.scale(-scale, -scale, scale);
        FormattedCharSequence seq = Component.literal(text).getVisualOrderText();
        int halfWidth = -getFont().width(seq) / 2;
        collector.submitText(poseStack, halfWidth, 0.0F, seq, false, Font.DisplayMode.NORMAL,
                0x40000000, 0xFFFFFFFF, 0, 0);
        poseStack.popPose();
    }
}
