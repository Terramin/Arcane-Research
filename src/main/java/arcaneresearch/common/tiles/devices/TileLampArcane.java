package arcaneresearch.common.tiles.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModBlocks;

public class TileLampArcane extends TileArcaneresearch {

    private static final int RANGE = 15;

    public TileLampArcane(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileLampArcane(BlockPos pos, BlockState state) {
        this(ModBlockEntities.LAMP_ARCANE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileLampArcane tile) {

        if (level.getLevelData().getGameTime() % 5 != 0) return;
        if (tile.gettingPower()) return;

        int x = level.getRandom().nextInt(16) - level.getRandom().nextInt(16);
        int y = level.getRandom().nextInt(16) - level.getRandom().nextInt(16);
        int z = level.getRandom().nextInt(16) - level.getRandom().nextInt(16);

        BlockPos targetPos = pos.offset(x, y, z);

        int surfaceY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, targetPos.getX(), targetPos.getZ());
        if (targetPos.getY() > surfaceY + 4) {
            targetPos = new BlockPos(targetPos.getX(), surfaceY + 4, targetPos.getZ());
        }
        if (targetPos.getY() < level.getMinY() + 5) {
            targetPos = new BlockPos(targetPos.getX(), level.getMinY() + 5, targetPos.getZ());
        }

        if (level.isEmptyBlock(targetPos) &&
                level.getBrightness(LightLayer.BLOCK, targetPos) < 11 &&
                hasLineOfSight(level, pos, targetPos)) {

            level.setBlock(targetPos, ModBlocks.EFFECT_GLIMMER.get().defaultBlockState(), 3);
        }
    }

    private static boolean hasLineOfSight(Level level, BlockPos from, BlockPos to) {

        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double dz = to.getZ() - from.getZ();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (distance < 1) return true;

        dx /= distance;
        dy /= distance;
        dz /= distance;

        double x = from.getX() + 0.5;
        double y = from.getY() + 0.5;
        double z = from.getZ() + 0.5;

        for (int i = 0; i < (int) distance; i++) {
            x += dx;
            y += dy;
            z += dz;

            BlockPos checkPos = new BlockPos((int) x, (int) y, (int) z);
            if (!level.isEmptyBlock(checkPos) && !checkPos.equals(from) && !checkPos.equals(to)) {
                BlockState state = level.getBlockState(checkPos);

                if (state.canOcclude()) {
                    return false;
                }
            }
        }

        return true;
    }

    public void removeLights() {
        if (level == null) return;

        for (int x = -RANGE; x <= RANGE; x++) {
            for (int y = -RANGE; y <= RANGE; y++) {
                for (int z = -RANGE; z <= RANGE; z++) {
                    BlockPos checkPos = worldPosition.offset(x, y, z);
                    BlockState state = level.getBlockState(checkPos);
                    if (state.is(ModBlocks.EFFECT_GLIMMER.get())) {
                        level.setBlock(checkPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    
    
    
    
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        super.preRemoveSideEffects(pos, oldState);
        removeLights();
    }

    protected boolean gettingPower() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }
}
