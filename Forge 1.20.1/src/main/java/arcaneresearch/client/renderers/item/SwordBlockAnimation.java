package arcaneresearch.client.renderers.item;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import arcaneresearch.common.items.tools.ItemElementalSword;
@OnlyIn(Dist.CLIENT)
public final class SwordBlockAnimation {
    public static final IClientItemExtensions HAND_EXTENSION = new IClientItemExtensions() {
        @Override
        public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm,
                                               ItemStack stack, float partialTick, float equipProgress, float swingProgress) {
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemElementalSword)) {
                return false;
            }
            InteractionHand hand = arm == player.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            ItemStack useStack = player.getUseItem();
            boolean using = player.isUsingItem() && !useStack.isEmpty()
                    && useStack.getItem() instanceof ItemElementalSword
                    && player.getUsedItemHand() == hand && player.getUseItemRemainingTicks() > 0;
            if (!using) {
                return false;
            }
            int i = arm == HumanoidArm.RIGHT ? 1 : -1;
            poseStack.translate((float) i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
            poseStack.translate((float) i * -0.5F, 0.2F, 0.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees((float) i * 30.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(-80.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees((float) i * 60.0F));
            return true;
        }
    };
    private SwordBlockAnimation() {
    }
}
