package arcaneresearch.discovered.client;

import arcaneresearch.helper.GuiBlitHelper;
import arcaneresearch.helper.IDsHelper;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.lib.AspectRenderer;
import arcaneresearch.discovered.note.ItemDiscoveredResearchNote;
import arcaneresearch.discovered.note.DiscoveredNoteData;
import arcaneresearch.discovered.note.DiscoveredNotes;
import arcaneresearch.discovered.table.CognitionNoteTableMenu;
import arcaneresearch.discovered.table.CognitionNoteTableTile;
import org.jetbrains.annotations.NotNull;

public class CognitionNoteTableScreen extends AbstractContainerScreen<CognitionNoteTableMenu> {
    private static final ResourceLocation BG =
            IDsHelper.mod("textures/gui/guiresearchtable.png");
    private static final ResourceLocation PARCHMENT =
            IDsHelper.mod("textures/misc/parchment.png");
    private static final ResourceLocation UNKNOWN_ICON =
            IDsHelper.mod("textures/aspects/_unknown.png");

    private float popupScale = 0.05F;
    private long buttonCooldown;
    private final List<int[]> coords = new ArrayList<>();
    private final List<Aspect> diagramTags = new ArrayList<>();
    private final List<Float> diagramProgress = new ArrayList<>();
    private ItemStack cachedNoteSource = ItemStack.EMPTY;
    private DiscoveredNoteData cachedNoteData;

    public CognitionNoteTableScreen(CognitionNoteTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 240;
        imageHeight = 242;
        inventoryLabelY = 148;
    }

    private boolean buttonActive() {
        return buttonCooldown <= System.currentTimeMillis() && isResearchActive();
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        GuiBlitHelper.blit(graphics, BG, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        GuiBlitHelper.blit(graphics, BG, leftPos + 76, topPos + 40, 241, buttonActive() ? 0 : 15, 15, 15, 256, 256);
        boolean safe = menu.isSafe();
        GuiBlitHelper.blit(graphics, BG, leftPos + 51, topPos + 130, safe ? 109 : 87, 246, 29, 7, 256, 256);
        int safeX = mouseX - leftPos - 51;
        int safeY = mouseY - topPos - 130;
        if (safeX >= 0 && safeY >= 0 && safeX < 29 && safeY < 7) {
            String mode = Component.translatable(safe
                    ? "gui.arcaneresearch.cognition.cursory"
                    : "gui.arcaneresearch.cognition.thorough").getString();
            graphics.pose().pushPose();
            graphics.pose().translate(leftPos + 65.0F, topPos + 142.0F, 0.0F);
            graphics.pose().scale(0.65F, 0.65F, 1.0F);
            graphics.drawString(font, mode, -font.width(mode) / 2, 0, 0xAEAE5E, false);
            graphics.pose().popPose();
        }
        DiscoveredNoteData note = noteData();
        for (int a = 0; a < CognitionNoteTableTile.INPUT_COUNT; a++) {
            Aspect aspect = menu.getTagAspect(a);
            if (aspect == null) {
                continue;
            }
            String tag = aspect.getTag();
            float opacity = note != null && tag != null && note.getFailed(tag) >= 10 ? 0.15F : 1.0F;
            AspectRenderer.drawAspect(graphics, leftPos + 48, topPos + 32 + 16 * a,
                    aspect, menu.getTagAmount(a), (int) menu.getTagBonus(a), opacity, false);
        }
        drawResearchData(graphics, note, leftPos + 167, topPos + 76);
        drawPopup(graphics, mouseX, mouseY);
    }

    private DiscoveredNoteData noteData() {
        ItemStack note = menu.getNoteStack();
        if (note == null || note.isEmpty() || !(note.getItem() instanceof ItemDiscoveredResearchNote)) {
            cachedNoteSource = ItemStack.EMPTY;
            cachedNoteData = null;
            return null;
        }
        if (!ItemStack.matches(note, cachedNoteSource)) {
            cachedNoteSource = note.copy();
            cachedNoteData = DiscoveredNoteData.read(note);
        }
        return cachedNoteData;
    }

    private void drawResearchData(GuiGraphics graphics, DiscoveredNoteData note, int x, int y) {
        coords.clear();
        diagramTags.clear();
        diagramProgress.clear();
        if (note == null || note.key == null) {
            return;
        }
        Aspect[] tags = note.tags;
        if (tags == null) {
            return;
        }
        float total = note.getTotalProgress();
        for (Aspect tag : tags) {
            if (tag == null) {
                continue;
            }
            float progress = note.getTagProgress(tag);
            if (progress > 0.0F || total >= 0.5F) {
                diagramTags.add(tag);
                diagramProgress.add(progress);
            }
        }
        if (diagramTags.isEmpty()) {
            return;
        }
        int count = diagramTags.size();
        float slice = 360.0F / count;
        float rotation = -90.0F;
        for (int a = 0; a < count; a++) {
            float progress = diagramProgress.get(a);
            float radius = 15.0F + 40.0F * progress;
            int px = (int) (x + Mth.cos(rotation / 180.0F * Mth.PI) * radius) - 8;
            int py = (int) (y + Mth.sin(rotation / 180.0F * Mth.PI) * radius) - 8;
            rotation += slice;
            coords.add(new int[]{px, py});
        }
        for (int a = 0; a < count; a++) {
            Aspect shown = diagramTags.get(a);
            float progress = diagramProgress.get(a);
            if (progress < 0.3F && total < 0.75F) {
                GuiBlitHelper.blit(graphics, UNKNOWN_ICON, coords.get(a)[0], coords.get(a)[1],
                        0, 0, 16, 16, 16, 16);
            } else {
                AspectRenderer.drawAspect(graphics, coords.get(a)[0], coords.get(a)[1],
                        shown, 0, 0, 1.0F, false);
            }
        }
        if (count > 1) {
            for (int a = 0; a < count; a++) {
                float instability = 1.0F - (diagramProgress.get(a) + total) / 2.0F;
                int[] point = coords.get(a);
                DiscoveredGuiLines.unstable(graphics, x, y, point[0] + 8, point[1] + 8, instability, 0.5F);
                if (count > 2) {
                    for (int b = a + 1; b < count; b++) {
                        int[] other = coords.get(b);
                        DiscoveredGuiLines.unstable(graphics, other[0] + 8, other[1] + 8,
                                point[0] + 8, point[1] + 8, instability, 0.5F);
                    }
                }
            }
        }
        for (int a = 0; a < count; a++) {
            if (diagramProgress.get(a) >= 1.0F) {
                int px = coords.get(a)[0];
                int py = coords.get(a)[1];
                DiscoveredGuiLines.unstable(graphics, px - 1, py - 1, px - 1, py + 17, 0.3F, 1.0F);
                DiscoveredGuiLines.unstable(graphics, px - 1, py - 1, px + 17, py - 1, 0.3F, 1.0F);
                DiscoveredGuiLines.unstable(graphics, px + 17, py - 1, px + 17, py + 17, 0.3F, 1.0F);
                DiscoveredGuiLines.unstable(graphics, px - 1, py + 17, px + 17, py + 17, 0.3F, 1.0F);
            }
        }
    }

    private void drawPopup(GuiGraphics graphics, int mouseX, int mouseY) {
        int hx = mouseX - leftPos - 103;
        int hy = mouseY - topPos - 12;
        if (hx >= 0 && hy >= 0 && hx < 17 && hy < 17) {
            popupScale *= 1.25F;
            if (popupScale > 0.48F) {
                popupScale = 0.48F;
            }
        } else if (popupScale > 0.05F) {
            popupScale *= 0.75F;
            if (popupScale < 0.05F) {
                popupScale = 0.05F;
            }
        }
        if (popupScale <= 0.05F) {
            return;
        }
        DiscoveredNoteData data = noteData();
        if (data == null || data.key == null) {
            return;
        }
        Component desc = describe(data.key);
        if (desc == null) {
            return;
        }
        DiscoveredNoteText.Fit fit = DiscoveredNoteText.parchmentFit(
                font, desc, data.getTotalProgress() < 0.5F, 130, 1.0F, 150.0F, 0.5F);
        List<FormattedCharSequence> lines = fit.lines();
        float scale = fit.scale();
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos + 105.0F, topPos + 14.0F, 300.0F);
        graphics.pose().scale(popupScale, popupScale, 1.0F);
        GuiBlitHelper.blit(graphics, PARCHMENT, 0, 0, 0, 0, 256, 256, 256, 256);
        graphics.pose().scale(1.5F, 1.5F, 1.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        float totalH = lines.size() * font.lineHeight;
        float startY = 80.0F / scale - totalH / 2.0F;
        for (int i = 0; i < lines.size(); i++) {
            font.drawInBatch(lines.get(i), 22.0F / scale, startY + i * font.lineHeight, 0xFF201510, false,
                    graphics.pose().last().pose(), graphics.bufferSource(),
                    Font.DisplayMode.NORMAL, 0,
                    net.minecraft.client.renderer.LightTexture.FULL_BRIGHT);
        }
        graphics.pose().popPose();
    }

    private boolean isResearchActive() {
        if (menu.getTagAspect(0) == null) {
            return false;
        }
        ItemStack note = menu.getNoteStack();
        if (note == null || note.isEmpty()) {
            return menu.getSlot(CognitionNoteTableTile.PAPER_SLOT).hasItem();
        }
        DiscoveredNoteData data = noteData();
        return data != null && !data.isComplete();
    }

    private Component describe(String key) {
        return DiscoveredNotes.researchText(key);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && minecraft != null && minecraft.gameMode != null) {
            int x = (int) mouseX - leftPos;
            int y = (int) mouseY - topPos;
            if (x >= 76 && x < 91 && y >= 40 && y < 55 && buttonActive()) {
                minecraft.gameMode.handleInventoryButtonClick(
                        menu.containerId, CognitionNoteTableMenu.BUTTON_RESEARCH);
                buttonCooldown = System.currentTimeMillis() + 150L;
                return true;
            }
            if (x >= 51 && x < 130 && y >= 128 && y < 140
                    && buttonCooldown <= System.currentTimeMillis()) {
                minecraft.gameMode.handleInventoryButtonClick(
                        menu.containerId, CognitionNoteTableMenu.BUTTON_TOGGLE_SAFE);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        int x = mouseX - leftPos;
        int y = mouseY - topPos;
        if (x >= 76 && x < 91 && y >= 40 && y < 55) {
            graphics.renderTooltip(font,
                    Component.translatable("gui.arcaneresearch.cognition.do_research"), mouseX, mouseY);
            return;
        }
        for (int i = 0; i < CognitionNoteTableTile.INPUT_COUNT; i++) {
            Aspect aspect = menu.getTagAspect(i);
            if (aspect == null) {
                continue;
            }
            String tag = aspect.getTag();
            if (tag == null) {
                continue;
            }
            int ix = leftPos + 48;
            int iy = topPos + 32 + i * 16;
            if (mouseX >= ix && mouseX < ix + 16 && mouseY >= iy && mouseY < iy + 16) {
                List<Component> lines = new ArrayList<>();
                lines.add(Component.translatable("tc.aspect." + tag));
                graphics.renderTooltip(font, lines, java.util.Optional.empty(), mouseX, mouseY);
            }
        }
        DiscoveredNoteData note = noteData();
        if (note == null || note.key == null) {
            return;
        }
        int limit = Math.min(diagramTags.size(), coords.size());
        for (int a = 0; a < limit; a++) {
            int mx = mouseX - coords.get(a)[0];
            int my = mouseY - coords.get(a)[1];
            if (mx >= 0 && my >= 0 && mx < 16 && my < 16) {
                Aspect aspect = diagramTags.get(a);
                String tag = aspect.getTag();
                if (tag == null) {
                    continue;
                }
                List<Component> lines = new ArrayList<>(
                        List.of(Component.translatable("tc.aspect." + tag)));
                float progress = note.getTagProgress(aspect);
                if (progress > 0.333332F) {
                    lines.add(Component.literal(String.valueOf((int) (progress * 100.0F)))
                            .append(Component.translatable("tc.thaumcraft.discoveryprogress")));
                }
                graphics.renderTooltip(font, lines, java.util.Optional.empty(), mouseX, mouseY);
            }
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}