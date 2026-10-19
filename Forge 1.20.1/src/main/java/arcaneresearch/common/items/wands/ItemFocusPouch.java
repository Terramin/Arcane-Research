package arcaneresearch.common.items.wands;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.wands.ItemFocusBasic;
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
    public boolean isFoil(ItemStack stack) {
        return true;
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            final InteractionHand usedHand = hand;
            net.minecraftforge.network.NetworkHooks.openScreen(serverPlayer,
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
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
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
        if (stack.hasTag()) {
            ListTag inv = stack.getTag().getList("Inventory", Tag.TAG_COMPOUND);
            for (int i = 0; i < inv.size(); i++) {
                CompoundTag slotTag = inv.getCompound(i);
                int slot = slotTag.getByte("Slot") & 0xFF;
                if (slot >= 0 && slot < list.size()) {
                    list.set(slot, ItemStack.of(slotTag));
                }
            }
        }
        return list;
    }
    public void setInventory(ItemStack stack, NonNullList<ItemStack> list) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.remove("Inventory");
        ListTag inv = new ListTag();
        for (int i = 0; i < list.size(); i++) {
            ItemStack slot = list.get(i);
            if (!slot.isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                slotTag.putByte("Slot", (byte) i);
                slot.save(slotTag);
                inv.add(slotTag);
            }
        }
        tag.put("Inventory", inv);
    }
    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.UNCOMMON;
    }
    @Override
    @Nullable
    public CompoundTag getShareTag(ItemStack stack) {
        return stack.getTag();
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
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
