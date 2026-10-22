package thaumictinker.kami.trinkets;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

 



 
public class BlackHoleRingItem extends Item implements ICurioItem {

    public BlackHoleRingItem(Properties properties) {
        super(properties);
    }

    public static Item getItem(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("TalismanBlock", Tag.TAG_STRING)) {
            return Items.AIR;
        }
        return BlockStorage.resolve(new net.minecraft.resources.ResourceLocation(tag.getString("TalismanBlock")));
    }

    public static int getCount(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("TalismanCount", Tag.TAG_INT) ? Math.max(0, tag.getInt("TalismanCount")) : 0;
    }

    public static boolean isActive(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean("RingActive");
    }

    public static void setActive(ItemStack stack, boolean active) {
        if (active) {
            stack.getOrCreateTag().putBoolean("RingActive", true);
        } else {
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                tag.remove("RingActive");
            }
        }
    }

    private static void setCount(ItemStack stack, int count) {
        CompoundTag tag = stack.getOrCreateTag();
        if (count <= 0) {
            tag.remove("TalismanCount");
            if (tag.isEmpty() && getItem(stack) == Items.AIR) {
                stack.setTag((CompoundTag) null);
            }
        } else {
            tag.putInt("TalismanCount", count);
        }
    }

    public static int remove(ItemStack stack, int count) {
        int current = getCount(stack);
        int removed = (int) Math.min((long) current, Math.max((long) count, 0L));
        setCount(stack, current - removed);
        return removed;
    }

    public static int add(ItemStack stack, int count) {
        int current = getCount(stack);
        int added = (int) Math.min((long) Integer.MAX_VALUE - current, Math.max((long) count, 0L));
        setCount(stack, current + added);
        return added;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (getItem(stack) != Items.AIR && player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                setActive(stack, !isActive(stack));
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3F, 0.1F);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Item stored = getItem(stack);
        if (stored == Items.AIR || getCount(stack) == 0) {
            Item blockItem = state.getBlock().asItem();
            if (blockItem == Items.AIR) {
                return InteractionResult.PASS;
            }
            stack.getOrCreateTag().putString("TalismanBlock",
                    BlockStorage.idOf(blockItem).toString());
            setCount(stack, 0);
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof Container container) {
            return insert(stack, stored, container, context.getClickedFace())
                    ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        ItemStack single = new ItemStack(stored);
        UseOnContext placement = new UseOnContext(level, player, context.getHand(), single,
                new BlockHitResult(context.getClickLocation(), context.getClickedFace(), pos, context.isInside()));
        InteractionResult result = single.useOn(placement);
        if (result.consumesAction()) {
            remove(stack, 1);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static boolean canPlace(Container container, int slot, ItemStack candidate, Direction face) {
        if (!container.canPlaceItem(slot, candidate)) {
            return false;
        }
        return !(container instanceof WorldlyContainer worldly)
                || worldly.canPlaceItemThroughFace(slot, candidate, face);
    }

    private static boolean insert(ItemStack ring, Item stored, Container container, Direction face) {
        int[] slots;
        if (container instanceof WorldlyContainer worldly) {
            slots = worldly.getSlotsForFace(face);
        } else {
            slots = new int[container.getContainerSize()];
            for (int index = 0; index < slots.length; index++) {
                slots[index] = index;
            }
        }
        boolean changed = false;
        for (int slot : slots) {
            int available = getCount(ring);
            if (available <= 0) {
                break;
            }
            ItemStack inSlot = container.getItem(slot);
            if (inSlot.isEmpty()) {
                int limit = Math.min(stored.getMaxStackSize(), container.getMaxStackSize());
                int move = Math.min(limit, available);
                if (move > 0) {
                    ItemStack placed = new ItemStack(stored, move);
                    if (canPlace(container, slot, placed, face)) {
                        remove(ring, move);
                        container.setItem(slot, placed);
                        container.setChanged();
                        changed = true;
                    }
                }
            } else if (BlockStorage.matches(inSlot, stored)) {
                int limit = Math.min(inSlot.getMaxStackSize(), container.getMaxStackSize());
                int move = Math.min(limit - inSlot.getCount(), available);
                if (move > 0 && canPlace(container, slot, inSlot, face)) {
                    inSlot.grow(remove(ring, move));
                    container.setChanged();
                    changed = true;
                }
            }
        }
        return changed;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, java.util.List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        Item stored = getItem(stack);
        if (stored != Items.AIR) {
            tooltip.add(Component.empty().append(new ItemStack(stored).getHoverName())
                    .append(" (x" + getCount(stack) + ")"));
        }
        tooltip.add(Component.translatable(isActive(stack)
                ? "ttinkerer.ttmisc.active" : "ttinkerer.ttmisc.inactive"));
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return slotContext.identifier().equals("ring");
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity wearer = slotContext.entity();
        if (!(wearer instanceof ServerPlayer player) || !isActive(stack)) {
            return;
        }
        Item item = getItem(stack);
        if (item == Items.AIR) {
            return;
        }
        absorb(stack, item, player);
    }

    public static void absorb(ItemStack ring, Item item, ServerPlayer player) {
        Inventory inventory = player.getInventory();
        int size = BlockStorage.INVENTORY_SIZE;
        int highest = -1;
        boolean hasFreeSlot = false;
        int[] counts = new int[size];
        for (int slot = 0; slot < size; slot++) {
            ItemStack inSlot = inventory.getItem(slot);
            if (inSlot.isEmpty()) {
                hasFreeSlot = true;
                continue;
            }
            if (BlockStorage.matches(inSlot, item)) {
                counts[slot] = inSlot.getCount();
                if (highest == -1) {
                    highest = slot;
                } else {
                    highest = counts[slot] > counts[highest] && highest > 8 ? slot : highest;
                }
            }
        }

        if (highest == -1) {
            ItemStack carried = player.containerMenu.getCarried();
            if (hasFreeSlot && (carried.isEmpty() || !BlockStorage.matches(carried, item))) {
                int amount = remove(ring, item.getMaxStackSize());
                if (amount > 0) {
                    ItemStack given = new ItemStack(item, amount);
                    inventory.add(given);
                    if (!given.isEmpty()) {
                        add(ring, given.getCount());
                    }
                }
            }
            return;
        }

        for (int slot = 0; slot < size; slot++) {
            int count = counts[slot];
            if (slot == highest || count == 0) {
                continue;
            }
            if ((long) getCount(ring) + count <= Integer.MAX_VALUE) {
                add(ring, count);
                inventory.setItem(slot, ItemStack.EMPTY);
            }
        }

        ItemStack inHighest = inventory.getItem(highest);
        int missing = inHighest.getMaxStackSize() - inHighest.getCount();
        if (missing > 0) {
            inHighest.grow(remove(ring, missing));
        }
    }
}
