package thaumictinker.mobilizer;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import arcaneresearch.init.ModBlocks;

 




 
public class MobilizerBlockEntity extends BlockEntity {

    private static final int PERIOD = 100;
    private static final int CARRY_FLAGS = Block.UPDATE_ALL;
    private static final int CLEAR_FLAGS = Block.UPDATE_ALL;

    private boolean linked;
    private int firstRelayX;
    private int firstRelayZ;
    private int secondRelayX;
    private int secondRelayZ;
    private Direction movementDirection = Direction.DOWN;

    public MobilizerBlockEntity(BlockPos pos, BlockState state) {
        super(MobilizerFeature.MOBILIZER_ENTITY, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MobilizerBlockEntity mobilizer) {
        mobilizer.tick(level);
    }

    public boolean isLinked() {
        return linked;
    }

    public Direction movementDirection() {
        return movementDirection;
    }

    public int firstRelayX() {
        return firstRelayX;
    }

    public int firstRelayZ() {
        return firstRelayZ;
    }

    public int secondRelayX() {
        return secondRelayX;
    }

    public int secondRelayZ() {
        return secondRelayZ;
    }

    public void link(int firstX, int firstZ, int secondX, int secondZ, Direction direction) {
        firstRelayX = firstX;
        firstRelayZ = firstZ;
        secondRelayX = secondX;
        secondRelayZ = secondZ;
        movementDirection = direction;
        linked = true;
        setChanged();
    }

    private void copyFrom(MobilizerBlockEntity other) {
        linked = other.linked;
        firstRelayX = other.firstRelayX;
        firstRelayZ = other.firstRelayZ;
        secondRelayX = other.secondRelayX;
        secondRelayZ = other.secondRelayZ;
        movementDirection = other.movementDirection;
        setChanged();
    }

    public void verifyRelay() {
        if (level == null) {
            return;
        }
        BlockPos relayPos = new BlockPos(firstRelayX, worldPosition.getY(), firstRelayZ);
        RelayBlockEntity relay = level.hasChunkAt(relayPos) && level.getBlockEntity(relayPos) instanceof RelayBlockEntity found ? found : null;
        if (relay != null && !level.isClientSide) {
            relay.verifyPartner();
        }
        if (linked && !(relay != null && relay.partnerX() == secondRelayX && relay.partnerZ() == secondRelayZ)) {
            linked = false;
            setChanged();
        }
    }

    private void tick(Level level) {
        if (isRemoved() || level.isClientSide) {
            return;
        }
        verifyRelay();
        long time = level.getGameTime();
        if (linked && time % PERIOD == 0 && !level.hasNeighborSignal(worldPosition)) {
            bounce(level);
        }
        if (linked && time % PERIOD == 1 && !level.hasNeighborSignal(worldPosition)) {
            move(level);
        }
    }

    private BlockPos targetPos() {
        return new BlockPos(worldPosition.getX() + movementDirection.getStepX(), worldPosition.getY(), worldPosition.getZ() + movementDirection.getStepZ());
    }

    public void bounce(Level level) {
        BlockPos target = targetPos();
        if (!level.hasChunkAt(target) || !level.getBlockState(target).isAir() || !level.getBlockState(target.above()).isAir()) {
            movementDirection = movementDirection.getOpposite();
            setChanged();
        }
    }

    public boolean move(Level level) {
        if (!(level instanceof ServerLevel serverLevel) || isRemoved()) {
            return false;
        }
        BlockState self = getBlockState();
        if (!self.is(MobilizerFeature.MOBILIZER)) {
            return false;
        }
        BlockPos target = targetPos();
        if (!serverLevel.hasChunkAt(target) || !serverLevel.getBlockState(target).isAir()) {
            return false;
        }
        BlockPos passengerPos = worldPosition.above();
        BlockPos passengerTarget = target.above();
        BlockState passenger = serverLevel.getBlockState(passengerPos);
        BlockEntity passengerEntity = serverLevel.getBlockEntity(passengerPos);
        CompoundTag passengerData = passengerEntity != null && isMovable(passengerEntity)
                ? passengerEntity.saveWithFullMetadata()
                : null;

        if (!serverLevel.setBlock(target, self, Block.UPDATE_ALL) || !(serverLevel.getBlockEntity(target) instanceof MobilizerBlockEntity moved)) {
            return false;
        }
        moved.copyFrom(this);

        if (canCarry(serverLevel, passengerPos, passenger, passengerEntity, passengerTarget)) {
            BlockEntity carried = null;
            if (passengerEntity != null) {
                carried = BlockEntity.loadStatic(passengerTarget, passenger, passengerData);
            }
            if (passengerEntity == null || carried != null) {
                carry(serverLevel, passengerPos, passenger, passengerTarget, carried);
            }
        }

        serverLevel.setBlock(worldPosition, Blocks.AIR.defaultBlockState(), CARRY_FLAGS);
        return true;
    }

    private static boolean canCarry(ServerLevel level, BlockPos from, BlockState passenger, @Nullable BlockEntity passengerEntity, BlockPos to) {
        if (passenger.isAir()
                || passenger.is(ModBlocks.HOLE.get())
                || passenger.getDestroySpeed(level, from) < 0.0F) {
            return false;
        }
        if (passengerEntity != null && !isMovable(passengerEntity)) {
            return false;
        }
        return level.hasChunkAt(to) && level.getBlockState(to).canBeReplaced() && passenger.canSurvive(level, to);
    }

    private static boolean isMovable(BlockEntity entity) {
        return entity instanceof MovableTile || "minecraft".equals(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(entity.getType()).getNamespace());
    }

    private static void carry(ServerLevel level, BlockPos from, BlockState passenger, BlockPos to, @Nullable BlockEntity carried) {
        if (!level.setBlock(to, passenger, CARRY_FLAGS) || !level.getBlockState(to).is(passenger.getBlock())) {
            return;
        }
        if (carried != null) {
            level.setBlockEntity(carried);
        }
        level.removeBlockEntity(from);
        level.setBlock(from, Blocks.AIR.defaultBlockState(), CLEAR_FLAGS);
    }

    protected void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        linked = tag.getBoolean("Linked");
        firstRelayX = tag.getInt("FirstRelayX");
        firstRelayZ = tag.getInt("FirstRelayZ");
        secondRelayX = tag.getInt("SecondRelayX");
        secondRelayZ = tag.getInt("SecondRelayZ");
        movementDirection = Direction.from3DDataValue(tag.getInt("Direction"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("Linked", linked);
        tag.putInt("FirstRelayX", firstRelayX);
        tag.putInt("FirstRelayZ", firstRelayZ);
        tag.putInt("SecondRelayX", secondRelayX);
        tag.putInt("SecondRelayZ", secondRelayZ);
        tag.putInt("Direction", movementDirection.get3DDataValue());
    }
}
