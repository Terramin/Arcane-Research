package thaumictinker.transvector;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import thaumictinker.camo.CamoBlock;

 





 
public class TransvectorDislocatorBlockEntity extends TransvectorBlockEntity {

    public static final int DISTANCE = 16;
    public static final int COOLDOWN = 10;

    private int cooldown;
    private boolean pulseStored;

    public TransvectorDislocatorBlockEntity(BlockPos pos, BlockState state) {
        super(TransvectorFeature.DISLOCATOR_ENTITY, pos, state);
    }

    @Override
    public int maxDistance() {
        return DISTANCE;
    }

    @Override
    protected boolean tileRequiredAtLink() {
        return false;
    }

    public BlockPos swapPos() {
        return worldPosition.relative(getBlockState().getValue(TransvectorDislocatorBlock.FACING));
    }

    public int cooldown() {
        return cooldown;
    }

    public void tick() {
        cooldown = Math.max(0, cooldown - 1);
        if (cooldown == 0 && pulseStored) {
            pulseStored = false;
            receiveRedstonePulse();
        }
    }

    public void receiveRedstonePulse() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        getTarget();
        BlockPos end = targetPos();
        if (end == null) {
            return;
        }
        if (cooldown > 0) {
            pulseStored = true;
            return;
        }
        BlockPos start = swapPos();
        Block self = getBlockState().getBlock();
        List<Entity> entitiesAtEnd = entitiesAt(serverLevel, end);
        List<Entity> entitiesAtStart = entitiesAt(serverLevel, start);
        Vec3 startToEnd = Vec3.atLowerCornerOf(end.subtract(start));
        Vec3 endToStart = startToEnd.scale(-1.0);
        if (!start.equals(end) && serverLevel.isLoaded(end) && swappable(serverLevel, start) && swappable(serverLevel, end)) {
            swap(serverLevel, start, end, self);
        }
        for (Entity entity : entitiesAtEnd) {
            move(serverLevel, entity, endToStart);
        }
        for (Entity entity : entitiesAtStart) {
            move(serverLevel, entity, startToEnd);
        }
        cooldown = COOLDOWN;
    }

    private static List<Entity> entitiesAt(ServerLevel level, BlockPos pos) {
        return level.getEntitiesOfClass(Entity.class, new AABB(pos));
    }

    private static void move(ServerLevel level, Entity entity, Vec3 offset) {
        Vec3 target = entity.position().add(offset);
        entity.teleportTo(target.x, target.y, target.z);
    }

    public static boolean swappable(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !(state.getBlock() instanceof CamoBlock)
                && state.getDestroySpeed(level, pos) >= 0.0F;
    }

    public static void swap(ServerLevel level, BlockPos first, BlockPos second, Block notifier) {
        BlockState firstState = level.getBlockState(first);
        BlockState secondState = level.getBlockState(second);
        CompoundTag firstData = snapshot(level, first);
        CompoundTag secondData = snapshot(level, second);
        level.removeBlockEntity(first);
        level.removeBlockEntity(second);
        place(level, second, firstState, firstData);
        place(level, first, secondState, secondData);
        level.neighborChanged(second, notifier, null);
        level.neighborChanged(first, notifier, null);
    }

    @Nullable
    private static CompoundTag snapshot(ServerLevel level, BlockPos pos) {
        BlockEntity entity = level.getBlockEntity(pos);
        return entity == null ? null : entity.saveWithFullMetadata();
    }

    private static void place(ServerLevel level, BlockPos pos, BlockState state, @Nullable CompoundTag data) {
        level.setBlock(pos, state, Block.UPDATE_ALL);
        BlockEntity restored = data == null ? null : BlockEntity.loadStatic(pos, state, data);
        if (restored != null) {
            level.setBlockEntity(restored);
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        }
    }
}
