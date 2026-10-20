package arcaneresearch.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor; // NEOFORGE_PORT: was GuiGraphics
import net.minecraft.client.renderer.RenderPipelines; // NEOFORGE_PORT: blit needs a pipeline
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.Toast.Visibility;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was ResourceLocation
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.research.ResearchEntry;
import arcaneresearch.client.gui.screens.ResearchBrowserScreen;
import arcaneresearch.common.lib.utils.InventoryUtils;
import arcaneresearch.init.ModSounds;

@OnlyIn(Dist.CLIENT)
public class ResearchToast implements Toast {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/hud.png");

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

    // NEOFORGE_PORT: entry points for PacketSyncKnowledge (common class); keeping the
    // Toast/ToastManager types here stops the server from linking client classes.
    public static void show(ResearchEntry research) {
        net.minecraft.client.Minecraft.getInstance().gui.toastManager().addToast(new ResearchToast(research));
    }

    public static void showDiscovered(ResearchEntry research) {
        net.minecraft.client.Minecraft.getInstance().gui.toastManager().addToast(discovered(research));
    }

    public static ResearchToast discovered(ResearchEntry research) {
        return new ResearchToast(research,
                Component.translatable("tc.research.toast.discovered"),
                research.getLocalizedName());
    }

    private static void drawResearchIcon(GuiGraphicsExtractor graphics, ResearchEntry iconResearch, int iconX, int iconY) {
        if (iconResearch == null || iconResearch.getIcons() == null
                || iconResearch.getIcons().length == 0) {
            return;
        }
        int idx = (int) (System.currentTimeMillis() / 1000L % iconResearch.getIcons().length);
        Object icon = iconResearch.getIcons()[idx];
        if (icon instanceof Identifier rl) {

            int[] size = ResearchBrowserScreen.getTextureSize(rl);
            int w = Math.max(size[0], 1);
            int h = Math.max(size[1], 1);
            if (h > w && h % w == 0) {
                int m = h / w;
                int frame = (int) (System.currentTimeMillis() / 150L % m);
                graphics.pose().pushMatrix();
                graphics.pose().translate(iconX, iconY);
                graphics.pose().scale(16.0f / w, 16.0f / w);
                ResearchBrowserScreen.drawTextureRegion(graphics, rl, 0, 0, 0, frame * w, w, w, w, h);
                graphics.pose().popMatrix();
            } else if (w > h && w % h == 0) {
                int m = w / h;
                int frame = (int) (System.currentTimeMillis() / 150L % m);
                graphics.pose().pushMatrix();
                graphics.pose().translate(iconX, iconY);
                graphics.pose().scale(16.0f / h, 16.0f / h);
                ResearchBrowserScreen.drawTextureRegion(graphics, rl, 0, 0, frame * h, 0, h, h, w, h);
                graphics.pose().popMatrix();
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

    // NEOFORGE_PORT: Toast.render(GuiGraphics, ToastComponent, long) ->
    // extractRenderState(GuiGraphicsExtractor, Font, long); width()/height() removed
    // (not in 26.x Toast); blit takes pipeline + sheet size (hud.png is 256x256).
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long timeSinceLastVisible) {
        if (firstDrawTime == 0) {
            firstDrawTime = timeSinceLastVisible;

            if (!hasPlayedSound) {
                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(ModSounds.LEARN.get(), 1.0f));
                hasPlayedSound = true;
            }
        }

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, 0, 224, 160, 32, 256, 256);

        drawResearchIcon(graphics, entry, 6, 8);

        graphics.text(font, title, 30, 7, 0xFFA239F1, false);

        String s = description.getString();
        float w = font.width(s);
        if (w > 124.0f) {
            float scale = 124.0f / w;
            graphics.pose().pushMatrix();
            graphics.pose().translate(30.0f, 18.0f);
            graphics.pose().scale(scale, scale);
            graphics.text(font, s, 0, 0, 0xFFFFAB09, false);
            graphics.pose().popMatrix();
        } else {
            graphics.text(font, s, 30, 18, 0xFFFFAB09, false);
        }
    }

    private Visibility wantedVisibility = Visibility.SHOW;

    @Override
    public Visibility getWantedVisibility() {
        return wantedVisibility;
    }

    // NEOFORGE_PORT: Forge getToken() (title|description dedup) kept verbatim;
    // without it all research toasts share NO_TOKEN and coalesce wrongly.
    @Override
    public Object getToken() {
        return title.getString() + "|" + description.getString();
    }

    @Override
    public void update(net.minecraft.client.gui.components.toasts.ToastManager manager, long visibleTime) {
        // NEOFORGE_PORT: same DISPLAY_TIME hide logic (visibleTime clock, as in Forge elapsed).
        this.wantedVisibility = visibleTime >= DISPLAY_TIME ? Visibility.HIDE : Visibility.SHOW;
    }
}