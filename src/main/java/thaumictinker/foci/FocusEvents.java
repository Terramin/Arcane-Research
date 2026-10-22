package thaumictinker.foci;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import arcaneresearch.api.wands.IWand;

import thaumictinker.TtCore;

 



 
@Mod.EventBusSubscriber(modid = TtCore.MODID)
public final class FocusEvents {

    private FocusEvents() {
    }

    @SubscribeEvent
    public static void onStopUsing(LivingEntityUseItemEvent.Stop event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        ItemStack stack = event.getItem();
        if (!(stack.getItem() instanceof IWand wand)) {
            return;
        }
        if (wand.getFocus(stack) instanceof HealFocus) {
            HealFocus.reset(player);
        }
        if (wand.getFocus(stack) instanceof SmeltFocus) {
            SmeltFocus.clearState(player.getUUID());
        }
    }
}
