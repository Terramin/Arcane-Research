package arcaneresearch.client.gui.screens;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.IGolemProperties;
import arcaneresearch.api.golems.parts.*;
import arcaneresearch.client.gui.widgets.HoverButton;
import arcaneresearch.client.gui.widgets.ScrollButton;
import arcaneresearch.common.golems.GolemProperties;
import arcaneresearch.common.lib.utils.InventoryUtils;
import arcaneresearch.common.menu.GolemBuilderMenu;
import arcaneresearch.common.tiles.crafting.TileGolemBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@OnlyIn(Dist.CLIENT)
public class GolemBuilderScreen extends AbstractContainerScreen<GolemBuilderMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/gui/gui_golembuilder.png");
    private static final ResourceLocation MAT_ICON =
            new ResourceLocation(Arcaneresearch.MODID, "textures/item/golem.png");
    static int headIndex = 0;
    static int matIndex = 0;
    static int armIndex = 0;
    static int legIndex = 0;
    static int addonIndex = 0;
    final ArrayList<GolemHead> valHeads = new ArrayList<>();
    final ArrayList<GolemMaterial> valMats = new ArrayList<>();
    final ArrayList<GolemArm> valArms = new ArrayList<>();
    final ArrayList<GolemLeg> valLegs = new ArrayList<>();
    final ArrayList<GolemAddon> valAddons = new ArrayList<>();
    final List<HoverButton> hoverButtons = new ArrayList<>();
    IGolemProperties props = GolemProperties.fromLong(0L);
    float hearts = 0.0f;
    float armor = 0.0f;
    float damage = 0.0f;
    int cost = 0;
    boolean allfound = false;
    ItemStack[] components = null;
    boolean[] owns = null;
    boolean disableAll = false;
    CraftButton craftButton = null;
    int lastMenuCost = -1;
    int lastMenuMaxCost = -1;
    boolean[] lastHasStuff = null;
    public GolemBuilderScreen(GolemBuilderMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 208;
        this.imageHeight = 224;
        this.inventoryLabelY = this.imageHeight - 94;
    }
    @Override
    protected void init() {
        super.init();
        if (minecraft == null || minecraft.player == null) return;
        valHeads.clear();
        for (GolemHead head : GolemHead.getHeads()) {
            if (head != null && ArcaneresearchCapabilities.knowsResearchStrict(minecraft.player, head.research)) {
                valHeads.add(head);
            }
        }
        valMats.clear();
        for (GolemMaterial mat : GolemMaterial.getMaterials()) {
            if (mat != null && ArcaneresearchCapabilities.knowsResearchStrict(minecraft.player, mat.research)) {
                valMats.add(mat);
            }
        }
        valArms.clear();
        for (GolemArm arm : GolemArm.getArms()) {
            if (arm != null && ArcaneresearchCapabilities.knowsResearchStrict(minecraft.player, arm.research)) {
                valArms.add(arm);
            }
        }
        valLegs.clear();
        for (GolemLeg leg : GolemLeg.getLegs()) {
            if (leg != null && ArcaneresearchCapabilities.knowsResearchStrict(minecraft.player, leg.research)) {
                valLegs.add(leg);
            }
        }
        valAddons.clear();
        for (GolemAddon addon : GolemAddon.getAddons()) {
            if (addon != null && ArcaneresearchCapabilities.knowsResearchStrict(minecraft.player, addon.research)) {
                valAddons.add(addon);
            }
        }
        if (headIndex >= valHeads.size()) headIndex = 0;
        if (matIndex >= valMats.size()) matIndex = 0;
        if (armIndex >= valArms.size()) armIndex = 0;
        if (legIndex >= valLegs.size()) legIndex = 0;
        if (addonIndex >= valAddons.size()) addonIndex = 0;
        gatherInfo();
    }
    private void addScroll(int x, int y, boolean minus, int buttonId) {
        addRenderableWidget(new ScrollButton(x, y, minus, btn -> onScroll(buttonId)));
    }
    private void addHover(int centerX, int centerY, Component name, Component description, Object content) {
        HoverButton hb = new HoverButton(centerX, centerY, 16, 16,
                name != null ? name.getString() : "", description != null ? description.getString() : "", content);
        hoverButtons.add(hb);
        addRenderableWidget(hb);
    }
    private void addHover(int centerX, int centerY, Component name, Component description, int color) {
        HoverButton hb = new HoverButton(centerX, centerY, 16, 16,
                name != null ? name.getString() : "", description != null ? description.getString() : "", GolemBuilderScreen.MAT_ICON, color);
        hoverButtons.add(hb);
        addRenderableWidget(hb);
    }
    private void gatherInfo() {
        clearWidgets();
        hoverButtons.clear();
        int x = leftPos;
        int y = topPos;
        craftButton = new CraftButton(x + 120, y + 104, btn -> onCraft());
        addRenderableWidget(craftButton);
        if (valHeads.size() > 1) {
            addScroll(x + 112 - 5 - 6, y - 5 + 16 + 8, true, 0);
            addScroll(x + 112 - 5 + 22, y - 5 + 16 + 8, false, 1);
        }
        if (valMats.size() > 1) {
            addScroll(x + 16 - 5 - 6, y - 5 + 16 + 8, true, 2);
            addScroll(x + 16 - 5 + 22, y - 5 + 16 + 8, false, 3);
        }
        if (valArms.size() > 1) {
            addScroll(x + 112 - 5 - 6, y - 5 + 40 + 8, true, 4);
            addScroll(x + 112 - 5 + 22, y - 5 + 40 + 8, false, 5);
        }
        if (valLegs.size() > 1) {
            addScroll(x + 112 - 5 - 6, y - 5 + 64 + 8, true, 6);
            addScroll(x + 112 - 5 + 22, y - 5 + 64 + 8, false, 7);
        }
        if (valAddons.size() > 1) {
            addScroll(x + 16 - 5 - 6, y - 5 + 64 + 8, true, 8);
            addScroll(x + 16 - 5 + 22, y - 5 + 64 + 8, false, 9);
        }
        if (!valHeads.isEmpty()) {
            GolemHead head = valHeads.get(headIndex);
            addHover(x + 120, y + 24, head.getLocalizedName(), head.getLocalizedDescription(), head.icon);
        }
        if (!valMats.isEmpty()) {
            GolemMaterial mat = valMats.get(matIndex);
            addHover(x + 24, y + 24, mat.getLocalizedName(), mat.getLocalizedDescription(), mat.itemColor);
        }
        if (!valArms.isEmpty()) {
            GolemArm arm = valArms.get(armIndex);
            addHover(x + 120, y + 48, arm.getLocalizedName(), arm.getLocalizedDescription(), arm.icon);
        }
        if (!valLegs.isEmpty()) {
            GolemLeg leg = valLegs.get(legIndex);
            addHover(x + 120, y + 72, leg.getLocalizedName(), leg.getLocalizedDescription(), leg.icon);
        }
        if (!valAddons.isEmpty() && !valAddons.get(addonIndex).key.equalsIgnoreCase("none")) {
            GolemAddon addon = valAddons.get(addonIndex);
            addHover(x + 24, y + 72, addon.getLocalizedName(), addon.getLocalizedDescription(), addon.icon);
        }
        if (valHeads.isEmpty() || valMats.isEmpty() || valArms.isEmpty() || valLegs.isEmpty() || valAddons.isEmpty()) {
            return;
        }
        props = GolemProperties.fromLong(0L);
        props.setHead(valHeads.get(headIndex));
        props.setMaterial(valMats.get(matIndex));
        props.setArms(valArms.get(armIndex));
        props.setLegs(valLegs.get(legIndex));
        props.setAddon(valAddons.get(addonIndex));
        TileGolemBuilder tile = menu.getBlockEntity();
        if (tile != null) {
            CompoundTag nbt = new CompoundTag();
            nbt.putBoolean("check", true);
            nbt.putLong("golem", props.toLong());
            tile.sendMessageToServer(nbt);
        }
        redoComps();
        EnumGolemTrait[] tags = props.getTraits().toArray(new EnumGolemTrait[0]);
        if (tags.length > 0) {
            int yy = (tags.length <= 4) ? ((tags.length - 1) % 4 * 8) : 24;
            int xx = (tags.length - 1) / 4 % 4 * 8;
            int i = 0;
            int q = 0;
            for (EnumGolemTrait tag : tags) {
                addHover(x + 72 + q * 16 - xx, y + 48 + 16 * i - yy,
                        tag.getLocalizedName(), tag.getLocalizedDescription(), tag.icon);
                i++;
                if (i > 3) {
                    i = 0;
                    q++;
                }
            }
        }
        int hh = 10 + props.getMaterial().healthMod;
        if (props.hasTrait(EnumGolemTrait.FRAGILE)) {
            hh = (int) (hh * 0.75D);
        }
        hearts = hh / 2.0f;
        int aa = props.getMaterial().armor;
        if (props.hasTrait(EnumGolemTrait.ARMORED)) {
            aa = (int) Math.max(aa * 1.5D, aa + 1);
        }
        if (props.hasTrait(EnumGolemTrait.FRAGILE)) {
            aa = (int) (aa * 0.75D);
        }
        armor = aa / 2.0f;
        double dd = props.hasTrait(EnumGolemTrait.FIGHTER) ? props.getMaterial().damage : 0.0D;
        if (props.hasTrait(EnumGolemTrait.BRUTAL)) {
            dd = Math.max(dd * 1.5D, dd + 1.0D);
        }
        damage = (float) (dd / 2.0D);
    }
    private void redoComps() {
        if (minecraft == null || minecraft.player == null) return;
        allfound = true;
        cost = props.getTraits().size() * 2;
        components = props.generateComponents();
        TileGolemBuilder watched = menu.getBlockEntity();
        boolean[] stocked = watched != null ? watched.hasStuff : null;
        boolean creative = minecraft.player.isCreative();
        if (components.length >= 1) {
            owns = new boolean[components.length];
            for (int a = 0; a < components.length; a++) {
                cost += components[a].getCount();
                owns[a] = creative;
                if (!creative && stocked != null && stocked.length > a) {
                    owns[a] = stocked[a];
                }
                if (!owns[a]) {
                    owns[a] = creative
                            || InventoryUtils.isPlayerCarryingAmount(minecraft.player, components[a], true);
                }
                if (!owns[a]) {
                    allfound = false;
                }
            }
        }
        if (creative) {
            allfound = true;
        }
        if (components.length > 0) {
            int x = leftPos;
            int y = topPos;
            addHover(x + 152, y + 24, Component.literal(Aspect.MECHANISM.getName()),
                    Component.literal(Aspect.MECHANISM.getLocalizedDescription()), Aspect.MECHANISM);
            int i = 1;
            int q = 0;
            for (ItemStack stack : components) {
                addHover(x + 152 + q * 16, y + 24 + 16 * i,
                        stack.getHoverName(), null, stack.copy());
                i++;
                if (i > 3) {
                    i = 0;
                    q++;
                }
            }
        }
        for (var widget : this.renderables) {
            if (widget instanceof AbstractWidget aw) {
                aw.active = !disableAll;
            }
        }
        if (craftButton != null) {
            craftButton.active = !disableAll && allfound;
        }
    }
    private void onScroll(int buttonId) {
        if (disableAll) return;
        switch (buttonId) {
            case 0 -> {
                headIndex--;
                if (headIndex < 0) headIndex = valHeads.size() - 1;
            }
            case 1 -> {
                headIndex++;
                if (headIndex >= valHeads.size()) headIndex = 0;
            }
            case 2 -> {
                matIndex--;
                if (matIndex < 0) matIndex = valMats.size() - 1;
            }
            case 3 -> {
                matIndex++;
                if (matIndex >= valMats.size()) matIndex = 0;
            }
            case 4 -> {
                armIndex--;
                if (armIndex < 0) armIndex = valArms.size() - 1;
            }
            case 5 -> {
                armIndex++;
                if (armIndex >= valArms.size()) armIndex = 0;
            }
            case 6 -> {
                legIndex--;
                if (legIndex < 0) legIndex = valLegs.size() - 1;
            }
            case 7 -> {
                legIndex++;
                if (legIndex >= valLegs.size()) legIndex = 0;
            }
            case 8 -> {
                addonIndex--;
                if (addonIndex < 0) addonIndex = valAddons.size() - 1;
            }
            case 9 -> {
                addonIndex++;
                if (addonIndex >= valAddons.size()) addonIndex = 0;
            }
            default -> {
                return;
            }
        }
        gatherInfo();
    }
    private void onCraft() {
        if (!allfound || disableAll || minecraft == null) return;
        TileGolemBuilder tile = menu.getBlockEntity();
        if (tile != null) {
            CompoundTag nbt = new CompoundTag();
            nbt.putLong("golem", props.toLong());
            tile.sendMessageToServer(nbt);
        }
        if (minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 99);
        }
        disableAll = true;
    }
    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        for (HoverButton hb : hoverButtons) {
            hb.renderTooltip(graphics, mouseX, mouseY);
        }
        this.renderTooltip(graphics, mouseX, mouseY);
    }
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        if (components != null && components.length > 0) {
            int i = 1;
            int q = 0;
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.5f);
            for (int a = 0; a < components.length; a++) {
                if (!owns[a]) {
                    graphics.blit(TEXTURE, x + 144 + q * 16, y + 16 + 16 * i, 240, 0, 16, 16);
                }
                i++;
                if (i > 3) {
                    i = 0;
                    q++;
                }
            }
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
        int menuCost = menu.getCost();
        int menuMax = menu.getMaxCost();
        if (menuCost > 0 && menuMax > 0) {
            graphics.blit(TEXTURE, x + 145, y + 89, 209, 89,
                    (int) (46.0f * (1.0f - (float) menuCost / (float) menuMax)), 6);
            if (!disableAll) {
                disableAll = true;
                redoComps();
            }
        } else if (disableAll) {
            disableAll = false;
            redoComps();
        }
        String heartsStr = "" + hearts;
        String armorStr = "" + armor;
        String damageStr = "" + damage;
        graphics.drawString(this.font, heartsStr, x + 48 - this.font.width(heartsStr) / 2, y + 108, 0xFFFFFF, false);
        graphics.drawString(this.font, armorStr, x + 72 - this.font.width(armorStr) / 2, y + 108, 0xFFFFFF, false);
        graphics.drawString(this.font, damageStr, x + 97 - this.font.width(damageStr) / 2, y + 108, 0xFFFFFF, false);
    }
    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        if (components != null && components.length > 0) {
            String s = "" + cost;
            graphics.drawString(this.font, s, 162 - this.font.width(s), 24, 0xFFFFFF, false);
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(TEXTURE, 12, 12, 228, 124, 24, 24);
        graphics.blit(TEXTURE, 12, 60, 228, 124, 24, 24);
        graphics.blit(TEXTURE, 108, 12, 228, 124, 24, 24);
        graphics.blit(TEXTURE, 108, 36, 228, 124, 24, 24);
        graphics.blit(TEXTURE, 108, 60, 228, 124, 24, 24);
        int menuCost = menu.getCost();
        int menuMax = menu.getMaxCost();
        TileGolemBuilder watched = menu.getBlockEntity();
        boolean[] stuffNow = watched != null ? watched.hasStuff : null;
        if (menuCost != lastMenuCost || menuMax != lastMenuMaxCost || !Arrays.equals(stuffNow, lastHasStuff)) {
            lastMenuCost = menuCost;
            lastMenuMaxCost = menuMax;
            lastHasStuff = stuffNow == null ? null : stuffNow.clone();
            redoComps();
        }
    }
    static class CraftButton extends Button {
        CraftButton(int x, int y, OnPress onPress) {
            super(x, y, 24, 16, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        }
        @Override
        protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(0.9f, 0.9f, 0.9f, 0.9f);
            if (this.active && this.isHovered()) {
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
            graphics.blit(TEXTURE, this.getX(), this.getY(), 216, 64, 24, 16);
            if (!this.active) {
                graphics.blit(TEXTURE, this.getX(), this.getY(), 216, 40, 24, 16);
            }
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }
}
