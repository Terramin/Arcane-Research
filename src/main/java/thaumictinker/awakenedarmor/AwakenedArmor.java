package thaumictinker.awakenedarmor;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

 



 
public final class AwakenedArmor {

      
    private static final String DISABLED_TAG = "thaumictinker_armor_disabled";

    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private AwakenedArmor() {
    }

      
    public static boolean isEnabled(Player player) {
        return !player.getPersistentData().getBoolean(DISABLED_TAG);
    }

    public static void setEnabled(Player player, boolean enabled) {
        if (enabled) {
            player.getPersistentData().remove(DISABLED_TAG);
        } else {
            player.getPersistentData().putBoolean(DISABLED_TAG, true);
        }
    }

    public static boolean isPieceDisabled(ItemStack stack) {
        return AwakenedArmorItem.isPieceDisabled(stack);
    }

    public static boolean isActive(Player player, EquipmentSlot slot, Item item) {
        ItemStack stack = player.getItemBySlot(slot);
        return stack.is(item) && !isPieceDisabled(stack) && isEnabled(player);
    }

    public static boolean wearsAny(Player player) {
        for (EquipmentSlot slot : SLOTS) {
            if (player.getItemBySlot(slot).getItem() instanceof AwakenedArmorItem) {
                return true;
            }
        }
        return false;
    }

    public static boolean carriesLight(Player player) {
        return isActive(player, EquipmentSlot.LEGS, AwakenedArmorFeature.LEGGINGS);
    }
}
