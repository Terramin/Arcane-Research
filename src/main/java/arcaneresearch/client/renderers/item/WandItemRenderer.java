package arcaneresearch.client.renderers.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public final class WandItemRenderer extends BlockEntityWithoutLevelRenderer {
    public WandItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static void registerClientItemExtensions(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final WandItemRenderer renderer = new WandItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm,
                                                   ItemStack stack, float partialTick, float equipProgress,
                                                   float swingProgress) {
                return WandUseAnimation.HAND_EXTENSION.applyForgeHandTransform(
                        poseStack, player, arm, stack, partialTick, equipProgress, swingProgress);
            }
        });
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        WandItemModelRenderer.WandArg arg = WandItemModelRenderer.extract(stack);
        if (arg == null) {
            return;
        }
        poseStack.pushPose();
        WandItemModelRenderer.applySubmitTransform(poseStack);
        if (arg.rod().isStaff() && displayContext == ItemDisplayContext.GUI) {
            float k = 0.654523F;
            poseStack.scale(k, k, k);
            poseStack.translate(-0.749999F, -0.734566F, 0.651060F);
        }
        WandItemModelRenderer.draw(arg, poseStack, buffer, packedLight, true);
        poseStack.popPose();
    }
}
