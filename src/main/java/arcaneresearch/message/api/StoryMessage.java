package arcaneresearch.message.api;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public record StoryMessage(
        @NotNull Component title,
        @NotNull Component text,
        @NotNull ResourceLocation icon,
        int titleColor,
        int textColor,
        int durationTicks,
        int maxWaitTicks,
        @NotNull UUID target,
        boolean shake,
        boolean typewriter
) {
    public static final UUID ALL = new UUID(0L, 0L);
    public static final ResourceLocation NONE =
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "none");

    public StoryMessage {
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(icon, "icon");
        Objects.requireNonNull(target, "target");
    }
}
