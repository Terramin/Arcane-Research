package arcaneresearch.message;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.message.api.PacketStoryMessageAck;
import arcaneresearch.message.api.StoryTelling;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

@EventBusSubscriber(modid = Arcaneresearch.MODID,
        value = Dist.CLIENT)
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
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "storytelling"),
                (graphics, deltaTracker) -> {
                    Minecraft mc = Minecraft.getInstance();
                    StoryTelling.render(graphics,
                            deltaTracker.getGameTimeDeltaPartialTick(false),
                            mc.getWindow().getGuiScaledWidth(),
                            mc.getWindow().getGuiScaledHeight());
                });
    }
}
