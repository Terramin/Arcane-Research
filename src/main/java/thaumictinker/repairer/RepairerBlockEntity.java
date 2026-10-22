package thaumictinker.repairer;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;

import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IEssentiaTransport;
import arcaneresearch.client.fx.FXDispatcher;
import thaumictinker.mechanism.ItemHolderBlockEntity;

 


 
public class RepairerBlockEntity extends ItemHolderBlockEntity implements IEssentiaTransport {

    public static final int SPARKLE_EVENT = 1;
    private static final int SUCTION = 128;
    private static final Map<Aspect, Integer> REPAIR_VALUES = new LinkedHashMap<>();

    static {
        REPAIR_VALUES.put(Aspect.TOOL, 8);
        REPAIR_VALUES.put(Aspect.CRAFT, 5);
        REPAIR_VALUES.put(Aspect.ORDER, 3);
    }

    private int ticksExisted;
    private boolean tookLastTick = true;
    private int damageLastTick;
    private boolean powered;

    public RepairerBlockEntity(BlockPos pos, BlockState state) {
        super(RepairerFeature.REPAIRER_ENTITY, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RepairerBlockEntity repairer) {
        if (++repairer.ticksExisted % 10 == 0 && !level.isClientSide) {
            repairer.repairCycle(level);
        }
    }

    public Direction orientation() {
        return getBlockState().getValue(RepairerBlock.FACING);
    }

    public boolean isDamaged() {
        ItemStack stack = getTheItem();
        return !stack.isEmpty() && stack.getDamageValue() != 0;
    }

    @Override
    public boolean accepts(ItemStack stack) {
        return !stack.isEmpty() && stack.isDamageableItem();
    }

    public void repairCycle(Level level) {
        boolean previous = tookLastTick;
        ItemStack stack = getTheItem();
        if (!stack.isEmpty() && stack.getDamageValue() > 0) {
            int essentia = drawEssentia(level);
            int damage = stack.getDamageValue();
            stack.setDamageValue(Math.max(0, damage - essentia));
            sync();
            if (damageLastTick != 0 && damageLastTick != damage) {
                level.blockEvent(worldPosition, getBlockState().getBlock(), SPARKLE_EVENT, 0);
                tookLastTick = true;
            } else {
                tookLastTick = false;
            }
        } else {
            tookLastTick = false;
        }
        damageLastTick = stack.isEmpty() ? 0 : stack.getDamageValue();
        if (previous != tookLastTick) {
            sync();
        }
    }

    public int drawEssentia(Level level) {
        Direction orientation = orientation();
        BlockEntity tile = ArcaneresearchApiHelper.getConnectableTile(level, worldPosition, orientation);
        if (!(tile instanceof IEssentiaTransport source)) {
            return 0;
        }
        Direction sourceFace = orientation.getOpposite();
        if (!source.canOutputTo(sourceFace)) {
            return 0;
        }
        for (Map.Entry<Aspect, Integer> entry : REPAIR_VALUES.entrySet()) {
            Aspect aspect = entry.getKey();
            if (source.getSuctionType(sourceFace) == aspect
                    && !source.isBlocked()
                    && source.getSuctionAmount(sourceFace) < getSuctionAmount(orientation)
                    && source.takeEssentia(aspect, 1, sourceFace) == 1) {
                return entry.getValue();
            }
        }
        return 0;
    }

    @Override
    protected void sync() {
        super.sync();
        if (level != null && !level.isClientSide) {
            boolean damaged = isDamaged();
            if (damaged != powered) {
                powered = damaged;
                Block block = getBlockState().getBlock();
                level.updateNeighborsAt(worldPosition, block);
                for (Direction direction : Direction.values()) {
                    level.updateNeighborsAt(worldPosition.relative(direction), block);
                }
            }
        }
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == SPARKLE_EVENT && level != null && level.isClientSide) {
            var random = level.random;
            FXDispatcher.INSTANCE.blockRunes(
                    worldPosition.getX() + 0.25 + random.nextFloat() / 2.0,
                    worldPosition.getY() + 1 + random.nextFloat() / 2.0,
                    worldPosition.getZ() - 0.25 + random.nextFloat() / 2.0,
                    1.5F, 0.0F, 0.4F, 20, 0.0F);
            return true;
        }
        return super.triggerEvent(id, param);
    }

    

    @Override
    public boolean isConnectable(Direction face) {
        return face == orientation();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return false;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return isConnectable(face);
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Nullable
    @Override
    public Aspect getSuctionType(Direction face) {
        return face == orientation() ? Aspect.TOOL : null;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return face == orientation() ? SUCTION : 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Nullable
    @Override
    public Aspect getEssentiaType(Direction face) {
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tookLastTick = tag.getBoolean("TookLastTick");
        powered = isDamaged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("TookLastTick", tookLastTick);
    }
}
