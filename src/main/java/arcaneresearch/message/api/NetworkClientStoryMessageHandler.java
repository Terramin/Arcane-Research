package arcaneresearch.message.api;

import net.neoforged.api.distmarker.Dist;
public final class NetworkClientStoryMessageHandler {

    private NetworkClientStoryMessageHandler() {
    }

    public static void handle(PacketStoryMessage pkt) {
        StoryTelling.receiveFromServer(pkt.message(), pkt.id());
    }
}
