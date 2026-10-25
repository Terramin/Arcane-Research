package arcaneresearch.client.renderers.entity.golem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import arcaneresearch.api.golems.IGolemAPI;
@OnlyIn(Dist.CLIENT)
public final class GolemRenderHooks {
    private GolemRenderHooks() {
    }
    public static void rotateDeg(PoseStack poseStack, float deg, float ax, float ay, float az) {
        if (deg == 0.0f) {
            return;
        }
        if (ax == 1.0f && ay == 0.0f && az == 0.0f) {
            poseStack.mulPose(Axis.XP.rotationDegrees(deg));
        } else if (ax == -1.0f && ay == 0.0f && az == 0.0f) {
            poseStack.mulPose(Axis.XN.rotationDegrees(deg));
        } else if (ax == 0.0f && ay == 1.0f && az == 0.0f) {
            poseStack.mulPose(Axis.YP.rotationDegrees(deg));
        } else if (ax == 0.0f && ay == -1.0f && az == 0.0f) {
            poseStack.mulPose(Axis.YN.rotationDegrees(deg));
        } else if (ax == 0.0f && ay == 0.0f && az == 1.0f) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(deg));
        } else if (ax == 0.0f && ay == 0.0f && az == -1.0f) {
            poseStack.mulPose(Axis.ZN.rotationDegrees(deg));
        } else {
            Vector3f axis = new Vector3f(ax, ay, az);
            axis.normalize();
            poseStack.mulPose(new Quaternionf().setAngleAxis(
                    (float) Math.toRadians(deg), axis.x, axis.y, axis.z));
        }
    }
    public static VertexConsumer cutout(MultiBufferSource buffer, ResourceLocation tex) {
        return buffer.getBuffer(RenderType.entityCutoutNoCull(tex));
    }
    public static void renderHeldItem(IGolemAPI golem, PoseStack poseStack, MultiBufferSource buffer,
                                      int packedLight, int packedOverlay, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        boolean block = stack.getItem() instanceof BlockItem;
        rotateDeg(poseStack, 90.0f, -1.0f, 0.0f, 0.0f);
        poseStack.scale(0.375f, 0.375f, 0.375f);
        poseStack.translate(0.0f, 0.25f, -1.5f);
        if (!block) {
            poseStack.translate(0.0f, -0.6f, 0.0f);
        }
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.HEAD,
                packedLight, packedOverlay, poseStack, buffer, golem.getGolemWorld(), 0);
        poseStack.popPose();
    }
    public static void renderCarried(IGolemAPI golem, PoseStack poseStack, MultiBufferSource buffer,
                                     int packedLight, int packedOverlay, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        boolean block = stack.getItem() instanceof BlockItem;
        poseStack.scale(0.375f, 0.375f, 0.375f);
        poseStack.translate(0.0f, 0.33f, 0.825f);
        if (!block) {
            poseStack.translate(0.0f, 0.0f, -0.25f);
        }
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.HEAD,
                packedLight, packedOverlay, poseStack, buffer, golem.getGolemWorld(), 0);
        poseStack.popPose();
    }
}
