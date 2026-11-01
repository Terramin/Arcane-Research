package arcaneresearch.common.tiles.misc;

import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.lib.utils.BlockUtils;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class TileHole extends TileMemory {

    public short countdown = 0;
    public short countdownMax = 120;
    public byte depth = 0;
    public Direction direction = null;

    public TileHole(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HOLE.get(), pos, state);
    }

    public TileHole(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileHole tile) {
        if (tile.countdown == 0 && tile.depth > 1 && tile.direction != null) {
            tile.propagateHole(level);
        }
        tile.countdown++;
        if (tile.countdown % 20 == 0) {
            tile.setChanged();
        }
        if (tile.countdown >= tile.countdownMax) {
            tile.restoreBlock(level);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TileHole tile) {
        for (int a = 0; a < 2; ++a) {
            tile.spawnParticles(level);
        }
    }

    public void configure(BlockState originalBlock, short maxTicks, byte depth, Direction direction) {
        setOldBlock(originalBlock);
        this.countdownMax = maxTicks;
        this.depth = depth;
        this.direction = direction;
        this.countdown = 0;
    }

    private void propagateHole(Level level) {
        Direction dir = this.direction;
        if (dir == null) {
            return;
        }
        Direction.Axis axis = dir.getAxis();
        for (int a = 0; a < 9; a++) {
            if (a / 3 == 1 && a % 3 == 1) {
                continue;
            }
            BlockPos offsetPos;
            switch (axis) {
                case Y -> offsetPos = worldPosition.offset(-1 + a / 3, 0, -1 + a % 3);
                case Z -> offsetPos = worldPosition.offset(-1 + a / 3, -1 + a % 3, 0);
                case X -> offsetPos = worldPosition.offset(0, -1 + a / 3, -1 + a % 3);
                default -> offsetPos = worldPosition;
            }
            createHoleAt(level, offsetPos, (byte) 1, countdownMax, null);
        }
        BlockPos nextPos = worldPosition.relative(dir.getOpposite());
        if (!createHoleAt(level, nextPos, (byte) (depth - 1), countdownMax, dir)) {
            depth = 0;
        }
    }

    private boolean createHoleAt(Level level, BlockPos pos, byte depth, short maxTicks, Direction dir) {
        if (level.getBlockEntity(pos) != null) {
            return false;
        }
        BlockState targetState = level.getBlockState(pos);
        Block holeBlock = ModBlocks.HOLE.get();
        if (BlockUtils.isPortableHoleBlackListed(targetState)
                || targetState.is(Blocks.BEDROCK)
                || targetState.is(holeBlock)) {
            return false;
        }
        if (!(targetState.isAir() || !targetState.canBeReplaced())) {
            return false;
        }
        if (!targetState.getFluidState().isEmpty()) {
            return false;
        }
        if (targetState.getDestroySpeed(level, pos) == -1.0f) {
            return false;
        }
        level.setBlock(pos, holeBlock.defaultBlockState(), 3);
        if (level.getBlockEntity(pos) instanceof TileHole holeTile) {
            holeTile.configure(targetState, maxTicks, depth, dir);
            return true;
        }
        level.setBlock(pos, targetState, 3);
        return false;
    }

    private void restoreBlock(Level level) {
        level.setBlock(worldPosition, oldBlock, 3);
        if (tileEntityCompound != null) {
            BlockEntity be = level.getBlockEntity(worldPosition);
            if (be != null) {
                be.load(tileEntityCompound);
                be.setChanged();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnParticles(Level level) {
        Block holeBlock = ModBlocks.HOLE.get();
        for (Direction d1 : Direction.values()) {
            BlockPos posD1 = worldPosition.relative(d1);
            BlockState b1 = level.getBlockState(posD1);
            if (b1.is(holeBlock) || b1.isSolidRender(level, posD1)) {
                continue;
            }
            for (Direction d2 : Direction.values()) {
                if (d1.getAxis() == d2.getAxis()) {
                    continue;
                }
                BlockPos posD2 = worldPosition.relative(d2);
                BlockState bD2 = level.getBlockState(posD2);
                boolean solid = bD2.isSolidRender(level, posD2);
                if (!solid) {
                    BlockPos posD1D2 = posD1.relative(d2);
                    solid = level.getBlockState(posD1D2).isSolidRender(level, posD1D2);
                }
                if (!solid) {
                    continue;
                }
                float sx = 0.5f * d1.getStepX();
                float sy = 0.5f * d1.getStepY();
                float sz = 0.5f * d1.getStepZ();
                if (sx == 0.0f) sx = 0.5f * d2.getStepX();
                if (sy == 0.0f) sy = 0.5f * d2.getStepY();
                if (sz == 0.0f) sz = 0.5f * d2.getStepZ();
                if (sx == 0.0f) sx = level.random.nextFloat(); else sx += 0.5f;
                if (sy == 0.0f) sy = level.random.nextFloat(); else sy += 0.5f;
                if (sz == 0.0f) sz = level.random.nextFloat(); else sz += 0.5f;
                FXDispatcher.INSTANCE.sparkle(
                        worldPosition.getX() + sx,
                        worldPosition.getY() + sy,
                        worldPosition.getZ() + sz,
                        0.25f, 0.25f, 1.0f);
            }
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putShort("countdown", countdown);
        tag.putShort("countdownMax", countdownMax);
        tag.putByte("depth", depth);
        tag.putByte("direction", direction == null ? (byte) -1 : (byte) direction.ordinal());
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        countdown = tag.getShort("countdown");
        countdownMax = tag.getShort("countdownMax");
        depth = tag.getByte("depth");
        byte dirByte = tag.getByte("direction");
        Direction[] dirs = Direction.values();
        direction = dirByte >= 0 && dirByte < dirs.length ? dirs[dirByte] : null;
    }
}