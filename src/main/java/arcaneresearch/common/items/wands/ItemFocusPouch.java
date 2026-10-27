package arcaneresearch.common.items.wands;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.wands.ItemFocusBasic;
import arcaneresearch.common.menu.WandFocusPouchMenu;
import arcaneresearch.common.menu.WandMenuTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class ItemFocusPouch extends Item implements ICurioItem {

    public static final int SLOTS = 18;

    public ItemFocusPouch(Properties props) {

        super(props.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public @NonNull InteractionResult use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(
                    new net.minecraft.world.MenuProvider() {
                        @Override
                        public @NotNull Component getDisplayName() {
                            return Component.translatable("container.arcaneresearch.wand_focus_pouch");
                        }

                        @Override
                        public AbstractContainerMenu createMenu(int containerId,
                                                                @NotNull Inventory playerInventory,
                                                                @NotNull Player menuPlayer) {
                            return new WandFocusPouchMenu(
                                    WandMenuTypes.WAND_FOCUS_POUCH.get(),
                                    containerId, playerInventory);
                        }
                    },
                    buf -> InteractionHand.STREAM_CODEC.encode(buf, hand));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return "belt".equals(slotContext.identifier());
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {

        return false;
    }

    public int getFociCount(ItemStack stack) {
        int count = 0;
        for (ItemStack s : getInventory(stack)) {
            if (!s.isEmpty() && s.getItem() instanceof ItemFocusBasic) {
                count++;
            }
        }
        return count;
    }

    public NonNullList<ItemStack> getInventory(ItemStack stack) {
        NonNullList<ItemStack> list = NonNullList.withSize(SLOTS, ItemStack.EMPTY);

        CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
        if (root == null) {
            return list;
        }
        ListTag inv = root.getListOrEmpty("Inventory");
        for (int i = 0; i < inv.size(); i++) {
            CompoundTag slotTag = inv.getCompoundOrEmpty(i);
            int slot = slotTag.getByteOr("Slot", (byte) 0) & 0xFF;
            if (slot < list.size()) {
                ItemStack s = ItemStack.CODEC.parse(arcaneresearch.common.lib.utils.RegistryNbt.ops(), slotTag)
                        .result().orElse(ItemStack.EMPTY);
                list.set(slot, s);
            }
        }
        return list;
    }

    public void setInventory(ItemStack stack, NonNullList<ItemStack> list) {
        CompoundTag root = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
        root.remove("Inventory");
        ListTag inv = new ListTag();
        for (int i = 0; i < list.size() && i < SLOTS; i++) {
            ItemStack slot = list.get(i);
            if (!slot.isEmpty()) {
                final int slotIndex = i;
                ItemStack.CODEC.encodeStart(arcaneresearch.common.lib.utils.RegistryNbt.ops(), slot.copy()).result().ifPresent(encoded -> {
                    if (encoded instanceof CompoundTag slotTag) {
                        slotTag.putByte("Slot", (byte) slotIndex);
                        inv.add(slotTag);
                    }
                });
            }
        }
        if (!inv.isEmpty()) {
            root.put("Inventory", inv);
        }
        ArcaneresearchInvHelper.setCustomTag(stack, root);
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (event.getItemStack()==stack) {
            int count = getFociCount(event.getItemStack());
            if (count > 0) {
                event.getToolTip().add(Component.translatable("item.arcaneresearch.wand_focus_pouch.contents", count, SLOTS)
                        .withStyle(style -> style.withColor(0x808080)));
            } else {
                event.getToolTip().add(Component.translatable("item.arcaneresearch.wand_focus_pouch.empty")
                        .withStyle(style -> style.withColor(0x808080)));
            }
        }
    }
}