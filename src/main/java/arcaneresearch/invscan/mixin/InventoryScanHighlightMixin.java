package arcaneresearch.invscan.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import arcaneresearch.invscan.InvScanClient;
import arcaneresearch.invscan.InvScanServer;

@Mixin(value = AbstractContainerScreen.class, remap = false)
public abstract class InventoryScanHighlightMixin {

    @Unique
    private long invscan$swayStartedAt;

    @Redirect(
            method = "renderSlotContents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;item(Lnet/minecraft/world/item/ItemStack;III)V"
            )
    )
    private void invscan$highlightItem(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, int seed,
                                       GuiGraphicsExtractor screenGraphics, ItemStack slotStack, Slot slot, String itemCount) {
        invscan$drawItem(graphics, stack, x, y, seed, slot, false);
    }

    @Redirect(
            method = "renderSlotContents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fakeItem(Lnet/minecraft/world/item/ItemStack;III)V"
            )
    )
    private void invscan$highlightFakeItem(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, int seed,
                                           GuiGraphicsExtractor screenGraphics, ItemStack slotStack, Slot slot, String itemCount) {
        invscan$drawItem(graphics, stack, x, y, seed, slot, true);
    }

    @Unique
    private void invscan$drawItem(GuiGraphicsExtractor g, ItemStack stack,
                                  int x, int y, int seed,
                                  Slot slot, boolean fake) {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        Player player = Minecraft.getInstance().player;

        boolean holding = !(screen instanceof CreativeModeInventoryScreen)
                && InvScanClient.canUseThaumometer(screen);

        if (!holding) {
            this.invscan$swayStartedAt = 0L;
        } else if (this.invscan$swayStartedAt == 0L) {
            this.invscan$swayStartedAt = System.nanoTime();
        }

        boolean sway = holding
                && player != null
                && InvScanClient.isScannableSlot(screen, slot)
                && !InvScanClient.hasBeenScanned(
                InvScanServer.itemScanKey(stack));

        if (sway) {
            double elapsed = (System.nanoTime() - this.invscan$swayStartedAt) / 1.0E9;
            double onset = Math.min(1.0, elapsed / 0.25);
            double amp = onset * onset * (3.0 - 2.0 * onset);
            float angle = (float) Math.toRadians(
                    3.0 * amp * Math.sin(elapsed * Math.PI * 2.0 / 1.8 + slot.index * 0.45));

            Matrix3x2fStack pose = g.pose();
            pose.pushMatrix();
            pose.translate(x + 8.0F, y + 16.0F);
            pose.rotate(angle);
            pose.translate(-x - 8.0F, -y - 16.0F);
        }

        try {
            if (fake) {
                g.fakeItem(stack, x, y, seed);
            } else {
                g.item(stack, x, y, seed);
            }
        } finally {
            if (sway) {
                g.pose().popMatrix();
            }
        }
    }
}
