package arcaneresearch.common.items.tools;

import net.minecraft.world.item.Item;

import arcaneresearch.api.items.IScribeTools;
import net.minecraft.world.item.Item;

public class ItemScribingTools extends Item implements IScribeTools {
    public ItemScribingTools(Item.Properties props) {
        super(props.stacksTo(1).durability(100));
    }
}
