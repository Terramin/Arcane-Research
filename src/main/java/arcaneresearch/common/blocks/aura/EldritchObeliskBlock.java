package arcaneresearch.common.blocks.aura;
import arcaneresearch.common.tiles.aura.EldritchObeliskTile;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.common.blocks.aura.AuraBlockEntities;
import javax.annotation.Nullable;
public class EldritchObeliskBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE =
            Shapes.create(new AABB(0.0D, 0.0D, 0.0D, 1.0D, 5.0D, 1.0D));
    public EldritchObeliskBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(-1.0F, 3600000.0F)
                .sound(SoundType.STONE)
                .lightLevel(state -> 8)
                .noLootTable()
                .noOcclusion());
    }
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        if (level.isClientSide) {
            return;
        }
        try {
            BlockState body = arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_OBELISK_BODY.get()
                    .defaultBlockState();
            for (int i = 1; i <= 4; i++) {
                BlockPos above = pos.above(i);
                if (level.getBlockState(above).isAir()) {
                    level.setBlock(above, body, 3);
                }
            }
        } catch (Exception ignored) {
        }
    }
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        super.onRemove(state, level, pos, newState, moving);
        if (level.isClientSide) {
            return;
        }
        try {
            if (newState.is(state.getBlock())) {
                return;
            }
            Block altar = arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_ALTAR.get();
            Block body = arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_OBELISK_BODY.get();
            Block cap = arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_CAP.get();
            for (int x = -3; x <= 3; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -3; z <= 3; z++) {
                        BlockPos p = pos.offset(x, y, z);
                        BlockState s = level.getBlockState(p);
                        if (s.getBlock() == this || s.getBlock() == altar
                                || s.getBlock() == body || s.getBlock() == cap) {
                            level.removeBlock(p, false);
                        }
                    }
                }
            }
            level.explode(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 1.0F,
                    Level.ExplosionInteraction.NONE);
        } catch (Exception ignored) {
        }
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }
    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SHAPE;
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        try {
            return new EldritchObeliskTile(AuraBlockEntities.ELDRITCH_OBELISK.get(), pos, state);
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
            if (level.isClientSide) {
                return createTickerHelper(type, AuraBlockEntities.ELDRITCH_OBELISK.get(),
                        EldritchObeliskTile::clientTick);
            }
            return createTickerHelper(type, AuraBlockEntities.ELDRITCH_OBELISK.get(),
                    EldritchObeliskTile::serverTick);
        } catch (Exception e) {
            return null;
        }
    }
}
