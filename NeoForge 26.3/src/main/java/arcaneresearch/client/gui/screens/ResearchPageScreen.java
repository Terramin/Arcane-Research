package arcaneresearch.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor; // NEOFORGE_PORT: was GuiGraphics
import net.minecraft.client.renderer.RenderPipelines; // NEOFORGE_PORT: blit needs a pipeline
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was ResourceLocation
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.crafting.CrucibleRecipe;
import arcaneresearch.api.crafting.IArcaneRecipe;
import arcaneresearch.api.crafting.InfusionRecipe;
import arcaneresearch.api.internal.CommonInternals;
import arcaneresearch.api.research.*;
import arcaneresearch.client.gui.RecipeGroups;
import arcaneresearch.client.gui.RecipeRenderer;
import arcaneresearch.client.lib.AspectRenderer;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.common.lib.crafting.CrucibleRecipeType;
import arcaneresearch.common.lib.crafting.InfusionRecipeType;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.playerdata.PacketSyncProgressToServer;
import arcaneresearch.common.lib.utils.InventoryUtils;
import arcaneresearch.init.ModSounds;

import java.util.*;

public class ResearchPageScreen extends Screen {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_researchbook.png");
    private static final Identifier OVERLAY = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_researchbook_overlay.png");

    private static final int PANE_WIDTH = 256;
    private static final int PANE_HEIGHT = 181;
    private static final int PAGE_WIDTH = 140;
    private static final int PAGE_HEIGHT = 210;
    private static final Deque<Identifier> history = new ArrayDeque<>();
    private static final Map<Integer, String> keyCache = new HashMap<>();
    private static final Identifier DUMMY_RESEARCH =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/aspects/_unknown.png");
    private static final Identifier DUMMY_MAP =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/research/rd_map.png");
    private static final Identifier DUMMY_CHEST =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/research/rd_chest.png");
    private static final Identifier DUMMY_FLASK =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/research/rd_flask.png");
    private static final Identifier PAPER_TEX =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/paper.png");
    private static final Identifier BACK_TEX =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/aspects/_back.png");
    private static final PageImage PILINE =
            PageImage.parse("arcaneresearch:textures/gui/gui_researchbook.png:24:184:95:6:1");
    private static final PageImage PIDIV =
            PageImage.parse("arcaneresearch:textures/gui/gui_researchbook.png:28:192:140:6:1");
    private static Identifier shownRecipe = null;
    private static int aspectsPage = 0;
    private final ResearchEntry research;
    private final Identifier highlightRecipe;
    private final double returnX;
    private final double returnY;
    private final LinkedHashMap<Identifier, List<Object>> recipeLists = new LinkedHashMap<>();
    private final LinkedHashMap<Identifier, List<Object>> recipeOutputs = new LinkedHashMap<>();
    private final LinkedHashMap<Identifier, List<Object>> drilldownLists = new LinkedHashMap<>();
    private final ArrayList<Page> pages = new ArrayList<>();
    private Player player;
    private IPlayerKnowledge playerKnowledge;
    private int currentStage = 0;
    private int page = 0;
    private int maxPages = 1;
    private boolean isComplete = false;
    private boolean hasAllRequisites = false;
    private boolean[] hasItem;
    private boolean[] hasCraft;
    private boolean[] hasResearch;
    private boolean[] hasKnow;
    private boolean[] hasDiscovery;
    /** Текст стадии зашифрован: требует непредъявленное открытие. */
    private boolean discoveryLocked = false;
    private boolean showingAspects = false;
    private boolean showingKnowledge = false;
    private int recipePage = 0;
    private int recipePageMax = 0;
    private boolean hasRecipePages = false;
    private boolean renderingCompound = false;
    private boolean allowWithPagePopup = false;
    private int cycle = 0;
    private long lastCycle = 0L;
    private int rhash = 0;
    private List<Aspect> knownPlayerAspects = new ArrayList<>();
    private int maxAspectPages = 0;
    private int reqBtnX = -1;
    private int reqBtnY = -1;
    private boolean reqBtnVisible = false;
    private boolean hold = false;
    private String holdStageKey = null;
    private long lastCheck = 0L;

    public ResearchPageScreen(ResearchEntry research, Identifier highlightRecipe, double returnX, double returnY) {
        super(Component.translatable(research.getName()));
        this.research = research;
        this.highlightRecipe = highlightRecipe;
        this.returnX = returnX;
        this.returnY = returnY;
    }

    private static List<String> splitKeep(String text, String delim) {
        List<String> out = new ArrayList<>();
        int idx = 0;
        int pos;
        while ((pos = text.indexOf(delim, idx)) >= 0) {
            out.add(text.substring(idx, pos));
            out.add(delim);
            idx = pos + delim.length();
        }
        out.add(text.substring(idx));
        return out;
    }

    private static void blitBlend() {
        // NEOFORGE_PORT: blend state is pipeline-owned in 26.x; kept as no-op
        // so Forge 1:1 call sites (checkmark overlays) still read the same.
    }

    public static String findCraftingRecipe(Player player, ItemStack stack) {
        if (stack.isEmpty() || player == null) {
            return null;
        }
        // NEOFORGE_PORT: ItemStack.serializeNBT gone in 26.x -> stable key from
        // item id + components hash (cache key only, format not persisted).
        int key = (stack.getItem().toString() + "#" + stack.getComponents().hashCode()).hashCode();
        if (keyCache.containsKey(key)) {
            return keyCache.get(key);
        }
        for (ResearchCategory rcl : ResearchCategories.researchCategories.values()) {
            for (ResearchEntry ri : rcl.research.values()) {
                if (ri.getStages() == null) {
                    continue;
                }
                for (ResearchStage stage : ri.getStages()) {
                    if (stage.getRecipes() == null) {
                        continue;
                    }
                    for (Identifier rec : stage.getRecipes()) {
                        List<Identifier> members = RecipeGroups.expand(rec);
                        for (int g = 0; g < members.size(); g++) {
                            int result = findRecipePage(player, members.get(g), stack);
                            if (result != -1) {
                                // Рецепт есть, но исследование закрыто: clickthrough
                                // не даём (потребитель всё равно игнорирует ref без ';').
                                // Раньше тут был Identifier.fromNamespaceAndPath(MODID, "UNKNOWN") —
                                // краш: заглавные буквы запрещены в путях 1.20.
                                if (result == -99) {
                                    keyCache.put(key, null);
                                    return null;
                                }
                                String s = rec + ";" + g;
                                keyCache.put(key, s);
                                return s;
                            }
                        }
                    }
                }
            }
        }
        keyCache.put(key, null);
        return null;
    }

    private static int findRecipePage(Player player, Identifier rk, ItemStack stack) {
        Object recipe = CommonInternals.getCatalogRecipe(rk);
        if (recipe == null) {
            recipe = CommonInternals.getCatalogRecipeFake(rk);
        }
        var level = Minecraft.getInstance().level;
        if (recipe == null && level != null) {
            // NEOFORGE_PORT: client getRecipeManager gone -> ClientRecipeCache (unwrap holder).
            var byKey = arcaneresearch.common.lib.crafting.ClientRecipeCache.byKey(rk);
            if (byKey.isPresent()) {
                recipe = byKey.get().value();
            }
        }
        if (recipe == null) {
            return -1;
        }
        if (recipe instanceof CrucibleRecipe crucible) {
            if (!crucible.getRecipeOutput().is(stack.getItem())) {
                return -1;
            }
            return gateByResearch(player, crucible.getResearch());
        }
        if (recipe instanceof arcaneresearch.common.lib.crafting.CrucibleRecipeType crucibleType) {
            if (!crucibleType.getResultItem().is(stack.getItem())) {
                return -1;
            }
            return gateByResearch(player, crucibleType.getResearch());
        }
        if (recipe instanceof InfusionRecipe infusion
                && infusion.getRecipeOutput() instanceof ItemStack out) {
            if (!out.is(stack.getItem())) {
                return -1;
            }
            return gateByResearch(player, infusion.getResearch());
        }
        if (recipe instanceof arcaneresearch.common.lib.crafting.InfusionRecipeType infusionType) {
            if (!infusionType.getResultItem().is(stack.getItem())) {
                return -1;
            }
            return gateByResearch(player, infusionType.getResearch());
        }
        if (recipe instanceof IArcaneRecipe arcane && level != null) {
            // NEOFORGE_PORT: IArcaneRecipe.getResultItem() is no-arg in 26.x.
            if (!arcane.getResultItem().is(stack.getItem())) {
                return -1;
            }
            return gateByResearch(player, arcane.getResearch());
        }
        if (recipe instanceof net.minecraft.world.item.crafting.CraftingRecipe crafting && level != null) {
            // NEOFORGE_PORT: CraftingRecipe.getResultItem(RegistryAccess) gone ->
            // display API (null = unresolvable, skip like Forge's EMPTY).
            net.minecraft.world.item.ItemStack craftingOut =
                    arcaneresearch.common.lib.crafting.ClientRecipeCache.displayResultStack(crafting);
            if (craftingOut == null || craftingOut.isEmpty() || !craftingOut.is(stack.getItem())) {
                return -1;
            }
            return 0;
        }
        if (recipe instanceof arcaneresearch.api.crafting.RecipeMisc misc) {
            if (!misc.getOutput().is(stack.getItem())) {
                return -1;
            }
            return 0;
        }
        if (recipe instanceof ArcaneresearchApi.BluePrint blueprint) {
            ItemStack display = blueprint.getDisplayStack();
            if (display == null || display.isEmpty() || !display.is(stack.getItem())) {
                return -1;
            }
            return gateByResearch(player, blueprint.getResearch());
        }
        return -1;
    }

    private static int gateByResearch(Player player, String researchKey) {
        if (researchKey != null && player != null
                && !ArcaneresearchCapabilities.isResearchComplete(player, researchKey)) {
            return -99;
        }
        return 0;
    }

    private static ItemStack iconSegmentStack(String icon) {
        if (icon == null || icon.isEmpty()) {
            return null;
        }
        try {
            net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM
                    .getValue(Identifier.parse(icon));
            if (item != null && item != net.minecraft.world.item.Items.AIR) {
                return new ItemStack(item);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String requirementName(Object o) {
        if (o instanceof ItemStack stack) {
            return stack.getHoverName().getString();
        } else if (o instanceof String tagName) {
            return tagName;
        }
        return "Item";
    }

    @Override
    protected void init() {
        super.init();

        player = Objects.requireNonNull(minecraft).player;
        // NEOFORGE_PORT: getKnowledge returns @Nullable directly (no Optional).
        playerKnowledge = ArcaneresearchCapabilities.getKnowledge(Objects.requireNonNull(player));

        parsePages();

        knownPlayerAspects = new ArrayList<>();
        for (Aspect a : Aspect.aspects.values()) {
            if (ArcaneresearchCapabilities.isResearchKnown(player, "!" + a.getTag().toLowerCase())) {
                knownPlayerAspects.add(a);
            }
        }
        knownPlayerAspects.sort(Comparator.comparing(Aspect::getName));
        maxAspectPages = (int) Math.ceil(knownPlayerAspects.size() / 5.0);

        if (highlightRecipe != null) {
            shownRecipe = highlightRecipe;
            recipePage = 0;
        }
    }

    private void parsePages() {
        pages.clear();

        if (playerKnowledge == null) {
            return;
        }

        ResearchStage[] stages = research.getStages();
        if (stages == null || stages.length == 0) {

            Page titlePage = new Page();
            titlePage.contents.add(Component.translatable("tc.research.nostages").getString());
            pages.add(titlePage);
            maxPages = 1;
            return;
        }

        currentStage = playerKnowledge.getResearchStage(research.getKey()) - 1;
        boolean complete = false;
        while (currentStage >= stages.length) {
            currentStage--;
            complete = true;
        }
        if (currentStage < 0) {
            currentStage = 0;
        }

        String stageKey = research.getKey() + "@" + currentStage;
        if (!stageKey.equals(holdStageKey)) {
            holdStageKey = stageKey;
            hold = false;
        }

        isComplete = playerKnowledge.isResearchComplete(research.getKey());

        checkRequisites();

        ResearchStage stage = stages[currentStage];
        discoveryLocked = false;
        if (!isComplete && stage != null && stage.getDiscovery() != null) {
            for (String d : stage.getDiscovery()) {
                if (d != null && !arcaneresearch.discovered.note.DiscoveredNotes.hasDiscovery(player, d)) {
                    discoveryLocked = true;
                    break;
                }
            }
        }
        ResearchAddendum[] addenda = null;
        if (research.getAddenda() != null && complete) {
            addenda = research.getAddenda();
        }
        generateRecipesLists(stage, addenda);

        StringBuilder rawText = new StringBuilder(stage.getText() == null ? ""
                : Component.translatable(stage.getText()).getString());
        if (addenda != null) {
            int ac = 0;
            for (ResearchAddendum addendum : addenda) {
                if (knowsAllResearch(addendum.getResearch())) {
                    ac++;
                    String header = Component.translatable("tc.addendumtext", ac).getString();
                    String body = addendum.getText() == null ? ""
                            : Component.translatable(addendum.getText()).getString();
                    rawText.append("<PAGE>").append(header).append("<BR>").append(body);
                }
            }
        }
        pages.addAll(parseTextPages(rawText.toString()));

        if (stage != null && stage.getStructure() != null) {
            PageStructure struct = PageStructure.parse(stage.getStructure());
            if (struct != null) {
                Page structPage = new Page();
                structPage.contents.add(struct);
                pages.add(structPage);
            }
        }

        if (pages.isEmpty()) {
            Page emptyPage = new Page();
            emptyPage.contents.add(Component.translatable("tc.research.empty").getString());
            pages.add(emptyPage);
        }

        rhash = research.getKey().hashCode() + currentStage * 50;
        maxPages = pages.size();
    }

    private boolean knowsAllResearch(String[] keys) {
        if (keys == null) {
            return true;
        }
        for (String key : keys) {
            if (!ArcaneresearchCapabilities.isResearchComplete(player, key)) {
                return false;
            }
        }
        return true;
    }

    private void generateRecipesLists(ResearchStage stage, ResearchAddendum[] addenda) {
        recipeLists.clear();
        recipeOutputs.clear();
        if (stage == null || stage.getRecipes() == null) {
            return;
        }
        for (Identifier rk : stage.getRecipes()) {
            addRecipesToList(rk, recipeLists, recipeOutputs, rk);
        }
        if (addenda == null) {
            return;
        }
        for (ResearchAddendum addendum : addenda) {
            if (addendum.getRecipes() != null && knowsAllResearch(addendum.getResearch())) {
                for (Identifier rk : addendum.getRecipes()) {
                    addRecipesToList(rk, recipeLists, recipeOutputs, rk);
                }
            }
        }
    }

    private void addRecipesToList(Identifier rk,
                                  LinkedHashMap<Identifier, List<Object>> lists,
                                  LinkedHashMap<Identifier, List<Object>> outputs,
                                  Identifier rkey) {
        Object recipe = CommonInternals.getCatalogRecipe(rk);
        if (recipe == null) {
            recipe = CommonInternals.getCatalogRecipeFake(rk);
        }
        if (recipe == null && minecraft != null && minecraft.level != null) {
            // NEOFORGE_PORT: client getRecipeManager gone -> ClientRecipeCache (unwrap holder).
            var byKey = arcaneresearch.common.lib.crafting.ClientRecipeCache.byKey(rk);
            if (byKey.isPresent()) {
                recipe = byKey.get().value();
            }
        }
        if (recipe == null) {

            if (RecipeGroups.isGroup(rk)) {
                for (Identifier rl : RecipeGroups.getGroupRecipes(rk)) {
                    addRecipesToList(rl, lists, outputs, rk);
                }
            }
            return;
        }
        if (RecipeGroups.isGroup(rk)) {
            for (Identifier rl : RecipeGroups.getGroupRecipes(rk)) {
                addRecipesToList(rl, lists, outputs, rk);
            }
            return;
        }
        if (!lists.containsKey(rkey)) {
            lists.put(rkey, new ArrayList<>());
            outputs.put(rkey, new ArrayList<>());
        }
        List<Object> list = lists.get(rkey);
        List<Object> outs = outputs.get(rkey);
        if (recipe instanceof ArcaneresearchApi.BluePrint bp) {
            if (!isGated(bp.getResearch())) {
                list.add(bp);
                outs.add(bp.getDisplayStack() != null ? bp.getDisplayStack() : bp.getParts());
            }
        } else if (recipe instanceof CrucibleRecipeType crucibleType) {
            ItemStack catalyst = InventoryUtils.cycleItemStack(crucibleType.getCatalyst(), 0);
            if (catalyst != null && !catalyst.isEmpty() && !isGated(crucibleType.getResearch())) {
                list.add(crucibleType);
                outs.add(crucibleType.getResultItem());
            }
        } else if (recipe instanceof InfusionRecipeType infusionType) {
            ItemStack out = infusionType.getResultItem();
            if (out != null && !out.isEmpty() && !isGated(infusionType.getResearch())) {
                list.add(infusionType);
                outs.add(out);
            }
        } else if (recipe instanceof CrucibleRecipe crucible) {
            ItemStack catalyst = InventoryUtils.cycleItemStack(crucible.getCatalyst(), 0);
            if (catalyst != null && !catalyst.isEmpty() && !isGated(crucible.getResearch())) {
                list.add(crucible);
                outs.add(crucible.getRecipeOutput());
            }
        } else if (recipe instanceof InfusionRecipe infusion) {

            ItemStack is = null;
            try {
                ItemStack base = InventoryUtils.cycleItemStack(
                        infusion.getRecipeInput(), 0).copy();
                NonNullList<net.minecraft.world.item.crafting.Ingredient> il =
                        infusion.getComponents();
                List<ItemStack> cl = new ArrayList<>();
                for (net.minecraft.world.item.crafting.Ingredient ing : il) {
                    cl.add(InventoryUtils.cycleItemStack(ing, 0));
                }
                Object out = infusion.getRecipeOutput(player, base, cl);
                is = InventoryUtils.cycleItemStack(out, 0);
                if (is == null && infusion.getRecipeOutput() instanceof ItemStack outStack) {
                    is = InventoryUtils.cycleItemStack(outStack, 0);
                }
                if (is == null) {
                    is = base.copy();
                    // NEOFORGE_PORT: ItemStack.addTagElement gone -> CUSTOM_DATA merge.
                    Object[] obj = (Object[]) infusion.getRecipeOutput();
                    if (obj[0] instanceof String tagKey && obj[1] instanceof net.minecraft.nbt.Tag tagVal) {
                        net.minecraft.nbt.CompoundTag custom =
                                arcaneresearch.api.ArcaneresearchInvHelper.getOrCreateCustomTag(is);
                        custom.put(tagKey, tagVal.copy());
                        arcaneresearch.api.ArcaneresearchInvHelper.setCustomTag(is, custom);
                    }
                }
            } catch (Exception ignored) {
            }
            if (is != null && !isGated(infusion.getResearch())) {
                list.add(infusion);
                outs.add(is);
            }
        } else if (recipe instanceof IArcaneRecipe arcane && minecraft != null && minecraft.level != null) {
            // NEOFORGE_PORT: IArcaneRecipe.getResultItem() is no-arg in 26.x.
            ItemStack is = InventoryUtils.cycleItemStack(
                    arcane.getResultItem(), 0);
            if (is != null && !is.isEmpty() && !isGated(arcane.getResearch())) {
                list.add(arcane);
                outs.add(arcane.getResultItem());
            }
        } else if (recipe instanceof net.minecraft.world.item.crafting.CraftingRecipe crafting
                && minecraft != null && minecraft.level != null) {
            // NEOFORGE_PORT: CraftingRecipe.getResultItem(RegistryAccess) gone -> display API
            // (Forge parity: getResultItem never returns null, EMPTY at worst — a null hole
            // in outs desyncs drawBookmarks tabs from mouseClicked zones and opens wrong/empty modals).
            net.minecraft.world.item.ItemStack craftingOut =
                    arcaneresearch.common.lib.crafting.ClientRecipeCache.displayResultStack(crafting);
            list.add(crafting);
            outs.add(craftingOut != null ? craftingOut : net.minecraft.world.item.ItemStack.EMPTY);
        } else if (recipe instanceof arcaneresearch.api.crafting.RecipeMisc misc) {
            list.add(misc);
            outs.add(misc.getOutput());
        }
    }

    private boolean isGated(String researchKey) {

        return researchKey != null && player != null
                && !arcaneresearch.api.capabilities.ArcaneresearchCapabilities.knowsResearchStrict(player, researchKey);
    }

    private List<Page> parseTextPages(String rawText) {
        List<Page> out = new ArrayList<>();
        if (rawText == null) {
            return out;
        }
        String t = rawText;
        t = t.replace("<BR/>", "~B\n\n").replace("<BR>", "~B\n\n");
        t = t.replace("<LINE/>", "~L").replace("<LINE>", "~L");
        t = t.replace("<DIV/>", "~D").replace("<DIV>", "~D");
        t = t.replace("<PAGE/>", "~P").replace("<PAGE>", "~P");

        ArrayList<PageImage> images = new ArrayList<>();
        String[] imgSplit = t.split("<IMG>", -1);
        StringBuilder rebuilt = new StringBuilder(imgSplit[0]);
        for (int i = 1; i < imgSplit.length; i++) {
            String s = imgSplit[i];
            int end = s.indexOf("</IMG>");
            if (end >= 0) {
                PageImage pi = PageImage.parse(s.substring(0, end));
                if (pi == null) {
                    rebuilt.append('\n').append(s.substring(end + 6));
                } else {
                    images.add(pi);
                    rebuilt.append("~I").append(s.substring(end + 6));
                }
            } else {
                rebuilt.append("<IMG>").append(s);
            }
        }
        t = rebuilt.toString().replace("<IMG>", "").replace("</IMG>", "");

        ArrayList<String> firstPass = new ArrayList<>();
        for (String a : splitKeep(t, "~P")) {
            if (a.equals("~P")) {
                firstPass.add(a);
                continue;
            }
            for (String b : splitKeep(a, "~D")) {
                if (b.equals("~D")) {
                    firstPass.add(b);
                    continue;
                }
                for (String c : splitKeep(b, "~L")) {
                    if (c.equals("~L")) {
                        firstPass.add(c);
                        continue;
                    }
                    firstPass.addAll(splitKeep(c, "~I"));
                }
            }
        }

        ArrayList<String> parsed = new ArrayList<>();
        for (String s : firstPass) {
            if (s.equals("~P") || s.equals("~D") || s.equals("~L") || s.equals("~I")) {
                parsed.add(s);
            } else {
                parsed.addAll(wrapText(s));
            }
        }

        int lineHeight = font.lineHeight;
        int heightRemaining = 182;
        int dividerSpace = 0;
        if (research.getKey().equals("KNOWLEDGETYPES")) {
            heightRemaining -= 2;
            int tc = 0;
            for (IPlayerKnowledge.EnumKnowledgeType type : IPlayerKnowledge.EnumKnowledgeType.values()) {
                for (ResearchCategory category : ResearchCategories.researchCategories.values()) {
                    if (!type.hasFields() && category != null) {
                        continue;
                    }
                    if (playerKnowledge.getKnowledgeRaw(type, category != null ? category.key : null) > 0) {
                        tc++;
                        break;
                    }
                }
            }
            heightRemaining -= 20 * tc;
            dividerSpace = 12;
        }
        if (!isComplete) {
            ResearchStage[] allStages = research.getStages();
            ResearchStage stage = allStages != null && currentStage >= 0 && currentStage < allStages.length
                    ? allStages[currentStage] : null;
            if (stage != null) {
                if (stage.getCraft() != null) {
                    heightRemaining -= 18;
                    dividerSpace = 15;
                }
                if (stage.getObtain() != null) {
                    heightRemaining -= 18;
                    dividerSpace = 15;
                }
                if (stage.getKnow() != null) {
                    heightRemaining -= 18;
                    dividerSpace = 15;
                }
                if (stage.getResearch() != null) {
                    heightRemaining -= 18;
                    dividerSpace = 15;
                }
                if (stage.getDiscovery() != null && stage.getDiscovery().length > 0) {
                    heightRemaining -= 18;
                    dividerSpace = 15;
                }
            }
        }
        heightRemaining -= dividerSpace;

        Page page = new Page();
        ArrayList<PageImage> pendingImages = new ArrayList<>();
        for (String line : parsed) {
            if (line.contains("~I")) {
                if (!images.isEmpty()) {
                    pendingImages.add(images.remove(0));
                }
                line = "";
            }
            if (line.contains("~L")) {
                pendingImages.add(PILINE);
                line = "";
            }
            if (line.contains("~D")) {
                pendingImages.add(PIDIV);
                line = "";
            }
            if (line.contains("~P")) {
                heightRemaining = 210;
                out.add(page.copy());
                page = new Page();
                line = "";
            }
            if (!line.isEmpty()) {
                line = line.trim();
                boolean big = line.endsWith("~B");
                // До предъявления открытия текст стадии — шифр (ширина та же).
                page.contents.add(discoveryLocked ? "§k" + line + "§r" : line);
                heightRemaining -= lineHeight;
                if (big) {
                    heightRemaining -= (int) (lineHeight * 0.66);
                }
            }
            while (!pendingImages.isEmpty() && heightRemaining >= pendingImages.get(0).ah + 2) {
                heightRemaining -= pendingImages.get(0).ah + 2;
                page.contents.add(pendingImages.remove(0));
            }
            if (heightRemaining < lineHeight && !page.contents.isEmpty()) {
                heightRemaining = 210;
                out.add(page.copy());
                page = new Page();
            }
        }
        if (!page.contents.isEmpty()) {
            out.add(page.copy());
        }
        page = new Page();
        heightRemaining = 210;
        while (!pendingImages.isEmpty()) {
            if (heightRemaining < pendingImages.get(0).ah + 2) {
                heightRemaining = 210;
                out.add(page.copy());
                page = new Page();
            } else {
                heightRemaining -= pendingImages.get(0).ah + 2;
                page.contents.add(pendingImages.remove(0));
            }
        }
        if (!page.contents.isEmpty()) {
            out.add(page.copy());
        }
        return out;
    }

    private void updateRequirementTracking(ResearchStage stage) {

        if (stage.getObtain() != null) {
            hasItem = new boolean[stage.getObtain().length];
            for (int i = 0; i < stage.getObtain().length; i++) {
                hasItem[i] = playerHasRequirement(stage.getObtain()[i]);
                if (!hasItem[i]) hasAllRequisites = false;
            }
        }

        if (stage.getCraft() != null) {
            hasCraft = new boolean[stage.getCraft().length];
            int[] refs = stage.getCraftReference();
            for (int i = 0; i < stage.getCraft().length; i++) {
                String refKey = "[#]" + refs[i];
                hasCraft[i] = playerKnowledge.isResearchKnown(refKey);
                if (!hasCraft[i]) hasAllRequisites = false;
            }
        }

        if (stage.getResearch() != null) {
            hasResearch = new boolean[stage.getResearch().length];
            for (int i = 0; i < stage.getResearch().length; i++) {
                hasResearch[i] = ArcaneresearchCapabilities.isResearchComplete(player, stage.getResearch()[i]);
                if (!hasResearch[i]) hasAllRequisites = false;
            }
        }

        if (stage.getKnow() != null) {
            hasKnow = new boolean[stage.getKnow().length];
            for (int i = 0; i < stage.getKnow().length; i++) {
                ResearchStage.Knowledge k = stage.getKnow()[i];
                String catKey = k.category != null ? k.category.key : null;
                int playerKnow = playerKnowledge.getKnowledge(k.type, catKey);
                hasKnow[i] = playerKnow >= k.amount;
                if (!hasKnow[i]) hasAllRequisites = false;
            }
        }

        // NEOFORGE_PORT (discovered/*, Forge 1:1).
        if (stage.getDiscovery() != null) {
            hasDiscovery = new boolean[stage.getDiscovery().length];
            for (int i = 0; i < stage.getDiscovery().length; i++) {
                hasDiscovery[i] = arcaneresearch.discovered.note.DiscoveredNotes
                        .hasDiscovery(player, stage.getDiscovery()[i]);
                if (!hasDiscovery[i]) hasAllRequisites = false;
            }
        }
    }

    private void checkRequisites() {
        ResearchStage[] stages = research.getStages();
        if (stages == null) return;
        isComplete = playerKnowledge.isResearchComplete(research.getKey());
        while (currentStage >= stages.length) {
            currentStage--;
        }
        if (currentStage < 0) return;
        hasAllRequisites = true;
        updateRequirementTracking(stages[currentStage]);
    }

    private boolean playerHasRequirement(Object required) {
        if (required instanceof ItemStack stack) {
            return playerHasItem(stack);
        } else if (required instanceof String tagName) {
            try {
                TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.parse(tagName));
                // NEOFORGE_PORT: Inventory.items приватны в 26.x -> Container-API.
                var inventory = player.getInventory();
                for (int i = 0; i < inventory.getContainerSize(); i++) {
                    ItemStack stack = inventory.getItem(i);
                    if (!stack.isEmpty() && stack.is(tag)) {
                        return true;
                    }
                }
            } catch (Exception ignored) {
            }
            return false;
        }
        return false;
    }

    private boolean playerHasItem(ItemStack required) {
        return InventoryUtils.isPlayerCarryingAmount(player, required, false);
    }

    private List<String> wrapText(String text) {
        List<String> lines = new ArrayList<>();

        String[] paragraphs = text.split("<LINE>|\\n");

        for (String paragraph : paragraphs) {
            if (paragraph.trim().isEmpty()) {
                lines.add("");
                continue;
            }

            String[] words = paragraph.split(" ");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
                int testWidth = font.width(testLine);

                if (testWidth > 140 && !currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    if (!currentLine.isEmpty()) {
                        currentLine.append(" ");
                    }
                    currentLine.append(word);
                }
            }

            if (!currentLine.isEmpty()) {
                lines.add(currentLine.toString());
            }
        }

        return lines;
    }

    // NEOFORGE_PORT: plain-Screen drawing lives in extractBackground (base handles
    // background/widgets/deferred tooltips).
        // NEOFORGE_PORT: Screen.extractBackground is public in 26.x.
    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // NEOFORGE_PORT (26.3): isInGameUi() -> super draws exactly the 1.20.1 in-world
        // renderBackground gradient (no blur / menu texture, no second gradient).
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        int sw = (width - PANE_WIDTH) / 2;
        int sh = (height - PANE_HEIGHT) / 2;



        graphics.pose().pushMatrix();
        float scale = 1.3f;
        float offsetX = (width - PANE_WIDTH * scale) / 2.0f;
        float offsetY = (height - PANE_HEIGHT * scale) / 2.0f;
        graphics.pose().translate(offsetX, offsetY);
        graphics.pose().scale(scale, scale);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, 0, 0, PANE_WIDTH, PANE_HEIGHT, 256, 256);
        graphics.pose().popMatrix();

        RecipeRenderer.modalOpen = shownRecipe != null;
        RecipeRenderer.clearClickables();
        RecipeRenderer.clearReference();
        allowWithPagePopup = false;
        hasRecipePages = false;
        renderingCompound = false;

        long nowMs = System.currentTimeMillis();
        if (lastCycle < nowMs) {
            cycle++;
            lastCycle = nowMs + 1000L;
        }
        float bob = (float) Math.sin(playerTick() / 3.0) * 0.2f + 0.1f;

        if (page == 0) {
            String title = research.getLocalizedName().getString();
            int titleWidth = font.width(title);
            if (titleWidth <= 140) {
                graphics.text(font, title, sw - 15 + 70 - titleWidth / 2, sh - 10, 0xFF202020, false);
            } else {
                float vv = 140f / titleWidth;
                graphics.pose().pushMatrix();
                graphics.pose().translate(sw - 15 + 70 - titleWidth / 2f * vv, (sh - 10 + 1) * vv);
                graphics.pose().scale(vv, vv);
                graphics.text(font, title, 0, 0, 0xFF202020, false);
                graphics.pose().popMatrix();
            }

            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, sw + 4, sh - 17, 24, 184, 96, 4, 256, 256);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, sw + 4, sh, 24, 184, 96, 4, 256, 256);
        }

        int leftY = sh - 10 + (page == 0 ? 28 : 0);
        int rightY = sh - 10;
        List<net.minecraft.network.chat.Component> tooltip = null;

        if (page < pages.size()) {
            tooltip = drawPageContent(graphics, pages.get(page), sw - 15, leftY, mouseX, mouseY, 0);
        }

        if (page + 1 < pages.size()) {
            List<net.minecraft.network.chat.Component> rightTooltip = drawPageContent(graphics, pages.get(page + 1), sw - 15 + 152, rightY, mouseX, mouseY, 1);
            if (tooltip == null) tooltip = rightTooltip;
        }

        if (!recipeLists.isEmpty()) {
            allowWithPagePopup = true;
            List<net.minecraft.network.chat.Component> bmTip =
                    drawBookmarks(graphics, sw, sh, mouseX, mouseY);
            allowWithPagePopup = false;
            if (tooltip == null) tooltip = bmTip;
        }

        if (!history.isEmpty()) {
            drawScaledBlit(graphics, sw + 118, sh + 190, 38, 202, 20, 12, bob);
            if (mouseX >= sw + 118 && mouseY >= sh + 190 && mouseX < sw + 138 && mouseY < sh + 202) {
                tooltip = List.of(Component.translatable("recipe.return"));
            }
        }

        if (page > 0 && shownRecipe == null) {
            drawScaledBlit(graphics, sw - 16, sh + 190, 0, 184, 12, 8, bob);
        }
        if (page < maxPages - 2 && shownRecipe == null) {
            drawScaledBlit(graphics, sw + 262, sh + 190, 12, 184, 12, 8, bob);
        }


        if (!isComplete && page == 0 && research.getStages() != null && currentStage >= 0 && currentStage < research.getStages().length) {
            List<net.minecraft.network.chat.Component> reqTooltip =
                    drawRequirements(graphics, sw, sh, mouseX, mouseY, research.getStages()[currentStage]);
            if (tooltip == null) tooltip = reqTooltip;
        }

        if (playerKnowledge != null && playerKnowledge.isResearchComplete("KNOWLEDGETYPES")
                && research.getKey().equals("KNOWLEDGETYPES")) {
            List<net.minecraft.network.chat.Component> knowTip =
                    drawKnowledges(graphics, sw, sh - 16 + 210, mouseX, mouseY, true);
            if (tooltip == null) tooltip = knowTip;
        }

        String pageNum = (page / 2 + 1) + " / " + ((maxPages + 1) / 2);
        graphics.centeredText(font, pageNum, sw + PANE_WIDTH / 2, sh + PANE_HEIGHT + 5, 0xFF808080);

        if (knownPlayerAspects != null && playerKnowledge != null && playerKnowledge.isResearchComplete("FIRSTSTEPS")) {
            List<net.minecraft.network.chat.Component> tabTip =
                    drawSideTab(graphics, sw, sh + 9, 76, -48, -28, mouseX, mouseY,
                            "tc.aspect.name", showingAspects);
            if (tooltip == null) tooltip = tabTip;
        }
        if (playerKnowledge != null && playerKnowledge.isResearchComplete("KNOWLEDGETYPES")
                && !research.getKey().equals("KNOWLEDGETYPES")) {
            List<net.minecraft.network.chat.Component> tabTip =
                    drawSideTab(graphics, sw, sh + 32, 44, -49, -29, mouseX, mouseY,
                            "tc.knowledge.name", showingKnowledge);
            if (tooltip == null) tooltip = tabTip;
        }

        if (showingAspects) {
            List<net.minecraft.network.chat.Component> overlayTip =
                    drawAspectsOverlay(graphics, mouseX, mouseY);
            if (tooltip == null) tooltip = overlayTip;
        } else if (showingKnowledge) {
            List<net.minecraft.network.chat.Component> overlayTip =
                    drawKnowledgeOverlay(graphics, mouseX, mouseY);
            if (tooltip == null) tooltip = overlayTip;
        } else if (shownRecipe != null) {
            List<net.minecraft.network.chat.Component> recipeTip =
                    drawRecipeModal(graphics, mouseX, mouseY);
            if (tooltip == null) tooltip = recipeTip;
        }

        if (tooltip != null && !tooltip.isEmpty()) {
            graphics.setTooltipForNextFrame(font, tooltip, java.util.Optional.empty(), mouseX, mouseY);
        }
    }

    private int playerTick() {
        if (player != null) {
            return player.tickCount;
        }
        return (int) (System.currentTimeMillis() / 50L);
    }

    private void drawScaledBlit(GuiGraphicsExtractor graphics, int x, int y, int u, int v,
                                int w, int h, float scale) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x + w / 2f, y + h / 2f);
        graphics.pose().scale(1f + scale, 1f + scale);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, -w / 2, -h / 2, u, v, w, h, 256, 256);
        graphics.pose().popMatrix();
    }

    private List<net.minecraft.network.chat.Component> drawPageContent(GuiGraphicsExtractor graphics, Page page, int x, int y, int mouseX, int mouseY, int side) {

        int lineY = y;
        List<net.minecraft.network.chat.Component> structTip = null;
        for (Object content : page.contents) {
            if (content instanceof String text) {
                String visible = text.replace("~B", "");
                graphics.text(font, visible, x, lineY - 6, 0xFF202020, false);
                lineY += font.lineHeight;
                if (text.endsWith("~B")) {
                    lineY += (int) (font.lineHeight * 0.66);
                }
            } else if (content instanceof PageImage pi) {
                // NEOFORGE_PORT (Forge 1:1): Forge enabled blend before the <IMG>
                // blit and disabled it after; 26.3 blend is pipeline-owned, so this
                // is a no-op call kept for 1:1 call-site parity.
                blitBlend();
                                int pad = (140 - pi.aw) / 2;
                graphics.pose().pushMatrix();
                graphics.pose().translate(x + pad, lineY - 5);
                graphics.pose().scale(pi.scale, pi.scale);
                graphics.blit(RenderPipelines.GUI_TEXTURED, pi.loc, 0, 0, pi.x, pi.y, pi.w, pi.h, 256, 256);
                graphics.pose().popMatrix();
                lineY += pi.ah + 2;
            } else if (content instanceof PageStructure ps) {
                List<net.minecraft.network.chat.Component> tip =
                        drawStructure(graphics, ps, x, lineY - 5, mouseX, mouseY);
                if (tip != null) {
                    structTip = tip;
                }
                lineY += ps.h + 2;
            }
        }

        if (structTip != null) {
            return structTip;
        }

        if (!showingAspects && !showingKnowledge && shownRecipe == null && !isComplete
                && research.getStages() != null && currentStage >= 0
                && currentStage < research.getStages().length
                && research.getStages()[currentStage].getWarp() > 0) {
            int warp = Math.min(research.getStages()[currentStage].getWarp(), 5);

                        ResearchBrowserScreen.drawForbidden(graphics, x - 42, y - 40);
            String levelName = Component.translatable("tc.forbidden.level." + warp).getString();
            graphics.text(font, levelName, x - 41 - font.width(levelName) / 2, y - 43, 0xFFAAAAFF, false);
            if (mouseX >= x - 52 && mouseY >= y - 50 && mouseX < x - 32 && mouseY < y - 30) {
                String warn = Component.translatable("tc.warp.warn").getString().replace("%n", levelName);
                return List.of(Component.literal(warn));
            }
        }

        return null;
    }

    /**
     * Изометрия структуры (порт reforged drawCompoundPage):
     * два прохода не нужны — предметы рисуются painter's algorithm
     * снизу вверх, тултип — имя блока под курсором.
     */
    private List<net.minecraft.network.chat.Component> drawStructure(GuiGraphicsExtractor graphics,
            PageStructure ps, int x, int y, int mouseX, int mouseY) {
        List<net.minecraft.network.chat.Component> tip = null;
        int pad = Math.max(0, (140 - ps.w) / 2);
        java.util.List<PageStructure.Cell> cells = new java.util.ArrayList<>(ps.cells);
        cells.sort((a, b) -> {
            int ao = a.y() * 100 - a.x() + a.z();
            int bo = b.y() * 100 - b.x() + b.z();
            return Integer.compare(ao, bo);
        });
        for (PageStructure.Cell c : cells) {
            int px = x + pad + (ps.dx - 1 - c.x()) * 16 + c.z() * 16;
            int py = y + (ps.dy - 1 - c.y()) * 50 - (ps.dx - 1 - c.x()) * 8 + c.z() * 8;
            arcaneresearch.client.gui.GuiRenderHelper.renderItem(graphics, c.stack(), px, py);
            if (tip == null && mouseX >= px && mouseY >= py && mouseX < px + 16 && mouseY < py + 16) {
                tip = List.of(c.stack().getHoverName());
            }
        }
        return tip;
    }

    private List<net.minecraft.network.chat.Component> drawRecipeModal(GuiGraphicsExtractor graphics, int mx, int my) {
        renderingCompound = false;
        allowWithPagePopup = true;
        int x = (width - 256) / 2;
        int y = (height - 256) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, PAPER_TEX, x, y, 0, 0, 255, 255, 256, 256);
        List<net.minecraft.network.chat.Component> tip = null;
        List<Object> list = recipeLists.get(shownRecipe);
        if (list == null || list.isEmpty()) {
            list = drilldownLists.get(shownRecipe);
        }
        if (list != null && !list.isEmpty()) {
            hasRecipePages = list.size() > 1;
            recipePageMax = list.size() - 1;
            if (recipePage > recipePageMax) {
                recipePage = recipePageMax;
            }
            if (recipePage < 0) {
                recipePage = 0;
            }
            Object recipe = list.get(recipePage % list.size());
            if (recipe instanceof ArcaneresearchApi.BluePrint) {
                renderingCompound = true;
            }
            tip = RecipeRenderer.renderRecipeObject(graphics, recipe, x + 128, y + 128, mx, my, font);
            if (hasRecipePages) {
                float bob = (float) Math.sin(playerTick() / 3.0) * 0.2f + 0.1f;
                if (recipePage > 0) {
                    drawScaledBlit(graphics, x + 40, y + 232, 0, 184, 12, 8, bob);
                }
                if (recipePage < recipePageMax) {
                    drawScaledBlit(graphics, x + 204, y + 232, 12, 184, 12, 8, bob);
                }
            }
        }
        allowWithPagePopup = false;
        return tip;
    }

    private List<net.minecraft.network.chat.Component> drawBookmarks(GuiGraphicsExtractor graphics, int sw, int sh,
                                                                     int mx, int my) {

        List<net.minecraft.network.chat.Component> tip = null;
        java.util.Random rng = new java.util.Random(rhash);
        int y = sh - 8;
        int space = recipeLists.isEmpty() ? 25 : Math.min(25, 200 / recipeLists.size());
        for (Identifier rk : recipeLists.keySet()) {
            List<Object> outs = recipeOutputs.get(rk);
            if (outs == null || outs.isEmpty()) {
                continue;
            }
            Object o = outs.get(cycle % outs.size());
            if (o == null) {
                continue;
            }
            int sh2 = rng.nextInt(3);
            boolean hover = mx >= sw + 280 && my >= y - 1 && mx < sw + 310 && my < y + 15;
            int le = rng.nextInt(3) + (hover ? 0 : 3);
                // NEOFORGE_PORT: Forge setShaderColor(1, 0.5, 0.5) on the open
                // bookmark tab -> pink ARGB tint.
                int tabTint = rk.equals(shownRecipe) ? 0xFFFF8080 : 0xFFFFFFFF;
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, sw + 280 + sh2, y - 1, 120 + le, 232, 28, 16, 256, 256, tabTint);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, sw + 280 + sh2, y - 1, 116, 232, 4, 16, 256, 256, tabTint);
                if (o instanceof ItemStack stack && !stack.isEmpty()) {
                List<net.minecraft.network.chat.Component> stackTip = RecipeRenderer.drawStackAt(
                        graphics, stack, sw + 287 + sh2 - le, y - 1, mx, my, false, null);
                if (tip == null) tip = stackTip;
            } else if (o instanceof arcaneresearch.api.crafting.Part[][][] parts) {
                RecipeRenderer.renderBlueprintBookmark(graphics, rk, parts,
                        sw + 295 + sh2 - le, y + 6 + parts.length);
            }
            y += space;
        }
        return tip;
    }

    private void goBack() {
        if (!history.isEmpty()) {
            playPageSound();
            shownRecipe = history.pollFirst();
            RecipeRenderer.resetBlueprint();
        } else {
            shownRecipe = null;
        }
    }

    private List<net.minecraft.network.chat.Component> drawRequirements(GuiGraphicsExtractor graphics, int sw, int sh, int mx, int my, ResearchStage stage) {


        int y = sh - 16 + 210;
        List<net.minecraft.network.chat.Component> tip = null;
        reqBtnVisible = false;

        boolean hasRows = false;

        if (stage.getResearch() != null && stage.getResearch().length > 0) {
            y -= 18;
            hasRows = true;
                    graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, sw - 12, y - 1, 200, 232, 56, 16, 256, 256, 0x40FFFFFF);
                tip = popupAt(tip, graphics, sw - 15, y, mx, my, Component.translatable("tc.need.research").getString());
            if (hasResearch != null) {
                if (hasResearch.length != stage.getResearch().length) {
                    hasResearch = new boolean[stage.getResearch().length];
                }
                int ss = stage.getResearch().length > 6 ? 110 / stage.getResearch().length : 18;
                int shift = 24;
                for (int a = 0; a < stage.getResearch().length; a++) {
                    String key = stage.getResearch()[a];
                    Object loc = stage.getResearchIcon() != null && stage.getResearchIcon()[a] != null
                            ? Identifier.parse(stage.getResearchIcon()[a]) : DUMMY_RESEARCH;
                    String text = Component.translatable("research." + key + ".text").getString();
                    if (stage.getResearchIcon() != null && stage.getResearchIcon()[a] != null) {

                        ItemStack iconStack = iconSegmentStack(stage.getResearchIcon()[a]);
                        if (iconStack != null && !iconStack.isEmpty()) {
                            loc = iconStack;
                            text = iconStack.getHoverName().getString();
                        }
                    }
                    if (key.startsWith("!")) {
                        Aspect as = Aspect.aspects.get(key.substring(1));
                        if (as != null) {
                            loc = as;
                            text = as.getName();
                        }
                    }
                    ResearchEntry re = ResearchCategories.getResearch(key);
                    if (re != null && re.getIcons() != null && re.getIcons().length > 0) {
                        int idx = (int) (System.currentTimeMillis() / 1000L % re.getIcons().length);
                        loc = re.getIcons()[idx];
                        text = re.getLocalizedName().getString();
                    } else if (key.startsWith("m_")) {
                        loc = DUMMY_MAP;
                    } else if (key.startsWith("c_")) {
                        loc = DUMMY_CHEST;
                    } else if (key.startsWith("f_")) {
                        loc = DUMMY_FLASK;
                    }

                    boolean blueTint = !(loc instanceof Aspect) && !(loc instanceof ItemStack)
                            && !(loc instanceof Identifier);
                                if (blueTint) {
                        drawReqIconTinted(graphics, InventoryUtils.cycleItemStack(loc), loc,
                                sw - 15 + shift, y, 0.5f, 0.75f);
                    } else {
                        drawReqIcon(graphics, InventoryUtils.cycleItemStack(loc), loc, sw - 15 + shift, y);
                    }
                    if (a < hasResearch.length && hasResearch[a]) {

                                                blitBlend();
                                                        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, sw - 15 + shift + 8, y, 159, 207, 10, 10, 256, 256);
                    }
                                tip = popupAt(tip, graphics, sw - 15 + shift, y, mx, my, text);
                    shift += ss;
                }
            }
        }

        if (stage.getObtain() != null && stage.getObtain().length > 0) {
            y -= 18;
            hasRows = true;
                    graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, sw - 12, y - 1, 200, 216, 56, 16, 256, 256, 0x40FFFFFF);
                tip = popupAt(tip, graphics, sw - 15, y, mx, my, Component.translatable("tc.need.obtain").getString());
            if (hasItem != null) {
                if (hasItem.length != stage.getObtain().length) {
                    hasItem = new boolean[stage.getObtain().length];
                }
                int ss = stage.getObtain().length > 6 ? 110 / stage.getObtain().length : 18;
                int shift = 24;
                for (int i = 0; i < stage.getObtain().length; i++) {
                    ItemStack stack = InventoryUtils.cycleItemStack(stage.getObtain()[i], i);
                    List<net.minecraft.network.chat.Component> stackTip = RecipeRenderer.drawStackAt(
                            graphics, stack, sw - 15 + shift, y, mx, my, true, null);
                    if (tip == null) tip = stackTip;
                    if (i < hasItem.length && hasItem[i]) {
                                                blitBlend();

                                                        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, sw - 15 + shift + 8, y, 159, 207, 10, 10, 256, 256);
                    }
                                shift += ss;
                }
            }
        }

        // NEOFORGE_PORT (discovered/*, Forge 1:1, extractor-API): ряд требований-открытий.
        if (stage.getDiscovery() != null && stage.getDiscovery().length > 0) {
            y -= 18;
            hasRows = true;
                    graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, sw - 12, y - 1, 200, 216, 56, 16, 256, 256, 0x40FFFFFF);
                tip = popupAt(tip, graphics, sw - 15, y, mx, my, Component.translatable("tc.need.discovery").getString());
            if (hasDiscovery != null) {
                if (hasDiscovery.length != stage.getDiscovery().length) {
                    hasDiscovery = new boolean[stage.getDiscovery().length];
                }
                int ss = stage.getDiscovery().length > 6 ? 110 / stage.getDiscovery().length : 18;
                int shift = 24;
                for (int i = 0; i < stage.getDiscovery().length; i++) {
                    ItemStack stack = arcaneresearch.discovered.note.DiscoveredNotes
                            .discoveryStack(stage.getDiscovery()[i]);
                    List<net.minecraft.network.chat.Component> stackTip = RecipeRenderer.drawStackAt(
                            graphics, stack, sw - 15 + shift, y, mx, my, true, null);
                    if (tip == null) tip = stackTip;
                    if (i < hasDiscovery.length && hasDiscovery[i]) {
                                                blitBlend();

                                                        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, sw - 15 + shift + 8, y, 159, 207, 10, 10, 256, 256);
                    }
                                shift += ss;
                }
            }
        }

        if (stage.getCraft() != null && stage.getCraft().length > 0) {
            y -= 18;
            hasRows = true;
                    graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, sw - 12, y - 1, 200, 200, 56, 16, 256, 256, 0x40FFFFFF);
                tip = popupAt(tip, graphics, sw - 15, y, mx, my, Component.translatable("tc.need.craft").getString());
            if (hasCraft != null) {
                if (hasCraft.length != stage.getCraft().length) {
                    hasCraft = new boolean[stage.getCraft().length];
                }
                int ss = stage.getCraft().length > 6 ? 110 / stage.getCraft().length : 18;
                int shift = 24;
                for (int i = 0; i < stage.getCraft().length; i++) {
                    ItemStack stack = InventoryUtils.cycleItemStack(stage.getCraft()[i], i);
                    List<net.minecraft.network.chat.Component> stackTip = RecipeRenderer.drawStackAt(
                            graphics, stack, sw - 15 + shift, y, mx, my, true, null);
                    if (tip == null) tip = stackTip;
                    if (i < hasCraft.length && hasCraft[i]) {
                                                blitBlend();

                                                        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, sw - 15 + shift + 8, y, 159, 207, 10, 10, 256, 256);
                    }
                                shift += ss;
                }
            }
        }

        if (stage.getKnow() != null && stage.getKnow().length > 0) {
            y -= 18;
            hasRows = true;
                    graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, sw - 12, y - 1, 200, 184, 56, 16, 256, 256, 0x40FFFFFF);
                tip = popupAt(tip, graphics, sw - 15, y, mx, my, Component.translatable("tc.need.know").getString());
            if (hasKnow != null) {
                if (hasKnow.length != stage.getKnow().length) {
                    hasKnow = new boolean[stage.getKnow().length];
                }
                int ss = stage.getKnow().length > 6 ? 110 / stage.getKnow().length : 18;
                int shift = 24;
                for (int i = 0; i < stage.getKnow().length; i++) {
                    ResearchStage.Knowledge kn = stage.getKnow()[i];
                    int ix = sw - 15 + shift;
                                                        graphics.blit(RenderPipelines.GUI_TEXTURED, arcaneresearch.client.lib.events.HudHandler.getKnowledgeTypeIcon(kn.type.ordinal()),
                            ix, y, 0, 0, 16, 16, 16, 16, 16, 16);
                    if (kn.type.hasFields() && kn.category != null && kn.category.icon != null) {
                        graphics.blit(RenderPipelines.GUI_TEXTURED, kn.category.icon, ix + 3, y + 3, 0, 0, 10, 10, 32, 32, 32, 32);
                    }
                                 String am = (i < hasKnow.length && hasKnow[i] ? "" : "§c") + kn.amount;
                    graphics.pose().pushMatrix();
                    graphics.pose().translate(ix + 16 - font.width(am) / 4f, y + 12);
                    graphics.pose().scale(0.5f, 0.5f);
                    graphics.text(font, am, 0, 0, 0xFFFFFFFF, true);
                    graphics.pose().popMatrix();
                    if (i < hasKnow.length && hasKnow[i]) {
                                                blitBlend();
                                                        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, ix + 8, y, 159, 207, 10, 10, 256, 256);
                    }
                                String label = Component.translatable("tc.type." + kn.type.toString().toLowerCase()).getString();
                    if (kn.type.hasFields() && kn.category != null) {
                        label += ": " + ResearchCategories.getCategoryName(kn.category.key).getString();
                    }
                    tip = popupAt(tip, graphics, ix, y, mx, my, label);
                    shift += ss;
                }
            }
        }

        boolean noRequisites = (stage.getResearch() == null || stage.getResearch().length == 0)
                && (stage.getObtain() == null || stage.getObtain().length == 0)
                && (stage.getCraft() == null || stage.getCraft().length == 0)
                && (stage.getKnow() == null || stage.getKnow().length == 0)
                && (stage.getDiscovery() == null || stage.getDiscovery().length == 0);
        if (hasRows || noRequisites) {
            y -= 12;
                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, sw + 4, y - 2, 24, 184, 96, 8, 256, 256);
            if (hasAllRequisites) {
                reqBtnX = sw + 20;
                reqBtnY = y - 6;
                reqBtnVisible = true;
                if (hold) {
                    String s = Component.translatable("tc.stage.hold").getString();
                    graphics.text(font, s, sw + 52 - Math.round(font.width(s) / 2f), y - 4, 0xFFFFFFFF, true);
                } else {
                    boolean hover = mx >= reqBtnX && my >= reqBtnY && mx < reqBtnX + 64 && my < reqBtnY + 12;
                                // NEOFORGE_PORT: Forge setShaderColor(1,1,1) / !hover (0.8,0.8,0.9) -> ARGB.
                                int btnTint = hover ? 0xFFFFFFFF : 0xFFCCCCE5;
                                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, reqBtnX, reqBtnY, 84, 216, 64, 12, 256, 256, btnTint);
                                String s = Component.translatable("tc.stage.complete").getString();
                    graphics.text(font, s, sw + 52 - Math.round(font.width(s) / 2f), y - 4, 0xFFFFFFFF, true);
                }
            }
        }

                blitBlend();
        return tip;
    }

    private void drawReqIcon(GuiGraphicsExtractor graphics, ItemStack cycled, Object loc, int ix, int iy) {
        drawReqIconTinted(graphics, cycled, loc, ix, iy, 1f, 1f);
    }

    private void drawReqIconTinted(GuiGraphicsExtractor graphics, ItemStack cycled, Object loc,
                                   int ix, int iy, float r, float g) {

        // NEOFORGE_PORT: Forge setShaderColor(r, g, 1) tint on the texture
        // branches (aspect branch used its own color instead) -> ARGB colors.
        int dimTint = 0xFF000000 | ((int) (r * 255.0f) << 16)
                | ((int) (g * 255.0f) << 8) | 0xFF;
        if (loc instanceof Aspect aspect) {
                java.awt.Color cc = new java.awt.Color(aspect.getColor());
                int aspectTint = 0xFF000000 | (cc.getRed() << 16) | (cc.getGreen() << 8) | cc.getBlue();
                // NEOFORGE_PORT (Forge 1:1): Forge strips the aspect bg here via
                // UtilsFX.strippedAspectTexture instead of drawing the raw image.
                graphics.blit(RenderPipelines.GUI_TEXTURED,
                        arcaneresearch.client.lib.UtilsFX.strippedAspectTexture(aspect.getImage()),
                        ix, iy, 0, 0, 16, 16, 16, 16, aspectTint);
            } else if (loc instanceof Identifier rl) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, rl, ix, iy, 0, 0, 16, 16, 16, 16, dimTint);
            } else if (cycled != null && !cycled.isEmpty()) {
            arcaneresearch.client.gui.GuiRenderHelper.renderItem(graphics, cycled, ix, iy);
        } else {

                graphics.blit(RenderPipelines.GUI_TEXTURED, DUMMY_RESEARCH, ix, iy, 0, 0, 16, 16, 16, 16, dimTint);
            }
    }

    private List<net.minecraft.network.chat.Component> popupAt(List<net.minecraft.network.chat.Component> current,
                                                               GuiGraphicsExtractor graphics, int ix, int iy, int mx, int my, String text) {
        if (current == null && text != null && (shownRecipe == null || allowWithPagePopup)
                && mx >= ix && my >= iy && mx < ix + 16 && my < iy + 16) {
            return List.of(Component.literal(text));
        }
        return current;
    }

    @Override
    public void tick() {
        super.tick();

        long nano = System.nanoTime();
        if (nano > lastCheck) {
            parsePages();
            lastCheck = nano + (hold ? 250000000L : 2000000000L);
        }
    }

            // NEOFORGE_PORT: mouse coords/button via MouseButtonEvent.
    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();

        checkRequisites();
        int sw = (width - PANE_WIDTH) / 2;
        int sh = (height - PANE_HEIGHT) / 2;

        if (shownRecipe == null && !hold && hasAllRequisites
                && reqBtnVisible && mouseX >= reqBtnX && mouseX < reqBtnX + 64
                && mouseY >= reqBtnY && mouseY < reqBtnY + 12) {

            PacketHandler.sendToServer(new PacketSyncProgressToServer(research.getKey(), false, true, true));
            playWriteSound();
            lastCheck = 0L;
            hold = true;
            keyCache.clear();
            drilldownLists.clear();
            return true;
        }

        if (knownPlayerAspects != null && playerKnowledge != null
                && playerKnowledge.isResearchComplete("FIRSTSTEPS")
                && mouseX >= sw - 48 && mouseY >= sh + 9 && mouseX < sw - 23 && mouseY < sh + 25) {
            shownRecipe = null;
            showingKnowledge = false;
            showingAspects = !showingAspects;
            RecipeRenderer.resetBlueprint();
            history.clear();
            if (aspectsPage > maxAspectPages) {
                aspectsPage = 0;
            }
            playPageSound(0.7f, 0.9f);
            return true;
        }

        if (playerKnowledge != null && playerKnowledge.isResearchComplete("KNOWLEDGETYPES")
                && !research.getKey().equals("KNOWLEDGETYPES")
                && mouseX >= sw - 48 && mouseY >= sh + 32 && mouseX < sw - 23 && mouseY < sh + 48) {
            shownRecipe = null;
            showingAspects = false;
            showingKnowledge = !showingKnowledge;
            RecipeRenderer.resetBlueprint();
            history.clear();
            playPageSound(0.7f, 0.9f);
            return true;
        }

        if (showingAspects && aspectsPage < maxAspectPages - 1 && mouseX >= sw + 205 && mouseY >= sh + 192
                && mouseX < sw + 219 && mouseY < sh + 206) {
            aspectsPage++;
            playPageSound(0.7f, 0.9f);
            return true;
        }
        if (showingAspects && aspectsPage > 0 && mouseX >= sw + 38 && mouseY >= sh + 192
                && mouseX < sw + 52 && mouseY < sh + 206) {
            aspectsPage--;
            playPageSound(0.7f, 0.9f);
            return true;
        }

        if (!recipeLists.isEmpty()) {
            int aa = 0;
            int space = Math.min(25, 200 / recipeLists.size());
            for (Identifier rk : recipeLists.keySet()) {
                if (mouseX >= sw + 280 && mouseY >= sh - 8 + aa * space - 1
                        && mouseX < sw + 310 && mouseY < sh - 8 + aa * space + 15) {
                    if (rk.equals(shownRecipe)) {
                        shownRecipe = null;
                    } else {
                        shownRecipe = rk;
                    }
                    showingAspects = false;
                    showingKnowledge = false;
                    RecipeRenderer.resetBlueprint();
                    history.clear();
                    playPageSound(0.7f, 0.9f);
                    return true;
                }
                aa++;
            }
        }

        if (hasRecipePages && recipePage < recipePageMax && mouseX >= sw + 205 && mouseY >= sh + 192
                && mouseX < sw + 219 && mouseY < sh + 206) {
            recipePage++;
            playPageSound(0.7f, 0.9f);
            return true;
        }
        if (hasRecipePages && recipePage > 0 && mouseX >= sw + 38 && mouseY >= sh + 192
                && mouseX < sw + 52 && mouseY < sh + 206) {
            recipePage--;
            playPageSound(0.7f, 0.9f);
            return true;
        }

        if (shownRecipe == null && mouseX >= sw + 261 && mouseX < sw + 275
                && mouseY >= sh + 189 && mouseY < sh + 199) {
            nextPage();
            return true;
        }
        if (shownRecipe == null && mouseX >= sw - 17 && mouseX < sw - 3
                && mouseY >= sh + 189 && mouseY < sh + 199) {
            prevPage();
            return true;
        }

        if (mouseX >= sw + 118 && mouseY >= sh + 190 && mouseX < sw + 138 && mouseY < sh + 202) {
            goBack();
            return true;
        }

        if (renderingCompound && mouseX >= sw + 210 && mouseY >= sh + 190
                && mouseX < sw + 220 && mouseY < sh + 200) {
            RecipeRenderer.toggleBlueprintSlices();
            if (minecraft != null && minecraft.player != null) {
                ModSounds.CLACK.get();
                minecraft.player.playSound(ModSounds.CLACK.get(), 0.66f, 1.0f);
            }
            return true;
        }

        for (RecipeRenderer.DrilldownRef ref : RecipeRenderer.getReference()) {
            if (mouseX >= ref.x() && mouseY >= ref.y()
                    && mouseX < ref.x() + 16 && mouseY < ref.y() + 16) {
                playPageSound();
                if (shownRecipe != null) {
                    history.addFirst(shownRecipe);
                }
                shownRecipe = ref.recipe();
                recipePage = ref.page();
                if (!drilldownLists.containsKey(shownRecipe)) {
                    addRecipesToList(shownRecipe, drilldownLists,
                            new LinkedHashMap<>(), shownRecipe);
                }
                RecipeRenderer.resetBlueprint();
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

        // NEOFORGE_PORT: key codes via KeyEvent.key().
    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {

        // NEOFORGE_PORT (26.3): KeyEvent.key() is an InputConstants (SDL scancode) value, not
        // GLFW: 256/259/262-267 -> KEY_ESCAPE/BACKSPACE/RIGHT/LEFT/DOWN/UP/PAGEUP/PAGEDOWN.
        if (minecraft != null && (event.key() == com.mojang.blaze3d.platform.InputConstants.KEY_ESCAPE
                || event.key() == minecraft.options.keyInventory.getKey().getValue())) {
            history.clear();
            if (shownRecipe != null || showingAspects || showingKnowledge) {
                shownRecipe = null;
                RecipeRenderer.resetBlueprint();
                showingAspects = false;
                showingKnowledge = false;
                playPageSound(0.4f, 1.1f);
                return true;
            }
            return super.keyPressed(event);
        }

        if (event.key() == com.mojang.blaze3d.platform.InputConstants.KEY_BACKSPACE) {
            goBack();
            return true;
        }

        if (event.key() == com.mojang.blaze3d.platform.InputConstants.KEY_LEFT
                || event.key() == com.mojang.blaze3d.platform.InputConstants.KEY_UP
                || event.key() == com.mojang.blaze3d.platform.InputConstants.KEY_PAGEUP) {
            prevPage();
            return true;
        } else if (event.key() == com.mojang.blaze3d.platform.InputConstants.KEY_RIGHT
                || event.key() == com.mojang.blaze3d.platform.InputConstants.KEY_DOWN
                || event.key() == com.mojang.blaze3d.platform.InputConstants.KEY_PAGEDOWN) {
            nextPage();
            return true;
        }
        return super.keyPressed(event);
    }

    private void prevPage() {
        if (page >= 2) {
            page -= 2;
            lastCycle = 0L;
            cycle = -1;
            RecipeRenderer.resetCycle();
            playPageSound();
        }
    }

    private void nextPage() {
        if (page < maxPages - 2) {
            page += 2;
            lastCycle = 0L;
            cycle = -1;
            RecipeRenderer.resetCycle();
            playPageSound();
        }
    }

    // NEOFORGE_PORT: mouseScrolled(double,double,double) -> (double,double,double,double).
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY < 0) {
            nextPage();
            return true;
        } else if (scrollY > 0) {
            prevPage();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void playPageSound() {
        playPageSound(0.66f, 1.0f);
    }

    private void playPageSound(float volume, float pitch) {
        if (minecraft != null && minecraft.player != null) {
            ModSounds.PAGE.get();
            minecraft.player.playSound(ModSounds.PAGE.get(), volume, pitch);
        }
    }

    private void playWriteSound() {
        if (minecraft != null && minecraft.player != null) {
            ModSounds.WRITE.get();
            minecraft.player.playSound(ModSounds.WRITE.get(), 0.66f, 1.0f);
        }
    }

    private List<net.minecraft.network.chat.Component> drawSideTab(GuiGraphicsExtractor graphics, int sw, int ty,
                                                                   int texU, int dxMain, int dxNub,
                                                                   int mouseX, int mouseY,
                                                                   String tooltipKey, boolean active) {

        boolean hover = mouseX >= sw - 48 && mouseY >= ty && mouseX < sw - 23 && mouseY < ty + 16;
        int le = hover ? 0 : 3;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, sw + dxMain + le, ty, texU, 232, 24 - le, 16, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, sw + dxNub, ty, 100, 232, 4, 16, 256, 256);
        if (hover) {
            return List.of(Component.translatable(tooltipKey));
        }
        return null;
    }

    private List<net.minecraft.network.chat.Component> drawAspectsOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int x = (width - 256) / 2;
        int y = (height - 256) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, PAPER_TEX, x, y, 0, 0, 255, 255, 256, 256);
        allowWithPagePopup = true;
        List<net.minecraft.network.chat.Component> tooltip = drawAspectPage(graphics, x + 60, y + 24, mouseX, mouseY);
        allowWithPagePopup = false;

        float bob = (float) Math.sin(playerTick() / 3.0) * 0.2f + 0.1f;
        aspectsPage = Math.min(Math.max(aspectsPage, 0), Math.max(0, maxAspectPages - 1));
        if (aspectsPage > 0) {
            drawScaledBlit(graphics, x + 60 - 20, y + 24 + 208, 0, 184, 12, 8, bob);
        }
        if (aspectsPage < maxAspectPages - 1) {
            drawScaledBlit(graphics, x + 60 + 144, y + 24 + 208, 12, 184, 12, 8, bob);
        }
        return tooltip;
    }

    private List<net.minecraft.network.chat.Component> drawAspectPage(GuiGraphicsExtractor graphics, int x, int y, int mx, int my) {
        List<net.minecraft.network.chat.Component> tip = null;
        int count = -1;
        int start = aspectsPage * 5;
        for (Aspect aspect : knownPlayerAspects) {
            if (aspect.getImage() == null) {
                continue;
            }
            if (++count >= start) {
                if (count > start + 4) {
                    break;
                }
                int slot = count % 5;
                int ty = y + slot * 40;
                if (mx >= x && my >= ty && mx < x + 40 && my < ty + 40) {

                                            graphics.pose().pushMatrix();
                    graphics.pose().translate(x - 2, ty - 2);
                    graphics.pose().scale(2f, 2f);
                    // NEOFORGE_PORT: Forge setShaderColor(1,1,1,0.5) soft highlight -> 50% ARGB.
                    graphics.blit(RenderPipelines.GUI_TEXTURED, BACK_TEX, 0, 0, 0, 0, 16, 16, 64, 64, 0x80FFFFFF);
                    graphics.pose().popMatrix();
                                tip = List.of(Component.literal(aspect.getName()));
                }

                                graphics.pose().pushMatrix();
                graphics.pose().translate(x + 2, ty + 2);
                graphics.pose().scale(1.5f, 1.5f);
                AspectRenderer.drawAspect(graphics, 0, 0, aspect);
                graphics.pose().popMatrix();
                graphics.pose().pushMatrix();
                graphics.pose().translate(x + 16, ty + 29);
                graphics.pose().scale(0.5f, 0.5f);
                String text = aspect.getName();
                graphics.text(font, text, -font.width(text) / 2, 0, 0xFF505050, false);
                graphics.pose().popMatrix();
                if (aspect.getComponents() != null && aspect.getComponents().length == 2) {
                    Aspect[] comp = aspect.getComponents();
                    for (int c = 0; c < 2; c++) {
                        boolean knownComp = ArcaneresearchCapabilities.isResearchKnown(player,
                                "!" + comp[c].getTag().toLowerCase());
                        graphics.pose().pushMatrix();
                        graphics.pose().translate(x + 60 + c * 42, ty + 4);
                        graphics.pose().scale(1.25f, 1.25f);
                        if (knownComp) {
                            AspectRenderer.drawAspect(graphics, 0, 0, comp[c]);
                        } else {
                            // NEOFORGE_PORT: Forge setShaderColor(0.8,0.8,0.8) muted combo -> gray ARGB.
                                                        graphics.blit(RenderPipelines.GUI_TEXTURED, DUMMY_RESEARCH, 0, 0, 0, 0, 16, 16, 16, 16, 0xFFCCCCCC);
                                                }
                        graphics.pose().popMatrix();
                        if (knownComp) {
                            graphics.pose().pushMatrix();
                            graphics.pose().translate(x + 22 + (c == 0 ? 50 : 92), ty + 29);
                            graphics.pose().scale(0.5f, 0.5f);
                            String cn = comp[c].getName();
                            graphics.text(font, cn, -font.width(cn) / 2, 0, 0xFF505050, false);
                            graphics.pose().popMatrix();
                        }
                    }
                    graphics.text(font, "=", x + 9 + 32, ty + 12, 0xFF999999, false);
                    graphics.text(font, "+", x + 10 + 79, ty + 12, 0xFF999999, false);
                } else {
                    graphics.text(font, Component.translatable("tc.aspect.primal").getString(),
                            x + 54, ty + 12, 0xFF777777, false);
                }
            }
        }
        return tip;
    }

    private List<net.minecraft.network.chat.Component> drawKnowledgeOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int x = (width - 256) / 2;
        int y = (height - 256) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, PAPER_TEX, x, y, 0, 0, 255, 255, 256, 256);
        allowWithPagePopup = true;

        int knowY = (height - PANE_HEIGHT) / 2 + 75;
        List<net.minecraft.network.chat.Component> tip =
                drawKnowledges(graphics, x + 60, knowY, mouseX, mouseY, false);
        allowWithPagePopup = false;
        return tip;
    }

    private List<net.minecraft.network.chat.Component> drawKnowledges(GuiGraphicsExtractor graphics, int x, int y,
                                                                      int mx, int my, boolean inpage) {

        List<net.minecraft.network.chat.Component> tip = null;
        y -= 18;
        boolean drewSomething = false;
        int tc = 0;
        int ka = Math.max(ResearchCategories.researchCategories.size(), 1);
        for (IPlayerKnowledge.EnumKnowledgeType type : IPlayerKnowledge.EnumKnowledgeType.values()) {
            int fc = 0;
            int hs = (int) (164f / ka);
            boolean b = false;
            for (ResearchCategory category : ResearchCategories.researchCategories.values()) {
                if (!type.hasFields() && category != null) {
                    continue;
                }
                String catKey = category != null ? category.key : null;
                int amt = playerKnowledge.getKnowledge(type, catKey);
                int par = playerKnowledge.getKnowledgeRaw(type, catKey) % type.getProgression();
                if (amt <= 0 && par <= 0) {
                    continue;
                }
                drewSomething = true;
                int step = inpage ? 18 : hs;
                int rowStep = inpage ? 20 : 28;
                int ix = x - 10 + step * fc;
                int iy = y - tc * rowStep;

                                        Identifier knowIcon = arcaneresearch.client.lib.events.HudHandler
                        .getKnowledgeTypeIcon(type.ordinal());

                graphics.blit(RenderPipelines.GUI_TEXTURED, knowIcon, ix, iy, 0, 0, 16, 16, 16, 16, 16, 16);
                if (type.hasFields() && category != null && category.icon != null) {

                                                        graphics.blit(RenderPipelines.GUI_TEXTURED, category.icon, ix + 3, iy + 3, 0, 0, 10, 10, 32, 32, 32, 32);
                }
                        String s = "" + amt;
                int m = font.width(s);

                // NEOFORGE_PORT: Forge translate(0, 0, 200) lifted the amount
                // above the icons/progress bars; the 2D stack has no z, so the
                // text submits AFTER the bars (painter's order = same result).
                String label = Component.translatable("tc.type." + type.toString().toLowerCase()).getString();
                if (type.hasFields() && category != null) {
                    label += ": " + ResearchCategories.getCategoryName(category.key).getString();
                }
                if (tip == null && (shownRecipe == null || allowWithPagePopup)
                        && mx >= ix && my >= iy && mx < ix + 16 && my < iy + 16) {
                    tip = List.of(Component.literal(label));
                }
                if (par > 0) {
                    int l = (int) (par / (float) type.getProgression() * 16f);
                                                        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, ix, iy + 17, 0, 232, l, 2, 256, 256);
                    graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, ix + l, iy + 17, l, 234, 16 - l, 2, 256, 256);
                            }
                graphics.text(font, s, x - 10 + 16 - m + step * fc, y - tc * rowStep + 8, 0xFFFFFFFF, true);
                ++fc;
                b = true;
            }
            if (b) {
                tc++;
            }
        }
        if (inpage && drewSomething) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 4, y - tc * 20 + 12, 24, 184, 96, 8, 256, 256);
        }
        return tip;
    }

    @Override
    public void onClose() {

        history.clear();
        // NEOFORGE_PORT: Minecraft.setScreen removed -> setScreenAndShow.
        Objects.requireNonNull(minecraft).setScreenAndShow(new ResearchBrowserScreen(returnX, returnY));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean isInGameUi() {
        return true;
    }

    private static class Page {
        ArrayList<Object> contents = new ArrayList<>();

        Page copy() {
            Page p = new Page();
            p.contents.addAll(contents);
            return p;
        }
    }

    /**
     * Изометрическая схема мультиблока из stage.structure
     * ("dx,dy,dz:itemId"), порт reforged drawCompoundPage:
     * та же раскладка px = i*16 + k*16, py = -i*8 + k*8 + j*50.
     */
    private static class PageStructure {
        record Cell(int x, int y, int z, ItemStack stack) {
        }

        final java.util.List<Cell> cells = new java.util.ArrayList<>();
        int dx;
        int dy;
        int dz;
        int w;
        int h;

        static PageStructure parse(String[] defs) {
            if (defs == null || defs.length == 0) {
                return null;
            }
            PageStructure ps = new PageStructure();
            for (String d : defs) {
                if (d == null) {
                    continue;
                }
                // У itemId внутри есть ':' ("0,0,0:arcaneresearch:table") —
                // без лимита split даёт 3 части и parse всегда возвращал null.
                String[] parts = d.split(":", 2);
                if (parts.length != 2) {
                    continue;
                }
                String[] c = parts[0].split(",");
                if (c.length != 3) {
                    continue;
                }
                try {
                    int x = Integer.parseInt(c[0].trim());
                    int y = Integer.parseInt(c[1].trim());
                    int z = Integer.parseInt(c[2].trim());
                    // NEOFORGE_PORT: ForgeRegistries.ITEMS -> BuiltInRegistries.ITEM.
                    Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM
                            .getValue(Identifier.parse(parts[1].trim()));
                    if (item == null) {
                        continue;
                    }
                    ItemStack stack = new ItemStack(item);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    ps.cells.add(new Cell(x, y, z, stack));
                    if (x + 1 > ps.dx) {
                        ps.dx = x + 1;
                    }
                    if (y + 1 > ps.dy) {
                        ps.dy = y + 1;
                    }
                    if (z + 1 > ps.dz) {
                        ps.dz = z + 1;
                    }
                } catch (Exception ignored) {
                }
            }
            if (ps.cells.isEmpty()) {
                return null;
            }
            ps.w = (ps.dx + ps.dz) * 16 + 16;
            ps.h = (ps.dy - 1) * 50 + (ps.dx + ps.dz) * 8 + 20;
            return ps;
        }
    }

    private static class PageImage {
        int x;
        int y;
        int w;
        int h;
        int aw;
        int ah;
        float scale;
        Identifier loc;

        static PageImage parse(String text) {
            String[] s = text.split(":");
            if (s.length != 7) {
                return null;
            }
            try {
                PageImage pi = new PageImage();
                pi.loc = Identifier.fromNamespaceAndPath(s[0], s[1]);
                pi.x = Integer.parseInt(s[2]);
                pi.y = Integer.parseInt(s[3]);
                pi.w = Integer.parseInt(s[4]);
                pi.h = Integer.parseInt(s[5]);
                pi.scale = Float.parseFloat(s[6]);
                pi.aw = (int) (pi.w * pi.scale);
                pi.ah = (int) (pi.h * pi.scale);
                if (pi.ah > 208 || pi.aw > 140) {
                    return null;
                }
                return pi;
            } catch (Exception ex) {
                return null;
            }
        }
    }
}