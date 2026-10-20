package arcaneresearch.common.blocks.aura;
import arcaneresearch.common.tiles.aura.EldritchCapTile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.common.blocks.aura.AuraBlockEntities;

import javax.annotation.Nullable;

/**
 * Крышка обелиска, порт TC4 blockEldritch meta 3 (TileEldritchCap).
 * Очень прочная (50/20000 как в TC4), светится. Тайл — маркер без тика.
 * Лежит в arcaneresearch.arcaneresearch.common.blocks.aura.
 */
public class EldritchCapBlock extends BaseEntityBlock {
    public EldritchCapBlock(BlockBehaviour.Properties props) {
        super(props
                .strength(50.0F, 20000.0F)
                .sound(SoundType.STONE)
                .lightLevel(state -> 8)
                .noLootTable()
                .noOcclusion()
                // TC4 BlockEldritch: func_149713_g(0) — светопропускание 0
                // (свет проходит сквозь кап), canCreatureSpawn=false,
                // func_149662_c/func_149686_d=false (не opaque, не
                // conducts redstone).
                .isValidSpawn((state, level, pos, type) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                // NEOFORGE_PORT: 26.x isViewBlocking lambda takes (state, level, pos, nearPlaneBox).
                .isViewBlocking((state, level, pos, nearPlaneBox) -> false));
    }

    /**
     * TC4: meta 3 не рисовалась ISBRH (BlockEldritchRenderer рисовал
     * только меты 4-10) — модель капа рисовалась TESR
     * (TileEldritchCapRenderer). Так же и здесь: блок невидим, модель
     * obelisk_cap.obj рисует EldritchCapRenderer.
     */
    @Override
    public RenderShape getRenderShape(BlockState state) {
        // NEOFORGE_PORT: 26.x RenderShape only has INVISIBLE/MODEL; the cap is drawn by EldritchCapRenderer.
        return RenderShape.INVISIBLE;
    }

    /** TC4 func_149713_g(0): светопропускание 0 — свет проходит сквозь кап. */
    // NEOFORGE_PORT (26.3): 1.20.1 getLightBlock(state, getter, pos) == 26.x getLightDampening(state).
    // propagatesSkylightDown stays default (false for this full-cube shape), as in the reference.
    @Override
    protected int getLightDampening(BlockState state) {
        return 0;
    }

    /**
     * TC4 BlockEldritch.func_149749_a (breakBlock, meta < 4): снос любого
     * из алтарь/база/тело/кап в радиусе 7x5x7 цепно рушит прочие меты < 4
     * и заканчивается взрывом (power 1, без огня). Сюда попадает и кап.
     */
    @Override
    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moving) {
        super.affectNeighborsAfterRemoval(state, level, pos, moving);
        try {
            if (level.getBlockState(pos).is(state.getBlock())) {
                return;
            }
            Block altar = arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_ALTAR.get();
            Block obelisk = arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_OBELISK.get();
            Block body = arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_OBELISK_BODY.get();
            Block cap = arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_CAP.get();
            for (int x = -3; x <= 3; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -3; z <= 3; z++) {
                        net.minecraft.core.BlockPos p = pos.offset(x, y, z);
                        BlockState s = level.getBlockState(p);
                        if (s.getBlock() == altar || s.getBlock() == obelisk
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

    /**
     * Крышка меньше куба: грани с соседями не отсекаем, иначе рядом
     * с блоками в модели появляются прозрачные дыры. Как TC4
     * (BlockEldritch не был opaque-кубом).
     */
    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction side) {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        try {
            return new EldritchCapTile(AuraBlockEntities.ELDRITCH_CAP.get(), pos, state);
        } catch (Exception e) {
            return null;
        }
    }
}
