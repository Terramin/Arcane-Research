package arcaneresearch.common.tiles.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import arcaneresearch.common.tiles.TileArcaneresearch;

 





 

 


 
public class EldritchCapTile extends TileArcaneresearch {
    public EldritchCapTile(BlockEntityType<?> tileType, BlockPos pos, BlockState state) {
        super(tileType, pos, state);
    }

    public AABB getRenderBoundingBox() {
        return AABB.encapsulatingFullBlocks(this.worldPosition.offset(-1, -1, -1), this.worldPosition.offset(2, 2, 2));
    }
}
