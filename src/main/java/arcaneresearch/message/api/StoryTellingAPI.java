package arcaneresearch.message.api;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.lib.network.PacketHandler;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public final class StoryTellingAPI {

    private record Pending(UUID player, BiConsumer<UUID, DeliveryStatus> cb) { }

    private static final Map<UUID, Pending> PENDING = new ConcurrentHashMap<>();

    private StoryTellingAPI() {
    }

    public static void send(StoryMessage msg, ServerPlayer player) {
        send(msg, player, null);
    }

    public static void send(StoryMessage msg, ServerPlayer player,
                            BiConsumer<UUID, DeliveryStatus> callback) {
        if (msg == null || player == null) return;
        UUID id = callback != null ? UUID.randomUUID() : null;
        if (id != null) PENDING.put(id, new Pending(player.getUUID(), callback));
        PacketHandler.sendToPlayer(new PacketStoryMessage(id, msg), player);
    }

    public static void sendToAll(StoryMessage msg) {
        if (msg == null) return;
        PacketHandler.sendToAll(new PacketStoryMessage(null, msg));
    }

    public static void sendToAll(StoryMessage msg,
                                 BiConsumer<ServerPlayer, DeliveryStatus> callback) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || msg == null) return;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            send(msg, p, callback == null ? null : (id, status) -> callback.accept(p, status));
        }
    }

    public static void sendToDimension(StoryMessage msg, ResourceKey<Level> dimension) {
        if (msg == null || dimension == null) return;
        PacketHandler.sendToDimension(new PacketStoryMessage(null, msg), dimension);
    }

    public static void sendToTrackers(StoryMessage msg, ServerPlayer seen) {
        if (msg == null || seen == null) return;
        PacketHandler.sendToAllTracking(new PacketStoryMessage(null, msg), seen);
    }

    static void onAck(UUID id, DeliveryStatus status) {
        Pending p = PENDING.remove(id);
        if (p == null) return;
        try {
            p.cb().accept(id, status);
        } catch (Throwable t) {
            Arcaneresearch.LOGGER.error("Story ack callback failed id={}", id, t);
        }
    }

    static void cleanupForPlayer(UUID playerId) {
        var it = PENDING.entrySet().iterator();
        while (it.hasNext()) {
            var e = it.next();
            if (e.getValue().player().equals(playerId)) {
                it.remove();
                try {
                    e.getValue().cb().accept(e.getKey(), DeliveryStatus.DROPPED);
                } catch (Throwable t) {
                    Arcaneresearch.LOGGER.error("Story cleanup callback failed id={}", e.getKey(), t);
                }
            }
        }
    }

    public static void sendDistClient(StoryMessage msg) {
        sendDistClient(msg, null);
    }

    public static void sendDistClient(StoryMessage msg,
                                      BiConsumer<UUID, DeliveryStatus> callback) {
        if (msg == null) return;
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientStoryMessageSender.send(msg, callback));
    }
}