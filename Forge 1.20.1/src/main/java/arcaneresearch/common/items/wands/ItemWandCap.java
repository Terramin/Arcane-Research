package arcaneresearch.common.items.wands;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.wands.WandCap;
public class ItemWandCap extends Item {
    public static final String TAG = "cap";
    public ItemWandCap() {
        super(new Properties());
    }
    public static String getCapTag(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(TAG)) {
            String tag = stack.getTag().getString(TAG);
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
        ItemStack stack;
        try {
            stack = new ItemStack(WandItems.WAND_CAP.get());
        } catch (Exception e) {
            stack = new ItemStack(new ItemWandCap());
        }
        if (cap != null) {
            stack.getOrCreateTag().putString(TAG, cap.getTag());
        }
        return stack;
    }
    @Override
    public String getDescriptionId(ItemStack stack) {
        String tag = getCapTag(stack);
        if (tag != null) {
            return "item.arcaneresearch.wand_cap." + tag;
        }
        return super.getDescriptionId(stack);
    }
}
