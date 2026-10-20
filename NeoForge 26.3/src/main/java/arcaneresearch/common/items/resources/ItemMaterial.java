package arcaneresearch.common.items.resources;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class ItemMaterial extends Item {

    public ItemMaterial(Item.Properties props) {
        super(props);
    }

    public static ItemMaterial basic(Item.Properties props) {
        return new ItemMaterial(props);
    }

    public static ItemMaterial uncommon(Item.Properties props) {
        return new ItemMaterial(props.rarity(Rarity.UNCOMMON));
    }

    public static ItemMaterial rare(Item.Properties props) {
        return new ItemMaterial(props.rarity(Rarity.RARE));
    }

    public static ItemMaterial epic(Item.Properties props) {
        return new ItemMaterial(props.rarity(Rarity.EPIC));
    }
}
