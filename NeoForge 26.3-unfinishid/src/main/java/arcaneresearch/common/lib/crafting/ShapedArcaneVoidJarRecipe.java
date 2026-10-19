package arcaneresearch.common.lib.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.crafting.IArcaneWorkbench;
import arcaneresearch.init.ModBlocks;

import java.util.List;
import java.util.Optional;

// NEOFORGE_PORT: 1:1 from Forge common/lib/crafting/ShapedArcaneVoidJarRecipe.java.
// Only the 26.x recipe shape changed: parent ctor takes List<Optional<Ingredient>>,
// assemble(IArcaneWorkbench) single-arg, codecs map through the parent codec and rewrap
// (same JSON keys as arcane_workbench_shaped). Jar-NBT copy logic VERBATIM.
public class ShapedArcaneVoidJarRecipe extends ShapedArcaneRecipe {

    public static final MapCodec<ShapedArcaneVoidJarRecipe> MAP_CODEC =
            ShapedArcaneRecipe.MAP_CODEC.xmap(
                    base -> new ShapedArcaneVoidJarRecipe(base.getId(), base.getGroup(),
                            base.getWidth(), base.getHeight(), base.getOptionalIngredients(),
                            base.getResultTemplate(), base.getVis(), base.getCrystals(), base.getResearch()),
                    jar -> new ShapedArcaneRecipe(jar.getId(), jar.getGroup(),
                            jar.getWidth(), jar.getHeight(), jar.getOptionalIngredients(),
                            jar.getResultTemplate(), jar.getVis(), jar.getCrystals(), jar.getResearch()));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShapedArcaneVoidJarRecipe> STREAM_CODEC =
            ShapedArcaneRecipe.STREAM_CODEC.map(
                    base -> new ShapedArcaneVoidJarRecipe(base.getId(), base.getGroup(),
                            base.getWidth(), base.getHeight(), base.getOptionalIngredients(),
                            base.getResultTemplate(), base.getVis(), base.getCrystals(), base.getResearch()),
                    jar -> new ShapedArcaneRecipe(jar.getId(), jar.getGroup(),
                            jar.getWidth(), jar.getHeight(), jar.getOptionalIngredients(),
                            jar.getResultTemplate(), jar.getVis(), jar.getCrystals(), jar.getResearch()));

    public static final RecipeSerializer<ShapedArcaneVoidJarRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public ShapedArcaneVoidJarRecipe(Identifier id, String group, int width, int height,
                                     List<Optional<Ingredient>> ingredients, ItemStackTemplate result,
                                     int visCost, AspectList crystals, String research) {
        super(id, group, width, height, ingredients, result, visCost, crystals, research);
    }

    @Override
    public ItemStack assemble(IArcaneWorkbench container) {
        ItemStack res = super.assemble(container);
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack in = container.getItem(i);
            if (!in.isEmpty() && Block.byItem(in.getItem()) == ModBlocks.JAR_NORMAL.get()) {
                net.minecraft.world.item.component.CustomData inData = in.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
                if (inData != null) {
                    res.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, inData);
                }
                break;
            }
        }
        return res;
    }

    @Override
    public net.minecraft.world.item.crafting.RecipeSerializer<ShapedArcaneVoidJarRecipe> getSerializer() {
        return Serializer.INSTANCE;
    }

    // NEOFORGE_PORT: kept so existing references keep resolving; the registered
    // serializer instance is SERIALIZER above.
    public static class Serializer {
        public static final RecipeSerializer<ShapedArcaneVoidJarRecipe> INSTANCE = SERIALIZER;
    }
}
