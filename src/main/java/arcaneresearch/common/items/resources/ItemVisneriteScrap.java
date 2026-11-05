package arcaneresearch.common.items.resources;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
public class ItemVisneriteScrap extends Item {
    public ItemVisneriteScrap() {
        super(new Item.Properties().stacksTo(64).fireResistant().rarity(Rarity.COMMON));
    }
}
