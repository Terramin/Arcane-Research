package arcaneresearch.common.blocks.aura;
import arcaneresearch.client.renderers.tile.EldritchObeliskRenderer;
import arcaneresearch.common.tiles.aura.EldritchObeliskTile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.server.level.ServerLevel;
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

/**
 * Элдритч-обелиск, порт TC4 blockEldritch meta 1 (TileEldritchObelisk).
 * База — невидимый полный куб (как meta 1 в TC4: BlockEldritchRenderer
 * ничего не рисует для мет 0-3), парящий пилон 1x1x3 + 2 капа + 4
 * портальные стены рисует EldritchObeliskRenderer. Нерушимый, светится.
 * Хитбокс — цельная колонна 1x1x5: в TC4 столб genObelisk состоял из базы
 * + 4 невидимых блоков тела (meta 2) и был сплошным.
 */
public class EldritchObeliskBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE =
            Shapes.create(new AABB(0.0D, 0.0D, 0.0D, 1.0D, 5.0D, 1.0D));

    public EldritchObeliskBlock(BlockBehaviour.Properties props) {
        super(props
                .strength(-1.0F, 3600000.0F)
                .sound(SoundType.STONE)
                .lightLevel(state -> 8)
                .noLootTable()
                .noOcclusion());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // NEOFORGE_PORT: 26.x RenderShape only has INVISIBLE/MODEL.
        return RenderShape.INVISIBLE;
    }

    /**
     * TC4 genObelisk: над базой всегда 4 тела столба. Достраиваем только
     * в воздух — чужие блоки не трогаем. Срабатывает и от руки игрока,
     * и от setBlock (ритуалы/структуры в загруженных чанках).
     */
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        if (level.isClientSide()) {
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

    /**
     * TC4 BlockEldritch.func_149749_a (breakBlock, meta < 4): снос любого
     * элдритч-блока (меты < 4: 0 алтарь/1 база/2 тело/3 кап) в радиусе
     * 7x5x7 цепно рушит прочие и заканчивается взрывом (power 1, без
     * огня). Прежний «снос только своего столба до первой дырки»
     * оставлял парящие body над сломанным телом — теперь столб убирается
     * целиком при любом месте сноса (зазоры не важны).
     */
    @Override
    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moving) {
        super.affectNeighborsAfterRemoval(state, level, pos, moving);
        try {
            if (level.getBlockState(pos).is(state.getBlock())) {
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
            if (level.isClientSide()) {
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
