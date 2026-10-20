package arcaneresearch.common.blocks.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.common.items.aura.ItemJarNode;
import arcaneresearch.common.tiles.aura.TileJarNode;

import javax.annotation.Nullable;

public class BlockJarNode extends BaseEntityBlock {

    private static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 12.0D, 13.0D);

    public BlockJarNode(BlockBehaviour.Properties props) {
        super(props
                .strength(0.5F)
                .sound(SoundType.GLASS)
                .lightLevel(state -> 11)
                .noOcclusion());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        try {
            return new TileJarNode(AuraBlockEntities.JAR_NODE.get(), pos, state);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        try {
            if (!level.isClientSide()) {
                return createTickerHelper(type, AuraBlockEntities.JAR_NODE.get(), TileJarNode::serverTick);
            }
            return null;
        } catch (Exception e) {

            return null;
        }
    }

    @Override
    public java.util.List<ItemStack> getDrops(BlockState state,
                                              net.minecraft.world.level.storage.loot.LootParams.Builder params) {
        try {
            BlockEntity tile = params.getOptionalParameter(
                    net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY);
            if (tile instanceof TileJarNode jar) {
                ItemStack stack = ItemJarNode.createStack(jar);
                if (!stack.isEmpty()) {
                    return java.util.List.of(stack);
                }
            }
        } catch (Exception ignored) {
        }
        return super.getDrops(state, params);
    }

    @Override
    public ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        // NEOFORGE_PORT (26.3): no includeData guard - the 1.20.1 override always returned the
        // filled jar (plain pick block, scanning and thaumometer all saw the node jar).
        try {
            if (level.getBlockEntity(pos) instanceof TileJarNode jar) {
                ItemStack stack = ItemJarNode.createStack(jar);
                if (!stack.isEmpty()) {
                    return stack;
                }
            }
        } catch (Exception ignored) {
        }
        return super.getCloneItemStack(level, pos, state, includeData);
    }
}
