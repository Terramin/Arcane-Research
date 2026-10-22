package arcaneresearch.client.gui.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor; 
import net.minecraft.client.renderer.RenderPipelines; 
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; 
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.ResearchCategory;
import arcaneresearch.api.research.theorycraft.ITheorycraftAid;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;
import arcaneresearch.api.research.theorycraft.TheorycraftManager;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.client.gui.widgets.ImageButton;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.misc.PacketStartTheoryToServer;
import arcaneresearch.common.menu.ResearchTableMenu;
import arcaneresearch.common.tiles.crafting.TileResearchTable;
import arcaneresearch.init.ModSounds;

import java.util.*;

public class ResearchTableScreen extends AbstractContainerScreen<ResearchTableMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_research_table.png");
    private static final Identifier TX_BASE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_base.png");
    private static final Identifier TX_PAPER =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/paper.png");
    private static final Identifier TX_PAPER_GILDED =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/papergilded.png");
    private static final Identifier TX_QUESTION =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/aspects/_unknown.png");
    private final TileResearchTable table;
    private final Player player;
    private final Set<String> sparkleCats = new HashSet<>();
    private final HashMap<String, Integer> tempCatTotals = new HashMap<>();
    private final Set<String> selectedAids = new HashSet<>();
    private final ArrayList<ResearchTableData.CardChoice> cardChoices = new ArrayList<>();
    private ImageButton buttonCreate;
    private ImageButton buttonComplete;
    private ImageButton buttonScrap;
    private float[] cardHover = new float[]{0.0f, 0.0f, 0.0f};
    private float[] cardZoomOut = new float[]{0.0f, 0.0f, 0.0f};
    private float[] cardZoomIn = new float[]{0.0f, 0.0f, 0.0f};
    private boolean[] cardActive = new boolean[]{true, true, true};
    private boolean cardSelected = false;
    private long nextCatCheck = 0L;
    private Set<String> currentAids = new HashSet<>();
    private long nextAidCheck = 0L;
    private int dummyInspirationStart = 0;
    private ResearchTableData.CardChoice lastDraw = null;

    public ResearchTableScreen(ResearchTableMenu menu, Inventory playerInventory, Component title) {
        
        super(menu, playerInventory, title, 255, 255);
        this.inventoryLabelY = this.imageHeight - 94;
        this.table = menu.getBlockEntity();
        this.player = playerInventory.player;

        if (table.data != null) {
            for (String cat : table.data.categoryTotals.keySet()) {
                tempCatTotals.put(cat, table.data.categoryTotals.get(cat));
            }
            syncFromTableChoices();
            lastDraw = table.data.lastDraw;
        }
    }

    private static void setButton(ImageButton button, boolean visible, boolean active) {
        button.visible = visible;
        button.active = active;
        button.setButtonActive(active);
    }

    private static void drawTexturedQuadFull(GuiGraphicsExtractor graphics, Identifier texture, float x, float y) {
        
        
        
        int[] dims = arcaneresearch.client.lib.AspectRenderer.textureDims(texture);
        int sw = dims != null ? dims[0] : 16;
        int sh = dims != null ? dims[1] : 16;
        graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, texture, (int) x, (int) y, 0, 0, 16, 16, sw, sh, sw, sh, 0xFFFFFFFF);
    }

    
    
    private static void drawTexturedQuadFull(GuiGraphicsExtractor graphics, Identifier texture, float x, float y, int color) {
        int[] dims = arcaneresearch.client.lib.AspectRenderer.textureDims(texture);
        int sw = dims != null ? dims[0] : 16;
        int sh = dims != null ? dims[1] : 16;
        graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, texture, (int) x, (int) y, 0, 0, 16, 16, sw, sh, sw, sh, color);
    }

    private void syncFromTableChoices() {
        cardChoices.clear();
        if (table.data != null) {
            cardChoices.addAll(table.data.cardChoices);
        }
    }

    @Override
    protected void init() {
        super.init();

        buttonCreate = addRenderableWidget(new ImageButton(
                leftPos + 128, topPos + 22, 49, 11, "button.create.theory", null,
                TX_BASE, 37, 66, 51, 13, 0xFF88FFAA, btn -> onCreateTheory()));

        buttonComplete = addRenderableWidget(new ImageButton(
                leftPos + 191, topPos + 96, 49, 11, "button.complete.theory", null,
                TX_BASE, 37, 66, 51, 13, 0xFF88FFAA, btn -> onCompleteTheory()));

        buttonScrap = addRenderableWidget(new ImageButton(
                leftPos + 128, topPos + 168, 49, 11, "button.scrap.theory", null,
                TX_BASE, 37, 66, 51, 13, 0xFFFF2222, btn -> onScrapTheory()));

        updateButtonStates();
    }

    private void onCreateTheory() {
        playButtonClick();

        PacketHandler.sendToServer(new PacketStartTheoryToServer(table.getBlockPos(), selectedAids));
    }

    private void onCompleteTheory() {
        playButtonClick();
        if (minecraft != null && minecraft.gameMode != null) {

            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 7);
        }
        tempCatTotals.clear();
        lastDraw = null;
    }

    private void onScrapTheory() {
        playButtonClick();
        if (minecraft != null && minecraft.gameMode != null) {

            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 9);
        }
        tempCatTotals.clear();
        lastDraw = null;
        table.data = null;
        cardChoices.clear();
    }

    private void updateButtonStates() {

        ItemStack tools = table.getItem(TileResearchTable.SLOT_SCRIBING_TOOLS);
        ItemStack paper = table.getItem(TileResearchTable.SLOT_PAPER);
        boolean toolsOk = !tools.isEmpty() && tools.getDamageValue() < tools.getMaxDamage();
        boolean paperOk = !paper.isEmpty();

        setButton(buttonCreate, table.data == null, table.data == null && toolsOk && paperOk);
        setButton(buttonComplete, table.data != null && table.data.isComplete(),
                table.data != null && table.data.isComplete());
        setButton(buttonScrap, table.data != null && !table.data.isComplete(),
                table.data != null && !table.data.isComplete());
    }

        
    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);


        
        int xx = this.leftPos;
        int yy = this.topPos;

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xx, yy, 0, 0, 255, 255, 256, 256);

        if (table.data == null) {

            renderPreGameState(graphics, mouseX, mouseY, partialTick);
        } else {

            renderActiveGameState(graphics, mouseX, mouseY, partialTick);
        }

        
        updateButtonStates();
        renderOverlay(graphics, mouseX, mouseY, partialTick);
    }

    private void renderPreGameState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int xx = leftPos;
        int yy = topPos;

        if (nextAidCheck < player.tickCount) {
            currentAids = table.checkSurroundingAids();
            dummyInspirationStart = ResearchTableData.getAvailableInspiration(player);
            nextAidCheck = player.tickCount + 100;
        }

        var pose = graphics.pose(); 
        pose.pushMatrix();
        pose.translate(xx + 128 - dummyInspirationStart * 5, yy + 55);
        pose.scale(0.5f, 0.5f);

        for (int a = 0; a < dummyInspirationStart; a++) {

            int texU = (dummyInspirationStart - selectedAids.size() <= a) ? 48 : 32;
            graphics.blit(RenderPipelines.GUI_TEXTURED, TX_BASE, 20 * a, 0, texU, 96, 16, 16, 256, 256);
        }
        pose.popMatrix();

        if (!currentAids.isEmpty()) {
            int side = Math.min(currentAids.size(), 6);
            int c = 0;
            int r = 0;

            for (String key : currentAids) {
                ITheorycraftAid aid = TheorycraftManager.aids.get(key);
                if (aid == null) continue;

                int x = xx + 128 + 20 * c - side * 10;
                int y = yy + 85 + 35 * r;

                if (selectedAids.contains(key)) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, TX_BASE, x, y, 0, 96, 16, 16, 256, 256);
                }

                Object aidObj = aid.getAidObject();
                if (aidObj instanceof ItemStack stack) {
                    arcaneresearch.client.gui.GuiRenderHelper.renderItem(graphics, stack, x, y);
                } else if (aidObj instanceof Block block) {
                    arcaneresearch.client.gui.GuiRenderHelper.renderItem(graphics, new ItemStack(block), x, y);
                }

                if (++c >= side) {
                    r++;
                    c = 0;
                }
            }
        }
    }

    private void renderActiveGameState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int xx = leftPos;
        int yy = topPos;
        ResearchTableData data = table.data;

        checkCards();

        var pose = graphics.pose(); 
        pose.pushMatrix();
        pose.translate(xx + 15, yy + 150);
        for (int a = 0; a < data.bonusDraws; a++) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TX_BASE, a * 2, a, 64, 96, 16, 16, 256, 256);
        }
        pose.popMatrix();

        pose.pushMatrix();
        pose.translate(xx + 128 - data.inspirationStart * 5, yy + 16);
        pose.scale(0.5f, 0.5f);
        for (int a = 0; a < data.inspirationStart; a++) {
            int texU = (data.inspiration <= a) ? 48 : 32;
            graphics.blit(RenderPipelines.GUI_TEXTURED, TX_BASE, 20 * a, 0, texU, 96, 16, 16, 256, 256);
        }
        pose.popMatrix();

        int sheets = 0;
        ItemStack paperStack = table.getItem(TileResearchTable.SLOT_PAPER);
        if (!paperStack.isEmpty()) {
            sheets = 1 + paperStack.getCount() / 4;
        }

        Random r = new Random(55L);
        if (sheets > 0 && !data.isComplete()) {

            for (int a = 0; a < sheets; a++) {
                drawSheet(graphics, xx + 65, yy + 100, 6.0f, r, 1.0f, 1.0f, null, partialTick);
            }

            boolean highlight = false;
            int var7 = mouseX - (25 + xx);
            int var8 = mouseY - (55 + yy);
            if (cardChoices.isEmpty() && var7 >= 0 && var8 >= 0 && var7 < 75 && var8 < 90) {
                highlight = true;
            }

            pose.pushMatrix();
                    pose.translate(xx + 65, yy + 100);
            float scale = highlight ? 1.75f : 1.5f;
            pose.scale(scale, scale);
            
            drawTexturedQuadFull(graphics, TX_QUESTION, -8, -8, highlight ? 0xFFFFFFFF : 0x80FFFFFF);
                pose.popMatrix();
        }

        for (Long seed : data.savedCards) {
            r = new Random(seed);
            drawSheet(graphics, xx + 191, yy + 100, 6.0f, r, 1.0f, 1.0f, null, partialTick);
        }

        if (lastDraw != null) {
            r = new Random(lastDraw.card.getSeed());
            drawSheet(graphics, xx + 191, yy + 100, 6.0f, r, 1.0f, 1.0f, lastDraw, partialTick);
        }

        updateCategoryTotals();

        drawCategoryProgress(graphics, mouseX, mouseY);

        drawCardChoices(graphics, mouseX, mouseY, partialTick);
    }

    private void drawSheet(GuiGraphicsExtractor graphics, float x, float y, float scale, Random r,
                           float alpha, float tilt, ResearchTableData.CardChoice cardChoice,
                           float partialTick) {
        var pose = graphics.pose(); 
        pose.pushMatrix();


        pose.translate((float) (x + r.nextGaussian()), (float) (y + r.nextGaussian()));
        pose.scale(scale, scale);

        float rotation = (float) (r.nextGaussian() * tilt);
        
        
        pose.rotate((float) Math.toRadians(rotation));

        pose.pushMatrix();

        Identifier paperTex = (cardChoice != null && cardChoice.fromAid) ? TX_PAPER_GILDED : TX_PAPER;

        if (r.nextBoolean()) {
            pose.rotate((float) Math.toRadians(180));
        }
        if (r.nextBoolean()) {
            pose.scale(-1.0f, 1.0f);
        }

        drawTexturedQuadFull(graphics, paperTex, -8, -8,
                0xFFFFFF | ((int) (alpha * 255.0f) << 24));
        pose.popMatrix();

        if (cardChoice != null && alpha == 1.0f) {
            drawCardContent(graphics, cardChoice, partialTick);
        }

        pose.popMatrix();
    }

    private void drawCardContent(GuiGraphicsExtractor graphics, ResearchTableData.CardChoice cardChoice,
                                 float partialTick) {
        var pose = graphics.pose(); 
        TheorycraftCard card = cardChoice.card;

        if (card.getResearchCategory() != null) {
            ResearchCategory rc = ResearchCategories.getResearchCategory(card.getResearchCategory());
            if (rc != null && rc.icon != null) {
                        pose.pushMatrix();
                pose.scale(0.5f, 0.5f);
                
                drawTexturedQuadFull(graphics, rc.icon, -8, -8, 0x2AFFFFFF);
                pose.popMatrix();
            }
        }

        pose.pushMatrix();
        pose.scale(0.0625f, 0.0625f);
        String name = "§l" + Component.translatable(card.getLocalizedName()).getString() + "§r";
        int nameWidth = font.width(name);
        graphics.text(font, name, -nameWidth / 2, -65, 0xFF000000, false);

        String text = Component.translatable(card.getLocalizedText()).getString();
        drawSplitString(graphics, text);
        pose.popMatrix();

        pose.pushMatrix();
        pose.scale(0.0625f, 0.0625f);
        int cost = card.getInspirationCost();
        boolean isGain = cost < 0;
        int hearts = isGain ? Math.abs(cost) + 1 : cost;

        for (int a = 0; a < hearts; a++) {
            int texU = (a == 0 && isGain) ? 48 : 32;
            graphics.blit(RenderPipelines.GUI_TEXTURED, TX_BASE, -10 * hearts + 20 * a, -95, texU, (isGain && a == 0) ? 0 : 96, 16, 16, 256, 256);
        }
        pose.popMatrix();

        ItemStack[] items = card.getRequiredItems();
        if (items != null) {
            pose.pushMatrix();
            for (int a = 0; a < items.length; a++) {
                pose.pushMatrix();
                pose.scale(0.125f, 0.125f);

                if (items[a] == null || items[a].isEmpty()) {

                                pose.translate(-9 * items.length + 18 * a, 35);
                    
                    drawTexturedQuadFull(graphics, TX_QUESTION, 0, 0, 0xFFBFBFBF);
                } else {

                    arcaneresearch.client.gui.GuiRenderHelper.renderItem(graphics, items[a], -9 * items.length + 18 * a, 35);

                    try {
                        if (card.getRequiredItemsConsumed()[a]) {
                            pose.pushMatrix();
                            float bob = (float) Math.sin((player.tickCount + a * 2 + partialTick) / 2.0f) * 0.03f;
                            pose.translate(-2 - 9 * items.length + 18 * a, 45.0f + bob * 10.0f);
                            pose.scale(0.5f, 0.5f + bob);
                            
                            graphics.blit(RenderPipelines.GUI_TEXTURED, TX_BASE, 0, 0, 64, 120, 16, 16, 256, 256);
                            pose.popMatrix();
                        }
                    } catch (Exception ignored) {
                    }
                }
                pose.popMatrix();
            }
            pose.popMatrix();
        }
    }

    private void drawSplitString(GuiGraphicsExtractor graphics, String text) {

        List<String> lines = new ArrayList<>();
        for (String paragraph : text.split("\n")) {
            StringBuilder currentLine = new StringBuilder();
            for (String word : paragraph.split(" ")) {
                String testLine = !currentLine.isEmpty() ? currentLine + " " + word : word;
                if (font.width(testLine) > 140) {
                    if (!currentLine.isEmpty()) {
                        lines.add(currentLine.toString());
                        currentLine = new StringBuilder(word);
                    } else {
                        lines.add(word);
                    }
                } else {
                    currentLine = new StringBuilder(testLine);
                }
            }
            lines.add(currentLine.toString());
        }

        for (int i = 0; i < lines.size(); i++) {
            graphics.text(font, lines.get(i), -70, -48 + i * 9, 0xFF000000, false);
        }
    }

    private void updateCategoryTotals() {
        if (table.data == null) return;

        if (nextCatCheck < player.tickCount) {
            sparkleCats.clear();
            for (String cat : ResearchCategories.researchCategories.keySet()) {
                int actual = table.data.categoryTotals.getOrDefault(cat, 0);
                int display = tempCatTotals.getOrDefault(cat, 0);

                if (actual == 0 && display == 0) {
                    tempCatTotals.remove(cat);
                } else {
                    if (display > actual) display--;
                    if (display < actual) {
                        display++;
                        sparkleCats.add(cat);
                    }
                    tempCatTotals.put(cat, display);
                }
            }
            nextCatCheck = player.tickCount + 1;
        }
    }

    private void drawCategoryProgress(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int xx = leftPos;
        int yy = topPos;
        ResearchTableData data = table.data;

        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(tempCatTotals.entrySet());
        sorted.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        sorted.removeIf(e -> e.getValue() <= 0);


        int i = 0;
        for (Map.Entry<String, Integer> entry : sorted) {
            String cat = entry.getKey();
            int total = entry.getValue();

            var pose = graphics.pose(); 
            pose.pushMatrix();

                pose.translate(xx + 253, yy + 16 + i * 18 + (i > 0 ? 4 : 0));

            ResearchCategory rc = ResearchCategories.getResearchCategory(cat);
            if (rc != null && rc.icon != null) {
                drawTexturedQuadFull(graphics, rc.icon, 0, 0);
            }
            pose.popMatrix();

            String s = total + "%";
            if (i > data.penaltyStart) {
                int penalty = total / 3;
                s = s + " (-" + penalty + ")";
            }

            int color = data.categoriesBlocked.contains(cat) ? 0xFF606060 :
                    (i <= data.penaltyStart ? 0xFF00E100 : 0xFFFFFFFF);

            graphics.text(font, s, xx + 276, yy + 20 + i * 18 + (i > data.penaltyStart ? 4 : 0), color, true);

            if (sparkleCats.contains(cat)) {
                net.minecraft.util.RandomSource prand = player.getRandom();
                for (int q = 0; q < 2; q++) {
                    float rr = 1.0f;
                    float gg = (189 + prand.nextInt(67)) / 255.0f;
                    float bb = (64 + prand.nextInt(192)) / 255.0f;
                    FXDispatcher.INSTANCE.drawSimpleSparkleGui(prand,
                            xx + 276 + prand.nextFloat() * font.width(s),
                            yy + 20 + prand.nextFloat() * 8.0f + i * 18 + (i > data.penaltyStart ? 4 : 0),
                            prand.nextGaussian() * 0.5, prand.nextGaussian() * 0.5,
                            24.0f, rr, gg, bb, 0, 0.9f, -1.0f);
                }
            }

            int var9 = mouseX - (xx + 256);
            int var10 = mouseY - (yy + 16 + i * 18 + (i > data.penaltyStart ? 4 : 0));
            if (var9 >= 0 && var10 >= 0 && var9 < 16 && var10 < 16) {
                
                
                graphics.setTooltipForNextFrame(font, ResearchCategories.getCategoryName(cat), mouseX, mouseY);
            }

            i++;

        }
    }

    private void drawCardChoices(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int xx = leftPos;
        int yy = topPos;
        int sx = 128;
        int cw = 110;
        int sz = cardChoices.size();

        int a = 0;
        for (ResearchTableData.CardChoice cardChoice : cardChoices) {
            Random r = new Random(cardChoice.card.getSeed());

            int var11 = mouseX - (5 + sx - 55 * sz + xx + cw * a);
            int var12 = mouseY - (100 + yy - 60);

            if (cardZoomOut[a] >= 0.95f && !cardSelected) {
                if (var11 >= 0 && var12 >= 0 && var11 < 100 && var12 < 120) {
                    cardHover[a] += Math.max((0.25f - cardHover[a]) / 3.0f * partialTick, 0.0025f);
                } else {
                    cardHover[a] -= 0.1f * partialTick;
                }
            }

            if (a == sz - 1 || cardZoomOut[a + 1] > 0.6f) {
                float prev = cardZoomOut[a];
                cardZoomOut[a] += Math.max((1.0f - cardZoomOut[a]) / 5.0f * partialTick, 0.0025f);
                if (cardZoomOut[a] > 0.0f && prev == 0.0f) {
                    playPageFlip();
                }
            }

            float prevZoomIn = cardZoomIn[a];
            if (cardSelected) {
                cardZoomIn[a] += (float) (cardActive[a] ?
                        Math.max((1.0f - cardZoomIn[a]) / 3.0f * partialTick, 0.0025) :
                        0.3f * partialTick);
                cardHover[a] = 1.0f - cardZoomIn[a];
            }

            cardZoomIn[a] = Mth.clamp(cardZoomIn[a], 0.0f, 1.0f);
            cardHover[a] = Mth.clamp(cardHover[a], 0.0f, 0.25f);
            cardZoomOut[a] = Mth.clamp(cardZoomOut[a], 0.0f, 1.0f);

            float dx = 55 + sx - 55 * sz + xx + cw * a - (xx + 65);
            float fx = xx + 65 + dx * cardZoomOut[a];
            float qx = xx + 191 - fx;
            if (cardActive[a]) {
                fx += qx * cardZoomIn[a];
            }

            float alpha = cardActive[a] ? 1.0f : (1.0f - cardZoomIn[a]);
            float tilt = Math.max(1.0f - cardZoomOut[a], cardZoomIn[a]);
            drawSheet(graphics, fx, yy + 100,
                    6.0f + cardZoomOut[a] * 2.0f - cardZoomIn[a] * 2.0f + cardHover[a],
                    r, alpha, tilt, cardChoice, partialTick);

            if (cardSelected && cardActive[a] && cardZoomIn[a] >= 1.0f && prevZoomIn < 1.0f) {
                playWrite();
                cardChoices.clear();
                cardSelected = false;
                lastDraw = table.data != null ? table.data.lastDraw : null;
                break;
            }

            a++;
        }
    }

    private void renderOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int xx = leftPos;
        int yy = topPos;

        if (table.data == null) {

            if (!currentAids.isEmpty()) {
                int side = Math.min(currentAids.size(), 6);
                int c = 0;
                int r = 0;

        
                                for (String key : currentAids) {
                    ITheorycraftAid aid = TheorycraftManager.aids.get(key);
                    if (aid == null) continue;

                    int x = xx + 128 + 20 * c - side * 10;
                    int y = yy + 85 + 35 * r;

                    if (isHovering(x - xx, y - yy, 16, 16, mouseX, mouseY) && !selectedAids.contains(key)) {
                        
                        graphics.blit(RenderPipelines.GUI_TEXTURED, TX_BASE, x, y, 0, 96, 16, 16, 256, 256, 0x33FFFFFF);
                    }

                    if (++c >= side) {
                        r++;
                        c = 0;
                    }
                }
                    }
        } else {

            int sx = 128;
            int cw = 110;
            int sz = cardChoices.size();
            int a = 0;

            if (!cardSelected) {
                for (ResearchTableData.CardChoice cardChoice : cardChoices) {
                    if (cardZoomOut[a] >= 1.0f) {
                        float dx = 55 + sx - 55 * sz + cw * a - 65;
                        float fx = 65 + dx * cardZoomOut[a];
                        float qx = 191 - fx;
                        if (cardActive[a]) {
                            fx += qx * cardZoomIn[a];
                        }
                        drawSheetOverlay(graphics, fx, cardChoice, mouseX, mouseY);
                    }
                    a++;
                }
            }

            
            
            
        }
    }

    private void renderWarnings(GuiGraphicsExtractor graphics) {
        if (table.data == null) {
            return;
        }
        int xx = leftPos;
        int yy = topPos;
        int warnY = 0;
        ItemStack tools = table.getItem(TileResearchTable.SLOT_SCRIBING_TOOLS);
        ItemStack paper = table.getItem(TileResearchTable.SLOT_PAPER);
        boolean noInk = tools.isEmpty() || tools.getDamageValue() >= tools.getMaxDamage();
        if (!noInk && !paper.isEmpty()) {
            return;
        }
        graphics.nextStratum();
        if (noInk) {
            List<Component> tooltip = List.of(
                    Component.translatable("tile.researchtable.noink.0"),
                    Component.translatable("tile.researchtable.noink.1")
            );
            int warnX = Math.max(font.width(tooltip.get(0)), font.width(tooltip.get(1))) / 2;
            drawTooltipNow(graphics, tooltip, xx - warnX + 116, yy + 60 + warnY);
            warnY += 40;
        }

        if (paper.isEmpty()) {
            Component text = Component.translatable("tile.researchtable.nopaper.0");
            drawTooltipNow(graphics, List.of(text), xx - font.width(text) / 2 + 116, yy + 60 + warnY);
        }
    }

    
    private void drawTooltipNow(GuiGraphicsExtractor graphics, List<Component> lines, int x, int y) {
        List<net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent> comps = new ArrayList<>();
        for (Component line : lines) {
            comps.add(net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent.create(line.getVisualOrderText()));
        }
        graphics.tooltip(font, comps, x, y,
                net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner.INSTANCE, null, false);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractContents(graphics, mouseX, mouseY, partialTick);
        renderWarnings(graphics);
    }

    private void drawSheetOverlay(GuiGraphicsExtractor graphics, float x,
                                  ResearchTableData.CardChoice cardChoice, int mouseX, int mouseY) {
        if (cardChoice == null || cardChoice.card.getRequiredItems() == null) return;

        ItemStack[] items = cardChoice.card.getRequiredItems();
        for (int a = 0; a < items.length; a++) {
            int itemX = (int) (x - 9 * items.length + 18 * a);
            int itemY = (int) ((float) 100 + 36);

            if (isHovering(itemX, itemY, 15, 15, mouseX, mouseY)) {
                if (items[a] == null || items[a].isEmpty()) {
                    graphics.setTooltipForNextFrame(font, Component.translatable("tc.card.unknown"), mouseX, mouseY);
                } else {
                    
                    graphics.setTooltipForNextFrame(font, items[a], mouseX, mouseY);
                }
            }
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {

    }

    
    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int xx = leftPos;
        int yy = topPos;
        int mx = (int) mouseX;
        int my = (int) mouseY;

        if (table.data == null) {

            if (!currentAids.isEmpty()) {
                int side = Math.min(currentAids.size(), 6);
                int c = 0;
                int r = 0;

                for (String key : currentAids) {
                    ITheorycraftAid aid = TheorycraftManager.aids.get(key);
                    if (aid == null) continue;

                    int x = 128 + 20 * c - side * 10;
                    int y = 85 + 35 * r;

                    if (isHovering(x, y, 16, 16, mx, my)) {
                        if (selectedAids.contains(key)) {
                            selectedAids.remove(key);
                        } else if (selectedAids.size() + 1 < dummyInspirationStart) {
                            selectedAids.add(key);
                        }
                        return true;
                    }

                    if (++c >= side) {
                        r++;
                        c = 0;
                    }
                }
            }
        } else {

            int sx = 128;
            int cw = 110;

            if (!cardChoices.isEmpty()) {

                int pressed = -1;
                for (int a = 0; a < cardChoices.size(); a++) {
                    int var7 = mx - (5 + sx - 55 * cardChoices.size() + xx + cw * a);
                    int var8 = my - (100 + yy - 60);

                    if (cardZoomOut[a] >= 0.95f && !cardSelected &&
                            var7 >= 0 && var8 >= 0 && var7 < 100 && var8 < 120) {
                        pressed = a;
                        break;
                    }
                }

                if (pressed >= 0) {
                    ItemStack tools = table.getItem(TileResearchTable.SLOT_SCRIBING_TOOLS);
                    if (!tools.isEmpty() && tools.getDamageValue() < tools.getMaxDamage()) {

                        if (minecraft != null && minecraft.gameMode != null) {
                            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 4 + pressed);
                        }
                        return true;
                    }
                }
            } else {

                int var9 = mx - (25 + xx);
                int var10 = my - (55 + yy);

                if (var9 >= 0 && var10 >= 0 && var9 < 75 && var10 < 90) {
                    ItemStack paper = table.getItem(TileResearchTable.SLOT_PAPER);
                    if (!paper.isEmpty()) {
                        drawCards();
                        return true;
                    }
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    private void drawCards() {
        cardSelected = false;
        cardHover = new float[]{0.0f, 0.0f, 0.0f};
        cardZoomOut = new float[]{0.0f, 0.0f, 0.0f};
        cardZoomIn = new float[]{0.0f, 0.0f, 0.0f};
        cardActive = new boolean[]{true, true, true};

        int draw = 2;
        if (table.data != null && table.data.bonusDraws > 0) {
            draw++;
            table.data.bonusDraws--;
        }
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, draw);
        }
        cardChoices.clear();
    }

    private void checkCards() {
        if (table.data == null) return;

        if (!table.data.cardChoices.isEmpty() && cardChoices.isEmpty()) {
            syncFromTableChoices();
        }

        if (!cardSelected) {
            for (int a = 0; a < cardChoices.size(); a++) {
                try {
                    if (table.data.cardChoices.size() > a && table.data.cardChoices.get(a).selected) {
                        for (int q = 0; q < cardChoices.size(); q++) {
                            cardActive[q] = table.data.cardChoices.get(q).selected;
                        }
                        cardSelected = true;
                        playPageSelect();
                        if (minecraft != null && minecraft.gameMode != null) {

                            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
                        }
                        break;
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateButtonStates();
    }

    private void playPageFlip() {
        if (player != null) {
            player.playSound(ModSounds.PAGE.get(), 1.0f, 1.0f);
        }
    }

    private void playPageSelect() {
        if (player != null) {
            player.playSound(ModSounds.PAGE_TURN.get(), 1.0f, 1.0f);
        }
    }

    private void playButtonClick() {
        if (player != null) {
            player.playSound(ModSounds.CLACK.get(), 0.4f, 1.0f);
        }
    }

    private void playWrite() {
        if (player != null) {
            player.playSound(ModSounds.WRITE.get(), 0.3f, 1.0f);
        }
    }
}