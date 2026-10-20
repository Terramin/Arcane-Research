package arcaneresearch.client.gui.screens;



import net.minecraft.ChatFormatting;

import net.minecraft.client.gui.GuiGraphicsExtractor; // NEOFORGE_PORT: was GuiGraphics

import net.minecraft.client.renderer.RenderPipelines; // NEOFORGE_PORT: blit needs a pipeline

import net.minecraft.client.gui.components.EditBox;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import net.minecraft.client.input.KeyEvent;

import net.minecraft.client.input.MouseButtonEvent;

import net.minecraft.network.chat.Component;

import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was ResourceLocation

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.Arcaneresearch;

import arcaneresearch.api.ArcaneresearchApiHelper;

import arcaneresearch.api.aspects.Aspect;

import arcaneresearch.api.aspects.AspectList;

import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;

import arcaneresearch.api.casters.*;

import arcaneresearch.client.gui.widgets.HoverButton;

import arcaneresearch.client.gui.widgets.ImageButton;

import arcaneresearch.client.gui.widgets.SliderWidget;

import arcaneresearch.client.gui.widgets.SpinnerWidget;

import arcaneresearch.common.items.casters.ItemFocus;

import arcaneresearch.common.lib.network.PacketHandler;

import arcaneresearch.common.lib.network.playerdata.PacketFocusNameToServer;

import arcaneresearch.common.lib.network.playerdata.PacketFocusNodesToServer;

import arcaneresearch.common.menu.FocalManipulatorMenu;

import arcaneresearch.common.tiles.crafting.FocusElementNode;

import arcaneresearch.common.tiles.crafting.TileFocalManipulator;



import java.awt.*;

import java.text.DecimalFormat;

import java.util.*;

import java.util.List;



public class FocalManipulatorScreen extends AbstractContainerScreen<FocalManipulatorMenu> {



    private static final Identifier TEX_MAIN =

            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_wandtable.png");

    private static final Identifier TEX_BG =

            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_wandtable2.png");

    private static final Identifier TEX_SIDE =

            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_wandtable3.png");

    private static final Identifier TEX_BASE =

            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_base.png");

    private static final Identifier TEX_MEDIUM =

            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/foci/_medium.png");

    private static final Identifier TEX_EFFECT =

            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/foci/_effect.png");

    private final List<SpinnerWidget> settingSpinners = new ArrayList<>();

    private final List<HoverButton> crystalButtons = new ArrayList<>();

    private final List<String> shownParts = new ArrayList<>();

    private final DecimalFormat formatter = new DecimalFormat("#######.##");

    private EditBox nameField;

    private ImageButton confirmButton;

    private SliderWidget partsScrollbar;

    private SliderWidget mainScrollbarX;

    private SliderWidget mainScrollbarY;

    private HoverButton infoComplexityButton;

    private HoverButton infoXpButton;

    private HoverButton infoVisButton;

    private int partsStart = 0;

    private int selectedNode = -1;

    private int lastNodeHover = -1;

    private int nodeID = 0;

    private int scrollX = 0;

    private int scrollY = 0;

    private int sMinX;

    private int sMaxX;

    private int sMaxY;

    private boolean isDragging = false;

    private int lastMouseX, lastMouseY;

    private boolean suppressNameSync = false;

    private int totalComplexity = 0;

    private int maxComplexity = 0;

    private float costCast = 0;

    private int costXp = 0;

    private int costVis = 0;

    private ItemStack[] components = null;

    private boolean valid = false;



    public FocalManipulatorScreen(FocalManipulatorMenu menu, Inventory playerInventory, Component title) {

        super(menu, playerInventory, title, 231, 231);

    }



    public static void drawPart(GuiGraphicsExtractor graphics, FocusNode node, int x, int y, float scale, boolean hover) {

        graphics.pose().pushMatrix();

        graphics.pose().translate(x, y);
        // Без поворота: иконки рисуются прямо, как лежат в PNG. Поворот
        // оригинала TC6 работал только в связке с его зеркальными UV;
        // при стандартных UV любой поворот на 90° кладёт иконки боком.


        boolean isRoot = node.getType() == IFocusElement.EnumUnitType.MOD || node.getKey().equals("arcaneresearch.ROOT");

        if (isRoot) {

            scale *= 2.0f;

        }



        Color color = new Color(FocusEngine.getElementColor(node.getKey()));

        float r = color.getRed() / 255.0f;

        float g = color.getGreen() / 255.0f;

        float b = color.getBlue() / 255.0f;

        // NEOFORGE_PORT: Forge setShaderColor(r, g, b) on the EFFECT/MEDIUM

        // backing -> ARGB tint (icon below stays untinted, submitted after).

        int backingTint = 0xFF000000 | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();



        float actualScale = (scale * 0.9f + (hover ? 2 : 0)) / 32.0f;





        if (node.getType() == IFocusElement.EnumUnitType.EFFECT) {
            graphics.pose().pushMatrix();
            graphics.pose().scale(actualScale, actualScale);

            graphics.blit(RenderPipelines.GUI_TEXTURED, TEX_EFFECT, -16, -16, 0, 0, 32, 32, 32, 32, backingTint);

            graphics.pose().popMatrix();

        } else if (node.getType() == IFocusElement.EnumUnitType.MEDIUM && !isRoot) {
            graphics.pose().pushMatrix();
            graphics.pose().scale(actualScale, actualScale);

            graphics.blit(RenderPipelines.GUI_TEXTURED, TEX_MEDIUM, -16, -16, 0, 0, 32, 32, 32, 32, backingTint);

            graphics.pose().popMatrix();

        }



        float iconScale = (scale / 2.0f + (hover ? 2 : 0)) / 32.0f;

        graphics.pose().pushMatrix();

        graphics.pose().translate(0, 0);

        graphics.pose().scale(iconScale, iconScale);

        Identifier icon = FocusEngine.getElementIcon(node.getKey());

        graphics.blit(RenderPipelines.GUI_TEXTURED, icon, -16, -16, 0, 0, 32, 32, 32, 32);

        graphics.pose().popMatrix();



        graphics.pose().popMatrix();

    }



    private TileFocalManipulator getTile() {

        return menu.getBlockEntity();

    }



    @Override

    protected void init() {

        super.init();



        nameField = new EditBox(font, leftPos + 30, topPos + 11, 170, 12, Component.empty());

        nameField.setBordered(false);

        nameField.setMaxLength(50);

        nameField.setTextColor(0xFFFFFFFF);

        nameField.setValue(getTile().focusName);

        nameField.setResponder(this::onNameChanged);

        addWidget(nameField);



        confirmButton = new ImageButton(

                leftPos + 242, topPos + 18, 24, 16,

                null, Component.translatable("wandtable.text3").getString(),

                TEX_BASE, 232, 240, 24, 16,

                btn -> onConfirmClicked()

        );

        addRenderableWidget(confirmButton);



        infoComplexityButton = new HoverButton(leftPos + 241, topPos + 39, 32, 16,

                Component.translatable("wandtable.text5").getString(), null,

                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/complex.png"));

        infoXpButton = new HoverButton(leftPos + 241, topPos + 53, 32, 16,

                Component.translatable("wandtable.text2").getString(), null,

                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/costxp.png"));

        infoVisButton = new HoverButton(leftPos + 241, topPos + 67, 32, 16,

                Component.translatable("wandtable.text1").getString(), null,

                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/costvis.png"));

        addRenderableWidget(infoComplexityButton);

        addRenderableWidget(infoXpButton);

        addRenderableWidget(infoVisButton);



        gatherInfo(false);

    }



    private void onNameChanged(String newName) {



        if (suppressNameSync) {

            return;

        }

        getTile().focusName = newName;

        PacketHandler.sendToServer(new PacketFocusNameToServer(getTile().getBlockPos(), newName));

    }



    private void setNameFieldValue(String value) {
        if (nameField == null) return;
        suppressNameSync = true;
        nameField.setValue(value);

        suppressNameSync = false;

    }



    private void onConfirmClicked() {

        if (confirmButton.isButtonActive() && valid) {

            assert minecraft != null;

            assert minecraft.gameMode != null;

            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);

        }

    }



    // NEOFORGE_PORT: per-frame customs moved into extractBackground (render() removed;

    // base extractRenderState handles background/super/tooltip). Tooltips are deferred calls.

    private void renderCustom(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {

        drawPartsList(graphics, mouseX, mouseY);



        if (!getTile().data.isEmpty() && nameField != null) {

            nameField.extractRenderState(graphics, mouseX, mouseY, partialTick);

        }



        drawPartsTooltips(graphics, mouseX, mouseY);

        drawNodeTooltips(graphics, mouseX, mouseY);

        if (infoComplexityButton != null) infoComplexityButton.renderTooltip(graphics, mouseX, mouseY);

        if (infoXpButton != null) infoXpButton.renderTooltip(graphics, mouseX, mouseY);

        if (infoVisButton != null) infoVisButton.renderTooltip(graphics, mouseX, mouseY);



        if (confirmButton != null && confirmButton.isMouseOver(mouseX, mouseY)

                && confirmButton.getDescription() != null && !confirmButton.getDescription().isEmpty()) {

            graphics.setComponentTooltipForNextFrame(font,

                    List.of(Component.literal(confirmButton.getDescription())

                            .withStyle(style -> style.withItalic(true).withColor(0x5555FF))),

                    mouseX, mouseY);

        }

        for (HoverButton button : crystalButtons) {

            button.renderTooltip(graphics, mouseX, mouseY);

        }



        assert confirmButton != null;

        confirmButton.setButtonActive(getTile().vis <= 0.0f && valid);

    }



    @Override

    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        // NEOFORGE_PORT (26.3): no extra dim - super.extractBackground already draws the in-game gradient (was drawn twice).
        int x = leftPos;
        int y = topPos;

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEX_BG, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEX_SIDE, x - 71, y - 3, 0, 0, 71, 239, 256, 256);



        TileFocalManipulator tile = getTile();

        if (tile.getItem(0).isEmpty() || tile.doGuiReset) {

            if (tile.doGuiReset) {

                resetNodes();

            } else {

                tile.data.clear();

                gatherInfo(false);

            }

            tile.focusName = "";

            if (!tile.getItem(0).isEmpty()) {

                tile.focusName = tile.getItem(0).getHoverName().getString();

                setNameFieldValue(tile.focusName);

            }

            tile.doGuiReset = false;

        }



        if (tile.doGather) {

            gatherInfo(false);

            tile.doGather = false;

        }



        drawNodes(graphics, leftPos + 132 - scrollX, topPos + 48 - scrollY, mouseX, mouseY);



        graphics.blit(RenderPipelines.GUI_TEXTURED, TEX_MAIN, x, y, 0, 0, imageWidth, imageHeight, 256, 256);



        if (maxComplexity > 0) {

            int complexColor = totalComplexity > maxComplexity ? 0xFFF67278 : 0xFFFFC23F;

            graphics.text(font, totalComplexity + "/" + maxComplexity, x + 242, y + 36, complexColor, true);

        }



        assert minecraft != null;

        assert minecraft.player != null;

        int xpColor = costXp > minecraft.player.experienceLevel ? 0xFFF67278 : 0xFF99FF8D;

        graphics.text(font, String.valueOf(costXp), x + 242, y + 50, xpColor, true);



        int visCost = getTile().vis > 0 ? (int) getTile().vis : costVis;

        graphics.text(font, ChatFormatting.AQUA + String.valueOf(visCost), x + 242, y + 64, 0xFF99FF8D, true);



        if (costCast > 0) {

            String cost = formatter.format(costCast);

            graphics.text(font, ChatFormatting.AQUA + Component.translatable("item.Focus.cost1").getString() + ": " + cost,

                    x + 230, y + 80, 0xFF99FF8D, true);

        }



        if (components != null && components.length > 0) {

            graphics.text(font, ChatFormatting.GOLD + Component.translatable("wandtable.text4").getString(),

                    x + 230, y + 92, 0xFF99FF8D, true);

        }



        if (!tile.data.isEmpty() && tile.focusName != null) {

            graphics.blit(RenderPipelines.GUI_TEXTURED, TEX_BASE, leftPos + 24, topPos + 8, 192, 224, 8, 14, 256, 256);

            for (int i = 1; i < 22; i++) {

                graphics.blit(RenderPipelines.GUI_TEXTURED, TEX_BASE, leftPos + 24 + i * 8, topPos + 8, 200, 224, 8, 14, 256, 256);

            }

            graphics.blit(RenderPipelines.GUI_TEXTURED, TEX_BASE, leftPos + 24 + 22 * 8, topPos + 8, 208, 224, 8, 14, 256, 256);

        }



        renderCustom(graphics, mouseX, mouseY, partialTick);

    }



    @Override

    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {



    }



    private void drawPartsList(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {

        int gx = leftPos;

        int gy = topPos;



        int count = 0;

        int index = 0;



        for (String key : shownParts) {

            if (++count - 1 < partsStart) continue;



            FocusNode node = (FocusNode) FocusEngine.getElement(key);

            if (node != null) {

                float scale = node.getType() == IFocusElement.EnumUnitType.MOD ? 24.0f : 32.0f;

                boolean hover = isInRegion(gx + 28, gy + 32 + 24 * index, 20, 20, mouseX, mouseY);

                drawPart(graphics, node, gx + 38, 43 + gy + 25 * index, scale, hover);

            }



            if (++index > 5) break;

        }

    }



    private void drawPartsTooltips(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {

        int gx = leftPos;

        int gy = topPos;



        int count = 0;

        int index = 0;



        for (String key : shownParts) {

            if (++count - 1 < partsStart) continue;



            FocusNode node = (FocusNode) FocusEngine.getElement(key);



            if (node != null && isInRegion(gx + 28, gy + 33 + 25 * index, 20, 20, mouseX, mouseY)) {

                List<Component> tooltip = generatePartTooltip(node, -1);



                graphics.setTooltipForNextFrame(font, tooltip, java.util.Optional.empty(), gx + 44, gy + 46 + 24 * index);

            }



            if (++index > 5) break;

        }

    }



    private List<Component> generatePartTooltip(FocusNode node, int idx) {

        List<Component> list = new ArrayList<>();

        if (node == null) return list;



        FocusElementNode placed = idx >= 0 ? getTile().data.get(idx) : null;



        list.add(Component.translatable(node.getUnlocalizedName()));



        list.add(Component.translatable(node.getUnlocalizedText()).withStyle(ChatFormatting.DARK_PURPLE));



        int complexity = node.getComplexity();

        if (placed != null) {

            complexity = (int) (node.getComplexity() * placed.complexityMultiplier);

        }

        ChatFormatting complexFormat = placed != null && placed.complexityMultiplier > 1.0f ? ChatFormatting.RED : ChatFormatting.GOLD;

        list.add(Component.translatable("focuspart.com").withStyle(ChatFormatting.GOLD)

                .append(Component.literal(" " + complexity).withStyle(complexFormat)));



        float power = node.getPowerMultiplier();

        if (placed != null) {

            power = placed.getPower(getTile().data);

        }

        if (power != 1.0f) {
            ChatFormatting powerFormat = power < 1.0f ? ChatFormatting.RED : ChatFormatting.GREEN;
            list.add(Component.translatable("focuspart.eff").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(" x" + net.minecraft.world.item.component.ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(power)).withStyle(powerFormat)));
        }

        if (node instanceof FocusEffect effect) {
            float damage = effect.getDamageForDisplay(placed == null ? 1.0f : placed.getPower(getTile().data));
            if (damage > 0) {
                list.add(Component.literal(ChatFormatting.DARK_RED + ""
                        + Component.translatable("attribute.modifier.equals.0",
                        net.minecraft.world.item.component.ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(damage),
                        Component.translatable("attribute.name.generic.attack_damage")).getString()));
            } else if (damage < 0) {
                list.add(Component.literal(ChatFormatting.DARK_GREEN + ""
                        + Component.translatable("attribute.modifier.equals.0",
                        net.minecraft.world.item.component.ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(-damage),
                        Component.translatable("focus.heal.power")).getString()));
            }
        }


        return list;

    }



    private void drawNodes(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY) {

        TileFocalManipulator tile = getTile();

        if (tile.data == null || tile.data.isEmpty()) return;



        int hover = -1;



        for (FocusElementNode fn : tile.data.values()) {

            int xx = x + fn.x * 24;

            int yy = y + fn.y * 32;



            boolean isInMainArea = isInRegion(leftPos + 63, topPos + 31, 136, 160, mouseX, mouseY);

            boolean isOverNode = isInRegion(xx - 10, yy - 10, 20, 20, mouseX, mouseY);

            boolean mouseover = isInMainArea && isOverNode;



            if (mouseover && fn.parent >= 0) {

                hover = fn.id;

            }



            if (fn.node != null) {

                if (isInRegion(leftPos + 48, topPos + 16, 154, 192, xx - 8, yy - 8)) {

                    drawPart(graphics, fn.node, xx, yy, 32.0f, mouseover);

                }

            } else {



                if (isInRegion(leftPos + 48, topPos + 26, 166, 174, xx, yy)) {

                    graphics.blit(RenderPipelines.GUI_TEXTURED, TEX_MAIN, xx - 12, yy - 12, 120, 232, 24, 24, 256, 256);

                }

            }



            if (selectedNode == fn.id || (mouseover && fn.parent >= 0)) {

                if (isInRegion(leftPos + 48, topPos + 26, 166, 174, xx, yy)) {

                    graphics.blit(RenderPipelines.GUI_TEXTURED, TEX_MAIN, xx - 12, yy - 12, 96, 232, 24, 24, 256, 256);

                }

            }



            FocusElementNode parent = tile.data.get(fn.parent);

            if (parent != null) {

                drawClipped(graphics, xx - 6, yy - 22, 54, 232, 12, 12);



                if (parent.node instanceof FocusModSplit) {

                    int dist = Math.abs(fn.x - parent.x);

                    for (int i = 0; i < dist; i++) {

                        if (fn.x < parent.x) {

                            if (i == 0) {

                                drawClipped(graphics, xx - 4, yy - 36, 8, 240, 16, 16);

                            } else {

                                drawClipped(graphics, xx - 12 + i * 24, yy - 36, 72, 240, 24, 16);

                            }

                        } else {

                            if (i == 0) {

                                drawClipped(graphics, xx - 12, yy - 36, 24, 240, 16, 16);

                            } else {

                                drawClipped(graphics, xx - 12 - i * 24, yy - 36, 72, 240, 24, 16);

                            }

                        }

                    }

                }



                if (fn.node == null && isInRegion(leftPos + 48, topPos + 16, 168, 192, xx - 4, yy - 4)) {

                    int offset = (parent.target && parent.trajectory) ? 4 : 0;



                    graphics.pose().pushMatrix();

                    if (parent.target) {

                        graphics.pose().translate(xx - offset, yy);

                        graphics.pose().scale(0.5f, 0.5f);

                        graphics.blit(RenderPipelines.GUI_TEXTURED, TEX_MAIN, -8, -8, 152, 240, 16, 16, 256, 256);

                        graphics.pose().popMatrix();

                        graphics.pose().pushMatrix();

                    }

                    if (parent.trajectory) {

                        graphics.pose().translate(xx + offset, yy);

                        graphics.pose().scale(0.5f, 0.5f);

                        graphics.blit(RenderPipelines.GUI_TEXTURED, TEX_MAIN, -8, -8, 168, 240, 16, 16, 256, 256);

                    }

                    graphics.pose().popMatrix();

                }

            }

        }



        if (hover >= 0 && lastNodeHover != hover) {

            playRollover();

            lastNodeHover = hover;

        }

        if (hover < 0) {

            lastNodeHover = -1;

        }



        if (selectedNode >= 0) {

            drawNodeSettings(graphics, selectedNode);

        }

    }



    private void drawClipped(GuiGraphicsExtractor graphics, int x, int y, int sx, int sy, int w, int h) {

        if (isInRegion(leftPos + 48, topPos + 26, 166, 174, x + w / 2, y + h / 2)) {

            graphics.blit(RenderPipelines.GUI_TEXTURED, TEX_MAIN, x, y, sx, sy, w, h, 256, 256);

        }

    }



    private void drawNodeTooltips(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {

        if (lastNodeHover >= 0) {

            FocusElementNode fn = getTile().data.get(lastNodeHover);

            if (fn != null && fn.node != null) {

                List<Component> tooltip = generatePartTooltip(fn.node, lastNodeHover);



                graphics.setTooltipForNextFrame(font, tooltip, java.util.Optional.empty(),

                        leftPos + 140 - scrollX + fn.x * 24,

                        topPos + 50 - scrollY + fn.y * 32);

            }

        }

    }



    private void drawNodeSettings(GuiGraphicsExtractor graphics, int nodeId) {

        FocusElementNode fn = getTile().data.get(nodeId);

        if (fn == null || fn.node == null || fn.node.getSettingList().isEmpty()) return;



        int idx = 0;

        for (String key : fn.node.getSettingList()) {

            NodeSetting setting = fn.node.getSetting(key);

            if (setting.getResearch() != null) {

                assert Objects.requireNonNull(minecraft).player != null;

                if (!ArcaneresearchCapabilities.knowsResearchStrict(minecraft.player, setting.getResearch())) {

                    continue;

                }

            }



            int settingX = leftPos + imageWidth;

            int settingY = topPos + imageHeight - 10 - fn.node.getSettingList().size() * 26 + idx * 26;



            graphics.text(font, ChatFormatting.GOLD + setting.getLocalizedName(), settingX, settingY, 0xFFFFFFFF, true);

            idx++;

        }

    }



    private void resetNodes() {

        nodeID = 0;

        getTile().data.clear();

        addNodeAt(FocusMediumRoot.class, 0, false);

        selectedNode = 1;

        calcNodeTreeLayout();

        gatherInfo(true);

    }



    private List<Integer> addNodeAt(Class<? extends FocusNode> nodeClass, int idx, boolean gather) {

        List<Integer> ret = new ArrayList<>();

        TileFocalManipulator tile = getTile();



        boolean same = false;

        FocusElementNode previous = null;



        if (tile.data.containsKey(idx)) {

            cullChildren(idx);

            if (tile.data.get(idx).node != null && tile.data.get(idx).node.getClass() == nodeClass) {

                same = true;

            } else {

                previous = tile.data.remove(idx);

            }

        }



        try {

            FocusElementNode fn;

            FocusNode node;



            if (!same) {

                fn = new FocusElementNode();

                node = nodeClass.getDeclaredConstructor().newInstance();

                fn.node = node;



                if (previous != null) {

                    fn.y = previous.y;

                    fn.x = previous.x;

                }



                fn.id = getNextId();

                ret.add(fn.id);

                selectedNode = fn.id;



                if (previous != null && tile.data.containsKey(previous.parent)) {

                    fn.parent = previous.parent;

                    int[] c = tile.data.get(previous.parent).children;

                    for (int i = 0; i < c.length; i++) {

                        if (c[i] == previous.id) {

                            tile.data.get(previous.parent).children[i] = fn.id;

                            break;

                        }

                    }

                }



                fn.target = node.canSupply(FocusNode.EnumSupplyType.TARGET);

                fn.trajectory = node.canSupply(FocusNode.EnumSupplyType.TRAJECTORY);

                tile.data.put(nodeID, fn);

            } else {

                fn = tile.data.get(idx);

                node = fn.node;

            }



            if (fn.target || fn.trajectory) {

                if (node instanceof FocusModSplit) {



                    FocusElementNode blank1 = new FocusElementNode();

                    blank1.parent = fn.id;

                    blank1.id = getNextId();

                    ret.add(nodeID);

                    blank1.x = fn.x - 1;

                    blank1.y = fn.y + 1;

                    tile.data.put(nodeID, blank1);

                    selectedNode = nodeID;



                    FocusElementNode blank2 = new FocusElementNode();

                    blank2.parent = fn.id;

                    blank2.x = fn.x + 1;

                    blank2.y = fn.y + 1;

                    blank2.id = getNextId();

                    ret.add(nodeID);

                    tile.data.put(nodeID, blank2);



                    fn.children = new int[]{blank1.id, blank2.id};

                } else {



                    FocusElementNode blank = new FocusElementNode();

                    blank.parent = fn.id;

                    blank.x = fn.x;

                    blank.y = fn.y + 1;

                    blank.id = getNextId();

                    ret.add(nodeID);

                    tile.data.put(nodeID, blank);

                    fn.children = new int[]{blank.id};

                    selectedNode = nodeID;

                }

            }

        } catch (Exception e) {

            Arcaneresearch.LOGGER.error("Error adding focus node", e);

        }



        if (gather) {

            calcNodeTreeLayout();

            gatherInfo(true);

        }



        return ret;

    }



    private void cullChildren(int idx) {

        TileFocalManipulator tile = getTile();

        if (tile.data.containsKey(idx)) {

            for (int childId : tile.data.get(idx).children) {

                cullChildren(childId);

                tile.data.remove(childId);

            }

        }

    }



    private int getNextId() {

        while (getTile().data.containsKey(nodeID)) {

            nodeID++;

        }

        return nodeID;

    }



    private void calcNodeTreeLayout() {

        TileFocalManipulator tile = getTile();



        int splitId = -1;

        for (FocusElementNode node : tile.data.values()) {

            if (splitId < 0 && node.node instanceof FocusModSplit) {

                splitId = node.id;

            }

        }



        if (splitId >= 0) {

            Bounds bounds = new Bounds();

            processLeftNodes(tile.data.get(splitId), bounds);

            centerNodes(splitId, bounds);

        }



        for (FocusElementNode node : tile.data.values()) {

            if (node.node instanceof FocusModSplit) {

                if (tile.data.containsKey(node.parent) && tile.data.get(node.parent).node != null

                        && !(tile.data.get(node.parent).node instanceof FocusModSplit)) {

                    node.x = tile.data.get(node.parent).x;

                } else {

                    int xx = 0;

                    for (int childId : node.children) {

                        xx += tile.data.get(childId).x;

                    }

                    if (node.children.length > 0) {

                        xx /= node.children.length;

                    }

                    node.x = xx;

                }

            }

        }



        if (selectedNode >= 0 && !tile.data.containsKey(selectedNode)) {

            selectedNode = -1;

        }

    }



    private void processLeftNodes(FocusElementNode start, Bounds bounds) {

        TileFocalManipulator tile = getTile();

        if (start.children.length > 0) {

            processLeftNodes(tile.data.get(start.children[0]), bounds);

        }

        int ox = 0;

        if (start.children.length == 1) {

            ox = bounds.x - 1;

            bounds.x = tile.data.get(start.children[0]).x;

        }

        start.x = bounds.x;

        if (start.children.length == 1) {

            bounds.x = ox;

        }

        ++bounds.x;

        if (start.children.length > 1) {

            processLeftNodes(tile.data.get(start.children[1]), bounds);

        }

    }



    private void centerNodes(int start, Bounds bounds) {

        TileFocalManipulator tile = getTile();

        int x = bounds.x / 2;

        FocusElementNode sn = tile.data.get(start);

        moveNodes(sn, x);

    }



    private void moveNodes(FocusElementNode start, int amt) {

        TileFocalManipulator tile = getTile();

        for (int ci : start.children) {

            moveNodes(tile.data.get(ci), amt);

        }

        start.x -= amt;

    }



    private void gatherInfo(boolean sync) {

        TileFocalManipulator tile = getTile();



        shownParts.clear();

        components = null;



        rebuildSettingSpinners();



        if (tile.getItem(0).isEmpty()) {



            if (partsScrollbar != null) {

                removeWidget(partsScrollbar);

                partsScrollbar = null;

            }

            if (mainScrollbarY != null) {

                removeWidget(mainScrollbarY);

                mainScrollbarY = null;

            }

            if (mainScrollbarX != null) {

                removeWidget(mainScrollbarX);

                mainScrollbarX = null;

            }

            rebuildCrystalButtons();

            return;

        }



        Map<String, Integer> compCount = new HashMap<>();

        totalComplexity = 0;

        maxComplexity = 0;



        ItemStack focusStack = menu.getSlot(0).getItem();

        if (!focusStack.isEmpty() && focusStack.getItem() instanceof ItemFocus focus) {

            maxComplexity = focus.getMaxComplexity();

        }



        boolean emptyNodes = false;

        AspectList crystals = new AspectList();



        if (tile.data != null && !tile.data.isEmpty()) {

            for (FocusElementNode node : tile.data.values()) {

                if (node.node != null) {

                    int count = compCount.getOrDefault(node.node.getKey(), 0) + 1;

                    node.complexityMultiplier = 0.5f * (count + 1);

                    compCount.put(node.node.getKey(), count);

                    totalComplexity += (int) (node.node.getComplexity() * node.complexityMultiplier);



                    if (node.node.getAspect() != null) {

                        crystals.add(node.node.getAspect(), 1);

                    }

                } else {

                    emptyNodes = true;

                }

            }

        }



        costCast = totalComplexity / 5.0f;

        costVis = totalComplexity * 10 + maxComplexity / 5;

        costXp = (int) Math.min(1L, Math.round(Math.sqrt(totalComplexity)));



        boolean validCrystals = false;

        if (crystals.getAspects().length > 0) {

            validCrystals = true;

            components = new ItemStack[crystals.getAspects().length];

            int idx = 0;

            for (Aspect aspect : crystals.getAspects()) {

                components[idx] = ArcaneresearchApiHelper.makeCrystal(aspect, crystals.getAmount(aspect));



                if (!arcaneresearch.common.lib.utils.InventoryUtils.isPlayerCarryingAmount(

                        Objects.requireNonNull(minecraft).player, components[idx], false)) {

                    validCrystals = false;

                }

                idx++;

            }

        }



        rebuildCrystalButtons();



        gatherPartsList();



        if (partsScrollbar != null) {

            removeWidget(partsScrollbar);

            partsScrollbar = null;

        }

        if (shownParts.size() > 6) {

            partsScrollbar = new SliderWidget(leftPos + 51, topPos + 30, 8, 149,

                    0.0f, shownParts.size() - 6, partsStart, true,

                    value -> partsStart = Math.round(value));

            addRenderableWidget(partsScrollbar);

        }



        calcScrollBounds();

        if (scrollY > (sMaxY - 3) * 32) {

            scrollY = (sMaxY - 3) * 32;

        }

        if (scrollX > sMaxX * 24) {

            scrollX = sMaxX * 24;

        }

        if (scrollX < sMinX * 24) {

            scrollX = sMinX * 24;

        }



        if (mainScrollbarY != null) {

            removeWidget(mainScrollbarY);

            mainScrollbarY = null;

        }

        if (mainScrollbarX != null) {

            removeWidget(mainScrollbarX);

            mainScrollbarX = null;

        }

        if (sMaxY * 32 > 130) {

            mainScrollbarY = new SliderWidget(leftPos + 203, topPos + 32, 8, 156,

                    0.0f, (sMaxY - 3) * 32, scrollY, true,

                    value -> scrollY = Math.round(value));

            addRenderableWidget(mainScrollbarY);

        } else {

            scrollY = 0;

        }

        if (sMinX * 24 < -70 || sMaxX * 24 > 70) {

            mainScrollbarX = new SliderWidget(leftPos + 64, topPos + 195, 132, 8,

                    sMinX * 24, sMaxX * 24, scrollX, false,

                    value -> scrollX = Math.round(value));

            addRenderableWidget(mainScrollbarX);

        } else {

            scrollX = 0;

        }



        boolean creative = Objects.requireNonNull(minecraft).player != null && minecraft.player.getAbilities().instabuild;

        valid = totalComplexity <= maxComplexity && !emptyNodes

                && (creative || (validCrystals && tile.xpCost <= Objects.requireNonNull(minecraft.player).experienceLevel));



        if (tile.focusName.isEmpty() && !tile.getItem(0).isEmpty()) {

            tile.focusName = tile.getItem(0).getHoverName().getString();

        }

        setNameFieldValue(tile.focusName);



        if (sync) {

            PacketHandler.sendToServer(new PacketFocusNodesToServer(tile.getBlockPos(), tile.data, tile.focusName));

        }

    }



    private void rebuildSettingSpinners() {

        for (SpinnerWidget spinner : settingSpinners) {

            removeWidget(spinner);

        }

        settingSpinners.clear();



        FocusElementNode fn = getTile().data.get(selectedNode);

        if (fn == null || fn.node == null || fn.node.getSettingList().isEmpty()) {

            return;

        }

        int a = 0;

        for (String sk : fn.node.getSettingList()) {

            NodeSetting setting = fn.node.getSetting(sk);

            if (setting.getResearch() != null

                    && !ArcaneresearchCapabilities.knowsResearchStrict(Objects.requireNonNull(Objects.requireNonNull(minecraft).player), setting.getResearch())) {

                continue;

            }

            int x = leftPos + imageWidth;

            int y = topPos + imageHeight + 3 - fn.node.getSettingList().size() * 26 + a * 26;

            int w = 32;

            if (setting.getType() instanceof NodeSetting.NodeSettingIntList) {

                w = 72;

            }

            SpinnerWidget spinner = new SpinnerWidget(x, y, w, setting,

                    (s, newValue) -> gatherInfo(true));

            settingSpinners.add(spinner);

            addRenderableWidget(spinner);

            ++a;

        }

    }



    private void rebuildCrystalButtons() {

        for (HoverButton button : crystalButtons) {

            removeWidget(button);

        }

        crystalButtons.clear();



        if (components == null) {

            return;

        }

        int i = 0, q = 0;

        for (ItemStack stack : components) {

            HoverButton button = new HoverButton(leftPos + 234 + i * 16, topPos + 110 + 16 * q,

                    16, 16, stack.getHoverName().getString(), null, stack);

            crystalButtons.add(button);

            addRenderableWidget(button);

            if (++i > 4) {

                i = 0;

                ++q;

            }

        }

    }



    private void gatherPartsList() {

        shownParts.clear();



        if (selectedNode < 0 || !getTile().data.containsKey(selectedNode)) return;



        partsStart = 0;

        List<String> mediums = new ArrayList<>();

        List<String> effects = new ArrayList<>();

        List<String> mods = new ArrayList<>();

        List<String> excluded = new ArrayList<>();



        boolean hasExclusive = false;

        boolean hasMedium = false;



        for (FocusElementNode fn : getTile().data.values()) {

            if (fn.node instanceof FocusMedium medium) {

                hasMedium = !(fn.node instanceof FocusMediumRoot);

                if (medium.isExclusive()) {

                    hasExclusive = true;

                    break;

                }

            }

            if (fn.node != null && fn.node.isExclusive()) {

                excluded.add(fn.node.getKey());

            }

        }



        FocusElementNode node = getTile().data.get(selectedNode);

        FocusElementNode parent = getTile().data.get(node.parent);



        if (parent != null && parent.node != null) {

            for (String key : FocusEngine.elements.keySet()) {

                IFocusElement element = FocusEngine.getElement(key);

                if (element == null) continue;



                if (!ArcaneresearchCapabilities.knowsResearchStrict(Objects.requireNonNull(Objects.requireNonNull(minecraft).player), element.getResearch())) {

                    continue;

                }



                if (element.getKey().equals("arcaneresearch.ROOT")) continue;



                if (!(element instanceof FocusNode focusNode)) continue;



                if (excluded.contains(focusNode.getKey())) continue;



                FocusNode.EnumSupplyType[] mustBeSupplied = focusNode.mustBeSupplied();

                if (mustBeSupplied == null) continue;



                boolean compatible = false;

                for (FocusNode.EnumSupplyType type : mustBeSupplied) {

                    if (parent.node.canSupply(type)) {

                        compatible = true;

                        break;

                    }

                }



                if (!compatible) continue;



                switch (element.getType()) {

                    case EFFECT -> effects.add(key);

                    case MEDIUM -> {

                        if (!hasExclusive && (!(element instanceof FocusMedium medium) || !medium.isExclusive() || !hasMedium)) {

                            mediums.add(key);

                        }

                    }

                    case MOD -> mods.add(key);

                }

            }

        }



        Collections.sort(mediums);

        Collections.sort(effects);

        Collections.sort(mods);



        shownParts.addAll(mediums);

        shownParts.addAll(effects);

        shownParts.addAll(mods);

    }



    private void calcScrollBounds() {

        int sMinY;

        sMinX = sMinY = sMaxX = sMaxY = 0;



        for (FocusElementNode fn : getTile().data.values()) {

            if (fn.x < sMinX) sMinX = fn.x;

            if (fn.y < sMinY) sMinY = fn.y;

            if (fn.x > sMaxX) sMaxX = fn.x;

            if (fn.y > sMaxY) sMaxY = fn.y;

        }

    }



    @Override

        // NEOFORGE_PORT: mouse coords/button via MouseButtonEvent.



    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {

        double mouseX = event.x();

        double mouseY = event.y();

        int button = event.button();

        TileFocalManipulator tile = getTile();



        if (tile.vis <= 0.0f && tile.data != null && !tile.data.isEmpty()) {



            if (nameField != null && nameField.mouseClicked(event, doubleClick)) {

                return true;

            }



            if (lastNodeHover >= 0) {

                selectedNode = lastNodeHover;



                // NEOFORGE_PORT (26.3): mouse buttons are 1-based (1 = LEFT); Forge 1 = right click.
                if (button == com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_RIGHT
                        && tile.data.get(selectedNode).node != null) {

                    FocusElementNode fn = tile.data.get(selectedNode);

                    if (tile.data.get(fn.parent).node != null) {

                        addNodeAt(tile.data.get(fn.parent).node.getClass(), fn.parent, true);

                    }

                }



                gatherInfo(false);

                playButtonClick();

            }



            int gx = leftPos;

            int gy = topPos;

            int count = 0;

            int index = 0;



            if (selectedNode >= 0) {

                for (String key : shownParts) {

                    if (++count - 1 < partsStart) continue;



                    if (isInRegion(gx + 28, gy + 32 + 24 * index, 20, 20, (int) mouseX, (int) mouseY)) {

                        Class<? extends FocusNode> nodeClass = FocusEngine.elements.get(key);

                        if (nodeClass != null) {

                            addNodeAt(nodeClass, selectedNode, true);

                        }

                        return true;

                    }



                    if (++index > 5) break;

                }

            }

        }



        if (isInRegion(leftPos + 63, topPos + 31, 136, 160, (int) mouseX, (int) mouseY)) {

            isDragging = true;

            lastMouseX = (int) mouseX;

            lastMouseY = (int) mouseY;

        }



        return super.mouseClicked(event, doubleClick);

    }



    @Override

        // NEOFORGE_PORT: release/drag via MouseButtonEvent.



    public boolean mouseReleased(MouseButtonEvent event) {
        isDragging = false;
        return super.mouseReleased(event);
    }



    @Override



    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (isDragging && isInRegion(leftPos + 63, topPos + 31, 136, 160, (int) mouseX, (int) mouseY)) {
            scrollX -= (int) mouseX - lastMouseX;

            scrollY -= (int) mouseY - lastMouseY;



            scrollX = Math.max(sMinX * 24, Math.min(sMaxX * 24, scrollX));

            scrollY = Math.max(0, Math.min((sMaxY - 3) * 32, scrollY));



            if (mainScrollbarX != null) mainScrollbarX.setSliderValue(scrollX, false);

            if (mainScrollbarY != null) mainScrollbarY.setSliderValue(scrollY, false);



            lastMouseX = (int) mouseX;

            lastMouseY = (int) mouseY;

            return true;

        }

        return super.mouseDragged(event, dragX, dragY);

    }



    @Override

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {

        double delta = scrollY;



        if (shownParts.size() > 6 && isInRegion(leftPos + 24, topPos + 24, 32, 157, (int) mouseX, (int) mouseY)) {

            if (delta > 0 && partsStart > 0) {

                partsStart--;

            } else if (delta < 0 && partsStart < shownParts.size() - 6) {

                partsStart++;

            }

            if (partsScrollbar != null) partsScrollbar.setSliderValue(partsStart, false);

            return true;

        }



        if (mainScrollbarY != null && isInRegion(leftPos + 63, topPos + 31, 136, 160, (int) mouseX, (int) mouseY)) {

            int maxScroll = (sMaxY - 3) * 32;

            // NEOFORGE_PORT: param scrollY (amount) shadows the field; field via this.* (as in Forge).

            if (delta < 0 && this.scrollY < maxScroll) {

                this.scrollY = Math.min(maxScroll, this.scrollY + 16);

            } else if (delta > 0 && this.scrollY > 0) {

                this.scrollY = Math.max(0, this.scrollY - 16);

            } else {

                return true;

            }

            mainScrollbarY.setSliderValue(this.scrollY, false);

            return true;

        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);

    }



    @Override

    // NEOFORGE_PORT: EditBox takes KeyEvent directly; 256 = ESC (see Thaumaturge screens).



    public boolean keyPressed(KeyEvent event) {

        // NEOFORGE_PORT (26.3): key values are SDL scancodes (GLFW 256 never matched) -> isEscape().
        if (nameField != null && nameField.isFocused() && !event.isEscape()) {

            nameField.keyPressed(event);

            return true;

        }

        return super.keyPressed(event);

    }



    private void playButtonClick() {

        if (Objects.requireNonNull(minecraft).player != null) {

            minecraft.player.playSound(arcaneresearch.init.ModSounds.CLACK.get(), 0.4f, 1.0f);

        }

    }



    private void playRollover() {

        if (Objects.requireNonNull(minecraft).player != null) {

            minecraft.player.playSound(arcaneresearch.init.ModSounds.CLACK.get(), 0.4f, 2.0f);

        }

    }



    private void playSocketSound(float pitch) {

        if (Objects.requireNonNull(minecraft).player != null) {

            minecraft.player.playSound(arcaneresearch.init.ModSounds.CRYSTAL.get(), 0.4f, pitch);

        }

    }



    private boolean isInRegion(int x, int y, int width, int height, int mouseX, int mouseY) {

        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;

    }



    private static class Bounds {

        int x;

    }

}
