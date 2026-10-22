package thaumictinker.client.enchanting;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import thaumictinker.enchanting.EnchanterBlockEntity;
import thaumictinker.enchanting.EnchanterMenu;
import thaumictinker.enchanting.EnchantmentManager;
import thaumictinker.net.TtNet;

 



 
public class EnchanterScreen extends AbstractContainerScreen<EnchanterMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("thaumictinker", "textures/gui/enchanter.png");
    private static final int MAX_BUTTONS = 16;
    private static final Component START_LABEL =
            Component.translatable("ttinkerer.ttmisc.startEnchant").withStyle(ChatFormatting.AQUA);

    private enum Kind {
        START, PICK, FRAMED, MINUS, PLUS
    }

    private record Hit(Kind kind, int x, int y, int size, @Nullable Enchantment enchantment, int index) {
        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size;
        }
    }

    public EnchanterScreen(EnchanterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 28;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 94;
    }

    private void sheet(GuiGraphics graphics, int x, int y, int width, int height, int u, int v) {
        if (width > 0 && height > 0) {
            graphics.blit(TEXTURE, x, y, width, height, u, v, width, height, 256, 256);
        }
    }

    private void textureChip(GuiGraphics graphics, int x, int y, Enchantment enchantment) {
        EnchantmentManager.Data data = EnchantmentManager.data(enchantment);
        if (data == null) {
            return;
        }
        ResourceLocation texture = new ResourceLocation("thaumictinker", "textures/enchants/" + data.texture() + ".png");
        graphics.blit(texture, x, y, 0.0F, 0.0F, 16, 16, 16, 16);
    }

    private List<Enchantment> available(EnchanterBlockEntity enchanter) {
        List<Enchantment> result = new ArrayList<>();
        ItemStack tool = enchanter.getTool();
        if (!EnchantmentManager.canEnchantStack(tool) || minecraft == null || minecraft.player == null) {
            return result;
        }
        for (Enchantment enchantment : EnchantmentManager.data().keySet()) {
            if (EnchantmentManager.canApply(tool, enchantment, enchanter.getEnchantments())
                    && EnchantmentManager.canEnchantmentBeUsed(minecraft.player, enchantment)) {
                result.add(enchantment);
                if (result.size() >= MAX_BUTTONS) {
                    break;
                }
            }
        }
        return result;
    }

    private List<Hit> layout(EnchanterBlockEntity enchanter, List<Enchantment> available) {
        List<Hit> hits = new ArrayList<>();
        if (!enchanter.getEnchantments().isEmpty()) {
            hits.add(new Hit(Kind.START, leftPos + 151, topPos + 33, 15, null, 0));
        }
        boolean twoRows = available.size() > 8;
        for (int index = 0; index < available.size(); index++) {
            int offset = index <= 7 && twoRows ? -24 : 0;
            hits.add(new Hit(Kind.PICK, leftPos + 34 + (index % 8) * 16, topPos + 54 + offset, 16, available.get(index), index));
        }
        for (int index = 0; index < enchanter.getEnchantments().size(); index++) {
            Enchantment enchantment = enchanter.getEnchantments().get(index);
            int baseX = leftPos + imageWidth + 4;
            int baseY = topPos + index * 26;
            hits.add(new Hit(Kind.FRAMED, baseX, baseY, 16, enchantment, index));
            hits.add(new Hit(Kind.MINUS, leftPos + imageWidth + 24, baseY - 4, 7, enchantment, index));
            hits.add(new Hit(Kind.PLUS, leftPos + imageWidth + 31, baseY - 4, 7, enchantment, index));
        }
        return hits;
    }

    private void drawEnchanterUi(GuiGraphics graphics, EnchanterBlockEntity enchanter) {
        List<Enchantment> available = available(enchanter);
        ItemStack tool = enchanter.getTool();
        boolean enchantable = !tool.isEmpty() && !tool.isEnchanted();
        boolean twoRows = available.size() > 8;
        if (twoRows && enchantable) {
            sheet(graphics, leftPos + 30, topPos + 26, 147, 24, 0, imageHeight);
        }
        if (enchantable) {
            sheet(graphics, leftPos + 30, topPos + 50, 147, 24, 0, imageHeight);
        }
        if (!enchanter.getEnchantments().isEmpty()) {
            int row = twoRows ? 26 : 50;
            int offset = 15;
            for (Aspect aspect : EnchantmentManager.PRIMAL_ASPECTS) {
                drawAspectBar(graphics, aspect, leftPos + 40 + offset, topPos + row);
                offset += 15;
            }
        }
        for (Hit hit : layout(enchanter, available)) {
            switch (hit.kind()) {
                case START -> sheet(graphics, hit.x(), hit.y(), 15, 15, 176, menu.isWorking() ? 39 : 24);
                case PICK -> textureChip(graphics, hit.x(), hit.y(), hit.enchantment());
                case FRAMED -> {
                    sheet(graphics, hit.x() - 4, hit.y() - 4, 24, 24, 176, 0);
                    textureChip(graphics, hit.x(), hit.y(), hit.enchantment());
                    int level = enchanter.getLevels().get(hit.index());
                    graphics.drawString(font, Component.translatable("enchantment.level." + level),
                            hit.x() + 26, hit.y() + 8, 0xFFFFFF, true);
                }
                case MINUS -> sheet(graphics, hit.x(), hit.y(), 7, 7, 218, 0);
                case PLUS -> sheet(graphics, hit.x(), hit.y(), 7, 7, 225, 0);
            }
        }
    }

    private void drawAspectBar(GuiGraphics graphics, Aspect aspect, int x, int y) {
        int total = menu.total(aspect);
        int current = menu.current(aspect);
        if (total == 0) {
            sheet(graphics, x, y - 15, 10, 4, 200, 0);
            sheet(graphics, x, y - 15 + 4, 10, 10, 200, 52);
        } else {
            int pixels = (int) (44.0 * ((double) current / (double) total));
            graphics.fill(x + 1, y - 59 + 4 + 44 - pixels, x + 9, y - 59 + 4 + 44, 0xFF000000 | aspect.getColor());
            sheet(graphics, x, y - 59, 10, 59, 200, 0);
            sheet(graphics, x, y - 59 + 59, 10, 10, 200, 52);
        }
    }

    @Override
    public void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        sheet(graphics, leftPos, topPos, imageWidth, imageHeight, 0, 0);
        EnchanterBlockEntity enchanter = menu.enchanter();
        if (enchanter != null) {
            drawEnchanterUi(graphics, enchanter);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        EnchanterBlockEntity enchanter = menu.enchanter();
        if (enchanter == null) {
            return;
        }
        List<Enchantment> available = available(enchanter);
        for (Hit hit : layout(enchanter, available)) {
            if (!hit.contains(mouseX, mouseY)) {
                continue;
            }
            if (hit.kind() == Kind.START && !menu.isWorking()) {
                graphics.renderTooltip(font, START_LABEL, mouseX, mouseY);
            } else if ((hit.kind() == Kind.PICK || hit.kind() == Kind.FRAMED) && hit.enchantment() != null) {
                List<Component> lines = new ArrayList<>();
                lines.add(Component.translatable(hit.enchantment().getDescriptionId()).withStyle(ChatFormatting.AQUA));
                EnchantmentManager.Data data = EnchantmentManager.data(hit.enchantment());
                AspectList aspects = data != null ? data.aspects(1) : new AspectList();
                for (Aspect aspect : aspects.getAspectsSortedByAmount()) {
                    lines.add(Component.literal(" ")
                            .append(Component.translatable(aspect.getLocalizedDescription())
                                    .withStyle(style -> style.withColor(TextColor.fromRgb(aspect.getColor()))))
                            .append(Component.literal(" x " + aspects.getAmount(aspect) + " "))
                            .append(Component.translatable("ttinkerer.ttmisc.baseCost")));
                }
                if (hit.kind() == Kind.FRAMED && !menu.isWorking()) {
                    lines.add(Component.literal(" ")
                            .append(Component.translatable("ttinkerer.ttmisc.clickToRemove"))
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                }
                graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
            }
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        EnchanterBlockEntity enchanter = menu.enchanter();
        if (button != 0 || enchanter == null) {
            return handled;
        }
        for (Hit hit : layout(enchanter, available(enchanter))) {
            if (!hit.contains(mouseX, mouseY)) {
                continue;
            }
            switch (hit.kind()) {
                case START -> TtNet.sendEnchanterStart(menu.pos());
                case PICK -> TtNet.sendEnchanterEdit(menu.pos(), path(hit.enchantment()), 0);
                case FRAMED -> TtNet.sendEnchanterEdit(menu.pos(), path(hit.enchantment()), -1);
                case MINUS -> {
                    int level = enchanter.getLevels().get(hit.index());
                    TtNet.sendEnchanterEdit(menu.pos(), path(hit.enchantment()), level == 1 ? -1 : level - 1);
                }
                case PLUS -> TtNet.sendEnchanterEdit(menu.pos(), path(hit.enchantment()),
                        enchanter.getLevels().get(hit.index()) + 1);
            }
            return true;
        }
        return handled;
    }

    @Nullable
    private static String path(@Nullable Enchantment enchantment) {
        if (enchantment == null) {
            return "";
        }
        ResourceLocation id = net.minecraftforge.registries.ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
        return id == null ? "" : id.getPath();
    }
}
