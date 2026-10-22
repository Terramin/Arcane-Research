package arcaneresearch.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.capabilities.Capabilities; 
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;

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
                if (strict ? ItemStack.isSameItemSameComponents(target, input)
                        : target.is(input.getItem())) {
                    return true;
                }
            }
        }
        return false;
    }

    
    
    
    
    
    @Nullable
    public static ResourceHandler<ItemResource> getItemHandlerAt(Level level, BlockPos pos, Direction side) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            var handler = level.getCapability(Capabilities.Item.BLOCK, pos, side);
            if (handler != null) {
                return handler;
            }

            if (blockEntity instanceof Container container) {
                return wrapInventory(container, side);
            }
        }
        return null;
    }

    public static ResourceHandler<ItemResource> wrapInventory(Container inventory, Direction side) {
        
        
        
        if (side != null && inventory instanceof WorldlyContainer worldly) {
            return new WorldlyContainerWrapper(worldly, side);
        }
        return VanillaContainerWrapper.of(inventory);
    }

    
    
    
    
    
    
    @Nullable
    public static CompoundTag getCustomTag(ItemStack stack) {
        if (stack.isEmpty()) return null;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? null : data.copyTag();
    }

    public static boolean hasCustomTag(ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_DATA);
    }

    
    
    public static CompoundTag getOrCreateCustomTag(ItemStack stack) {
        CompoundTag tag = getCustomTag(stack);
        return tag != null ? tag : new CompoundTag();
    }

    public static void setCustomTag(ItemStack stack, CompoundTag tag) {
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
    }

    public static boolean areItemStackTagsEqualRelaxed(ItemStack prime, ItemStack other) {
        if (prime.isEmpty() && other.isEmpty()) {
            return true;
        }
        if (!prime.isEmpty() && !other.isEmpty()) {
            return prime.getComponentsPatch().isEmpty() || compareComponentsRelaxed(prime, other);
        }
        return false;
    }

    
    
    
    private static boolean compareComponentsRelaxed(ItemStack prime, ItemStack other) {
        for (net.minecraft.core.component.DataComponentType<?> type : prime.getComponentsPatch().keySet()) {
            if (type == DataComponents.CUSTOM_DATA) {
                if (!compareTagsRelaxed(getCustomTag(prime), getCustomTag(other))) return false;
            } else if (!java.util.Objects.equals(prime.get(type), other.get(type))) {
                return false;
            }
        }
        return true;
    }

    public static boolean compareTagsRelaxed(CompoundTag prime, CompoundTag other) {
        if (prime == null) return true;
        if (other == null) return false;

        for (String key : prime.keySet()) {
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

            boolean nbtMatch = !hasCustomTag(stack0) || areItemStackTagsEqualForCrafting(stack0, other);
            if (!nbtMatch) return false;

            return ItemStack.isSameItem(stack0, other);
        }

        return false;
    }

    public static boolean containsTagMatch(ItemStack input, ItemStack target) {
        
        for (var tag : input.typeHolder().tags().toList()) {
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
        return ItemStack.isSameItemSameComponents(s1, s2);
    }

    public static boolean areItemStackTagsEqualForCrafting(ItemStack slotItem, ItemStack recipeItem) {
        if (recipeItem == null || slotItem == null) return false;
        if (hasCustomTag(recipeItem) && !hasCustomTag(slotItem)) return false;
        if (!hasCustomTag(recipeItem)) return true;

        CompoundTag recipeTag = getCustomTag(recipeItem);
        CompoundTag slotTag = getCustomTag(slotItem);

        for (String key : recipeTag.keySet()) {
            if (!slotTag.contains(key)) return false;
            if (!slotTag.get(key).toString().equals(recipeTag.get(key).toString())) {
                return false;
            }
        }
        return true;
    }

    public static ItemStack insertStackAt(Level level, BlockPos pos, Direction side, ItemStack stack, boolean simulate) {
        
        
        
        
        
        
        
        
        ResourceHandler<ItemResource> inventory = getItemHandlerAt(level, pos, side);
        if (inventory != null && !stack.isEmpty()) {
            try (var tx = Transaction.openRoot()) {
                int inserted = net.neoforged.neoforge.transfer.ResourceHandlerUtil.insertStacking(
                        inventory, ItemResource.of(stack), stack.getCount(), tx);
                if (!simulate) tx.commit();
                int leftover = stack.getCount() - inserted;
                return leftover == 0 ? ItemStack.EMPTY : stack.copyWithCount(leftover);
            }
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

    public static int countTotalItemsIn(ResourceHandler<ItemResource> inventory, ItemStack stack, InvFilter filter) {
        int count = 0;
        if (inventory != null) {
            for (int slot = 0; slot < inventory.size(); slot++) {
                
                ItemStack slotStack = ItemUtil.getStack(inventory, slot);
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
            
            
            String mod0 = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack0.getItem()).getNamespace();
            String mod1 = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack1.getItem()).getNamespace();
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
                
                
                nbtMatch = ItemStack.isSameItemSameComponents(stack0, stack1) ||
                           (stack0.getComponentsPatch().isEmpty() && stack1.getComponentsPatch().isEmpty());
            }
        }

        boolean damageMatch = filter.igDmg ||
                stack0.getDamageValue() == 32767 || stack1.getDamageValue() == 32767 ||
                stack0.getDamageValue() == stack1.getDamageValue() ||
                !stack0.isDamageableItem();

        return stack0.is(stack1.getItem()) && damageMatch && nbtMatch;
    }
}
