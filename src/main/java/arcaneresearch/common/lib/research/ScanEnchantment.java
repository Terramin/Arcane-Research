package arcaneresearch.common.lib.research;

import arcaneresearch.api.research.IScanThing;
import arcaneresearch.api.research.ScanningManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

public class ScanEnchantment implements IScanThing {

    private final Enchantment enchantment;
    private String researchKey;

    public ScanEnchantment(Enchantment enchantment) {
        this.enchantment = enchantment;
    }

    @Override
    public boolean checkThing(Player player, Object obj) {
        return getEnchantment(player, obj) != null;
    }

    private Enchantment getEnchantment(Player player, Object obj) {
        if (obj == null || enchantment == null) return null;
        if (player == null) return null;

        ItemStack stack = ScanningManager.getItemFromParams(player, obj);
        if (stack == null || stack.isEmpty()) return null;

        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        if (enchantments.isEmpty()) return null;

        return enchantments.containsKey(enchantment) ? enchantment : null;
    }

    @Override
    public String getResearchKey(Player player, Object obj) {
        if (enchantment == null) return null;

        if (researchKey == null) {
            ResourceLocation id = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
            researchKey = id != null ? "!" + id : null;
        }
        return researchKey;
    }
}