package arcaneresearch.client.gui.screens;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor; // NEOFORGE_PORT: was GuiGraphics
import net.minecraft.client.renderer.RenderPipelines; // NEOFORGE_PORT: blit needs a pipeline
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was ResourceLocation
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput; // NEOFORGE_PORT: ClickType -> ContainerInput
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.gui.widgets.ImageButton;
import arcaneresearch.client.gui.widgets.PlusMinusButton;
import arcaneresearch.client.gui.widgets.ScrollButton;
import arcaneresearch.client.gui.widgets.SliderWidget;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.misc.PacketLogisticsRequestToServer;
import arcaneresearch.common.lib.network.misc.PacketMiscStringToServer;
import arcaneresearch.common.menu.LogisticsMenu;
import arcaneresearch.init.ModSounds;

public class LogisticsScreen extends AbstractContainerScreen<LogisticsMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_logistics.png");
    private static final Identifier GUI_BASE_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_base.png");

    private int selectedSlot = -1;
    private ItemStack selectedStack = ItemStack.EMPTY;

    private int lastScrollPos = 0;
    private int lastServerStart = 0;

    private int stackSize = 1;
    private int lastStackSize = 1;
    private boolean stacksizeUpdated = false;

    private long lastUpdateTime = 0;

    private EditBox searchField;
    private ImageButton requestButton;
    private PlusMinusButton decreaseButton;
    private PlusMinusButton increaseButton;
    private SliderWidget scrollbar;
    private SliderWidget countbar;

    public LogisticsScreen(LogisticsMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 215, 215);
    }

    @Override
    protected void init() {
        super.init();

        addRenderableWidget(new ScrollButton(leftPos + 195, topPos + 16, true, true, btn -> scroll(-1)));
        addRenderableWidget(new ScrollButton(leftPos + 195, topPos + 180, false, true, btn -> scroll(1)));

        decreaseButton = new PlusMinusButton(leftPos + 13, topPos + 195, true, btn -> adjustStackSize(-1));
        increaseButton = new PlusMinusButton(leftPos + 57, topPos + 195, false, btn -> adjustStackSize(1));
        addRenderableWidget(decreaseButton);
        addRenderableWidget(increaseButton);

        scrollbar = new SliderWidget(leftPos + 196, topPos + 28, 8, 149,
                0.0f, Math.max(0, menu.getEnd()), menu.getStart(), true, this::onScrollbarDragged);
        addRenderableWidget(scrollbar);

        countbar = new SliderWidget(leftPos + 24, topPos + 196, 32, 8,
                1.0f, 64.0f, 1.0f, false, this::onCountbarDragged);
        addRenderableWidget(countbar);

        requestButton = new ImageButton(leftPos + 116, topPos + 200, 40, 13,
                "tc.logistics.request", "logistics.request", GUI_BASE_TEXTURE,
                37, 82, 40, 13, btn -> requestItems());
        addRenderableWidget(requestButton);

        searchField = new EditBox(font, leftPos + 143, topPos + 196, 55, font.lineHeight, Component.empty());
        searchField.setMaxLength(10);
        searchField.setBordered(true);
        searchField.setVisible(true);
        searchField.setTextColor(0xFFFFFFFF);
        searchField.setResponder(this::onSearchTextChanged);
        addRenderableWidget(searchField);

        updateButtonVisibility();
    }

    private void onScrollbarDragged(float value) {
        int v = Math.round(value);
        if (v != lastScrollPos && minecraft != null && minecraft.gameMode != null) {
            lastScrollPos = v;
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 100 + v);
        }
    }

    private void onCountbarDragged(float value) {
        if (selectedSlot < 0) return;
        int v = Math.round(value);
        if (v != lastStackSize) {
            lastStackSize = v;
            stackSize = v;
        }
    }

    private void updateButtonVisibility() {

        boolean hasSelection = selectedSlot >= 0 && !selectedStack.isEmpty();
        decreaseButton.visible = hasSelection;
        increaseButton.visible = hasSelection;
        countbar.visible = hasSelection;
        requestButton.visible = hasSelection;
    }

    private void adjustStackSize(int delta) {
        if (selectedSlot < 0 || selectedStack.isEmpty()) return;
        stackSize += delta;
        stackSize = Math.max(1, Math.min(stackSize, selectedStack.getCount()));
        stacksizeUpdated = true;
    }

    private void scroll(int direction) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, direction > 0 ? 0 : 1);
        }
    }

    private void onSearchTextChanged(String text) {

        PacketHandler.sendToServer(new PacketMiscStringToServer(0, text));
    }

    private void requestItems() {
        if (selectedSlot < 0 || selectedStack.isEmpty()) return;

        if (menu.isDeliverToPlayer()) {
            PacketHandler.sendToServer(new PacketLogisticsRequestToServer(
                    selectedStack.copyWithCount(1),
                    stackSize
            ));
        } else {
            PacketHandler.sendToServer(new PacketLogisticsRequestToServer(
                    menu.getTargetPos(),
                    menu.getTargetSide(),
                    selectedStack.copyWithCount(1),
                    stackSize
            ));
        }

        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.CLACK.get(), 1.0f));
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        // NEOFORGE_PORT (26.3): no extra dim - super.extractBackground already draws the in-game gradient (was drawn twice).
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);

        if (selectedSlot >= 0 && selectedSlot < 81) {
            int selX = selectedSlot % 9;
            int selY = selectedSlot / 9;
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 17 + selX * 19, topPos + 17 + selY * 19, 222, 46, 20, 20, 256, 256);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {

        long currentTime = System.currentTimeMillis();
        if (currentTime > lastUpdateTime) {
            lastUpdateTime = currentTime + 1000L;
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 22);
            }
        }

        if (scrollbar.getMax() != menu.getEnd()) scrollbar.setMax(menu.getEnd());
        int serverStart = menu.getStart();
        if (serverStart != lastServerStart) {
            lastServerStart = serverStart;
            lastScrollPos = serverStart;
            scrollbar.setSliderValue(serverStart, false);
        }

        if (selectedSlot >= 0) {
            Slot slot = menu.getSlot(selectedSlot);
            if (!slot.hasItem()) {
                selectedSlot = -1;
                selectedStack = ItemStack.EMPTY;
            } else if (!ItemStack.isSameItemSameComponents(selectedStack, slot.getItem())) {

                selectedSlot = -1;
                for (int i = 0; i < 81; i++) {
                    Slot s = menu.getSlot(i);
                    if (ItemStack.isSameItemSameComponents(selectedStack, s.getItem())) {
                        selectedSlot = i;
                        break;
                    }
                }
                if (selectedSlot < 0) {
                    selectedStack = ItemStack.EMPTY;
                }
            }
        }

        updateButtonVisibility();

        if (selectedSlot >= 0 && !selectedStack.isEmpty()) {

            Slot selected = menu.getSlot(selectedSlot);
            if (selected.hasItem()) {
                if (countbar.getMax() != selected.getItem().getCount()) {
                    countbar.setMax(selected.getItem().getCount());
                }
                int sv = Math.round(countbar.getSliderValue());
                if (sv != lastStackSize) {
                    lastStackSize = sv;
                    stackSize = sv;
                } else if (stacksizeUpdated && sv != stackSize) {
                    countbar.setSliderValue(stackSize, false);
                    stacksizeUpdated = false;
                }
            }
            String sizeText = String.valueOf(stackSize);
            int textWidth = font.width(sizeText);
            graphics.text(font, sizeText, 83 - textWidth / 2, 196, 0xFF333333, false);
        }

        if (!searchField.isFocused() && searchField.getValue().isEmpty()) {
            graphics.text(font, Component.translatable("tc.logistics.search"),
                    146, 197, 0xFF222222, false);
        }
    }

    @Override
    protected void slotClicked(@NotNull Slot slot, int slotId, int mouseButton, @NotNull ContainerInput type) {

        if (slotId >= 0 && slotId < 81 && slot.hasItem()) {
            if (minecraft != null) {
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.CLACK.get(), 0.66f));
            }
            selectedSlot = slotId;
            selectedStack = slot.getItem().copy();
            stackSize = 1;
            lastStackSize = 1;
            stacksizeUpdated = false;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        double delta = scrollY;
        if (delta < 0) {
            scroll(1);
        } else if (delta > 0) {
            scroll(-1);
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        // NEOFORGE_PORT (26.3): key values are SDL scancodes now (GLFW 256 never matched) -> isEscape().
        if (event.isEscape() && searchField.isFocused()) {
            searchField.setFocused(false);
            return super.keyPressed(event);
        }
        if (searchField.isFocused()) {
            return searchField.keyPressed(event);
        }

        if (minecraft != null) {
            for (KeyMapping hotbarKey : minecraft.options.keyHotbarSlots) {
                if (hotbarKey.matches(event)) {
                    return true;
                }
            }
        }
        return super.keyPressed(event);
    }
}