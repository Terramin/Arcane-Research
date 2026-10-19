package arcaneresearch.client.gui;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
@OnlyIn(Dist.CLIENT)
public final class GuiRenderHelper {
    private GuiRenderHelper() {
    }
    private static BlendState saveBlendState() {
        return new BlendState(
                GL11.glIsEnabled(GL11.GL_BLEND),
                GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB),
                GL11.glGetInteger(GL14.GL_BLEND_DST_RGB),
                GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA),
                GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA));
    }
    private static void restoreBlendState(BlendState state) {
        RenderSystem.blendFuncSeparate(state.srcRgb(), state.dstRgb(), state.srcAlpha(), state.dstAlpha());
        if (state.enabled()) {
            RenderSystem.enableBlend();
        } else {
            RenderSystem.disableBlend();
        }
    }
    public static void renderItem(GuiGraphics graphics, ItemStack stack, int x, int y) {
        if (stack.isEmpty()) {
            return;
        }
        BlendState saved = saveBlendState();
        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        try {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            graphics.renderItem(stack, x, y);
        } finally {
            restoreBlendState(saved);
            if (!depth) {
                RenderSystem.disableDepthTest();
            }
        }
    }
    public static void renderItemWithDecorations(GuiGraphics graphics, ItemStack stack, int x, int y) {
        renderItemWithDecorations(graphics, Minecraft.getInstance().font, stack, x, y);
    }
    public static void renderItemWithDecorations(GuiGraphics graphics, Font font, ItemStack stack, int x, int y) {
        if (stack.isEmpty()) {
            return;
        }
        BlendState saved = saveBlendState();
        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        try {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            graphics.renderItem(stack, x, y);
            graphics.renderItemDecorations(font, stack, x, y);
        } finally {
            restoreBlendState(saved);
            if (!depth) {
                RenderSystem.disableDepthTest();
            }
        }
    }
    private record BlendState(boolean enabled, int srcRgb, int dstRgb, int srcAlpha, int dstAlpha) {
    }
}
