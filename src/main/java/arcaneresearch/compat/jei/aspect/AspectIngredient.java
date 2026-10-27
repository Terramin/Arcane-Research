package arcaneresearch.compat.jei.aspect;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class AspectIngredient {
    public static final IIngredientType<AspectList> TYPE = () -> AspectList.class;

    private AspectIngredient() {
    }

    public static List<AspectList> createAll() {
        List<AspectList> aspects = new ArrayList<>();
        List<Aspect> primal = Aspect.getPrimalAspects();
        if (primal != null) {
            for (Aspect aspect : primal) {
                if (aspect != null) {
                    aspects.add(new AspectList().add(aspect, 1));
                }
            }
        }
        List<Aspect> compound = Aspect.getCompoundAspects();
        if (compound != null) {
            for (Aspect aspect : compound) {
                if (aspect != null) {
                    aspects.add(new AspectList().add(aspect, 1));
                }
            }
        }
        return aspects;
    }

    public static Aspect first(AspectList list) {
        if (list == null || list.size() == 0) {
            return null;
        }
        Aspect[] arr = list.getAspects();
        if (arr == null || arr.length == 0) {
            return null;
        }
        return arr[0];
    }

    public static class Helper implements IIngredientHelper<AspectList> {
        @Override
        public @NotNull IIngredientType<AspectList> getIngredientType() {
            return TYPE;
        }

        @Override
        public @NotNull String getDisplayName(@NotNull AspectList ingredient) {
            Aspect aspect = first(ingredient);
            if (aspect == null) {
                return "";
            }
            String name = aspect.getName();
            return name == null ? "" : name;
        }

        @Override
        public @NotNull String getUniqueId(@NotNull AspectList ingredient, @NotNull UidContext context) {
            Aspect aspect = first(ingredient);
            if (aspect == null) {
                return "empty";
            }
            String tag = aspect.getTag();
            return tag == null ? "empty" : tag;
        }

        @Override
        public @NotNull ResourceLocation getResourceLocation(@NotNull AspectList ingredient) {
            Aspect aspect = first(ingredient);
            String path = "empty";
            if (aspect != null) {
                String tag = aspect.getTag();
                if (tag != null && !tag.isEmpty()) {
                    path = tag.toLowerCase(Locale.ROOT);
                }
            }
            return ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, path);
        }

        @Override
        public @NotNull AspectList copyIngredient(@NotNull AspectList ingredient) {
            AspectList copy = ingredient.copy();
            return copy == null ? new AspectList() : copy;
        }

        @Override
        public @NotNull String getErrorInfo(AspectList ingredient) {
            return ingredient == null ? "null" : ingredient.toString();
        }
    }
}