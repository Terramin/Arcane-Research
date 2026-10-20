package arcaneresearch.common.items.baubles;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.items.IVisDiscountGear;
import arcaneresearch.api.items.IWarpingGear;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.function.Consumer;

public class ItemVoidseerCharm extends Item implements ICurioItem, IVisDiscountGear, IWarpingGear {

    public ItemVoidseerCharm(Item.Properties props) {
        super(props
                .stacksTo(1)
                .rarity(Rarity.RARE));
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return slotContext.identifier().equals("charm");
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public int getVisDiscount(ItemStack stack, Player player) {

        int permanentWarp = getPlayerPermanentWarp(player);
        permanentWarp = Math.min(100, permanentWarp);
        return (int) (permanentWarp / 100.0f * 25.0f);
    }

    @Override
    public int getWarp(ItemStack stack, Player player) {

        return getVisDiscount(stack, player) / 5;
    }

    private int getPlayerPermanentWarp(Player player) {
        IPlayerWarp warp = ArcaneresearchCapabilities.getWarp(player);
        if (warp != null) {
            return warp.get(IPlayerWarp.EnumWarpType.PERMANENT);
        }
        return 0;
    }

    // NEOFORGE_PORT: appendHoverText 5-arg (TooltipContext, TooltipDisplay, Consumer<Component>).
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {

        tooltip.accept(Component.translatable("item.arcaneresearch.voidseer_charm.text")
                .withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.ITALIC));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}
