package arcaneresearch.common.items.wands;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.wands.WandCap;

public class ItemWandCap extends Item {
    public static final String TAG = "cap";

    public ItemWandCap(Properties props) {
        super(props);
    }

    public static String getCapTag(ItemStack stack) {
        // NEOFORGE_PORT: stack root tag -> CUSTOM_DATA (same "cap" key).
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
        if (tag != null) {
            return WandCap.caps.get(tag);
        }
        return null;
    }

    public static float getCapIndex(ItemStack stack) {
        String tag = getCapTag(stack);
        if (tag != null) {
            int i = 0;
            for (String key : WandCap.caps.keySet()) {
                if (key.equals(tag)) {
                    return (float) i;
                }
                i++;
            }
        }
        return 0.0F;
    }

    public static ItemStack createStack(WandCap cap) {
        ItemStack stack = new ItemStack(WandItems.WAND_CAP.get());
        if (cap != null) {
            CompoundTag root = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
            root.putString(TAG, cap.getTag());
            ArcaneresearchInvHelper.setCustomTag(stack, root);
        }
        return stack;
    }

    // NEOFORGE_PORT: getDescriptionId(stack) REMOVED in 26.x (getDescriptionId() is final) ->
    // override getName(stack) with the same composed key (same displayed name).
    @Override
    public Component getName(ItemStack stack) {
        String tag = getCapTag(stack);
        if (tag != null) {
            return Component.translatable("item.arcaneresearch.wand_cap." + tag);
        }
        return super.getName(stack);
    }
}
