package thaumictinker.enchanting;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

 




 
public final class TtEnchantments {

    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final EquipmentSlot[] MAIN_HAND = {EquipmentSlot.MAINHAND};
    private static final EquipmentSlot[] LEGS = {EquipmentSlot.LEGS};
    private static final EquipmentSlot[] FEET = {EquipmentSlot.FEET};

    public static final Enchantment ASCENT_BOOST = ench("ascent_boost", Enchantment.Rarity.COMMON, EnchantmentCategory.WEARABLE, ARMOR_SLOTS);
    public static final Enchantment SLOW_FALL = ench("slow_fall", Enchantment.Rarity.COMMON, EnchantmentCategory.WEARABLE, ARMOR_SLOTS);
    public static final Enchantment AUTO_SMELT = ench("auto_smelt", Enchantment.Rarity.RARE, EnchantmentCategory.DIGGER, MAIN_HAND);
    public static final Enchantment DESINTEGRATE = ench("desintegrate", Enchantment.Rarity.RARE, EnchantmentCategory.DIGGER, MAIN_HAND);
    public static final Enchantment QUICK_DRAW = ench("quick_draw", Enchantment.Rarity.COMMON, EnchantmentCategory.BOW, MAIN_HAND);
    public static final Enchantment VAMPIRISM = ench("vampirism", Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON, MAIN_HAND);
    public static final Enchantment DISPERSED_STRIKE = ench("dispersed_strike", Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON, MAIN_HAND);
    public static final Enchantment SHATTER = ench("shatter", Enchantment.Rarity.RARE, EnchantmentCategory.DIGGER, MAIN_HAND);
    public static final Enchantment FINAL_STRIKE = ench("final_strike", Enchantment.Rarity.VERY_RARE, EnchantmentCategory.WEAPON, MAIN_HAND);
    public static final Enchantment FOCUSED_STRIKE = ench("focused_strike", Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON, MAIN_HAND);
    public static final Enchantment POUNCE = ench("pounce", Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR_LEGS, LEGS);
    public static final Enchantment SHOCKWAVE = ench("shockwave", Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR_FEET, FEET);
    public static final Enchantment TUNNEL = ench("tunnel", Enchantment.Rarity.COMMON, EnchantmentCategory.DIGGER, MAIN_HAND);
    public static final Enchantment VALIANCE = ench("valiance", Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON, MAIN_HAND);

    private TtEnchantments() {
    }

    private static Enchantment ench(String path, Enchantment.Rarity rarity, EnchantmentCategory category, EquipmentSlot[] slots) {
        return thaumictinker.registry.TtRegistry.enchantment(path, new TtEnchantment(rarity, category, slots));
    }

      
    public static int level(net.minecraft.world.item.ItemStack stack, Enchantment enchantment) {
        return net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack);
    }

    public static int maxArmorLevel(net.minecraft.world.entity.LivingEntity entity, Enchantment enchantment) {
        int result = 0;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            result = Math.max(result, level(entity.getItemBySlot(slot), enchantment));
        }
        return result;
    }

      
    public static class TtEnchantment extends Enchantment {
        public TtEnchantment(Rarity rarity, EnchantmentCategory category, EquipmentSlot[] slots) {
            super(rarity, category, slots);
        }
    }

     



 
    public static boolean areCompatible(Enchantment a, Enchantment b) {
        if (a == b) {
            return false;
        }
        if (a instanceof net.minecraft.world.item.enchantment.DamageEnchantment
                && b instanceof net.minecraft.world.item.enchantment.DamageEnchantment) {
            return false;
        }
        if (a == net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH
                && b == net.minecraft.world.item.enchantment.Enchantments.BLOCK_FORTUNE
                || a == net.minecraft.world.item.enchantment.Enchantments.BLOCK_FORTUNE
                && b == net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH) {
            return false;
        }
        if (a == net.minecraft.world.item.enchantment.Enchantments.INFINITY_ARROWS
                && b == net.minecraft.world.item.enchantment.Enchantments.MENDING
                || a == net.minecraft.world.item.enchantment.Enchantments.MENDING
                && b == net.minecraft.world.item.enchantment.Enchantments.INFINITY_ARROWS) {
            return false;
        }
        return true;
    }
}
