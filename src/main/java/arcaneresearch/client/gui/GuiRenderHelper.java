package arcaneresearch.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;



@OnlyIn(Dist.CLIENT)
public final class GuiRenderHelper {

    private GuiRenderHelper() {
    }

    public static void renderItem(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y) {
        if (stack.isEmpty()) {
            return;
        }
        graphics.item(stack, x, y);
    }

    public static void renderItemWithDecorations(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y) {
        renderItemWithDecorations(graphics, Minecraft.getInstance().font, stack, x, y);
    }

    public static void renderItemWithDecorations(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int x, int y) {
        if (stack.isEmpty()) {
            return;
        }
        graphics.item(stack, x, y);
        graphics.itemDecorations(font, stack, x, y);
    }
}