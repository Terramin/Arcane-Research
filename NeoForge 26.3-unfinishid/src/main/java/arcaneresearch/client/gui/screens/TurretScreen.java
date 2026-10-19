package arcaneresearch.client.gui.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor; // NEOFORGE_PORT: was GuiGraphics
import net.minecraft.client.renderer.RenderPipelines; // NEOFORGE_PORT: blit needs a pipeline
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was ResourceLocation
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.gui.widgets.ToggleButton;
import arcaneresearch.common.entities.construct.EntityTurretCrossbowAdvanced;
import arcaneresearch.common.menu.TurretMenu;

public class TurretScreen extends AbstractContainerScreen<TurretMenu> {

    private static final Identifier TEXTURE_BASIC =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_turret_basic.png");
    private static final Identifier TEXTURE_ADVANCED =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_turret_advanced.png");

    private final boolean isAdvanced;

    public TurretScreen(TurretMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 175, 232);
        this.isAdvanced = menu.isAdvanced();
    }

    @Override
    protected void init() {
        super.init();

        if (isAdvanced && menu.getTurret() instanceof EntityTurretCrossbowAdvanced advanced) {
            final ToggleButton[] holders = new ToggleButton[4];
            holders[0] = new ToggleButton(
                    this.leftPos + 90, this.topPos + 13, "button.turretfocus.1",
                    () -> holders[0].setToggled(advanced.getTargetAnimal()),
                    btn -> sendButtonClick(1));
            addRenderableWidget(holders[0]);
            holders[1] = new ToggleButton(
                    this.leftPos + 90, this.topPos + 27, "button.turretfocus.2",
                    () -> holders[1].setToggled(advanced.getTargetMob()),
                    btn -> sendButtonClick(2));
            addRenderableWidget(holders[1]);
            holders[2] = new ToggleButton(
                    this.leftPos + 90, this.topPos + 41, "button.turretfocus.3",
                    () -> holders[2].setToggled(advanced.getTargetPlayer()),
                    btn -> sendButtonClick(3));
            addRenderableWidget(holders[2]);
            holders[3] = new ToggleButton(
                    this.leftPos + 90, this.topPos + 55, "button.turretfocus.4",
                    () -> holders[3].setToggled(advanced.getTargetFriendly()),
                    btn -> sendButtonClick(4));
            addRenderableWidget(holders[3]);
        }
    }

    private void sendButtonClick(int buttonId) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        // 1.20.1 Screen.renderBackground dim (fillGradient)
        graphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        int x = this.leftPos;
        int y = this.topPos;

        Identifier texture = isAdvanced ? TEXTURE_ADVANCED : TEXTURE_BASIC;
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        float maxHp = menu.getTurret().getMaxHealth();
        int h = (int) (39.0f * (maxHp > 0 ? menu.getTurret().getHealth() / maxHp : 0));
        if (h > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x + (isAdvanced ? 30 : 68), y + 59, 192, 48, h, 6, 256, 256);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {

    }
}