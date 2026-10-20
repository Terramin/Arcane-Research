package arcaneresearch.common.tiles.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IAspectContainer;
import arcaneresearch.init.ModBlockEntities;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileTubeFilter extends TileTube implements IAspectContainer {

    public Aspect aspectFilter = null;

    public TileTubeFilter(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileTubeFilter(BlockPos pos, BlockState state) {
        this(ModBlockEntities.TUBE_FILTER.get(), pos, state);
    }

    @Override
    protected void writeSyncNBT(ValueOutput tag) {
        super.writeSyncNBT(tag);
        if (aspectFilter != null) {
            tag.putString("AspectFilter", aspectFilter.getTag());
        }
    }

    @Override
    protected void readSyncNBT(ValueInput tag) {
        super.readSyncNBT(tag);
        aspectFilter = Aspect.getAspect(tag.getStringOr("AspectFilter", ""));
    }

    @Override
    protected void calculateSuction(Aspect filter, boolean restrict, boolean directional) {

        super.calculateSuction(aspectFilter, restrict, directional);
    }

    @Override
    public AspectList getAspects() {
        if (aspectFilter != null) {
            return new AspectList().add(aspectFilter, -1);
        }
        return null;
    }

    @Override
    public void setAspects(AspectList aspects) {

    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return false;
    }

    @Override
    public int addToContainer(Aspect tag, int amount) {
        return 0;
    }

    @Override
    public boolean takeFromContainer(Aspect tag, int amount) {
        return false;
    }

    @Override
    public boolean takeFromContainer(AspectList list) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tag, int amount) {
        return false;
    }

    @Override
    public boolean doesContainerContain(AspectList list) {
        return false;
    }

    @Override
    public int containerContains(Aspect tag) {
        return 0;
    }

    public Aspect getFilter() {
        return aspectFilter;
    }

    public void setFilter(Aspect aspect) {
        this.aspectFilter = aspect;
        markDirtyAndSync();
    }

    public void clearFilter() {
        this.aspectFilter = null;
        markDirtyAndSync();
    }
}
