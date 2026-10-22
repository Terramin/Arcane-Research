package arcaneresearch.client.gui.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor; 
import net.minecraft.client.renderer.RenderPipelines; 
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; 
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput; 
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.seals.ISealConfigFilter;
import arcaneresearch.api.golems.seals.ISealConfigToggles;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.api.golems.seals.ISealGui;
import arcaneresearch.client.gui.widgets.HoverButton;
import arcaneresearch.client.gui.widgets.PlusMinusButton;
import arcaneresearch.common.golems.client.gui.*;
import arcaneresearch.common.menu.SealMenu;
import arcaneresearch.common.menu.slot.GhostSlot;

import java.util.ArrayList;
import java.util.List;

public class SealScreen extends AbstractContainerScreen<SealMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_base.png");
    private final List<GuiGolemCategoryButton> categoryButtons = new ArrayList<>();
    private final List<HoverButton> tagHovers = new ArrayList<>();
    private final int middleX;
    private final int middleY;
    private final int[] categories;
    private int currentCategory;

    public SealScreen(SealMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 232);
        this.middleX = imageWidth / 2;
        this.middleY = (imageHeight - 72) / 2 - 8;
        this.categories = menu.getAvailableCategories();
        this.currentCategory = menu.getCurrentCategory();
    }

    @Override
    protected void init() {
        super.init();
        setupCategories();
    }

    private void setupCategories() {
        clearWidgets();
        categoryButtons.clear();
        tagHovers.clear();

        int c = 0;
        float slice = 60.0f / categories.length;
        float start = -180.0f + (categories.length - 1) * slice / 2.0f;
        slice = Mth.clamp(slice, 12.0f, 24.0f);

        for (int cat : categories) {
            if (categories.length > 1) {
                int xx = (int) (Mth.cos((start - c * slice) / 180.0f * (float) Math.PI) * 86.0f);
                int yy = (int) (Mth.sin((start - c * slice) / 180.0f * (float) Math.PI) * 86.0f);

                final int catIndex = c;

                GuiGolemCategoryButton btn = new GuiGolemCategoryButton(
                        leftPos + middleX + xx - 8,
                        topPos + middleY + yy - 8,
                        "button.category." + cat,
                        cat,
                        currentCategory == cat,
                        b -> switchCategory(catIndex)
                );
                categoryButtons.add(btn);
                addRenderableWidget(btn);
            }
            c++;
        }

        int xxRedstone = (int) (Mth.cos((start - c * slice) / 180.0f * (float) Math.PI) * 86.0f);
        int yyRedstone = (int) (Mth.sin((start - c * slice) / 180.0f * (float) Math.PI) * 86.0f);
        addRenderableWidget(new GuiGolemRedstoneButton(
                leftPos + middleX + xxRedstone - 8, topPos + middleY + yyRedstone - 8,
                menu::isRedstoneSensitive, btn -> toggleRedstone()));

        switch (currentCategory) {
            case ISealGui.CAT_PRIORITY -> setupPriorityCategory();
            case ISealGui.CAT_FILTER -> setupFilterCategory();
            case ISealGui.CAT_AREA -> setupAreaCategory();
            case ISealGui.CAT_TOGGLES -> setupTogglesCategory();
            case ISealGui.CAT_TAGS -> setupTagsCategory();
        }
    }

    private void setupPriorityCategory() {
        addRenderableWidget(new PlusMinusButton(
                leftPos + middleX - 5 - 14, topPos + middleY - 17, true, btn -> adjustPriority(-1)));
        addRenderableWidget(new PlusMinusButton(
                leftPos + middleX - 5 + 14, topPos + middleY - 17, false, btn -> adjustPriority(1)));
        addRenderableWidget(new PlusMinusButton(
                leftPos + middleX + 18 - 12, topPos + middleY + 4, true, btn -> adjustColor(-1)));
        addRenderableWidget(new PlusMinusButton(
                leftPos + middleX + 18 + 11, topPos + middleY + 4, false, btn -> adjustColor(1)));

        if (isSealOwner()) {
            addRenderableWidget(new GuiGolemLockButton(
                    leftPos + middleX - 32, topPos + middleY,
                    menu::isLocked, btn -> toggleLock()));
        }
    }

    private void setupFilterCategory() {
        ISealEntity seal = menu.getSeal();
        if (seal.getSeal() instanceof ISealConfigFilter filter) {
            int size = filter.getFilterSize();
            int sy = 16 + (size - 1) / 3 * 12;
            addRenderableWidget(new GuiGolemBWListButton(
                    leftPos + middleX - 8, topPos + middleY + (size - 1) / 3 * 24 - sy + 27,
                    menu::isBlacklist, btn -> toggleBlacklist()));
        }
    }

    private void setupAreaCategory() {
        addRenderableWidget(new PlusMinusButton(
                leftPos + middleX - 5 - 14, topPos + middleY - 25, true, btn -> adjustArea(0, -1, 0)));
        addRenderableWidget(new PlusMinusButton(
                leftPos + middleX - 5 + 14, topPos + middleY - 25, false, btn -> adjustArea(0, 1, 0)));
        addRenderableWidget(new PlusMinusButton(
                leftPos + middleX - 5 - 14, topPos + middleY, true, btn -> adjustArea(-1, 0, 0)));
        addRenderableWidget(new PlusMinusButton(
                leftPos + middleX - 5 + 14, topPos + middleY, false, btn -> adjustArea(1, 0, 0)));
        addRenderableWidget(new PlusMinusButton(
                leftPos + middleX - 5 - 14, topPos + middleY + 25, true, btn -> adjustArea(0, 0, -1)));
        addRenderableWidget(new PlusMinusButton(
                leftPos + middleX - 5 + 14, topPos + middleY + 25, false, btn -> adjustArea(0, 0, 1)));
    }

    private void setupTogglesCategory() {
        ISealEntity seal = menu.getSeal();
        if (seal.getSeal() instanceof ISealConfigToggles toggles) {
            ISealConfigToggles.SealToggle[] props = toggles.getToggles();
            int s2 = props.length < 4 ? 8 : props.length < 6 ? 7 : props.length < 9 ? 6 : 5;
            int h = (props.length - 1) * s2;
            int w = 12;
            for (ISealConfigToggles.SealToggle prop : props) {
                int ww = 12 + Math.min(100, this.font.width(Component.translatable(prop.getName())));
                ww /= 2;
                if (ww > w) {
                    w = ww;
                }
            }
            for (int i = 0; i < props.length; i++) {
                final int toggleIndex = i;
                ISealConfigToggles.SealToggle prop = props[i];
                addRenderableWidget(new GuiGolemPropButton(
                        leftPos + middleX - w, topPos + middleY - 5 - h + i * (s2 * 2),
                        8, 8,
                        Component.translatable(prop.getName()), prop::getValue,
                        btn -> toggleProp(toggleIndex)));
            }
        }
    }

    private void setupTagsCategory() {
        ISealEntity seal = menu.getSeal();
        EnumGolemTrait[] required = seal.getSeal().getRequiredTags();
        if (required != null && required.length > 0) {
            for (int i = 0; i < required.length; i++) {
                EnumGolemTrait tag = required[i];
                HoverButton hb = new HoverButton(
                        leftPos + middleX + i * 18 - (required.length - 1) * 9,
                        topPos + middleY - 8, 16, 16,
                        tag.getLocalizedName().getString(), tag.getLocalizedDescription().getString(),
                        tag.icon);
                tagHovers.add(hb);
                addRenderableWidget(hb);
            }
        }
        EnumGolemTrait[] forbidden = seal.getSeal().getForbiddenTags();
        if (forbidden != null && forbidden.length > 0) {
            for (int i = 0; i < forbidden.length; i++) {
                EnumGolemTrait tag = forbidden[i];
                HoverButton hb = new HoverButton(
                        leftPos + middleX + i * 18 - (forbidden.length - 1) * 9,
                        topPos + middleY + 24, 16, 16,
                        tag.getLocalizedName().getString(), tag.getLocalizedDescription().getString(),
                        tag.icon);
                tagHovers.add(hb);
                addRenderableWidget(hb);
            }
        }
    }

    private void switchCategory(int catIndex) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, catIndex);
        }
        currentCategory = categories[catIndex];

        menu.setCurrentCategory(currentCategory);
        setupCategories();
    }

    private void adjustPriority(int delta) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, delta > 0 ? 81 : 80);
        }
    }

    private void adjustColor(int delta) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, delta > 0 ? 83 : 82);
        }
    }

    private void adjustArea(int dx, int dy, int dz) {
        if (minecraft != null && minecraft.gameMode != null) {
            int buttonId = 90;
            if (dy != 0) buttonId = dy > 0 ? 91 : 90;
            else if (dx != 0) buttonId = dx > 0 ? 93 : 92;
            else if (dz != 0) buttonId = dz > 0 ? 95 : 94;
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }

    private void toggleLock() {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, menu.isLocked() ? 26 : 25);
        }
    }

    private boolean isSealOwner() {
        try {
            if (minecraft == null || minecraft.player == null) return false;
            var seal = menu.getSeal();
            if (seal == null || seal.getOwner() == null) return false;
            return seal.getOwner().equals(minecraft.player.getUUID().toString());
        } catch (Exception e) {
            return false;
        }
    }

    private void toggleRedstone() {
        if (minecraft != null && minecraft.gameMode != null
                && isSealOwner()) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, menu.isRedstoneSensitive() ? 28 : 27);
        }
    }

    private void toggleBlacklist() {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, menu.isBlacklist() ? 21 : 20);
        }
    }

    private void toggleProp(int index) {
        if (minecraft != null && minecraft.gameMode != null) {
            ISealEntity seal = menu.getSeal();
            if (seal.getSeal() instanceof ISealConfigToggles toggles) {
                boolean currentValue = toggles.getToggles()[index].getValue();
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, currentValue ? 60 + index : 30 + index);
            }
        }
        setupCategories();
    }

    @Override
    protected void slotClicked(@NotNull Slot slot, int slotId, int mouseButton, @NotNull ContainerInput type) {

        if (slot instanceof GhostSlot && menu.getSeal().getSeal() instanceof ISealConfigFilter) {
            if (minecraft != null && minecraft.gameMode != null && minecraft.player != null) {
                
                minecraft.gameMode.handleContainerInput(
                        menu.containerId, slotId, mouseButton, type, minecraft.player);
            }
            return;
        }
        super.slotClicked(slot, slotId, mouseButton, type);
    }

    
    @Override
    protected boolean checkHotbarKeyPressed(net.minecraft.client.input.KeyEvent event) {
        return false;
    }

    
    
    private void renderFilterCounts(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {

        if (currentCategory == ISealGui.CAT_FILTER
                && menu.getSeal().getSeal() instanceof ISealConfigFilter filter
                && !filter.isBlacklist()) {
            for (int i = 0; i < filter.getFilterSize() && i < menu.slots.size(); i++) {
                Slot slot = menu.slots.get(i);
                if (slot.isActive() && slot.hasItem()) {
                    int sz = filter.getFilterSlotSize(i);
                    String s = sz == 0
                            ? net.minecraft.ChatFormatting.GOLD + "*"
                            : String.valueOf(sz);
                    graphics.text(font, s,
                            leftPos + slot.x + 19 - 2 - font.width(s),
                            topPos + slot.y + 6 + 3, 0xFFFFFFFF, true);
                }
            }
        }
        for (HoverButton hb : tagHovers) {
            hb.renderTooltip(graphics, mouseX, mouseY);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + middleX - 80, topPos + middleY - 80, 96, 0, 160, 160, 256, 256);

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos + 143, 0, 167, 176, 89, 256, 256);

        String categoryName = getCategoryName(currentCategory);
        graphics.centeredText(font, categoryName, leftPos + middleX, topPos + middleY - 64, 0xFFFFFFFF);

        switch (currentCategory) {
            case ISealGui.CAT_PRIORITY -> renderPriorityCategory(graphics, mouseX, mouseY);
            case ISealGui.CAT_FILTER -> renderFilterCategory(graphics);
            case ISealGui.CAT_AREA -> renderAreaCategory(graphics);
            case ISealGui.CAT_TAGS -> renderTagsCategory(graphics);
        }
    }
    private void renderPriorityCategory(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + middleX + 17, topPos + middleY + 3, 2, 18, 12, 12, 256, 256);
        int color = menu.getColor();
        if (color >= 1 && color <= 16) {
            
            int diffuse = DyeColor.byId(color - 1).getTextureDiffuseColor();
            int tint = net.minecraft.util.ARGB.colorFromFloat(1.0f,
                    ((diffuse >> 16) & 0xFF) / 255.0f,
                    ((diffuse >> 8) & 0xFF) / 255.0f,
                    (diffuse & 0xFF) / 255.0f);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + middleX + 20, topPos + middleY + 6, 74, 31, 6, 6, 256, 256, tint);
        }
        int mx = mouseX - leftPos;
        int my = mouseY - topPos;
        if (mx >= middleX + 5 && mx <= middleX + 41 && my >= middleY + 3 && my <= middleY + 15) {
            Component label;
            if (color >= 1 && color <= 16) {
                DyeColor dye = DyeColor.byId(color - 1);
                label = Component.translatable("golem.prop.color",
                        Component.translatable("color.minecraft." + dye.getName()));
            } else {
                label = Component.translatable("golem.prop.colorall");
            }
            graphics.centeredText(font, label, leftPos + middleX + 23, topPos + middleY + 17, 0xFFFFFFFF);
        }

        graphics.centeredText(font, Component.translatable("golem.prop.priority"),
                leftPos + middleX, topPos + middleY - 28, 0xFFBBCC9F);
        graphics.centeredText(font, String.valueOf(menu.getPriority()),
                leftPos + middleX, topPos + middleY - 16, 0xFFFFFFFF);

        if (isSealOwner()) {
            graphics.centeredText(font, Component.translatable("golem.prop.owner"),
                    leftPos + middleX, topPos + middleY + 32, 0xFFBBCC9F);
        }
    }

    private void renderFilterCategory(GuiGraphicsExtractor graphics) {
        ISealEntity seal = menu.getSeal();
        if (seal.getSeal() instanceof ISealConfigFilter filter) {
            int size = filter.getFilterSize();
            int sx = 16 + (size - 1) % 3 * 12;
            int sy = 16 + (size - 1) / 3 * 12;

            for (int i = 0; i < size; i++) {
                int x = i % 3;
                int y = i / 3;
                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + middleX + x * 24 - sx, topPos + middleY + y * 24 - sy, 0, 56, 32, 32, 256, 256);
            }
        }
    }

    private void renderAreaCategory(GuiGraphicsExtractor graphics) {
        BlockPos area = menu.getArea();

        graphics.centeredText(font, Component.translatable("button.caption.y"),
                leftPos + middleX, topPos + middleY - 33, 0xFFDDDDDD);
        graphics.centeredText(font, Component.translatable("button.caption.x"),
                leftPos + middleX, topPos + middleY - 9, 0xFFDDDDDD);
        graphics.centeredText(font, Component.translatable("button.caption.z"),
                leftPos + middleX, topPos + middleY + 15, 0xFFDDDDDD);

        graphics.centeredText(font, String.valueOf(area.getY()),
                leftPos + middleX, topPos + middleY - 24, 0xFFFFFFFF);
        graphics.centeredText(font, String.valueOf(area.getX()),
                leftPos + middleX, topPos + middleY, 0xFFFFFFFF);
        graphics.centeredText(font, String.valueOf(area.getZ()),
                leftPos + middleX, topPos + middleY + 24, 0xFFFFFFFF);
    }

    private void renderTagsCategory(GuiGraphicsExtractor graphics) {
        graphics.centeredText(font, Component.translatable("button.caption.required"),
                leftPos + middleX, topPos + middleY - 26, 0xFFDDDDDD);
        graphics.centeredText(font, Component.translatable("button.caption.forbidden"),
                leftPos + middleX, topPos + middleY + 6, 0xFFDDDDDD);

    }

    @Override
    protected void extractLabels(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    
    
    
    
    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractContents(graphics, mouseX, mouseY, partialTick);
        renderFilterCounts(graphics, mouseX, mouseY);
    }

    private String getCategoryName(int category) {
        return Component.translatable("button.category." + category).getString();
    }
}