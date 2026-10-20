package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.entities.monster.boss.EntityCultistPortalGreater;

// NEOFORGE_PORT (26.3): same portal-quad conversion as CultistPortalRenderer
// (billboard + submitCustomGeometry, wobble math 1:1).
public class CultistPortalGreaterRenderer extends EntityRenderer<EntityCultistPortalGreater, CultistPortalGreaterRenderer.State> {

    public static class State extends EntityRenderState {
        public int hurtTime;
        public int pulse;
        public float health;
        public float maxHealth;
        public float height;
        public float tickCount;
        public float partialTicks;
    }

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/cultist_portal.png");

    public CultistPortalGreaterRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.1F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityCultistPortalGreater entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.hurtTime = entity.hurtTime;
        state.pulse = entity.pulse;
        state.health = entity.getHealth();
        state.maxHealth = entity.getMaxHealth();
        state.height = entity.getBbHeight();
        state.tickCount = entity.tickCount;
        state.partialTicks = partialTicks;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0, state.height / 2.0F, 0.0);
        poseStack.rotate(camera.orientation);
        poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(180.0F));

        RenderType renderType = TCRenderTypes.translucentPortal(TEXTURE);
        collector.submitCustomGeometry(poseStack, renderType,
                (pose, consumer) -> renderPortalQuad(pose, consumer, state));
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    private static void renderPortalQuad(PoseStack.Pose pose, VertexConsumer consumer, State state) {
        float scaley = 1.5F;
        int activeFrames = (int) Math.min(50.0F, state.tickCount + state.partialTicks);
        if (state.hurtTime > 0) {
            double hurtWobble = Math.sin(state.hurtTime * 72 * Math.PI / 180.0);
            scaley -= (float) (hurtWobble / 4.0);
            activeFrames += (int) (6.0 * hurtWobble);
        }
        if (state.pulse > 0) {
            double pulseWobble = Math.sin(state.pulse * 36 * Math.PI / 180.0);
            scaley += (float) (pulseWobble / 4.0);
            activeFrames += (int) (12.0 * pulseWobble);
        }
        float scale = activeFrames / 50.0F * 1.3F;
        float healthPercent = 1.0F - state.health / state.maxHealth;
        float m = healthPercent / 3.0F;
        float bob = Mth.sin(state.tickCount / (5.0F - 12.0F * m)) * m + m;
        float bob2 = Mth.sin(state.tickCount / (6.0F - 15.0F * m)) * m + m;
        float alpha = 1.0F - bob;
        scaley -= bob / 4.0F;
        scale -= bob2 / 3.0F;

        long time = System.nanoTime() / 50000000L;
        int frame = 15 - (int) (time % 16);
        float minU = frame / 16.0F;
        float maxU = minU + 0.0625F;
        int light = 0xE0;

        CultistPortalRenderer.quad(consumer, pose, -scale, -scaley, 0, maxU, 0.0F, alpha, light);
        CultistPortalRenderer.quad(consumer, pose, -scale, scaley, 0, maxU, 1.0F, alpha, light);
        CultistPortalRenderer.quad(consumer, pose, scale, scaley, 0, minU, 1.0F, alpha, light);
        CultistPortalRenderer.quad(consumer, pose, scale, -scaley, 0, minU, 0.0F, alpha, light);
    }
}