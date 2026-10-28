package arcaneresearch.discovered.table;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

 












 
public class HexNoteTableBlock extends Block implements EntityBlock {

    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

      
    private static VoxelShape shapeFor(Part part, Direction facing) {
        Direction eff = part == Part.SIDE ? facing.getOpposite() : facing;
        double[][] boxes = part == Part.MAIN
                ? new double[][]{
                        {0, 12, 0, 16, 16, 16},
                        {2, 0, 2, 6, 12, 6},
                        {2, 0, 10, 6, 12, 14},
                        {4, 2, 6, 16, 6, 10}}
                : new double[][]{
                        {0, 12, 0, 16, 16, 16},
                        {10, 0, 2, 14, 12, 6},
                        {10, 0, 10, 14, 12, 14},
                        {0, 2, 6, 12, 6, 10}};
        VoxelShape shape = Shapes.empty();
        for (double[] b : boxes) {
            shape = Shapes.or(shape, rotBox(b, eff));
        }
        return shape;
    }

    private static VoxelShape rotBox(double[] b, Direction facing) {
        double x1 = b[0], y1 = b[1], z1 = b[2], x2 = b[3], y2 = b[4], z2 = b[5];
        return switch (facing) {
            case EAST -> Block.box(x1, y1, z1, x2, y2, z2);
            case SOUTH -> Block.box(16.0 - z2, y1, x1, 16.0 - z1, y2, x2);
            case WEST -> Block.box(16.0 - x2, y1, 16.0 - z2, 16.0 - x1, y2, 16.0 - z1);
            case NORTH -> Block.box(z1, y1, 16.0 - x2, z2, y2, 16.0 - x1);
            default -> Block.box(x1, y1, z1, x2, y2, z2);
        };
    }

    public HexNoteTableBlock(BlockBehaviour.Properties props) {
        super(props
                .mapColor(MapColor.WOOD)
                .strength(2.0F)
                .sound(SoundType.WOOD)
                .noOcclusion());
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, Part.MAIN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

      
    public static BlockPos mainPos(BlockPos pos, BlockState state) {
        return state.getValue(PART) == Part.MAIN ? pos : pos.relative(state.getValue(FACING));
    }

    private static BlockPos partnerPos(BlockPos pos, BlockState state) {
        return pos.relative(state.getValue(FACING));
    }

    private static boolean isPartner(BlockState state, BlockState neighbour) {
        if (!(neighbour.getBlock() instanceof HexNoteTableBlock)) {
            return false;
        }
        return neighbour.getValue(PART) != state.getValue(PART)
                && neighbour.getValue(FACING) == state.getValue(FACING).getOpposite();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState neighbour = level.getBlockState(partnerPos(pos, state));
        return isPartner(state, neighbour);
    }

    
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level,
            net.minecraft.world.level.ScheduledTickAccess ticks, BlockPos pos, Direction direction,
            BlockPos neighbourPos, BlockState neighbourState, net.minecraft.util.RandomSource random) {
        if (direction == state.getValue(FACING) && !isPartner(state, neighbourState)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            Block single = arcaneresearch.discovered.table.DiscoveredTableSetup.getSingleTable();
            if (single != null) {
                BlockPos partner = partnerPos(pos, state);
                if (level.getBlockState(partner).getBlock() instanceof HexNoteTableBlock) {
                    level.setBlock(partner, single.defaultBlockState(), 3);
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state.getValue(PART), state.getValue(FACING));
    }

     



 
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos,
            net.minecraft.util.RandomSource random) {
        if (state.getValue(PART) != Part.MAIN) {
            return;
        }
        if (random.nextInt(5) == 0) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof HexNoteTableTile tile && level.isClientSide()
                    && !tile.getItem(HexNoteTableTile.NOTE_SLOT).isEmpty()) {
                
                arcaneresearch.discovered.client.HexTableClientFX.researchSparkle(level, pos, random);
            }
        }
    }

    @Override
    
    
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockPos main = mainPos(pos, state);
        BlockEntity be = level.getBlockEntity(main);
        if (be instanceof HexNoteTableTile tile && player instanceof ServerPlayer serverPlayer) {
            
            
            serverPlayer.openMenu(tile, buf -> buf.writeBlockPos(main));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level,
            BlockPos pos, boolean isMoving) {
        
        
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
        if (!state.is(level.getBlockState(pos).getBlock())) {
            if (state.getValue(PART) == Part.MAIN
                    && level.getBlockEntity(pos) instanceof HexNoteTableTile tile) {
                tile.dropContents();
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == Part.MAIN ? new HexNoteTableTile(pos, state) : null;
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide() || state.getValue(PART) != Part.MAIN) {
            return null;
        }
        return (BlockEntityTicker<T>) (BlockEntityTicker<HexNoteTableTile>) HexNoteTableTile::tick;
    }

    public enum Part implements StringRepresentable {
        MAIN("main"),
        SIDE("side");

        private final String name;

        Part(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
