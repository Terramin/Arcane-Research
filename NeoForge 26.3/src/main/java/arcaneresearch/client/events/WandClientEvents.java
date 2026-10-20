package arcaneresearch.client.events;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber; // NEOFORGE_PORT: was Mod
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.lib.events.WandHudHandler;
import arcaneresearch.client.lib.events.WandRenderingHandler;

@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class WandClientEvents {
    private static final WandRenderingHandler WAND_HANDLER = new WandRenderingHandler();
    private static final WandHudHandler HUD_HANDLER = new WandHudHandler();

    // NEOFORGE_PORT: RenderGuiEvent removed in 26.x -> GuiLayer.
    public static final GuiLayer WAND_LAYER = (graphics, deltaTracker) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        long time = System.nanoTime() / 1000000L;
        float partial = deltaTracker.getGameTimeDeltaPartialTick(false);
        WAND_HANDLER.handleFociRadial(mc, time, graphics, partial);
        ItemStack held = WandRenderingHandler.heldWand(mc.player);
        if (!held.isEmpty()) {
            HUD_HANDLER.renderCastingWandHud(mc, graphics, partial, mc.player, time, held);
        }
    };

    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL,
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "wand_hud"), WAND_LAYER);
    }
}