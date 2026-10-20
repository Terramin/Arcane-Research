package arcaneresearch.common.lib.network;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.lib.network.playerdata.PacketSyncKnowledge;
import arcaneresearch.common.lib.network.playerdata.PacketSyncWarp;
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandlerNetwork {
    @SubscribeEvent
    public static void playerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketHandler.sendToPlayer(new PacketSyncWarp(player), player);
            PacketHandler.sendToPlayer(new PacketSyncKnowledge(player), player);
        }
    }
}
