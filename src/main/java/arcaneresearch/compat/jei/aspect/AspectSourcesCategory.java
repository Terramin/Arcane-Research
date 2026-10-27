package arcaneresearch.compat.jei.aspect;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectHelper;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.compat.jei.ArcaneresearchJEIPlugin;
import arcaneresearch.init.ModItems;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
public class AspectSourcesCategory implements IRecipeCategory<AspectSourcesCategory.Sources> {
    public static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "aspect_sources");

    private static final int WIDTH = 162;
    private static final int HEIGHT = 76;
    private static final int PAGE_SIZE = 18;

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public AspectSourcesCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModItems.THAUMONOMICON.get()));
        this.title = Component.translatable("gui.arcaneresearch.aspect_sources");
    }

    public static List<Sources> buildIndex() {
        Map<String, Aspect> byTag = new LinkedHashMap<>();

        for (Aspect aspect : Aspect.getPrimalAspects()) {
            if (aspect == null) {
                continue;
            }
            String tag = aspect.getTag();
            if (tag != null) {
                byTag.put(tag, aspect);
            }
        }

        for (Aspect aspect : Aspect.getCompoundAspects()) {
            if (aspect == null) {
                continue;
            }
            String tag = aspect.getTag();
            if (tag != null) {
                byTag.put(tag, aspect);
            }
        }

        Map<Aspect, List<ItemStack>> index = new LinkedHashMap<>();
        for (Item item : ForgeRegistries.ITEMS) {
            if (item == null) {
                continue;
            }
            ItemStack stack;
            try {
                stack = new ItemStack(item);
            } catch (Exception ignored) {
                continue;
            }
            if (stack.isEmpty()) {
                continue;
            }
            AspectList aspects;
            try {
                aspects = AspectHelper.getObjectAspects(stack);
            } catch (Exception ignored) {
                continue;
            }
            if (aspects == null || aspects.size() == 0) {
                continue;
            }
            for (Aspect aspect : aspects.getAspects()) {
                if (aspect == null) {
                    continue;
                }
                String tag = aspect.getTag();
                if (tag == null) {
                    continue;
                }
                Aspect canonical = byTag.get(tag);
                if (canonical == null) {
                    continue;
                }
                index.computeIfAbsent(canonical, a -> new ArrayList<>()).add(stack);
            }
        }

        List<Sources> recipes = new ArrayList<>();
        for (Map.Entry<Aspect, List<ItemStack>> entry : index.entrySet()) {
            List<ItemStack> items = entry.getValue();
            int size = items.size();
            for (int start = 0; start < size; start += PAGE_SIZE) {
                recipes.add(new Sources(entry.getKey(),
                        new ArrayList<>(items.subList(start, Math.min(start + PAGE_SIZE, size)))));
            }
        }
        return recipes;
    }

    @Override
    public @NotNull RecipeType<Sources> getRecipeType() {
        return ArcaneresearchJEIPlugin.ASPECT_SOURCES_TYPE;
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
    public void draw(@NotNull Sources recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        if (background != null) {
            background.draw(guiGraphics, 0, 0);
        }
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull Sources recipe, @NotNull IFocusGroup focuses) {
        Aspect aspect = recipe.aspect();
        if (aspect != null) {
            AspectList aspectList = new AspectList();
            aspectList.add(aspect, 1);
            builder.addSlot(RecipeIngredientRole.INPUT, 72, 4)
                    .addIngredients(AspectIngredient.TYPE, List.of(aspectList));
        }
        List<ItemStack> items = recipe.items();
        if (items == null) {
            return;
        }
        int limit = Math.min(items.size(), PAGE_SIZE);
        for (int i = 0; i < limit; i++) {
            ItemStack stack = items.get(i);
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            int col = i % 9;
            int row = i / 9;
            builder.addSlot(RecipeIngredientRole.OUTPUT, col * 18, 30 + row * 18)
                    .addItemStack(stack);
        }
    }

    public static final class Sources {
        private final Aspect aspect;
        private final List<ItemStack> items;

        public Sources(Aspect aspect, List<ItemStack> items) {
            this.aspect = aspect;
            this.items = items;
        }

        public Aspect aspect() {
            return aspect;
        }

        public List<ItemStack> items() {
            return items;
        }
    }
}