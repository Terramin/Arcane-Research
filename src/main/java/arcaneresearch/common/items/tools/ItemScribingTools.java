package arcaneresearch.common.items.tools;

import arcaneresearch.api.items.IScribeTools;
import net.minecraft.world.item.Item;

public class ItemScribingTools extends Item implements IScribeTools {
    public ItemScribingTools() {
        super(new Properties().stacksTo(1).durability(100));
    }
}