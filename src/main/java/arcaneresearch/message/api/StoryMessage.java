package arcaneresearch.message.api;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record StoryMessage(
        @NotNull Component title,
        @NotNull Component text,
        @NotNull Identifier icon,
        int titleColor,
        int textColor,
        int durationTicks,
        int maxWaitTicks,
        boolean shake,
        boolean typewriter
) {
    public static final Identifier NONE =
            Identifier.fromNamespaceAndPath("arcaneresearch", "none");

    public StoryMessage {
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(icon, "icon");
        durationTicks = Math.max(1, durationTicks);
        maxWaitTicks = Math.max(durationTicks*5, maxWaitTicks);
    }
}