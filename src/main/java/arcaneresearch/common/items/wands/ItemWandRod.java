package arcaneresearch.common.items.wands;

import arcaneresearch.api.wands.WandRod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * 1.20.1 equivalent of Thaumcraft's ItemWandRod: a single item whose NBT tag
 * selects the rod or staff core variant (the original used metadata 0-7,
 * 50-57 and 100).
 */
public class ItemWandRod extends Item {

    public static final String TAG = "rod";

    /** Order is shared with the item-model overrides. */
    public static final String[] ROD_TAGS = {
            "greatwood", "obsidian", "silverwood", "ice", "quartz", "reed", "blaze", "bone",
            "greatwood_staff", "obsidian_staff", "silverwood_staff", "ice_staff",
            "quartz_staff", "reed_staff", "blaze_staff", "bone_staff", "primal_staff"
    };

    public ItemWandRod() {
        super(new Item.Properties().stacksTo(64));
    }

    public static String getRodTag(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG)) {
            String rod = tag.getString(TAG);
            if (!rod.isEmpty()) {
                return rod;
            }
        }
        return null;
    }

    public static WandRod getRod(ItemStack stack) {
        String tag = getRodTag(stack);
        return tag != null ? WandRod.rods.get(tag) : null;
    }

    public static float getRodIndex(ItemStack stack) {
        String tag = getRodTag(stack);
        if (tag != null) {
            for (int i = 0; i < ROD_TAGS.length; i++) {
                if (tag.equals(ROD_TAGS[i])) {
                    return (float) (i + 1);
                }
            }
        }
        return 0.0F;
    }

    public static ItemStack createStack(String rodTag) {
        if (!WandItems.WAND_ROD.isPresent() || rodTag == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(WandItems.WAND_ROD.get());
        stack.getOrCreateTag().putString(TAG, rodTag);
        return stack;
    }

    @Override
    public @NotNull String getDescriptionId(@NotNull ItemStack stack) {
        String tag = getRodTag(stack);
        if (tag != null) {
            return "item.WandRod." + tag;
        }
        return super.getDescriptionId(stack);
    }
}
