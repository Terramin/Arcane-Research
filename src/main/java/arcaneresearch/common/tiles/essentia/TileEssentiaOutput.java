package arcaneresearch.common.tiles.essentia;

import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.IEssentiaTransport;
import arcaneresearch.common.lib.events.EssentiaHandler;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nullable;

public class TileEssentiaOutput extends TileArcaneresearch implements IEssentiaTransport {

    private int tickCounter = 0;

    @Nullable
    private BlockState cachedFacingState;
    @Nullable
    private Direction cachedFacing;

    public TileEssentiaOutput(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ESSENTIA_OUTPUT.get(), pos, state);
    }

    public void serverTick() {
        if (level == null || level.isClientSide) return;
        if (++tickCounter % 5 == 0) {
            fillBuffer();
        }
    }

    private Direction getFacing() {
        BlockState state = getBlockState();
        Direction cached = cachedFacing;
        if (state != cachedFacingState || cached == null) {
            cached = state.hasProperty(BlockStateProperties.FACING)
                    ? state.getValue(BlockStateProperties.FACING)
                    : Direction.UP;
            cachedFacingState = state;
            cachedFacing = cached;
        }
        return cached;
    }

    private void fillBuffer() {
        Direction facing = getFacing();
        Direction outputSide = facing.getOpposite();
        assert level != null;
        BlockEntity be = ArcaneresearchApiHelper.getConnectableTile(level, worldPosition, outputSide);
        if (!(be instanceof IEssentiaTransport ic)) return;
        if (!ic.canInputFrom(facing)) return;
        int destSuction = ic.getSuctionAmount(facing);
        if (destSuction <= 0) return;
        Aspect destWants = ic.getSuctionType(facing);
        if (destWants == null) return;
        if (EssentiaHandler.drainEssentiaWithConfirmation(this, destWants, facing, 16, false, 5)) {
            if (ic.addEssentia(destWants, 1, facing) > 0) {
                EssentiaHandler.confirmDrain();
            }
        }
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face == getFacing().getOpposite();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return false;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face == getFacing().getOpposite();
    }

    @Override
    public void setSuction(Aspect aspect, int amount) {
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public Aspect getSuctionType(Direction loc) {
        return null;
    }

    @Override
    public int getSuctionAmount(Direction loc) {
        return 0;
    }

    @Override
    public Aspect getEssentiaType(Direction loc) {
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction loc) {
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
}