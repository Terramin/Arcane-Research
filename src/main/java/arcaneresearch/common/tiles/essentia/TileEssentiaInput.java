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

public class TileEssentiaInput extends TileArcaneresearch implements IEssentiaTransport {

    private static final int SUCTION = 128;

    private int tickCounter = 0;

    @Nullable
    private BlockState cachedFacingState;
    @Nullable
    private Direction cachedFacing;

    public TileEssentiaInput(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ESSENTIA_INPUT.get(), pos, state);
    }

    public void serverTick() {
        if (level == null || level.isClientSide) return;
        if (++tickCounter % 5 == 0) {
            fillJar();
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

    private void fillJar() {
        Direction facing = getFacing();
        Direction inputSide = facing.getOpposite();
        assert level != null;
        BlockEntity be = ArcaneresearchApiHelper.getConnectableTile(level, worldPosition, inputSide);
        if (!(be instanceof IEssentiaTransport ic)) return;
        if (!ic.canOutputTo(facing)) return;
        if (ic.getEssentiaAmount(facing) <= 0) return;
        int ourSuction = SUCTION;
        if (ic.getSuctionAmount(facing) >= ourSuction) return;
        if (ourSuction < ic.getMinimumSuction()) return;
        Aspect ta = ic.getEssentiaType(facing);
        if (ta == null) return;
        if (EssentiaHandler.addEssentia(this, ta, facing, 16, false, 5)) {
            ic.takeEssentia(ta, 1, facing);
        }
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face == getFacing().getOpposite();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face == getFacing().getOpposite();
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
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
        return SUCTION;
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
        return canInputFrom(face) ? amount : 0;
    }
}