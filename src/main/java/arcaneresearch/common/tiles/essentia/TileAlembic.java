package arcaneresearch.common.tiles.essentia;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IAspectContainer;
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
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;

public class TileAlembic extends TileArcaneresearch implements IAspectContainer, IEssentiaTransport {

    public static final int MAX_AMOUNT = 128;
    private static final Direction[] DIRECTIONS = Direction.values();

    public Aspect aspect = null;
    public Aspect aspectFilter = null;
    public int amount = 0;
    public int facing = Direction.DOWN.ordinal();
    public boolean aboveFurnace = false;

    @Nullable
    private AABB renderBoundingBox;

    public TileAlembic(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileAlembic(BlockPos pos, BlockState state) {
        this(ModBlockEntities.ALEMBIC.get(), pos, state);
    }

    public static boolean processAlembics(Level level, BlockPos pos, Aspect aspect) {
        for (int deep = 1; ; deep++) {
            BlockEntity be = level.getBlockEntity(pos.above(deep));
            if (!(be instanceof TileAlembic alembic)) break;
            if (alembic.amount > 0 && alembic.aspect == aspect && alembic.addToContainer(aspect, 1) == 0) {
                return true;
            }
        }
        for (int deep = 1; ; deep++) {
            BlockEntity be = level.getBlockEntity(pos.above(deep));
            if (!(be instanceof TileAlembic alembic)) break;
            if (alembic.amount == 0
                    && (alembic.aspectFilter == null || alembic.aspectFilter == aspect)
                    && alembic.addToContainer(aspect, 1) == 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        if (aspect != null) {
            tag.putString("aspect", aspect.getTag());
        }
        if (aspectFilter != null) {
            tag.putString("AspectFilter", aspectFilter.getTag());
        }
        tag.putInt("amount", amount);
        tag.putInt("facing", facing);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        aspect = Aspect.getAspect(tag.contains("aspect") ? tag.getString("aspect") : tag.getString("Aspect"));
        aspectFilter = Aspect.getAspect(tag.getString("AspectFilter"));
        amount = tag.contains("amount") ? tag.getInt("amount") : tag.getInt("Amount");
        if (amount < 0) amount = 0;
        if (amount > MAX_AMOUNT) amount = MAX_AMOUNT;
        if (amount == 0) aspect = null;
        facing = tag.contains("facing") ? tag.getInt("facing") : tag.getInt("Facing");
        if (facing < 0 || facing >= DIRECTIONS.length) facing = Direction.DOWN.ordinal();
    }

    @Override
    public AspectList getAspects() {
        return (aspect != null && amount > 0) ? new AspectList().add(aspect, amount) : new AspectList();
    }

    @Override
    public void setAspects(AspectList aspects) {
    }

    @Override
    public int addToContainer(Aspect tag, int amt) {
        if (amt <= 0 || tag == null) return amt;
        if (aspectFilter != null && tag != aspectFilter) return amt;
        if (!((amount < MAX_AMOUNT && tag == aspect) || amount == 0)) return amt;
        aspect = tag;
        int added = Math.min(amt, MAX_AMOUNT - amount);
        amount += added;
        markDirtyAndSync();
        return amt - added;
    }

    @Override
    public boolean takeFromContainer(Aspect tag, int amt) {
        if (amt <= 0 || tag == null || aspect == null || amount < amt || tag != aspect) return false;
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
        if (list == null || amount <= 0 || aspect == null) return false;
        return list.getAmount(aspect) > 0;
    }

    @Override
    public int containerContains(Aspect tag) {
        return (tag == aspect) ? amount : 0;
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return tag != null;
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face != null && face != DIRECTIONS[facing] && face != Direction.DOWN;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return false;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face != null && face != DIRECTIONS[facing] && face != Direction.DOWN;
    }

    @Override
    public void setSuction(Aspect aspect, int amount) {
    }

    @Override
    public Aspect getSuctionType(Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return 0;
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
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    public Aspect getAspect() {
        return aspect;
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
        int clamped = (facing >= 0 && facing < DIRECTIONS.length) ? facing : Direction.DOWN.ordinal();
        if (this.facing != clamped) {
            this.facing = clamped;
            markDirtyAndSync();
        }
    }

    public AABB getCustomRenderBoundingBox() {
        AABB bb = renderBoundingBox;
        if (bb == null) {
            bb = new AABB(
                    worldPosition.getX() - 0.1, worldPosition.getY() - 0.1, worldPosition.getZ() - 0.1,
                    worldPosition.getX() + 1.1, worldPosition.getY() + 1.1, worldPosition.getZ() + 1.1);
            renderBoundingBox = bb;
        }
        return bb;
    }
}