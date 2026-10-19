package arcaneresearch.common.items.casters;

import net.minecraft.nbt.NbtOps;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.common.menu.FocusPouchMenu;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.function.Consumer;

public class ItemFocusPouch extends Item implements ICurioItem {

    public static final int INVENTORY_SIZE = 18;

    public ItemFocusPouch(Item.Properties props) {
        super(props
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON));
    }

    // NEOFORGE_PORT: use() now returns InteractionResult (holder removed);
    // NetworkHooks.openScreen REMOVED -> ServerPlayer.openMenu(provider, extraDataWriter).
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            final InteractionHand usedHand = hand;
            serverPlayer.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("container.arcaneresearch.focus_pouch");
                }

                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player menuPlayer) {
                    return new FocusPouchMenu(containerId, playerInventory, usedHand);
                }
            }, buf -> {
                buf.writeEnum(usedHand);
            });
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.SUCCESS;
    }

    public NonNullList<ItemStack> getInventory(ItemStack item) {
        NonNullList<ItemStack> stackList = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        // NEOFORGE_PORT: ContainerHelper.loadAllItems(CompoundTag, ...) removed in 26.x ->
        // ItemStack.CODEC via NbtOps (same "Items" list + "Slot" entries shape).
        CompoundTag root = ArcaneresearchInvHelper.getCustomTag(item);
        if (root == null) {
            return stackList;
        }
        ListTag items = root.getListOrEmpty("Items");
        for (int i = 0; i < items.size(); i++) {
            CompoundTag slotTag = items.getCompoundOrEmpty(i);
            int slot = slotTag.getByteOr("Slot", (byte) 0) & 0xFF;
            if (slot >= 0 && slot < stackList.size()) {
                ItemStack stack = ItemStack.CODEC.parse(NbtOps.INSTANCE, slotTag)
                        .result().orElse(ItemStack.EMPTY);
                stackList.set(slot, stack);
            }
        }
        return stackList;
    }

    public void setInventory(ItemStack item, NonNullList<ItemStack> stackList) {
        CompoundTag root = ArcaneresearchInvHelper.getOrCreateCustomTag(item);
        ListTag items = new ListTag();
        for (int i = 0; i < stackList.size(); i++) {
            ItemStack stack = stackList.get(i);
            if (!stack.isEmpty()) {
                final int slot = i;
                ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, stack.copy()).result().ifPresent(encoded -> {
                    if (encoded instanceof CompoundTag slotTag) {
                        slotTag.putByte("Slot", (byte) slot);
                        items.add(slotTag);
                    }
                });
            }
        }
        root.put("Items", items);
        ArcaneresearchInvHelper.setCustomTag(item, root);
    }

    public int getFociCount(ItemStack item) {
        NonNullList<ItemStack> inv = getInventory(item);
        int count = 0;
        for (ItemStack stack : inv) {
            if (!stack.isEmpty() && stack.getItem() instanceof ItemFocus) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean isFoil(ItemStack stack) {

        return false;
    }

    // NEOFORGE_PORT: getShareTag REMOVED in 26.x; component sync (CUSTOM_DATA included)
    // is automatic, so no override is needed.

    // NEOFORGE_PORT: appendHoverText 5-arg (TooltipContext, TooltipDisplay, Consumer<Component>).
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        int count = getFociCount(stack);
        if (count > 0) {
            tooltip.accept(Component.translatable("item.arcaneresearch.focus_pouch.contents", count, INVENTORY_SIZE)
                    .withStyle(style -> style.withColor(0x808080)));
        } else {
            tooltip.accept(Component.translatable("item.arcaneresearch.focus_pouch.empty")
                    .withStyle(style -> style.withColor(0x808080)));
        }
        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return slotContext.identifier().equals("belt");
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {

        return false;
    }
}