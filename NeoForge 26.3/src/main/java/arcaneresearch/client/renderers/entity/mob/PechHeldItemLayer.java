package arcaneresearch.client.renderers.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BowItem;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.client.models.entity.PechModel;
import arcaneresearch.client.renderers.entity.PechRenderer;

// NEOFORGE_PORT (26.3): RenderLayer<S, M>, submit() instead of render(); held
// items come from ArmedEntityRenderState (right/leftHandItemState), submitted
// via ItemStackRenderState.submit (Thaumaturge PechRenderer oracle pattern).
// Pose math (bow/shift offsets) 1:1 from Forge.
public class PechHeldItemLayer extends RenderLayer<PechRenderer.State, PechModel> {

    public PechHeldItemLayer(RenderLayerParent<PechRenderer.State, PechModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                       PechRenderer.State state, float limbSwing, float partialTicks) {
        // NEOFORGE_PORT (26.3): Forge drew the item held by each arm on that arm
        // (left-handed pechs keep the main-hand item on the left); the states are per arm.
        ItemStackRenderState mainHand = state.rightHandItemState;
        ItemStackRenderState offHand = state.leftHandItemState;
        if (mainHand.isEmpty() && offHand.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        // NEOFORGE_PORT: ModelPart has no young field in 26.x -> state.isBaby.
        if (state.isBaby) {
            poseStack.translate(0.0F, 0.625F, 0.0F);
            poseStack.rotate(com.mojang.math.Axis.XP.rotationDegrees(20.0F));
            poseStack.scale(0.5F, 0.5F, 0.5F);
        }
        submitHeldItem(collector, mainHand, HumanoidArm.RIGHT, state, poseStack, packedLight);
        submitHeldItem(collector, offHand, HumanoidArm.LEFT, state, poseStack, packedLight);
        poseStack.popPose();
    }

    private void submitHeldItem(SubmitNodeCollector collector, ItemStackRenderState item,
                                HumanoidArm side, PechRenderer.State state,
                                PoseStack poseStack, int packedLight) {
        if (item.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        getParentModel().translateToHand(state, side, poseStack);
        poseStack.translate(0.0F, -0.1F, 0.0625F);
        // NEOFORGE_PORT (26.3): bow offset applies to whichever arm holds the bow (Forge parity).
        if (state.getUseItemStackForArm(side).getItem() instanceof BowItem) {
            poseStack.translate(-0.075F, -0.1F, 0.0F);
        }
        if (state.crouching) {
            poseStack.translate(0.0F, 0.2F, 0.0F);
        }
        poseStack.rotate(com.mojang.math.Axis.XP.rotationDegrees(-90.0F));
        poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(180.0F));
        boolean left = side == HumanoidArm.LEFT;
        poseStack.translate(left ? -0.0625F : 0.0625F, 0.125F, -0.625F);
        item.submit(poseStack, collector, packedLight, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}