package arcaneresearch.common.tiles.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.IEssentiaTransport;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;
import java.util.ArrayList;
import java.util.List;

public class TileLampGrowth extends TileArcaneresearch implements IEssentiaTransport {
    private static final int RANGE = 6;
    private static final int MAX_CHARGES = 20;

    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
    private final List<BlockPos> checklist = new ArrayList<>((2 * RANGE + 1) * (2 * RANGE + 1));

    public int charges = -1;
    private boolean reserve = false;
    private int lastX, lastY, lastZ;
    private BlockState lastState;
    private int drawDelay = 0;

    public TileLampGrowth(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileLampGrowth(BlockPos pos, BlockState state) {
        this(ModBlockEntities.LAMP_GROWTH.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileLampGrowth tile) {
        boolean powered = tile.gettingPower();
        boolean drew = tile.drawEssentia();

        if (tile.charges <= 0) {
            if (tile.reserve) {
                tile.charges = MAX_CHARGES;
                tile.reserve = false;
                tile.setChanged();
                tile.syncTile(true);
            } else if (drew) {
                tile.charges = MAX_CHARGES;
                drew = false;
                tile.setChanged();
                tile.syncTile(true);
            }
            if (tile.charges <= 0) {
                tile.setEnabled(state, false);
            } else if (!powered) {
                tile.setEnabled(state, true);
            }
        }

        if (drew) {
            tile.reserve = true;
        }

        if (tile.charges == 0) {
            tile.charges = -1;
            tile.syncTile(true);
        }

        if (!powered && tile.charges > 0) {
            tile.updatePlant();
        }
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        tag.putBoolean("reserve", reserve);
        tag.putInt("charges", charges);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        reserve = tag.contains("reserve") ? tag.getBoolean("reserve") : tag.getBoolean("Reserve");
        charges = tag.contains("charges") ? tag.getInt("charges") : tag.getInt("Charges");
    }

    boolean isPlant(BlockPos bp) {
        if (level == null) return false;
        Block block = level.getBlockState(bp).getBlock();
        if (block instanceof net.minecraft.world.level.block.CactusBlock
                || block instanceof net.minecraft.world.level.block.SugarCaneBlock) {
            return true;
        }
        return block instanceof BonemealableBlock
                && !(block instanceof net.minecraft.world.level.block.TallGrassBlock)
                && !(block instanceof net.minecraft.world.level.block.DoublePlantBlock);
    }

    private void updatePlant() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (lastState != null) {
            mutablePos.set(lastX, lastY, lastZ);
            BlockState tracked = level.getBlockState(mutablePos);
            if (!tracked.equals(lastState)) {
                BlockPos effectPos = new BlockPos(lastX, lastY, lastZ);
                arcaneresearch.common.lib.network.PacketHandler.sendToAllTrackingChunk(
                        new arcaneresearch.common.lib.network.fx.PacketFXBlockMist(effectPos, 4259648),
                        serverLevel, effectPos);
                lastState = tracked;
            }
        }

        if (checklist.isEmpty()) {
            for (int x = -RANGE; x <= RANGE; x++) {
                for (int z = -RANGE; z <= RANGE; z++) {
                    checklist.add(worldPosition.offset(x, RANGE, z));
                }
            }
            for (int i = checklist.size() - 1; i > 0; i--) {
                int j = level.random.nextInt(i + 1);
                BlockPos temp = checklist.get(i);
                checklist.set(i, checklist.get(j));
                checklist.set(j, temp);
            }
        }

        BlockPos startPos = checklist.remove(checklist.size() - 1);
        int x = startPos.getX();
        int z = startPos.getZ();
        int minY = worldPosition.getY() - RANGE;
        int maxDistSq = RANGE * RANGE;

        for (int y = startPos.getY(); y >= minY; y--) {
            mutablePos.set(x, y, z);
            if (level.isEmptyBlock(mutablePos)) continue;
            if (!isPlant(mutablePos)) continue;
            if (mutablePos.distSqr(worldPosition) > maxDistSq) continue;
            if (arcaneresearch.common.lib.utils.CropUtils.isGrownCrop(level, mutablePos)) continue;
            if (!arcaneresearch.common.lib.utils.CropUtils.doesLampGrow(level, mutablePos)) continue;

            charges--;
            lastX = x;
            lastY = y;
            lastZ = z;
            lastState = level.getBlockState(mutablePos);
            serverLevel.scheduleTick(new BlockPos(x, y, z), lastState.getBlock(), 1);
            return;
        }
    }

    private boolean drawEssentia() {
        if (++drawDelay < 5) return false;
        drawDelay = 0;
        if (level == null) return false;

        Direction facing = getFacing();
        BlockEntity te = ArcaneresearchApiHelper.getConnectableTile(level, worldPosition, facing);
        if (!(te instanceof IEssentiaTransport transport)) return false;

        Direction opposite = facing.getOpposite();
        if (!transport.canOutputTo(opposite)) return false;
        return transport.getSuctionAmount(opposite) < getSuctionAmount(facing)
                && transport.takeEssentia(Aspect.PLANT, 1, opposite) == 1;
    }

    private Direction getFacing() {
        if (level == null) return Direction.DOWN;
        BlockState state = getBlockState();
        return state.hasProperty(BlockStateProperties.FACING)
                ? state.getValue(BlockStateProperties.FACING)
                : Direction.DOWN;
    }

    private void setEnabled(BlockState state, boolean enabled) {
        if (level == null) return;
        if (state.hasProperty(BlockStateProperties.ENABLED)) {
            boolean current = state.getValue(BlockStateProperties.ENABLED);
            if (current != enabled) {
                level.setBlock(worldPosition, state.setValue(BlockStateProperties.ENABLED, enabled), 3);
            }
        }
    }

    protected boolean gettingPower() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face == getFacing();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face == getFacing();
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(Aspect aspect, int amount) {
    }

    @Override
    public Aspect getSuctionType(Direction face) {
        return Aspect.PLANT;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        if (face == getFacing() && (!reserve || charges <= 0)) {
            return 128;
        }
        return 0;
    }

    @Override
    public Aspect getEssentiaType(Direction face) {
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }
}