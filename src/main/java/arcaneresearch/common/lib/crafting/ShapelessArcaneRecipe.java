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
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.RecipeMatcher; 
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.crafting.IArcaneRecipe;
import arcaneresearch.api.crafting.IArcaneWorkbench;
import arcaneresearch.init.ModRecipeTypes;

import java.util.ArrayList;
import java.util.List;














public class ShapelessArcaneRecipe implements IArcaneRecipe {

    public static final MapCodec<ShapelessArcaneRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
            Codec.STRING.optionalFieldOf("research", "").forGetter(r -> r.research),
            Codec.INT.optionalFieldOf("vis", 0).forGetter(r -> r.visCost),
            AspectList.MAP_CODEC.codec().optionalFieldOf("crystals", new AspectList()).forGetter(r -> r.crystals != null ? r.crystals : new AspectList()),
            Codec.lazyInitialized(() -> Ingredient.CODEC.listOf(1, 9)).fieldOf("ingredients").forGetter(r -> r.ingredients),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result)
    ).apply(inst, (group, research, visCost, crystals, ingredients, result) ->
            new ShapelessArcaneRecipe(null, group, ingredients, result, visCost, crystals, research)));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShapelessArcaneRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, r -> r.group,
            ByteBufCodecs.STRING_UTF8, r -> r.research,
            ByteBufCodecs.VAR_INT, r -> r.visCost,
            AspectList.STREAM_CODEC, r -> r.crystals != null ? r.crystals : new AspectList(),
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.ingredients,
            ItemStackTemplate.STREAM_CODEC, r -> r.result,
            (group, research, visCost, crystals, ingredients, result) ->
                    new ShapelessArcaneRecipe(null, group, ingredients, result, visCost, crystals, research)
    );

    public static final RecipeSerializer<ShapelessArcaneRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final Identifier id;
    private final String group;
    private final List<Ingredient> ingredients;
    private final ItemStackTemplate result;
    private final int visCost;
    private final AspectList crystals;
    private final String research;

    public ShapelessArcaneRecipe(Identifier id, String group,
                                 List<Ingredient> ingredients, ItemStackTemplate result,
                                 int visCost, AspectList crystals, String research) {
        this.id = id;
        this.group = group;
        this.ingredients = ingredients;
        this.result = result;
        this.visCost = visCost;
        this.crystals = crystals;
        this.research = research;
    }

    @Override
    public boolean matches(IArcaneWorkbench container, Level level) {
        List<ItemStack> inputs = new ArrayList<>();

        for (int i = 0; i < 9; i++) {
            ItemStack item = container.getItem(i);
            if (!item.isEmpty()) {
                inputs.add(item);
            }
        }

        return inputs.size() == ingredients.size() && RecipeMatcher.findMatches(inputs, ingredients) != null;
    }

    @Override
    public ItemStack assemble(IArcaneWorkbench container) {
        return result.create();
    }

    @Override
    public ItemStack getResultItem() {
        return result.create();
    }

    public ItemStackTemplate getResultTemplate() {
        return result;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<? extends net.minecraft.world.item.crafting.Recipe<IArcaneWorkbench>> getSerializer() {
        
        
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<IArcaneRecipe> getType() { 
        return ModRecipeTypes.ARCANE_WORKBENCH.get();
    }

    @Override
    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(ingredients);
    }

    @Override
    public String getGroup() {
        return group;
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

    
    
    public static class Serializer {
        public static final RecipeSerializer<ShapelessArcaneRecipe> INSTANCE = SERIALIZER;
        public static final Identifier ID = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "arcane_workbench_shapeless");
    }
}
