package arcaneresearch.common.blocks.misc;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.common.tiles.misc.TileNitor;
import arcaneresearch.init.ModBlockEntities;
import javax.annotation.Nullable;
public class BlockNitor extends Block implements EntityBlock, SimpleWaterloggedBlock {
    /** Whether the block is submerged, like slabs and stairs. */
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape SHAPE = Shapes.create(
            arcaneresearch.common.tiles.misc.NitorRules.SHAPE_MIN,
            arcaneresearch.common.tiles.misc.NitorRules.SHAPE_MIN,
            arcaneresearch.common.tiles.misc.NitorRules.SHAPE_MIN,
            arcaneresearch.common.tiles.misc.NitorRules.SHAPE_MAX,
            arcaneresearch.common.tiles.misc.NitorRules.SHAPE_MAX,
            arcaneresearch.common.tiles.misc.NitorRules.SHAPE_MAX);
    private final DyeColor color;
    public BlockNitor(DyeColor color) {
        super(BlockBehaviour.Properties.of()
                .mapColor(getMapColorForDye(color))
                .strength(0.1f)
                .sound(SoundType.WOOL)
                .lightLevel(state -> 15)
                .noOcclusion()
                .noCollission());
        this.color = color;
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean submerged = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(WATERLOGGED, submerged);
    }
    @Override
    public net.minecraft.world.level.material.FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
    private static MapColor getMapColorForDye(DyeColor dye) {
        return switch (dye) {
            case WHITE -> MapColor.SNOW;
            case ORANGE -> MapColor.COLOR_ORANGE;
            case MAGENTA -> MapColor.COLOR_MAGENTA;
            case LIGHT_BLUE -> MapColor.COLOR_LIGHT_BLUE;
            case YELLOW -> MapColor.COLOR_YELLOW;
            case LIME -> MapColor.COLOR_LIGHT_GREEN;
            case PINK -> MapColor.COLOR_PINK;
            case GRAY -> MapColor.COLOR_GRAY;
            case LIGHT_GRAY -> MapColor.COLOR_LIGHT_GRAY;
            case CYAN -> MapColor.COLOR_CYAN;
            case PURPLE -> MapColor.COLOR_PURPLE;
            case BLUE -> MapColor.COLOR_BLUE;
            case BROWN -> MapColor.COLOR_BROWN;
            case GREEN -> MapColor.COLOR_GREEN;
            case RED -> MapColor.COLOR_RED;
            case BLACK -> MapColor.COLOR_BLACK;
        };
    }
    public static BlockNitor create(DyeColor color) {
        return new BlockNitor(color);
    }
    public DyeColor getColor() {
        return color;
    }
    public int getColorValue() {
        return getMapColorForDye(color).col;
    }
    /**
     * Flowing water and lava must not wash the nitor away. Placement *into* water
     * is unaffected because that check is made against the water block's state.
     */
    @Override
    public boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileNitor(pos, state);
    }
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide || type != ModBlockEntities.NITOR.get()) {
            return null;
        }
        return (lvl, pos, st, te) -> TileNitor.clientTick(lvl, pos, st, (TileNitor) te);
    }
}
