package arcaneresearch.compat.jei.aspect;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries; // NEOFORGE_PORT: was ForgeRegistries
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectHelper;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.compat.jei.ArcaneresearchJEIPlugin;
import arcaneresearch.init.ModItems;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AspectSourcesCategory implements IRecipeCategory<AspectSourcesCategory.Sources> {

    public static final Identifier UID = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "aspect_sources");

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
            byTag.put(aspect.getTag(), aspect);
        }
        for (Aspect aspect : Aspect.getCompoundAspects()) {
            byTag.put(aspect.getTag(), aspect);
        }
        Map<Aspect, List<ItemStack>> index = new LinkedHashMap<>();
        // NEOFORGE_PORT: Registry extends IdMap extends Iterable (same iteration).
        for (Item item : BuiltInRegistries.ITEM) {
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
                Aspect canonical = byTag.get(aspect.getTag());
                if (canonical == null) {
                    continue;
                }
                index.computeIfAbsent(canonical, a -> new ArrayList<>()).add(stack);
            }
        }
        List<Sources> recipes = new ArrayList<>();
        for (Map.Entry<Aspect, List<ItemStack>> entry : index.entrySet()) {
            List<ItemStack> items = entry.getValue();
            for (int start = 0; start < items.size(); start += PAGE_SIZE) {
                recipes.add(new Sources(entry.getKey(),
                        new ArrayList<>(items.subList(start, Math.min(start + PAGE_SIZE, items.size())))));
            }
        }
        return recipes;
    }

    @Override
    public RecipeType<Sources> getRecipeType() {
        return ArcaneresearchJEIPlugin.ASPECT_SOURCES_TYPE;
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
    public void setRecipe(IRecipeLayoutBuilder builder, Sources recipe, IFocusGroup focuses) {

        builder.addSlot(RecipeIngredientRole.INPUT, 72, 4)
                .addIngredients(AspectIngredient.TYPE, List.of(new AspectList().add(recipe.aspect(), 1)));

        List<ItemStack> items = recipe.items();
        for (int i = 0; i < items.size() && i < PAGE_SIZE; i++) {
            int col = i % 9;
            int row = i / 9;
            builder.addSlot(RecipeIngredientRole.OUTPUT, col * 18, 30 + row * 18)
                    .addItemStack(items.get(i));
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
