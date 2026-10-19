package arcaneresearch.helper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
public final class StackNbtHelper {
    private StackNbtHelper() {
    }
    public static boolean hasCompound(ItemStack stack, String key) {
        try {
            return stack != null && !stack.isEmpty() && stack.hasTag()
                    && stack.getTag().contains(key, Tag.TAG_COMPOUND);
        } catch (Exception e) {
            return false;
        }
    }
    public static CompoundTag getCompound(ItemStack stack, String key) {
        try {
            if (stack == null || stack.isEmpty() || !stack.hasTag()) {
                return null;
            }
            CompoundTag root = stack.getTag();
            if (root != null && root.contains(key, Tag.TAG_COMPOUND)) {
                return root.getCompound(key);
            }
        } catch (Exception ignored) {
        }
        return null;
    }
    public static CompoundTag getOrCreateCompound(ItemStack stack, String key) {
        try {
            if (stack == null || stack.isEmpty()) {
                return null;
            }
            CompoundTag root = stack.getOrCreateTag();
            if (root.contains(key, Tag.TAG_COMPOUND)) {
                return root.getCompound(key);
            }
            CompoundTag created = new CompoundTag();
            root.put(key, created);
            return created;
        } catch (Exception e) {
            return null;
        }
    }
    public static void putCompound(ItemStack stack, String key, CompoundTag tag) {
        try {
            if (stack == null || stack.isEmpty() || tag == null) {
                return;
            }
            stack.getOrCreateTag().put(key, tag);
        } catch (Exception ignored) {
        }
    }
    public static void removeStackKey(ItemStack stack, String key) {
        try {
            if (stack == null || stack.isEmpty() || !stack.hasTag()) {
                return;
            }
            stack.getTag().remove(key);
        } catch (Exception ignored) {
        }
    }
    public static void copyStackTag(ItemStack from, ItemStack to) {
        try {
            if (from == null || to == null || !from.hasTag()) {
                return;
            }
            to.setTag(from.getTag().copy());
        } catch (Exception ignored) {
        }
    }
}
