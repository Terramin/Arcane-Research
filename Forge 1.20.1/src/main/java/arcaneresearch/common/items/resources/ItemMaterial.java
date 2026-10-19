package arcaneresearch.common.items.resources;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
public class ItemMaterial extends Item {
    public ItemMaterial(Properties properties) {
        super(properties);
    }
    public ItemMaterial() {
        super(new Properties());
    }
    public static ItemMaterial basic() {
        return new ItemMaterial();
    }
    public static ItemMaterial uncommon() {
        return new ItemMaterial(new Properties().rarity(Rarity.UNCOMMON));
    }
    public static ItemMaterial rare() {
        return new ItemMaterial(new Properties().rarity(Rarity.RARE));
    }
    public static ItemMaterial epic() {
        return new ItemMaterial(new Properties().rarity(Rarity.EPIC));
    }
}
