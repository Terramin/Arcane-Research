package arcaneresearch.common.blocks.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;

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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.transfer.fluid.FluidUtil; 
import arcaneresearch.common.tiles.devices.TileWaterJug;
import arcaneresearch.init.ModBlockEntities;

import javax.annotation.Nullable;

public class BlockWaterJug extends Block implements EntityBlock {

    private static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);

    public BlockWaterJug(BlockBehaviour.Properties props) {
        super(props
                .mapColor(MapColor.STONE)
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
    
    

    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TileWaterJug tile)) {
            return InteractionResult.PASS;
        }

        ItemStack heldItem = player.getItemInHand(hand);

        if (FluidUtil.interactWithFluidHandler(player, hand, pos, tile.tank, null)) {
            tile.setChanged();
            tile.syncTile(false);
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS,
                    0.33f, 1.0f + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.3f);
            return InteractionResult.CONSUME;
        }

        if (heldItem.is(Items.GLASS_BOTTLE) && tile.tank.getFluidAmount() >= 333) {
            
            ItemStack waterBottle = net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                    Items.POTION, Potions.WATER);

            if (!player.getAbilities().instabuild) {
                heldItem.shrink(1);
            }

            if (heldItem.isEmpty()) {
                player.setItemInHand(hand, waterBottle);
            } else if (!player.getInventory().add(waterBottle)) {
                player.drop(waterBottle, false, net.minecraft.util.Prediction.SERVER_ONLY);
            }

            tile.tank.drain(333, true);
            tile.setChanged();
            tile.syncTile(false);
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS,
                    0.33f, 1.0f + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.3f);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileWaterJug tile && tile.isFull()) {

            if (random.nextInt(5) == 0) {
                arcaneresearch.client.fx.FXDispatcher.INSTANCE.jarSplashFx(
                        pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
            }
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    
    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, net.minecraft.core.Direction direction) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileWaterJug tile) {
            return (int) ((float) tile.getWaterLevel() / TileWaterJug.TANK_CAPACITY * 15);
        }
        return 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileWaterJug(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.WATER_JUG.get()) {
            if (level.isClientSide()) {
                return (lvl, p, s, be) -> TileWaterJug.clientTick(lvl, p, s, (TileWaterJug) be);
            } else {
                return (lvl, p, s, be) -> TileWaterJug.serverTick(lvl, p, s, (TileWaterJug) be);
            }
        }
        return null;
    }
}