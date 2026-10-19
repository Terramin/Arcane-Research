package arcaneresearch.common.tiles.essentia;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.init.ModBlockEntities;
@Deprecated
public class TileJarFillableVoid extends TileJarVoid {
    public TileJarFillableVoid(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    public TileJarFillableVoid(BlockPos pos, BlockState state) {
        this(ModBlockEntities.JAR_VOID.get(), pos, state);
    }
}
