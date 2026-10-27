package arcaneresearch.common.items.wands;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.wands.WandCap;
import org.jspecify.annotations.NonNull;

public class ItemWandCap extends Item {
    public static final String TAG = "cap";

    public ItemWandCap(Properties props) {
        super(props);
    }

    public static String getCapTag(ItemStack stack) {

        CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
        if (root != null && root.contains(TAG)) {
            String tag = root.getStringOr(TAG, "");
            if (!tag.isEmpty()) {
                return tag;
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
        if (!WandItems.WAND_CAP.isBound()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(WandItems.WAND_CAP.get());
        if (cap != null) {
            String capTag = cap.getTag();
            if (capTag != null && !capTag.isEmpty()) {
                CompoundTag root = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
                root.putString(TAG, capTag);
                ArcaneresearchInvHelper.setCustomTag(stack, root);
            }
        }
        return stack;
    }

    @Override
    public @NonNull Component getName(@NonNull ItemStack stack) {
        String tag = getCapTag(stack);
        if (tag != null) {
            return Component.translatable("item.arcaneresearch.wand_cap." + tag);
        }
        return super.getName(stack);
    }
}
