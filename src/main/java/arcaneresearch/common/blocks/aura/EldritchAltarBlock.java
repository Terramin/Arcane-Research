package arcaneresearch.common.blocks.aura;
import arcaneresearch.common.lib.aura.EldritchOculusRitual;
import arcaneresearch.common.tiles.aura.EldritchAltarTile;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.BlockHitResult;
import arcaneresearch.common.blocks.aura.AuraBlockEntities;
import arcaneresearch.init.ModSounds;
import javax.annotation.Nullable;
public class EldritchAltarBlock extends BaseEntityBlock {
    public EldritchAltarBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(50.0F, 20000.0F)
                .sound(SoundType.STONE)
                .lightLevel(state -> 8)
                .noOcclusion()
                .noLootTable()
                .isValidSpawn((state, level, pos, type) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false));
    }
    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
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
            Block obelisk = arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_OBELISK.get();
            Block body = arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_OBELISK_BODY.get();
            Block cap = arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_CAP.get();
            for (int x = -3; x <= 3; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -3; z <= 3; z++) {
                        BlockPos p = pos.offset(x, y, z);
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
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState,
                                 net.minecraft.core.Direction side) {
        return false;
    }
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
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
            if (level.isClientSide) {
                return null;
            }
            return createTickerHelper(type, AuraBlockEntities.ELDRITCH_ALTAR.get(),
                    EldritchAltarTile::serverTick);
        } catch (Exception e) {
            return null;
        }
    }
}
