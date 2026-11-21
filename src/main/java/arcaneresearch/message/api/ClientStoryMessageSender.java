package arcaneresearch.message.api;

import net.neoforged.api.distmarker.Dist;
import java.util.UUID;
import java.util.function.BiConsumer;
final class ClientStoryMessageSender {

    private ClientStoryMessageSender() {
    }

    static void send(StoryMessage msg, BiConsumer<UUID, DeliveryStatus> callback) {
        StoryTelling.say(msg, callback);
    }
}