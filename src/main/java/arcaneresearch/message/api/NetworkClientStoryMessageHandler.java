package arcaneresearch.message.api;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class NetworkClientStoryMessageHandler {

    private NetworkClientStoryMessageHandler() {
    }

    public static void handle(PacketStoryMessage pkt) {
        StoryTelling.receiveFromServer(pkt.message(), pkt.id());
    }
}
