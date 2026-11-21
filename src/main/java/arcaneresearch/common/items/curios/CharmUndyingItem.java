package arcaneresearch.common.items.curios;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import arcaneresearch.common.lib.compat.CuriosCompat;

import java.util.List;

public class CharmUndyingItem extends EquippableCurioItem {
    public CharmUndyingItem(Item.Properties props) {
        super(props
                .rarity(Rarity.RARE), CurioSlot.CHARM);
    }

    public static ItemStack findCharm(Player player) {
        List<ItemStack> curiosCharms = CuriosCompat.findCuriosItems(player,
                s -> !s.isEmpty() && s.getItem() instanceof CharmUndyingItem);
        if (!curiosCharms.isEmpty()) {
            return curiosCharms.get(0);
        }
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (!stack.isEmpty() && stack.getItem() instanceof CharmUndyingItem) {
                return stack;
            }
        }
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty() && offhand.getItem() instanceof CharmUndyingItem) {
            return offhand;
        }
        return null;
    }

    public static void applyUndyingEffect(Player player, ItemStack charm) {
        player.setHealth(1.0f);
        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(
                MobEffects.REGENERATION, 900, 1));
        player.addEffect(new MobEffectInstance(
                MobEffects.ABSORPTION, 100, 1));
        charm.shrink(1);
    }

}
