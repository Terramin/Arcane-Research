package arcaneresearch.common.tiles.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IAspectContainer;
import arcaneresearch.api.aspects.IEssentiaTransport;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileAlembic extends TileArcaneresearch implements IAspectContainer, IEssentiaTransport {

    public static final int MAX_AMOUNT = 128;

    public Aspect aspect = null;
    public Aspect aspectFilter = null;
    public int amount = 0;
    public int facing = Direction.DOWN.ordinal();
    public boolean aboveFurnace = false;

    private Direction facingDir = null;

    public TileAlembic(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileAlembic(BlockPos pos, BlockState state) {
        this(ModBlockEntities.ALEMBIC.get(), pos, state);
    }

    public static boolean processAlembics(Level level, BlockPos pos, Aspect aspect) {

        int deep = 1;
        while (true) {
            BlockEntity be = level.getBlockEntity(pos.above(deep));
            if (be instanceof TileAlembic alembic) {
                if (alembic.amount > 0 && alembic.aspect == aspect && alembic.addToContainer(aspect, 1) == 0) {
                    return true;
                }
                deep++;
            } else {
                break;
            }
        }

        deep = 1;
        while (true) {
            BlockEntity be = level.getBlockEntity(pos.above(deep));
            if (be instanceof TileAlembic alembic) {
                if ((alembic.aspectFilter == null || alembic.aspectFilter == aspect) && alembic.addToContainer(aspect, 1) == 0) {
                    return true;
                }
                deep++;
            } else {
                break;
            }
        }

        return false;
    }

    @Override
    protected void writeSyncNBT(ValueOutput tag) {
        super.writeSyncNBT(tag);

        if (aspect != null) {
            tag.putString("aspect", aspect.getTag());
        }
        if (aspectFilter != null) {
            tag.putString("AspectFilter", aspectFilter.getTag());
        }
        tag.putShort("amount", (short) amount);
        tag.putByte("facing", (byte) facing);
    }

    @Override
    protected void readSyncNBT(ValueInput tag) {
        super.readSyncNBT(tag);
        aspect = Aspect.getAspect(tag.getString("aspect").isPresent() ? tag.getStringOr("aspect", "") : tag.getStringOr("Aspect", ""));
        aspectFilter = Aspect.getAspect(tag.getStringOr("AspectFilter", ""));
        amount = tag.read("amount", com.mojang.serialization.Codec.SHORT).isPresent() ? tag.getShortOr("amount", (short) 0) : tag.getShortOr("Amount", (short) 0);
        facing = tag.read("facing", com.mojang.serialization.Codec.BYTE).isPresent() ? tag.getByteOr("facing", (byte) 0) : tag.getByteOr("Facing", (byte) 0);
        facingDir = Direction.values()[facing];
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
        if (aspectFilter != null && tag != aspectFilter) {
            return amt;
        }
        if ((amount < MAX_AMOUNT && tag == aspect) || amount == 0) {
            aspect = tag;
            int added = Math.min(amt, MAX_AMOUNT - amount);
            amount += added;
            amt -= added;
        }
        markDirtyAndSync();
        return amt;
    }

    @Override
    public boolean takeFromContainer(Aspect tag, int amt) {
        if (amount == 0 || aspect == null) {
            aspect = null;
            amount = 0;
        }
        if (aspect != null && amount >= amt && tag == aspect) {
            amount -= amt;
            if (amount <= 0) {
                aspect = null;
                amount = 0;
            }
            markDirtyAndSync();
            return true;
        }
        return false;
    }

    @Override
    public boolean takeFromContainer(AspectList list) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tag, int amt) {
        return amount >= amt && tag == aspect;
    }

    @Override
    public boolean doesContainerContain(AspectList list) {
        return amount > 0 && aspect != null && list.getAmount(aspect) > 0;
    }

    @Override
    public int containerContains(Aspect tag) {
        return (tag == aspect) ? amount : 0;
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {

        return true;
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face != Direction.values()[facing] && face != Direction.DOWN;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return false;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face != Direction.values()[facing] && face != Direction.DOWN;
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
        return (canOutputTo(face) && takeFromContainer(aspect, amount)) ? amount : 0;
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
        this.aspectFilter = filter;
        markDirtyAndSync();
    }

    public int getFacing() {
        return facing;
    }

    public void setFacing(int facing) {
        this.facing = facing;
        this.facingDir = Direction.values()[facing];
        markDirtyAndSync();
    }

    public AABB getCustomRenderBoundingBox() {
        return new AABB(
                worldPosition.getX() - 0.1, worldPosition.getY() - 0.1, worldPosition.getZ() - 0.1,
                worldPosition.getX() + 1.1, worldPosition.getY() + 1.1, worldPosition.getZ() + 1.1
        );
    }
}
