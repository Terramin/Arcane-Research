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

import org.jetbrains.annotations.NotNull;

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

    private static void drawCenteredSmall(GuiGraphicsExtractor guiGraphics, Font font, String text, int cx2) {
        if (guiGraphics == null || font == null || text == null) {
            return;
        }
        guiGraphics.text(font, Component.literal(text).withStyle(ChatFormatting.DARK_GRAY),
                cx2 - font.width(text) / 2, 36, 0xFFFFFFFF, false);
    }

    public static List<Aspect> recipes() {
        List<Aspect> out = new java.util.ArrayList<>();
        List<Aspect> compound = Aspect.getCompoundAspects();
        if (compound == null) {
            return out;
        }
        for (Aspect a : compound) {
            if (a == null) {
                continue;
            }
            Aspect[] comp = a.getComponents();
            if (comp != null && comp.length >= 2) {
                out.add(a);
            }
        }
        return out;
    }

    @Override
    public @NotNull RecipeType<Aspect> getRecipeType() {
        return ArcaneresearchJEIPlugin.ASPECT_COMPOUND_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
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
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull Aspect recipe, @NotNull IFocusGroup focuses) {
        AspectList output = new AspectList();
        output.add(recipe, 1);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 82, 2)
                .addIngredients(AspectIngredient.TYPE, List.of(output));

        Aspect[] components = recipe.getComponents();
        if (components != null && components.length >= 2
                && components[0] != null && components[1] != null) {
            AspectList left = new AspectList();
            left.add(components[0], 1);
            builder.addSlot(RecipeIngredientRole.INPUT, 8, 2)
                    .addIngredients(AspectIngredient.TYPE, List.of(left));

            AspectList right = new AspectList();
            right.add(components[1], 1);
            builder.addSlot(RecipeIngredientRole.INPUT, 46, 2)
                    .addIngredients(AspectIngredient.TYPE, List.of(right));
        }
    }

    @Override
    public void draw(@NotNull Aspect recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        guiGraphics.text(font, Component.literal("+").withStyle(ChatFormatting.DARK_GRAY), 36, 6, 0xFFFFFFFF, false);
        guiGraphics.text(font, Component.literal("=").withStyle(ChatFormatting.DARK_GRAY), 72, 6, 0xFFFFFFFF, false);

        Aspect[] components = recipe.getComponents();
        if (components == null || components.length < 2
                || components[0] == null || components[1] == null) {
            return;
        }

        String name0 = components[0].getName();
        String name1 = components[1].getName();
        String nameOut = recipe.getName();
        if (name0 == null) name0 = "";
        if (name1 == null) name1 = "";
        if (nameOut == null) nameOut = "";

        guiGraphics.pose().pushMatrix();
        try {
            guiGraphics.pose().scale(0.5f, 0.5f);
            drawCenteredSmall(guiGraphics, font, name0, 32);
            drawCenteredSmall(guiGraphics, font, name1, 108);
            drawCenteredSmall(guiGraphics, font, nameOut, 180);
        } finally {
            guiGraphics.pose().popMatrix();
        }
    }
}
