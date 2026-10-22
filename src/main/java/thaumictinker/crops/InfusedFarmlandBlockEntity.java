package thaumictinker.crops;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IAspectContainer;

 


 
public class InfusedFarmlandBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity
        implements IAspectContainer {

    public static final int MAX_ASPECTS = 20;

    private AspectList aspectList = new AspectList();

    public InfusedFarmlandBlockEntity(BlockPos pos, BlockState state) {
        super(CropsFeature.INFUSED_FARMLAND_ENTITY, pos, state);
    }

    public void reduceSaturatedAspects(RandomSource random) {
        int toRemove = aspectList.visSize() - MAX_ASPECTS;
        while (toRemove > 0) {
            Aspect[] aspects = aspectList.getAspects();
            if (aspects.length == 0) {
                break;
            }
            Aspect target = aspects[random.nextInt(aspects.length)];
            aspectList.remove(target, 1);
            toRemove--;
        }
    }

    @Override
    public AspectList getAspects() {
        return aspectList;
    }

    @Override
    public void setAspects(AspectList aspects) {
        aspectList = aspects.copy();
        setChanged();
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return false;
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        return 0;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        return false;
    }

    @Override
    public boolean takeFromContainer(AspectList aspects) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        return false;
    }

    @Override
    public boolean doesContainerContain(AspectList aspects) {
        return false;
    }

    @Override
    public int containerContains(Aspect aspect) {
        return 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        aspectList.writeToNBT(tag, "aspect_list");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        aspectList = new AspectList();
        aspectList.readFromNBT(tag, "aspect_list");
    }
}
