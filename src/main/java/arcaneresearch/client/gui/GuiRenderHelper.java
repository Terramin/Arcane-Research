package arcaneresearch.client.gui;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
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
    /**
     * Draws an item exactly like {@link GuiGraphics#renderItem} does (same
     * centering, flip, scale and lighting), but on the depth plane given by
     * {@code z} instead of the hardcoded {@code 150} used by vanilla. Needed
     * when items must stay attached to a transformed sheet (e.g. research
     * table cards) instead of always floating above every other GUI element.
     */
    public static void renderItemOnLayer(GuiGraphics graphics, ItemStack stack, int x, int y, float z) {
        if (stack.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        BlendState saved = saveBlendState();
        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        PoseStack pose = graphics.pose();
        BakedModel model = mc.getItemRenderer().getModel(stack, mc.level, mc.player, 0);
        boolean flat = !model.usesBlockLight();
        pose.pushPose();
        try {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            pose.translate((float) (x + 8), (float) (y + 8), z);
            pose.mulPoseMatrix(new Matrix4f().scaling(1.0F, -1.0F, 1.0F));
            pose.scale(16.0F, 16.0F, 16.0F);
            if (flat) {
                Lighting.setupForFlatItems();
            }
            mc.getItemRenderer().render(stack, ItemDisplayContext.GUI, false, pose,
                    graphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, model);
            graphics.flush();
        } finally {
            if (flat) {
                Lighting.setupFor3DItems();
            }
            pose.popPose();
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
