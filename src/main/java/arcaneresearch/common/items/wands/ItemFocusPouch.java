package arcaneresearch.common.items.wands;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import arcaneresearch.api.wands.ItemFocusBasic;
import arcaneresearch.common.menu.WandFocusPouchMenu;
import arcaneresearch.common.menu.WandMenuTypes;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;

public class ItemFocusPouch extends Item implements ICurioItem {

    public static final int SLOTS = 18;

    public ItemFocusPouch() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new net.minecraft.world.MenuProvider() {
                @Override
                public @NotNull Component getDisplayName() {
                    return Component.translatable("container.arcaneresearch.wand_focus_pouch");
                }

                @Override
                public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player menuPlayer) {
                    return new WandFocusPouchMenu(
                            WandMenuTypes.WAND_FOCUS_POUCH.get(),
                            containerId, playerInventory);
                }
            }, buf -> buf.writeEnum(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
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
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return list;
        }
        ListTag inv = tag.getList("Inventory", Tag.TAG_COMPOUND);
        for (int i = 0; i < inv.size(); i++) {
            CompoundTag slotTag = inv.getCompound(i);
            int slot = slotTag.getByte("Slot") & 0xFF;
            if (slot < list.size()) {
                list.set(slot, ItemStack.of(slotTag));
            }
        }
        return list;
    }

    public void setInventory(ItemStack stack, NonNullList<ItemStack> list) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.remove("Inventory");
        ListTag inv = new ListTag();
        for (int i = 0; i < list.size() && i < SLOTS; i++) {
            ItemStack slot = list.get(i);
            if (!slot.isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                slotTag.putByte("Slot", (byte) i);
                slot.save(slotTag);
                inv.add(slotTag);
            }
        }
        if (!inv.isEmpty()) {
            tag.put("Inventory", inv);
        }
    }

    @Override
    public @NotNull Rarity getRarity(@NotNull ItemStack stack) {
        return Rarity.UNCOMMON;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        int count = getFociCount(stack);
        if (count > 0) {
            tooltip.add(Component.translatable("item.arcaneresearch.wand_focus_pouch.contents", count, SLOTS)
                    .withStyle(style -> style.withColor(0x808080)));
        } else {
            tooltip.add(Component.translatable("item.arcaneresearch.wand_focus_pouch.empty")
                    .withStyle(style -> style.withColor(0x808080)));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}