package arcaneresearch.discovered.table;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.discovered.table.DiscoveredTableBlock;
public final class HexTableFormer {
    private HexTableFormer() {
    }
    public static Direction findPairSide(Level level, BlockPos pos) {
        if (!(level.getBlockState(pos).getBlock() instanceof DiscoveredTableBlock)) {
            return null;
        }
        return findNeighbourSingle(level, pos);
    }
    public static boolean form(Level level, BlockPos pos, Direction side) {
        if (level.isClientSide() || HexTableRegistry.NOTE_BLOCK == null || side == null) {
            return false;
        }
        BlockPos sidePos = pos.relative(side);
        if (!(level.getBlockState(pos).getBlock() instanceof DiscoveredTableBlock)
                || !(level.getBlockState(sidePos).getBlock() instanceof DiscoveredTableBlock)) {
            return false;
        }
        BlockState main = HexTableRegistry.NOTE_BLOCK.defaultBlockState()
                .setValue(HexNoteTableBlock.FACING, side)
                .setValue(HexNoteTableBlock.PART, HexNoteTableBlock.Part.MAIN);
        BlockState sideState = HexTableRegistry.NOTE_BLOCK.defaultBlockState()
                .setValue(HexNoteTableBlock.FACING, side.getOpposite())
                .setValue(HexNoteTableBlock.PART, HexNoteTableBlock.Part.SIDE);
        level.setBlock(pos, main, 3);
        level.setBlock(sidePos, sideState, 3);
        level.playSound(null, pos, SoundType.WOOD.getPlaceSound(), SoundSource.BLOCKS, 1.0F, 0.9F);
        return true;
    }
    private static Direction findNeighbourSingle(Level level, BlockPos pos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(pos.relative(dir)).getBlock() instanceof DiscoveredTableBlock) {
                return dir;
            }
        }
        return null;
    }
}
