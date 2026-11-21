package arcaneresearch.common.items.curios;

import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.api.items.IVisDiscountGear;
import arcaneresearch.api.items.IWarpingGear;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class VoidseerCharmItem extends EquippableCurioItem implements IVisDiscountGear, IWarpingGear {
    public VoidseerCharmItem(Item.Properties props) {
        super(props
                .rarity(Rarity.RARE), CurioSlot.CHARM);
    }

    @Override
    public int getVisDiscount(ItemStack stack, Player player) {
        int permanentWarp = Math.min(100, getPlayerPermanentWarp(player));
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

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.arcaneresearch.voidseer_charm.text")
                .withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.ITALIC));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}
