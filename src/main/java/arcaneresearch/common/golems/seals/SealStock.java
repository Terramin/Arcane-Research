package arcaneresearch.common.golems.seals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.GolemHelper;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.seals.ISealConfigToggles;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.api.golems.tasks.Task;
import arcaneresearch.common.lib.utils.InventoryUtils;

import java.util.Random;

public class SealStock extends SealFiltered implements ISealConfigToggles {

    protected SealToggle[] props;
    private int delay;
    private Identifier icon;

    public SealStock() {
        delay = new Random(System.nanoTime()).nextInt(50);
        icon = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "item/seals/seal_stock");
        props = new SealToggle[]{
                new SealToggle(true, "pmeta", "golem.prop.meta"),
                new SealToggle(true, "pnbt", "golem.prop.nbt"),
                new SealToggle(false, "pore", "golem.prop.ore"),
                new SealToggle(false, "pmod", "golem.prop.mod")
        };
    }

    @Override
    public String getKey() {
        return "arcaneresearch:stock";
    }

    @Override
    public int getFilterSize() {
        return 9;
    }

    @Override
    public void tickSeal(Level level, ISealEntity seal) {
        if (delay++ % 20 != 0) {
            return;
        }

        ResourceHandler<ItemResource> inv = getItemHandler(level, seal.getSealPos().pos, seal.getSealPos().face);
        if (inv != null) {
            for (int a = 0; a < 9; a++) {
                int amt = ArcaneresearchInvHelper.countTotalItemsIn(inv, getFilterSlot(a), invFilter(props));
                if (amt < getFilterSlotSize(a)) {
                    ItemStack fs = getFilterSlot(a).copy();
                    fs.setCount(Math.min(fs.getMaxStackSize(), getFilterSlotSize(a) - amt));
                    fs = ArcaneresearchInvHelper.hasRoomFor(level, seal.getSealPos().pos, seal.getSealPos().face, fs);
                    if (!fs.isEmpty()) {
                        GolemHelper.requestProvisioning(level, seal.getSealPos().pos, seal.getSealPos().face, fs);
                    }
                }
            }
        }
    }

    private ResourceHandler<ItemResource> getItemHandler(Level level, BlockPos pos, Direction face) {
        return arcaneresearch.api.ArcaneresearchInvHelper.getItemHandlerAt(level, pos, face);
    }

    private int countItemsMatching(ResourceHandler<ItemResource> inv, ItemStack filterStack) {
        return ArcaneresearchInvHelper.countTotalItemsIn(inv, filterStack, invFilter(props));
    }

    private boolean matchesItem(ItemStack stack, ItemStack filter) {
        if (stack.isEmpty() || filter.isEmpty()) return false;
        return InventoryUtils.areItemStacksEqual(filter, stack, invFilter(props));
    }

    @Override
    public void onTaskStarted(Level level, IGolemAPI golem, Task task) {
    }

    @Override
    public boolean onTaskCompletion(Level level, IGolemAPI golem, Task task) {
        return true;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        return false;
    }

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction side) {
        return getItemHandler(level, pos, side) != null;
    }

    @Override
    public Identifier getSealIcon() {
        return icon;
    }

    @Override
    public int[] getGuiCategories() {
        return new int[]{1, 3, 0, 4};
    }

    @Override
    public EnumGolemTrait[] getRequiredTags() {
        return null;
    }

    @Override
    public EnumGolemTrait[] getForbiddenTags() {
        return new EnumGolemTrait[]{EnumGolemTrait.CLUMSY};
    }

    @Override
    public void onTaskSuspension(Level level, Task task) {
    }

    @Override
    public void onRemoval(Level level, BlockPos pos, Direction side) {
    }

    @Override
    public boolean hasStacksizeLimiters() {
        return true;
    }

    @Override
    public boolean isBlacklist() {
        return false;
    }

    @Override
    public Object returnContainer(Level level, Player player, BlockPos pos, Direction side, ISealEntity seal) {
        return null;
    }

    @Override
    public Object returnGui(Level level, Player player, BlockPos pos, Direction side, ISealEntity seal) {
        return null;
    }

    @Override
    public SealToggle[] getToggles() {
        return props;
    }

    @Override
    public void setToggle(int index, boolean value) {
        if (index >= 0 && index < props.length) {
            props[index].setValue(value);
        }
    }
}