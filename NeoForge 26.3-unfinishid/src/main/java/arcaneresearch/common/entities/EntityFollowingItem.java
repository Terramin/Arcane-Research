package arcaneresearch.common.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.init.ModEntities;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;

public class EntityFollowingItem extends EntitySpecialItem {
    // TODO_PORT (packet wave): IEntityAdditionalSpawnData removed in NeoForge; extra spawn data must go through sendPairingData (see Entity base).

    private static final EntityDataAccessor<Integer> DATA_TYPE =
            SynchedEntityData.defineId(EntityFollowingItem.class, EntityDataSerializers.INT);
    public double gravity = 0.04;
    private double targetX;
    private double targetY;
    private double targetZ;
    private Entity target;
    private int age = 20;

    public EntityFollowingItem(EntityType<? extends EntityFollowingItem> type, Level level) {
        super(type, level);
    }

    public EntityFollowingItem(Level level) {
        this(ModEntities.FOLLOWING_ITEM.get(), level);
    }

    public EntityFollowingItem(Level level, double x, double y, double z, ItemStack stack) {
        this(ModEntities.FOLLOWING_ITEM.get(), level);
        this.setPos(x, y, z);
        this.setItem(stack);
        this.setYRot((float) (Math.random() * 360.0));
    }

    public EntityFollowingItem(Level level, double x, double y, double z, ItemStack stack, Entity target, int type) {
        this(level, x, y, z, stack);
        this.target = target;
        this.targetX = target.getX();
        this.targetY = target.getBoundingBox().minY + target.getBbHeight() / 2.0f;
        this.targetZ = target.getZ();
        setFollowType(type);
        this.noPhysics = true;
    }

    public EntityFollowingItem(Level level, double x, double y, double z, ItemStack stack, double tx, double ty, double tz) {
        this(level, x, y, z, stack);
        this.targetX = tx;
        this.targetY = ty;
        this.targetZ = tz;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TYPE, 3);
    }

    public int getFollowType() {
        return this.entityData.get(DATA_TYPE);
    }

    public void setFollowType(int type) {
        this.entityData.set(DATA_TYPE, type);
    }

    @Override
    public void tick() {

        if (target != null) {
            targetX = target.getX();
            targetY = target.getBoundingBox().minY + target.getBbHeight() / 2.0f;
            targetZ = target.getZ();
        }

        if (targetX != 0.0 || targetY != 0.0 || targetZ != 0.0) {
            float xd = (float) (targetX - getX());
            float yd = (float) (targetY - getY());
            float zd = (float) (targetZ - getZ());

            if (age > 1) {
                --age;
            }

            double distance = Mth.sqrt(xd * xd + yd * yd + zd * zd);
            if (distance > 0.5) {

                double scaledDist = distance * age;
                setDeltaMovement(xd / scaledDist, yd / scaledDist, zd / scaledDist);
            } else {

                setDeltaMovement(getDeltaMovement().scale(0.1));
                targetX = 0.0;
                targetY = 0.0;
                targetZ = 0.0;
                target = null;
                this.noPhysics = false;
            }

            if (level().isClientSide()) {
                spawnFollowingParticles();
            }
        } else {

            setDeltaMovement(getDeltaMovement().add(0, -gravity, 0));
        }

        super.tick();
    }

    private void spawnFollowingParticles() {
        int type = getFollowType();
        float h = (float) ((getBoundingBox().maxY - getBoundingBox().minY) / 2.0)
                + Mth.sin(tickCount / 10.0f) * 0.1f + 0.1f;
        if (type != 10) {
            FXDispatcher.INSTANCE.drawNitorCore((float) xOld + (random.nextFloat() - random.nextFloat()) * 0.125f,
                    (float) yOld + h + (random.nextFloat() - random.nextFloat()) * 0.125f,
                    (float) zOld + (random.nextFloat() - random.nextFloat()) * 0.125f,
                    random.nextGaussian() * 0.01, random.nextGaussian() * 0.01, random.nextGaussian() * 0.01);
        } else {
            FXDispatcher.INSTANCE.crucibleBubble((float) xOld + (random.nextFloat() - random.nextFloat()) * 0.125f,
                    (float) yOld + h + (random.nextFloat() - random.nextFloat()) * 0.125f,
                    (float) zOld + (random.nextFloat() - random.nextFloat()) * 0.125f, 0.33f, 0.33f, 1.0f);
        }
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putShort("followType", (short) getFollowType());
        tag.putDouble("targetX", targetX);
        tag.putDouble("targetY", targetY);
        tag.putDouble("targetZ", targetZ);
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        setFollowType(tag.getShortOr("followType", (short) 0));
        targetX = tag.getDoubleOr("targetX", 0.0D);
        targetY = tag.getDoubleOr("targetY", 0.0D);
        targetZ = tag.getDoubleOr("targetZ", 0.0D);
    }
}