package arcaneresearch.common.items.resources;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
public class ItemVisneriteIngot extends Item {
    public ItemVisneriteIngot(Item.Properties props) {
        super(props.stacksTo(64).fireResistant().rarity(Rarity.COMMON));
    }
}
