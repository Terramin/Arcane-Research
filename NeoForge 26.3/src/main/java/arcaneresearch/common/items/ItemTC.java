package arcaneresearch.common.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class ItemTC extends Item {

    public ItemTC(Properties properties) {
        super(properties);
    }

    public ItemTC() {
        super(new Properties());
    }

    public static Properties basicProps() {
        return new Properties();
    }

    public static Properties unstackable() {
        return new Properties().stacksTo(1);
    }

    public static Properties toolProps() {
        return new Properties().stacksTo(1);
    }

    public static Properties uncommonProps() {
        return new Properties().rarity(Rarity.UNCOMMON);
    }

    public static Properties rareProps() {
        return new Properties().rarity(Rarity.RARE);
    }

    public static Properties epicProps() {
        return new Properties().rarity(Rarity.EPIC);
    }
}
