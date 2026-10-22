package arcaneresearch.invscan;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

 








 
public final class ScreenAccess {

    private ScreenAccess() {
    }

    public static Slot hoveredSlot(AbstractContainerScreen<?> screen) {
        return screen.getHoveredSlot();
    }

    public static int imageWidth(AbstractContainerScreen<?> screen) {
        return screen.getImageWidth();
    }

    public static int imageHeight(AbstractContainerScreen<?> screen) {
        return screen.getImageHeight();
    }

    public static int leftPos(AbstractContainerScreen<?> screen) {
        return screen.getLeftPos();
    }

    public static int topPos(AbstractContainerScreen<?> screen) {
        return screen.getTopPos();
    }
}
