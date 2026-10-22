package thaumictinker.enchanting;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;

 





 
public final class EnchantmentManager {

    public static final List<Aspect> PRIMAL_ASPECTS = List.of(
            Aspect.AIR, Aspect.EARTH, Aspect.FIRE, Aspect.WATER, Aspect.ORDER, Aspect.ENTROPY);

    public record Data(Enchantment enchantment, String texture, boolean vanilla, AspectList level1Aspects, String research) {
        public AspectList aspects(int level) {
            double multiplier = level * (1.0 + level * 0.2);
            AspectList result = new AspectList();
            for (Aspect aspect : level1Aspects.getAspects()) {
                result.add(aspect, (int) (level1Aspects.getAmount(aspect) * multiplier));
            }
            return result;
        }
    }

    private static final Map<Enchantment, Data> DATA = new LinkedHashMap<>();

    static {
        vanilla(Enchantments.ALL_DAMAGE_PROTECTION, "protection", new AspectList().add(Aspect.EARTH, 10).add(Aspect.ENTROPY, 7));
        vanilla(Enchantments.FIRE_PROTECTION, "fireprotection", new AspectList().add(Aspect.FIRE, 10).add(Aspect.ENTROPY, 3).add(Aspect.WATER, 4));
        vanilla(Enchantments.FALL_PROTECTION, "featherfalling", new AspectList().add(Aspect.AIR, 16).add(Aspect.ORDER, 5));
        vanilla(Enchantments.BLAST_PROTECTION, "blastprotection", new AspectList().add(Aspect.EARTH, 5).add(Aspect.FIRE, 5).add(Aspect.ENTROPY, 8));
        vanilla(Enchantments.PROJECTILE_PROTECTION, "projectileprotection", new AspectList().add(Aspect.AIR, 10).add(Aspect.ENTROPY, 7));
        vanilla(Enchantments.RESPIRATION, "respiration", new AspectList().add(Aspect.WATER, 10).add(Aspect.AIR, 8).add(Aspect.ORDER, 5));
        vanilla(Enchantments.AQUA_AFFINITY, "aquaaffinity", new AspectList().add(Aspect.WATER, 25).add(Aspect.ORDER, 20).add(Aspect.EARTH, 5));
        vanilla(Enchantments.THORNS, "thorns", new AspectList().add(Aspect.EARTH, 10).add(Aspect.ENTROPY, 12));
        vanilla(Enchantments.SHARPNESS, "sharpness", new AspectList().add(Aspect.ORDER, 10));
        vanilla(Enchantments.SMITE, "smite", new AspectList().add(Aspect.ORDER, 5).add(Aspect.AIR, 5));
        vanilla(Enchantments.BANE_OF_ARTHROPODS, "baneofawfulturds", new AspectList().add(Aspect.ORDER, 5).add(Aspect.FIRE, 5));
        vanilla(Enchantments.KNOCKBACK, "knockback", new AspectList().add(Aspect.ENTROPY, 5).add(Aspect.AIR, 10));
        vanilla(Enchantments.FIRE_ASPECT, "fireaspect", new AspectList().add(Aspect.FIRE, 15).add(Aspect.EARTH, 4));
        vanilla(Enchantments.MOB_LOOTING, "looting", new AspectList().add(Aspect.AIR, 10).add(Aspect.FIRE, 10).add(Aspect.WATER, 10)
                .add(Aspect.EARTH, 10).add(Aspect.ORDER, 15).add(Aspect.ENTROPY, 15));
        vanilla(Enchantments.BLOCK_EFFICIENCY, "efficiency", new AspectList().add(Aspect.ENTROPY, 12).add(Aspect.EARTH, 4));
        vanilla(Enchantments.SILK_TOUCH, "silktouch", new AspectList().add(Aspect.ORDER, 50).add(Aspect.EARTH, 10).add(Aspect.ENTROPY, 10));
        vanilla(Enchantments.UNBREAKING, "unbreaking", new AspectList().add(Aspect.ORDER, 15).add(Aspect.WATER, 8).add(Aspect.EARTH, 8));
        vanilla(Enchantments.BLOCK_FORTUNE, "fortune", new AspectList().add(Aspect.AIR, 10).add(Aspect.FIRE, 10).add(Aspect.WATER, 10)
                .add(Aspect.EARTH, 10).add(Aspect.ORDER, 15).add(Aspect.ENTROPY, 15));
        vanilla(Enchantments.POWER_ARROWS, "power", new AspectList().add(Aspect.EARTH, 5).add(Aspect.ORDER, 10));
        vanilla(Enchantments.PUNCH_ARROWS, "punch", new AspectList().add(Aspect.AIR, 4).add(Aspect.EARTH, 10).add(Aspect.ENTROPY, 5));
        vanilla(Enchantments.FLAMING_ARROWS, "flame", new AspectList().add(Aspect.ENTROPY, 5).add(Aspect.FIRE, 20).add(Aspect.EARTH, 5));
        vanilla(Enchantments.INFINITY_ARROWS, "infinity", new AspectList().add(Aspect.ENTROPY, 40).add(Aspect.ORDER, 40).add(Aspect.EARTH, 10));
        vanilla(Enchantments.FISHING_SPEED, "lure", new AspectList().add(Aspect.WATER, 20).add(Aspect.BEAST, 20));
        vanilla(Enchantments.FISHING_LUCK, "luckofthesea", new AspectList().add(Aspect.ENTROPY, 20).add(Aspect.WATER, 20));
        custom(TtEnchantments.ASCENT_BOOST, "ascentboost", new AspectList().add(Aspect.ENTROPY, 8).add(Aspect.AIR, 10), EnchantingFeature.ENCHANT_ASCENT_BOOST);
        custom(TtEnchantments.SLOW_FALL, "slowfall", new AspectList().add(Aspect.ORDER, 8).add(Aspect.AIR, 10), EnchantingFeature.ENCHANT_SLOW_FALL);
        custom(TtEnchantments.AUTO_SMELT, "autosmelt", new AspectList().add(Aspect.ENTROPY, 20).add(Aspect.FIRE, 30), EnchantingFeature.ENCHANT_AUTO_SMELT);
        custom(TtEnchantments.DESINTEGRATE, "desintegrate", new AspectList().add(Aspect.ENTROPY, 25).add(Aspect.AIR, 10).add(Aspect.EARTH, 10), EnchantingFeature.ENCHANT_DESINTEGRATE);
        custom(TtEnchantments.QUICK_DRAW, "quickdraw", new AspectList().add(Aspect.ORDER, 10).add(Aspect.AIR, 10).add(Aspect.WATER, 5), EnchantingFeature.ENCHANT_QUICK_DRAW);
        custom(TtEnchantments.VAMPIRISM, "vamprisim", new AspectList().add(Aspect.ENTROPY, 8).add(Aspect.FIRE, 10).add(Aspect.WATER, 10), EnchantingFeature.ENCHANT_VAMPIRISM);
        custom(TtEnchantments.FOCUSED_STRIKE, "focusedstrikes", new AspectList().add(Aspect.ORDER, 12).add(Aspect.AIR, 10).add(Aspect.WATER, 10), EnchantingFeature.ENCHANT_FOCUSED_STRIKE);
        custom(TtEnchantments.DISPERSED_STRIKE, "dispersedstrikes", new AspectList().add(Aspect.ENTROPY, 12).add(Aspect.FIRE, 10).add(Aspect.EARTH, 10), EnchantingFeature.ENCHANT_DISPERSED_STRIKE);
        custom(TtEnchantments.VALIANCE, "valiance", new AspectList().add(Aspect.ORDER, 12).add(Aspect.FIRE, 10).add(Aspect.EARTH, 10), EnchantingFeature.ENCHANT_VALIANCE);
        custom(TtEnchantments.FINAL_STRIKE, "finalstrike", new AspectList().add(Aspect.ENTROPY, 16).add(Aspect.FIRE, 16), EnchantingFeature.ENCHANT_FINAL_STRIKE);
        custom(TtEnchantments.TUNNEL, "tunnel", new AspectList().add(Aspect.EARTH, 16).add(Aspect.ORDER, 16), EnchantingFeature.ENCHANT_TUNNEL);
        custom(TtEnchantments.SHATTER, "shatter", new AspectList().add(Aspect.EARTH, 16).add(Aspect.ENTROPY, 16), EnchantingFeature.ENCHANT_SHATTER);
        custom(TtEnchantments.SHOCKWAVE, "shockwave", new AspectList().add(Aspect.EARTH, 16).add(Aspect.AIR, 16), EnchantingFeature.ENCHANT_SHOCKWAVE);
        custom(TtEnchantments.POUNCE, "pounce", new AspectList().add(Aspect.EARTH, 16).add(Aspect.AIR, 16), EnchantingFeature.ENCHANT_POUNCE);
    }

    private EnchantmentManager() {
    }

    private static void vanilla(Enchantment enchantment, String texture, AspectList level1) {
        DATA.put(enchantment, new Data(enchantment, texture, true, level1, ""));
    }

    private static void custom(Enchantment enchantment, String texture, AspectList level1, String research) {
        DATA.put(enchantment, new Data(enchantment, texture, false, level1, research));
    }

    public static Map<Enchantment, Data> data() {
        return DATA;
    }

    public static Data data(Enchantment enchantment) {
        return DATA.get(enchantment);
    }

    public static boolean isTool(ItemStack stack) {
        return !stack.isEmpty() && !stack.is(Items.BOOK) && stack.getMaxStackSize() == 1
                && (stack.isDamageableItem() || stack.getItem() instanceof ArmorItem);
    }

    public static boolean canApply(ItemStack stack, Enchantment enchantment, List<Enchantment> current) {
        if (!DATA.containsKey(enchantment) || current.contains(enchantment) || !enchantment.canEnchant(stack)) {
            return false;
        }
        for (Enchantment other : current) {
            if (!TtEnchantments.areCompatible(other, enchantment)) {
                return false;
            }
        }
        return true;
    }

    public static boolean canEnchantStack(ItemStack stack) {
        return isTool(stack)
                && (stack.isEnchantable() || stack.getItem() instanceof ArmorItem)
                && !stack.isEnchanted();
    }

      
    public static boolean canEnchantmentBeUsed(Player player, Enchantment enchantment) {
        return DATA.containsKey(enchantment);
    }

    public static boolean isNoRemoveEnchant(ItemStack stack) {
        return stack.getItem() instanceof NoRemoveEnchant;
    }
}
