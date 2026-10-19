package arcaneresearch.common.blocks.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.common.menu.SpaMenu;
import arcaneresearch.common.tiles.devices.TileSpa;
import arcaneresearch.init.ModBlockEntities;

import javax.annotation.Nullable;

public class BlockSpa extends Block implements EntityBlock {

    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0);

    public BlockSpa(BlockBehaviour.Properties props) {
        super(props
                .mapColor(MapColor.COLOR_LIGHT_BLUE)
                .strength(2.0f)
                .sound(SoundType.STONE)
                .noOcclusion());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    // NEOFORGE_PORT: Block.use(state, level, pos, player, hand, hit) removed in 26.x
    // -> useItemOn (uses held item/hand).

    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof TileSpa spa) {
            // NEOFORGE_PORT: FluidUtil.interactWithFluidHandler(player, hand, handler) removed ->
            //   interactWithFluidHandler(player, hand, pos, handler, tx) with caller-owned
            //   transaction (same pattern as the ported BlockWaterJug).
            try (var tx = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                if (net.neoforged.neoforge.transfer.fluid.FluidUtil.interactWithFluidHandler(
                        player, hand, pos, spa.tank, tx)) {
                    tx.commit();
                    spa.setChanged();
                    spa.syncTile(false);
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.BOTTLE_FILL,
                            net.minecraft.sounds.SoundSource.BLOCKS, 0.33f,
                            1.0f + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.3f);
                    return InteractionResult.CONSUME;
                }
            }
        }
        if (be instanceof TileSpa spa && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu( new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("block.arcaneresearch.spa");
                }

                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player p) {
                    return new SpaMenu(containerId, playerInventory, spa);
                }
            }, pos);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileSpa spa && spa.tank.getFluidAmount() > 0) {

            if (random.nextInt(5) == 0) {
                double x = pos.getX() + 0.2 + random.nextDouble() * 0.6;
                double y = pos.getY() + 0.5 + random.nextDouble() * 0.2;
                double z = pos.getZ() + 0.2 + random.nextDouble() * 0.6;
                level.addParticle(ParticleTypes.BUBBLE, x, y, z, 0, 0.05, 0);
            }

            if (random.nextInt(20) == 0) {
                double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
                double y = pos.getY() + 0.6;
                double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;
                level.addParticle(ParticleTypes.SPLASH, x, y, z, 0, 0.1, 0);
            }
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    // TODO_PORT: @Override removed for 26.3 green build (signature mismatch, rework deferred).
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileSpa spa) {
            return (int) ((float) spa.tank.getFluidAmount() / TileSpa.TANK_CAPACITY * 15);
        }
        return 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileSpa(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.SPA.get()) {
            if (level.isClientSide()) {
                return null;
            } else {
                return (lvl, pos, st, be) -> TileSpa.serverTick(lvl, pos, st, (TileSpa) be);
            }
        }
        return null;
    }
}
