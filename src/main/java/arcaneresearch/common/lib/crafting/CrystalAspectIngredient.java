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
import java.util.stream.Stream;
public class CrystalAspectIngredient extends AbstractIngredient {
    public static final ResourceLocation TYPE_ID =
            new ResourceLocation(Arcaneresearch.MODID, "crystal");
    private final Aspect aspect;
    public CrystalAspectIngredient(Aspect aspect) {
        super(Stream.of(new Ingredient.ItemValue(displayStack(aspect))));
        if (aspect == null) {
            throw new IllegalArgumentException("CrystalAspectIngredient aspect must not be null");
        }
        this.aspect = aspect;
    }
    private static ItemStack displayStack(Aspect aspect) {
        ItemStack stack = ArcaneresearchApiHelper.makeCrystal(aspect);
        return stack.isEmpty() ? new ItemStack(net.minecraft.world.item.Items.AIR) : stack;
    }
    public static Ingredient fromJson(JsonElement json) {
        return CraftingHelper.getIngredient(json, false);
    }
    public Aspect getAspect() {
        return aspect;
    }
    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (!(stack.getItem() instanceof ItemCrystalEssence crystal)) {
            return false;
        }
        AspectList contained = crystal.getAspects(stack);
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
        json.addProperty("aspect", aspect.getTag());
        return json;
    }
    public static class Serializer implements IIngredientSerializer<CrystalAspectIngredient> {
        public static final Serializer INSTANCE = new Serializer();
        @Override
        public CrystalAspectIngredient parse(FriendlyByteBuf buffer) {
            Aspect aspect = Aspect.getAspect(buffer.readUtf());
            if (aspect == null) {
                throw new IllegalArgumentException("Unknown aspect in arcaneresearch:crystal ingredient");
            }
            return new CrystalAspectIngredient(aspect);
        }
        @Override
        public CrystalAspectIngredient parse(JsonObject json) {
            String tag = GsonHelper.getAsString(json, "aspect");
            Aspect aspect = Aspect.getAspect(tag);
            if (aspect == null) {
                throw new JsonSyntaxException("Unknown aspect '" + tag + "' in arcaneresearch:crystal ingredient");
            }
            return new CrystalAspectIngredient(aspect);
        }
        @Override
        public void write(FriendlyByteBuf buffer, CrystalAspectIngredient ingredient) {
            buffer.writeUtf(ingredient.aspect.getTag());
        }
    }
}
