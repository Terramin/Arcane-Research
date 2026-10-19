package arcaneresearch.discovered.table;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

import arcaneresearch.discovered.table.DiscoveredTableBlock;

/**
 * Сборка hex-пары из двух проектных одиночных столов: кликнутый — MAIN,
 * сосед — SIDE (та же схема, что клик чернилами у стола 1). Инициатор —
 * клик таумометром ({@code MsgStudyTable}); salis-пути нет.
 *
 * NEOFORGE_PORT: isClientSide-поле -&gt; isClientSide().
 */
public final class HexTableFormer {

    private HexTableFormer() {
    }

    /**
     * Собрать пару из одиночного стола под кликом: кликнутый — MAIN,
     * сосед — SIDE. Возвращает сторону соседа (null — не из чего).
     */
    public static Direction findPairSide(Level level, BlockPos pos) {
        if (!(level.getBlockState(pos).getBlock() instanceof DiscoveredTableBlock)) {
            return null;
        }
        return findNeighbourSingle(level, pos);
    }

    /** Поставить MAIN/SIDE со звуком (только сервер). */
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

    /** Соседний проектный одиночный стол по горизонтали (первый найденный). */
    private static Direction findNeighbourSingle(Level level, BlockPos pos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(pos.relative(dir)).getBlock() instanceof DiscoveredTableBlock) {
                return dir;
            }
        }
        return null;
    }
}
