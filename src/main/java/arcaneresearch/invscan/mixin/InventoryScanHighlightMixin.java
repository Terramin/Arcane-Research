package arcaneresearch.invscan.mixin;

import arcaneresearch.helper.RegsHelper;
import arcaneresearch.invscan.InvScanClient;
import arcaneresearch.invscan.InvScanServer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Порт 1.21.9 InventoryScanHighlightMixin на 1.20.1.
 *
 * В 1.20.1 AbstractContainerScreen#renderSlot(GuiGraphics, Slot) делает ровно один
 * вызовы GuiGraphics#renderItem(ItemStack,III) (флаг fakeItem — вызовы
 * GuiGraphics#renderFakeItem — внутри renderSlot нет, там есть только
 * renderFloatingItem для перетаскиваемого стека).
 * Поэтому вместо двух @Inject (как в референсе на renderSlotContents) здесь один
 * @Redirect, который поворачивает матрицу и вызывает оригинальный renderItem.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class InventoryScanHighlightMixin {

    @Unique
    private long invscan$swayStartedAt;

    @Redirect(
            method = "renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;III)V"
            )
    )
    private void invscan$highlightItem(GuiGraphics graphics, ItemStack stack, int x, int y, int seed,
                                       GuiGraphics screenGraphics, Slot slot) {
        invscan$drawItem(graphics, stack, x, y, seed, slot);
    }

    @Unique
    private void invscan$drawItem(GuiGraphics g, ItemStack stack,
                                  int x, int y, int seed,
                                  Slot slot) {
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

            PoseStack pose = g.pose();
            pose.pushPose();
            pose.translate(x + 8.0F, y + 16.0F, 0.0F);
            pose.mulPose(Axis.ZP.rotation(angle));
            pose.translate(-x - 8.0F, -y - 16.0F, 0.0F);
        }

        try {
            g.renderItem(stack, x, y, seed);
        } finally {
            if (sway) {
                g.pose().popPose();
            }
        }
    }
}
