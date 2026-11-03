package arcaneresearch.invscan.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import arcaneresearch.common.items.tools.ItemThaumometer;
import arcaneresearch.invscan.InvScanClient;
import arcaneresearch.invscan.client.ScanLensMotion;

@Mixin(value = AbstractContainerScreen.class, remap = false)
public abstract class InventoryScanLensMixin {

    @Unique private ScanLensMotion invscan$lensMotion;
    @Unique private boolean invscan$extractingCarriedItem;

    @Inject(
            method = "extractCarriedItem",
            at = @At("HEAD")
    )
    private void invscan$beginCarriedItem(CallbackInfo ci) {
        this.invscan$extractingCarriedItem = true;
    }

    @Inject(
            method = "extractCarriedItem",
            at = @At("RETURN")
    )
    private void invscan$endCarriedItem(CallbackInfo ci) {
        this.invscan$extractingCarriedItem = false;
    }

    @Redirect(
            method = "extractFloatingItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;item(Lnet/minecraft/world/item/ItemStack;II)V"
            )
    )
    private void invscan$animateLens(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y) {
        if (!this.invscan$extractingCarriedItem) {
            graphics.item(stack, x, y);
            return;
        }
        if (!(stack.getItem() instanceof ItemThaumometer)) {
            this.invscan$lensMotion = null;
            graphics.item(stack, x, y);
            return;
        }

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        Minecraft mc = Minecraft.getInstance();

        double mouseX = mc.mouseHandler.getScaledXPos(mc.getWindow());
        double mouseY = mc.mouseHandler.getScaledYPos(mc.getWindow());

        Slot slot = InvScanClient.scannableSlot(screen, mouseX, mouseY);
        int ticks = InvScanClient.activeTicks(screen, slot);

        if (ticks <= 0 && this.invscan$lensMotion == null) {
            graphics.item(stack, x, y);
            return;
        }
        if (this.invscan$lensMotion == null) {
            this.invscan$lensMotion = new ScanLensMotion();
        }

        double slotX = slot == null ? x + 8.0D : screen.getLeftPos() + slot.x + 8.0D;
        double slotY = slot == null ? y + 8.0D : screen.getTopPos() + slot.y + 8.0D;

        ScanLensMotion.Pose pose = this.invscan$lensMotion.update(
                slot, ticks, System.nanoTime(),
                x + 8.0D, y + 8.0D,
                slotX, slotY,
                mouseX, mouseY);

        Matrix3x2fStack matrix = graphics.pose();
        matrix.pushMatrix();
        try {
            matrix.translate(pose.centerX(), pose.centerY());
            matrix.rotate(pose.angle());
            matrix.translate(-x - 8.0F, -y - 8.0F);
            graphics.item(stack, x, y);
        } finally {
            matrix.popMatrix();
        }

        if (this.invscan$lensMotion.isResting()) {
            this.invscan$lensMotion = null;
        }
    }
}
