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
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.init.ModRecipeTypes;

import java.util.List;
import java.util.Optional;

// NEOFORGE_PORT: 1:1 logic port from Forge common/lib/crafting/InfusionRecipeType.java.
// REQUIRED changes (vanilla 26.x recipe rework, verified in 26.3.0.23-beta jar):
//   - Recipe<Container> -> Recipe<RecipeInput> (Container is not a RecipeInput).
//     matches(Container, Level) -> matches(RecipeInput, Level) (still returns false;
//     real matching is matchesInfusion, called by the altar tile).
//   - assemble(Container, RegistryAccess) -> assemble(RecipeInput); canCraftInDimensions,
//     getResultItem(RegistryAccess), getIngredients, getId removed from the interface
//     (kept only where our code needs them: getResultItem()/getComponents()/getId()).
//   - RecipeSerializer interface (fromJson/fromNetwork/toNetwork) -> RecipeSerializer RECORD
//     (MapCodec + StreamCodec); see SERIALIZER below.
//   - MapCodec accepts the legacy JSON key aliases (central|center|input,
//     components|ingredients) so existing data files parse unchanged.
//   - Ingredient.EMPTY removed -> central input is @Nullable (absent = no requirement);
//     matchesInfusion null/isEmpty-safe.
//   - placementInfo() -> NOT_PLACEABLE (altar recipes never use the vanilla grid);
//     recipeBookCategory() -> CRAFTING_MISC (never meaningfully displayed);
//     showNotification() -> false (research toasts are handled by our own system).
// Match/aspect/instability logic VERBATIM.
public class InfusionRecipeType implements Recipe<RecipeInput>, IArcaneresearchRecipe {

    public static final MapCodec<InfusionRecipeType> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
            Codec.STRING.optionalFieldOf("research", "").forGetter(r -> r.research),
            Codec.INT.optionalFieldOf("instability", 0).forGetter(r -> r.instability),
            Ingredient.CODEC.optionalFieldOf("central").forGetter(r -> Optional.ofNullable(r.centralItem)),
            Ingredient.CODEC.optionalFieldOf("center").forGetter(r -> Optional.empty()),
            Ingredient.CODEC.optionalFieldOf("input").forGetter(r -> Optional.empty()),
            Ingredient.CODEC.listOf().optionalFieldOf("components").forGetter(r -> Optional.of(r.components)),
            Ingredient.CODEC.listOf().optionalFieldOf("ingredients").forGetter(r -> Optional.empty()),
            AspectList.MAP_CODEC.codec().optionalFieldOf("aspects", new AspectList()).forGetter(r -> r.aspects),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result)
    ).apply(inst, (group, research, instability, central, center, input, components, ingredients,
                   aspects, result) -> {
        Ingredient centralItem = central.or(() -> center).or(() -> input).orElse(null);
        // NEOFORGE_PORT (26.3): Forge read "components" if present, otherwise "ingredients"
        // (never both) - same precedence here instead of concatenating the two keys.
        NonNullList<Ingredient> merged = NonNullList.create();
        merged.addAll(components.isPresent() ? components.get() : ingredients.orElse(List.of()));
        return new InfusionRecipeType(group, centralItem, merged, aspects, result, research, instability);
    }));

    public static final StreamCodec<RegistryFriendlyByteBuf, InfusionRecipeType> STREAM_CODEC =
            StreamCodec.ofMember(InfusionRecipeType::write, InfusionRecipeType::read);

    public static final RecipeSerializer<InfusionRecipeType> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final String group;
    private final Ingredient centralItem;
    private final NonNullList<Ingredient> components;
    private final AspectList aspects;
    // NEOFORGE_PORT 26.3: result is a template (vanilla SingleItemRecipe/ShapedRecipe
    // pattern). Null only for InfusionEnchantmentRecipe (no static output).
    private final ItemStackTemplate result;
    private final String research;
    private final int instability;

    // NEOFORGE_PORT: id removed from the ctor chain (vanilla recipes are Holder-keyed now).
    // Kept out of the serialized form; runtime-only uses (research display) pass ids separately.
    public InfusionRecipeType(String group, Ingredient centralItem,
                              NonNullList<Ingredient> components, AspectList aspects,
                              ItemStackTemplate result, String research, int instability) {
        this.group = group;
        this.centralItem = centralItem;
        this.components = components;
        this.aspects = aspects;
        this.result = result;
        this.research = research;
        this.instability = instability;
    }

    // NEOFORGE_PORT: Forge-era legacy helper (id is holder-keyed in 26.x).
    public java.util.List<Ingredient> getIngredients() {
        java.util.List<Ingredient> all = new java.util.ArrayList<>(1 + components.size());
        all.add(centralItem);
        all.addAll(components);
        return all;
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {

        return false;
    }

    public boolean matchesInfusion(List<ItemStack> pedestalItems, ItemStack centralItemStack,
                                   Level level, Player player) {

        if (research != null && !research.isEmpty()) {
            if (!ArcaneresearchCapabilities.isResearchKnown(player, research)) {
                return false;
            }
        }

        // NEOFORGE_PORT (26.3): only an ABSENT central input (Forge Ingredient.EMPTY, here null)
        // skips the check. A present ingredient that resolves to nothing (empty tag) must
        // still be tested (and fail) - Forge never treated it as "no requirement".
        if (centralItem != null && !centralItem.test(centralItemStack)) {
            return false;
        }

        return net.neoforged.neoforge.common.util.RecipeMatcher.findMatches(pedestalItems, components) != null;
    }

    @Override
    public ItemStack assemble(RecipeInput input) {
        return result == null ? ItemStack.EMPTY : result.create();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    public ItemStack getResultItem() {
        return result == null ? ItemStack.EMPTY : result.create();
    }

    public ItemStackTemplate getResultTemplate() {
        return result;
    }

    // NEOFORGE_PORT: wildcard bound (parameterized types are invariant, so the
    // concrete RecipeSerializer<InfusionRecipeType> would block covariant
    // overrides in subclasses like InfusionEnchantmentRecipe).
    @Override
    public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<InfusionRecipeType> getType() {
        return ModRecipeTypes.INFUSION.get();
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

    public Ingredient getCentralItem() {
        return centralItem;
    }

    public NonNullList<Ingredient> getComponents() {
        return components;
    }

    public AspectList getAspects() {
        return aspects;
    }

    public int getInstability() {
        return instability;
    }

    public ItemStack getRecipeOutput(Player player, ItemStack input, List<ItemStack> pedestalItems) {
        return result == null ? ItemStack.EMPTY : result.create();
    }

    public AspectList getAspects(Player player, ItemStack input, List<ItemStack> pedestalItems) {
        return aspects;
    }

    public int getInstability(Player player, ItemStack input, List<ItemStack> pedestalItems) {
        return instability;
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
        buf.writeVarInt(instability);

        Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.encode(buf, Optional.ofNullable(centralItem));

        buf.writeVarInt(components.size());
        for (Ingredient component : components) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, component);
        }

        AspectList.STREAM_CODEC.encode(buf, aspects);

        ItemStackTemplate.STREAM_CODEC.encode(buf, result);
    }

    private static InfusionRecipeType read(RegistryFriendlyByteBuf buf) {
        String group = buf.readUtf();
        String research = buf.readUtf();
        int instability = buf.readVarInt();

        Ingredient centralItem = Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.decode(buf).orElse(null);

        int componentCount = buf.readVarInt();
        NonNullList<Ingredient> components = NonNullList.create();
        for (int i = 0; i < componentCount; i++) {
            components.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
        }

        AspectList aspects = AspectList.STREAM_CODEC.decode(buf);

        ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buf);

        return new InfusionRecipeType(group, centralItem, components, aspects, result, research, instability);
    }
}
