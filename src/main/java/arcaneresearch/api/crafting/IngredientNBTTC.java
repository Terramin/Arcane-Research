package arcaneresearch.api.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.ArcaneresearchInvHelper;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import javax.annotation.Nullable;
import java.util.stream.Stream;









public class IngredientNBTTC implements ICustomIngredient {

    public static final MapCodec<IngredientNBTTC> CODEC =
            ItemStack.CODEC.fieldOf("stack").xmap(IngredientNBTTC::new, ing -> ing.stack);

    public static final StreamCodec<RegistryFriendlyByteBuf, IngredientNBTTC> STREAM_CODEC =
            ItemStack.STREAM_CODEC.map(IngredientNBTTC::new, ing -> ing.stack);

    public static final IngredientType<IngredientNBTTC> TYPE =
            new IngredientType<>(CODEC, STREAM_CODEC);

    private final ItemStack stack;

    public IngredientNBTTC(ItemStack stack) {
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
    public Stream<net.minecraft.core.Holder<net.minecraft.world.item.Item>> items() {
        return Stream.of(stack.typeHolder());
    }

    
    
    
    @Override
    public net.minecraft.world.item.crafting.display.SlotDisplay display() {
        if (stack.isEmpty()) {
            return net.minecraft.world.item.crafting.display.SlotDisplay.Empty.INSTANCE;
        }
        return new net.minecraft.world.item.crafting.display.SlotDisplay.ItemStackSlotDisplay(
                net.minecraft.world.item.ItemStackTemplate.fromNonEmptyStack(stack));
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return TYPE;
    }
}
