package arcaneresearch.common.lib.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.lib.network.playerdata.PacketSyncKnowledge;
import arcaneresearch.common.lib.network.playerdata.PacketSyncWarp;

@EventBusSubscriber(modid = Arcaneresearch.MODID)
public class EventHandlerNetwork {
    @SubscribeEvent
    public static void playerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketHandler.sendToPlayer(new PacketSyncWarp(player), player);
            PacketHandler.sendToPlayer(new PacketSyncKnowledge(player), player);
        }
    }

    @SubscribeEvent
    public static void playerLoggedOutEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        arcaneresearch.client.lib.events.TCShaderHandler.onPlayerLoggedOut(event.getEntity().getUUID());
    }
}
