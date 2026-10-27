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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.compat.jei.ArcaneresearchJEIPlugin;
import arcaneresearch.init.ModItems;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
public class AspectCompoundCategory implements IRecipeCategory<Aspect> {
    public static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "aspect_compound");

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

    private static void drawCenteredSmall(GuiGraphics guiGraphics, Font font, String text, int cx2) {
        if (guiGraphics == null || font == null || text == null) {
            return;
        }
        guiGraphics.drawString(font, Component.literal(text).withStyle(ChatFormatting.DARK_GRAY),
                cx2 - font.width(text) / 2, 36, 0xFFFFFF, false);
    }

    public static List<Aspect> recipes() {
        List<Aspect> out = new ArrayList<>();
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
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public IDrawable getBackground() {
        return background;
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
    public void draw(@NotNull Aspect recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        if (background != null) {
            background.draw(guiGraphics, 0, 0);
        }

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        guiGraphics.drawString(font,
                Component.literal("+").withStyle(ChatFormatting.DARK_GRAY),
                36, 6, 0xFFFFFF, false);
        guiGraphics.drawString(font,
                Component.literal("=").withStyle(ChatFormatting.DARK_GRAY),
                72, 6, 0xFFFFFF, false);

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

        var pose = guiGraphics.pose();
        pose.pushPose();
        try {
            pose.scale(0.5f, 0.5f, 1f);
            drawCenteredSmall(guiGraphics, font, name0, 32);
            drawCenteredSmall(guiGraphics, font, name1, 108);
            drawCenteredSmall(guiGraphics, font, nameOut, 180);
        } finally {
            pose.popPose();
        }
    }
}