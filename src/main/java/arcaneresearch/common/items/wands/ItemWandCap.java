package arcaneresearch.common.items.wands;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.wands.WandCap;
import org.jetbrains.annotations.NotNull;

public class ItemWandCap extends Item {
    public static final String TAG = "cap";

    public ItemWandCap() {
        super(new Properties());
    }

    public static String getCapTag(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains(TAG)) {
            String cap = nbt.getString(TAG);
            if (!cap.isEmpty()) {
                return cap;
            }
        }
        return null;
    }

    public static WandCap getCap(ItemStack stack) {
        String tag = getCapTag(stack);
        return tag != null ? WandCap.caps.get(tag) : null;
    }

    public static float getCapIndex(ItemStack stack) {
        String tag = getCapTag(stack);
        if (tag != null) {
            int i = 0;
            for (String key : WandCap.caps.keySet()) {
                if (tag.equals(key)) {
                    return (float) i;
                }
                i++;
            }
        }
        return 0.0F;
    }

    public static ItemStack createStack(WandCap cap) {
        if (!WandItems.WAND_CAP.isPresent()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(WandItems.WAND_CAP.get());
        if (cap != null) {
            String tag = cap.getTag();
            if (tag != null && !tag.isEmpty()) {
                stack.getOrCreateTag().putString(TAG, tag);
            }
        }
        return stack;
    }

    @Override
    public @NotNull String getDescriptionId(@NotNull ItemStack stack) {
        String tag = getCapTag(stack);
        if (tag != null) {
            return "item.arcaneresearch.wand_cap." + tag;
        }
        return super.getDescriptionId(stack);
    }
}
