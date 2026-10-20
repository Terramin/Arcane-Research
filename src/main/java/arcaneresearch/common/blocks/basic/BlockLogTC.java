package arcaneresearch.common.blocks.basic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
public class BlockLogTC extends RotatedPillarBlock {
    public BlockLogTC(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(AXIS, Direction.Axis.Y));
    }
    public static BlockLogTC createGreatwood() {
        return new BlockLogTC(
                Properties.of()
                        .mapColor(state -> state.getValue(AXIS) == Direction.Axis.Y ?
                                MapColor.COLOR_BROWN : MapColor.PODZOL)
                        .strength(2.0f, 5.0f)
                        .sound(SoundType.WOOD)
        );
    }
    public static BlockLogTC createSilverwood() {
        return new BlockLogTC(
                Properties.of()
                        .mapColor(state -> state.getValue(AXIS) == Direction.Axis.Y ?
                                MapColor.QUARTZ : MapColor.WOOL)
                        .strength(2.0f, 5.0f)
                        .sound(SoundType.WOOD)
                        .lightLevel(state -> 5)
        );
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis());
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }
    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }
    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            int radius = 4;
            int checkRadius = radius + 1;
            if (level.isAreaLoaded(pos, checkRadius)) {
                for (BlockPos checkPos : BlockPos.betweenClosed(
                        pos.offset(-radius, -radius, -radius),
                        pos.offset(radius, radius, radius))) {
                    BlockState checkState = level.getBlockState(checkPos);
                    if (checkState.is(BlockTags.LEAVES)) {
                        level.scheduleTick(checkPos.immutable(), checkState.getBlock(), 1);
                    }
                }
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }
}
