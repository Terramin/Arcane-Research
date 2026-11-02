package arcaneresearch.common.lib.utils;

import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.items.ItemsTC;
import arcaneresearch.common.entities.EntityFollowingItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class InventoryUtils {

    private static final Direction[] DIRECTIONS = Direction.values();

    public static ItemStack copyMaxedStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return ItemStack.EMPTY;
        return copyLimitedStack(stack, stack.getMaxStackSize());
    }

    public static ItemStack copyLimitedStack(ItemStack stack, int limit) {
        if (stack == null || stack.isEmpty()) return ItemStack.EMPTY;
        int max = Math.max(1, limit);
        ItemStack s = stack.copy();
        if (s.getCount() > max) {
            s.setCount(max);
        }
        return s;
    }

    public static boolean consumeItemsFromAdjacentInventoryOrPlayer(Level level, BlockPos pos, Player player,
                                                                    boolean simulate, ItemStack... items) {
        if (items == null) return true;

        for (ItemStack stack : items) {
            if (stack == null || stack.isEmpty()) continue;
            boolean found = checkAdjacentChests(level, pos, stack);
            if (!found) found = isPlayerCarryingAmount(player, stack, true);
            if (!found) return false;
        }
        if (simulate) return true;

        for (ItemStack stack : items) {
            if (stack == null || stack.isEmpty()) continue;
            if (!consumeFromAdjacentChests(level, pos, stack.copy())) {
                consumePlayerItem(player, stack, true, true);
            }
        }
        return true;
    }

    public static boolean checkAdjacentChests(Level level, BlockPos pos, ItemStack itemStack) {
        if (level == null || pos == null || itemStack == null || itemStack.isEmpty()) return false;
        int needed = itemStack.getCount();
        for (Direction face : DIRECTIONS) {
            if (face == Direction.UP) continue;
            needed -= ArcaneresearchInvHelper.countTotalItemsIn(level, pos.relative(face),
                    face.getOpposite(), itemStack.copy(), ArcaneresearchInvHelper.InvFilter.BASEORE);
            if (needed <= 0) return true;
        }
        return false;
    }

    public static boolean consumeFromAdjacentChests(Level level, BlockPos pos, ItemStack itemStack) {
        if (level == null || pos == null || itemStack == null || itemStack.isEmpty()) return false;
        for (Direction face : DIRECTIONS) {
            if (face == Direction.UP || itemStack.isEmpty()) continue;
            ItemStack removed = removeStackFrom(level, pos.relative(face), face.getOpposite(),
                    itemStack, ArcaneresearchInvHelper.InvFilter.BASEORE, false);
            itemStack.shrink(removed.getCount());
        }
        return itemStack.isEmpty();
    }

    public static void ejectStackAt(Level level, BlockPos pos, Direction side, ItemStack out) {
        ejectStackAt(level, pos, side, out, false);
    }

    public static ItemStack ejectStackAt(Level level, BlockPos pos, Direction side, ItemStack out, boolean smart) {
        if (level == null || pos == null || side == null || out == null || out.isEmpty()) return out;

        BlockPos targetPos = pos.relative(side);
        Direction opposite = side.getOpposite();
        out = ArcaneresearchInvHelper.insertStackAt(level, targetPos, opposite, out, false);

        if (smart && ArcaneresearchInvHelper.getItemHandlerAt(level, targetPos, opposite) != null) {
            return out;
        }

        if (!out.isEmpty()) {
            BlockPos spawnPos = level.getBlockState(targetPos).isCollisionShapeFullBlock(level, targetPos)
                    ? pos.relative(opposite)
                    : pos;

            double x = spawnPos.getX() + 0.5 + side.getStepX();
            double y = spawnPos.getY() + side.getStepY();
            double z = spawnPos.getZ() + 0.5 + side.getStepZ();
            ItemEntity entityItem = new ItemEntity(level, x, y, z, out);
            entityItem.setDeltaMovement(
                    0.3 * side.getStepX(),
                    0.3 * side.getStepY(),
                    0.3 * side.getStepZ()
            );
            level.addFreshEntity(entityItem);
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack removeStackFrom(Level level, BlockPos pos, Direction side, ItemStack stack,
                                            ArcaneresearchInvHelper.InvFilter filter, boolean simulate) {
        if (level == null) return ItemStack.EMPTY;
        return removeStackFrom(ArcaneresearchInvHelper.getItemHandlerAt(level, pos, side), stack, filter, simulate);
    }

    public static ItemStack removeStackFrom(@Nullable IItemHandler inventory, ItemStack stack,
                                            ArcaneresearchInvHelper.InvFilter filter, boolean simulate) {
        if (stack == null || stack.isEmpty() || inventory == null) return ItemStack.EMPTY;

        int amount = stack.getCount();
        int removed = 0;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack slotStack = inventory.getStackInSlot(slot);
            if (areItemStacksEqual(stack, slotStack, filter)) {
                int toExtract = Math.min(amount - removed, slotStack.getCount());
                if (toExtract <= 0) continue;
                ItemStack extracted = inventory.extractItem(slot, toExtract, simulate);
                if (!extracted.isEmpty()) {
                    removed += extracted.getCount();
                }
            }
            if (removed >= amount) break;
        }
        if (removed == 0) return ItemStack.EMPTY;

        ItemStack result = stack.copy();
        result.setCount(removed);
        return result;
    }

    public static int countStackInWorld(Level level, BlockPos pos, ItemStack stack, double range,
                                        ArcaneresearchInvHelper.InvFilter filter) {
        if (level == null || stack == null || stack.isEmpty()) return 0;

        int count = 0;
        List<ItemEntity> entities = EntityUtils.getEntitiesInRange(level, pos, range, ItemEntity.class);
        if (entities == null) return 0;

        for (ItemEntity ei : entities) {
            ItemStack itemStack = ei.getItem();
            if (itemStack != null && !itemStack.isEmpty() && areItemStacksEqual(stack, itemStack, filter)) {
                count += itemStack.getCount();
            }
        }
        return count;
    }

    public static void dropItems(Level level, BlockPos pos) {
        if (level == null || pos == null) return;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof Container container) {
            Containers.dropContents(level, pos, container);
        }
    }

    public static boolean consumePlayerItem(Player player, ItemStack item, boolean nocheck, boolean useTagMatch) {
        if (player == null || item == null || item.isEmpty()) return false;
        if (!nocheck && !isPlayerCarryingAmount(player, item, useTagMatch)) return false;

        ArcaneresearchInvHelper.InvFilter filter = new ArcaneresearchInvHelper.InvFilter(false, !item.hasTag(), useTagMatch, false)
                .setRelaxedNBT();

        int count = item.getCount();
        NonNullList<ItemStack> invItems = player.getInventory().items;
        for (int slot = 0; slot < invItems.size(); slot++) {
            ItemStack slotStack = invItems.get(slot);
            if (slotStack == null || slotStack.isEmpty()) continue;

            if (checkEnchantedPlaceholder(item, slotStack) || areItemStacksEqual(slotStack, item, filter)) {
                if (slotStack.getCount() > count) {
                    slotStack.shrink(count);
                } else {
                    count -= slotStack.getCount();
                    invItems.set(slot, ItemStack.EMPTY);
                }
                if (count <= 0) return true;
            }
        }
        return false;
    }

    public static boolean consumePlayerItem(Player player, Item item, int amount) {
        if (player == null || item == null || amount <= 0) return false;
        if (!isPlayerCarryingAmount(player, new ItemStack(item, amount), false)) return false;

        int remaining = amount;
        NonNullList<ItemStack> invItems = player.getInventory().items;
        for (int slot = 0; slot < invItems.size(); slot++) {
            ItemStack slotStack = invItems.get(slot);
            if (slotStack == null || !slotStack.is(item)) continue;

            if (slotStack.getCount() > remaining) {
                slotStack.shrink(remaining);
                remaining = 0;
            } else {
                remaining -= slotStack.getCount();
                invItems.set(slot, ItemStack.EMPTY);
            }
            if (remaining <= 0) return true;
        }
        return false;
    }

    public static boolean isPlayerCarryingAmount(Player player, ItemStack stack, boolean useTagMatch) {
        if (player == null || stack == null || stack.isEmpty()) return false;

        int needed = stack.getCount();
        ArcaneresearchInvHelper.InvFilter filter = new ArcaneresearchInvHelper.InvFilter(false, !stack.hasTag(), useTagMatch, false)
                .setRelaxedNBT();
        NonNullList<ItemStack> invItems = player.getInventory().items;
        for (int slot = 0; slot < invItems.size(); slot++) {
            ItemStack slotStack = invItems.get(slot);
            if (slotStack == null || slotStack.isEmpty()) continue;
            if (checkEnchantedPlaceholder(stack, slotStack) || areItemStacksEqual(slotStack, stack, filter)) {
                needed -= slotStack.getCount();
                if (needed <= 0) return true;
            }
        }
        return false;
    }

    @Nullable
    public static EquipmentSlot isHoldingItem(Player player, Item item) {
        if (player == null || item == null) return null;
        if (!player.getMainHandItem().isEmpty() && player.getMainHandItem().is(item)) {
            return EquipmentSlot.MAINHAND;
        }
        if (!player.getOffhandItem().isEmpty() && player.getOffhandItem().is(item)) {
            return EquipmentSlot.OFFHAND;
        }
        return null;
    }

    @Nullable
    public static EquipmentSlot isHoldingItem(Player player, Class<?> itemClass) {
        if (player == null || itemClass == null) return null;
        if (!player.getMainHandItem().isEmpty() && itemClass.isAssignableFrom(player.getMainHandItem().getItem().getClass())) {
            return EquipmentSlot.MAINHAND;
        }
        if (!player.getOffhandItem().isEmpty() && itemClass.isAssignableFrom(player.getOffhandItem().getItem().getClass())) {
            return EquipmentSlot.OFFHAND;
        }
        return null;
    }

    public static int getPlayerSlotFor(Player player, ItemStack stack) {
        if (player == null || stack == null || stack.isEmpty()) return -1;
        NonNullList<ItemStack> invItems = player.getInventory().items;
        for (int i = 0; i < invItems.size(); i++) {
            ItemStack slotStack = invItems.get(i);
            if (slotStack != null && !slotStack.isEmpty() && ItemStack.isSameItemSameTags(stack, slotStack)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean stackEqualExact(ItemStack stack1, ItemStack stack2) {
        return ItemStack.isSameItemSameTags(stack1, stack2);
    }

    public static boolean areItemStacksEqualStrict(ItemStack stack0, ItemStack stack1) {
        return areItemStacksEqual(stack0, stack1, ArcaneresearchInvHelper.InvFilter.STRICT);
    }

    public static boolean checkEnchantedPlaceholder(ItemStack required, ItemStack inv) {
        if (ItemsTC.enchantedPlaceholder == null || required == null) return false;
        if (required.getItem() != ItemsTC.enchantedPlaceholder) return false;
        if (inv == null || inv.isEmpty()) return false;

        Map<Enchantment, Integer> req = EnchantmentHelper.getEnchantments(required);
        if (req.isEmpty()) return false;

        Map<Enchantment, Integer> has = EnchantmentHelper.getEnchantments(inv);
        if (has.isEmpty()) return false;

        for (Map.Entry<Enchantment, Integer> e : req.entrySet()) {
            Integer level = has.get(e.getKey());
            if (level == null) return false;
            if (level < e.getValue()) return false;
        }
        return true;
    }

    public static boolean areItemStacksEqual(ItemStack stack0, ItemStack stack1, ArcaneresearchInvHelper.InvFilter filter) {
        if (stack0 == null && stack1 == null) return true;
        if (stack0 == null || stack1 == null) return false;

        boolean empty0 = stack0.isEmpty();
        boolean empty1 = stack1.isEmpty();
        if (empty0 != empty1) return false;
        if (empty0) return true;

        if (filter == null) filter = ArcaneresearchInvHelper.InvFilter.STRICT;

        if (filter.useMod) {
            String mod0 = stack0.getItem().builtInRegistryHolder().key().location().getNamespace();
            String mod1 = stack1.getItem().builtInRegistryHolder().key().location().getNamespace();
            return mod0.equals(mod1);
        }

        if (filter.useOre) {
            for (var tag : stack0.getTags().toList()) {
                if (stack1.is(tag)) return true;
            }
        }

        boolean nbtMatch = true;
        if (!filter.igNBT) {
            if (filter.relaxedNBT) {
                nbtMatch = ArcaneresearchInvHelper.areItemStackTagsEqualRelaxed(stack0, stack1);
            } else {
                nbtMatch = ItemStack.isSameItemSameTags(stack0, stack1)
                        || (!stack0.hasTag() && !stack1.hasTag());
            }
        }

        boolean damageMatch = filter.igDmg
                || stack0.getDamageValue() == stack1.getDamageValue()
                || !stack0.isDamageableItem();

        return stack0.is(stack1.getItem()) && damageMatch && nbtMatch;
    }

    public static ItemStack findFirstMatchFromFilter(NonNullList<ItemStack> filterStacks, boolean blacklist,
                                                     IItemHandler inv, Direction face, ArcaneresearchInvHelper.InvFilter filter) {
        return findFirstMatchFromFilter(filterStacks, blacklist, inv, face, filter, false);
    }

    public static ItemStack findFirstMatchFromFilter(NonNullList<ItemStack> filterStacks, boolean blacklist,
                                                     IItemHandler inv, Direction face, ArcaneresearchInvHelper.InvFilter filter, boolean leaveOne) {
        if (inv == null || filterStacks == null) return ItemStack.EMPTY;

        slotLoop:
        for (int slot = 0; slot < inv.getSlots(); slot++) {
            ItemStack slotStack = inv.getStackInSlot(slot);
            if (slotStack == null || slotStack.isEmpty() || slotStack.getCount() <= 0) continue;

            if (leaveOne && ArcaneresearchInvHelper.countTotalItemsIn(inv, slotStack, filter) < 2) continue;

            boolean allow = false;
            boolean allEmpty = true;
            for (ItemStack filterStack : filterStacks) {
                if (filterStack == null || filterStack.isEmpty()) continue;
                allEmpty = false;

                boolean matches = areItemStacksEqual(filterStack, slotStack, filter);
                if (blacklist) {
                    if (matches) continue slotLoop;
                    allow = true;
                } else if (matches) {
                    return slotStack;
                }
            }
            if (blacklist && (allow || allEmpty)) {
                return slotStack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean matchesFilters(NonNullList<ItemStack> filterList, boolean blacklist,
                                         ItemStack stack, ArcaneresearchInvHelper.InvFilter filter) {
        if (stack == null || stack.isEmpty() || filterList == null) return false;

        boolean allow = false;
        boolean allEmpty = true;
        for (ItemStack filterStack : filterList) {
            if (filterStack == null || filterStack.isEmpty()) continue;
            allEmpty = false;

            boolean matches = areItemStacksEqual(filterStack, stack, filter);
            if (blacklist) {
                if (matches) return false;
                allow = true;
            } else if (matches) {
                return true;
            }
        }
        return blacklist && (allow || allEmpty);
    }

    public static Tuple<ItemStack, Integer> findFirstMatchFromFilterTuple(NonNullList<ItemStack> filterStacks,
                                                                          NonNullList<Integer> filterStacksSizes, boolean blacklist,
                                                                          NonNullList<ItemStack> stacks,
                                                                          ArcaneresearchInvHelper.InvFilter filter) {
        if (stacks == null || filterStacks == null || filterStacksSizes == null) {
            return new Tuple<>(ItemStack.EMPTY, 0);
        }
        int sizeLimit = Math.min(filterStacks.size(), filterStacksSizes.size());

        outer:
        for (ItemStack is : stacks) {
            if (is == null || is.isEmpty() || is.getCount() <= 0) continue;

            boolean allow = false;
            boolean allEmpty = true;
            for (int idx = 0; idx < sizeLimit; idx++) {
                ItemStack fs = filterStacks.get(idx);
                if (fs == null || fs.isEmpty()) continue;

                allEmpty = false;
                boolean r = areItemStacksEqual(fs, is, filter);
                if (blacklist) {
                    if (!r) {
                        allow = true;
                    } else {
                        continue outer;
                    }
                } else if (r) {
                    return new Tuple<>(is, filterStacksSizes.get(idx));
                }
            }
            if (blacklist && (allow || allEmpty)) {
                return new Tuple<>(is, 0);
            }
        }
        return new Tuple<>(ItemStack.EMPTY, 0);
    }

    public static void dropHarvestsAtPos(Level level, BlockPos pos, List<ItemStack> items) {
        dropHarvestsAtPos(level, pos, items, false, 0, null);
    }

    public static void dropHarvestsAtPos(Level level, BlockPos pos, List<ItemStack> items,
                                         boolean followItem, int color, @Nullable Entity target) {
        if (level == null || level.isClientSide() || items == null) return;
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) return;

        for (ItemStack item : items) {
            if (item == null || item.isEmpty()) continue;

            float spread = 0.5f;
            double x = pos.getX() + level.random.nextFloat() * spread + (1.0f - spread) * 0.5;
            double y = pos.getY() + level.random.nextFloat() * spread + (1.0f - spread) * 0.5;
            double z = pos.getZ() + level.random.nextFloat() * spread + (1.0f - spread) * 0.5;

            ItemEntity entityItem = (followItem && target != null)
                    ? new EntityFollowingItem(level, x, y, z, item, target, 0)
                    : new ItemEntity(level, x, y, z, item);
            entityItem.setDefaultPickUpDelay();
            level.addFreshEntity(entityItem);
        }
    }

    public static void dropItemAtPos(Level level, ItemStack item, BlockPos pos) {
        if (level == null || pos == null || item == null || item.isEmpty()) return;
        if (level.isClientSide()) return;

        ItemEntity entityItem = new ItemEntity(level,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, item.copy());
        level.addFreshEntity(entityItem);
    }

    public static void dropItemAtEntity(Level level, ItemStack item, Entity entity) {
        if (level == null || entity == null || item == null || item.isEmpty()) return;
        if (level.isClientSide()) return;

        ItemEntity entityItem = new ItemEntity(level,
                entity.getX(), entity.getY() + entity.getEyeHeight() / 2.0f, entity.getZ(),
                item.copy());
        level.addFreshEntity(entityItem);
    }

    public static void dropItemsAtEntity(Level level, BlockPos pos, Entity entity) {
        if (level == null || pos == null || entity == null || level.isClientSide()) return;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof Container container)) return;

        double x = entity.getX();
        double y = entity.getY() + entity.getEyeHeight() / 2.0f;
        double z = entity.getZ();

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack item = container.getItem(i);
            if (item == null || item.isEmpty()) continue;

            ItemEntity entityItem = new ItemEntity(level, x, y, z, item.copy());
            level.addFreshEntity(entityItem);
            container.setItem(i, ItemStack.EMPTY);
        }
    }

    public static ItemStack cycleItemStack(Object input) {
        return cycleItemStack(input, 0);
    }

    public static ItemStack cycleItemStack(Object input, int counter) {
        if (input == null) return ItemStack.EMPTY;

        long timeSec = System.currentTimeMillis() / 1000L;

        if (input instanceof Ingredient ingredient) {
            ItemStack[] stacks = ingredient.getItems();
            if (stacks.length > 0) {
                int idx = (int) ((counter + timeSec) % stacks.length);
                return cycleItemStack(stacks[idx], counter + 1);
            }
        } else if (input instanceof ItemStack[] stacks) {
            if (stacks.length > 0) {
                int idx = (int) ((counter + timeSec) % stacks.length);
                return cycleItemStack(stacks[idx], counter + 1);
            }
        } else if (input instanceof ItemStack stack) {
            if (stack.isEmpty() || !stack.isDamageableItem()) return stack;

            int maxDamage = stack.getMaxDamage();
            if (maxDamage <= 0) return stack;

            int interval = Math.max(1, 5000 / maxDamage);
            long damageL = (counter + System.currentTimeMillis() / interval) % maxDamage;
            ItemStack cycled = stack.copy();
            cycled.setDamageValue((int) damageL);
            return cycled;
        } else if (input instanceof List<?> list) {
            if (list.isEmpty()) return ItemStack.EMPTY;
            Object first = list.get(0);
            if (!(first instanceof ItemStack)) return ItemStack.EMPTY;

            int idx = (int) ((counter + timeSec) % list.size());
            Object picked = list.get(idx);
            if (!(picked instanceof ItemStack)) return ItemStack.EMPTY;
            return cycleItemStack(picked, counter + 1);
        } else if (input instanceof TagKey<?> tag) {
            @SuppressWarnings("unchecked")
            TagKey<Item> itemTag = (TagKey<Item>) tag;
            var items = BuiltInRegistries.ITEM.getTag(itemTag);
            if (items.isPresent()) {
                var list = items.get().stream().toList();
                if (!list.isEmpty()) {
                    int idx = (int) ((counter + timeSec) % list.size());
                    return new ItemStack(list.get(idx).value());
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Nullable
    public static IItemHandler getItemHandler(Level level, BlockPos pos, Direction side) {
        if (level == null || pos == null) return null;
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null) {
            return be.getCapability(ForgeCapabilities.ITEM_HANDLER, side).orElse(null);
        }
        return null;
    }
}