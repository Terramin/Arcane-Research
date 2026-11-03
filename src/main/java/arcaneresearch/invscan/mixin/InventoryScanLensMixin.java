package arcaneresearch.invscan.mixin;

import arcaneresearch.common.items.tools.ItemThaumometer;
import arcaneresearch.invscan.InvScanClient;
import arcaneresearch.invscan.client.ScanLensMotion;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class InventoryScanLensMixin {

    @Unique private ScanLensMotion invscan$lensMotion;
    @Unique private boolean invscan$inFloating;

    @Inject(
            method = "renderFloatingItem(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At("HEAD")
    )
    private void invscan$beginFloating(GuiGraphics g, ItemStack stack, int x, int y, String text, CallbackInfo ci) {
        this.invscan$inFloating = true;
    }

    @Inject(
            method = "renderFloatingItem(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At("RETURN")
    )
    private void invscan$endFloating(GuiGraphics g, ItemStack stack, int x, int y, String text, CallbackInfo ci) {
        this.invscan$inFloating = false;
    }

    @Redirect(
            method = "renderFloatingItem(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;II)V")
    )
    private void invscan$animateLens(GuiGraphics g, ItemStack stack, int x, int y) {
        if (!this.invscan$inFloating) {
            g.renderItem(stack, x, y);
            return;
        }
        if (!(stack.getItem() instanceof ItemThaumometer)) {
            this.invscan$lensMotion = null;
            g.renderItem(stack, x, y);
            return;
        }

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        Minecraft mc = Minecraft.getInstance();

        double scaleX = mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getScreenWidth();
        double scaleY = mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getScreenHeight();
        // Абсолютные GUI-координаты курсора
        double mouseX = mc.mouseHandler.xpos() * scaleX;
        double mouseY = mc.mouseHandler.ypos() * scaleY;

        Slot slot = InvScanClient.scannableSlot(screen, mouseX, mouseY);
        int ticks = InvScanClient.activeTicks(screen, slot);

        if (ticks <= 0 && this.invscan$lensMotion == null) {
            g.renderItem(stack, x, y);
            return;
        }
        if (this.invscan$lensMotion == null) {
            this.invscan$lensMotion = new ScanLensMotion();
        }

        double slotX = slot == null ? x + 8.0D : slot.x + 8.0D;
        double slotY = slot == null ? y + 8.0D : slot.y + 8.0D;
        double localMouseX = mouseX - screen.getGuiLeft();
        double localMouseY = mouseY - screen.getGuiTop();

        ScanLensMotion.Pose pose = this.invscan$lensMotion.update(
                slot, ticks, System.nanoTime(),
                x + 8.0D, y + 8.0D,
                slotX, slotY,
                localMouseX, localMouseY);

        PoseStack poseStack = g.pose();
        poseStack.pushPose();
        try {
            poseStack.translate(pose.centerX(), pose.centerY(), 0.0F);
            poseStack.mulPose(Axis.ZP.rotation(pose.angle()));
            poseStack.translate(-x - 8.0F, -y - 8.0F, 0.0F);
            g.renderItem(stack, x, y);
        } finally {
            poseStack.popPose();
        }

        if (this.invscan$lensMotion.isResting()) {
            this.invscan$lensMotion = null;
        }
    }
}