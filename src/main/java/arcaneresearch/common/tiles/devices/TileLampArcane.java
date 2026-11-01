package arcaneresearch.common.tiles.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
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
        if (level == null) return;
        if (level.getGameTime() % 5 != 0) return;
        if (tile.gettingPower()) return;

        Block glimmer = ModBlocks.EFFECT_GLIMMER.get();

        int x = level.random.nextInt(16) - level.random.nextInt(16);
        int y = level.random.nextInt(16) - level.random.nextInt(16);
        int z = level.random.nextInt(16) - level.random.nextInt(16);

        BlockPos.MutableBlockPos targetPos = new BlockPos.MutableBlockPos(
                pos.getX() + x,
                pos.getY() + y,
                pos.getZ() + z
        );

        int surfaceY = level.getHeight(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                targetPos.getX(),
                targetPos.getZ()
        );

        int minY = level.getMinBuildHeight() + 5;

        if (targetPos.getY() > surfaceY + 4) {
            targetPos.setY(surfaceY + 4);
        }

        if (targetPos.getY() < minY) {
            targetPos.setY(minY);
        }

        if (level.isEmptyBlock(targetPos)
                && level.getBrightness(LightLayer.BLOCK, targetPos) < 11
                && hasLineOfSight(level, pos, targetPos)) {
            level.setBlock(targetPos, glimmer.defaultBlockState(), 3);
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

        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
        int steps = (int) Math.ceil(distance);

        for (int i = 0; i < steps; i++) {
            x += dx;
            y += dy;
            z += dz;

            checkPos.set(
                    (int) Math.floor(x),
                    (int) Math.floor(y),
                    (int) Math.floor(z)
            );

            if (checkPos.equals(from) || checkPos.equals(to)) continue;
            if (level.isEmptyBlock(checkPos)) continue;

            BlockState state = level.getBlockState(checkPos);
            if (state.canOcclude()) {
                return false;
            }
        }

        return true;
    }

    public void removeLights() {
        if (level == null) return;

        Block glimmer = ModBlocks.EFFECT_GLIMMER.get();

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();

        for (int x = -RANGE; x <= RANGE; x++) {
            for (int y = -RANGE; y <= RANGE; y++) {
                for (int z = -RANGE; z <= RANGE; z++) {
                    checkPos.set(
                            worldPosition.getX() + x,
                            worldPosition.getY() + y,
                            worldPosition.getZ() + z
                    );

                    BlockState state = level.getBlockState(checkPos);
                    if (state.is(glimmer)) {
                        level.setBlock(checkPos, air, 3);
                    }
                }
            }
        }
    }

    protected boolean gettingPower() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }
}