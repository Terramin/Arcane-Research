package arcaneresearch.api;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import javax.annotation.Nullable;
public class ArcaneresearchInvHelper {
    public static class InvFilter {
        public boolean igDmg;
        public boolean igNBT;
        public boolean useOre;
        public boolean useMod;
        public boolean relaxedNBT = false;
        public InvFilter(boolean ignoreDamage, boolean ignoreNBT, boolean useOre, boolean useMod) {
            this.igDmg = ignoreDamage;
            this.igNBT = ignoreNBT;
            this.useOre = useOre;
            this.useMod = useMod;
        }
        public InvFilter setRelaxedNBT() {
            this.relaxedNBT = true;
            return this;
        }
        public static final InvFilter STRICT = new InvFilter(false, false, false, false);
        public static final InvFilter BASEORE = new InvFilter(false, false, true, false);
    }
    public static boolean containsMatch(boolean strict, ItemStack[] inputs, java.util.List<ItemStack> targets) {
        for (ItemStack input : inputs) {
            for (ItemStack target : targets) {
                if (strict ? ItemStack.isSameItemSameTags(target, input)
                        : target.is(input.getItem())) {
                    return true;
                }
            }
        }
        return false;
    }
    @Nullable
    public static IItemHandler getItemHandlerAt(Level level, BlockPos pos, Direction side) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            var capability = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side);
            if (capability.isPresent()) {
                return capability.orElse(null);
            }
            if (blockEntity instanceof Container container) {
                return wrapInventory(container, side);
            }
        }
        return null;
    }
    public static IItemHandler wrapInventory(Container inventory, Direction side) {
        if (inventory instanceof WorldlyContainer worldly) {
            return new SidedInvWrapper(worldly, side);
        }
        return new InvWrapper(inventory);
    }
    public static boolean areItemStackTagsEqualRelaxed(ItemStack prime, ItemStack other) {
        if (prime.isEmpty() && other.isEmpty()) {
            return true;
        }
        if (!prime.isEmpty() && !other.isEmpty()) {
            return prime.getTag() == null || compareTagsRelaxed(prime.getTag(), other.getTag());
        }
        return false;
    }
    public static boolean compareTagsRelaxed(CompoundTag prime, CompoundTag other) {
        if (prime == null) return true;
        if (other == null) return false;
        for (String key : prime.getAllKeys()) {
            if (!other.contains(key)) return false;
            if (!prime.get(key).equals(other.get(key))) return false;
        }
        return true;
    }
    public static boolean areItemStacksEqualForCrafting(ItemStack stack0, Object input) {
        if (stack0 == null && input != null) return false;
        if (stack0 != null && input == null) return false;
        if (stack0 == null) return true;
        if (input instanceof Object[]) return true;
        if (input instanceof ItemStack other) {
            boolean nbtMatch = !stack0.hasTag() || areItemStackTagsEqualForCrafting(stack0, other);
            if (!nbtMatch) return false;
            return ItemStack.isSameItem(stack0, other);
        }
        return false;
    }
    public static boolean containsTagMatch(ItemStack input, ItemStack target) {
        for (var tag : input.getTags().toList()) {
            if (target.is(tag)) {
                return true;
            }
        }
        return false;
    }
    public static boolean areItemsEqual(ItemStack s1, ItemStack s2) {
        if (s1.isDamageableItem() && s2.isDamageableItem()) {
            return s1.is(s2.getItem());
        }
        return ItemStack.isSameItemSameTags(s1, s2);
    }
    public static boolean areItemStackTagsEqualForCrafting(ItemStack slotItem, ItemStack recipeItem) {
        if (recipeItem == null || slotItem == null) return false;
        if (recipeItem.hasTag() && !slotItem.hasTag()) return false;
        if (!recipeItem.hasTag()) return true;
        CompoundTag recipeTag = recipeItem.getTag();
        CompoundTag slotTag = slotItem.getTag();
        for (String key : recipeTag.getAllKeys()) {
            if (!slotTag.contains(key)) return false;
            if (!slotTag.get(key).toString().equals(recipeTag.get(key).toString())) {
                return false;
            }
        }
        return true;
    }
    public static ItemStack insertStackAt(Level level, BlockPos pos, Direction side, ItemStack stack, boolean simulate) {
        IItemHandler inventory = getItemHandlerAt(level, pos, side);
        if (inventory != null) {
            return ItemHandlerHelper.insertItemStacked(inventory, stack, simulate);
        }
        return stack;
    }
    public static ItemStack hasRoomFor(Level level, BlockPos pos, Direction side, ItemStack stack) {
        ItemStack testStack = insertStackAt(level, pos, side, stack.copy(), true);
        if (testStack.isEmpty()) {
            return stack.copy();
        }
        ItemStack result = stack.copy();
        result.setCount(stack.getCount() - testStack.getCount());
        return result;
    }
    public static boolean hasRoomForSome(Level level, BlockPos pos, Direction side, ItemStack stack) {
        ItemStack testStack = insertStackAt(level, pos, side, stack.copy(), true);
        return stack.isEmpty() || testStack.getCount() != stack.getCount();
    }
    public static boolean hasRoomForAll(Level level, BlockPos pos, Direction side, ItemStack stack) {
        return insertStackAt(level, pos, side, stack.copy(), true).isEmpty();
    }
    public static int countTotalItemsIn(IItemHandler inventory, ItemStack stack, InvFilter filter) {
        int count = 0;
        if (inventory != null) {
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack slotStack = inventory.getStackInSlot(slot);
                if (areItemStacksEqualWithFilter(stack, slotStack, filter)) {
                    count += slotStack.getCount();
                }
            }
        }
        return count;
    }
    public static int countTotalItemsIn(Level level, BlockPos pos, Direction side, ItemStack stack, InvFilter filter) {
        return countTotalItemsIn(getItemHandlerAt(level, pos, side), stack, filter);
    }
    public static boolean areItemStacksEqualWithFilter(ItemStack stack0, ItemStack stack1, InvFilter filter) {
        if (stack0 == null && stack1 != null) return false;
        if (stack0 != null && stack1 == null) return false;
        if (stack0 == null) return true;
        if (stack0.isEmpty() && !stack1.isEmpty()) return false;
        if (!stack0.isEmpty() && stack1.isEmpty()) return false;
        if (stack0.isEmpty()) return true;
        if (filter.useMod) {
            String mod0 = stack0.getItem().builtInRegistryHolder().key().location().getNamespace();
            String mod1 = stack1.getItem().builtInRegistryHolder().key().location().getNamespace();
            return mod0.equals(mod1);
        }
        if (filter.useOre) {
            if (containsTagMatch(stack0, stack1)) {
                return true;
            }
        }
        boolean nbtMatch = true;
        if (!filter.igNBT) {
            if (filter.relaxedNBT) {
                nbtMatch = areItemStackTagsEqualRelaxed(stack0, stack1);
            } else {
                nbtMatch = ItemStack.isSameItemSameTags(stack0, stack1) ||
                           (!stack0.hasTag() && !stack1.hasTag());
            }
        }
        boolean damageMatch = filter.igDmg ||
                stack0.getDamageValue() == 32767 || stack1.getDamageValue() == 32767 ||
                stack0.getDamageValue() == stack1.getDamageValue() ||
                !stack0.isDamageableItem();
        return stack0.is(stack1.getItem()) && damageMatch && nbtMatch;
    }
}
