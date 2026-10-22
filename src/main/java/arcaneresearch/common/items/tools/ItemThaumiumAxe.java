package arcaneresearch.common.items.tools;

import net.minecraft.world.item.Item;
import arcaneresearch.api.ArcaneresearchMaterials;

public class ItemThaumiumAxe extends Item {

    public ItemThaumiumAxe(Item.Properties props) {
        
        
        super(props
                .axe(ArcaneresearchMaterials.TOOLMAT_THAUMIUM, 5.5F, -3.0F));
    }

    
    
    
}
