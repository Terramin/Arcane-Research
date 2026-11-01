package arcaneresearch.common.tiles.essentia;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IAspectSource;
import arcaneresearch.api.aspects.IEssentiaTransport;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TileEssentiaReservoir extends TileArcaneresearch implements IAspectSource, IEssentiaTransport {

    public static final int CAPACITY = 500;

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final Direction[] HORIZONTALS =
            Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);

    protected Aspect aspect = null;
    protected Aspect aspectFilter = null;
    protected int amount = 0;
    protected int facing = 2;

    private int tickCount = 0;

    public TileEssentiaReservoir(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileEssentiaReservoir(BlockPos pos, BlockState state) {
        this(ModBlockEntities.ESSENTIA_RESERVOIR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileEssentiaReservoir tile) {
        if (++tile.tickCount % 5 == 0 && tile.amount < CAPACITY) {
            tile.pullFromConnections();
        }
        if (tile.tickCount % 20 == 0) {
            tile.balanceWithNeighbors();
        }
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        if (aspect != null) {
            tag.putString("Aspect", aspect.getTag());
        }
        if (aspectFilter != null) {
            tag.putString("AspectFilter", aspectFilter.getTag());
        }
        tag.putInt("Amount", amount);
        tag.putInt("Facing", facing);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        aspect = Aspect.getAspect(tag.getString("Aspect"));
        aspectFilter = Aspect.getAspect(tag.getString("AspectFilter"));
        amount = tag.contains("Amount") ? tag.getInt("Amount") : tag.getShort("Amount");
        if (amount < 0) amount = 0;
        if (amount > CAPACITY) amount = CAPACITY;
        if (amount == 0) aspect = null;
        facing = tag.contains("Facing") ? tag.getInt("Facing") : tag.getByte("Facing");
        if (facing < 0) facing = 0;
    }

    private void pullFromConnections() {
        if (level == null || level.isClientSide) return;
        for (Direction dir : DIRECTIONS) {
            if (amount >= CAPACITY) break;
            BlockEntity be = level.getBlockEntity(worldPosition.relative(dir));
            if (!(be instanceof IEssentiaTransport transport)) continue;
            if (!transport.canOutputTo(dir.getOpposite())) continue;

            int ourSuction = getSuctionAmount(dir);
            int theirSuction = transport.getSuctionAmount(dir.getOpposite());

            Aspect toGet = null;
            if (aspectFilter != null) {
                toGet = aspectFilter;
            } else if (aspect != null && amount > 0) {
                toGet = aspect;
            } else if (transport.getEssentiaAmount(dir.getOpposite()) > 0
                    && theirSuction < ourSuction) {
                toGet = transport.getEssentiaType(dir.getOpposite());
            }

            if (toGet != null && theirSuction < ourSuction) {
                int taken = transport.takeEssentia(toGet, 1, dir.getOpposite());
                if (taken > 0) {
                    addToContainer(toGet, taken);
                    break;
                }
            }
        }
    }

    private void balanceWithNeighbors() {
        if (level == null || level.isClientSide || amount <= 0 || aspect == null) return;
        for (Direction dir : HORIZONTALS) {
            BlockEntity be = level.getBlockEntity(worldPosition.relative(dir));
            if (be instanceof TileEssentiaReservoir neighbor) {
                if ((neighbor.aspect == null || neighbor.aspect == aspect)
                        && neighbor.amount < amount - 1
                        && (neighbor.aspectFilter == null || neighbor.aspectFilter == aspect)) {
                    if (takeFromContainer(aspect, 1)) {
                        neighbor.addToContainer(aspect, 1);
                    }
                    return;
                }
            }
        }
    }

    @Override
    public AspectList getAspects() {
        AspectList list = new AspectList();
        if (aspect != null && amount > 0) {
            list.add(aspect, amount);
        }
        return list;
    }

    @Override
    public void setAspects(AspectList aspects) {
        if (aspects == null || aspects.size() <= 0) return;
        Aspect[] sorted = aspects.getAspectsSortedByAmount();
        if (sorted.length == 0 || sorted[0] == null) return;
        Aspect a = sorted[0];
        int amt = aspects.getAmount(a);
        Aspect newAspect = amt <= 0 ? null : a;
        int newAmount = newAspect == null ? 0 : Math.min(amt, CAPACITY);
        if (this.aspect != newAspect || this.amount != newAmount) {
            this.aspect = newAspect;
            this.amount = newAmount;
            markDirtyAndSync();
        }
    }

    @Override
    public int addToContainer(Aspect tag, int amt) {
        if (amt <= 0 || tag == null) return amt;
        if (aspectFilter != null && tag != aspectFilter) {
            return amt;
        }
        int before = amount;
        if ((amount < CAPACITY && tag == aspect) || amount == 0) {
            aspect = tag;
            int added = Math.min(amt, CAPACITY - amount);
            amount += added;
            amt -= added;
        }
        if (amount != before) {
            markDirtyAndSync();
        }
        return amt;
    }

    @Override
    public boolean takeFromContainer(Aspect tag, int amt) {
        if (tag == null || amt <= 0 || amount < amt || tag != aspect) return false;
        amount -= amt;
        if (amount == 0) {
            aspect = null;
        }
        markDirtyAndSync();
        return true;
    }

    @Override
    public boolean takeFromContainer(AspectList list) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tag, int amt) {
        return amt > 0 && amount >= amt && tag == aspect;
    }

    @Override
    public boolean doesContainerContain(AspectList list) {
        if (list == null || amount <= 0) return false;
        for (Aspect a : list.getAspects()) {
            if (a == aspect) return true;
        }
        return false;
    }

    @Override
    public int containerContains(Aspect tag) {
        return (tag == aspect) ? amount : 0;
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        if (tag == null) return false;
        return aspectFilter == null || tag.equals(aspectFilter);
    }

    @Override
    public boolean isConnectable(Direction face) {
        return true;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return true;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return true;
    }

    @Override
    public void setSuction(Aspect aspect, int amount) {
    }

    @Override
    public int getMinimumSuction() {
        return (aspectFilter != null) ? 80 : 48;
    }

    @Override
    public Aspect getSuctionType(Direction face) {
        return (aspectFilter != null) ? aspectFilter : aspect;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        if (amount >= CAPACITY) return 0;
        return (aspectFilter != null) ? 80 : 48;
    }

    @Override
    public Aspect getEssentiaType(Direction face) {
        return aspect;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return amount;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        if (amount <= 0 || !canOutputTo(face)) return 0;
        return takeFromContainer(aspect, amount) ? amount : 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        if (amount <= 0 || !canInputFrom(face)) return 0;
        return amount - addToContainer(aspect, amount);
    }

    @Override
    public boolean isBlocked() {
        return false;
    }

    public Aspect getAspect() {
        return aspect;
    }

    public int getAmount() {
        return amount;
    }

    public float getFillPercent() {
        return (float) amount / CAPACITY;
    }

    public Aspect getAspectFilter() {
        return aspectFilter;
    }

    public void setAspectFilter(Aspect filter) {
        if (this.aspectFilter != filter) {
            this.aspectFilter = filter;
            markDirtyAndSync();
        }
    }

    public int getFacing() {
        return facing;
    }

    public void setFacing(int facing) {
        int clamped = Math.max(0, facing);
        if (this.facing != clamped) {
            this.facing = clamped;
            markDirtyAndSync();
        }
    }
}