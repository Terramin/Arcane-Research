package arcaneresearch.invscan;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

/**
 * Access to AbstractContainerScreen internals keyed by hover/layout state.
 *
 * NEOFORGE_PORT: Forge ObfuscationReflectionHelper + SRG-поля не нужны —
 * NeoForge 26.3 добавляет публичные геттеры
 * getHoveredSlot/getLeftPos/getTopPos/getImageWidth/getImageHeight.
 * If the screen is still animating, leftPos/topPos are the real values
 * (vanilla always sets them in init()). API-shape kept 1:1 with Forge so
 * the client logic does not change.
 */
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
