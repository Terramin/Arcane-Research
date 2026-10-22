package thaumictinker.gas;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

 


 
public class GasBottleItem extends Item {

    public static final int RELEASE_LEVEL = 4;

    private final Supplier<? extends SpreadingGasBlock> gas;

    public GasBottleItem(Supplier<? extends SpreadingGasBlock> gas, Properties properties) {
        super(properties);
        this.gas = gas;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockPos pos = new BlockPos((int) player.getX(), (int) player.getY() + 1, (int) player.getZ());
        boolean free = GasBlock.canHoldGas(level.getBlockState(pos));
        if (!level.isClientSide) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            float pitch = 0.4F / (level.random.nextFloat() * 0.4F + 0.8F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5F, pitch);
            if (free) {
                gas.get().release((net.minecraft.server.level.ServerLevel) level, pos, RELEASE_LEVEL);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
