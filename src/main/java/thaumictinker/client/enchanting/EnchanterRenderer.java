package thaumictinker.client.enchanting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import thaumictinker.enchanting.EnchanterBlockEntity;

 




 
public class EnchanterRenderer implements BlockEntityRenderer<EnchanterBlockEntity> {

    private final Minecraft minecraft = Minecraft.getInstance();

    public EnchanterRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(EnchanterBlockEntity entity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int light, int overlay) {
        if (entity.getLevel() == null) {
            return;
        }
        double ticks = entity.getLevel().getGameTime() + partialTicks;
        ItemStack tool = entity.getTool();
        if (!tool.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.4F, 0.765F, 0.6F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(30.0F));
            poseStack.scale(0.7F, 0.7F, 0.7F);
            minecraft.getItemRenderer().renderStatic(tool, ItemDisplayContext.FIXED, light,
                    overlay, poseStack, bufferSource, entity.getLevel(), 0);
            poseStack.popPose();
        }
        ItemStack wand = entity.getWand();
        if (!wand.isEmpty()) {
            double millis = ticks * 50.0D;
            poseStack.pushPose();
            poseStack.translate(0.7F, 0.9F + (float) Math.cos(millis / 1000.0D) / 40.0F, 0.35F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-70.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees((float) Math.cos(millis / 500.0D) * 5.0F));
            poseStack.scale(0.5F, 0.5F, 0.5F);
            minecraft.getItemRenderer().renderStatic(wand, ItemDisplayContext.FIXED, light,
                    overlay, poseStack, bufferSource, entity.getLevel(), 0);
            poseStack.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(EnchanterBlockEntity entity) {
        return true;
    }
}
