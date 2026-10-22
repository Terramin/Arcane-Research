package thaumictinker.crops;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IAspectContainer;

 



 
public class InfusedGrainBlockEntity extends BlockEntity implements IAspectContainer {

    public static final int MAX_TENDENCIES = 50;
    public static final int MAX_ABSORBED = 5;
    public static final int GROWTH_LIGHT = 9;

    private Aspect aspect = Aspect.AIR;
    private AspectList tendencies = new AspectList();

    public InfusedGrainBlockEntity(BlockPos pos, BlockState state) {
        super(CropsFeature.INFUSED_GRAIN_ENTITY, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, InfusedGrainBlockEntity grain) {
        if (level instanceof ServerLevel serverLevel) {
            grain.tickServer(serverLevel, pos, state, serverLevel.getRandom());
        }
    }

    public Aspect getAspect() {
        return aspect;
    }

    public AspectList getTendencies() {
        return tendencies;
    }

    public int tendency(Aspect tendency) {
        return tendencies.getAmount(tendency);
    }

    public void setup(Aspect aspect, AspectList tendencies) {
        this.aspect = aspect;
        this.tendencies = tendencies.copy();
        refreshKind();
        sync();
    }

    public void setAspect(Aspect aspect) {
        this.aspect = aspect;
        refreshKind();
        sync();
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private void refreshKind() {
        if (level == null) {
            return;
        }
        BlockState state = getBlockState();
        if (state.getBlock() instanceof InfusedGrainBlock block) {
            BlockState updated = block.withAspect(state, aspect);
            if (updated != state) {
                level.setBlock(worldPosition, updated, Block.UPDATE_CLIENTS);
            }
        }
    }

    public boolean growthRoll(RandomSource random) {
        int water = tendency(Aspect.WATER);
        return random.nextInt(Math.max(1, (2510 - water * water) * 6)) == 0;
    }

    public boolean exchangeRoll(RandomSource random) {
        int air = tendency(Aspect.AIR);
        return random.nextInt(Math.max(1, (2550 - air * air) * 10)) == 0 && !aspect.isPrimal();
    }

    public void tickServer(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (level.getMaxLocalRawBrightness(pos.above()) >= GROWTH_LIGHT) {
            tryGrow(level, pos, state, random);
        }
        if (exchangeRoll(random)) {
            exchange(level, random);
        }
    }

    public boolean tryGrow(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (!(state.getBlock() instanceof InfusedGrainBlock block)
                || block.isMaxAge(state) || !growthRoll(random)) {
            return false;
        }
        level.setBlock(pos, state.setValue(InfusedGrainBlock.AGE, block.getAge(state) + 1), Block.UPDATE_ALL);
        return true;
    }

    public boolean exchange(ServerLevel level, RandomSource random) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = worldPosition.relative(direction);
            if (!(level.getBlockEntity(neighborPos) instanceof InfusedGrainBlockEntity neighbor)) {
                continue;
            }
            Aspect neighborAspect = neighbor.aspect;
            if (neighborAspect.isPrimal()) {
                if (tendencies.getAmount(neighborAspect) < MAX_ABSORBED) {
                    tendencies.add(neighborAspect, 1);
                    reduceSaturatedAspects(random);
                    sync();
                    sendFx(level, neighborPos, neighborAspect);
                    return true;
                }
            } else {
                AspectList source = neighbor.tendencies;
                if (source.size() == 0) {
                    return false;
                }
                Aspect[] candidates = source.getAspects();
                Aspect chosen = candidates[random.nextInt(candidates.length)];
                if (source.getAmount(chosen) >= tendencies.getAmount(chosen)) {
                    tendencies.add(chosen, 1);
                    source.reduce(chosen, 1);
                    reduceSaturatedAspects(random);
                    neighbor.setChanged();
                    sync();
                    sendFx(level, neighborPos, chosen);
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    private void sendFx(ServerLevel level, BlockPos from, Aspect colorAspect) {
        thaumictinker.net.TtNet.sendToTrackingChunk(level, worldPosition,
                thaumictinker.net.TtNet.cropFx(from, worldPosition, colorAspect.getColor()));
    }

    public void reduceSaturatedAspects(RandomSource random) {
        int toRemove = tendencies.visSize() - MAX_TENDENCIES;
        while (toRemove > 0) {
            Aspect[] aspects = tendencies.getAspects();
            if (aspects.length == 0) {
                break;
            }
            tendencies.remove(aspects[random.nextInt(aspects.length)], 1);
            toRemove--;
        }
    }

    @Override
    public AspectList getAspects() {
        return new AspectList().add(aspect, 1);
    }

    @Override
    public void setAspects(AspectList aspects) {
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
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("aspect", aspect.getTag());
        tendencies.writeToNBT(tag, "tendencies");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        Aspect loaded = Aspect.getAspect(tag.getString("aspect"));
        aspect = loaded != null ? loaded : Aspect.AIR;
        tendencies = new AspectList();
        tendencies.readFromNBT(tag, "tendencies");
    }
}
