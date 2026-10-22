package arcaneresearch.common.golems.seals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.api.golems.seals.ISeal;
import arcaneresearch.api.golems.seals.ISealConfigFilter;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.api.golems.seals.ISealGui;

public abstract class SealFiltered implements ISeal, ISealGui, ISealConfigFilter {

    protected NonNullList<ItemStack> filter;
    protected NonNullList<Integer> filterSize;
    protected boolean blacklist;

    public SealFiltered() {
        filter = NonNullList.withSize(getFilterSize(), ItemStack.EMPTY);
        filterSize = NonNullList.withSize(getFilterSize(), 0);
        blacklist = true;
    }

    @Override
    public void readCustomNBT(CompoundTag nbt) {
        filter = NonNullList.withSize(getFilterSize(), ItemStack.EMPTY);
        
        
        
        ListTag items = nbt.getListOrEmpty("Items");
        for (int i = 0; i < items.size(); i++) {
            CompoundTag slotTag = items.getCompoundOrEmpty(i);
            int slot = slotTag.getByteOr("Slot", (byte) 0) & 0xFF;
            if (slot >= 0 && slot < filter.size() && slotTag.contains("Item")) {
                filter.set(slot, ItemStack.CODEC
                        .parse(SealHandler.nbtOps(), slotTag.get("Item"))
                        .result().orElse(ItemStack.EMPTY));
            }
        }

        for (ItemStack s : filter) {
            if (s.getCount() > 1) {
                s.setCount(1);
            }
        }

        blacklist = nbt.getBooleanOr("bl", false);

        filterSize = NonNullList.withSize(getFilterSize(), 0);
        ListTag sizeList = nbt.getListOrEmpty("Sizes");
        for (int i = 0; i < sizeList.size(); i++) {
            CompoundTag sizeTag = sizeList.getCompoundOrEmpty(i);
            int slot = sizeTag.getByteOr("Slot", (byte) 0) & 0xFF;
            if (slot >= 0 && slot < filterSize.size()) {
                filterSize.set(slot, sizeTag.getIntOr("Size", 0));
            }
        }
    }

    @Override
    public void writeCustomNBT(CompoundTag nbt) {
        
        ListTag items = new ListTag();
        for (int i = 0; i < filter.size(); i++) {
            ItemStack stack = filter.get(i);
            if (!stack.isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                slotTag.putByte("Slot", (byte) i);
                ItemStack.CODEC.encodeStart(SealHandler.nbtOps(), stack)
                        .result().ifPresent(encoded -> slotTag.put("Item", encoded));
                items.add(slotTag);
            }
        }
        nbt.put("Items", items);
        nbt.putBoolean("bl", blacklist);

        ListTag sizeList = new ListTag();
        for (int i = 0; i < filterSize.size(); i++) {
            int size = filterSize.get(i);
            if (size != 0) {
                CompoundTag sizeTag = new CompoundTag();
                sizeTag.putByte("Slot", (byte) i);
                sizeTag.putInt("Size", size);
                sizeList.add(sizeTag);
            }
        }
        nbt.put("Sizes", sizeList);
    }

    @Override
    public Object returnContainer(Level level, Player player, BlockPos pos, Direction side, ISealEntity seal) {
        return null;
    }

    @Override
    public Object returnGui(Level level, Player player, BlockPos pos, Direction side, ISealEntity seal) {
        return null;
    }

    public int[] getGuiCategories() {
        return new int[]{0};
    }

    @Override
    public int getFilterSize() {
        return 1;
    }

    @Override
    public NonNullList<ItemStack> getInv() {
        return filter;
    }

    @Override
    public NonNullList<Integer> getSizes() {
        return filterSize;
    }

    @Override
    public ItemStack getFilterSlot(int i) {
        return filter.get(i);
    }

    @Override
    public int getFilterSlotSize(int i) {
        return filterSize.get(i);
    }

    @Override
    public void setFilterSlot(int i, ItemStack stack) {
        filter.set(i, stack.copy());
    }

    @Override
    public void setFilterSlotSize(int i, int size) {
        filterSize.set(i, size);
    }

    @Override
    public boolean isBlacklist() {
        return blacklist;
    }

    @Override
    public void setBlacklist(boolean black) {
        blacklist = black;
    }

    @Override
    public boolean hasStacksizeLimiters() {
        return false;
    }

    protected arcaneresearch.api.ArcaneresearchInvHelper.InvFilter invFilter(
            arcaneresearch.api.golems.seals.ISealConfigToggles.SealToggle[] props) {
        boolean pmeta = props.length > 0 ? props[0].getValue() : true;
        boolean pnbt = props.length > 1 ? props[1].getValue() : true;
        boolean pore = props.length > 2 ? props[2].getValue() : false;
        boolean pmod = props.length > 3 ? props[3].getValue() : false;
        return new arcaneresearch.api.ArcaneresearchInvHelper.InvFilter(!pmeta, !pnbt, pore, pmod);
    }
}