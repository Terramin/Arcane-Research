package arcaneresearch.client.renderers.entity.mob;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.client.models.entity.PechModel;
import arcaneresearch.common.entities.monster.EntityPech;
@OnlyIn(Dist.CLIENT)
public class PechHeldItemLayer extends RenderLayer<EntityPech, PechModel> {
    private final ItemInHandRenderer itemInHandRenderer;
    public PechHeldItemLayer(RenderLayerParent<EntityPech, PechModel> renderer,
                             ItemInHandRenderer itemInHandRenderer) {
        super(renderer);
        this.itemInHandRenderer = itemInHandRenderer;
    }
    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       EntityPech entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        boolean flag = entity.getMainArm() == HumanoidArm.RIGHT;
        ItemStack itemstack = flag ? entity.getOffhandItem() : entity.getMainHandItem();
        ItemStack itemstack2 = flag ? entity.getMainHandItem() : entity.getOffhandItem();
        if (!itemstack.isEmpty() || !itemstack2.isEmpty()) {
            poseStack.pushPose();
            if (getParentModel().young) {
                poseStack.translate(0.0F, 0.625F, 0.0F);
                poseStack.mulPose(Axis.XP.rotationDegrees(20.0F));
                poseStack.scale(0.5F, 0.5F, 0.5F);
            }
            renderHeldItem(entity, itemstack2, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                    HumanoidArm.RIGHT, poseStack, buffer, packedLight);
            renderHeldItem(entity, itemstack, ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                    HumanoidArm.LEFT, poseStack, buffer, packedLight);
            poseStack.popPose();
        }
    }
    private void renderHeldItem(LivingEntity entity, ItemStack stack, ItemDisplayContext transform,
                                HumanoidArm side, PoseStack poseStack,
                                MultiBufferSource buffer, int packedLight) {
        if (stack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        getParentModel().translateToHand(side, poseStack);
        poseStack.translate(0.0F, -0.1F, 0.0625F);
        if (stack.getItem() instanceof BowItem) {
            poseStack.translate(-0.075F, -0.1F, 0.0F);
        }
        if (entity.isShiftKeyDown()) {
            poseStack.translate(0.0F, 0.2F, 0.0F);
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        boolean left = side == HumanoidArm.LEFT;
        poseStack.translate(left ? -0.0625F : 0.0625F, 0.125F, -0.625F);
        itemInHandRenderer.renderItem(entity, stack, transform, left, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
    @SuppressWarnings("unused")
    private boolean shouldCombineTextures() {
        return false;
    }
}
