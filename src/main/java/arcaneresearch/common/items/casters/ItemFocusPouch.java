package arcaneresearch.common.items.casters;

import arcaneresearch.common.menu.FocusPouchMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;

public class ItemFocusPouch extends Item implements ICurioItem {

    public static final int INVENTORY_SIZE = 18;
    private static final String BELT_SLOT = "belt";

    public ItemFocusPouch() {
        super(new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean clientSide = level.isClientSide();

        if (!clientSide && player instanceof ServerPlayer serverPlayer) {
            final InteractionHand usedHand = hand;
            NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
                @Override
                public @NotNull Component getDisplayName() {
                    return Component.translatable("container.arcaneresearch.focus_pouch");
                }

                @Override
                public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player menuPlayer) {
                    return new FocusPouchMenu(containerId, playerInventory, usedHand);
                }
            }, (FriendlyByteBuf buf) -> buf.writeEnum(usedHand));
        }

        return InteractionResultHolder.sidedSuccess(stack, clientSide);
    }

    public NonNullList<ItemStack> getInventory(ItemStack item) {
        NonNullList<ItemStack> stackList = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        if (item == null || !item.hasTag()) {
            return stackList;
        }
        assert item.getTag() != null;
        ContainerHelper.loadAllItems(item.getTag(), stackList);
        return stackList;
    }

    public void setInventory(ItemStack item, NonNullList<ItemStack> stackList) {
        if (item == null || stackList == null) {
            return;
        }
        CompoundTag tag = item.getOrCreateTag();
        ContainerHelper.saveAllItems(tag, stackList);
    }

    public int getFociCount(ItemStack item) {
        if (item == null) {
            return 0;
        }

        NonNullList<ItemStack> inv = getInventory(item);
        int count = 0;
        for (ItemStack stack : inv) {
            if (stack != null && !stack.isEmpty() && stack.getItem() instanceof ItemFocus) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    @Nullable
    public CompoundTag getShareTag(ItemStack stack) {
        return stack == null ? null : stack.getTag();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        int count = getFociCount(stack);
        if (count > 0) {
            tooltip.add(Component.translatable("item.arcaneresearch.focus_pouch.contents", count, INVENTORY_SIZE)
                    .withStyle(style -> style.withColor(0x808080)));
        } else {
            tooltip.add(Component.translatable("item.arcaneresearch.focus_pouch.empty")
                    .withStyle(style -> style.withColor(0x808080)));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        if (slotContext == null) {
            return false;
        }
        return BELT_SLOT.equals(slotContext.identifier());
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return false;
    }
}