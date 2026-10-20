package arcaneresearch.common.lib.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.crafting.IArcaneresearchRecipe;
import arcaneresearch.init.ModRecipeTypes;

import java.util.List;
import java.util.Optional;

// NEOFORGE_PORT: 1:1 logic port from Forge common/lib/crafting/CrucibleRecipeType.java.
// Same 26.x recipe-system adaptation as InfusionRecipeType (see its javadoc):
// Recipe<RecipeInput>, codec/streamCodec SERIALIZER, legacy "ingredient" key alias,
// placementInfo NOT_PLACEABLE, CRAFTING_MISC book category, no-book notification.
// matchesCrucible/catalystMatches/removeMatchingAspects logic VERBATIM.
public class CrucibleRecipeType implements Recipe<RecipeInput>, IArcaneresearchRecipe {

    public static final MapCodec<CrucibleRecipeType> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
            Codec.STRING.optionalFieldOf("research", "").forGetter(r -> r.research),
            Ingredient.CODEC.optionalFieldOf("catalyst").forGetter(r -> Optional.ofNullable(r.catalyst)),
            Ingredient.CODEC.optionalFieldOf("ingredient").forGetter(r -> Optional.empty()),
            AspectList.MAP_CODEC.codec().optionalFieldOf("aspects", new AspectList()).forGetter(r -> r.aspects),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result)
    ).apply(inst, (group, research, catalyst, ingredient, aspects, result) -> {
        Ingredient merged = catalyst.or(() -> ingredient).orElse(null);
        return new CrucibleRecipeType(group, merged, aspects, result, research);
    }));

    public static final StreamCodec<RegistryFriendlyByteBuf, CrucibleRecipeType> STREAM_CODEC =
            StreamCodec.ofMember(CrucibleRecipeType::write, CrucibleRecipeType::read);

    public static final RecipeSerializer<CrucibleRecipeType> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final String group;
    private final Ingredient catalyst;
    private final AspectList aspects;
    private final ItemStackTemplate result;
    private final String research;

    public CrucibleRecipeType(String group, Ingredient catalyst,
                              AspectList aspects, ItemStackTemplate result, String research) {
        this.group = group;
        this.catalyst = catalyst;
        this.aspects = aspects;
        this.result = result;
        this.research = research;
    }

    // NEOFORGE_PORT: Forge-era legacy helper (id is holder-keyed in 26.x, so no
    // getId(); ingredients kept for JEI/display callers).
    // Null-safe: absent catalyst (neither "catalyst" nor legacy "ingredient" key)
    // decodes to null (no Ingredient.EMPTY in 26.x); List.of(null) would NPE.
    public java.util.List<Ingredient> getIngredients() {
        return catalyst == null ? java.util.List.of() : java.util.List.of(catalyst);
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {

        return false;
    }

    public boolean matchesCrucible(AspectList crucibleAspects, ItemStack catalystStack) {
        if (catalyst == null || !catalyst.test(catalystStack)) return false;
        if (crucibleAspects == null) return false;

        for (Aspect aspect : aspects.getAspects()) {
            if (crucibleAspects.getAmount(aspect) < aspects.getAmount(aspect)) {
                return false;
            }
        }
        return true;
    }

    public boolean catalystMatches(ItemStack catalystStack) {
        return catalyst != null && catalyst.test(catalystStack);
    }

    public AspectList removeMatchingAspects(AspectList crucibleAspects) {
        AspectList result = crucibleAspects.copy();
        for (Aspect aspect : aspects.getAspects()) {
            result.remove(aspect, aspects.getAmount(aspect));
        }
        return result;
    }

    @Override
    public ItemStack assemble(RecipeInput input) {
        return result.create();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    public ItemStack getResultItem() {
        return result.create();
    }

    public ItemStackTemplate getResultTemplate() {
        return result;
    }

    // NEOFORGE_PORT: concrete generic bounds (Recipe<RecipeInput> requires
    // RecipeType/Serializer<? extends Recipe<RecipeInput>>).
    @Override
    public RecipeSerializer<CrucibleRecipeType> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<CrucibleRecipeType> getType() {
        return ModRecipeTypes.CRUCIBLE.get();
    }

    @Override
    public String getGroup() {
        return group;
    }

    // NEOFORGE_PORT: Recipe.group() abstract in 26.3.
    @Override
    public String group() {
        return group;
    }

    @Override
    public String getResearch() {
        return research;
    }

    public Ingredient getCatalyst() {
        return catalyst;
    }

    public AspectList getAspects() {
        return aspects;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(group);
        buf.writeUtf(research);

        Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.encode(buf, Optional.ofNullable(catalyst));

        AspectList.STREAM_CODEC.encode(buf, aspects);

        ItemStackTemplate.STREAM_CODEC.encode(buf, result);
    }

    private static CrucibleRecipeType read(RegistryFriendlyByteBuf buf) {
        String group = buf.readUtf();
        String research = buf.readUtf();

        Ingredient catalyst = Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.decode(buf).orElse(null);

        AspectList aspects = AspectList.STREAM_CODEC.decode(buf);

        ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buf);

        return new CrucibleRecipeType(group, catalyst, aspects, result, research);
    }
}
