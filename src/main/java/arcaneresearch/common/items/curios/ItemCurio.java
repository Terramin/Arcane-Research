package arcaneresearch.common.items.curios;

import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.ResearchCategory;
import arcaneresearch.init.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class ItemCurio extends Item {

    public static final String[] VARIANTS = {
            "arcane", "preserved", "ancient", "eldritch", "knowledge", "twisted", "rites"
    };

    private static final ResearchCategory[] EMPTY_CATEGORIES = new ResearchCategory[0];

    private final String variant;

    public ItemCurio(String variant) {
        super(new Item.Properties().rarity(Rarity.UNCOMMON));
        this.variant = Objects.requireNonNull(variant, "variant");
    }

    private static void addBoth(Player player, String categoryKey, int oProg, int tProg, RandomSource random) {
        ResearchCategory category = ResearchCategories.getResearchCategory(categoryKey);
        if (category == null) {
            return;
        }
        ArcaneresearchApi.internalMethods.addKnowledge(
                player,
                IPlayerKnowledge.EnumKnowledgeType.OBSERVATION,
                category,
                random.nextInt(oProg / 2, oProg + 1)
        );
        ArcaneresearchApi.internalMethods.addKnowledge(
                player,
                IPlayerKnowledge.EnumKnowledgeType.THEORY,
                category,
                random.nextInt(tProg / 3, tProg / 2 + 1)
        );
    }

    private static ResearchCategory[] getValidCategories() {
        var map = ResearchCategories.researchCategories;
        if (map == null) {
            return EMPTY_CATEGORIES;
        }

        int count = 0;
        for (ResearchCategory category : map.values()) {
            if (category != null) {
                count++;
            }
        }
        if (count == 0) {
            return EMPTY_CATEGORIES;
        }

        ResearchCategory[] result = new ResearchCategory[count];
        int index = 0;
        for (ResearchCategory category : map.values()) {
            if (category != null) {
                result[index++] = category;
            }
        }
        return result;
    }

    public String getVariant() {
        return variant;
    }

    @Override
    public @NotNull String getDescriptionId(@NotNull ItemStack stack) {
        return "item.arcaneresearch.curio." + variant;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.curio.text"));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean clientSide = level.isClientSide;
        RandomSource random = player.getRandom();

        if (!clientSide) {
            SoundEvent learn = ModSounds.LEARN.get();
            level.playSound(null, player.getX(), player.getY(), player.getZ(), learn,
                    SoundSource.NEUTRAL, 0.5f, 0.4f / (random.nextFloat() * 0.4f + 0.8f));

            int oProg = IPlayerKnowledge.EnumKnowledgeType.OBSERVATION.getProgression();
            int tProg = IPlayerKnowledge.EnumKnowledgeType.THEORY.getProgression();

            switch (variant) {
                default -> addBoth(player, "AUROMANCY", oProg, tProg, random);
                case "preserved" -> addBoth(player, "ALCHEMY", oProg, tProg, random);
                case "ancient" -> addBoth(player, "GOLEMANCY", oProg, tProg, random);
                case "eldritch" -> {
                    addBoth(player, "ELDRITCH", oProg, tProg, random);
                    ArcaneresearchApi.internalMethods.addWarpToPlayer(player, 1, IPlayerWarp.EnumWarpType.NORMAL);
                    ArcaneresearchApi.internalMethods.addWarpToPlayer(player, 5, IPlayerWarp.EnumWarpType.TEMPORARY);
                }
                case "knowledge" -> addBoth(player, "INFUSION", oProg, tProg, random);
                case "twisted" -> addBoth(player, "ARTIFICE", oProg, tProg, random);
                case "rites" -> {
                    int actualWarp = ArcaneresearchApi.internalMethods.getActualWarp(player);
                    if (actualWarp <= 20) {
                        player.displayClientMessage(
                                Component.translatable("fail.crimsonrites").withStyle(ChatFormatting.DARK_PURPLE),
                                false
                        );
                        return super.use(level, player, hand);
                    }

                    IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
                    if (knowledge != null && !knowledge.isResearchKnown("CrimsonRites")) {
                        ArcaneresearchApi.internalMethods.completeResearch(player, "CrimsonRites");
                    }

                    addBoth(player, "ELDRITCH", oProg, tProg, random);
                    ArcaneresearchApi.internalMethods.addWarpToPlayer(player, 1, IPlayerWarp.EnumWarpType.NORMAL);
                    ArcaneresearchApi.internalMethods.addWarpToPlayer(player, 5, IPlayerWarp.EnumWarpType.TEMPORARY);
                    if (random.nextBoolean()) {
                        ArcaneresearchApi.internalMethods.addWarpToPlayer(player, 1, IPlayerWarp.EnumWarpType.PERMANENT);
                    }
                }
            }

            ResearchCategory[] categories = getValidCategories();
            if (categories.length > 0) {
                ArcaneresearchApi.internalMethods.addKnowledge(
                        player,
                        IPlayerKnowledge.EnumKnowledgeType.OBSERVATION,
                        categories[random.nextInt(categories.length)],
                        random.nextInt(oProg / 2, oProg + 1)
                );
                ArcaneresearchApi.internalMethods.addKnowledge(
                        player,
                        IPlayerKnowledge.EnumKnowledgeType.THEORY,
                        categories[random.nextInt(categories.length)],
                        random.nextInt(tProg / 3, tProg / 2 + 1)
                );
            }

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            player.displayClientMessage(
                    Component.translatable("tc.knowledge.gained").withStyle(ChatFormatting.DARK_PURPLE),
                    false
            );
        }

        player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
        return super.use(level, player, hand);
    }
}