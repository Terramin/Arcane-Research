package thaumictinker.client.attractor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

 



 
public class AttractorItemRenderer extends BlockEntityWithoutLevelRenderer {

    public static final AttractorItemRenderer INSTANCE =
            new AttractorItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                    Minecraft.getInstance().getEntityModels());

    public AttractorItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet models) {
        super(dispatcher, models);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                              MultiBufferSource buffers, int light, int overlay) {
        boolean corporeal = stack.getItem() == thaumictinker.attractor.AttractorFeature.CORPOREAL_ATTRACTOR.asItem();
        long ticks = Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getGameTime();
        long millis = System.currentTimeMillis();
        if (context == ItemDisplayContext.FIXED) {
            
            AttractorRendering.submit(poseStack, buffers, corporeal, true, 15, light, ticks, millis);
            return;
        }
        poseStack.pushPose();
        if (context == ItemDisplayContext.GUI) {
            poseStack.translate(0.5F, 0.55F, 0.5F);
            poseStack.scale(0.42F, 0.42F, 0.42F);
            poseStack.mulPose(Axis.YP.rotationDegrees(200.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(25.0F));
        } else {
            poseStack.translate(0.2F, 0.55F, 0.2F);
            poseStack.scale(0.42F, 0.42F, 0.42F);
        }
        AttractorRendering.submit(poseStack, buffers, corporeal, true, 15, light, ticks, millis);
        poseStack.popPose();
    }
}
