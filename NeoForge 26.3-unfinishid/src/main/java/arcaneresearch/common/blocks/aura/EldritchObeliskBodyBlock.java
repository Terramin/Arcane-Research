package arcaneresearch.common.blocks.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Тело элдритч-обелиска, порт TC4 blockEldritch meta 2 (без тайла).
 * Невидимые цельные кубы над базой (y+1..y+4): дают парящему пилону
 * твёрдый хитбокс — коллизии сущностей опрашивают только блоки,
 * пересекающие их AABB, поэтому высокий VoxelShape одной базы для
 * пролёта на высоте пилона не работает. Нерушимый, светится как в TC4.
 * Снос тела (креатив) валит базу под ним, та чистит остаток столба.
 * Лежит в arcaneresearch.arcaneresearch.common.blocks.aura.
 */
public class EldritchObeliskBodyBlock extends Block {
    public EldritchObeliskBodyBlock(BlockBehaviour.Properties props) {
        super(props
                .strength(-1.0F, 3600000.0F)
                .sound(SoundType.STONE)
                .lightLevel(state -> 8)
                .noLootTable()
                .noOcclusion());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moving) {
        super.affectNeighborsAfterRemoval(state, level, pos, moving);
        try {
            if (level.getBlockState(pos).is(this)) {
                return;
            }
            // TC4 func_149749_a (meta < 4, сюда попадает и тело meta 2):
            // все элдритч-блоки в 7x5x7 рушатся цепно + взрыв. Прежний
            // поиск базы под собой затыкался на дырке в столбе и оставлял
            // парящие body — свип убирает целиком.
            Block altar = arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_ALTAR.get();
            Block base = arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_OBELISK.get();
            Block cap = arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_CAP.get();
            for (int x = -3; x <= 3; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -3; z <= 3; z++) {
                        BlockPos p = pos.offset(x, y, z);
                        BlockState s = level.getBlockState(p);
                        if (s.getBlock() == this || s.getBlock() == base
                                || s.getBlock() == altar || s.getBlock() == cap) {
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
}
