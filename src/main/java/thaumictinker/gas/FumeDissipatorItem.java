package thaumictinker.gas;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import arcaneresearch.init.ModSounds;

 



 
public class FumeDissipatorItem extends Item {

    public static final int RADIUS = 3;

    public FumeDissipatorItem(Properties properties) {
        super(properties);
    }

    public static int dissipate(Level level, int centerX, int centerY, int centerZ) {
        int removed = 0;
        for (int x = centerX - RADIUS; x < centerX + RADIUS; x++) {
            for (int y = centerY - RADIUS; y < centerY + RADIUS; y++) {
                for (int z = centerZ - RADIUS; z < centerZ + RADIUS; z++) {
                    if (level.getBlockState(new net.minecraft.core.BlockPos(x, y, z)).getBlock() instanceof GasBlock) {
                        level.setBlock(new net.minecraft.core.BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        removed++;
                    }
                }
            }
        }
        return removed;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
        if (!level.isClientSide) {
            dissipate(level, (int) player.getX(), (int) player.getY(), (int) player.getZ());
            level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.WAND.get(), net.minecraft.sounds.SoundSource.PLAYERS, 0.2F, 1.0F);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}
