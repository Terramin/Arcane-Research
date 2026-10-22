package arcaneresearch.common.lib.crafting;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import arcaneresearch.api.aspects.AspectList;

import java.util.List;
import java.util.Optional;


@Deprecated
public class ShapedArcaneVoidJar extends ShapedArcaneVoidJarRecipe {
    public ShapedArcaneVoidJar(Identifier id, String group, int width, int height,
                               List<Optional<Ingredient>> ingredients, ItemStackTemplate result,
                               int visCost, AspectList crystals, String research) {
        super(id, group, width, height, ingredients, result, visCost, crystals, research);
    }
}
