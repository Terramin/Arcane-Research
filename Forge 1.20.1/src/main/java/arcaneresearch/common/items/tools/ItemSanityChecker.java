package arcaneresearch.common.items.tools;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
public class ItemSanityChecker extends Item {
    public ItemSanityChecker() {
        super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }
}