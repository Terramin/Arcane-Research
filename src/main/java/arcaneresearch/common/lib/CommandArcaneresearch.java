package arcaneresearch.common.lib;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.ResearchCategory;
import arcaneresearch.api.research.ResearchEntry;
import arcaneresearch.api.research.ResearchStage;
import arcaneresearch.common.config.ConfigResearch;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.playerdata.PacketWarpMessage;
import arcaneresearch.common.lib.research.ResearchManager;
import arcaneresearch.common.world.aura.AuraHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandArcaneresearch {

    private static final SuggestionProvider<CommandSourceStack> RESEARCH_SUGGESTIONS = (context, builder) -> {
        List<String> keys = new ArrayList<>();
        for (ResearchCategory cat : ResearchCategories.researchCategories.values()) {
            for (ResearchEntry ri : cat.research.values()) {
                keys.add(ri.getKey());
            }
        }
        return SharedSuggestionProvider.suggest(keys, builder);
    };

    private static final SuggestionProvider<CommandSourceStack> ASPECT_SUGGESTIONS = (context, builder) -> {
        List<String> tags = new ArrayList<>();
        tags.add("all");
        for (arcaneresearch.api.aspects.Aspect aspect : arcaneresearch.api.aspects.Aspect.aspects.values()) {
            if (aspect != null) {
                tags.add(aspect.getTag());
            }
        }
        return SharedSuggestionProvider.suggest(tags, builder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("arcaneresearch")
                // NEOFORGE_PORT: hasPermission(int) removed -> permissions() + COMMANDS_GAMEMASTER
                // (old level 2, verified in 26.3 jar).
                .requires(source -> source.permissions().hasPermission(
                        net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER));

        builder.then(Commands.literal("help")
                .executes(context -> help(context.getSource())));

        builder.then(Commands.literal("reload")
                .executes(context -> reload(context.getSource())));

        builder.then(Commands.literal("spawnaura")
                .then(Commands.argument("vis", FloatArgumentType.floatArg(0))
                        .executes(context -> spawnAura(context.getSource(),
                                FloatArgumentType.getFloat(context, "vis"), 0.0f))
                        .then(Commands.argument("flux", FloatArgumentType.floatArg(0))
                                .executes(context -> spawnAura(context.getSource(),
                                        FloatArgumentType.getFloat(context, "vis"),
                                        FloatArgumentType.getFloat(context, "flux"))))));

        builder.then(Commands.literal("research")
                .then(Commands.literal("list")
                        .executes(context -> listResearch(context.getSource())))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.literal("list")
                                .executes(context -> listPlayerResearch(context.getSource(), EntityArgument.getPlayer(context, "player"))))
                        .then(Commands.literal("all")
                                .executes(context -> giveAllResearch(context.getSource(), EntityArgument.getPlayer(context, "player"))))
                        .then(Commands.literal("reset")
                                .executes(context -> resetResearch(context.getSource(), EntityArgument.getPlayer(context, "player"))))
                        .then(Commands.argument("research", StringArgumentType.string())
                                .suggests(RESEARCH_SUGGESTIONS)
                                .executes(context -> giveResearch(context.getSource(), EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "research")))
                                .then(Commands.literal("revoke")
                                        .executes(context -> revokeResearch(context.getSource(), EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "research"))))
                        )
                )
        );

        // NEOFORGE_PORT: Forge "aspect" subcommand (hex-table aspects) was missing in the port.
        builder.then(Commands.literal("aspect")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("aspect", StringArgumentType.string())
                                .suggests(ASPECT_SUGGESTIONS)
                                .executes(context -> giveAspect(context.getSource(),
                                        EntityArgument.getPlayer(context, "player"),
                                        StringArgumentType.getString(context, "aspect"), 8))
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> giveAspect(context.getSource(),
                                                EntityArgument.getPlayer(context, "player"),
                                                StringArgumentType.getString(context, "aspect"),
                                                IntegerArgumentType.getInteger(context, "amount"))))))
        );

        builder.then(Commands.literal("warp")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.literal("add")
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(context -> addWarp(context.getSource(), EntityArgument.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "amount"), ""))
                                        .then(Commands.literal("PERM")
                                                .executes(context -> addWarp(context.getSource(), EntityArgument.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "amount"), "PERM")))
                                        .then(Commands.literal("TEMP")
                                                .executes(context -> addWarp(context.getSource(), EntityArgument.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "amount"), "TEMP")))
                                )
                        )
                        .then(Commands.literal("set")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(context -> setWarp(context.getSource(), EntityArgument.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "amount"), ""))
                                        .then(Commands.literal("PERM")
                                                .executes(context -> setWarp(context.getSource(), EntityArgument.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "amount"), "PERM")))
                                        .then(Commands.literal("TEMP")
                                                .executes(context -> setWarp(context.getSource(), EntityArgument.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "amount"), "TEMP")))
                                )
                        )
                )
        );

        // The aliases must redirect to the registered node: Forge redirected to builder.build()
        // taken before any subcommand was added, so /thaum and /tc had no subcommands at all.
        com.mojang.brigadier.tree.LiteralCommandNode<CommandSourceStack> root = dispatcher.register(builder);
        dispatcher.register(Commands.literal("thaum").requires(builder.getRequirement()).redirect(root));
        dispatcher.register(Commands.literal("tc").requires(builder.getRequirement()).redirect(root));
    }

    private static int giveAspect(CommandSourceStack source, ServerPlayer player, String tag, int amount) {
        List<arcaneresearch.api.aspects.Aspect> targets = new ArrayList<>();
        if ("all".equalsIgnoreCase(tag)) {
            for (arcaneresearch.api.aspects.Aspect aspect : arcaneresearch.api.aspects.Aspect.aspects.values()) {
                if (aspect != null) {
                    targets.add(aspect);
                }
            }
        } else {
            arcaneresearch.api.aspects.Aspect aspect = arcaneresearch.api.aspects.Aspect.getAspect(tag);
            if (aspect == null) {
                source.sendFailure(Component.literal("§cUnknown aspect: " + tag));
                return 0;
            }
            targets.add(aspect);
        }
        arcaneresearch.api.aspects.AspectList granted = new arcaneresearch.api.aspects.AspectList();
        for (arcaneresearch.api.aspects.Aspect aspect : targets) {
            try {
                if (!arcaneresearch.discovered.scan.ScanTargets.known(player, aspect)) {
                    arcaneresearch.api.ArcaneresearchApi.internalMethods.progressResearch(player, "!" + aspect.getTag());
                }
            } catch (Exception ignored) {
            }
            arcaneresearch.discovered.scan.ScanWallet.add(player, aspect, amount);
            granted.add(aspect, amount);
        }
        try {
            arcaneresearch.discovered.scan.MsgWalletRequest.push(player);
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                    new arcaneresearch.discovered.scan.MsgStudyReward(granted));
        } catch (Exception ignored) {
        }
        source.sendSuccess(() -> Component.literal("§5Gave " + amount + "x " + tag
                + " aspect to " + player.getName().getString()), true);
        return 1;
    }

    private static int help(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("§3You can also use /thaum or /tc instead of /arcaneresearch."), false);
        source.sendSuccess(() -> Component.literal("§3Use this to give research to a player."), false);
        source.sendSuccess(() -> Component.literal("  /arcaneresearch research <list|player> <list|all|reset|<research>>"), false);
        source.sendSuccess(() -> Component.literal("§3Use this to remove research from a player."), false);
        source.sendSuccess(() -> Component.literal("  /arcaneresearch research <player> <research> revoke"), false);
        source.sendSuccess(() -> Component.literal("§3Use this to give hex-table aspects to a player."), false);
        source.sendSuccess(() -> Component.literal("  /arcaneresearch aspect <player> <tag|all> [amount]"), false);
        source.sendSuccess(() -> Component.literal("§3Use this to set a player's warp level."), false);
        source.sendSuccess(() -> Component.literal("  /arcaneresearch warp <player> <add|set> <amount> [PERM|TEMP]"), false);
        source.sendSuccess(() -> Component.literal("  not specifying perm or temp will just add normal warp"), false);
        source.sendSuccess(() -> Component.literal("§3Use this to reload json research data"), false);
        source.sendSuccess(() -> Component.literal("  /arcaneresearch reload"), false);
        source.sendSuccess(() -> Component.literal("§3Use this to add aura vis/flux to your chunk."), false);
        source.sendSuccess(() -> Component.literal("  /arcaneresearch spawnaura <vis> [flux]"), false);
        return 1;
    }

    private static int reload(CommandSourceStack source) {
        ConfigResearch.init();
        source.sendSuccess(() -> Component.literal("§5Reloaded research data."), true);
        return 1;
    }

    private static int spawnAura(CommandSourceStack source, float vis, float flux) {
        ServerLevel level = source.getLevel();
        BlockPos pos = BlockPos.containing(source.getPosition());
        if (vis > 0) {
            AuraHandler.addVis(level, pos, vis);
        }
        if (flux > 0) {
            AuraHandler.addFlux(level, pos, flux);
        }
        source.sendSuccess(() -> Component.literal("§5Added aura: "
                + AuraHandler.getVis(level, pos) + " vis, "
                + AuraHandler.getFlux(level, pos) + " flux in this chunk."), true);
        return 1;
    }

    private static int listResearch(CommandSourceStack source) {
        for (ResearchCategory cat : ResearchCategories.researchCategories.values()) {
            for (ResearchEntry ri : cat.research.values()) {
                source.sendSuccess(() -> Component.literal("§5" + ri.getKey()), false);
            }
        }
        return 1;
    }

    private static int listPlayerResearch(CommandSourceStack source, ServerPlayer player) {
        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
        if (knowledge != null) {
            String ss = String.join(", ", knowledge.getResearchList());
            source.sendSuccess(() -> Component.literal("§5Research for " + player.getName().getString()), false);
            source.sendSuccess(() -> Component.literal("§5" + ss), false);
        }
        return 1;
    }

    private static int giveAllResearch(CommandSourceStack source, ServerPlayer player) {
        for (ResearchCategory cat : ResearchCategories.researchCategories.values()) {
            for (ResearchEntry ri : cat.research.values()) {
                giveRecursiveResearch(player, ri.getKey());
            }
        }
        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
        if (knowledge != null) {
            knowledge.sync(player);
        }
        source.sendSuccess(() -> Component.literal("§5Gave all research to " + player.getName().getString()), true);
        return 1;
    }

    private static int resetResearch(CommandSourceStack source, ServerPlayer player) {
        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
        if (knowledge != null) {
            knowledge.clear();
            for (ResearchCategory cat : ResearchCategories.researchCategories.values()) {
                for (ResearchEntry ri : cat.research.values()) {
                    if (ri.hasMeta(ResearchEntry.EnumResearchMeta.AUTOUNLOCK)) {
                        ResearchManager.completeResearch(player, ri.getKey(), false);
                    }
                }
            }
            knowledge.sync(player);
        }
        source.sendSuccess(() -> Component.literal("§5Reset research for " + player.getName().getString()), true);
        return 1;
    }

    private static int giveResearch(CommandSourceStack source, ServerPlayer player, String research) {
        if (ResearchCategories.getResearch(research) != null) {
            giveRecursiveResearch(player, research);
            IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
            if (knowledge != null) {
                knowledge.sync(player);
            }
            source.sendSuccess(() -> Component.literal("§5Gave research " + research + " to " + player.getName().getString()), true);
        } else {
            source.sendFailure(Component.literal("§cResearch does not exist."));
        }
        return 1;
    }

    public static void giveRecursiveResearch(ServerPlayer player, String research) {
        if (research.contains("@")) {
            research = research.substring(0, research.indexOf("@"));
        }
        String finalResearch = research;
        ResearchEntry res = ResearchCategories.getResearch(research);
        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);

        if (knowledge != null && !knowledge.isResearchComplete(finalResearch)) {
            if (res != null && res.getParents() != null) {
                for (String rsi : res.getParentsStripped()) {
                    giveRecursiveResearch(player, rsi);
                }
            }
            if (res != null && res.getStages() != null) {
                for (ResearchStage page : res.getStages()) {
                    if (page.getResearch() != null) {
                        for (String gr : page.getResearch()) {
                            ResearchManager.completeResearch(player, gr);
                        }
                    }
                }
            }
            ResearchManager.completeResearch(player, finalResearch);

            for (String rc : ResearchCategories.researchCategories.keySet()) {
                for (ResearchEntry ri : ResearchCategories.getResearchCategory(rc).research.values()) {
                    if (ri.getStages() != null) {
                        for (ResearchStage stage : ri.getStages()) {
                            if (stage.getResearch() != null && Arrays.asList(stage.getResearch()).contains(finalResearch)) {
                                knowledge.setResearchFlag(ri.getKey(), IPlayerKnowledge.EnumResearchFlag.PAGE);
                                break;
                            }
                        }
                    }
                }
            }

            if (res != null && res.getSiblings() != null) {
                for (String rsi : res.getSiblings()) {
                    giveRecursiveResearch(player, rsi);
                }
            }
        }
    }

    private static int revokeResearch(CommandSourceStack source, ServerPlayer player, String research) {
        if (ResearchCategories.getResearch(research) != null) {
            revokeRecursiveResearch(player, research);
            IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
            if (knowledge != null) {
                // NEOFORGE_PORT (26.3): Forge also cleared presented discoveries on revoke.
                knowledge.clearPresentedDiscoveries();
                knowledge.sync(player);
            }
            source.sendSuccess(() -> Component.literal("§5Revoked research " + research + " from " + player.getName().getString()), true);
        } else {
            source.sendFailure(Component.literal("§cResearch does not exist."));
        }
        return 1;
    }

    public static void revokeRecursiveResearch(ServerPlayer player, String research) {
        if (research.contains("@")) {
            research = research.substring(0, research.indexOf("@"));
        }
        String finalResearch = research;
        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);

        if (knowledge != null && knowledge.isResearchComplete(finalResearch)) {
            for (String rc : ResearchCategories.researchCategories.keySet()) {
                for (ResearchEntry ri : ResearchCategories.getResearchCategory(rc).research.values()) {
                    if (ri != null && ri.getParents() != null && knowledge.isResearchComplete(ri.getKey())) {
                        for (String rsi : ri.getParentsStripped()) {
                            if (rsi.equals(finalResearch)) {
                                revokeRecursiveResearch(player, ri.getKey());
                            }
                        }
                    }
                }
            }
            knowledge.removeResearch(finalResearch);
        }
    }

    private static int addWarp(CommandSourceStack source, ServerPlayer player, int amount, String type) {
        IPlayerWarp.EnumWarpType warpType = IPlayerWarp.EnumWarpType.NORMAL;
        if (type.equalsIgnoreCase("PERM")) warpType = IPlayerWarp.EnumWarpType.PERMANENT;
        else if (type.equalsIgnoreCase("TEMP")) warpType = IPlayerWarp.EnumWarpType.TEMPORARY;

        final IPlayerWarp.EnumWarpType finalWarpType = warpType;
        IPlayerWarp warp = ArcaneresearchCapabilities.getWarp(player);
        if (warp != null) {
            warp.add(finalWarpType, amount);
            warp.sync(player);
        }

        if (type.isEmpty()) {
            PacketHandler.sendToPlayer(new PacketWarpMessage(player, (byte) 0, amount), player);
        }

        source.sendSuccess(() -> Component.literal("§5Added " + amount + " " + finalWarpType.name() + " warp to " + player.getName().getString()), true);
        return 1;
    }

    private static int setWarp(CommandSourceStack source, ServerPlayer player, int amount, String type) {
        IPlayerWarp.EnumWarpType warpType = IPlayerWarp.EnumWarpType.NORMAL;
        if (type.equalsIgnoreCase("PERM")) warpType = IPlayerWarp.EnumWarpType.PERMANENT;
        else if (type.equalsIgnoreCase("TEMP")) warpType = IPlayerWarp.EnumWarpType.TEMPORARY;

        final IPlayerWarp.EnumWarpType finalWarpType = warpType;
        IPlayerWarp warp = ArcaneresearchCapabilities.getWarp(player);
        if (warp != null) {
            warp.set(finalWarpType, amount);
            warp.sync(player);
        }

        source.sendSuccess(() -> Component.literal("§5Set " + finalWarpType.name() + " warp to " + amount + " for " + player.getName().getString()), true);
        return 1;
    }
}
