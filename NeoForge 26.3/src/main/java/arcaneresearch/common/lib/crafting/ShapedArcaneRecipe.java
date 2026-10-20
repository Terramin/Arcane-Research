package arcaneresearch.common.lib.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.crafting.IArcaneRecipe;
import arcaneresearch.api.crafting.IArcaneWorkbench;
import arcaneresearch.init.ModRecipeTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// NEOFORGE_PORT: gameplay logic 1:1 from Forge common/lib/crafting/ShapedArcaneRecipe.java.
// 26.x changes only:
//   - Old Serializer (fromJson/fromNetwork/toNetwork) replaced by MAP_CODEC/STREAM_CODEC
//     + `new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC)` (mirrors vanilla ShapedRecipe).
//     JSON keys unchanged ("group", "research", "vis", "crystals", key+pattern via
//     ShapedRecipePattern.MAP_CODEC, "result" as ItemStack.CODEC), so data files parse
//     unchanged. The vanilla id parameter is gone (recipes are holder-keyed); id is kept
//     as a nullable runtime field for research-display paths (see IArcaneRecipe#getId).
//   - Ingredient.EMPTY gone: grid is List<Optional<Ingredient>> (empty = Optional.empty()).
//   - matches(IArcaneWorkbench, Level) grid-scan VERBATIM (no crystal check here —
//     crystals are validated separately by ArcaneWorkbenchMenu, same as Forge).
//   - assemble(IArcaneWorkbench) single-arg; placementInfo() via
//     PlacementInfo.createFromOptionals (mirrors vanilla ShapedRecipe).
public class ShapedArcaneRecipe implements IArcaneRecipe {

    public static final MapCodec<ShapedArcaneRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
            Codec.STRING.optionalFieldOf("research", "").forGetter(r -> r.research),
            Codec.INT.optionalFieldOf("vis", 0).forGetter(r -> r.visCost),
            AspectList.MAP_CODEC.codec().optionalFieldOf("crystals", new AspectList()).forGetter(r -> r.crystals != null ? r.crystals : new AspectList()),
            ShapedRecipePattern.MAP_CODEC.forGetter(ShapedArcaneRecipe::toPattern),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result)
    ).apply(inst, (group, research, visCost, crystals, pattern, result) ->
            new ShapedArcaneRecipe(null, group, pattern.width(), pattern.height(),
                    pattern.ingredients(), result, visCost, crystals, research)));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShapedArcaneRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, r -> r.group,
            ByteBufCodecs.STRING_UTF8, r -> r.research,
            ByteBufCodecs.VAR_INT, r -> r.visCost,
            AspectList.STREAM_CODEC, r -> r.crystals != null ? r.crystals : new AspectList(),
            ShapedRecipePattern.STREAM_CODEC, ShapedArcaneRecipe::toPattern,
            ItemStackTemplate.STREAM_CODEC, r -> r.result,
            (group, research, visCost, crystals, pattern, result) ->
                    new ShapedArcaneRecipe(null, group, pattern.width(), pattern.height(),
                            pattern.ingredients(), result, visCost, crystals, research)
    );

    public static final RecipeSerializer<ShapedArcaneRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private static ShapedRecipePattern toPattern(ShapedArcaneRecipe recipe) {
        return new ShapedRecipePattern(recipe.width, recipe.height, recipe.ingredients, Optional.empty());
    }

    private final Identifier id;
    private final String group;
    private final int width;
    private final int height;
    private final List<Optional<Ingredient>> ingredients;
    private final ItemStackTemplate result;
    private final int visCost;
    private final AspectList crystals;
    private final String research;

    public ShapedArcaneRecipe(Identifier id, String group, int width, int height,
                              List<Optional<Ingredient>> ingredients, ItemStackTemplate result,
                              int visCost, AspectList crystals, String research) {
        this.id = id;
        this.group = group;
        this.width = width;
        this.height = height;
        this.ingredients = ingredients;
        this.result = result;
        this.visCost = visCost;
        this.crystals = crystals;
        this.research = research;
    }

    @Override
    public boolean matches(IArcaneWorkbench container, Level level) {

        for (int x = 0; x <= 3 - width; x++) {
            for (int y = 0; y <= 3 - height; y++) {
                if (matches(container, x, y, true) || matches(container, x, y, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matches(IArcaneWorkbench container, int offsetX, int offsetY, boolean mirrored) {
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                int checkX = x - offsetX;
                int checkY = y - offsetY;

                Optional<Ingredient> ingredient = Optional.empty();
                if (checkX >= 0 && checkY >= 0 && checkX < width && checkY < height) {
                    if (mirrored) {
                        ingredient = ingredients.get(width - checkX - 1 + checkY * width);
                    } else {
                        ingredient = ingredients.get(checkX + checkY * width);
                    }
                }

                if (!Ingredient.testOptionalIngredient(ingredient, container.getItem(x + y * 3))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(IArcaneWorkbench container) {
        return result.create();
    }

    @Override
    public ItemStack getResultItem() {
        return result.create();
    }

    // NEOFORGE_PORT 26.3: template accessor so codecs/xmap can rewrap without
    // calling create() during datapack decode (create() touches components,
    // which are unbound until freeze — same "does not have components yet"
    // crash as ItemStack.CODEC). See docs.neoforged.net custom recipes:
    // result is ItemStackTemplate, assemble via template.create().
    public ItemStackTemplate getResultTemplate() {
        return result;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<? extends Recipe<arcaneresearch.api.crafting.IArcaneWorkbench>> getSerializer() { // NEOFORGE_PORT: wildcard bound (parameterized types are invariant; concrete type would block ShapedArcaneVoidJarRecipe override)
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<IArcaneRecipe> getType() { // NEOFORGE_PORT: Recipe bound requires concrete type
        return ModRecipeTypes.ARCANE_WORKBENCH.get();
    }

    @Override
    public List<Ingredient> getIngredients() {
        List<Ingredient> present = new ArrayList<>(ingredients.size());
        for (Optional<Ingredient> maybe : ingredients) {
            maybe.ifPresent(present::add);
        }
        return present;
    }

    public List<Optional<Ingredient>> getOptionalIngredients() {
        return ingredients;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.createFromOptionals(ingredients);
    }

    @Override
    public String getGroup() {
        return group;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public int getVis() {
        return visCost;
    }

    @Override
    public String getResearch() {
        return research;
    }

    @Override
    public AspectList getCrystals() {
        return crystals;
    }

    // NEOFORGE_PORT: kept so existing references (api ShapedArcaneRecipe#getSerializer,
    // ArcaneWandDynamicRecipe#getSerializer, ShapedArcaneVoidJarRecipe.Serializer base)
    // keep resolving; the registered serializer instance is SERIALIZER above.
    public static class Serializer {
        public static final RecipeSerializer<ShapedArcaneRecipe> INSTANCE = SERIALIZER;
        public static final Identifier ID = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "arcane_workbench_shaped");
    }
}
