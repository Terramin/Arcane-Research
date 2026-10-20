package arcaneresearch.common.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModEntities;
import arcaneresearch.init.ModSounds;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;

public class EntityFallingTaint extends Entity implements net.neoforged.neoforge.entity.IEntityWithComplexSpawn {
    // NEOFORGE_PORT: Entity.hurtServer abstract in 26.3; this entity is unhurtable (was: no hurt override).
    @Override
    public final boolean hurtServer(net.minecraft.server.level.ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float damage) {
        if (!isInvulnerableToBase(source)) {
            markHurt();
        }
        return false;
    }

    // NEOFORGE_PORT: Forge IEntityAdditionalSpawnData -> NeoForge IEntityWithComplexSpawn (same payload).

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
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {

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

        if (!level().isClientSide()) {

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
            // NEOFORGE_PORT (26.3): getMaxBuildHeight() (exclusive) == getMaxY() + 1.
            } else if ((fallTime > 100 && (currentPos.getY() < level().getMinY() ||
                    currentPos.getY() > level().getMaxY() + 1)) || fallTime > 600) {
                discard();
            }
        } else {

            if (onGround() || fallTime == 1) {
                spawnLandingFX();
            }
        }
    }

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

    // NEOFORGE_PORT: 26.x causeFallDamage takes a double fall distance.
    @Override
    public boolean causeFallDamage(double distance, float multiplier, net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {

        return true;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput tag) {
        if (fallTile != null) {
            // NEOFORGE_PORT: ValueOutput has no put(CompoundTag) -> store with CODEC.
            tag.store("BlockState", net.minecraft.nbt.CompoundTag.CODEC, NbtUtils.writeBlockState(fallTile));
        }
        tag.putInt("Time", fallTime);
        tag.putFloat("FallHurtAmount", fallHurtAmount);
        tag.putInt("FallHurtMax", fallHurtMax);
        if (oldPos != null) {
            tag.putLong("Old", oldPos.asLong());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput tag) {
        // NEOFORGE_PORT: 1:1 from Forge (CompoundTag getters -> ValueInput Or-getters;
        // BlockState via CompoundTag.CODEC read, mirroring the ValueOutput store above).
        var blockStateTag = tag.read("BlockState", CompoundTag.CODEC);
        if (blockStateTag.isPresent()) {
            fallTile = NbtUtils.readBlockState(net.minecraft.core.registries.BuiltInRegistries.BLOCK, blockStateTag.get());
        } else {
            fallTile = Blocks.SAND.defaultBlockState();
        }
        fallTime = tag.getIntOr("Time", 0);
        var hurtAmount = tag.read("FallHurtAmount", com.mojang.serialization.Codec.FLOAT);
        if (hurtAmount.isPresent()) {
            fallHurtAmount = hurtAmount.get();
            fallHurtMax = tag.getIntOr("FallHurtMax", 0);
        }
        var old = tag.read("Old", com.mojang.serialization.Codec.LONG);
        if (old.isPresent()) {
            oldPos = BlockPos.of(old.get());
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

    @Override
    public void writeSpawnData(net.minecraft.network.RegistryFriendlyByteBuf buffer) {
        if (fallTile != null) {
            buffer.writeInt(net.minecraft.world.level.block.Block.getId(fallTile));
        } else {
            buffer.writeInt(0);
        }
    }

    @Override
    public void readSpawnData(net.minecraft.network.RegistryFriendlyByteBuf buffer) {
        int stateId = buffer.readInt();
        if (stateId > 0) {
            fallTile = net.minecraft.world.level.block.Block.stateById(stateId);
        }
    }
}
