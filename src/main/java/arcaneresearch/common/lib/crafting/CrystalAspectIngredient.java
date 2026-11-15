package arcaneresearch.common.lib.crafting;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.items.resources.ItemCrystalEssence;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CrystalAspectIngredient extends AbstractIngredient {
    public static final ResourceLocation TYPE_ID =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "crystal");
    @Nullable
    private final Aspect aspect;
    public CrystalAspectIngredient(@Nullable Aspect aspect) {
        super(aspect == null ? anyAspectValues() : Stream.of(new Ingredient.ItemValue(displayStack(aspect))));
        this.aspect = aspect;
    }
    private static ItemStack displayStack(Aspect aspect) {
        ItemStack stack = ArcaneresearchApiHelper.makeCrystal(aspect);
        return stack.isEmpty() ? new ItemStack(net.minecraft.world.item.Items.AIR) : stack;
    }
    private static Stream<Ingredient.Value> anyAspectValues() {
        List<Ingredient.Value> values = new ArrayList<>();
        for (Aspect aspect : Aspect.aspects.values()) {
            ItemStack stack = displayStack(aspect);
            if (!stack.isEmpty()) {
                values.add(new Ingredient.ItemValue(stack));
            }
        }
        return values.stream();
    }
    public static Ingredient fromJson(JsonElement json) {
        return CraftingHelper.getIngredient(json, false);
    }
    @Nullable
    public Aspect getAspect() {
        return aspect;
    }
    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (!(stack.getItem() instanceof ItemCrystalEssence)) {
            return false;
        }
        if (aspect == null) {
            return true;
        }
        AspectList contained = ((ItemCrystalEssence) stack.getItem()).getAspects(stack);
        if (contained == null || contained.size() == 0) {
            return false;
        }
        return aspect.getTag().equals(contained.getAspects()[0].getTag());
    }
    @Override
    public boolean isSimple() {
        return false;
    }
    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return Serializer.INSTANCE;
    }
    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", TYPE_ID.toString());
        if (aspect != null) {
            json.addProperty("aspect", aspect.getTag());
        }
        return json;
    }
    public static class Serializer implements IIngredientSerializer<CrystalAspectIngredient> {
        public static final Serializer INSTANCE = new Serializer();
        @Override
        public CrystalAspectIngredient parse(FriendlyByteBuf buffer) {
            String tag = buffer.readUtf();
            Aspect aspect = tag.isEmpty() ? null : Aspect.getAspect(tag);
            if (tag.isEmpty()) {
                return new CrystalAspectIngredient(null);
            }
            if (aspect == null) {
                throw new IllegalArgumentException("Unknown aspect in arcaneresearch:crystal ingredient");
            }
            return new CrystalAspectIngredient(aspect);
        }
        @Override
        public CrystalAspectIngredient parse(JsonObject json) {
            if (!json.has("aspect") || json.get("aspect").isJsonNull()) {
                return new CrystalAspectIngredient(null);
            }
            String tag = GsonHelper.getAsString(json, "aspect");
            Aspect aspect = Aspect.getAspect(tag);
            if (aspect == null) {
                throw new JsonSyntaxException("Unknown aspect '" + tag + "' in arcaneresearch:crystal ingredient");
            }
            return new CrystalAspectIngredient(aspect);
        }
        @Override
        public void write(FriendlyByteBuf buffer, CrystalAspectIngredient ingredient) {
            buffer.writeUtf(ingredient.aspect == null ? "" : ingredient.aspect.getTag());
        }
    }
}
