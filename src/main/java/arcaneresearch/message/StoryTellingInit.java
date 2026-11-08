package arcaneresearch.message;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.message.api.StoryTelling;
import arcaneresearch.message.api.PacketStoryMessageAck;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class StoryTellingInit {

    private StoryTellingInit() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                StoryTelling.setAckSender((id, status) ->
                        PacketHandler.sendToServer(new PacketStoryMessageAck(id, status))));
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("storytelling",
                (gui, g, partialTick, width, height) ->
                        StoryTelling.render(g, partialTick, width, height));
    }
}
