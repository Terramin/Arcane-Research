package arcaneresearch.common.items.tools;

import net.minecraft.world.item.Item;
import arcaneresearch.api.ArcaneresearchMaterials;

public class ItemThaumiumShovel extends Item {

    public ItemThaumiumShovel(Item.Properties props) {
        
        
        super(props
                .shovel(ArcaneresearchMaterials.TOOLMAT_THAUMIUM, 1.5F, -3.0F));
    }

    
    
    
}
