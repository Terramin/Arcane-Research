package arcaneresearch.common.tiles.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IAspectContainer;
import arcaneresearch.api.aspects.IEssentiaTransport;
import arcaneresearch.common.menu.ThaumatoriumMenu;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;

import javax.annotation.Nullable;

public class TileThaumatoriumTop extends TileArcaneresearch implements IAspectContainer, IEssentiaTransport, WorldlyContainer, MenuProvider {

    @Nullable
    public TileThaumatorium thaumatorium = null;

    public TileThaumatoriumTop(BlockPos pos, BlockState state) {
        super(ModBlockEntities.THAUMATORIUM_TOP.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileThaumatoriumTop tile) {
        if (tile.thaumatorium == null) {
            BlockEntity below = level.getBlockEntity(pos.below());
            if (below instanceof TileThaumatorium t) tile.thaumatorium = t;
        }
    }

    @Nullable
    public TileThaumatorium getThaumatorium() {
        if (thaumatorium != null && !thaumatorium.isRemoved()) return thaumatorium;
        if (level != null) {
            BlockEntity below = level.getBlockEntity(worldPosition.below());
            if (below instanceof TileThaumatorium t) {
                thaumatorium = t;
                return t;
            }
        }
        thaumatorium = null;
        return null;
    }

    @Override
    public AspectList getAspects() {
        TileThaumatorium t = getThaumatorium();
        return t != null ? t.essentia : null;
    }

    @Override
    public void setAspects(AspectList aspects) {
        TileThaumatorium t = getThaumatorium();
        if (t != null) t.setAspects(aspects);
    }

    @Override
    public int addToContainer(Aspect tt, int am) {
        TileThaumatorium t = getThaumatorium();
        return t == null ? am : t.addToContainer(tt, am);
    }

    @Override
    public boolean takeFromContainer(Aspect tt, int am) {
        TileThaumatorium t = getThaumatorium();
        return t != null && t.takeFromContainer(tt, am);
    }

    @Override
    @Deprecated
    public boolean takeFromContainer(AspectList ot) {
        return false;
    }

    @Override
    @Deprecated
    public boolean doesContainerContain(AspectList ot) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tt, int am) {
        TileThaumatorium t = getThaumatorium();
        return t != null && t.doesContainerContainAmount(tt, am);
    }

    @Override
    public int containerContains(Aspect tt) {
        TileThaumatorium t = getThaumatorium();
        return t == null ? 0 : t.containerContains(tt);
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return true;
    }

    @Override
    public boolean isConnectable(Direction face) {
        TileThaumatorium t = getThaumatorium();
        return t != null && t.isConnectable(face);
    }

    @Override
    public boolean canInputFrom(Direction face) {
        TileThaumatorium t = getThaumatorium();
        return t != null && t.canInputFrom(face);
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(Aspect aspect, int amount) {
        TileThaumatorium t = getThaumatorium();
        if (t != null) t.setSuction(aspect, amount);
    }

    @Override
    public Aspect getSuctionType(Direction loc) {
        TileThaumatorium t = getThaumatorium();
        return t == null ? null : t.getSuctionType(loc);
    }

    @Override
    public int getSuctionAmount(Direction loc) {
        TileThaumatorium t = getThaumatorium();
        return t == null ? 0 : t.getSuctionAmount(loc);
    }

    @Override
    public Aspect getEssentiaType(Direction loc) {
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction loc) {
        return 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        TileThaumatorium t = getThaumatorium();
        return t == null ? 0 : t.takeEssentia(aspect, amount, face);
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        TileThaumatorium t = getThaumatorium();
        return t == null ? 0 : t.addEssentia(aspect, amount, face);
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean isBlocked() {
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        TileThaumatorium t = getThaumatorium();
        return t == null || t.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        TileThaumatorium t = getThaumatorium();
        return t == null ? ItemStack.EMPTY : t.getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        TileThaumatorium t = getThaumatorium();
        return t == null ? ItemStack.EMPTY : t.removeItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        TileThaumatorium t = getThaumatorium();
        return t == null ? ItemStack.EMPTY : t.removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        TileThaumatorium t = getThaumatorium();
        if (t != null) t.setItem(slot, stack);
    }

    @Override
    public void setChanged() {
        super.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) return false;
        return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        TileThaumatorium t = getThaumatorium();
        if (t != null) t.clearContent();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.arcaneresearch.thaumatorium");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        TileThaumatorium t = getThaumatorium();
        if (t != null) return new ThaumatoriumMenu(containerId, playerInventory, t);
        return null;
    }
}
