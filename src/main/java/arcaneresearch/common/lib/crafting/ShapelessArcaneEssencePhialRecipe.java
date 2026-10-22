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
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.crafting.IArcaneRecipe;
import arcaneresearch.api.crafting.IArcaneWorkbench;
import arcaneresearch.common.items.consumables.ItemPhial;
import arcaneresearch.common.items.resources.ItemWispEssence;

import java.util.List;

 
















 
public class ShapelessArcaneEssencePhialRecipe extends ShapelessArcaneRecipe {

    public static final MapCodec<ShapelessArcaneEssencePhialRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(ShapelessArcaneRecipe::getGroup),
            Codec.STRING.optionalFieldOf("research", "").forGetter(ShapelessArcaneRecipe::getResearch),
            Codec.INT.optionalFieldOf("vis", 0).forGetter(ShapelessArcaneRecipe::getVis),
            AspectList.MAP_CODEC.codec().optionalFieldOf("crystals", new AspectList())
                    .forGetter(r -> r.getCrystals() != null ? r.getCrystals() : new AspectList()),
            Codec.lazyInitialized(() -> Ingredient.CODEC.listOf(1, 9)).fieldOf("ingredients")
                    .forGetter(ShapelessArcaneRecipe::getIngredients),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(ShapelessArcaneRecipe::getResultTemplate)
    ).apply(inst, (group, research, visCost, crystals, ingredients, result) ->
            new ShapelessArcaneEssencePhialRecipe(null, group, ingredients, result, visCost, crystals, research)));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShapelessArcaneEssencePhialRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ShapelessArcaneRecipe::getGroup,
            ByteBufCodecs.STRING_UTF8, ShapelessArcaneRecipe::getResearch,
            ByteBufCodecs.VAR_INT, ShapelessArcaneRecipe::getVis,
            AspectList.STREAM_CODEC, r -> r.getCrystals() != null ? r.getCrystals() : new AspectList(),
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), ShapelessArcaneRecipe::getIngredients,
            ItemStackTemplate.STREAM_CODEC, ShapelessArcaneRecipe::getResultTemplate,
            (group, research, visCost, crystals, ingredients, result) ->
                    new ShapelessArcaneEssencePhialRecipe(null, group, ingredients, result, visCost, crystals, research)
    );

    public static final RecipeSerializer<ShapelessArcaneEssencePhialRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public ShapelessArcaneEssencePhialRecipe(Identifier id, String group,
                                             List<Ingredient> ingredients,
                                             ItemStackTemplate result, int visCost,
                                             AspectList crystals, String research) {
        super(id, group, ingredients, result, visCost, crystals, research);
    }

    @Override
    public boolean matches(IArcaneWorkbench container, Level level) {
        if (!super.matches(container, level)) {
            return false;
        }
        Aspect aspect = null;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemWispEssence)) {
                continue;
            }
            Aspect contained = ItemWispEssence.getAspect(stack);
            if (contained == null) {
                return false;
            }
            if (aspect == null) {
                aspect = contained;
            } else if (!aspect.getTag().equals(contained.getTag())) {
                return false;
            }
        }
        return aspect != null;
    }

    @Override
    public ItemStack assemble(IArcaneWorkbench container) {
        Aspect aspect = null;
        int total = 0;
        boolean mixed = false;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemWispEssence wisp)) {
                continue;
            }
            AspectList contained = wisp.getAspects(stack);
            if (contained == null || contained.size() <= 0) {
                continue;
            }
            Aspect a = contained.getAspects()[0];
            if (aspect == null) {
                aspect = a;
            } else if (!aspect.getTag().equals(a.getTag())) {
                mixed = true;
            }
            total += contained.getAmount(a);
        }
        if (aspect == null || mixed || total <= 0) {
            return super.assemble(container);
        }
        return ItemPhial.makePhial(aspect, Math.min(ItemPhial.PHIAL_CAPACITY, total));
    }

    @Override
    public RecipeSerializer<ShapelessArcaneEssencePhialRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<IArcaneRecipe> getType() {
        return super.getType();
    }

    @Override
    public PlacementInfo placementInfo() {
        return super.placementInfo();
    }

    @Override
    public String getGroup() {
        return super.getGroup();
    }

    @Override
    public int getVis() {
        return super.getVis();
    }

    @Override
    public String getResearch() {
        return super.getResearch();
    }

    @Override
    public AspectList getCrystals() {
        return super.getCrystals();
    }

    
    public static class Serializer {
        public static final RecipeSerializer<ShapelessArcaneEssencePhialRecipe> INSTANCE = SERIALIZER;
        public static final Identifier ID = Identifier.fromNamespaceAndPath("arcaneresearch", "arcane_workbench_shapeless_essence_phial");
    }
}
