package arcaneresearch.common.entities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModEntities;
import arcaneresearch.init.ModSounds;
public class EntityFallingTaint extends Entity implements IEntityAdditionalSpawnData {
    public BlockState fallTile;
    public int fallTime;
    private BlockPos oldPos;
    private int fallHurtMax = 40;
    private float fallHurtAmount = 2.0f;
    public EntityFallingTaint(EntityType<?> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }
    public EntityFallingTaint(Level level) {
        this(ModEntities.FALLING_TAINT.get(), level);
    }
    public EntityFallingTaint(Level level, double x, double y, double z, BlockState state, BlockPos originalPos) {
        this(ModEntities.FALLING_TAINT.get(), level);
        this.fallTile = state;
        this.oldPos = originalPos;
        this.blocksBuilding = true;
        setPos(x, y, z);
        setDeltaMovement(Vec3.ZERO);
        xo = x;
        yo = y;
        zo = z;
    }
    public BlockState getBlockState() {
        return fallTile;
    }
    @Override
    protected void defineSynchedData() {
    }
    @Override
    public boolean isPickable() {
        return !isRemoved();
    }
    @Override
    protected boolean repositionEntityAfterLoad() {
        return false;
    }
    @Override
    public void tick() {
        if (fallTile == null || fallTile.isAir()) {
            discard();
            return;
        }
        xo = getX();
        yo = getY();
        zo = getZ();
        ++fallTime;
        setDeltaMovement(getDeltaMovement().add(0.0, -0.04, 0.0));
        move(MoverType.SELF, getDeltaMovement());
        setDeltaMovement(getDeltaMovement().scale(0.98));
        BlockPos currentPos = blockPosition();
        if (!level().isClientSide) {
            if (fallTime == 1) {
                if (oldPos != null && !level().getBlockState(oldPos).equals(fallTile)) {
                    discard();
                    return;
                }
                if (oldPos != null) {
                    level().removeBlock(oldPos, false);
                }
            }
            if (onGround() || isTaintGooBelow(currentPos)) {
                setDeltaMovement(getDeltaMovement().multiply(0.7, -0.5, 0.7));
                BlockState landedState = level().getBlockState(currentPos);
                if (landedState.is(net.minecraft.world.level.block.Blocks.PISTON)
                        || landedState.is(net.minecraft.world.level.block.Blocks.PISTON_HEAD)
                        || landedState.is(net.minecraft.world.level.block.Blocks.MOVING_PISTON)) {
                    discard();
                } else {
                    playSound(ModSounds.GORE.get(), 0.5f, ((random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f) * 0.8f);
                    discard();
                    if (canPlace(currentPos)
                            && !arcaneresearch.common.blocks.world.taint.BlockTaint.canFallBelow(level(), currentPos.below())) {
                        level().setBlock(currentPos, fallTile, 3);
                    }
                }
            } else if ((fallTime > 100 && (currentPos.getY() < level().getMinBuildHeight() ||
                    currentPos.getY() > level().getMaxBuildHeight())) || fallTime > 600) {
                discard();
            }
        } else {
            if (onGround() || fallTime == 1) {
                spawnLandingFX();
            }
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void spawnLandingFX() {
        for (int j = 0; j < 10; ++j) {
            FXDispatcher.INSTANCE.taintLandFX(this);
        }
    }
    private boolean isTaintGooBelow(BlockPos pos) {
        return level().getBlockState(pos.below()).is(ModBlocks.FLUX_GOO.get());
    }
    private boolean canPlace(BlockPos pos) {
        BlockState currentState = level().getBlockState(pos);
        if (currentState.isAir() || currentState.canBeReplaced()) {
            return true;
        }
        return currentState.is(ModBlocks.TAINT_FIBRE.get())
                || currentState.is(ModBlocks.FLUX_GOO.get());
    }
    @Override
    public boolean causeFallDamage(float distance, float multiplier, net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }
    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (fallTile != null) {
            tag.put("BlockState", NbtUtils.writeBlockState(fallTile));
        }
        tag.putInt("Time", fallTime);
        tag.putFloat("FallHurtAmount", fallHurtAmount);
        tag.putInt("FallHurtMax", fallHurtMax);
        if (oldPos != null) {
            tag.putLong("Old", oldPos.asLong());
        }
    }
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("BlockState")) {
            fallTile = NbtUtils.readBlockState(level().holderLookup(net.minecraft.core.registries.Registries.BLOCK), tag.getCompound("BlockState"));
        } else {
            fallTile = Blocks.SAND.defaultBlockState();
        }
        fallTime = tag.getInt("Time");
        if (tag.contains("FallHurtAmount")) {
            fallHurtAmount = tag.getFloat("FallHurtAmount");
            fallHurtMax = tag.getInt("FallHurtMax");
        }
        if (tag.contains("Old")) {
            oldPos = BlockPos.of(tag.getLong("Old"));
        }
    }
    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        if (fallTile != null) {
            buffer.writeInt(Block.getId(fallTile));
        } else {
            buffer.writeInt(0);
        }
    }
    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        int stateId = buffer.readInt();
        if (stateId > 0) {
            fallTile = Block.stateById(stateId);
        }
    }
    @Override
    public SoundSource getSoundSource() {
        return SoundSource.BLOCKS;
    }
    @Override
    public boolean displayFireAnimation() {
        return false;
    }
}
