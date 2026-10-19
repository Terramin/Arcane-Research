package arcaneresearch.common.items.baubles;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import arcaneresearch.common.lib.compat.CuriosCompat;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

public class ItemCharmUndying extends Item implements ICurioItem {

    public ItemCharmUndying(Item.Properties props) {
        super(props
                .stacksTo(1)
                .rarity(Rarity.RARE));
    }

    public static ItemStack findCharm(net.minecraft.world.entity.player.Player player) {

        List<ItemStack> curiosCharms = CuriosCompat.findCuriosItems(player,
                s -> !s.isEmpty() && s.getItem() instanceof ItemCharmUndying);
        if (!curiosCharms.isEmpty()) {
            return curiosCharms.get(0);
        }

        // NEOFORGE_PORT: Inventory.items is private in 26.x -> indexed access via getItem (same order).
        net.minecraft.world.entity.player.Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemCharmUndying) {
                return stack;
            }
        }

        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty() && offhand.getItem() instanceof ItemCharmUndying) {
            return offhand;
        }
        return null;
    }

    public static void applyUndyingEffect(net.minecraft.world.entity.player.Player player, ItemStack charm) {

        player.setHealth(1.0f);

        player.removeAllEffects();

        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.REGENERATION, 900, 1));
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.ABSORPTION, 100, 1));

        charm.shrink(1);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return slotContext.identifier().equals("charm");
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}
