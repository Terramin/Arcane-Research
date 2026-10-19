package arcaneresearch.discovered.client;

import arcaneresearch.helper.GuiBlitHelper;
import arcaneresearch.helper.IDsHelper;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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

/**
 * Экран стола записок, полный порт reforged ResearchTableScreen
 * (он же TC3 GuiResearchTable): фон 240x242, спрайт кнопки 15x15,
 * спрайт safe-тоггла 29x7, теги входов через AspectRenderer, круговая
 * диаграмма исследования с линиями нестабильности, попап-пергамент,
 * тултипы тегов с процентами. Кнопки — зоны кликов с кулдауном.
 *
 * NEOFORGE_PORT (26.3 render-state): GuiGraphics -> GuiGraphicsExtractor;
 * renderBg -> extractBackground; renderLabels -> extractLabels (пустой);
 * тултипы рисуются в конце extractBackground (отдельного оверрайда
 * renderTooltip у суперкласса больше нет); render() не оверрайдится
 * (фреймворк); mouseClicked(MouseButtonEvent, boolean); pose 2D
 * (pushMatrix/translate(x,y)/scale(x,y)/popMatrix); blit с
 * RenderPipelines.GUI_TEXTURED; drawString -> text; popup-текст через
 * pose-масштаб + text (drawInBatch/bufferSource удалены).
 */
public class CognitionNoteTableScreen extends AbstractContainerScreen<CognitionNoteTableMenu> {

    private static final Identifier BG =
            IDsHelper.mod("textures/gui/guiresearchtable.png");
    private static final Identifier PARCHMENT =
            IDsHelper.mod("textures/misc/parchment.png");
    private static final Identifier UNKNOWN_ICON =
            IDsHelper.mod("textures/aspects/_unknown.png");

    private float popupScale = 0.05F;
    private long buttonCooldown;

    private final List<int[]> coords = new ArrayList<>();
    private final List<Aspect> diagramTags = new ArrayList<>();

    public CognitionNoteTableScreen(CognitionNoteTableMenu menu, Inventory inventory, Component title) {
        // NEOFORGE_PORT: 26.x screen size via 5-arg super ctor (was imageWidth/imageHeight fields).
        super(menu, inventory, title, 240, 242);
        inventoryLabelY = 148;
    }

    private boolean buttonActive() {
        return buttonCooldown <= System.currentTimeMillis() && isResearchActive();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        // 1.20.1 Screen.renderBackground dim (fillGradient)
        graphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        GuiBlitHelper.blit(graphics, BG, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        GuiBlitHelper.blit(graphics, BG, leftPos + 76, topPos + 40, 241, buttonActive() ? 0 : 15, 15, 15, 256, 256);
        GuiBlitHelper.blit(graphics, BG, leftPos + 51, topPos + 130, menu.isSafe() ? 109 : 87, 246, 29, 7, 256, 256);

        int safeX = mouseX - leftPos - 51;
        int safeY = mouseY - topPos - 130;
        if (safeX >= 0 && safeY >= 0 && safeX < 29 && safeY < 7) {
            String mode = menu.isSafe()
                    ? Component.translatable("gui.arcaneresearch.cognition.cursory").getString()
                    : Component.translatable("gui.arcaneresearch.cognition.thorough").getString();
            var pose = graphics.pose();
            pose.pushMatrix();
            pose.translate(leftPos + 65.0F, topPos + 142.0F);
            pose.scale(0.65F, 0.65F);
            graphics.text(font, mode, -font.width(mode) / 2, 0, 0xAEAE5E, false);
            pose.popMatrix();
        }

        DiscoveredNoteData note = noteData();
        for (int a = 0; a < CognitionNoteTableTile.INPUT_COUNT; a++) {
            Aspect aspect = menu.getTagAspect(a);
            if (aspect == null) {
                continue;
            }
            float opacity = note != null && note.getFailed(aspect.getTag()) >= 10 ? 0.15F : 1.0F;
            AspectRenderer.drawAspect(graphics, leftPos + 48, topPos + 32 + 16 * a,
                    aspect, menu.getTagAmount(a), (int) menu.getTagBonus(a), opacity, false);
        }
        drawResearchData(graphics, note, leftPos + 167, topPos + 76);
        drawPopup(graphics, mouseX, mouseY);
        renderNoteTooltips(graphics, mouseX, mouseY);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    private DiscoveredNoteData noteData() {
        ItemStack note = menu.getNoteStack();
        if (!(note.getItem() instanceof ItemDiscoveredResearchNote)) {
            return null;
        }
        return DiscoveredNoteData.read(note);
    }

    /**
     * Круговая диаграмма исследования, порт reforged drawResearchData:
     * теги по кругу (радиус 15+40*прогресс), линии нестабильности,
     * рамки завершённых. UNKNOWN — иконка _unknown.png.
     */
    private void drawResearchData(GuiGraphicsExtractor graphics, DiscoveredNoteData note, int x, int y) {
        coords.clear();
        diagramTags.clear();
        if (note == null || note.key == null) {
            return;
        }
        float total = note.getTotalProgress();
        for (Aspect tag : note.tags) {
            if (tag == null) {
                continue;
            }
            float progress = note.getTagProgress(tag);
            if (progress > 0.0F || total >= 0.5F) {
                diagramTags.add(tag);
            }
        }
        if (diagramTags.isEmpty()) {
            return;
        }
        float slice = 360.0F / diagramTags.size();
        float rotation = -90.0F;
        for (Aspect tag : diagramTags) {
            float radius = 15.0F + 40.0F * note.getTagProgress(tag);
            int px = (int) (x + Mth.cos(rotation / 180.0F * Mth.PI) * radius) - 8;
            int py = (int) (y + Mth.sin(rotation / 180.0F * Mth.PI) * radius) - 8;
            rotation += slice;
            coords.add(new int[]{px, py});
        }
        for (int a = 0; a < diagramTags.size(); a++) {
            Aspect shown = diagramTags.get(a);
            float progress = note.getTagProgress(shown);
            if (progress < 0.3F && total < 0.75F
                    || progress < 0.2F && total < 0.75F) {
                GuiBlitHelper.blit(graphics, UNKNOWN_ICON, coords.get(a)[0], coords.get(a)[1],
                        0, 0, 16, 16, 16, 16);
            } else {
                AspectRenderer.drawAspect(graphics, coords.get(a)[0], coords.get(a)[1],
                        shown, 0, 0, 1.0F, false);
            }
        }
        if (diagramTags.size() > 1) {
            for (int a = 0; a < diagramTags.size(); a++) {
                float instability = 1.0F - (note.getTagProgress(diagramTags.get(a)) + total) / 2.0F;
                int[] point = coords.get(a);
                DiscoveredGuiLines.unstable(graphics, x, y, point[0] + 8, point[1] + 8, instability, 0.5F);
                if (diagramTags.size() > 2) {
                    for (int b = a + 1; b < diagramTags.size(); b++) {
                        int[] other = coords.get(b);
                        DiscoveredGuiLines.unstable(graphics, other[0] + 8, other[1] + 8,
                                point[0] + 8, point[1] + 8, instability, 0.5F);
                    }
                }
            }
        }
        for (int a = 0; a < diagramTags.size(); a++) {
            if (note.getTagProgress(diagramTags.get(a)) >= 1.0F) {
                int px = coords.get(a)[0];
                int py = coords.get(a)[1];
                DiscoveredGuiLines.unstable(graphics, px - 1, py - 1, px - 1, py + 17, 0.3F, 1.0F);
                DiscoveredGuiLines.unstable(graphics, px - 1, py - 1, px + 17, py - 1, 0.3F, 1.0F);
                DiscoveredGuiLines.unstable(graphics, px + 17, py - 1, px + 17, py + 17, 0.3F, 1.0F);
                DiscoveredGuiLines.unstable(graphics, px - 1, py + 17, px + 17, py + 17, 0.3F, 1.0F);
            }
        }
    }

    /**
     * Паритет TC3: попап-пергамент с longText растёт при наведении
     * на зону 17x17 (+103,+12): 0.05 -> 0.48, шифр при прогрессе <0.5.
     */
    private void drawPopup(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
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
        ItemStack note = menu.getNoteStack();
        if (!(note.getItem() instanceof ItemDiscoveredResearchNote)) {
            return;
        }
        DiscoveredNoteData data = DiscoveredNoteData.read(note);
        if (data.key == null) {
            return;
        }
        Component desc = describe(data.key);
        if (desc == null) {
            return;
        }
        // Низ не режем: масштаб и ширина переноса подбираются совместно,
        // центрирование 80-y — паритет TC3 GuiResearchTable.
        DiscoveredNoteText.Fit fit = DiscoveredNoteText.parchmentFit(
                font, desc, data.getTotalProgress() < 0.5F, 130, 1.0F, 150.0F, 0.5F);
        List<FormattedCharSequence> lines = fit.lines();
        float scale = fit.scale();
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(leftPos + 105.0F, topPos + 14.0F);
        pose.scale(popupScale, popupScale);
        GuiBlitHelper.blit(graphics, PARCHMENT, 0, 0, 0, 0, 256, 256, 256, 256);
        pose.scale(1.5F, 1.5F);
        pose.scale(scale, scale);
        float totalH = lines.size() * font.lineHeight;
        float startY = 80.0F / scale - totalH / 2.0F;
        for (int i = 0; i < lines.size(); i++) {
            graphics.text(font, lines.get(i), Math.round(22.0F / scale),
                    Math.round(startY + i * font.lineHeight), 0xFF201510, false);
        }
        pose.popMatrix();
    }

    private boolean isResearchActive() {
        ItemStack note = menu.getNoteStack();
        boolean paper = menu.getSlot(CognitionNoteTableTile.PAPER_SLOT).hasItem();
        if (menu.getTagAspect(0) == null) {
            return false;
        }
        if (note.isEmpty()) {
            return paper;
        }
        return note.getItem() instanceof ItemDiscoveredResearchNote
                && !DiscoveredNoteData.read(note).isComplete();
    }

    private Component describe(String key) {
        return DiscoveredNotes.researchText(key);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
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
        return super.mouseClicked(event, doubleClick);
    }

    /** Тултипы кнопок/тегов/диаграммы (раньше renderTooltip-оверрайд). */
    private void renderNoteTooltips(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        // NEOFORGE_PORT: Forge renderTooltip body; immediate overloads gone ->
        // deferred setTooltipForNextFrame / setComponentTooltipForNextFrame.
        int x = mouseX - leftPos;
        int y = mouseY - topPos;
        if (x >= 76 && x < 91 && y >= 40 && y < 55) {
            graphics.setTooltipForNextFrame(font,
                    Component.translatable("gui.arcaneresearch.cognition.do_research"), mouseX, mouseY);
            return;
        }
        for (int i = 0; i < CognitionNoteTableTile.INPUT_COUNT; i++) {
            Aspect aspect = menu.getTagAspect(i);
            if (aspect == null) {
                continue;
            }
            int ix = leftPos + 48;
            int iy = topPos + 32 + i * 16;
            if (mouseX >= ix && mouseX < ix + 16 && mouseY >= iy && mouseY < iy + 16) {
                graphics.setComponentTooltipForNextFrame(font,
                        List.of(Component.translatable("tc.aspect." + aspect.getTag())), mouseX, mouseY);
            }
        }
        DiscoveredNoteData note = noteData();
        if (note == null || note.key == null) {
            return;
        }
        for (int a = 0; a < diagramTags.size() && a < coords.size(); a++) {
            int mx = mouseX - coords.get(a)[0];
            int my = mouseY - coords.get(a)[1];
            if (mx >= 0 && my >= 0 && mx < 16 && my < 16) {
                Aspect aspect = diagramTags.get(a);
                List<Component> lines = new ArrayList<>(
                        List.of(Component.translatable("tc.aspect." + aspect.getTag())));
                float progress = note.getTagProgress(aspect);
                if (progress > 0.333332F) {
                    lines.add(Component.literal(String.valueOf((int) (progress * 100.0F)))
                            .append(Component.translatable("tc.thaumcraft.discoveryprogress")));
                }
                graphics.setComponentTooltipForNextFrame(font, lines, mouseX, mouseY);
            }
        }
    }
}
