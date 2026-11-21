package arcaneresearch.message.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Сюжетные сообщения: нормализация и контракт")
class StoryMessageTest {

    @Test
    void durationsAreNormalizedToSafeMinimums() {
        StoryMessage message = message(0, -5);

        assertEquals(1, message.durationTicks());
        assertEquals(5, message.maxWaitTicks());
    }

    @Test
    void explicitWaitLongerThanTheMinimumIsPreserved() {
        StoryMessage message = message(30, 1000);

        assertEquals(30, message.durationTicks());
        assertEquals(1000, message.maxWaitTicks());
    }

    @Test
    void waitShorterThanFiveDurationsIsRaised() {
        StoryMessage message = message(20, 10);

        assertEquals(100, message.maxWaitTicks());
    }

    @Test
    void nullComponentsAndIconAreRejected() {
        var icon = StoryMessage.NONE;
        assertThrows(NullPointerException.class, () -> new StoryMessage(null, Component.literal("x"), icon, 0, 0, 1, 5, false, false));
        assertThrows(NullPointerException.class, () -> new StoryMessage(Component.literal("x"), null, icon, 0, 0, 1, 5, false, false));
        assertThrows(NullPointerException.class, () -> new StoryMessage(Component.literal("x"), Component.literal("y"), null, 0, 0, 1, 5, false, false));
    }

    @Test
    void recordKeepsFlagsAndColors() {
        StoryMessage message = new StoryMessage(Component.literal("t"), Component.literal("x"),
                StoryMessage.NONE, 0xFF0000, 0x00FF00, 40, 300, true, true);

        assertEquals(0xFF0000, message.titleColor());
        assertEquals(0x00FF00, message.textColor());
        assertTrue(message.shake());
        assertTrue(message.typewriter());
        assertTrue(StoryMessage.NONE.getPath().endsWith("none"));
    }

    private static StoryMessage message(int duration, int wait) {
        return new StoryMessage(Component.literal("title"), Component.literal("text"),
                StoryMessage.NONE, 0, 0, duration, wait, false, false);
    }
}
