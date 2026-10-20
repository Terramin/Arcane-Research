package arcaneresearch.common.blocks.devices;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.common.world.aura.AuraHandler;
public class BlockVisBattery extends Block {
    public static final IntegerProperty CHARGE = IntegerProperty.create("charge", 0, 10);
    public BlockVisBattery() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(0.5f)
                .sound(SoundType.STONE)
                .randomTicks()
                .lightLevel(state -> Math.max(state.getValue(CHARGE), 11)));
        registerDefaultState(stateDefinition.any().setValue(CHARGE, 0));
    }
    public static BlockVisBattery create() {
        return new BlockVisBattery();
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGE);
    }
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        regulate(state, level, pos, random);
    }
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        regulate(state, level, pos, random);
    }
    private void regulate(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int charge = state.getValue(CHARGE);
        if (level.hasNeighborSignal(pos)) {
            if (charge > 0) {
                AuraHandler.addVis(level, pos, 1.0f);
                level.setBlockAndUpdate(pos, state.setValue(CHARGE, charge - 1));
                level.scheduleTick(pos, this, 5);
            }
        } else {
            float aura = AuraHelper.getVis(level, pos);
            int base = AuraHelper.getAuraBase(level, pos);
            if (charge < 10 && aura > base * 0.9 && aura > 1.0f) {
                AuraHandler.drainVis(level, pos, 1.0f, false);
                level.setBlockAndUpdate(pos, state.setValue(CHARGE, charge + 1));
                level.scheduleTick(pos, this, 100 + random.nextInt(100));
            } else if (charge > 0 && aura < base * 0.75) {
                AuraHandler.addVis(level, pos, 1.0f);
                level.setBlockAndUpdate(pos, state.setValue(CHARGE, charge - 1));
                level.scheduleTick(pos, this, 20 + random.nextInt(20));
            }
        }
    }
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.hasNeighborSignal(pos)) {
            level.scheduleTick(pos, this, 1);
        }
    }
    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }
    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return state.getValue(CHARGE);
    }
}
