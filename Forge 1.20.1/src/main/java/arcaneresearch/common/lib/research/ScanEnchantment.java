package arcaneresearch.common.lib.research;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;
import arcaneresearch.api.research.IScanThing;
import arcaneresearch.api.research.ScanningManager;
import java.util.Map;
public class ScanEnchantment implements IScanThing {
    private final Enchantment enchantment;
    public ScanEnchantment(Enchantment enchantment) {
        this.enchantment = enchantment;
    }
    @Override
    public boolean checkThing(Player player, Object obj) {
        return getEnchantment(player, obj) != null;
    }
    private Enchantment getEnchantment(Player player, Object obj) {
        if (obj == null || enchantment == null) {
            return null;
        }
        ItemStack stack = ScanningManager.getItemFromParams(player, obj);
        if (stack != null && !stack.isEmpty()) {
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
            for (Enchantment ench : enchantments.keySet()) {
                if (ench == enchantment) {
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
        return "!" + ForgeRegistries.ENCHANTMENTS.getKey(enchantment).toString();
    }
}
