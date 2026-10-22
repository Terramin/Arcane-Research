package thaumictinker.funnel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.items.consumables.ItemPhial;
import arcaneresearch.common.tiles.essentia.TileJar;
import arcaneresearch.common.tiles.essentia.TileJarVoid;
import arcaneresearch.init.ModItems;
import thaumictinker.mechanism.ItemHolderBlockEntity;

 



 
public class FunnelBlockEntity extends ItemHolderBlockEntity {

    public FunnelBlockEntity(BlockPos pos, BlockState state) {
        super(FunnelFeature.FUNNEL_ENTITY, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FunnelBlockEntity funnel) {
        funnel.transfer(level, pos);
    }

    @Override
    public boolean accepts(ItemStack stack) {
        return stack.getItem() instanceof ItemPhial phial && phial.isFilled();
    }

    @Override
    protected boolean exposesSlot(Direction face) {
        return face != Direction.DOWN;
    }

    private void transfer(Level level, BlockPos pos) {
        ItemStack stack = getTheItem();
        if (!(stack.getItem() instanceof ItemPhial phial)) {
            return;
        }
        AspectList aspects = phial.getAspects(stack);
        if (aspects == null || aspects.size() != 1) {
            return;
        }
        Aspect aspect = aspects.getAspects()[0];

        BlockEntity hopper = level.getBlockEntity(pos.below());
        if (!(hopper instanceof HopperBlockEntity)) {
            return;
        }
        Direction facing = hopper.getBlockState().getValue(HopperBlock.FACING);
        BlockEntity destination = level.getBlockEntity(pos.below().relative(facing));
        if (!(destination instanceof TileJar jar)) {
            return;
        }
        
        
        
        boolean accepts = jar.getAmount() == 0
                ? jar.doesContainerAccept(aspect)
                : jar.containerContains(aspect) > 0
                        && (jar.getAmount() < TileJar.CAPACITY || jar instanceof TileJarVoid);
        if (!accepts) {
            return;
        }
        int leftover = jar.addToContainer(aspect, 1);
        if (leftover != 0) {
            return;
        }
        AspectList remaining = aspects.remove(aspect, 1);
        if (remaining.size() > 0) {
            phial.setAspects(stack, remaining);
        } else {
            setTheItem(new ItemStack(ModItems.PHIAL_EMPTY.get()));
        }
        setChanged();
    }
}
