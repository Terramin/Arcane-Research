package arcaneresearch.api.crafting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import arcaneresearch.api.ArcaneresearchInvHelper;
import javax.annotation.Nullable;
import java.util.stream.Stream;
public class IngredientNBTTC extends Ingredient {
    private final ItemStack stack;
    public IngredientNBTTC(ItemStack stack) {
        super(Stream.of(new ItemValue(stack)));
        this.stack = stack;
    }
    public boolean apply(@Nullable ItemStack input) {
        return test(input);
    }
    @Override
    public boolean test(@Nullable ItemStack input) {
        if (input == null) {
            return false;
        }
        return stack.getItem() == input.getItem()
                && stack.getDamageValue() == input.getDamageValue()
                && ArcaneresearchInvHelper.areItemStackTagsEqualRelaxed(stack, input);
    }
    @Override
    public boolean isSimple() {
        return false;
    }
}
