package arcaneresearch.common.blocks.aura;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
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
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.items.resources.ItemWispEssence;
import arcaneresearch.common.tiles.aura.AuraNodeTile;
import arcaneresearch.init.ModItems;
import javax.annotation.Nullable;
public class BlockAuraNode extends BaseEntityBlock {
    private static final VoxelShape TARGET = Block.box(5.0D, 5.0D, 5.0D, 11.0D, 11.0D, 11.0D);
    public BlockAuraNode() {
        super(BlockBehaviour.Properties.of()
                .strength(2.0F, 200.0F)
                .sound(SoundType.WOOL)
                .noLootTable()
                .noOcclusion()
                .pushReaction(PushReaction.BLOCK));
    }
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return TARGET;
    }
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }
    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }
    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        try {
            return new AuraNodeTile(AuraBlockEntities.AURA_NODE.get(), pos, state);
        } catch (Exception e) {
            return null;
        }
    }
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !newState.is(state.getBlock())) {
            BlockEntity te = level.getBlockEntity(pos);
            if (te instanceof AuraNodeTile node) {
                AspectList aspects = node.getAspects();
                if (aspects != null && aspects.size() > 0) {
                    for (Aspect aspect : aspects.getAspects()) {
                        int amount = aspects.getAmount(aspect);
                        if (amount >= 5) {
                            int stacks = amount / 10 + 1;
                            for (int a = 0; a < stacks; a++) {
                                ItemStack ess = ItemWispEssence.createStack(
                                        ModItems.WISP_ESSENCE.get(), aspect, 1);
                                ItemEntity entity = new ItemEntity(level,
                                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ess);
                                level.addFreshEntity(entity);
                            }
                        }
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        try {
            if (level.isClientSide) {
                return createTickerHelper(type, AuraBlockEntities.AURA_NODE.get(), AuraNodeTile::clientTick);
            }
            return createTickerHelper(type, AuraBlockEntities.AURA_NODE.get(), AuraNodeTile::serverTick);
        } catch (Exception e) {
            return null;
        }
    }
}
