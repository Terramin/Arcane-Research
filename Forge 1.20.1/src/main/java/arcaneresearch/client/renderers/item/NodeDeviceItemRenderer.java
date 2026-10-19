package arcaneresearch.client.renderers.item;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.client.renderers.tile.StabilizerRenderer;
import arcaneresearch.common.items.aura.ItemNodeDevice;
import arcaneresearch.client.renderers.tile.NodeConverterRenderer;
@OnlyIn(Dist.CLIENT)
public class NodeDeviceItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final ItemNodeDevice.Kind kind;
    public NodeDeviceItemRenderer(ItemNodeDevice.Kind kind) {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
        this.kind = kind;
    }
    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext,
                             PoseStack poseStack, MultiBufferSource buffer,
                             int packedLight, int packedOverlay) {
        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GROUND) {
            poseStack.translate(0.0D, 0.15D, 0.0D);
        }
        poseStack.translate(0.5D, 0.5D, 0.5D);
        applyDisplay(displayContext, poseStack);
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        switch (kind) {
            case ADVANCED -> {
                poseStack.translate(0.0D, 0.125D, 0.0D);
                StabilizerRenderer.renderDevice(poseStack, buffer,
                        20, 2, packedLight, packedOverlay, false);
            }
            case TRANSDUCER, CONVERTER -> {
                poseStack.translate(0.0D, -0.1D, 0.0D);
                NodeConverterRenderer.renderDevice(poseStack, buffer,
                        20, 0, packedLight, packedOverlay);
            }
            default -> {
                poseStack.translate(0.0D, 0.125D, 0.0D);
                StabilizerRenderer.renderDevice(poseStack, buffer,
                        20, 1, packedLight, packedOverlay, false);
            }
        }
        poseStack.popPose();
    }
    private static void applyDisplay(ItemDisplayContext context, PoseStack poseStack) {
        switch (context) {
            case GUI -> {
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(30.0F));
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(225.0F));
                poseStack.scale(0.625F, 0.625F, 0.625F);
            }
            case FIXED -> poseStack.scale(0.5F, 0.5F, 0.5F);
            case GROUND -> poseStack.scale(0.25F, 0.25F, 0.25F);
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(75.0F));
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(45.0F));
                poseStack.scale(0.375F, 0.375F, 0.375F);
            }
            case FIRST_PERSON_RIGHT_HAND -> {
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(45.0F));
                poseStack.scale(0.4F, 0.4F, 0.4F);
            }
            case FIRST_PERSON_LEFT_HAND -> {
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(225.0F));
                poseStack.scale(0.4F, 0.4F, 0.4F);
            }
            case HEAD -> poseStack.scale(0.625F, 0.625F, 0.625F);
            default -> poseStack.scale(0.625F, 0.625F, 0.625F);
        }
    }
}
