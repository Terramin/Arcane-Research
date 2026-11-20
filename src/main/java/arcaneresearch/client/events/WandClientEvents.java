package arcaneresearch.client.events;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.lib.events.WandHudHandler;
import arcaneresearch.client.lib.events.WandRenderingHandler;
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class WandClientEvents {
    private static final WandRenderingHandler WAND_HANDLER = new WandRenderingHandler();
    private static final WandHudHandler HUD_HANDLER = new WandHudHandler();
    @SubscribeEvent
    public static void renderHud(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        ItemStack held = WandRenderingHandler.heldWand(mc.player);
        if (!held.isEmpty()) {
            HUD_HANDLER.renderCastingWandHud(mc, event.getGuiGraphics(),
                    event.getPartialTick(), mc.player,
                    System.nanoTime() / 1000000L, held);
        }
    }

    @SubscribeEvent
    public static void renderRadial(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        WAND_HANDLER.handleFociRadial(mc, System.nanoTime() / 1000000L, event);
    }
}
