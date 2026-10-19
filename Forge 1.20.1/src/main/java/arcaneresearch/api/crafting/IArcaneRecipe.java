package arcaneresearch.api.crafting;
import net.minecraft.world.item.crafting.Recipe;
import arcaneresearch.api.aspects.AspectList;
public interface IArcaneRecipe extends Recipe<IArcaneWorkbench>, IArcaneresearchRecipe {
    int getVis();
    @Override
    String getResearch();
    AspectList getCrystals();
    @Override
    default String getGroup() {
        return IArcaneresearchRecipe.super.getGroup();
    }
}
