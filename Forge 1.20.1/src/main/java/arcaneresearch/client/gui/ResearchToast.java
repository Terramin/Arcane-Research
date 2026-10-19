package arcaneresearch.client.gui;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.research.ResearchEntry;
import arcaneresearch.client.gui.screens.ResearchBrowserScreen;
import arcaneresearch.common.lib.utils.InventoryUtils;
import arcaneresearch.init.ModSounds;
@OnlyIn(Dist.CLIENT)
public class ResearchToast implements Toast {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/gui/hud.png");
    private static final long DISPLAY_TIME = 5000L;
    private final ResearchEntry entry;
    private final Component title;
    private final Component description;
    private long firstDrawTime;
    private boolean hasPlayedSound;
    public ResearchToast(ResearchEntry research) {
        this.entry = research;
        this.title = Component.translatable("research.complete");
        this.description = research.getLocalizedName();
    }
    private ResearchToast(ResearchEntry research, Component title, Component description) {
        this.entry = research;
        this.title = title;
        this.description = description;
    }
    public static ResearchToast discovered(ResearchEntry research) {
        return new ResearchToast(research,
                Component.translatable("tc.research.toast.discovered"),
                research.getLocalizedName());
    }
    private static void drawResearchIcon(GuiGraphics graphics, ResearchEntry iconResearch, int iconX, int iconY) {
        if (iconResearch == null || iconResearch.getIcons() == null
                || iconResearch.getIcons().length == 0) {
            return;
        }
        int idx = (int) (System.currentTimeMillis() / 1000L % iconResearch.getIcons().length);
        Object icon = iconResearch.getIcons()[idx];
        if (icon instanceof ResourceLocation rl) {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            int[] size = ResearchBrowserScreen.getTextureSize(rl);
            int w = Math.max(size[0], 1);
            int h = Math.max(size[1], 1);
            if (h > w && h % w == 0) {
                int m = h / w;
                int frame = (int) (System.currentTimeMillis() / 150L % m);
                graphics.pose().pushPose();
                graphics.pose().translate(iconX, iconY, 0);
                graphics.pose().scale(16.0f / w, 16.0f / w, 1.0f);
                ResearchBrowserScreen.drawTextureRegion(graphics, rl, 0, 0, 0, frame * w, w, w, w, h);
                graphics.pose().popPose();
            } else if (w > h && w % h == 0) {
                int m = w / h;
                int frame = (int) (System.currentTimeMillis() / 150L % m);
                graphics.pose().pushPose();
                graphics.pose().translate(iconX, iconY, 0);
                graphics.pose().scale(16.0f / h, 16.0f / h, 1.0f);
                ResearchBrowserScreen.drawTextureRegion(graphics, rl, 0, 0, frame * h, 0, h, h, w, h);
                graphics.pose().popPose();
            } else {
                ResearchBrowserScreen.drawFullTexture(graphics, rl, iconX, iconY, 16, 16);
            }
        } else if (icon instanceof ItemStack stack) {
            arcaneresearch.client.gui.GuiRenderHelper.renderItem(graphics,
                    InventoryUtils.cycleItemStack(stack), iconX, iconY);
        } else if (icon instanceof String str && str.startsWith("focus:")) {
            var fp = arcaneresearch.api.casters.FocusEngine.getElement(str.replace("focus:", "").trim());
            if (fp instanceof arcaneresearch.api.casters.FocusNode node) {
                arcaneresearch.client.gui.screens.FocalManipulatorScreen.drawPart(
                        graphics, node, iconX + 8, iconY + 8, 24.0f, true);
            }
        }
    }
    @Override
    public Visibility render(GuiGraphics graphics, ToastComponent toastComponent, long timeSinceLastVisible) {
        if (firstDrawTime == 0) {
            firstDrawTime = timeSinceLastVisible;
            if (!hasPlayedSound) {
                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(ModSounds.LEARN.get(), 1.0f));
                hasPlayedSound = true;
            }
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        graphics.blit(TEXTURE, 0, 0, 0, 224, this.width(), this.height());
        drawResearchIcon(graphics, entry, 6, 8);
        var font = toastComponent.getMinecraft().font;
        graphics.drawString(font, title, 30, 7, 10631665, false);
        String s = description.getString();
        float w = font.width(s);
        if (w > 124.0f) {
            float scale = 124.0f / w;
            graphics.pose().pushPose();
            graphics.pose().translate(30.0f, 18.0f, 0.0f);
            graphics.pose().scale(scale, scale, 1.0f);
            graphics.drawString(font, s, 0, 0, 16755465, false);
            graphics.pose().popPose();
        } else {
            graphics.drawString(font, s, 30, 18, 16755465, false);
        }
        long elapsed = timeSinceLastVisible - firstDrawTime;
        return elapsed >= DISPLAY_TIME ? Visibility.HIDE : Visibility.SHOW;
    }
    @Override
    public int width() {
        return 160;
    }
    @Override
    public int height() {
        return 32;
    }
    @Override
    public Object getToken() {
        return title.getString() + "|" + description.getString();
    }
}
