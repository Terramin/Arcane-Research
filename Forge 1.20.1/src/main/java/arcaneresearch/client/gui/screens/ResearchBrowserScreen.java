package arcaneresearch.client.gui.screens;
import arcaneresearch.common.world.maze.Cell;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.casters.FocusEngine;
import arcaneresearch.api.casters.FocusNode;
import arcaneresearch.api.casters.IFocusElement;
import arcaneresearch.api.crafting.CrucibleRecipe;
import arcaneresearch.api.crafting.InfusionRecipe;
import arcaneresearch.api.internal.CommonInternals;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.ResearchCategory;
import arcaneresearch.api.research.ResearchEntry;
import arcaneresearch.api.research.ResearchStage;
import arcaneresearch.common.config.ConfigResearch;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.playerdata.PacketSyncProgressToServer;
import arcaneresearch.common.lib.network.playerdata.PacketSyncResearchFlagsToServer;
import arcaneresearch.common.lib.research.ResearchManager;
import arcaneresearch.common.lib.utils.InventoryUtils;
import java.util.*;
@OnlyIn(Dist.CLIENT)
public class ResearchBrowserScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Arcaneresearch.MODID, "textures/gui/gui_research_browser.png");
    private static final ResourceLocation NODE_TEXTURE = new ResourceLocation(Arcaneresearch.MODID, "textures/misc/auranodes.png");
    private static final java.util.Map<ResourceLocation, int[]> textureSizeCache = new java.util.HashMap<>();
    public static double lastX = -9999.0;
    public static double lastY = -9999.0;
    static String selectedCategory;
    static int catScrollPos = 0;
    static int catScrollMax = 0;
    private static int guiBoundsLeft;
    private static int guiBoundsTop;
    private static int guiBoundsRight;
    private static int guiBoundsBottom;
    private static boolean searching = false;
    private final int startX = 16;
    private final int startY = 16;
    private final LinkedList<ResearchEntry> research = new LinkedList<>();
    private final ArrayList<String> categoriesTC = new ArrayList<>();
    private final ArrayList<String> categoriesOther = new ArrayList<>();
    private final ArrayList<String> invisible = new ArrayList<>();
    public int addonShift = 0;
    protected int lastMouseX;
    protected int lastMouseY;
    protected float screenZoom = 1.0f;
    protected double curMouseX;
    protected double curMouseY;
    protected double guiMapX;
    protected double guiMapY;
    protected double tempMapX;
    protected double tempMapY;
    long popuptime = 0L;
    String popupmessage = "";
    ArrayList<Pair<String, SearchResult>> searchResults = new ArrayList<>();
    long t = 0L;
    private boolean isDragging = false;
    private int screenX;
    private int screenY;
    private ResearchEntry currentHighlight;
    private Player player;
    private EditBox searchField;
    public ResearchBrowserScreen() {
        super(Component.translatable("gui.arcaneresearch.thaumonomicon"));
        initPositions(lastX, lastY);
    }
    public ResearchBrowserScreen(double x, double y) {
        super(Component.translatable("gui.arcaneresearch.thaumonomicon"));
        initPositions(x, y);
    }
    public static void drawFullTexture(GuiGraphics graphics, ResourceLocation texture,
                                       int x, int y, int w, int h) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(matrix, x, y + h, 0).uv(0, 1).endVertex();
        builder.vertex(matrix, x + w, y + h, 0).uv(1, 1).endVertex();
        builder.vertex(matrix, x + w, y, 0).uv(1, 0).endVertex();
        builder.vertex(matrix, x, y, 0).uv(0, 0).endVertex();
        BufferUploader.drawWithShader(builder.end());
    }
    public static int[] getTextureSize(ResourceLocation rl) {
        int[] cached = textureSizeCache.get(rl);
        if (cached != null) {
            return cached;
        }
        int[] size = new int[]{16, 16};
        try {
            var res = Minecraft.getInstance().getResourceManager().getResource(rl);
            if (res.isPresent()) {
                try (var in = res.get().open()) {
                    var img = com.mojang.blaze3d.platform.NativeImage.read(in);
                    size[0] = img.getWidth();
                    size[1] = img.getHeight();
                    img.close();
                }
            }
        } catch (Exception ignored) {
        }
        textureSizeCache.put(rl, size);
        return size;
    }
    public static void drawTextureRegion(GuiGraphics graphics, ResourceLocation texture,
                                         int x, int y, int u, int v, int w, int h, int texW, int texH) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        Matrix4f matrix = graphics.pose().last().pose();
        float u0 = u / (float) texW;
        float v0 = v / (float) texH;
        float u1 = (u + w) / (float) texW;
        float v1 = (v + h) / (float) texH;
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(matrix, x, y + h, 0).uv(u0, v1).endVertex();
        builder.vertex(matrix, x + w, y + h, 0).uv(u1, v1).endVertex();
        builder.vertex(matrix, x + w, y, 0).uv(u1, v0).endVertex();
        builder.vertex(matrix, x, y, 0).uv(u0, v0).endVertex();
        BufferUploader.drawWithShader(builder.end());
    }
    public static void drawForbidden(GuiGraphics graphics, double x, double y) {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        int count = Minecraft.getInstance().player.tickCount;
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(770, 1);
        RenderSystem.setShaderColor(0.33f, 0.0f, 0.44f, 0.9f);
        int frame = 160 + count % 32;
        float cell = 2048.0f / 32.0f;
        float u0 = (frame % 32) * cell / 2048.0f;
        float v0 = (frame / 32) * cell / 2048.0f;
        float u1 = u0 + cell / 2048.0f;
        float v1 = v0 + cell / 2048.0f;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, NODE_TEXTURE);
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(matrix, -45, 45, 0).uv(u0, v1).endVertex();
        builder.vertex(matrix, 45, 45, 0).uv(u1, v1).endVertex();
        builder.vertex(matrix, 45, -45, 0).uv(u1, v0).endVertex();
        builder.vertex(matrix, -45, -45, 0).uv(u0, v0).endVertex();
        BufferUploader.drawWithShader(builder.end());
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        graphics.pose().popPose();
    }
    private static boolean hasWarp(ResearchEntry entry) {
        if (entry.getStages() == null) {
            return false;
        }
        for (ResearchStage stage : entry.getStages()) {
            if (stage != null && stage.getWarp() > 0) {
                return true;
            }
        }
        return false;
    }
    private void initPositions(double x, double y) {
        tempMapX = x;
        guiMapX = x;
        curMouseX = x;
        tempMapY = y;
        guiMapY = y;
        curMouseY = y;
        player = Minecraft.getInstance().player;
    }
    @Override
    protected void init() {
        super.init();
        updateResearch();
    }
    public void updateResearch() {
        clearWidgets();
        addRenderableWidget(new IconButton(1, height - 17, 16, 16,
                Component.translatable("tc.search"), this::toggleSearch,
                (g, btn, mx, my) -> {
                    RenderSystem.setShaderTexture(0, TEXTURE);
                    float c = btn.isHovered() ? 1f : 0.8f;
                    RenderSystem.setShaderColor(c, c, c, 1f);
                    g.blit(TEXTURE, btn.getX(), btn.getY(), 160, 16, 16, 16);
                    RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                    if (btn.isHovered()) {
                        g.drawString(font, btn.getMessage().getString(),
                                btn.getX() + 19, btn.getY() + 4, 0xFFFFFF, false);
                    }
                }));
        searchField = new EditBox(font, 20, 20, 89, font.lineHeight, Component.empty());
        searchField.setMaxLength(15);
        searchField.setBordered(false);
        searchField.setVisible(false);
        searchField.setTextColor(0xFFFFFF);
        if (searching) {
            searchField.setVisible(true);
            searchField.setFocused(true);
            searchField.setValue("");
            updateSearch();
        }
        screenX = width - 32;
        screenY = height - 32;
        research.clear();
        if (selectedCategory == null || selectedCategory.isEmpty()) {
            Collection<String> cats = ResearchCategories.researchCategories.keySet();
            if (!cats.isEmpty()) {
                selectedCategory = cats.iterator().next();
            }
        }
        int limit = (int) Math.floor((screenY - 28) / 24.0f);
        addonShift = 0;
        int count = 0;
        categoriesTC.clear();
        categoriesOther.clear();
        for (String rcl : ResearchCategories.researchCategories.keySet()) {
            ResearchCategory rc = ResearchCategories.getResearchCategory(rcl);
            if (rc == null) continue;
            if (rc.researchKey != null && !ArcaneresearchCapabilities.isResearchComplete(player, rc.researchKey)) {
                continue;
            }
            int total = 0;
            int complete = 0;
            for (ResearchEntry res : rc.research.values()) {
                total++;
                if (!res.hasMeta(ResearchEntry.EnumResearchMeta.AUTOUNLOCK)
                        && ArcaneresearchCapabilities.isResearchKnown(player, res.getKey())) {
                    complete++;
                }
            }
            int completion = total > 0 ? (int) (complete / (float) total * 100.0f) : 0;
            boolean isTC = false;
            for (String tcc : ConfigResearch.TC_CATEGORIES) {
                if (tcc.equals(rcl)) {
                    isTC = true;
                    break;
                }
            }
            if (isTC) {
                categoriesTC.add(rcl);
                int yPos = 10 + categoriesTC.size() * 24;
                addRenderableWidget(new CategoryButton(rc, rcl, false, 1, yPos, completion));
            } else {
                count++;
                if (count > limit + catScrollPos) continue;
                if (count - 1 < catScrollPos) continue;
                categoriesOther.add(rcl);
                int yPos = 10 + categoriesOther.size() * 24;
                addRenderableWidget(new CategoryButton(rc, rcl, true, width - 17, yPos, completion));
            }
        }
        if (count > limit || count < catScrollPos) {
            addonShift = (screenY - 28) % 24 / 2;
            addRenderableWidget(new IconButton(width - 14, 20, 10, 11, Component.empty(),
                    () -> scrollCategories(-1),
                    (g, btn, mx, my) -> {
                        RenderSystem.setShaderTexture(0, TEXTURE);
                        float c = btn.isHovered() ? 1f : 0.7f;
                        RenderSystem.setShaderColor(c, c, c, 1f);
                        g.blit(TEXTURE, btn.getX(), btn.getY(), 51, 55, 10, 11);
                        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                    }));
            addRenderableWidget(new IconButton(width - 14, screenY + 1, 10, 11, Component.empty(),
                    () -> scrollCategories(1),
                    (g, btn, mx, my) -> {
                        RenderSystem.setShaderTexture(0, TEXTURE);
                        float c = btn.isHovered() ? 1f : 0.7f;
                        RenderSystem.setShaderColor(c, c, c, 1f);
                        g.blit(TEXTURE, btn.getX(), btn.getY(), 51, 71, 10, 11);
                        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                    }));
        }
        catScrollMax = count - limit;
        if (selectedCategory == null || selectedCategory.isEmpty()) {
            return;
        }
        ResearchCategory currentCat = ResearchCategories.getResearchCategory(selectedCategory);
        if (currentCat != null) {
            research.addAll(currentCat.research.values());
        }
        guiBoundsLeft = 99999;
        guiBoundsTop = 99999;
        guiBoundsRight = -99999;
        guiBoundsBottom = -99999;
        for (ResearchEntry res : research) {
            if (res != null && isVisible(res)) {
                int col = res.getDisplayColumn() * 24;
                int row = res.getDisplayRow() * 24;
                if (col - screenX + 48 < guiBoundsLeft) {
                    guiBoundsLeft = col - screenX + 48;
                }
                if (col - 24 > guiBoundsRight) {
                    guiBoundsRight = col - 24;
                }
                if (row - screenY + 48 < guiBoundsTop) {
                    guiBoundsTop = row - screenY + 48;
                }
                if (row - 24 > guiBoundsBottom) {
                    guiBoundsBottom = row - 24;
                }
            }
        }
        if (lastX == -9999.0 || guiMapX > guiBoundsRight || guiMapX < guiBoundsLeft) {
            double centerX = (guiBoundsLeft + guiBoundsRight) / 2.0;
            tempMapX = centerX;
            guiMapX = centerX;
        }
        if (lastY == -9999.0 || guiMapY > guiBoundsBottom || guiMapY < guiBoundsTop) {
            double centerY = (guiBoundsTop + guiBoundsBottom) / 2.0;
            tempMapY = centerY;
            guiMapY = centerY;
        }
    }
    private void toggleSearch() {
        if (!searching) {
            selectedCategory = "";
            searching = true;
            searchField.setVisible(true);
            searchField.setFocused(true);
            searchField.setValue("");
            updateSearch();
        } else {
            searching = false;
            searchField.setVisible(false);
            searchField.setFocused(false);
            updateResearch();
        }
    }
    private void scrollCategories(int direction) {
        if (direction < 0 && catScrollPos > 0) {
            catScrollPos--;
            updateResearch();
        } else if (direction > 0 && catScrollPos < catScrollMax) {
            catScrollPos++;
            updateResearch();
        }
    }
    private boolean isVisible(ResearchEntry res) {
        if (ArcaneresearchCapabilities.isResearchKnown(player, res.getKey())) {
            return true;
        }
        if (invisible.contains(res.getKey())) {
            return false;
        }
        if (res.hasMeta(ResearchEntry.EnumResearchMeta.HIDDEN) && !canUnlockResearch(res)) {
            return false;
        }
        if (res.getParents() == null && res.hasMeta(ResearchEntry.EnumResearchMeta.HIDDEN)) {
            return false;
        }
        if (res.getParents() != null) {
            for (String r : res.getParents()) {
                String cleanParent = r.startsWith("~") ? r.substring(1) : r;
                if (cleanParent.contains("@")) {
                    cleanParent = cleanParent.substring(0, cleanParent.indexOf("@"));
                }
                ResearchEntry ri = ResearchCategories.getResearch(cleanParent);
                if (ri != null && !isVisible(ri)) {
                    invisible.add(r);
                    return false;
                }
            }
        }
        return true;
    }
    private boolean canUnlockResearch(ResearchEntry res) {
        return ResearchManager.doesPlayerHaveRequisites(player, res.getKey());
    }
    @Override
    public void onClose() {
        lastX = guiMapX;
        lastY = guiMapY;
        super.onClose();
    }
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searching && searchField.keyPressed(keyCode, scanCode, modifiers)) {
            updateSearch();
            return true;
        }
        if (minecraft != null && keyCode == minecraft.options.keyInventory.getKey().getValue()) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searching && searchField.charTyped(codePoint, modifiers)) {
            updateSearch();
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }
    private void updateSearch() {
        searchResults.clear();
        invisible.clear();
        String searchText = searchField.getValue().toLowerCase();
        for (String cat : categoriesTC) {
            String catName = Component.translatable("tc.research_category." + cat).getString();
            if (cat.toLowerCase().contains(searchText)) {
                searchResults.add(Pair.of(catName, new SearchResult(cat, null, true)));
            }
        }
        for (String cat : categoriesOther) {
            String catName = Component.translatable("tc.research_category." + cat).getString();
            if (cat.toLowerCase().contains(searchText)) {
                searchResults.add(Pair.of(catName, new SearchResult(cat, null, true)));
            }
        }
        ArcaneresearchCapabilities.getKnowledge(player).ifPresent(knowledge -> {
            java.util.ArrayList<ResourceLocation> dupCheck = new java.util.ArrayList<>();
            for (String resKey : knowledge.getResearchList()) {
                ResearchEntry ri = ResearchCategories.getResearch(resKey);
                if (ri != null) {
                    String name = ri.getLocalizedName().getString();
                    if (name.toLowerCase().contains(searchText)) {
                        searchResults.add(Pair.of(name, new SearchResult(resKey, null, false)));
                    }
                    if (ri.getStages() != null) {
                        int stage = knowledge.getResearchStage(resKey);
                        int s2 = Math.min(ri.getStages().length - 1, stage + 1);
                        ResearchStage page = ri.getStages()[s2];
                        if (page != null && page.getRecipes() != null
                                && minecraft != null && minecraft.level != null) {
                            for (ResourceLocation rec : page.getRecipes()) {
                                if (dupCheck.contains(rec)) {
                                    continue;
                                }
                                dupCheck.add(rec);
                                Object recipeObject = CommonInternals.getCatalogRecipe(rec);
                                if (recipeObject == null) {
                                    recipeObject = CommonInternals.getCatalogRecipeFake(rec);
                                }
                                ItemStack ro = null;
                                if (recipeObject instanceof net.minecraft.world.item.crafting.Recipe<?> vanilla) {
                                    ro = vanilla.getResultItem(Objects.requireNonNull(minecraft).level.registryAccess());
                                } else if (recipeObject instanceof InfusionRecipe infusion
                                        && infusion.getRecipeOutput() instanceof ItemStack out) {
                                    ro = out;
                                } else if (recipeObject instanceof CrucibleRecipe crucible) {
                                    ro = crucible.getRecipeOutput();
                                } else if (minecraft != null && minecraft.level != null) {
                                    var byKey = minecraft.level.getRecipeManager().byKey(rec);
                                    if (byKey.isPresent()) {
                                        ro = byKey.get().getResultItem(minecraft.level.registryAccess());
                                    }
                                }
                                if (ro != null && !ro.isEmpty() && ro.getHoverName().getString()
                                        .toLowerCase().contains(searchText)) {
                                    searchResults.add(Pair.of(ro.getHoverName().getString(),
                                            new SearchResult(resKey, rec, false)));
                                }
                            }
                        }
                    }
                }
            }
        });
        searchResults.sort(Comparator.comparing(Pair::getLeft));
    }
    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!searching) {
            handleDragging(mouseX, mouseY);
            handleZoom();
        }
        renderBackground(graphics);
        t = System.nanoTime() / 50000000L;
        int locX = Mth.floor(curMouseX + (guiMapX - curMouseX) * partialTick);
        int locY = Mth.floor(curMouseY + (guiMapY - curMouseY) * partialTick);
        if (locX < guiBoundsLeft * screenZoom) {
            locX = (int) (guiBoundsLeft * screenZoom);
        }
        if (locY < guiBoundsTop * screenZoom) {
            locY = (int) (guiBoundsTop * screenZoom);
        }
        if (locX >= guiBoundsRight * screenZoom) {
            locX = (int) (guiBoundsRight * screenZoom - 1.0f);
        }
        if (locY >= guiBoundsBottom * screenZoom) {
            locY = (int) (guiBoundsBottom * screenZoom - 1.0f);
        }
        if (!searching) {
            renderResearchBackground(graphics, locX, locY);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            graphics.pose().pushPose();
            graphics.pose().scale(1.0f / screenZoom, 1.0f / screenZoom, 1.0f);
            renderResearchContent(graphics, mouseX, mouseY, locX, locY);
            graphics.pose().popPose();
            RenderSystem.disableBlend();
            graphics.flush();
        } else {
            renderSearchResults(graphics, mouseX, mouseY);
        }
        renderFrame(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.flush();
        if (currentHighlight != null && !searching) {
            renderResearchTooltip(graphics, mouseX, mouseY);
        }
        if (popuptime > System.currentTimeMillis() && popupmessage != null) {
            graphics.renderTooltip(font, List.of(Component.literal(popupmessage)), Optional.empty(), 10, 34);
        }
        RenderSystem.disableBlend();
    }
    private void handleDragging(int mouseX, int mouseY) {
        if (isDragging && mouseX >= startX && mouseX < startX + screenX
                && mouseY >= startY && mouseY < startY + screenY) {
            guiMapX -= (mouseX - lastMouseX) * screenZoom;
            guiMapY -= (mouseY - lastMouseY) * screenZoom;
            curMouseX = guiMapX;
            tempMapX = guiMapX;
            curMouseY = guiMapY;
            tempMapY = guiMapY;
        }
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        if (tempMapX < guiBoundsLeft * screenZoom) {
            tempMapX = guiBoundsLeft * screenZoom;
        }
        if (tempMapY < guiBoundsTop * screenZoom) {
            tempMapY = guiBoundsTop * screenZoom;
        }
        if (tempMapX >= guiBoundsRight * screenZoom) {
            tempMapX = guiBoundsRight * screenZoom - 1.0f;
        }
        if (tempMapY >= guiBoundsBottom * screenZoom) {
            tempMapY = guiBoundsBottom * screenZoom - 1.0f;
        }
    }
    private void handleZoom() {
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!searching) {
            if (delta < 0) {
                screenZoom += 0.25f;
            } else if (delta > 0) {
                screenZoom -= 0.25f;
            }
            screenZoom = Mth.clamp(screenZoom, 1.0f, 2.0f);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && !searching) {
            isDragging = true;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        popuptime = System.currentTimeMillis() - 1L;
        if (!searching && currentHighlight != null) {
            if (!ArcaneresearchCapabilities.isResearchKnown(player, currentHighlight.getKey())
                    && canUnlockResearch(currentHighlight)) {
                updateResearch();
                PacketHandler.sendToServer(new PacketSyncProgressToServer(currentHighlight.getKey(), true));
                Objects.requireNonNull(minecraft).setScreen(new ResearchPageScreen(currentHighlight, null, guiMapX, guiMapY));
                popuptime = System.currentTimeMillis() + 3000L;
                popupmessage = Component.translatable("tc.research.popup", currentHighlight.getLocalizedName()).getString();
                return true;
            } else if (ArcaneresearchCapabilities.isResearchKnown(player, currentHighlight.getKey())) {
                ArcaneresearchCapabilities.getKnowledge(player).ifPresent(knowledge -> {
                    knowledge.clearResearchFlag(currentHighlight.getKey(), IPlayerKnowledge.EnumResearchFlag.RESEARCH);
                    knowledge.clearResearchFlag(currentHighlight.getKey(), IPlayerKnowledge.EnumResearchFlag.PAGE);
                });
                PacketHandler.sendToServer(new PacketSyncResearchFlagsToServer(currentHighlight.getKey(), false, false, false));
                ArcaneresearchCapabilities.getKnowledge(player).ifPresent(knowledge -> {
                    int stage = knowledge.getResearchStage(currentHighlight.getKey());
                    if (currentHighlight.getStages() != null
                            && stage > 1 && stage >= currentHighlight.getStages().length) {
                        PacketHandler.sendToServer(new PacketSyncProgressToServer(
                                currentHighlight.getKey(), false, true, false));
                    }
                });
                Objects.requireNonNull(minecraft).setScreen(new ResearchPageScreen(currentHighlight, null, guiMapX, guiMapY));
                return true;
            }
        } else if (searching) {
            int q = 0;
            for (Pair<String, SearchResult> p : searchResults) {
                SearchResult sr = p.getRight();
                if (mouseX > 22 && mouseX < 18 + screenX
                        && mouseY >= 32 + q * 10 && mouseY < 40 + q * 10) {
                    if (sr.cat) {
                        searching = false;
                        searchField.setVisible(false);
                        searchField.setFocused(false);
                        selectedCategory = sr.key;
                        updateResearch();
                        double centerX = (guiBoundsLeft + guiBoundsRight) / 2.0;
                        double centerY = (guiBoundsTop + guiBoundsBottom) / 2.0;
                        tempMapX = centerX;
                        guiMapX = centerX;
                        tempMapY = centerY;
                        guiMapY = centerY;
                        return true;
                    } else if (ArcaneresearchCapabilities.isResearchKnown(player, sr.key)) {
                        ResearchEntry entry = ResearchCategories.getResearch(sr.key);
                        if (entry != null) {
                            Objects.requireNonNull(minecraft).setScreen(new ResearchPageScreen(entry, sr.recipe, guiMapX, guiMapY));
                            return true;
                        }
                    }
                }
                q++;
                if (32 + (q + 1) * 10 > screenY) {
                    break;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override
    public void tick() {
        super.tick();
        curMouseX = guiMapX;
        curMouseY = guiMapY;
        double dx = tempMapX - guiMapX;
        double dy = tempMapY - guiMapY;
        if (dx * dx + dy * dy < 4.0) {
            guiMapX += dx;
            guiMapY += dy;
        } else {
            guiMapX += dx * 0.85;
            guiMapY += dy * 0.85;
        }
    }
    private void renderResearchBackground(GuiGraphics graphics, int locX, int locY) {
        if (selectedCategory == null || selectedCategory.isEmpty()) return;
        ResearchCategory cat = ResearchCategories.getResearchCategory(selectedCategory);
        if (cat == null || cat.background == null) return;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, cat.background);
        drawScrollingTexture(graphics, screenX + 4, screenY + 4, locX / 2.0f, locY / 2.0f);
        if (cat.background2 != null) {
            RenderSystem.setShaderTexture(0, cat.background2);
            drawScrollingTexture(graphics, screenX + 4, screenY + 4, locX / 1.5f, locY / 1.5f);
        }
        RenderSystem.disableBlend();
    }
    private void drawScrollingTexture(GuiGraphics graphics, float width, float height, float scrollX, float scrollY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        float texScale = 1.0f / 256.0f;
        buffer.vertex(matrix, (float) 14, (float) 14 + height, 0).uv((scrollX) * texScale, (scrollY + height) * texScale).endVertex();
        buffer.vertex(matrix, (float) 14 + width, (float) 14 + height, 0).uv((scrollX + width) * texScale, (scrollY + height) * texScale).endVertex();
        buffer.vertex(matrix, (float) 14 + width, (float) 14, 0).uv((scrollX + width) * texScale, (scrollY) * texScale).endVertex();
        buffer.vertex(matrix, (float) 14, (float) 14, 0).uv((scrollX) * texScale, (scrollY) * texScale).endVertex();
        BufferUploader.drawWithShader(buffer.end());
    }
    private void renderResearchContent(GuiGraphics graphics, int mouseX, int mouseY, int locX, int locY) {
        if (research.isEmpty()) return;
        RenderSystem.setShaderTexture(0, TEXTURE);
        for (ResearchEntry source : research) {
            if (source.getParents() != null && source.getParents().length > 0) {
                for (int a = 0; a < source.getParents().length; a++) {
                    String parentKey = source.getParents()[a];
                    if (parentKey == null) continue;
                    if (parentKey.contains("||")) {
                        for (String alt : parentKey.split("\\|\\|")) {
                            drawParentLine(graphics, source, alt, locX, locY);
                        }
                        continue;
                    }
                    String cleanParent = source.getParentsClean()[a];
                    ResearchEntry parent = ResearchCategories.getResearch(cleanParent);
                    if (parent == null || !parent.getCategory().equals(selectedCategory)) continue;
                    if (parent.getSiblings() != null && Arrays.asList(parent.getSiblings()).contains(source.getKey())) {
                        continue;
                    }
                    boolean knowsParent = ArcaneresearchCapabilities.isResearchComplete(player, parentKey);
                    boolean visible = isVisible(source) && !parentKey.startsWith("~");
                    if (visible) {
                        if (knowsParent) {
                            drawLine(graphics, source.getDisplayColumn(), source.getDisplayRow(),
                                    parent.getDisplayColumn(), parent.getDisplayRow(),
                                    0.6f, 0.6f, 0.6f, locX, locY, 3.0f, true,
                                    source.hasMeta(ResearchEntry.EnumResearchMeta.REVERSE));
                        } else if (isVisible(parent)) {
                            drawLine(graphics, source.getDisplayColumn(), source.getDisplayRow(),
                                    parent.getDisplayColumn(), parent.getDisplayRow(),
                                    0.2f, 0.2f, 0.2f, locX, locY, 2.0f, true,
                                    source.hasMeta(ResearchEntry.EnumResearchMeta.REVERSE));
                        }
                    }
                }
            }
            if (source.getSiblings() != null) {
                source.getSiblings();
                for (String siblingKey : source.getSiblings()) {
                    if (siblingKey == null) continue;
                    ResearchEntry sibling = ResearchCategories.getResearch(siblingKey);
                    if (sibling == null || !sibling.getCategory().equals(selectedCategory)) continue;
                    boolean knowsSibling = ArcaneresearchCapabilities.isResearchComplete(player, siblingKey);
                    if (isVisible(source) && !siblingKey.startsWith("~")) {
                        if (knowsSibling) {
                            drawLine(graphics, sibling.getDisplayColumn(), sibling.getDisplayRow(),
                                    source.getDisplayColumn(), source.getDisplayRow(),
                                    0.3f, 0.3f, 0.4f, locX, locY, 1.0f, false,
                                    source.hasMeta(ResearchEntry.EnumResearchMeta.REVERSE));
                        } else if (isVisible(sibling)) {
                            drawLine(graphics, sibling.getDisplayColumn(), sibling.getDisplayRow(),
                                    source.getDisplayColumn(), source.getDisplayRow(),
                                    0.1875f, 0.1875f, 0.25f, locX, locY, 0.0f, false,
                                    source.hasMeta(ResearchEntry.EnumResearchMeta.REVERSE));
                        }
                    }
                }
            }
        }
        currentHighlight = null;
        for (ResearchEntry iconResearch : research) {
            int iconPosX = iconResearch.getDisplayColumn() * 24 - locX;
            int iconPosY = iconResearch.getDisplayRow() * 24 - locY;
            if (iconPosX < -24 || iconPosY < -24 || iconPosX > screenX * screenZoom || iconPosY > screenY * screenZoom) {
                continue;
            }
            int iconX = startX + iconPosX;
            int iconY = startY + iconPosY;
            if (isVisible(iconResearch)) {
                if (hasWarp(iconResearch)) {
                    drawForbidden(graphics, iconX + 8, iconY + 8);
                }
                float brightness = 0.3f;
                if (ArcaneresearchCapabilities.isResearchComplete(player, iconResearch.getKey())) {
                    brightness = 1.0f;
                } else if (canUnlockResearch(iconResearch)) {
                    brightness = (float) Math.sin(System.currentTimeMillis() % 600L / 600.0 * Math.PI * 2.0) * 0.25f + 0.75f;
                }
                RenderSystem.setShaderColor(brightness, brightness, brightness, 1.0f);
                RenderSystem.setShaderTexture(0, TEXTURE);
                int frameU = 80;
                int frameV = 48;
                if (iconResearch.hasMeta(ResearchEntry.EnumResearchMeta.HIDDEN)) {
                    frameV += 32;
                }
                if (iconResearch.hasMeta(ResearchEntry.EnumResearchMeta.ROUND)) {
                    frameU = 144;
                } else if (iconResearch.hasMeta(ResearchEntry.EnumResearchMeta.HEX)) {
                    frameU = 112;
                }
                graphics.blit(TEXTURE, iconX - 8, iconY - 8, frameU, frameV, 32, 32);
                if (iconResearch.hasMeta(ResearchEntry.EnumResearchMeta.SPIKY)) {
                    graphics.blit(TEXTURE, iconX - 8, iconY - 8, 176,
                            48 + (iconResearch.hasMeta(ResearchEntry.EnumResearchMeta.HIDDEN) ? 32 : 0), 32, 32);
                }
                ArcaneresearchCapabilities.getKnowledge(player).ifPresent(knowledge -> {
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                    if (knowledge.hasResearchFlag(iconResearch.getKey(), IPlayerKnowledge.EnumResearchFlag.RESEARCH)) {
                        graphics.pose().pushPose();
                        graphics.pose().translate(iconX - 9, iconY - 9, 0);
                        graphics.pose().scale(0.5f, 0.5f, 1.0f);
                        graphics.blit(TEXTURE, 0, 0, 176, 16, 32, 32);
                        graphics.pose().popPose();
                    }
                    if (knowledge.hasResearchFlag(iconResearch.getKey(), IPlayerKnowledge.EnumResearchFlag.PAGE)) {
                        graphics.pose().pushPose();
                        graphics.pose().translate(iconX - 9, iconY + 9, 0);
                        graphics.pose().scale(0.5f, 0.5f, 1.0f);
                        graphics.blit(TEXTURE, 0, 0, 208, 16, 32, 32);
                        graphics.pose().popPose();
                    }
                });
                RenderSystem.setShaderColor(canUnlockResearch(iconResearch) ? 1.0f : 0.1f,
                        canUnlockResearch(iconResearch) ? 1.0f : 0.1f,
                        canUnlockResearch(iconResearch) ? 1.0f : 0.1f, 1.0f);
                drawResearchIcon(graphics, iconResearch, iconX, iconY);
                if (mouseX >= startX && mouseY >= startY && mouseX < startX + screenX && mouseY < startY + screenY
                        && mouseX >= (iconX - 2) / screenZoom && mouseX <= (iconX + 18) / screenZoom
                        && mouseY >= (iconY - 2) / screenZoom && mouseY <= (iconY + 18) / screenZoom) {
                    currentHighlight = iconResearch;
                }
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
    }
    private void drawResearchIcon(GuiGraphics graphics, ResearchEntry research, int x, int y) {
        if (research.getIcons() == null || research.getIcons().length == 0) {
            return;
        }
        int idx = (int) (System.currentTimeMillis() / 1000L % research.getIcons().length);
        Object icon = research.getIcons()[idx];
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (icon instanceof ResourceLocation rl) {
            int[] size = getTextureSize(rl);
            int w = Math.max(size[0], 1);
            int h = Math.max(size[1], 1);
            if (h > w && h % w == 0) {
                int m = h / w;
                int frame = (int) (System.currentTimeMillis() / 150L % m);
                graphics.pose().pushPose();
                graphics.pose().translate(x, y, 0);
                graphics.pose().scale(16.0f / w, 16.0f / w, 1.0f);
                drawTextureRegion(graphics, rl, 0, 0, 0, frame * w, w, w, w, h);
                graphics.pose().popPose();
            } else if (w > h && w % h == 0) {
                int m = w / h;
                int frame = (int) (System.currentTimeMillis() / 150L % m);
                graphics.pose().pushPose();
                graphics.pose().translate(x, y, 0);
                graphics.pose().scale(16.0f / h, 16.0f / h, 1.0f);
                drawTextureRegion(graphics, rl, 0, 0, frame * h, 0, h, h, w, h);
                graphics.pose().popPose();
            } else {
                drawFullTexture(graphics, rl, x, y, 16, 16);
            }
        } else if (icon instanceof ItemStack stack) {
            arcaneresearch.client.gui.GuiRenderHelper.renderItem(graphics,
                    InventoryUtils.cycleItemStack(stack), x, y);
        } else if (icon instanceof String s && s.startsWith("focus:")) {
            IFocusElement fp = FocusEngine.getElement(s.replace("focus:", "").trim());
            if (fp instanceof FocusNode node) {
                FocalManipulatorScreen.drawPart(graphics, node, x + 8, y + 8, 24.0f, canUnlockResearch(research));
            }
        }
    }
    private void drawParentLine(GuiGraphics graphics, ResearchEntry source, String parentKey,
                                int locX, int locY) {
        if (parentKey == null) return;
        String cleanParent = parentKey.startsWith("~") ? parentKey.substring(1) : parentKey;
        if (cleanParent.contains("@")) {
            cleanParent = cleanParent.substring(0, cleanParent.indexOf("@"));
        }
        ResearchEntry parent = ResearchCategories.getResearch(cleanParent);
        if (parent == null || !parent.getCategory().equals(selectedCategory)) return;
        if (parent.getSiblings() != null && Arrays.asList(parent.getSiblings()).contains(source.getKey())) {
            return;
        }
        boolean knowsParent = ArcaneresearchCapabilities.isResearchComplete(player, parentKey);
        boolean visible = isVisible(source) && !parentKey.startsWith("~");
        if (visible) {
            if (knowsParent) {
                drawLine(graphics, source.getDisplayColumn(), source.getDisplayRow(),
                        parent.getDisplayColumn(), parent.getDisplayRow(),
                        0.6f, 0.6f, 0.6f, locX, locY, 3.0f, true,
                        source.hasMeta(ResearchEntry.EnumResearchMeta.REVERSE));
            } else if (isVisible(parent)) {
                drawLine(graphics, source.getDisplayColumn(), source.getDisplayRow(),
                        parent.getDisplayColumn(), parent.getDisplayRow(),
                        0.2f, 0.2f, 0.2f, locX, locY, 3.0f, true,
                        source.hasMeta(ResearchEntry.EnumResearchMeta.REVERSE));
            }
        }
    }
    private void drawLine(GuiGraphics graphics, int x, int y, int x2, int y2,
                          float r, float g, float b, int locX, int locY,
                          float zMod, boolean arrow, boolean flipped) {
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, zMod);
        RenderSystem.setShaderColor(r, g, b, 1.0f);
        boolean bigCorner = false;
        int xd;
        int yd;
        int xm;
        int ym;
        int xx;
        int yy;
        if (flipped) {
            xd = Math.abs(x2 - x);
            yd = Math.abs(y2 - y);
            xm = xd == 0 ? 0 : (x2 - x > 0 ? -1 : 1);
            ym = yd == 0 ? 0 : (y2 - y > 0 ? -1 : 1);
            if (xd > 1 && yd > 1) {
                bigCorner = true;
            }
            xx = x2 * 24 - 4 - locX + startX;
            yy = y2 * 24 - 4 - locY + startY;
        } else {
            xd = Math.abs(x - x2);
            yd = Math.abs(y - y2);
            xm = xd == 0 ? 0 : (x - x2 > 0 ? -1 : 1);
            ym = yd == 0 ? 0 : (y - y2 > 0 ? -1 : 1);
            if (xd > 1 && yd > 1) {
                bigCorner = true;
            }
            xx = x * 24 - 4 - locX + startX;
            yy = y * 24 - 4 - locY + startY;
        }
        if (arrow) {
            if (flipped) {
                int xx2 = x * 24 - 8 - locX + startX;
                int yy2 = y * 24 - 8 - locY + startY;
                if (xm < 0) {
                    graphics.blit(TEXTURE, xx2, yy2, 160, 112, 32, 32);
                } else if (xm > 0) {
                    graphics.blit(TEXTURE, xx2, yy2, 128, 112, 32, 32);
                } else if (ym > 0) {
                    graphics.blit(TEXTURE, xx2, yy2, 64, 112, 32, 32);
                } else if (ym < 0) {
                    graphics.blit(TEXTURE, xx2, yy2, 96, 112, 32, 32);
                }
            } else if (ym < 0) {
                graphics.blit(TEXTURE, xx - 4, yy - 4, 64, 112, 32, 32);
            } else if (ym > 0) {
                graphics.blit(TEXTURE, xx - 4, yy - 4, 96, 112, 32, 32);
            } else if (xm > 0) {
                graphics.blit(TEXTURE, xx - 4, yy - 4, 160, 112, 32, 32);
            } else if (xm < 0) {
                graphics.blit(TEXTURE, xx - 4, yy - 4, 128, 112, 32, 32);
            }
        }
        int v = 1;
        int h = 0;
        while (v < yd - (bigCorner ? 1 : 0)) {
            graphics.blit(TEXTURE, xx + xm * 24 * h, yy + ym * 24 * v, 0, 228, 24, 24);
            ++v;
        }
        if (bigCorner) {
            if (xm < 0 && ym > 0) {
                graphics.blit(TEXTURE, xx + xm * 24 * h - 24, yy + ym * 24 * v, 0, 180, 48, 48);
            }
            if (xm > 0 && ym > 0) {
                graphics.blit(TEXTURE, xx + xm * 24 * h, yy + ym * 24 * v, 48, 180, 48, 48);
            }
            if (xm < 0 && ym < 0) {
                graphics.blit(TEXTURE, xx + xm * 24 * h - 24, yy + ym * 24 * v - 24, 96, 180, 48, 48);
            }
            if (xm > 0 && ym < 0) {
                graphics.blit(TEXTURE, xx + xm * 24 * h, yy + ym * 24 * v - 24, 144, 180, 48, 48);
            }
        } else {
            if (xm < 0 && ym > 0) {
                graphics.blit(TEXTURE, xx + xm * 24 * h, yy + ym * 24 * v, 48, 228, 24, 24);
            }
            if (xm > 0 && ym > 0) {
                graphics.blit(TEXTURE, xx + xm * 24 * h, yy + ym * 24 * v, 72, 228, 24, 24);
            }
            if (xm < 0 && ym < 0) {
                graphics.blit(TEXTURE, xx + xm * 24 * h, yy + ym * 24 * v, 96, 228, 24, 24);
            }
            if (xm > 0 && ym < 0) {
                graphics.blit(TEXTURE, xx + xm * 24 * h, yy + ym * 24 * v, 120, 228, 24, 24);
            }
        }
        v += bigCorner ? 1 : 0;
        for (h += bigCorner ? 2 : 1; h < xd; ++h) {
            graphics.blit(TEXTURE, xx + xm * 24 * h, yy + ym * 24 * v, 24, 228, 24, 24);
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        graphics.pose().popPose();
    }
    private void renderFrame(GuiGraphics graphics) {
        RenderSystem.disableDepthTest();
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        for (int c = 16; c < width - 16; c += 64) {
            int p = Math.min(64, width - 16 - c);
            if (p > 0) {
                graphics.blit(TEXTURE, c, -2, 48, 13, p, 22);
                graphics.blit(TEXTURE, c, height - 20, 48, 13, p, 22);
            }
        }
        for (int c = 16; c < height - 16; c += 64) {
            int p = Math.min(64, height - 16 - c);
            if (p > 0) {
                graphics.blit(TEXTURE, -2, c, 13, 48, 22, p);
                graphics.blit(TEXTURE, width - 20, c, 13, 48, 22, p);
            }
        }
        graphics.blit(TEXTURE, -2, -2, 13, 13, 22, 22);
        graphics.blit(TEXTURE, -2, height - 20, 13, 13, 22, 22);
        graphics.blit(TEXTURE, width - 20, -2, 13, 13, 22, 22);
        graphics.blit(TEXTURE, width - 20, height - 20, 13, 13, 22, 22);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }
    private void renderSearchResults(GuiGraphics graphics, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        searchField.render(graphics, mouseX, mouseY, 0);
        int q = 0;
        for (Pair<String, SearchResult> p : searchResults) {
            SearchResult sr = p.getRight();
            int color = sr.cat ? 0xDDCF9A : (sr.recipe == null ? 0xDDDDDD : 0xAAAAAA);
            if (mouseX > 22 && mouseX < 18 + screenX
                    && mouseY >= 32 + q * 10 && mouseY < 40 + q * 10) {
                color = sr.recipe == null ? 0xFFFFFF : (sr.cat ? 0xFFDD6C : 0xCCFFFF);
            }
            if (sr.recipe != null) {
                graphics.pose().pushPose();
                graphics.pose().scale(0.5f, 0.5f, 1.0f);
                graphics.blit(TEXTURE, 44, (32 + q * 10) * 2, 224, 48, 16, 16);
                graphics.pose().popPose();
            }
            graphics.drawString(font, p.getLeft(), 32, 32 + q * 10, color);
            q++;
            if (32 + (q + 1) * 10 > screenY) {
                graphics.drawString(font, Component.translatable("tc.search.more").getString(), 22, 34 + q * 10, 0xAAAAAA);
                break;
            }
        }
    }
    private void renderResearchTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("§6" + currentHighlight.getLocalizedName().getString()));
        if (canUnlockResearch(currentHighlight)) {
            if (!ArcaneresearchCapabilities.isResearchComplete(player, currentHighlight.getKey())
                    && currentHighlight.getStages() != null) {
                ArcaneresearchCapabilities.getKnowledge(player).ifPresent(knowledge -> {
                    int stage = knowledge.getResearchStage(currentHighlight.getKey());
                    if (stage > 0) {
                        lines.add(Component.literal("§b" + Component.translatable("tc.research.stage").getString()
                                + " " + stage + "/" + currentHighlight.getStages().length));
                    } else {
                        lines.add(Component.literal("§a" + Component.translatable("tc.research.begin").getString()));
                    }
                });
            }
        } else {
            lines.add(Component.literal("§c" + Component.translatable("tc.researchmissing").getString()));
            if (currentHighlight.getParents() != null) {
                int idx = 0;
                for (String parent : currentHighlight.getParents()) {
                    if (!ArcaneresearchCapabilities.isResearchComplete(player, parent)) {
                        String name = "?";
                        try {
                            String cleanParent = currentHighlight.getParentsClean()[idx];
                            ResearchEntry parentEntry = ResearchCategories.getResearch(cleanParent);
                            if (parentEntry != null) {
                                name = parentEntry.getLocalizedName().getString();
                            }
                        } catch (Exception ignored) {
                        }
                        lines.add(Component.literal("§e - " + name));
                    }
                    idx++;
                }
            }
        }
        ArcaneresearchCapabilities.getKnowledge(player).ifPresent(knowledge -> {
            if (knowledge.hasResearchFlag(currentHighlight.getKey(), IPlayerKnowledge.EnumResearchFlag.RESEARCH)) {
                lines.add(Component.translatable("tc.research.newresearch"));
            }
            if (knowledge.hasResearchFlag(currentHighlight.getKey(), IPlayerKnowledge.EnumResearchFlag.PAGE)) {
                lines.add(Component.translatable("tc.research.newpage"));
            }
        });
        graphics.renderTooltip(font, lines, Optional.empty(), mouseX + 3, mouseY - 3);
    }
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    private static class SearchResult {
        String key;
        ResourceLocation recipe;
        boolean cat;
        SearchResult(String key, ResourceLocation recipe, boolean cat) {
            this.key = key;
            this.recipe = recipe;
            this.cat = cat;
        }
    }
    private static class IconButton extends Button {
        private final IconRenderer renderer;
        IconButton(int x, int y, int w, int h, Component message, Runnable action, IconRenderer renderer) {
            super(x, y, w, h, message, b -> action.run(), Button.DEFAULT_NARRATION);
            this.renderer = renderer;
        }
        @Override
        public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            this.isHovered = mouseX >= this.getX() && mouseY >= this.getY()
                    && mouseX < this.getX() + this.getWidth()
                    && mouseY < this.getY() + this.getHeight();
            RenderSystem.enableBlend();
            renderer.render(graphics, this, mouseX, mouseY);
        }
        interface IconRenderer {
            void render(GuiGraphics graphics, Button button, int mouseX, int mouseY);
        }
    }
    private class CategoryButton extends Button {
        private final ResearchCategory category;
        private final String catKey;
        private final boolean isRightSide;
        private final int completion;
        public CategoryButton(ResearchCategory category, String key, boolean rightSide, int x, int y, int completion) {
            super(x, y, 16, 16, Component.translatable("tc.research_category." + key),
                    b -> {
                    }, Button.DEFAULT_NARRATION);
            this.category = category;
            this.catKey = key;
            this.isRightSide = rightSide;
            this.completion = completion;
        }
        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return this.active && this.visible
                    && mouseX >= (double) this.getX()
                    && mouseY >= (double) (this.getY() + addonShift)
                    && mouseX < (double) (this.getX() + this.getWidth())
                    && mouseY < (double) (this.getY() + addonShift + this.getHeight());
        }
        @Override
        public void onPress() {
            searching = false;
            searchField.setVisible(false);
            searchField.setFocused(false);
            selectedCategory = catKey;
            updateResearch();
            double centerX = (guiBoundsLeft + guiBoundsRight) / 2.0;
            double centerY = (guiBoundsTop + guiBoundsBottom) / 2.0;
            tempMapX = centerX;
            guiMapX = centerX;
            tempMapY = centerY;
            guiMapY = centerY;
        }
        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            RenderSystem.enableBlend();
            RenderSystem.setShaderTexture(0, TEXTURE);
            float frameColor = selectedCategory != null && selectedCategory.equals(catKey) ? 0.6f : 1.0f;
            RenderSystem.setShaderColor(frameColor, 1.0f, 1.0f, 1.0f);
            graphics.blit(TEXTURE, getX() - 3, getY() - 3 + addonShift, 13, 13, 22, 22);
            if (category.icon != null) {
                float iconBrightness = (selectedCategory != null && selectedCategory.equals(catKey)) || isHovered ? 1.0f : 0.66f;
                RenderSystem.setShaderColor(iconBrightness, iconBrightness, iconBrightness, isHovered ? 1.0f : 0.8f);
                if ("BASICS".equals(catKey)) {
                    arcaneresearch.client.gui.GuiRenderHelper.renderItem(graphics,
                            new ItemStack(arcaneresearch.init.ModItems.THAUMONOMICON.get()),
                            getX(), getY() + addonShift);
                } else {
                    drawFullTexture(graphics, category.icon, getX(), getY() + addonShift, 16, 16);
                }
            }
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            boolean catNewResearch = false;
            boolean catNewPage = false;
            if (category.research != null && player != null) {
                for (String rk : category.research.keySet()) {
                    if (ArcaneresearchCapabilities.isResearchKnown(player, rk)) {
                        boolean[] flags = getCategoryFlags(rk);
                        catNewResearch |= flags[0];
                        catNewPage |= flags[1];
                        if (catNewResearch && catNewPage) {
                            break;
                        }
                    }
                }
            }
            if (catNewResearch) {
                graphics.pose().pushPose();
                graphics.pose().translate(getX() - 2, getY() + addonShift - 2, 0);
                graphics.pose().scale(0.25f, 0.25f, 1.0f);
                graphics.blit(TEXTURE, 0, 0, 176, 16, 32, 32);
                graphics.pose().popPose();
            }
            if (catNewPage) {
                graphics.pose().pushPose();
                graphics.pose().translate(getX() - 2, getY() + addonShift + 9, 0);
                graphics.pose().scale(0.25f, 0.25f, 1.0f);
                graphics.blit(TEXTURE, 0, 0, 208, 16, 32, 32);
                graphics.pose().popPose();
            }
            if (isHovered) {
                String displayText = getMessage().getString() + " (" + completion + "%)";
                int textX = isRightSide ? (screenX + 9 - font.width(displayText)) : (getX() + 22);
                graphics.drawString(font, displayText, textX, getY() + 4 + addonShift, 0xFFFFFF);
                int t = 9;
                if (catNewResearch) {
                    String line = Component.translatable("tc.research.newresearch").getString();
                    int lx = isRightSide ? (screenX + 9 - font.width(line)) : (getX() + 22);
                    graphics.drawString(font, line, lx, getY() + 4 + t + addonShift, 0xFFFFFF);
                    t += 9;
                }
                if (catNewPage) {
                    String line = Component.translatable("tc.research.newpage").getString();
                    int lx = isRightSide ? (screenX + 9 - font.width(line)) : (getX() + 22);
                    graphics.drawString(font, line, lx, getY() + 4 + t + addonShift, 0xFFFFFF);
                }
            }
            RenderSystem.disableBlend();
        }
        private boolean[] getCategoryFlags(String researchKey) {
            boolean[] out = new boolean[2];
            ArcaneresearchCapabilities.getKnowledge(player).ifPresent(knowledge -> {
                out[0] = knowledge.hasResearchFlag(researchKey, IPlayerKnowledge.EnumResearchFlag.RESEARCH);
                out[1] = knowledge.hasResearchFlag(researchKey, IPlayerKnowledge.EnumResearchFlag.PAGE);
            });
            return out;
        }
    }
}
