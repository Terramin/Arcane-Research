package arcaneresearch.common.blocks.crafting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import arcaneresearch.common.tiles.crafting.ThaumatoriumRules;
import arcaneresearch.common.tiles.crafting.TileThaumatorium;
import arcaneresearch.common.tiles.crafting.TileThaumatoriumTop;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModBlocks;
import javax.annotation.Nullable;
/**
 * Upper half of the alchemy furnace. It has no block entity of its own: all state
 * lives in the furnace below, and using it opens that furnace's menu.
 */
public class BlockThaumatoriumTop extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 10.0, 12.0);
    public BlockThaumatoriumTop() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0f)
                .sound(SoundType.METAL)
                .noOcclusion()
                .noLootTable()
                .requiresCorrectToolForDrops());
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (player.isCrouching()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity below = level.getBlockEntity(pos.below());
        if (below instanceof TileThaumatorium thaumatorium && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, thaumatorium, pos.below());
        }
        return InteractionResult.CONSUME;
    }
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (ThaumatoriumRules.shouldDropContents(isMoving, state.is(newState.getBlock()))) {
            boolean brassConversion = newState.is(ModBlocks.ALCHEMICAL_BRASS_BLOCK.get());

            // The catalyst lives in the furnace below; drop it even on a brass conversion.
            BlockEntity belowEntity = level.getBlockEntity(pos.below());
            if (belowEntity instanceof TileThaumatorium thaumatorium) {
                for (int i = 0; i < thaumatorium.getContainerSize(); i++) {
                    net.minecraft.world.item.ItemStack stack = thaumatorium.getItem(i);
                    if (!stack.isEmpty()) {
                        net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                        thaumatorium.setItem(i, net.minecraft.world.item.ItemStack.EMPTY);
                    }
                }
            }

            // The brass item is dropped only for a real break, never for the chained
            // conversion of the partner block.
            if (ThaumatoriumRules.shouldDropBlockItem(brassConversion)) {
                BlockState below = level.getBlockState(pos.below());
                if (below.is(ModBlocks.THAUMATORIUM.get())) {
                    level.setBlock(pos.below(), ModBlocks.ALCHEMICAL_BRASS_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);
                }
                net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(),
                        new net.minecraft.world.item.ItemStack(ModBlocks.ALCHEMICAL_BRASS_BLOCK.get()));
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileThaumatoriumTop(pos, state);
    }
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.THAUMATORIUM_TOP.get()) {
            if (!level.isClientSide) {
                return (lvl, pos, st, be) -> TileThaumatoriumTop.serverTick(lvl, pos, st, (TileThaumatoriumTop) be);
            }
        }
        return null;
    }
}
