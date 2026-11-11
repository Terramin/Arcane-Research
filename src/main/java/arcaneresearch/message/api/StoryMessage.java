package arcaneresearch.message.api;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record StoryMessage(
        @NotNull Component title,
        @NotNull Component text,
        @NotNull ResourceLocation icon,
        int titleColor,
        int textColor,
        int durationTicks,
        int maxWaitTicks,
        boolean shake,
        boolean typewriter
) {
    public static final ResourceLocation NONE =
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "none");

    public StoryMessage {
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(icon, "icon");
        durationTicks = Math.max(0, durationTicks);
    }
}