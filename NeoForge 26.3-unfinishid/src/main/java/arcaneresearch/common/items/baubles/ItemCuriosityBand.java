package arcaneresearch.common.items.baubles;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class ItemCuriosityBand extends Item implements ICurioItem {

    public ItemCuriosityBand(Item.Properties props) {
        super(props
                .stacksTo(1)
                .rarity(Rarity.RARE));
    }

    public static boolean isWearingBand(net.minecraft.world.entity.player.Player player) {

        if (arcaneresearch.common.lib.compat.CuriosCompat.isCuriosLoaded() && !arcaneresearch.common.lib.compat.CuriosCompat.findCuriosItems(player,
                s -> !s.isEmpty() && s.getItem() == arcaneresearch.init.ModItems.CURIOSITY_BAND.get()).isEmpty()) {
            return true;
        }

        // NEOFORGE_PORT: Inventory.items is private in 26.x -> indexed access via getItem (same order).
        net.minecraft.world.entity.player.Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemCuriosityBand) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return slotContext.identifier().equals("head");
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}
