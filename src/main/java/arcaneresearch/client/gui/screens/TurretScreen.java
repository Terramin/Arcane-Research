package arcaneresearch.client.gui.screens;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.gui.widgets.ToggleButton;
import arcaneresearch.common.entities.construct.EntityTurretCrossbowAdvanced;
import arcaneresearch.common.menu.TurretMenu;
@OnlyIn(Dist.CLIENT)
public class TurretScreen extends AbstractContainerScreen<TurretMenu> {
    private static final ResourceLocation TEXTURE_BASIC =
            new ResourceLocation(Arcaneresearch.MODID, "textures/gui/gui_turret_basic.png");
    private static final ResourceLocation TEXTURE_ADVANCED =
            new ResourceLocation(Arcaneresearch.MODID, "textures/gui/gui_turret_advanced.png");
    private final boolean isAdvanced;
    public TurretScreen(TurretMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 175;
        this.imageHeight = 232;
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
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        int x = this.leftPos;
        int y = this.topPos;
        ResourceLocation texture = isAdvanced ? TEXTURE_ADVANCED : TEXTURE_BASIC;
        graphics.blit(texture, x, y, 0, 0, this.imageWidth, this.imageHeight);
        float maxHp = menu.getTurret().getMaxHealth();
        int h = (int) (39.0f * (maxHp > 0 ? menu.getTurret().getHealth() / maxHp : 0));
        if (h > 0) {
            graphics.blit(texture, x + (isAdvanced ? 30 : 68), y + 59, 192, 48, h, 6);
        }
    }
    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
    }
}
