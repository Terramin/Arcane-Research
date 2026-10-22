package arcaneresearch.common.items.aura;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

 



 
public class ItemNodeDevice extends BlockItem {
    public enum Kind {
        STABILIZER,
        ADVANCED,
        TRANSDUCER,
        CONVERTER
    }

    private final Kind kind;

    public ItemNodeDevice(Block block, Properties properties, Kind kind) {
        super(block, properties);
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }

    
    
    
    
}
