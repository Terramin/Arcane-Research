package arcaneresearch.client.gui;

import net.minecraft.client.Minecraft;
import arcaneresearch.client.gui.screens.ResearchBrowserScreen;

/** Client-only entry point called reflectively from common code (keeps Screen off the server classpath). */
public final class ThaumonomiconGuiOpener {

    private ThaumonomiconGuiOpener() {
    }

    public static void open() {
        Minecraft.getInstance().setScreenAndShow(new ResearchBrowserScreen());
    }
}
