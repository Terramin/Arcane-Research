package thaumictinker.gas;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

 



 
public class BrightNitorItem extends Item {

    public BrightNitorItem(Properties properties) {
        super(properties);
    }

    public static void placeNitorGas(Level level, BlockPos pos, boolean strong) {
        if (level.isClientSide) {
            return;
        }
        BlockState existing = level.getBlockState(pos);
        if (existing.isAir() || existing.getBlock() instanceof GasBlock) {
            level.setBlock(pos, GasFeature.NITOR_GAS.defaultBlockState().setValue(NitorGasBlock.STRONG, strong),
                    Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity owner, int slot, boolean selected) {
        placeNitorGas(level, new BlockPos((int) Math.floor(owner.getX()), (int) owner.getY() + 1, (int) Math.floor(owner.getZ())),
                false);
        super.inventoryTick(stack, level, owner, slot, selected);
    }
}
