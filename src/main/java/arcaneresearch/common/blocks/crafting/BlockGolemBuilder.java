package arcaneresearch.common.blocks.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.common.blocks.BlockTCDevice;
import arcaneresearch.common.tiles.crafting.TileGolemBuilder;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModBlocks;

import javax.annotation.Nullable;

public class BlockGolemBuilder extends BlockTCDevice {

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);
    public static boolean ignore = false;

    public BlockGolemBuilder(net.minecraft.world.level.block.state.BlockBehaviour.Properties props) {
        super(props
                .strength(2.0f, 6.0f)
                .sound(SoundType.STONE)
                .noOcclusion());
    }

    public static void destroyPress(Level level, BlockPos pos, BlockPos startpos) {
        if (ignore || level.isClientSide()) {
            return;
        }
        ignore = true;
        for (int a = -1; a <= 1; ++a) {
            for (int b = 0; b <= 1; ++b) {
                for (int c = -1; c <= 1; ++c) {
                    if (!pos.offset(a, b, c).equals(startpos)) {
                        BlockState bs = level.getBlockState(pos.offset(a, b, c));
                        if (bs.is(ModBlocks.PLACEHOLDER_BARS.get())) {
                            level.setBlock(pos.offset(a, b, c), Blocks.IRON_BARS.defaultBlockState(), 3);
                        }
                        if (bs.is(ModBlocks.PLACEHOLDER_ANVIL.get())) {
                            level.setBlock(pos.offset(a, b, c), Blocks.ANVIL.defaultBlockState(), 3);
                        }
                        if (bs.is(ModBlocks.PLACEHOLDER_CAULDRON.get())) {
                            level.setBlock(pos.offset(a, b, c), Blocks.CAULDRON.defaultBlockState(), 3);
                        }
                        if (bs.is(ModBlocks.PLACEHOLDER_TABLE.get())) {
                            level.setBlock(pos.offset(a, b, c), ModBlocks.TABLE_STONE.get().defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
        if (!pos.equals(startpos)) {
            level.setBlock(pos, Blocks.PISTON.defaultBlockState()
                    .setValue(DirectionalBlock.FACING, Direction.UP), 3);
        }
        ignore = false;
    }

    @Override
    protected boolean hasFacing() {

        return true;
    }

    @Override
    protected boolean hasEnabled() {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileGolemBuilder(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return createTickerHelper(type, ModBlockEntities.GOLEM_BUILDER.get(), TileGolemBuilder::clientTick);
        } else {
            return createTickerHelper(type, ModBlockEntities.GOLEM_BUILDER.get(), TileGolemBuilder::serverTick);
        }
    }

    @Override
    
    

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileGolemBuilder golemBuilder) {
            ((ServerPlayer) player).openMenu(golemBuilder, buf -> buf.writeBlockPos(pos));
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        
        
        
        
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
        if (!state.is(level.getBlockState(pos).getBlock())) {
            destroyPress(level, pos, pos);
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileGolemBuilder golemBuilder) {
                golemBuilder.dropContents();
            }
        }

    }
}
