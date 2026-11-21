package arcaneresearch.debug;

import arcaneresearch.Arcaneresearch;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumSet;

/**
 * Client-side debug command.
 *
 * <p>Registered on Forge's client command dispatcher, so it is available in
 * singleplayer and on any multiplayer server - even when the server has no
 * commands enabled or does not have this mod installed. The command is never
 * sent to the server.</p>
 *
 * <pre>
 * /arcaneresearch debug &lt;levels&gt; [type]
 * /arcaneresearch debug stop | status | reset | writefile | help
 * </pre>
 */
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public final class DebugCommand {
    private static final String PREFIX = "[ArcaneResearch] ";

    private DebugCommand() {
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("arcaneresearch")
                .then(Commands.literal("debug")
                        // Literals first: Brigadier resolves those before the argument node.
                        .then(Commands.literal("help").executes(DebugCommand::help))
                        .then(Commands.literal("status").executes(DebugCommand::status))
                        .then(Commands.literal("reset").executes(DebugCommand::reset))
                        .then(Commands.literal("writefile").executes(DebugCommand::writeFile))
                        .then(Commands.literal("stop").executes(DebugCommand::stop))
                        .then(Commands.argument("levels", LogLevelArgument.levels())
                                // No type given: profile everything.
                                .executes(context -> start(context, ProfileCategory.ALL))
                                .then(Commands.argument("type", ProfileCategoryArgument.category())
                                        .executes(context -> start(context,
                                                ProfileCategoryArgument.get(context, "type")))))
                        // Bare /arcaneresearch debug behaves like status.
                        .executes(DebugCommand::status)));
    }

    private static int start(CommandContext<CommandSourceStack> context, ProfileCategory type) {
        CommandSourceStack source = context.getSource();
        EnumSet<LogLevel> levels = LogLevelArgument.get(context, "levels");
        DebugSession session = DebugSession.get();
        if (levels.isEmpty()) {
            session.stop();
            send(source, "Debug logging STOPPED.", ChatFormatting.YELLOW);
            return Command.SINGLE_SUCCESS;
        }
        session.start(levels, type);
        ArcaneLogger.forCategory(type).info("Debug session started: levels={}, profile={}", levels, type.name());
        send(source, "Debug logging ACTIVE - levels=" + levels + ", type=" + type.name(), ChatFormatting.AQUA);
        send(source, "Use /arcaneresearch debug status to inspect counters and timings.", ChatFormatting.GRAY);
        return Command.SINGLE_SUCCESS;
    }

    private static int stop(CommandContext<CommandSourceStack> context) {
        DebugSession.get().stop();
        send(context.getSource(), "Debug logging STOPPED.", ChatFormatting.YELLOW);
        return Command.SINGLE_SUCCESS;
    }

    private static int status(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        for (String line : DebugSession.get().statusLines()) {
            source.sendSuccess(() -> Component.literal(PREFIX + line), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int reset(CommandContext<CommandSourceStack> context) {
        DebugSession.get().resetStats();
        send(context.getSource(), "Debug counters and timings cleared.", ChatFormatting.AQUA);
        return Command.SINGLE_SUCCESS;
    }

    private static int writeFile(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            Path file = DebugSession.get().writeLogFile();
            ArcaneLogger.forCategory(ProfileCategory.CORE).info("Debug session exported to {}", file);
            send(source, "Debug log written to " + file.toAbsolutePath(), ChatFormatting.GREEN);
        } catch (IOException e) {
            ArcaneLogger.forCategory(ProfileCategory.CORE).error("Failed to write debug log", e);
            send(source, "Failed to write debug log: " + e.getMessage(), ChatFormatting.RED);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int help(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        send(source, "Debug logging (client-side, works on any server):", ChatFormatting.GOLD);
        send(source, "  /arcaneresearch debug <levels> [type]", ChatFormatting.AQUA);
        send(source, "    levels: " + LogLevel.names() + " or comma separated (e.g. DEBUG,INFO)", ChatFormatting.GRAY);
        send(source, "    type:   " + ProfileCategory.names() + " (default: ALL)", ChatFormatting.GRAY);
        send(source, "  /arcaneresearch debug status | reset | writefile | stop | help", ChatFormatting.AQUA);
        send(source, "Examples:", ChatFormatting.GOLD);
        send(source, "  /arcaneresearch debug DEBUG AURA", ChatFormatting.GRAY);
        send(source, "  /arcaneresearch debug TRACE", ChatFormatting.GRAY);
        send(source, "  /arcaneresearch debug OFF", ChatFormatting.GRAY);
        return Command.SINGLE_SUCCESS;
    }

    private static void send(CommandSourceStack source, String text, ChatFormatting color) {
        source.sendSuccess(() -> Component.literal(PREFIX + text).withStyle(color), false);
    }
}
