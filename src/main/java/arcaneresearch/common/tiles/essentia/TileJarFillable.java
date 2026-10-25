package arcaneresearch.common.tiles.essentia;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.init.ModBlockEntities;
@Deprecated
public class TileJarFillable extends TileJar {
    public static int CAPACITY = TileJar.CAPACITY;
    public TileJarFillable(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    public TileJarFillable(BlockPos pos, BlockState state) {
        this(ModBlockEntities.JAR.get(), pos, state);
    }
}
