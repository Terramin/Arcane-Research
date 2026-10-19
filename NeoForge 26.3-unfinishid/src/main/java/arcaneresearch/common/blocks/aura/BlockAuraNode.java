package arcaneresearch.common.blocks.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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

/**
 * Порт {@code thaumcraft.common.blocks.BlockAiry} (1.7.10) для нод:
 * <li>мета 0 — TileNode (обычная аура-нода): твёрдость 2.0, взрывостойкость
 * 200 (func_149712_f / getExplosionResistance);</li>
 * <li>при разрушении ноды с неё падают мудровые эссенции — для каждого
 * аспекта ноды стак по 2 эссенции, стаков = amount/10 + 1 при
 * amount >= 5 (func_149681_a, вызывается независимо от режима игры);</li>
 * <li>наполнитель (TileNodeEnergized — мета 5) — отдельный блок, дропа нет
 * (паритет условия par5 == 0).</li>
 */
public class BlockAuraNode extends BaseEntityBlock {
    private static final VoxelShape TARGET = Block.box(5.0D, 5.0D, 5.0D, 11.0D, 11.0D, 11.0D);

    public BlockAuraNode(BlockBehaviour.Properties props) {
        super(props
                .strength(2.0F, 200.0F)
                .sound(SoundType.WOOL)
                .noLootTable()
                .noOcclusion()
                // NEOFORGE_PORT: 26.x PushReaction.BLOCK was renamed to IMMOVEABLE.
                .pushReaction(PushReaction.IMMOVEABLE));
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

    // NEOFORGE_PORT: 26.x signature is propagatesSkylightDown(BlockState).
    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    // NEOFORGE_PORT: Block.onRemove(..) was removed in 26.x; the wisp-essence
    // drop (TC4 BlockAiry.func_149681_a, meta 0) now runs from
    // AuraNodeTile.preRemoveSideEffects, where the tile is still present
    // (LevelChunk.setBlockState calls preRemoveSideEffects before removing it).

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        try {
            return new AuraNodeTile(AuraBlockEntities.AURA_NODE.get(), pos, state);
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
                return createTickerHelper(type, AuraBlockEntities.AURA_NODE.get(), AuraNodeTile::clientTick);
            }
            return createTickerHelper(type, AuraBlockEntities.AURA_NODE.get(), AuraNodeTile::serverTick);
        } catch (Exception e) {

            return null;
        }
    }
}
