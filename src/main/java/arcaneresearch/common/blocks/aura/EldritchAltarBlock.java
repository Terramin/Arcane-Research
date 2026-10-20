package arcaneresearch.common.blocks.aura;
import arcaneresearch.common.lib.aura.EldritchOculusRitual;
import arcaneresearch.common.tiles.aura.EldritchAltarTile;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import arcaneresearch.common.blocks.aura.AuraBlockEntities;
import arcaneresearch.init.ModSounds;

import javax.annotation.Nullable;

/**
 * Eldritch altar block, seat of the EldritchAltarTile spawner logic.
 */
public class EldritchAltarBlock extends BaseEntityBlock {
    public EldritchAltarBlock(BlockBehaviour.Properties props) {
        super(props
                .strength(50.0F, 20000.0F)
                .sound(SoundType.STONE)
                .lightLevel(state -> 8)
                .noOcclusion()
                // TC4 BlockEldritch meta 0: func_149650_a даёт null-дроп
                // (никогда не добывается предметом), canCreatureSpawn=false,
                // func_149686_d=false (не проводит редстоун), свет проходит.
                .noLootTable()
                .isValidSpawn((state, level, pos, type) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                // NEOFORGE_PORT: 26.x isViewBlocking lambda takes (state, level, pos, nearPlaneBox).
                .isViewBlocking((state, level, pos, nearPlaneBox) -> false));
    }

    /** TC4 func_149713_g(0): светопропускание 0 — свет проходит сквозь алтарь. */
    // NEOFORGE_PORT (26.3): 1.20.1 getLightBlock(state, getter, pos) == 26.x getLightDampening(state).
    // propagatesSkylightDown stays default (false for this full-cube shape), as in the reference.
    @Override
    protected int getLightDampening(BlockState state) {
        return 0;
    }

    /**
     * TC4 BlockEldritch.func_149749_a (breakBlock, meta < 4): снос алтаря
     * цепно рушит прочие меты < 4 в радиусе 7x5x7 и заканчивается взрывом
     * (power 1, без огня). Логика 1:1 как у крышки.
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
     * TC4: meta 0 (алтарь) не рисовалась ISBRH — модель капа рисовалась
     * TESR (TileEldritchCapRenderer с obelisk_cap_altar.png). Блок невидим,
     * модель рисует EldritchAltarRenderer (+ плавающие глаза).
     */
    @Override
    public RenderShape getRenderShape(BlockState state) {
        // NEOFORGE_PORT: 26.x RenderShape only has INVISIBLE/MODEL.
        return RenderShape.INVISIBLE;
    }

    /**
     * Как у крышки: модель меньше куба, грани с соседями не отсекаем,
     * иначе рядом с блоками появляются прозрачные дыры.
     */
    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState,
                                 net.minecraft.core.Direction side) {
        return false;
    }

    /**
     * Порт TC4 BlockEldritch meta 0 + WandManager.createOculus:
     * глаз в руку — вставить глаз (до 4; с 2 глаз страж-надзиратель);
     * жезл без фокуса — ритуал окулуса (4 глаза + тёмная нода сверху
     * + готовый лабиринт + 100 каждого праймала → портал над алтарём).
     */
    @Override
    // NEOFORGE_PORT: Block.use(state, level, pos, player, hand, hit) removed in 26.x
    // -> useItemOn (uses held item/hand).

    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        try {
            ItemStack held = player.getItemInHand(hand);
            if (held.isEmpty()) {
                return InteractionResult.PASS;
            }
            BlockEntity tile = level.getBlockEntity(pos);
            if (!(tile instanceof EldritchAltarTile altar)) {
                return InteractionResult.PASS;
            }
            if (held.getItem() instanceof arcaneresearch.api.wands.IWand) {
                if (!player.isCrouching()
                        && EldritchOculusRitual.createOculus(held, player, level, pos)) {
                    return InteractionResult.CONSUME;
                }
                return InteractionResult.PASS;
            }
            if (!player.isCrouching() && held.is(arcaneresearch.init.ModItems.ELDRITCH_EYE.get())
                    && !altar.isOpen() && altar.getEyes() < 4) {
                if (altar.getEyes() >= 2) {
                    altar.setSpawner(true);
                    altar.setSpawnType((byte) 1);
                }
                altar.setEyes((byte) (altar.getEyes() + 1));
                altar.checkForMaze();
                if (!player.isCreative()) {
                    held.shrink(1);
                }
                altar.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
                level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        ModSounds.CRYSTAL.get(), SoundSource.BLOCKS, 0.2F, 1.0F);
                return InteractionResult.CONSUME;
            }
        } catch (Exception ignored) {
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        try {
            return new EldritchAltarTile(AuraBlockEntities.ELDRITCH_ALTAR.get(), pos, state);
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
                return null;
            }
            return createTickerHelper(type, AuraBlockEntities.ELDRITCH_ALTAR.get(),
                    EldritchAltarTile::serverTick);
        } catch (Exception e) {
            return null;
        }
    }
}
