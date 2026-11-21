package arcaneresearch.debug;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Brigadier argument for a single {@link ProfileCategory}. */
public final class ProfileCategoryArgument implements ArgumentType<ProfileCategory> {
    public static final SimpleCommandExceptionType ERROR_INVALID_CATEGORY = new SimpleCommandExceptionType(
            Component.literal("Unknown profiling type. Valid values: " + ProfileCategory.names()));

    private static final ProfileCategoryArgument INSTANCE = new ProfileCategoryArgument();
    private static final List<String> SUGGESTIONS = Arrays.stream(ProfileCategory.values())
            .map(Enum::name)
            .toList();

    private ProfileCategoryArgument() {
    }

    public static ProfileCategoryArgument category() {
        return INSTANCE;
    }

    public static ProfileCategory get(CommandContext<?> context, String name) {
        return context.getArgument(name, ProfileCategory.class);
    }

    @Override
    public ProfileCategory parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        while (reader.canRead() && reader.peek() != ' ') {
            reader.skip();
        }
        String token = reader.getString().substring(start, reader.getCursor());
        ProfileCategory parsed = ProfileCategory.parse(token);
        if (parsed == null) {
            throw ERROR_INVALID_CATEGORY.create();
        }
        return parsed;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(SUGGESTIONS, builder);
    }
}
