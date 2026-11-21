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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Brigadier argument for a comma separated list of {@link LogLevel}s. */
public final class LogLevelArgument implements ArgumentType<EnumSet<LogLevel>> {
    public static final SimpleCommandExceptionType ERROR_INVALID_LEVEL = new SimpleCommandExceptionType(
            Component.literal("Unknown profiling level. Valid values: " + LogLevel.names()));

    private static final LogLevelArgument INSTANCE = new LogLevelArgument();
    private static final List<String> SUGGESTIONS = buildSuggestions();

    private LogLevelArgument() {
    }

    public static LogLevelArgument levels() {
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public static EnumSet<LogLevel> get(CommandContext<?> context, String name) {
        return context.getArgument(name, EnumSet.class);
    }

    @Override
    public EnumSet<LogLevel> parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        while (reader.canRead() && reader.peek() != ' ') {
            reader.skip();
        }
        String token = reader.getString().substring(start, reader.getCursor());
        EnumSet<LogLevel> parsed = LogLevel.parseSet(token);
        if (parsed == null) {
            throw ERROR_INVALID_LEVEL.create();
        }
        return parsed;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(SUGGESTIONS, builder);
    }

    private static List<String> buildSuggestions() {
        List<String> values = new ArrayList<>();
        for (LogLevel level : LogLevel.values()) {
            values.add(level.name());
        }
        values.add("ALL");
        values.add("TRACE,DEBUG,INFO");
        return values;
    }
}
