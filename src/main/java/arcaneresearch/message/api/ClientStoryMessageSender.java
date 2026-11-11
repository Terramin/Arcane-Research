package arcaneresearch.message.api;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.UUID;
import java.util.function.BiConsumer;

@OnlyIn(Dist.CLIENT)
final class ClientStoryMessageSender {

    private ClientStoryMessageSender() {
    }

    static void send(StoryMessage msg, BiConsumer<UUID, DeliveryStatus> callback) {
        StoryTelling.say(msg, callback);
    }
}