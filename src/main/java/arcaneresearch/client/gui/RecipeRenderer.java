package arcaneresearch.client.gui;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.crafting.CrucibleRecipe;
import arcaneresearch.api.crafting.IArcaneRecipe;
import arcaneresearch.api.crafting.IArcaneresearchRecipe;
import arcaneresearch.api.crafting.InfusionRecipe;
import arcaneresearch.api.internal.CommonInternals;
import arcaneresearch.client.lib.AspectRenderer;
import arcaneresearch.common.lib.crafting.CrucibleRecipeType;
import arcaneresearch.common.lib.crafting.InfusionRecipeType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@OnlyIn(Dist.CLIENT)
public class RecipeRenderer {
    private static final ResourceLocation OVERLAY_TEXTURE = new ResourceLocation(Arcaneresearch.MODID, "textures/gui/gui_researchbook_overlay.png");
    private static final ResourceLocation BOOK_TEXTURE = new ResourceLocation(Arcaneresearch.MODID, "textures/gui/gui_researchbook.png");
    private static final int SLOT_SIZE = 18;
    private static final int ITEM_SIZE = 16;
    private static final List<ClickableIngredient> clickables = new ArrayList<>();
    private static final List<DrilldownRef> reference = new ArrayList<>();
    private static final float BLUEPRINT_ROT_X = 25.0f;
    private static final float BLUEPRINT_ROT_Y = -45.0f;
    private static final java.util.Map<ResourceLocation, BlueprintBlockAccess> blueprintIconAccess =
            new java.util.HashMap<>();
    public static boolean modalOpen = false;
    public static boolean allowWithPagePopup = false;
    private static long lastCycleTime = 0;
    private static int cycleIndex = 0;
    private static boolean dumpedManagerIds = false;
    private static boolean cycleMultiblockLines = true;
    private static BlueprintBlockAccess blueprintAccess = null;
    private static String blueprintAccessKey = null;
    private static long blueprintLastSlice = 0L;
    public static void clearReference() {
        reference.clear();
    }
    public static List<DrilldownRef> getReference() {
        return List.copyOf(reference);
    }
    public static void resetCycle() {
        cycleIndex = 0;
        lastCycleTime = System.currentTimeMillis();
    }
    public static void resetBlueprint() {
        blueprintAccess = null;
        blueprintAccessKey = null;
    }
    public static void clearClickables() {
        clickables.clear();
    }
    public static List<ClickableIngredient> getClickables() {
        return List.copyOf(clickables);
    }
    public static List<net.minecraft.network.chat.Component> renderRecipe(
            GuiGraphics graphics, ResourceLocation recipeId, int x, int y,
            int mouseX, int mouseY, Font font) {
        updateCycle();
        Object recipe = findRecipe(recipeId);
        if (recipe == null) {
            graphics.drawCenteredString(font, "Recipe not found:", x, y - 20, 0x804040);
            String idStr = recipeId.toString();
            if (idStr.length() > 30) {
                idStr = "..." + idStr.substring(idStr.length() - 27);
            }
            graphics.drawCenteredString(font, idStr, x, y - 8, 0x606060);
            return null;
        }
        return renderRecipeObject(graphics, recipe, x, y, mouseX, mouseY, font);
    }
    public static List<net.minecraft.network.chat.Component> renderRecipeObject(
            GuiGraphics graphics, Object recipe, int x, int y,
            int mouseX, int mouseY, Font font) {
        updateCycle();
        if (recipe == null) {
            graphics.drawCenteredString(font, "Unknown recipe type", x, y, 0x804040);
            return null;
        }
        if (recipe instanceof CrucibleRecipeType crucibleType) {
            return renderCrucibleTypeRecipe(graphics, crucibleType, x, y, mouseX, mouseY, font);
        } else if (recipe instanceof InfusionRecipeType infusionType) {
            return renderInfusionTypeRecipe(graphics, infusionType, x, y, mouseX, mouseY, font);
        } else if (recipe instanceof ArcaneresearchApi.BluePrint blueprint) {
            return renderBlueprintRecipe(graphics, blueprint, x, y, mouseX, mouseY, font);
        } else if (recipe instanceof CrucibleRecipe crucible) {
            return renderCrucibleRecipe(graphics, crucible, x, y, mouseX, mouseY, font);
        } else if (recipe instanceof InfusionRecipe infusion) {
            return renderInfusionRecipe(graphics, infusion, x, y, mouseX, mouseY, font);
        } else if (recipe instanceof IArcaneRecipe arcane) {
            return renderArcaneRecipe(graphics, arcane, x, y, mouseX, mouseY, font);
        } else if (recipe instanceof CraftingRecipe crafting) {
            return renderCraftingRecipe(graphics, crafting, x, y, mouseX, mouseY, font);
        } else {
            graphics.drawCenteredString(font, "Unknown recipe type", x, y, 0x804040);
            return null;
        }
    }
    private static Object findRecipe(ResourceLocation id) {
        IArcaneresearchRecipe tcRecipe = CommonInternals.getCatalogRecipe(id);
        if (tcRecipe != null) {
            return tcRecipe;
        }
        Object fakeRecipe = CommonInternals.getCatalogRecipeFake(id);
        if (fakeRecipe != null) {
            return fakeRecipe;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Optional<? extends Recipe<?>> vanillaRecipe = mc.level.getRecipeManager().byKey(id);
            if (vanillaRecipe.isPresent()) {
                return vanillaRecipe.get();
            }
            for (Recipe<?> known : mc.level.getRecipeManager().getRecipes()) {
                if (known.getId().equals(id)) {
                    Arcaneresearch.LOGGER.debug("Thaumonomicon resolved {} via fallback scan ({})",
                            id, known.getClass().getSimpleName());
                    return known;
                }
            }
            int tcCount = 0;
            int infusionCount = 0;
            StringBuilder idDump = new StringBuilder();
            for (Recipe<?> known : mc.level.getRecipeManager().getRecipes()) {
                if (known.getId().getNamespace().equals(Arcaneresearch.MODID)) {
                    tcCount++;
                    if (tcCount <= 400) {
                        idDump.append(known.getId()).append(' ');
                    }
                    if (known instanceof InfusionRecipeType) {
                        infusionCount++;
                    }
                }
            }
            if (!dumpedManagerIds) {
                dumpedManagerIds = true;
                Arcaneresearch.LOGGER.debug("Thaumonomicon manager tc recipe ids: {}", idDump);
            }
            Arcaneresearch.LOGGER.warn("Thaumonomicon page references unknown recipe: {} "
                            + "(catalog={}, fake={}, managerRecipes={}, tcInManager={}, infusionInManager={})", id,
                    CommonInternals.getCatalogRecipe(id) != null,
                    CommonInternals.getCatalogRecipeFake(id) != null,
                    mc.level.getRecipeManager().getRecipes().size(), tcCount, infusionCount);
        }
        return null;
    }
    private static void updateCycle() {
        long now = System.currentTimeMillis();
        if (now - lastCycleTime > 1000) {
            cycleIndex++;
            lastCycleTime = now;
        }
    }
    private static ItemStack cycleIngredient(Ingredient ingredient, int slotIndex) {
        ItemStack[] items = ingredient.getItems();
        if (items.length == 0) return ItemStack.EMPTY;
        return items[(cycleIndex + slotIndex) % items.length];
    }
    private static List<net.minecraft.network.chat.Component> renderCraftingRecipe(
            GuiGraphics graphics, CraftingRecipe recipe, int x, int y,
            int mouseX, int mouseY, Font font) {
        List<net.minecraft.network.chat.Component> tooltip = null;
        String titleKey = recipe instanceof ShapedRecipe
                ? "recipe.type.workbench" : "recipe.type.workbenchshapeless";
        String title = net.minecraft.network.chat.Component.translatable(titleKey).getString();
        graphics.drawCenteredString(font, title, x, y - 104, 0x505050);
        drawGridBackdrop(graphics, x, y, 60, 15, 51, 52);
        ItemStack output = recipe.getResultItem(Minecraft.getInstance().level.registryAccess());
        renderItem(graphics, output, x - 8, y - 84);
        tooltip = checkItemTooltip(output, x - 8, y - 84, mouseX, mouseY, false, tooltip);
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        int width = recipe instanceof ShapedRecipe shaped ? shaped.getWidth() : 3;
        int height = recipe instanceof ShapedRecipe shaped ? shaped.getHeight() : (ingredients.size() + 2) / 3;
        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient ing = ingredients.get(i);
            if (ing.isEmpty()) continue;
            int gridX, gridY;
            if (recipe instanceof ShapedRecipe) {
                gridX = i % width;
                gridY = i / width;
            } else {
                gridX = i % 3;
                gridY = i / 3;
            }
            int itemX = x - 40 + gridX * 32;
            int itemY = y - 40 + gridY * 32;
            ItemStack stack = cycleIngredient(ing, i);
            renderItem(graphics, stack, itemX, itemY);
            tooltip = checkItemTooltip(stack, itemX, itemY, mouseX, mouseY, true, tooltip);
        }
        return tooltip;
    }
    private static void drawGridBackdrop(GuiGraphics graphics, int x, int y,
                                         int u, int v, int w, int h) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(2f, 2f, 1f);
        graphics.blit(OVERLAY_TEXTURE, -26, -26, u, v, w, h);
        graphics.blit(OVERLAY_TEXTURE, -8, -46, 20, 3, 16, 16);
        graphics.pose().popPose();
    }
    private static List<net.minecraft.network.chat.Component> renderArcaneRecipe(
            GuiGraphics graphics, IArcaneRecipe recipe, int x, int y,
            int mouseX, int mouseY, Font font) {
        List<net.minecraft.network.chat.Component> tooltip = null;
        boolean shaped = recipe instanceof arcaneresearch.api.crafting.ShapedArcaneRecipe
                || recipe instanceof arcaneresearch.common.lib.crafting.ShapedArcaneRecipe;
        String titleKey = shaped ? "recipe.type.arcane" : "recipe.type.arcane.shapeless";
        if (recipe instanceof arcaneresearch.api.crafting.ShapelessArcaneRecipe
                || recipe instanceof arcaneresearch.common.lib.crafting.ShapelessArcaneRecipe) {
            titleKey = "recipe.type.arcane.shapeless";
            shaped = false;
        }
        String title = net.minecraft.network.chat.Component.translatable(titleKey).getString();
        graphics.drawCenteredString(font, title, x, y - 104, 0x505050);
        drawGridBackdrop(graphics, x, y, 112, 15, 52, 52);
        RenderSystem.setShaderColor(1f, 1f, 1f, 0.4f);
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(2f, 2f, 1f);
        graphics.blit(OVERLAY_TEXTURE, -6, 40, 68, 76, 12, 12);
        graphics.pose().popPose();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        ItemStack output = recipe.getResultItem(Minecraft.getInstance().level.registryAccess());
        renderItem(graphics, output, x - 8, y - 84);
        tooltip = checkItemTooltip(output, x - 8, y - 84, mouseX, mouseY, false, tooltip);
        int visCost = recipe.getVis();
        String visText = "" + visCost;
        int visOffset = font.width(visText);
        graphics.drawString(font, visText, x - visOffset / 2, y + 90, 0x505050, false);
        tooltip = checkPopup(x - visOffset / 2 - 15, y + 75, 30, 30,
                mouseX, mouseY, "wandtable.text1", tooltip);
        AspectList crystals = recipe.getCrystals();
        if (crystals != null && crystals.size() > 0) {
            int a = 0;
            int sz = crystals.size();
            for (Aspect aspect : crystals.getAspects()) {
                int amount = crystals.getAmount(aspect);
                ItemStack crystal = arcaneresearch.api.ArcaneresearchApiHelper.makeCrystal(aspect, amount);
                if (!crystal.isEmpty()) {
                    ItemStack stack = cycleIngredient(Ingredient.of(crystal), a);
                    renderItem(graphics, stack, x + 4 - sz * 10 + a * 20, y + 59);
                    tooltip = checkItemTooltip(stack, x + 4 - sz * 10 + a * 20, y + 59, mouseX, mouseY, true, tooltip);
                }
                a++;
            }
        }
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        int rw = 3;
        int rh = 3;
        try {
            if (recipe instanceof arcaneresearch.api.crafting.ShapedArcaneRecipe shapedArc) {
                rw = Math.min(3, shapedArc.getWidth());
                rh = Math.min(3, shapedArc.getHeight());
            } else if (recipe instanceof arcaneresearch.common.lib.crafting.ShapedArcaneRecipe shapedArc2) {
                rw = Math.min(3, shapedArc2.getWidth());
                rh = Math.min(3, shapedArc2.getHeight());
            }
        } catch (Exception ignored) {
        }
        if (shaped) {
            for (int i = 0; i < rw; i++) {
                for (int j = 0; j < rh; j++) {
                    int idx = i + j * rw;
                    if (idx >= ingredients.size()) continue;
                    Ingredient ing = ingredients.get(idx);
                    if (ing == null || ing.isEmpty()) continue;
                    int itemX = x - 40 + i * 32;
                    int itemY = y - 40 + j * 32;
                    ItemStack stack = cycleIngredient(ing, idx);
                    renderItem(graphics, stack, itemX, itemY);
                    tooltip = checkItemTooltip(stack, itemX, itemY, mouseX, mouseY, true, tooltip);
                }
            }
        } else {
            for (int i = 0; i < ingredients.size() && i < 9; i++) {
                Ingredient ing = ingredients.get(i);
                if (ing.isEmpty()) continue;
                int gridX = i % 3;
                int gridY = i / 3;
                int itemX = x - 40 + gridX * 32;
                int itemY = y - 40 + gridY * 32;
                ItemStack stack = cycleIngredient(ing, i);
                renderItem(graphics, stack, itemX, itemY);
                tooltip = checkItemTooltip(stack, itemX, itemY, mouseX, mouseY, true, tooltip);
            }
        }
        return tooltip;
    }
    private static List<net.minecraft.network.chat.Component> renderCrucibleRecipe(
            GuiGraphics graphics, CrucibleRecipe recipe, int x, int y,
            int mouseX, int mouseY, Font font) {
        List<net.minecraft.network.chat.Component> tooltip = null;
        String title = net.minecraft.network.chat.Component.translatable("recipe.type.crucible").getString();
        graphics.drawCenteredString(font, title, x, y - 104, 0x505050);
        drawCrucibleBackdrop(graphics, x, y);
        ItemStack output = recipe.getRecipeOutput();
        renderItem(graphics, output, x - 8, y - 50);
        tooltip = checkItemTooltip(output, x - 8, y - 50, mouseX, mouseY, false, tooltip);
        ItemStack catalyst = cycleIngredient(recipe.getCatalyst(), 0);
        renderItem(graphics, catalyst, x - 64, y - 56);
        tooltip = checkItemTooltip(catalyst, x - 64, y - 56, mouseX, mouseY, true, tooltip);
        AspectList aspects = recipe.getAspects();
        if (aspects != null && aspects.size() > 0) {
            tooltip = drawCrucibleAspects(graphics, aspects, x, y, mouseX, mouseY, tooltip);
        }
        return tooltip;
    }
    private static void drawCrucibleBackdrop(GuiGraphics graphics, int x, int y) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(2f, 2f, 1f);
        graphics.blit(OVERLAY_TEXTURE, -28, -29, 0, 3, 56, 17);
        graphics.blit(OVERLAY_TEXTURE, -28, -12, 0, 20, 56, 48);
        graphics.blit(OVERLAY_TEXTURE, -25, -26, 100, 84, 11, 13);
        graphics.pose().popPose();
    }
    private static List<net.minecraft.network.chat.Component> drawCrucibleAspects(
            GuiGraphics graphics, AspectList aspects, int x, int y,
            int mouseX, int mouseY,
            List<net.minecraft.network.chat.Component> tooltip) {
        int total = 0;
        int rows = (aspects.size() - 1) / 3;
        int shift = (3 - aspects.size() % 3) * 10;
        int sx = x - 28;
        int sy = y + 8 - 10 * rows;
        for (Aspect tag : aspects.getAspectsSortedByName()) {
            int m = 0;
            if (total / 3 >= rows && (rows > 1 || aspects.size() < 3)) {
                m = 1;
            }
            int vx = sx + total % 3 * 20 + shift * m;
            int vy = sy + total / 3 * 20;
            int amount = aspects.getAmount(tag);
            List<net.minecraft.network.chat.Component> aspectTooltip =
                    AspectRenderer.drawAspectWithTooltip(graphics, vx, vy,
                            tag, amount, mouseX, mouseY);
            if (aspectTooltip != null && tooltip == null) {
                tooltip = aspectTooltip;
            }
            total++;
        }
        return tooltip;
    }
    private static List<net.minecraft.network.chat.Component> renderInfusionRecipe(
            GuiGraphics graphics, InfusionRecipe recipe, int x, int y,
            int mouseX, int mouseY, Font font) {
        List<net.minecraft.network.chat.Component> tooltip = null;
        String title = net.minecraft.network.chat.Component.translatable("recipe.type.infusion").getString();
        int titleOffset = font.width(title);
        graphics.drawString(font, title, x - titleOffset / 2, y - 104, 0x505050, false);
        drawInfusionBackdrop(graphics, x, y);
        NonNullList<Ingredient> components = recipe.getComponents();
        AspectList aspects = recipe.getAspects();
        Object outputObj = recipe.getRecipeOutput();
        net.minecraft.world.entity.player.Player viewer0 = Minecraft.getInstance().player;
        try {
            ItemStack base = ItemStack.EMPTY;
            ItemStack[] baseOpts = recipe.getRecipeInput().getItems();
            if (baseOpts.length > 0) {
                base = baseOpts[0].copy();
            }
            java.util.ArrayList<ItemStack> com = new java.util.ArrayList<>();
            for (Ingredient ing : components) {
                ItemStack[] opts = ing.getItems();
                if (opts.length > 0) com.add(opts[0]);
            }
            aspects = recipe.getAspects(viewer0, base, com);
            outputObj = recipe.getRecipeOutput(viewer0, base, com);
        } catch (Exception ignored) {
        }
        if (outputObj instanceof ItemStack output) {
            ItemStack disp = cycleIngredient(Ingredient.of(output.copy()), 0);
            renderItem(graphics, disp, x - 8, y - 85);
            tooltip = checkItemTooltip(disp, x - 8, y - 85, mouseX, mouseY, false, tooltip);
        } else if (outputObj != null) {
            ItemStack disp = cycleIngredient(recipe.getRecipeInput(), 0).copy();
            try {
                Object[] obj = (Object[]) outputObj;
                disp.addTagElement((String) obj[0], (net.minecraft.nbt.Tag) obj[1]);
            } catch (Exception ignored) {
            }
            renderItem(graphics, disp, x - 8, y - 85);
            tooltip = checkItemTooltip(disp, x - 8, y - 85, mouseX, mouseY, false, tooltip);
        }
        ItemStack central = cycleIngredient(recipe.getRecipeInput(), 0);
        renderItem(graphics, central, x - 8, y - 16);
        tooltip = checkItemTooltip(central, x - 8, y - 16, mouseX, mouseY, true, tooltip);
        int numComponents = components.size();
        int radius = 40;
        java.util.ArrayList<ItemStack> cmps = new java.util.ArrayList<>();
        for (int i = 0; i < numComponents; i++) {
            double angle = (2 * Math.PI * i / numComponents) - Math.PI / 2;
            int compX = x + (int) (Math.cos(angle) * radius) - 8;
            int compY = (y - 8) + (int) (Math.sin(angle) * radius) - 8;
            ItemStack comp = cycleIngredient(components.get(i), i);
            ItemStack single = comp.copy();
            single.setCount(1);
            renderItem(graphics, single, compX, compY);
            tooltip = checkItemTooltip(single, compX, compY, mouseX, mouseY, true, tooltip);
            cmps.add(comp.copy());
        }
        if (aspects != null && aspects.size() > 0) {
            tooltip = drawInfusionAspects(graphics, aspects, x, y, mouseX, mouseY, font, tooltip);
        }
        net.minecraft.world.entity.player.Player viewer = Minecraft.getInstance().player;
        ItemStack rinp = central.copy();
        int inst = 0;
        try {
            inst = Math.min(5, recipe.getInstability(viewer, rinp, cmps) / 2);
        } catch (Exception ignored) {
        }
        String instText = net.minecraft.network.chat.Component.translatable("tc.inst").getString()
                + " " + net.minecraft.network.chat.Component.translatable("tc.inst." + inst).getString();
        graphics.drawCenteredString(font, instText, x, y + 94, 0x505050);
        return tooltip;
    }
    private static void drawInfusionBackdrop(GuiGraphics graphics, int x, int y) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        graphics.pose().pushPose();
        graphics.pose().translate(x, y + 20, 0);
        graphics.pose().scale(2f, 2f, 1f);
        graphics.blit(OVERLAY_TEXTURE, -28, -56, 0, 3, 56, 17);
        graphics.blit(OVERLAY_TEXTURE, -28, -36, 200, 77, 60, 44);
        graphics.pose().popPose();
    }
    private static List<net.minecraft.network.chat.Component> drawInfusionAspects(
            GuiGraphics graphics, AspectList aspects, int x, int y,
            int mouseX, int mouseY, Font font,
            List<net.minecraft.network.chat.Component> tooltip) {
        int total = 0;
        int rows = (aspects.size() - 1) / 5;
        int shift = (5 - aspects.size() % 5) * 10;
        int sx = x - 48;
        int sy = y + 50 - 10 * rows;
        for (Aspect tag : aspects.getAspectsSortedByName()) {
            int m = 0;
            if (total / 5 >= rows && (rows > 1 || aspects.size() < 5)) {
                m = 1;
            }
            int vx = sx + total % 5 * 20 + shift * m;
            int vy = sy + total / 5 * 20;
            int amount = aspects.getAmount(tag);
            List<net.minecraft.network.chat.Component> aspectTooltip =
                    AspectRenderer.drawAspectWithTooltip(graphics, vx, vy,
                            tag, amount, mouseX, mouseY);
            if (aspectTooltip != null && tooltip == null) {
                tooltip = aspectTooltip;
            }
            total++;
        }
        return tooltip;
    }
    private static List<net.minecraft.network.chat.Component> renderCrucibleTypeRecipe(
            GuiGraphics graphics, CrucibleRecipeType recipe, int x, int y,
            int mouseX, int mouseY, Font font) {
        List<net.minecraft.network.chat.Component> tooltip = null;
        String title = net.minecraft.network.chat.Component.translatable("recipe.type.crucible").getString();
        graphics.drawCenteredString(font, title, x, y - 104, 0x505050);
        drawCrucibleBackdrop(graphics, x, y);
        ItemStack output = recipe.getResultItem();
        renderItem(graphics, output, x - 8, y - 50);
        tooltip = checkItemTooltip(output, x - 8, y - 50, mouseX, mouseY, false, tooltip);
        ItemStack catalyst = cycleIngredient(recipe.getCatalyst(), 0);
        renderItem(graphics, catalyst, x - 64, y - 56);
        tooltip = checkItemTooltip(catalyst, x - 64, y - 56, mouseX, mouseY, true, tooltip);
        AspectList aspects = recipe.getAspects();
        if (aspects != null && aspects.size() > 0) {
            tooltip = drawCrucibleAspects(graphics, aspects, x, y, mouseX, mouseY, tooltip);
        }
        return tooltip;
    }
    private static List<net.minecraft.network.chat.Component> renderInfusionTypeRecipe(
            GuiGraphics graphics, InfusionRecipeType recipe, int x, int y,
            int mouseX, int mouseY, Font font) {
        List<net.minecraft.network.chat.Component> tooltip = null;
        String title = net.minecraft.network.chat.Component.translatable("recipe.type.infusion").getString();
        int titleOffset = font.width(title);
        graphics.drawString(font, title, x - titleOffset / 2, y - 104, 0x505050, false);
        drawInfusionBackdrop(graphics, x, y);
        ItemStack output = recipe.getResultItem();
        renderItem(graphics, output, x - 8, y - 85);
        tooltip = checkItemTooltip(output, x - 8, y - 85, mouseX, mouseY, false, tooltip);
        ItemStack central = cycleIngredient(recipe.getCentralItem(), 0);
        renderItem(graphics, central, x - 8, y - 16);
        tooltip = checkItemTooltip(central, x - 8, y - 16, mouseX, mouseY, true, tooltip);
        NonNullList<Ingredient> components = recipe.getComponents();
        int numComponents = components.size();
        int radius = 40;
        for (int i = 0; i < numComponents; i++) {
            double angle = (2 * Math.PI * i / numComponents) - Math.PI / 2;
            int compX = x + (int) (Math.cos(angle) * radius) - 8;
            int compY = (y - 8) + (int) (Math.sin(angle) * radius) - 8;
            ItemStack comp = cycleIngredient(components.get(i), i);
            renderItem(graphics, comp, compX, compY);
            tooltip = checkItemTooltip(comp, compX, compY, mouseX, mouseY, true, tooltip);
        }
        AspectList aspects = recipe.getAspects();
        if (aspects != null && aspects.size() > 0) {
            tooltip = drawInfusionAspects(graphics, aspects, x, y, mouseX, mouseY, font, tooltip);
        }
        int instability = recipe.getInstability();
        String instText = net.minecraft.network.chat.Component.translatable("tc.inst").getString()
                + " " + net.minecraft.network.chat.Component.translatable("tc.inst." + Math.min(5, instability)).getString();
        graphics.drawCenteredString(font, instText, x, y + 94, 0x505050);
        return tooltip;
    }
    public static void toggleBlueprintSlices() {
        cycleMultiblockLines = !cycleMultiblockLines;
    }
    private static List<net.minecraft.network.chat.Component> renderBlueprintRecipe(
            GuiGraphics graphics, ArcaneresearchApi.BluePrint blueprint, int x, int y,
            int mouseX, int mouseY, Font font) {
        List<net.minecraft.network.chat.Component> tooltip = null;
        if (blueprint.getParts() == null) {
            return null;
        }
        String key = blueprint.getParts().length + "x" + blueprint.getParts()[0].length
                + "x" + blueprint.getParts()[0][0].length + "@" + blueprint.getParts().hashCode();
        if (blueprintAccess == null || !key.equals(blueprintAccessKey)) {
            blueprintAccess = new BlueprintBlockAccess(blueprint.getParts());
            blueprintAccessKey = key;
        }
        long now = System.currentTimeMillis();
        if (cycleMultiblockLines && now - blueprintLastSlice > 1000L) {
            blueprintLastSlice = now;
            blueprintAccess.sliceLine++;
        }
        arcaneresearch.api.crafting.Part[][][] parts = blueprint.getParts();
        int ySize = parts.length;
        int xSize = parts[0].length;
        int zSize = parts[0][0].length;
        graphics.drawCenteredString(font,
                net.minecraft.network.chat.Component.translatable("recipe.type.construct").getString(),
                x, y - 104, 0x505050);
        float scale = Math.max(8f, 38 - Math.max(ySize, Math.max(xSize, zSize)) * 2);
        var blockRenderer = Minecraft.getInstance().getBlockRenderer();
        var buffer = graphics.bufferSource();
        graphics.pose().pushPose();
        graphics.pose().translate(x, y - 10, 200);
        graphics.pose().scale(scale, -scale, scale);
        graphics.pose().mulPose(com.mojang.math.Axis.XP.rotationDegrees(BLUEPRINT_ROT_X));
        graphics.pose().mulPose(com.mojang.math.Axis.YP.rotationDegrees(BLUEPRINT_ROT_Y));
        graphics.pose().translate(-xSize / 2f, -ySize / 2f, -zSize / 2f);
        for (int h = 0; h < ySize; ++h) {
            for (int l = 0; l < xSize; ++l) {
                for (int w = 0; w < zSize; ++w) {
                    // BlueprintBlockAccess кладёт слои зеркально по Y:
                    // structure[h] ← parts[ySize - 1 - h], иначе нода не рисуется.
                    arcaneresearch.api.crafting.Part part = parts[ySize - 1 - h][l][w];
                    net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(l, h, w);
                    net.minecraft.world.level.block.state.BlockState state = blueprintAccess.getBlockState(pos);
                    try {
                        graphics.pose().pushPose();
                        graphics.pose().translate(l, h, w);
                        // блоки рисуем как в мире: алтарь — через BER
                        BlueprintBlockAccess.renderBlueprintState(graphics.pose(), buffer, state, 15728880);
                        // нода рисуется и в пустой клетке (AIR вместо стекла-заглушки),
                        // но скрытые срезом слои гасятся вместе с блоками
                        if (!blueprintAccess.isHiddenBySlice(h)) {
                            BlueprintBlockAccess.renderNodeOverlay(graphics.pose(), buffer, part);
                        }
                        graphics.pose().popPose();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        graphics.pose().popPose();
        graphics.flush();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        boolean hoverToggle = mouseX >= x + 80 && mouseY >= y + 100
                && mouseX < x + 88 && mouseY < y + 108;
        RenderSystem.setShaderTexture(0, BOOK_TEXTURE);
        RenderSystem.setShaderColor(1f, 1f, 1f, hoverToggle ? 1f : 0.75f);
        graphics.blit(BOOK_TEXTURE, x + 80, y + 100,
                cycleMultiblockLines ? 168 : 160, 224, 8, 8);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        ItemStack[] ingredients = blueprint.getIngredientList();
        if (ingredients != null) {
            for (int a = 0; a < ingredients.length; a++) {
                if (ingredients[a] == null || ingredients[a].isEmpty()
                        || ingredients[a].getItem() == null) {
                    continue;
                }
                renderItem(graphics, ingredients[a], x - 85 + a * 17, y + 90);
                tooltip = checkItemTooltip(ingredients[a], x - 85 + a * 17, y + 90,
                        mouseX, mouseY, true, tooltip);
            }
        }
        RenderSystem.enableDepthTest();
        return tooltip;
    }
    public static void renderBlueprintBookmark(GuiGraphics graphics,
                                               ResourceLocation key, arcaneresearch.api.crafting.Part[][][] parts, int x, int y) {
        if (parts == null || parts.length == 0) {
            return;
        }
        BlueprintBlockAccess ba = blueprintIconAccess.get(key);
        if (ba == null) {
            ba = new BlueprintBlockAccess(parts, true);
            blueprintIconAccess.put(key, ba);
        }
        int ySize = parts.length;
        int xSize = parts[0].length;
        int zSize = parts[0][0].length;
        var blockRenderer = Minecraft.getInstance().getBlockRenderer();
        var buffer = graphics.bufferSource();
        float transX = x - xSize / 2f;
        float transY = y - (float) Math.sqrt(ySize * ySize + xSize * xSize + zSize * zSize) / 2f;
        graphics.pose().pushPose();
        graphics.pose().translate(transX, transY, Math.max(ySize, Math.max(xSize, zSize)));
        graphics.pose().scale(4f, -4f, 1f);
        graphics.pose().mulPose(com.mojang.math.Axis.XP.rotationDegrees(BLUEPRINT_ROT_X));
        graphics.pose().mulPose(com.mojang.math.Axis.YP.rotationDegrees(BLUEPRINT_ROT_Y));
        graphics.pose().translate(-zSize / 2f, -ySize / 2f, -xSize / 2f);
        for (int h = 0; h < ySize; ++h) {
            for (int l = 0; l < xSize; ++l) {
                for (int w = 0; w < zSize; ++w) {
                    net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(l, h, w);
                    net.minecraft.world.level.block.state.BlockState state = ba.getBlockState(pos);
                    if (state.isAir()) {
                        continue;
                    }
                    try {
                        graphics.pose().pushPose();
                        graphics.pose().translate(l, h, w);
                        blockRenderer.renderSingleBlock(state, graphics.pose(), buffer,
                                15728880, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                                net.minecraftforge.client.model.data.ModelData.EMPTY, null);
                        graphics.pose().popPose();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        graphics.pose().popPose();
        graphics.flush();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }
    private static void renderItem(GuiGraphics graphics, ItemStack stack, int x, int y) {
        GuiRenderHelper.renderItemWithDecorations(graphics, stack, x, y);
    }
    public static ItemStack getRecipeOutput(ResourceLocation recipeId) {
        Object recipe = findRecipe(recipeId);
        if (recipe == null) {
            return ItemStack.EMPTY;
        }
        var level = Minecraft.getInstance().level;
        try {
            if (recipe instanceof CrucibleRecipeType crucibleType) {
                return crucibleType.getResultItem();
            } else if (recipe instanceof InfusionRecipeType infusionType) {
                return infusionType.getResultItem();
            } else if (recipe instanceof ArcaneresearchApi.BluePrint blueprint) {
                ItemStack display = blueprint.getDisplayStack();
                return display != null ? display : ItemStack.EMPTY;
            } else if (recipe instanceof CrucibleRecipe crucible) {
                return crucible.getRecipeOutput();
            } else if (recipe instanceof InfusionRecipe infusion
                    && infusion.getRecipeOutput() instanceof ItemStack out) {
                return out;
            } else if (recipe instanceof IArcaneRecipe arcane && level != null) {
                return arcane.getResultItem(level.registryAccess());
            } else if (recipe instanceof CraftingRecipe crafting && level != null) {
                return crafting.getResultItem(level.registryAccess());
            }
        } catch (Exception ignored) {
        }
        return ItemStack.EMPTY;
    }
    private static List<net.minecraft.network.chat.Component> checkItemTooltip(
            ItemStack stack, int itemX, int itemY, int mouseX, int mouseY,
            boolean clickthrough,
            List<net.minecraft.network.chat.Component> existingTooltip) {
        if (stack.isEmpty()) return existingTooltip == null ? null : existingTooltip;
        clickables.add(new ClickableIngredient(stack.copy(), itemX, itemY));
        if (existingTooltip != null) return existingTooltip;
        if (modalOpen && !allowWithPagePopup) return null;
        if (mouseX >= itemX && mouseX < itemX + ITEM_SIZE &&
                mouseY >= itemY && mouseY < itemY + ITEM_SIZE) {
            java.util.ArrayList<net.minecraft.network.chat.Component> lines = new java.util.ArrayList<>(
                    stack.getTooltipLines(Minecraft.getInstance().player,
                            Minecraft.getInstance().options.advancedItemTooltips ?
                                    net.minecraft.world.item.TooltipFlag.Default.ADVANCED :
                                    net.minecraft.world.item.TooltipFlag.Default.NORMAL));
            if (clickthrough) {
                String ref = arcaneresearch.client.gui.screens.ResearchPageScreen
                        .findCraftingRecipe(Minecraft.getInstance().player, stack);
                if (ref != null) {
                    String[] sr = ref.split(";", 2);
                    if (sr.length > 1) {
                        ResourceLocation res = new ResourceLocation(sr[0]);
                        if (res.getPath().equals("UNKNOWN")) {
                            lines.add(net.minecraft.network.chat.Component.translatable("recipe.unknown")
                                    .withStyle(net.minecraft.ChatFormatting.DARK_RED,
                                            net.minecraft.ChatFormatting.ITALIC));
                        } else {
                            lines.add(net.minecraft.network.chat.Component.translatable("recipe.clickthrough")
                                    .withStyle(net.minecraft.ChatFormatting.BLUE,
                                            net.minecraft.ChatFormatting.ITALIC));
                            try {
                                reference.add(new DrilldownRef(mouseX, mouseY, res,
                                        Integer.parseInt(sr[1])));
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                }
            }
            return lines;
        }
        return null;
    }
    private static List<net.minecraft.network.chat.Component> checkPopup(
            int ix, int iy, int w, int h, int mouseX, int mouseY, String key,
            List<net.minecraft.network.chat.Component> existingTooltip) {
        if (existingTooltip != null) return existingTooltip;
        if (modalOpen && !allowWithPagePopup) return null;
        if (mouseX >= ix && mouseY >= iy && mouseX < ix + w && mouseY < iy + h) {
            return List.of(net.minecraft.network.chat.Component.translatable(key));
        }
        return null;
    }
    public static List<net.minecraft.network.chat.Component> drawStackAt(
            GuiGraphics graphics, ItemStack stack, int x, int y,
            int mouseX, int mouseY, boolean clickthrough,
            List<net.minecraft.network.chat.Component> existingTooltip) {
        renderItem(graphics, stack, x, y);
        return checkItemTooltip(stack, x, y, mouseX, mouseY, clickthrough, existingTooltip);
    }
    public record ClickableIngredient(ItemStack stack, int x, int y) {
    }
    public record DrilldownRef(int x, int y, ResourceLocation recipe, int page) {
    }
}
