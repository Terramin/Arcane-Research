package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.entities.construct.EntityTurretCrossbowAdvanced;

// NEOFORGE_PORT (26.3): direct EntityRenderer (OBJ groups, no Mob model).
// Load/swing/hurt-jitter/head-pose inputs via State; groups emitted with
// TCRenderTypes.objSolid (was RenderType via buffer). Pose math 1:1.
// shouldShowName hook now takes (entity, distance).
public class TurretCrossbowAdvancedRenderer extends EntityRenderer<EntityTurretCrossbowAdvanced, TurretCrossbowAdvancedRenderer.State> {

    public static class State extends LivingEntityRenderState {
        public float loadProgress;
        public float swing;
        public float hurtJiggleX;
        public float hurtJiggleY;
        public float hurtJiggleZ;
        public boolean hurt;
        public float yawHead;
        public float pitch;
        public boolean minecart;
    }

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/crossbow_advanced.png");
    private static final Identifier MODEL =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "models/obj/crossbow_advanced.obj");

    public TurretCrossbowAdvancedRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityTurretCrossbowAdvanced turret, State state, float partialTicks) {
        super.extractRenderState(turret, state, partialTicks);
        state.loadProgress = turret.getLoadProgress(partialTicks);
        state.swing = turret.getShootSwingPhase(partialTicks);
        state.hurt = turret.hurtTime > 0;
        if (state.hurt) {
            float jiggle = turret.hurtTime / 500.0F;
            state.hurtJiggleX = (float) turret.getRandom().nextGaussian() * jiggle;
            state.hurtJiggleY = (float) turret.getRandom().nextGaussian() * jiggle;
            state.hurtJiggleZ = (float) turret.getRandom().nextGaussian() * jiggle;
        }
        state.yawHead = Mth.lerp(partialTicks, turret.yHeadRotO, turret.yHeadRot);
        state.pitch = Mth.lerp(partialTicks, turret.xRotO, turret.getXRot());
        state.minecart = turret.isPassenger() && turret.getVehicle() instanceof AbstractMinecart;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        TurretObjModel model = TurretObjModel.load(MODEL);
        if (model == null) {
            return;
        }
        float loadProgress = state.loadProgress;
        float swing = state.swing;
        float rr = 1.0F, gg = 1.0F, bb = 1.0F;
        if (state.hurt) {
            gg = 0.5F;
            bb = 0.5F;
        }
        final float r = rr, g = gg, b = bb;
        RenderType type = TCRenderTypes.objSolid(TEXTURE);

        poseStack.pushPose();
        poseStack.translate(0.0, 0.75, 0.0);

        poseStack.pushPose();
        if (state.minecart) {
            poseStack.scale(0.66F, 0.75F, 0.66F);
        }
        emitGroup(poseStack, collector, type, state, (work, consumer) ->
                model.renderGroup(work, consumer, "legs", state.lightCoords, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F));
        poseStack.popPose();

        if (state.hurt) {
            poseStack.translate(state.hurtJiggleX, state.hurtJiggleY, state.hurtJiggleZ);
        }

        poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(-state.yawHead));
        poseStack.rotate(com.mojang.math.Axis.XP.rotationDegrees(state.pitch));

        emitGroup(poseStack, collector, type, state, (work, consumer) ->
                model.renderGroup(work, consumer, "mech", state.lightCoords, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F));
        emitGroup(poseStack, collector, type, state, (work, consumer) ->
                model.renderGroup(work, consumer, "box", state.lightCoords, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F));
        emitGroup(poseStack, collector, type, state, (work, consumer) ->
                model.renderGroup(work, consumer, "shield", state.lightCoords, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F));
        emitGroup(poseStack, collector, type, state, (work, consumer) ->
                model.renderGroup(work, consumer, "brain", state.lightCoords, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F));

        poseStack.pushPose();
        poseStack.translate(0.0, 0.0,
                Mth.sin(Mth.sqrt(loadProgress) * (float) Math.PI * 2.0F) / 12.0F);
        emitGroup(poseStack, collector, type, state, (work, consumer) ->
                model.renderGroup(work, consumer, "loader", state.lightCoords, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F));
        poseStack.popPose();

        float sp = 0.0F;
        if (swing > 0.0F) {
            sp = Mth.sin(Mth.sqrt(swing) * (float) Math.PI * 2.0F) * 20.0F;
        }
        poseStack.translate(0.0, 0.0, 0.375);
        poseStack.pushPose();
        poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(sp));
        emitGroup(poseStack, collector, type, state, (work, consumer) ->
                model.renderGroup(work, consumer, "bow1", state.lightCoords, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F));
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(-sp));
        emitGroup(poseStack, collector, type, state, (work, consumer) ->
                model.renderGroup(work, consumer, "bow2", state.lightCoords, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F));
        poseStack.popPose();

        poseStack.popPose();
    }

    private static void emitGroup(PoseStack poseStack, SubmitNodeCollector collector, RenderType type,
                                  State state, GroupDraw draw) {
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

    @Override
    protected boolean shouldShowName(EntityTurretCrossbowAdvanced entity, double distance) {
        return false;
    }
}