package arcaneresearch.common.items.consumables;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemBathSalts extends Item {

    public ItemBathSalts(Item.Properties props) {
        super(props);
    }

    // NEOFORGE_PORT: no change needed - getEntityLifespan(ItemStack, Level) survives in 26.x
    // as an IItemExtension default, so this override still compiles as-is.
    @Override
    public int getEntityLifespan(ItemStack itemStack, Level level) {
        return 200;
    }
}
