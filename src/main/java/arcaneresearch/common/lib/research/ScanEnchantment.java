package arcaneresearch.common.lib.research;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import arcaneresearch.api.research.IScanThing;
import arcaneresearch.api.research.ScanningManager;





public class ScanEnchantment implements IScanThing {

    private final Holder<Enchantment> enchantment;

    public ScanEnchantment(Holder<Enchantment> enchantment) {
        this.enchantment = enchantment;
    }

    @Override
    public boolean checkThing(Player player, Object obj) {
        return getEnchantment(player, obj) != null;
    }

    private Holder<Enchantment> getEnchantment(Player player, Object obj) {
        if (obj == null || enchantment == null) {
            return null;
        }
        ItemStack stack = ScanningManager.getItemFromParams(player, obj);
        if (stack != null && !stack.isEmpty()) {
            ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
            for (Holder<Enchantment> ench : enchantments.keySet()) {
                if (ench.equals(enchantment)) {
                    return ench;
                }
            }
        }
        return null;
    }

    @Override
    public String getResearchKey(Player player, Object obj) {
        if (enchantment == null) {
            return null;
        }

        return "!" + enchantment.unwrapKey().map(k -> k.identifier().toString()).orElse("enchantment");
    }
}
