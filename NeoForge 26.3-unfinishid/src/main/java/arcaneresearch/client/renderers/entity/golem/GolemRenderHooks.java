package arcaneresearch.client.renderers.entity.golem;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.neoforged.api.distmarker.Dist;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class GolemRenderHooks {

    private GolemRenderHooks() {
    }

    public static void rotateDeg(PoseStack poseStack, float deg, float ax, float ay, float az) {
        if (deg == 0.0f) {
            return;
        }
        if (ax == 1.0f && ay == 0.0f && az == 0.0f) {
            poseStack.rotate(Axis.XP.rotationDegrees(deg));
        } else if (ax == -1.0f && ay == 0.0f && az == 0.0f) {
            poseStack.rotate(Axis.XN.rotationDegrees(deg));
        } else if (ax == 0.0f && ay == 1.0f && az == 0.0f) {
            poseStack.rotate(Axis.YP.rotationDegrees(deg));
        } else if (ax == 0.0f && ay == -1.0f && az == 0.0f) {
            poseStack.rotate(Axis.YN.rotationDegrees(deg));
        } else if (ax == 0.0f && ay == 0.0f && az == 1.0f) {
            poseStack.rotate(Axis.ZP.rotationDegrees(deg));
        } else if (ax == 0.0f && ay == 0.0f && az == -1.0f) {
            poseStack.rotate(Axis.ZN.rotationDegrees(deg));
        } else {
            Vector3f axis = new Vector3f(ax, ay, az);
            axis.normalize();
            poseStack.rotate(new Quaternionf().setAngleAxis(
                    (float) Math.toRadians(deg), axis.x, axis.y, axis.z));
        }
    }

    // NEOFORGE_PORT: items are pre-resolved to ItemStackRenderState in
    // GolemRenderer.extractRenderState; same pose math, submitted directly.
    public static void renderHeldItem(ItemStackRenderState item, boolean block,
                                      PoseStack poseStack, SubmitNodeCollector collector,
                                      int packedLight, int packedOverlay) {
        if (item.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        rotateDeg(poseStack, 90.0f, -1.0f, 0.0f, 0.0f);
        poseStack.scale(0.375f, 0.375f, 0.375f);
        poseStack.translate(0.0f, 0.25f, -1.5f);
        if (!block) {
            poseStack.translate(0.0f, -0.6f, 0.0f);
        }
        item.submit(poseStack, collector, packedLight, packedOverlay, 0);
        poseStack.popPose();
    }

    public static void renderCarried(ItemStackRenderState item, boolean block,
                                     PoseStack poseStack, SubmitNodeCollector collector,
                                     int packedLight, int packedOverlay) {
        if (item.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.scale(0.375f, 0.375f, 0.375f);
        poseStack.translate(0.0f, 0.33f, 0.825f);
        if (!block) {
            poseStack.translate(0.0f, 0.0f, -0.25f);
        }
        item.submit(poseStack, collector, packedLight, packedOverlay, 0);
        poseStack.popPose();
    }
}