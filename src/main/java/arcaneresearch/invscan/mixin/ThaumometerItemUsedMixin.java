package arcaneresearch.invscan.mixin;

import arcaneresearch.common.items.tools.ItemThaumometer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ThaumometerItemUsedMixin {

    @Inject(method = "itemUsed(Lnet/minecraft/world/InteractionHand;)V", at = @At("HEAD"), cancellable = true)
    private void arcaneresearch$cancelThaumometerItemUsed(InteractionHand hand, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        ItemStack stack = mc.player.getItemInHand(hand);
        if (stack.getItem() instanceof ItemThaumometer) {
            ci.cancel();
        }
    }
}