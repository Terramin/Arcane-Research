package arcaneresearch.compat.jei.aspect;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class AspectIngredient {
    public static final IIngredientType<AspectList> TYPE = () -> AspectList.class;
    private AspectIngredient() {
    }
    public static List<AspectList> createAll() {
        List<AspectList> aspects = new ArrayList<>();
        aspects.addAll(Aspect.getPrimalAspects().stream()
                .map(aspect -> new AspectList().add(aspect, 1))
                .collect(Collectors.toList()));
        aspects.addAll(Aspect.getCompoundAspects().stream()
                .map(aspect -> new AspectList().add(aspect, 1))
                .collect(Collectors.toList()));
        return aspects;
    }
    public static Aspect first(AspectList list) {
        if (list == null || list.size() == 0) {
            return null;
        }
        return list.getAspects()[0];
    }
    public static class Helper implements IIngredientHelper<AspectList> {
        @Override
        public IIngredientType<AspectList> getIngredientType() {
            return TYPE;
        }
        @Override
        public String getDisplayName(AspectList ingredient) {
            Aspect aspect = first(ingredient);
            return aspect != null ? aspect.getName() : "";
        }
        @Override
        public String getUniqueId(AspectList ingredient, UidContext context) {
            Aspect aspect = first(ingredient);
            return aspect != null ? aspect.getTag() : "empty";
        }
        @Override
        public ResourceLocation getResourceLocation(AspectList ingredient) {
            Aspect aspect = first(ingredient);
            return new ResourceLocation(Arcaneresearch.MODID,
                    aspect != null ? aspect.getTag().toLowerCase(java.util.Locale.ROOT) : "empty");
        }
        @Override
        public AspectList copyIngredient(AspectList ingredient) {
            return ingredient == null ? new AspectList() : ingredient.copy();
        }
        @Override
        public String getErrorInfo(AspectList ingredient) {
            return ingredient == null ? "null" : ingredient.toString();
        }
    }
}
