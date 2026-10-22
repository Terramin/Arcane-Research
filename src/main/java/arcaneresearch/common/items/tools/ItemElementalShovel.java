package arcaneresearch.common.items.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.api.items.IArchitect;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.lib.utils.InventoryUtils;
import arcaneresearch.common.lib.utils.Utils;

import java.util.ArrayList;

public class ItemElementalShovel extends Item implements IArchitect {

    public ItemElementalShovel(Item.Properties props) {
        
        super(props
                .rarity(Rarity.RARE)
                .shovel(ArcaneresearchMaterials.TOOLMAT_ELEMENTAL, 1.5F, -3.0F));
    }

    public static byte getOrientation(ItemStack stack) {
        
        CompoundTag tag = ArcaneresearchInvHelper.getCustomTag(stack);
        if (tag != null && tag.contains("or")) {
            return tag.getByteOr("or", (byte) 0);
        }
        return 0;
    }

    public static void setOrientation(ItemStack stack, byte orientation) {
        
        CompoundTag tag = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
        tag.putByte("or", (byte) (orientation % 3));
        ArcaneresearchInvHelper.setCustomTag(stack, tag);
    }

    private static int[] planeOffsets(ItemStack stack, Direction side, Player player, int aa, int bb) {
        int xx = 0;
        int yy = 0;
        int zz = 0;
        byte o = getOrientation(stack);
        if (o == 1) {
            yy = bb;
            if (side.ordinal() <= 1) {
                int l = Mth.floor(player.getYRot() * 4.0F / 360.0F + 0.5D) & 0x3;
                if (l == 0 || l == 2) {
                    xx = aa;
                } else {
                    zz = aa;
                }
            } else if (side.ordinal() <= 3) {
                zz = aa;
            } else {
                xx = aa;
            }
        } else if (o == 2) {
            if (side.ordinal() <= 1) {
                int l = Mth.floor(player.getYRot() * 4.0F / 360.0F + 0.5D) & 0x3;
                yy = bb;
                if (l == 0 || l == 2) {
                    xx = aa;
                } else {
                    zz = aa;
                }
            } else {
                zz = bb;
                xx = aa;
            }
        } else if (side.ordinal() <= 1) {
            xx = aa;
            zz = bb;
        } else if (side.ordinal() <= 3) {
            xx = aa;
            yy = bb;
        } else {
            zz = aa;
            yy = bb;
        }
        return new int[]{xx, yy, zz};
    }

    private static boolean canPlaceAt(BlockState state, Level level, BlockPos pos) {
        BlockState target = level.getBlockState(pos);
        return target.isAir() || target.canBeReplaced();
    }

    private static boolean placeCopy(Level level, Player player, UseOnContext ctx,
                                     ItemStack stack, BlockPos p2, BlockState toPlace, Direction side) {
        BlockState oldState = level.getBlockState(p2);
        if (!level.setBlock(p2, toPlace, Block.UPDATE_ALL)) {
            return false;
        }

        level.sendBlockUpdated(p2, oldState, toPlace, Block.UPDATE_ALL);
        
        
        stack.hurtAndBreak(1, player, ctx.getHand());
        level.playSound(null, p2, toPlace.getSoundType().getPlaceSound(), SoundSource.BLOCKS,
                0.6F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        player.swing(ctx.getHand(), SwingAnimation.DEFAULT, false);
        return true;
    }

    private static void placeFX(Level level, UseOnContext ctx,
                                BlockPos p2, BlockState state, Direction side) {
        level.playSound(null, p2, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS,
                0.6F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        FXDispatcher.INSTANCE.drawBamf(p2, 8401408, false, false, side);
        Player player = ctx.getPlayer();
        if (player != null) {
            
            player.swing(ctx.getHand(), SwingAnimation.DEFAULT, false);
        }
    }

    
    
    

    @Override
    public BlockHitResult getArchitectMOP(ItemStack stack, Level level, LivingEntity player) {
        return Utils.rayTrace(level, player, false);
    }

    @Override
    public boolean useBlockHighlight(ItemStack stack) {
        return true;
    }

    @Override
    public boolean showAxis(ItemStack stack, Level level, Player player, Direction side, EnumAxis axis) {
        return false;
    }

    @Override
    public ArrayList<BlockPos> getArchitectBlocks(ItemStack stack, Level level,
                                                  BlockPos pos, Direction side, Player player) {
        ArrayList<BlockPos> out = new ArrayList<>();

        if (!player.isShiftKeyDown()) {
            return out;
        }
        BlockState bs = level.getBlockState(pos);
        for (int aa = -1; aa <= 1; aa++) {
            for (int bb = -1; bb <= 1; bb++) {
                int[] off = planeOffsets(stack, side, player, aa, bb);
                BlockPos p2 = pos.relative(side).offset(off[0], off[1], off[2]);
                if (canPlaceAt(bs, level, p2)) {
                    out.add(p2);
                }
            }
        }
        return out;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Direction side = ctx.getClickedFace();
        BlockState bs = level.getBlockState(pos);
        if (player == null || level.getBlockEntity(pos) != null) {
            return InteractionResult.FAIL;
        }
        ItemStack stack = ctx.getItemInHand();
        int placed = 0;
        for (int aa = -1; aa <= 1; aa++) {
            for (int bb = -1; bb <= 1; bb++) {
                int[] off = planeOffsets(stack, side, player, aa, bb);
                BlockPos p2 = pos.relative(side).offset(off[0], off[1], off[2]);
                if (canPlaceAt(bs, level, p2)) {

                    if (level.isClientSide()) {
                        placeFX(level, ctx, p2, bs, side);
                    } else if (player.isCreative()
                            || InventoryUtils.consumePlayerItem(player, bs.getBlock().asItem(), 1)) {
                        if (placeCopy(level, player, ctx, stack, p2, bs, side)) {
                            placed++;
                        }
                    } else if (bs.is(Blocks.GRASS_BLOCK) && (player.isCreative()
                            || InventoryUtils.consumePlayerItem(player, Items.DIRT, 1))) {
                        if (placeCopy(level, player, ctx, stack, p2, Blocks.DIRT.defaultBlockState(), side)) {
                            placed++;
                        }
                        if (stack.isEmpty() || stack.getCount() < 1) {
                            break;
                        }
                    }
                }
            }
        }
        return placed > 0 ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }
}
