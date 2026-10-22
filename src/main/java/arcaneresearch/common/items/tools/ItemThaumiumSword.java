package arcaneresearch.common.items.tools;

import net.minecraft.world.item.Item;
import arcaneresearch.api.ArcaneresearchMaterials;

public class ItemThaumiumSword extends Item {

    public ItemThaumiumSword(Item.Properties props) {
        
        
        super(props
                .sword(ArcaneresearchMaterials.TOOLMAT_THAUMIUM, 3, -2.4F));
    }

    
    
    
}
