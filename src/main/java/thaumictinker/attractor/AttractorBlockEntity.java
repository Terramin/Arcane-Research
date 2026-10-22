package thaumictinker.attractor;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

 


 
public class AttractorBlockEntity extends BlockEntity {

    public static final float STRENGTH = 0.25F;
    public static final int PULL_COLOR = 2;
    public static final int PUSH_COLOR = 4;

    public AttractorBlockEntity(BlockPos pos, BlockState state) {
        this(AttractorFeature.KINETIC_ENTITY, pos, state);
    }

    protected AttractorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public boolean applies(Entity entity) {
        return entity instanceof ItemEntity;
    }

    public static int redstone(Level level, BlockPos pos) {
        return level.getBestNeighborSignal(pos);
    }

    public static AABB range(BlockPos pos, int redstone) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        double radius = redstone / 2.0D;
        return new AABB(x - radius, pos.getY(), z - radius, x + radius, y + radius, z + radius);
    }

    protected void sync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        int redstone = redstone(level, pos);
        if (redstone <= 0) {
            return;
        }
        boolean pull = state.getValue(AttractorBlock.PULL);
        float speed = pull ? STRENGTH : -STRENGTH;
        Vec3 origin = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        List<Entity> entities = level.getEntitiesOfClass(Entity.class, range(pos, redstone), this::applies);
        for (Entity entity : entities) {
            double x = entity.getX();
            double y = entity.getY() + (entity instanceof ItemEntity ? entity.getBbHeight() / 2.0D : 0.0D);
            double z = entity.getZ();
            double distanceSquared = pull ? origin.distanceToSqr(x, y, z) : 1.1D;
            if (distanceSquared > 1.0D) {
                if (level.isClientSide()) {
                    if (pull) {
                        arcaneresearch.client.fx.FXDispatcher.INSTANCE.sparkle((float) x, (float) y, (float) z, 0.4F, 0.7F, 1.0F);
                    } else {
                        arcaneresearch.client.fx.FXDispatcher.INSTANCE.sparkle((float) x, (float) y, (float) z, 1.0F, 0.6F, 0.3F);
                    }
                } else {
                    pullTowards(entity, origin, speed);
                }
            }
        }
    }

    public static void pullTowards(Entity entity, Vec3 origin, float modifier) {
        Vec3 center = new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() / 2.0D, entity.getZ());
        Vec3 difference = origin.subtract(center);
        if (difference.length() > 1.0D) {
            difference = difference.normalize();
        }
        entity.setDeltaMovement(difference.scale(modifier));
        entity.hasImpulse = true;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}