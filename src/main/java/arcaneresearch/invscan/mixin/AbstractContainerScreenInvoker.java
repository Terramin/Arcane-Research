package arcaneresearch.invscan.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = AbstractContainerScreen.class, remap = false)
public interface AbstractContainerScreenInvoker {

    @Invoker("getHoveredSlot")
    Slot invscan$findSlot(double mouseX, double mouseY);
}
