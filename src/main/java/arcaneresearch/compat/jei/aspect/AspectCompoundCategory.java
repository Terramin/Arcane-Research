package arcaneresearch.compat.jei.aspect;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor; 
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; 
import net.minecraft.world.item.ItemStack;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.compat.jei.ArcaneresearchJEIPlugin;
import arcaneresearch.init.ModItems;

import java.util.List;
import java.util.stream.Collectors;

public class AspectCompoundCategory implements IRecipeCategory<Aspect> {

    public static final Identifier UID = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "aspect_compound");

    private static final int WIDTH = 108;
    private static final int HEIGHT = 40;

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public AspectCompoundCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModItems.SALIS_MUNDUS.get()));
        this.title = Component.translatable("gui.arcaneresearch.aspect_compound");
    }

    private static void drawCenteredSmall(GuiGraphicsExtractor guiGraphics, Font font, String text, int cx2, int y2) {
        guiGraphics.text(font, Component.literal(text).withStyle(ChatFormatting.DARK_GRAY),
                cx2 - font.width(text) / 2, y2, 0xFFFFFFFF, false);
    }

    public static List<Aspect> recipes() {
        return Aspect.getCompoundAspects().stream()
                .filter(a -> a.getComponents() != null && a.getComponents().length >= 2)
                .collect(Collectors.toList());
    }

    @Override
    public RecipeType<Aspect> getRecipeType() {
        return ArcaneresearchJEIPlugin.ASPECT_COMPOUND_TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    
    
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
    public void setRecipe(IRecipeLayoutBuilder builder, Aspect recipe, IFocusGroup focuses) {
        Aspect[] components = recipe.getComponents();

        builder.addSlot(RecipeIngredientRole.OUTPUT, 82, 2)
                .addIngredients(AspectIngredient.TYPE, List.of(new AspectList().add(recipe, 1)));

        if (components != null && components.length >= 2) {
            builder.addSlot(RecipeIngredientRole.INPUT, 8, 2)
                    .addIngredients(AspectIngredient.TYPE, List.of(new AspectList().add(components[0], 1)));
            builder.addSlot(RecipeIngredientRole.INPUT, 46, 2)
                    .addIngredients(AspectIngredient.TYPE, List.of(new AspectList().add(components[1], 1)));
        }
    }

    @Override
    public void draw(Aspect recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        guiGraphics.text(font, Component.literal("+").withStyle(ChatFormatting.DARK_GRAY), 36, 6, 0xFFFFFFFF, false);
        guiGraphics.text(font, Component.literal("=").withStyle(ChatFormatting.DARK_GRAY), 72, 6, 0xFFFFFFFF, false);

        Aspect[] components = recipe.getComponents();
        if (components != null && components.length >= 2) {
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().scale(0.5f, 0.5f);

            drawCenteredSmall(guiGraphics, font, components[0].getName(), 32, 36);
            drawCenteredSmall(guiGraphics, font, components[1].getName(), 108, 36);
            drawCenteredSmall(guiGraphics, font, recipe.getName(), 180, 36);
            guiGraphics.pose().popMatrix();
        }
    }
}
