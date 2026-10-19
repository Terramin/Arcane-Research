package arcaneresearch.client.events;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.client.lib.events.WandHudHandler;
import arcaneresearch.client.lib.events.WandRenderingHandler;
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class WandClientEvents {
    private static final WandRenderingHandler WAND_HANDLER = new WandRenderingHandler();
    private static final WandHudHandler HUD_HANDLER = new WandHudHandler();
    @SubscribeEvent
    public static void renderOverlay(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        long time = System.nanoTime() / 1000000L;
        WAND_HANDLER.handleFociRadial(mc, time, event);
        ItemStack held = WandRenderingHandler.heldWand(mc.player);
        if (!held.isEmpty()) {
            HUD_HANDLER.renderCastingWandHud(mc, event.getGuiGraphics(),
                    event.getPartialTick(), mc.player, time, held);
        }
    }
}
