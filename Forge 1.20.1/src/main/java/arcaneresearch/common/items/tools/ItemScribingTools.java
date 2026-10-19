package arcaneresearch.common.items.tools;
import net.minecraft.world.item.Item;
import arcaneresearch.api.items.IScribeTools;
public class ItemScribingTools extends Item implements IScribeTools {
    public ItemScribingTools() {
        super(new Properties().stacksTo(1).durability(100));
    }
}