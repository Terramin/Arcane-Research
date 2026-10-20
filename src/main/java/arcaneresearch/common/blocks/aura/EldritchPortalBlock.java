package arcaneresearch.common.blocks.aura;
import arcaneresearch.common.tiles.aura.EldritchPortalTile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.common.blocks.aura.AuraBlockEntities;
import arcaneresearch.init.ModBlocks;

import javax.annotation.Nullable;

/**
 * Eldritch portal block, ported from TC4 BlockEldritchPortal.
 * Airy, invisible, glowing and unbreakable; breaks when the eldritch
 * frame above or below it goes missing. The swirl itself is drawn by
 * EldritchPortalRenderer from the tile's opencount.
 */
public class EldritchPortalBlock extends BaseEntityBlock {
    public EldritchPortalBlock(BlockBehaviour.Properties props) {
        super(props
                .strength(-1.0F, 3600000.0F)
                .sound(SoundType.GLASS)
                .lightLevel(state -> 15)
                .noLootTable()
                .noCollision());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block block,
                                net.minecraft.world.level.redstone.Orientation orientation, boolean moved) {
        super.neighborChanged(state, level, pos, block, orientation, moved);
        // Рама TC4 (любой eldritch-камень сверху/снизу): плитка, крышка, обелиск.
        if (!isFrame(level.getBlockState(pos.above()))
                || !isFrame(level.getBlockState(pos.below()))) {
            level.removeBlock(pos, false);
        }
    }

    public static boolean isFrame(BlockState state) {
        try {
            if (state.is(ModBlocks.ELDRITCH_STONE_TILE.get())) {
                return true;
            }
            // TC4 BlockEldritchPortal: рамой считался ЛЮБОЙ blockEldritch сверху
            // и снизу — включая сам алтарь (под порталом после ритуала) и
            // столб eldritch_pillar над ним в генерируемой структуре.
            if (state.is(arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_ALTAR.get())) {
                return true;
            }
            if (state.is(ModBlocks.ELDRITCH_PILLAR.get())) {
                return true;
            }
            if (state.is(arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_CAP.get())) {
                return true;
            }
            return state.is(arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_OBELISK.get());
        } catch (Exception e) {
            return false;
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        try {
            return new EldritchPortalTile(AuraBlockEntities.ELDRITCH_PORTAL.get(), pos, state);
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
            if (level.isClientSide()) {
                return createTickerHelper(type, AuraBlockEntities.ELDRITCH_PORTAL.get(),
                        EldritchPortalTile::clientTick);
            }
            return createTickerHelper(type, AuraBlockEntities.ELDRITCH_PORTAL.get(),
                    EldritchPortalTile::serverTick);
        } catch (Exception e) {
            return null;
        }
    }
}
