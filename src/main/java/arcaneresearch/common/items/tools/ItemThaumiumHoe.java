package arcaneresearch.common.items.tools;

import net.minecraft.world.item.Item;
import arcaneresearch.api.ArcaneresearchMaterials;

public class ItemThaumiumHoe extends Item {

    public ItemThaumiumHoe(Item.Properties props) {
        
        
        super(props
                .hoe(ArcaneresearchMaterials.TOOLMAT_THAUMIUM, -2, -1.0F));
    }

    
    
    
}
