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

public class TileJar extends TileArcaneresearch implements IAspectSource, IEssentiaTransport {

    public static final int CAPACITY = 250;

    protected Aspect aspect = null;
    protected Aspect aspectFilter = null;
    protected int amount = 0;
    protected int facing = 2;
    protected boolean blocked = false;
    protected int tickCount = 0;

    public TileJar(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileJar(BlockPos pos, BlockState state) {
        this(ModBlockEntities.JAR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileJar tile) {
        if (++tile.tickCount % 5 == 0 && tile.amount < CAPACITY) {
            tile.fillFromAbove();
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
        tag.putInt("facing", facing);
        tag.putBoolean("blocked", blocked);
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
        facing = tag.contains("facing") ? tag.getInt("facing") : tag.getByte("facing");
        if (facing < 0) facing = 0;
        blocked = tag.getBoolean("blocked");
    }

    protected void fillFromAbove() {
        if (level == null || level.isClientSide) return;
        BlockEntity be = level.getBlockEntity(worldPosition.above());
        if (!(be instanceof IEssentiaTransport transport)) return;
        if (!transport.canOutputTo(Direction.DOWN)) return;

        int ourSuction = getSuctionAmount(Direction.UP);
        int theirSuction = transport.getSuctionAmount(Direction.DOWN);

        Aspect toGet = null;
        if (aspectFilter != null) {
            toGet = aspectFilter;
        } else if (aspect != null && amount > 0) {
            toGet = aspect;
        } else if (transport.getEssentiaAmount(Direction.DOWN) > 0
                && theirSuction < ourSuction
                && ourSuction >= transport.getMinimumSuction()) {
            toGet = transport.getEssentiaType(Direction.DOWN);
        }

        if (toGet != null && theirSuction < ourSuction) {
            int taken = transport.takeEssentia(toGet, 1, Direction.DOWN);
            if (taken > 0) {
                addToContainer(toGet, taken);
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
        if (sorted.length > 0 && sorted[0] != null) {
            Aspect a = sorted[0];
            int amt = aspects.getAmount(a);
            if (this.aspect != a || this.amount != amt) {
                this.aspect = a;
                this.amount = Math.min(Math.max(amt, 0), CAPACITY);
                markDirtyAndSync();
            }
        }
    }

    @Override
    public int addToContainer(Aspect tag, int amt) {
        if (amt <= 0 || tag == null) return amt;
        int before = amount;
        if (amount < CAPACITY && tag == aspect || amount == 0) {
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
        return face == Direction.UP;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face == Direction.UP;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face == Direction.UP;
    }

    @Override
    public void setSuction(Aspect aspect, int amount) {
    }

    @Override
    public int getMinimumSuction() {
        return (aspectFilter != null) ? 64 : 32;
    }

    @Override
    public Aspect getSuctionType(Direction face) {
        return (aspectFilter != null) ? aspectFilter : aspect;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        if (amount >= CAPACITY) return 0;
        return (aspectFilter != null) ? 64 : 32;
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
        if (!canOutputTo(face) || amount <= 0) return 0;
        return takeFromContainer(aspect, amount) ? amount : 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        if (!canInputFrom(face) || amount <= 0) return 0;
        return amount - addToContainer(aspect, amount);
    }

    @Override
    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        if (this.blocked != blocked) {
            this.blocked = blocked;
            markDirtyAndSync();
        }
    }

    public Aspect getAspect() {
        return aspect;
    }

    public void setAspect(Aspect aspect, int amount) {
        Aspect newAspect = (aspect == null || amount <= 0) ? null : aspect;
        int newAmount = newAspect == null ? 0 : Math.min(amount, CAPACITY);
        if (this.aspect != newAspect || this.amount != newAmount) {
            this.aspect = newAspect;
            this.amount = newAmount;
            markDirtyAndSync();
        }
    }

    public int getAmount() {
        return amount;
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