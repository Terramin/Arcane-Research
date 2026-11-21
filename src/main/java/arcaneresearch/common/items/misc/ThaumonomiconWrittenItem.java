package arcaneresearch.common.items.misc;

import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.common.items.ItemTC;
import arcaneresearch.common.lib.research.WrittenBookRules;
import arcaneresearch.init.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
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
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ThaumonomiconWrittenItem extends ItemTC {

    private static final String TAG_STORED = "StoredKnowledge";
    private static final String TAG_OWNER = "Owner";

    public ThaumonomiconWrittenItem(Item.Properties props) {
        super(props
                .stacksTo(1)
                .rarity(Rarity.EPIC));
    }

    public static boolean hasStored(@NotNull ItemStack stack) {
        CompoundTag tag = ArcaneresearchInvHelper.getCustomTag(stack);
        return tag != null && tag.contains(TAG_STORED);
    }

    @Override
    public @NotNull InteractionResult use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            SoundEvent page = ModSounds.PAGE.get();
            level.playLocalSound(player.getX(), player.getY(), player.getZ(), page,
                    SoundSource.PLAYERS, 1.0f, 1.0f, false);
            return InteractionResult.SUCCESS;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
            if (knowledge != null) {
                if (!hasStored(stack)) {
                    record(serverPlayer, stack, knowledge);
                } else {
                    grant(serverPlayer, stack, knowledge);
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    private static void record(ServerPlayer player, ItemStack stack, IPlayerKnowledge knowledge) {
        CompoundTag tag = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
        tag.put(TAG_STORED, knowledge.serializeNBT());
        tag.putString(TAG_OWNER, player.getName().getString());
        ArcaneresearchInvHelper.setCustomTag(stack, tag);
        player.sendSystemMessage(Component.translatable("tc.thaumonomicon_written.recorded"));
    }

    private static void grant(ServerPlayer player, ItemStack stack, IPlayerKnowledge knowledge) {
        CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
        if (root == null) {
            return;
        }
        CompoundTag stored = root.getCompoundOrEmpty(TAG_STORED);

        ListTag researchList = stored.getListOrEmpty("research");
        for (int i = 0; i < researchList.size(); i++) {
            CompoundTag entry = researchList.getCompoundOrEmpty(i);
            String key = entry.getStringOr("key", "");
            if (key.isEmpty()) {
                continue;
            }
            if (!knowledge.isResearchKnown(key)) {
                knowledge.addResearch(key);
            }
            int stage = entry.getIntOr("stage", 0);
            if (stage > knowledge.getResearchStage(key)) {
                knowledge.setResearchStage(key, stage);
            }

            if (WrittenBookRules.grantsPageFlag(entry.getStringOr("flags", ""))) {
                knowledge.setResearchFlag(key, IPlayerKnowledge.EnumResearchFlag.PAGE);
            }
        }

        ListTag knowledgeList = stored.getListOrEmpty("knowledge");
        for (int i = 0; i < knowledgeList.size(); i++) {
            CompoundTag entry = knowledgeList.getCompoundOrEmpty(i);
            String key = entry.getStringOr("key", "");
            int amount = entry.getIntOr("amount", 0);
            WrittenBookRules.KnowledgeKey parsed = WrittenBookRules.parseKnowledgeKey(key);
            if (parsed == null) {
                continue;
            }
            IPlayerKnowledge.EnumKnowledgeType type = parsed.type() == WrittenBookRules.KnowledgeType.THEORY
                    ? IPlayerKnowledge.EnumKnowledgeType.THEORY
                    : IPlayerKnowledge.EnumKnowledgeType.OBSERVATION;
            String category = parsed.category();
            int delta = WrittenBookRules.positiveDelta(amount, knowledge.getKnowledgeRaw(type, category));
            if (delta > 0) {
                knowledge.addKnowledge(type, category, delta);
            }
        }

        ListTag presentedList = stored.getListOrEmpty("presented");
        for (int i = 0; i < presentedList.size(); i++) {
            String key = presentedList.getCompoundOrEmpty(i).getStringOr("key", "");
            if (!key.isEmpty()) {
                knowledge.addPresentedDiscovery(key);
            }
        }

        knowledge.sync(player);
        player.sendSystemMessage(Component.translatable("tc.thaumonomicon_written.granted",
                root.getStringOr(TAG_OWNER, "")));

        stack.shrink(1);
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = ArcaneresearchInvHelper.getCustomTag(stack);
        if (tag != null && tag.contains(TAG_OWNER)) {
            tooltip.accept(Component.translatable("tc.thaumonomicon_written.owner", tag.getStringOr(TAG_OWNER, ""))
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.accept(Component.translatable("tc.thaumonomicon_written.empty")
                    .withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return hasStored(stack);
    }
}
