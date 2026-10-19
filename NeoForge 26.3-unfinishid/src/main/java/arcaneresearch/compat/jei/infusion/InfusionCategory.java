package arcaneresearch.compat.jei.infusion;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor; // NEOFORGE_PORT: was GuiGraphics
import mezz.jei.api.gui.builder.ITooltipBuilder; // NEOFORGE_PORT: getTooltipStrings -> getTooltip
import net.minecraft.client.renderer.RenderPipelines; // NEOFORGE_PORT: blit needs a pipelineExtractor
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.client.lib.AspectRenderer;
import arcaneresearch.common.lib.crafting.InfusionRecipeType;
import arcaneresearch.compat.jei.JeiResearch;
import arcaneresearch.compat.jei.ArcaneresearchJEIPlugin;
import arcaneresearch.init.ModBlocks;

import java.util.ArrayList;
import java.util.List;

public class InfusionCategory implements IRecipeCategory<InfusionRecipeType> {

    public static final Identifier UID = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "infusion");

    private static final Identifier OVERLAY_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_researchbook_overlay.png");

    private static final int WIDTH = 140;
    private static final int HEIGHT = 205;

    private static final int OX = 75;
    private static final int OY = 95;

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public InfusionCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.INFUSION_MATRIX.get()));
        this.title = Component.translatable("gui.arcaneresearch.infusion");
    }

    private static List<AspectSpot> layoutAspects(AspectList aspects) {
        List<AspectSpot> spots = new ArrayList<>();
        if (aspects == null || aspects.size() == 0) {
            return spots;
        }
        int total = 0;
        int rows = (aspects.size() - 1) / 5;
        int shift = (5 - aspects.size() % 5) * 10;
        int sx = OX - 48;
        int sy = OY + 50 - 10 * rows;
        for (Aspect tag : aspects.getAspectsSortedByName()) {
            int m = 0;
            if (total / 5 >= rows && (rows > 1 || aspects.size() < 5)) {
                m = 1;
            }
            int vx = sx + total % 5 * 20 + shift * m;
            int vy = sy + total / 5 * 20;
            spots.add(new AspectSpot(tag, aspects.getAmount(tag), vx, vy));
            total++;
        }
        return spots;
    }

    @Override
    public RecipeType<InfusionRecipeType> getRecipeType() {
        return ArcaneresearchJEIPlugin.INFUSION_TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    // NEOFORGE_PORT: IRecipeCategory.getBackground removed in JEI 26.3;
    // getWidth/getHeight are mandatory instead (background field kept unused).
    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, InfusionRecipeType recipe, IFocusGroup focuses) {
        // NEOFORGE_PORT: Neo InfusionRecipeType.getResultItem() is no-arg.
        builder.addSlot(RecipeIngredientRole.OUTPUT, OX - 9, OY - 86)
                .addItemStack(recipe.getResultItem());

        Ingredient centralItem = recipe.getCentralItem();
        if (centralItem != null && !centralItem.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, OX - 9, OY - 17)
                    .addIngredients(centralItem);
        }

        NonNullList<Ingredient> components = recipe.getComponents();
        int numComponents = components.size();
        int radius = 40;
        for (int i = 0; i < numComponents; i++) {
            double angle = (2 * Math.PI * i / numComponents) - Math.PI / 2;
            int compX = OX + (int) (Math.cos(angle) * radius) - 9;
            int compY = (OY - 8) + (int) (Math.sin(angle) * radius) - 9;
            Ingredient component = components.get(i);
            if (!component.isEmpty()) {
                builder.addSlot(RecipeIngredientRole.INPUT, compX, compY)
                        .addIngredients(component);
            }
        }
    }

    @Override
    public void draw(InfusionRecipeType recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(OX, OY + 20);
        guiGraphics.pose().scale(2f, 2f);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, OVERLAY_TEXTURE, -28, -56, 0, 3, 56, 17, 512, 512);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, OVERLAY_TEXTURE, -28, -36, 200, 77, 60, 44, 512, 512);
        guiGraphics.pose().popMatrix();

        for (AspectSpot spot : layoutAspects(recipe.getAspects())) {
            AspectRenderer.drawAspect(guiGraphics, spot.x(), spot.y(), spot.aspect(), spot.amount());
        }

        int instability = recipe.getInstability();
        if (instability > 0) {
            String instText = "Instability: " + instability;
            int instColor = instability > 3 ? 0xFFFF6060 : (instability > 1 ? 0xFFFFFF60 : 0xFF60FF60);
            guiGraphics.centeredText(font, instText, OX, OY + 94, instColor);
        }

        JeiResearch.drawTopRight(guiGraphics, recipe.getResearch(), WIDTH);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, InfusionRecipeType recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        for (AspectSpot spot : layoutAspects(recipe.getAspects())) {
            if (AspectRenderer.isMouseOverAspect(spot.x(), spot.y(), (int) mouseX, (int) mouseY)) {
                tooltip.addAll(AspectRenderer.getAspectTooltip(spot.aspect(), spot.amount()));
                return;
            }
        }
        tooltip.addAll(JeiResearch.tooltip(recipe.getResearch(), mouseX, mouseY, WIDTH));
    }

    private record AspectSpot(Aspect aspect, int amount, int x, int y) {
    }
}
