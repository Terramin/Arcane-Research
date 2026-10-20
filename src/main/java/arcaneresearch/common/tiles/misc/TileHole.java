package arcaneresearch.common.tiles.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.client.fx.FXDispatcher;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import arcaneresearch.common.lib.utils.BlockUtils;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModBlocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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

        Direction.Axis axis = direction.getAxis();

        for (int a = 0; a < 9; a++) {

            if (a / 3 == 1 && a % 3 == 1) continue;

            BlockPos offsetPos;
            switch (axis) {
                case Y -> offsetPos = worldPosition.offset(-1 + a / 3, 0, -1 + a % 3);
                case Z -> offsetPos = worldPosition.offset(-1 + a / 3, -1 + a % 3, 0);
                case X -> offsetPos = worldPosition.offset(0, -1 + a / 3, -1 + a % 3);
                default -> offsetPos = worldPosition;
            }

            createHoleAt(level, offsetPos, (byte) 1, countdownMax, null);
        }

        BlockPos nextPos = worldPosition.relative(direction.getOpposite());
        if (!createHoleAt(level, nextPos, (byte) (depth - 1), countdownMax, direction)) {

            depth = 0;
        }
    }

    private boolean createHoleAt(Level level, BlockPos pos, byte depth, short maxTicks, Direction dir) {

        if (level.getBlockEntity(pos) != null) {
            return false;
        }
        BlockState targetState = level.getBlockState(pos);
        if (BlockUtils.isPortableHoleBlackListed(targetState)
                || targetState.is(net.minecraft.world.level.block.Blocks.BEDROCK)
                || targetState.is(ModBlocks.HOLE.get())) {
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

        level.setBlock(pos, ModBlocks.HOLE.get().defaultBlockState(), 3);

        if (level.getBlockEntity(pos) instanceof TileHole holeTile) {
            holeTile.configure(targetState, maxTicks, depth, dir);
            return true;
        }

        return false;
    }

    private void restoreBlock(Level level) {
        level.setBlock(worldPosition, oldBlock, 3);

        if (tileEntityCompound != null && level.getBlockEntity(worldPosition) != null) {
            // NEOFORGE_PORT: BlockEntity.load(CompoundTag) removed -> TagValueInput + loadCustomOnly
            // (cf. EldritchOculusRitual).
            try {
                level.getBlockEntity(worldPosition).loadCustomOnly(
                        net.minecraft.world.level.storage.TagValueInput.create(
                                net.minecraft.util.ProblemReporter.DISCARDING,
                                level.registryAccess(), tileEntityCompound));
            } catch (Exception ignored) {
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnParticles(Level level) {
        for (Direction d1 : Direction.values()) {
            BlockState b1 = level.getBlockState(worldPosition.relative(d1));
            if (!b1.is(ModBlocks.HOLE.get()) && !b1.isSolidRender()) {
                for (Direction d2 : Direction.values()) {
                    if (d1.getAxis() != d2.getAxis()
                            && (level.getBlockState(worldPosition.relative(d2)).isSolidRender()
                            || level.getBlockState(worldPosition.relative(d1).relative(d2)).isSolidRender())) {
                        float sx = 0.5f * d1.getStepX();
                        float sy = 0.5f * d1.getStepY();
                        float sz = 0.5f * d1.getStepZ();
                        if (sx == 0.0f) {
                            sx = 0.5f * d2.getStepX();
                        }
                        if (sy == 0.0f) {
                            sy = 0.5f * d2.getStepY();
                        }
                        if (sz == 0.0f) {
                            sz = 0.5f * d2.getStepZ();
                        }
                        if (sx == 0.0f) {
                            sx = level.getRandom().nextFloat();
                        } else {
                            sx += 0.5f;
                        }
                        if (sy == 0.0f) {
                            sy = level.getRandom().nextFloat();
                        } else {
                            sy += 0.5f;
                        }
                        if (sz == 0.0f) {
                            sz = level.getRandom().nextFloat();
                        } else {
                            sz += 0.5f;
                        }
                        FXDispatcher.INSTANCE.sparkle(worldPosition.getX() + sx, worldPosition.getY() + sy,
                                worldPosition.getZ() + sz, 0.25f, 0.25f, 1.0f);
                    }
                }
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putShort("countdown", countdown);
        tag.putShort("countdownMax", countdownMax);
        tag.putByte("depth", depth);
        tag.putByte("direction", direction == null ? (byte) -1 : (byte) direction.ordinal());
    }

    @Override
    public void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        // NEOFORGE_PORT: ValueInput.getShortOr returns int -> cast (fields are short).
        countdown = (short) tag.getShortOr("countdown", (short) 0);
        countdownMax = (short) tag.getShortOr("countdownMax", (short) 0);
        depth = tag.getByteOr("depth", (byte) 0);
        byte dirByte = tag.getByteOr("direction", (byte) 0);
        direction = dirByte >= 0 && dirByte < Direction.values().length ? Direction.values()[dirByte] : null;
    }
}
