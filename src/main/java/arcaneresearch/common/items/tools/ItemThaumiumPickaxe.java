package arcaneresearch.common.items.tools;

import net.minecraft.world.item.Item;
import arcaneresearch.api.ArcaneresearchMaterials;

public class ItemThaumiumPickaxe extends Item {

    public ItemThaumiumPickaxe(Item.Properties props) {
        
        
        super(props
                .pickaxe(ArcaneresearchMaterials.TOOLMAT_THAUMIUM, 1, -2.8F));
    }

    
    
    
}
