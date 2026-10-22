package arcaneresearch.common.items.wands;

import net.minecraft.nbt.NbtOps;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.wands.ItemFocusBasic;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.function.Consumer;

public class ItemFocusPouch extends Item implements ICurioItem {
    public static final int SLOTS = 18;

    public ItemFocusPouch(Properties props) {
        
        super(props.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    
    
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            final InteractionHand usedHand = hand;
            serverPlayer.openMenu(
                    new net.minecraft.world.MenuProvider() {
                        @Override
                        public Component getDisplayName() {
                            return Component.translatable("container.arcaneresearch.wand_focus_pouch");
                        }

                        @Override
                        public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int containerId,
                                                                                              net.minecraft.world.entity.player.Inventory playerInventory,
                                                                                              Player menuPlayer) {
                            return new arcaneresearch.common.menu.WandFocusPouchMenu(
                                    arcaneresearch.common.menu.WandMenuTypes.WAND_FOCUS_POUCH.get(),
                                    containerId, playerInventory);
                        }
                    },
                    buf -> buf.writeEnum(usedHand));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return slotContext.identifier().equals("belt");
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
            if (slot >= 0 && slot < list.size()) {
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
        for (int i = 0; i < list.size(); i++) {
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
        root.put("Inventory", inv);
        ArcaneresearchInvHelper.setCustomTag(stack, root);
    }

    
    

    
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        int count = getFociCount(stack);
        if (count > 0) {
            tooltip.accept(Component.translatable("item.arcaneresearch.wand_focus_pouch.contents", count, SLOTS)
                    .withStyle(style -> style.withColor(0x808080)));
        } else {
            tooltip.accept(Component.translatable("item.arcaneresearch.wand_focus_pouch.empty")
                    .withStyle(style -> style.withColor(0x808080)));
        }
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}