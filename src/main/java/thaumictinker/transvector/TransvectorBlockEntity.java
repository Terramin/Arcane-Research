package thaumictinker.transvector;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import thaumictinker.camo.CamoBlockEntity;

 



 
public abstract class TransvectorBlockEntity extends CamoBlockEntity {

    private BlockPos target;

    protected TransvectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract int maxDistance();

    protected boolean tileRequiredAtLink() {
        return true;
    }

    @Nullable
    public BlockPos targetPos() {
        return target;
    }

    public boolean isBound() {
        return target != null;
    }

    public void bind(BlockPos pos) {
        target = pos.immutable();
        sync();
    }

    public boolean isInRange(BlockPos pos) {
        return Math.abs(pos.getX() - worldPosition.getX()) <= maxDistance()
                && Math.abs(pos.getY() - worldPosition.getY()) <= maxDistance()
                && Math.abs(pos.getZ() - worldPosition.getZ()) <= maxDistance();
    }

    @Nullable
    public final BlockEntity getTarget() {
        if (target == null || level == null || !level.isLoaded(target)) {
            return null;
        }
        BlockEntity entity = level.getBlockEntity(target);
        if ((entity == null && tileRequiredAtLink()) || !isInRange(target)) {
            if (!level.isClientSide) {
                target = null;
                setChanged();
            }
            return null;
        }
        return entity;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        target = tag.contains("target") ? BlockPos.of(tag.getLong("target")) : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (target != null) {
            tag.putLong("target", target.asLong());
        }
    }
}
