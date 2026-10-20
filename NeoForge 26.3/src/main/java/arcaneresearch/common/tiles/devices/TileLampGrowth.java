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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileLampGrowth extends TileArcaneresearch implements IEssentiaTransport {

    private static final int RANGE = 6;
    private static final int MAX_CHARGES = 20;
    public int charges = -1;
    private boolean reserve = false;
    private int lastX, lastY, lastZ;
    private BlockState lastState;

    private List<BlockPos> checklist = new ArrayList<>();
    private int drawDelay = 0;

    public TileLampGrowth(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileLampGrowth(BlockPos pos, BlockState state) {
        this(ModBlockEntities.LAMP_GROWTH.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileLampGrowth tile) {

        if (tile.charges <= 0) {
            if (tile.reserve) {
                tile.charges = MAX_CHARGES;
                tile.reserve = false;
                tile.setChanged();
                tile.syncTile(true);
            } else if (tile.drawEssentia()) {
                tile.charges = MAX_CHARGES;
                tile.setChanged();
                tile.syncTile(true);
            }

            if (tile.charges <= 0) {
                tile.setEnabled(state, false);
            } else if (!tile.gettingPower()) {
                tile.setEnabled(state, true);
            }
        }

        if (!tile.reserve && tile.drawEssentia()) {
            tile.reserve = true;
        }

        if (tile.charges == 0) {
            tile.charges = -1;
            tile.syncTile(true);
        }

        if (!tile.gettingPower() && tile.charges > 0) {
            tile.updatePlant();
        }
    }

    @Override
    protected void writeSyncNBT(ValueOutput tag) {
        super.writeSyncNBT(tag);

        tag.putBoolean("reserve", reserve);
        tag.putInt("charges", charges);
    }

    @Override
    protected void readSyncNBT(ValueInput tag) {
        super.readSyncNBT(tag);
        reserve = hasBoolean(tag, "reserve") ? tag.getBooleanOr("reserve", false) : tag.getBooleanOr("Reserve", false);
        charges = tag.getIntOr("charges", tag.getIntOr("Charges", 0));
    }

    boolean isPlant(BlockPos bp) {

        BlockState state = level.getBlockState(bp);
        Block block = state.getBlock();
        if (block instanceof BonemealableBlock
                && !(block instanceof net.minecraft.world.level.block.TallGrassBlock)
                && !(block instanceof net.minecraft.world.level.block.DoublePlantBlock)) {
            return true;
        }
        return block instanceof net.minecraft.world.level.block.CactusBlock
                || block instanceof net.minecraft.world.level.block.SugarCaneBlock;
    }

    private void updatePlant() {
        if (level == null || !(level instanceof ServerLevel serverLevel)) return;

        BlockState tracked = level.getBlockState(new BlockPos(lastX, lastY, lastZ));
        if (!tracked.equals(lastState)) {
            arcaneresearch.common.lib.network.PacketHandler.sendToAllTrackingChunk(
                    new arcaneresearch.common.lib.network.fx.PacketFXBlockMist(
                            new BlockPos(lastX, lastY, lastZ), 4259648),
                    serverLevel, new BlockPos(lastX, lastY, lastZ));
            lastState = tracked;
        }

        if (checklist.isEmpty()) {
            for (int x = -RANGE; x <= RANGE; x++) {
                for (int z = -RANGE; z <= RANGE; z++) {
                    checklist.add(worldPosition.offset(x, RANGE, z));
                }
            }

            for (int i = checklist.size() - 1; i > 0; i--) {
                int j = level.getRandom().nextInt(i + 1);
                BlockPos temp = checklist.get(i);
                checklist.set(i, checklist.get(j));
                checklist.set(j, temp);
            }
        }

        if (checklist.isEmpty()) return;
        BlockPos startPos = checklist.remove(0);
        int x = startPos.getX();
        int z = startPos.getZ();

        for (int y = startPos.getY(); y >= worldPosition.getY() - RANGE; y--) {
            BlockPos checkPos = new BlockPos(x, y, z);

            if (level.isEmptyBlock(checkPos)) continue;
            if (!isPlant(checkPos)) continue;
            if (checkPos.distSqr(worldPosition) > RANGE * RANGE) continue;
            if (arcaneresearch.common.lib.utils.CropUtils.isGrownCrop(level, checkPos)) continue;
            if (!arcaneresearch.common.lib.utils.CropUtils.doesLampGrow(level, checkPos)) continue;

            charges--;
            lastX = x;
            lastY = y;
            lastZ = z;
            BlockState blockState = level.getBlockState(checkPos);
            lastState = blockState;
            serverLevel.scheduleTick(checkPos, lastState.getBlock(), 1);
            return;
        }
    }

    private boolean drawEssentia() {
        if (++drawDelay % 5 != 0) return false;
        if (level == null) return false;

        Direction facing = getFacing();
        BlockEntity te = ArcaneresearchApiHelper.getConnectableTile(level, worldPosition, facing);

        if (te instanceof IEssentiaTransport transport) {
            Direction opposite = facing.getOpposite();

            if (!transport.canOutputTo(opposite)) return false;

            if (transport.getSuctionAmount(opposite) < getSuctionAmount(facing) &&
                    transport.takeEssentia(Aspect.PLANT, 1, opposite) == 1) {
                return true;
            }
        }

        return false;
    }

    private Direction getFacing() {
        BlockState state = getBlockState();
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.getValue(BlockStateProperties.FACING);
        }
        return Direction.DOWN;
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
