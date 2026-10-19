package arcaneresearch.common.items.curios;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.ResearchCategory;
import arcaneresearch.init.ModSounds;

import java.util.function.Consumer;

public class ItemCurio extends Item {

    public static final String[] VARIANTS = {
            "arcane", "preserved", "ancient", "eldritch", "knowledge", "twisted", "rites"
    };

    private final String variant;

    public ItemCurio(Item.Properties props, String variant) {
        super(props.rarity(Rarity.UNCOMMON));
        this.variant = variant;
    }

    private static void addBoth(Player player, String category, int oProg, int tProg) {
        ArcaneresearchApi.internalMethods.addKnowledge(player, IPlayerKnowledge.EnumKnowledgeType.OBSERVATION,
                ResearchCategories.getResearchCategory(category),
                player.getRandom().nextInt(oProg / 2, oProg + 1));
        ArcaneresearchApi.internalMethods.addKnowledge(player, IPlayerKnowledge.EnumKnowledgeType.THEORY,
                ResearchCategories.getResearchCategory(category),
                player.getRandom().nextInt(tProg / 3, tProg / 2 + 1));
    }

    public String getVariant() {
        return variant;
    }

    // TODO_PORT (items): Forge overrode getDescriptionId(ItemStack) for the variant key, but
    // per-stack description ids are GONE in 26.x (getDescriptionId() is final). Simplest compiling
    // equivalent: return the same translation key via getName (Forge ref: getDescriptionId).
    @Override
    public Component getName(ItemStack stack) {

        return Component.translatable("item.arcaneresearch.curio." + variant);
    }

    // NEOFORGE_PORT: appendHoverText signature changed in 26.x
    // (ItemStack, Level, List) -> (ItemStack, TooltipContext, TooltipDisplay, Consumer, TooltipFlag).
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.curio.text"));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    // NEOFORGE_PORT: use() now returns InteractionResult (holder removed);
    // InteractionResultHolder.pass(stack) -> InteractionResult.PASS, and
    // Player.displayClientMessage(msg, false) -> sendSystemMessage(msg).
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.LEARN.get(),
                SoundSource.NEUTRAL, 0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));
        if (!level.isClientSide()) {
            int oProg = IPlayerKnowledge.EnumKnowledgeType.OBSERVATION.getProgression();
            int tProg = IPlayerKnowledge.EnumKnowledgeType.THEORY.getProgression();
            switch (variant) {
                default -> {
                    addBoth(player, "AUROMANCY", oProg, tProg);
                }
                case "preserved" -> {
                    addBoth(player, "ALCHEMY", oProg, tProg);
                }
                case "ancient" -> {
                    addBoth(player, "GOLEMANCY", oProg, tProg);
                }
                case "eldritch" -> {
                    addBoth(player, "ELDRITCH", oProg, tProg);
                    ArcaneresearchApi.internalMethods.addWarpToPlayer(player, 1, IPlayerWarp.EnumWarpType.NORMAL);
                    ArcaneresearchApi.internalMethods.addWarpToPlayer(player, 5, IPlayerWarp.EnumWarpType.TEMPORARY);
                }
                case "knowledge" -> {
                    addBoth(player, "INFUSION", oProg, tProg);
                }
                case "twisted" -> {
                    addBoth(player, "ARTIFICE", oProg, tProg);
                }
                case "rites" -> {
                    int aw = ArcaneresearchApi.internalMethods.getActualWarp(player);
                    if (aw > 20) {
                        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
                        if (knowledge != null && !knowledge.isResearchKnown("CrimsonRites")) {
                            ArcaneresearchApi.internalMethods.completeResearch(player, "CrimsonRites");
                        }
                        addBoth(player, "ELDRITCH", oProg, tProg);
                        ArcaneresearchApi.internalMethods.addWarpToPlayer(player, 1, IPlayerWarp.EnumWarpType.NORMAL);
                        ArcaneresearchApi.internalMethods.addWarpToPlayer(player, 5, IPlayerWarp.EnumWarpType.TEMPORARY);
                        if (player.getRandom().nextBoolean()) {
                            ArcaneresearchApi.internalMethods.addWarpToPlayer(player, 1, IPlayerWarp.EnumWarpType.PERMANENT);
                        }
                    } else {
                        player.sendSystemMessage(
                                Component.literal("§5" + Component.translatable("fail.crimsonrites").getString()));
                        return super.use(level, player, hand);
                    }
                }
            }

            ResearchCategory[] rc = ResearchCategories.researchCategories.values()
                    .toArray(new ResearchCategory[0]);
            ArcaneresearchApi.internalMethods.addKnowledge(player, IPlayerKnowledge.EnumKnowledgeType.OBSERVATION,
                    rc[player.getRandom().nextInt(rc.length)],
                    player.getRandom().nextInt(oProg / 2, oProg + 1));
            ArcaneresearchApi.internalMethods.addKnowledge(player, IPlayerKnowledge.EnumKnowledgeType.THEORY,
                    rc[player.getRandom().nextInt(rc.length)],
                    player.getRandom().nextInt(tProg / 3, tProg / 2 + 1));

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            player.sendSystemMessage(
                    Component.literal("§5" + Component.translatable("tc.knowledge.gained").getString()));
        }
        player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
        return super.use(level, player, hand);
    }
}
