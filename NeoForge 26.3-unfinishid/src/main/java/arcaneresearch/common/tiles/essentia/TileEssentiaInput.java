package arcaneresearch.common.tiles.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.IEssentiaTransport;
import arcaneresearch.common.lib.events.EssentiaHandler;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;

public class TileEssentiaInput extends TileArcaneresearch implements IEssentiaTransport {

    private int tickCounter = 0;

    public TileEssentiaInput(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ESSENTIA_INPUT.get(), pos, state);
    }

    public void serverTick() {
        if (level == null || level.isClientSide()) return;

        if (++tickCounter % 5 == 0) {
            fillJar();
        }
    }

    private Direction getFacing() {
        BlockState state = getBlockState();
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.getValue(BlockStateProperties.FACING);
        }
        return Direction.UP;
    }

    private void fillJar() {
        Direction facing = getFacing();
        Direction inputSide = facing.getOpposite();

        BlockEntity te = ArcaneresearchApiHelper.getConnectableTile(level, worldPosition, inputSide);
        if (te instanceof IEssentiaTransport ic) {

            if (!ic.canOutputTo(facing)) {
                return;
            }

            if (ic.getEssentiaAmount(facing) > 0
                    && ic.getSuctionAmount(facing) < getSuctionAmount(inputSide)
                    && getSuctionAmount(inputSide) >= ic.getMinimumSuction()) {

                Aspect ta = ic.getEssentiaType(facing);
                if (ta != null) {

                    if (EssentiaHandler.addEssentia(this, ta, facing, 16, false, 5)) {

                        ic.takeEssentia(ta, 1, facing);
                    }
                }
            }
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

        return 128;
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

        return amount;
    }
}
