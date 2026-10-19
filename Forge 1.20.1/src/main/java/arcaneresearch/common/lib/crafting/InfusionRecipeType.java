package arcaneresearch.common.lib.crafting;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.RecipeMatcher;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.crafting.IArcaneresearchRecipe;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.init.ModRecipeTypes;
import javax.annotation.Nullable;
import java.util.List;
public class InfusionRecipeType implements Recipe<Container>, IArcaneresearchRecipe {
    private final ResourceLocation id;
    private final String group;
    private final Ingredient centralItem;
    private final NonNullList<Ingredient> components;
    private final AspectList aspects;
    private final ItemStack result;
    private final String research;
    private final int instability;
    public InfusionRecipeType(ResourceLocation id, String group, Ingredient centralItem,
                              NonNullList<Ingredient> components, AspectList aspects,
                              ItemStack result, String research, int instability) {
        this.id = id;
        this.group = group;
        this.centralItem = centralItem;
        this.components = components;
        this.aspects = aspects;
        this.result = result;
        this.research = research;
        this.instability = instability;
    }
    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }
    public boolean matchesInfusion(List<ItemStack> pedestalItems, ItemStack centralItemStack,
                                   Level level, Player player) {
        if (research != null && !research.isEmpty()) {
            if (!ArcaneresearchCapabilities.isResearchKnown(player, research)) {
                return false;
            }
        }
        if (centralItem != Ingredient.EMPTY && !centralItem.test(centralItemStack)) {
            return false;
        }
        return RecipeMatcher.findMatches(pedestalItems, components) != null;
    }
    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return result.copy();
    }
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }
    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result;
    }
    public ItemStack getResultItem() {
        return result.copy();
    }
    @Override
    public ResourceLocation getId() {
        return id;
    }
    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }
    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.INFUSION.get();
    }
    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> allIngredients = NonNullList.create();
        allIngredients.add(centralItem);
        allIngredients.addAll(components);
        return allIngredients;
    }
    @Override
    public String getGroup() {
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
        return result.copy();
    }
    public AspectList getAspects(Player player, ItemStack input, List<ItemStack> pedestalItems) {
        return aspects;
    }
    public int getInstability(Player player, ItemStack input, List<ItemStack> pedestalItems) {
        return instability;
    }
    public static class Serializer implements RecipeSerializer<InfusionRecipeType> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(Arcaneresearch.MODID, "infusion");
        @Override
        public InfusionRecipeType fromJson(ResourceLocation recipeId, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            String research = GsonHelper.getAsString(json, "research", "");
            int instability = GsonHelper.getAsInt(json, "instability", 0);
            Ingredient centralItem = Ingredient.EMPTY;
            if (json.has("central")) {
                centralItem = Ingredient.fromJson(json.get("central"), false);
            } else if (json.has("center")) {
                centralItem = Ingredient.fromJson(json.get("center"), false);
            } else if (json.has("input")) {
                centralItem = Ingredient.fromJson(json.get("input"), false);
            }
            NonNullList<Ingredient> components = NonNullList.create();
            JsonArray componentsArray = null;
            if (json.has("components")) {
                componentsArray = GsonHelper.getAsJsonArray(json, "components");
            } else if (json.has("ingredients")) {
                componentsArray = GsonHelper.getAsJsonArray(json, "ingredients");
            }
            if (componentsArray != null) {
                for (int i = 0; i < componentsArray.size(); i++) {
                    Ingredient ingredient = Ingredient.fromJson(componentsArray.get(i), false);
                    if (!ingredient.isEmpty()) {
                        components.add(ingredient);
                    }
                }
            }
            AspectList aspects = new AspectList();
            if (json.has("aspects")) {
                JsonObject aspectsJson = GsonHelper.getAsJsonObject(json, "aspects");
                for (String aspectName : aspectsJson.keySet()) {
                    Aspect aspect = Aspect.getAspect(aspectName);
                    if (aspect != null) {
                        aspects.add(aspect, aspectsJson.get(aspectName).getAsInt());
                    }
                }
            }
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            return new InfusionRecipeType(recipeId, group, centralItem, components, aspects, result, research, instability);
        }
        @Override
        public @Nullable InfusionRecipeType fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            String research = buffer.readUtf();
            int instability = buffer.readVarInt();
            Ingredient centralItem = Ingredient.fromNetwork(buffer);
            int componentCount = buffer.readVarInt();
            NonNullList<Ingredient> components = NonNullList.withSize(componentCount, Ingredient.EMPTY);
            for (int i = 0; i < componentCount; i++) {
                components.set(i, Ingredient.fromNetwork(buffer));
            }
            AspectList aspects = new AspectList();
            int aspectCount = buffer.readVarInt();
            for (int i = 0; i < aspectCount; i++) {
                String aspectName = buffer.readUtf();
                int amount = buffer.readVarInt();
                Aspect aspect = Aspect.getAspect(aspectName);
                if (aspect != null) {
                    aspects.add(aspect, amount);
                }
            }
            ItemStack result = buffer.readItem();
            return new InfusionRecipeType(recipeId, group, centralItem, components, aspects, result, research, instability);
        }
        @Override
        public void toNetwork(FriendlyByteBuf buffer, InfusionRecipeType recipe) {
            buffer.writeUtf(recipe.group);
            buffer.writeUtf(recipe.research);
            buffer.writeVarInt(recipe.instability);
            recipe.centralItem.toNetwork(buffer);
            buffer.writeVarInt(recipe.components.size());
            for (Ingredient component : recipe.components) {
                component.toNetwork(buffer);
            }
            Aspect[] aspectArray = recipe.aspects.getAspects();
            buffer.writeVarInt(aspectArray.length);
            for (Aspect aspect : aspectArray) {
                buffer.writeUtf(aspect.getTag());
                buffer.writeVarInt(recipe.aspects.getAmount(aspect));
            }
            buffer.writeItem(recipe.result);
        }
    }
}
