package arcaneresearch.common.items.consumables;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
public class ItemBathSalts extends Item {
    public ItemBathSalts() {
        super(new Item.Properties());
    }
    @Override
    public int getEntityLifespan(ItemStack itemStack, Level level) {
        return 200;
    }
}
