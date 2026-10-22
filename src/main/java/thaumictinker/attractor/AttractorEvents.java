package thaumictinker.attractor;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


 


 
@Mod.EventBusSubscriber(modid = thaumictinker.TtCore.MODID)
public final class AttractorEvents {

    private AttractorEvents() {
    }

    @SubscribeEvent
    public static void soulMouldPriority(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof SoulMouldItem && event.getTarget() instanceof LivingEntity) {
            InteractionResult result = stack.interactLivingEntity(event.getEntity(), (LivingEntity) event.getTarget(),
                    event.getHand());
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }
}
