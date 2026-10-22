package arcaneresearch.helper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import arcaneresearch.api.ArcaneresearchInvHelper;

 







 
public final class StackNbtHelper {

    private StackNbtHelper() {
    }

      
    public static boolean hasCompound(ItemStack stack, String key) {
        try {
            CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
            return root != null && root.contains(key);
        } catch (Exception e) {
            return false;
        }
    }

     


 
    public static CompoundTag getCompound(ItemStack stack, String key) {
        try {
            CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
            if (root == null || !root.contains(key)) {
                return null;
            }
            return root.getCompound(key).orElse(null);
        } catch (Exception ignored) {
        }
        return null;
    }

     


 
    public static CompoundTag getOrCreateCompound(ItemStack stack, String key) {
        try {
            if (stack == null || stack.isEmpty()) {
                return null;
            }
            CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
            if (root == null) {
                root = new CompoundTag();
            }
            CompoundTag nested = root.getCompound(key).orElse(null);
            if (nested == null) {
                nested = new CompoundTag();
                root.put(key, nested);
                ArcaneresearchInvHelper.setCustomTag(stack, root);
                nested = new CompoundTag();
            }
            return nested;
        } catch (Exception e) {
            return null;
        }
    }

      
    public static void putCompound(ItemStack stack, String key, CompoundTag tag) {
        try {
            if (stack == null || stack.isEmpty() || tag == null) {
                return;
            }
            CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
            if (root == null) {
                root = new CompoundTag();
            }
            root.put(key, tag);
            ArcaneresearchInvHelper.setCustomTag(stack, root);
        } catch (Exception ignored) {
        }
    }

      
    public static void removeStackKey(ItemStack stack, String key) {
        try {
            CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
            if (root == null) {
                return;
            }
            root.remove(key);
            ArcaneresearchInvHelper.setCustomTag(stack, root);
        } catch (Exception ignored) {
        }
    }

      
    public static void copyStackTag(ItemStack from, ItemStack to) {
        try {
            if (from == null || to == null) {
                return;
            }
            CompoundTag root = ArcaneresearchInvHelper.getCustomTag(from);
            if (root == null) {
                return;
            }
            ArcaneresearchInvHelper.setCustomTag(to, root);
        } catch (Exception ignored) {
        }
    }
}
