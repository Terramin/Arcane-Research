package arcaneresearch.common.tiles.essentia;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IAspectContainer;
import arcaneresearch.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class TileTubeFilter extends TileTube implements IAspectContainer {

    public Aspect aspectFilter = null;

    @Nullable
    private Aspect cachedFilter = null;
    @Nullable
    private AspectList cachedList = null;

    public TileTubeFilter(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileTubeFilter(BlockPos pos, BlockState state) {
        this(ModBlockEntities.TUBE_FILTER.get(), pos, state);
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        if (aspectFilter != null) {
            tag.putString("AspectFilter", aspectFilter.getTag());
        }
    }

    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        aspectFilter = tag.contains("AspectFilter") ? Aspect.getAspect(tag.getString("AspectFilter")) : null;
    }

    @Override
    protected void calculateSuction(Aspect filter, boolean restrict, boolean directional) {
        super.calculateSuction(aspectFilter, restrict, directional);
    }

    @Override
    public AspectList getAspects() {
        Aspect f = aspectFilter;
        if (f == null) {
            return null;
        }
        AspectList cached = cachedList;
        if (cached == null || cachedFilter != f) {
            cached = new AspectList().add(f, -1);
            cachedList = cached;
            cachedFilter = f;
        }
        return cached;
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
        if (this.aspectFilter != aspect) {
            this.aspectFilter = aspect;
            markDirtyAndSync();
        }
    }

    public void clearFilter() {
        if (this.aspectFilter != null) {
            this.aspectFilter = null;
            markDirtyAndSync();
        }
    }
}