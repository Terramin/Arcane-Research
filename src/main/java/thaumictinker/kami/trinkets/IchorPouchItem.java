package thaumictinker.kami.trinkets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import arcaneresearch.common.items.wands.ItemFocusPouch;

 


 
public class IchorPouchItem extends ItemFocusPouch {

    public IchorPouchItem() {
        super();
    }

    @Override
    public net.minecraft.world.item.Rarity getRarity(ItemStack stack) {
        return net.minecraft.world.item.Rarity.EPIC;
    }

    @Override
    public NonNullList<ItemStack> getInventory(ItemStack stack) {
        NonNullList<ItemStack> list = NonNullList.withSize(IchorPouchMenu.POUCH_SLOTS, ItemStack.EMPTY);
        if (stack.hasTag()) {
            ListTag inv = stack.getTag().getList("Inventory", Tag.TAG_COMPOUND);
            for (int index = 0; index < inv.size(); index++) {
                CompoundTag slotTag = inv.getCompound(index);
                int slot = slotTag.getByte("Slot") & 0xFF;
                if (slot >= 0 && slot < list.size()) {
                    list.set(slot, ItemStack.of(slotTag));
                }
            }
        }
        return list;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            MenuProvider provider = new SimpleMenuProvider((containerId, inventory, ignored) ->
                    new IchorPouchMenu(containerId, inventory, player.getItemInHand(hand)),
                    Component.translatable("item.thaumictinker.ichor_pouch"));
            NetworkHooks.openScreen(serverPlayer, provider);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
