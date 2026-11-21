package arcaneresearch.common.items.misc;

import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.common.items.ItemTC;
import arcaneresearch.common.lib.research.WrittenBookRules;
import arcaneresearch.init.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ThaumonomiconWrittenItem extends ItemTC {

    private static final String TAG_STORED = "StoredKnowledge";
    private static final String TAG_OWNER = "Owner";

    public ThaumonomiconWrittenItem() {
        super(new Properties()
                .stacksTo(1)
                .rarity(Rarity.EPIC));
    }

    public static boolean hasStored(@NotNull ItemStack stack) {
        return stack.getTag() != null && stack.getTag().contains(TAG_STORED);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            SoundEvent page = ModSounds.PAGE.get();
            level.playLocalSound(
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    page,
                    SoundSource.PLAYERS,
                    1.0f,
                    1.0f,
                    false
            );
            return InteractionResultHolder.sidedSuccess(stack, true);
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

        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    private static void record(ServerPlayer player, ItemStack stack, IPlayerKnowledge knowledge) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.put(TAG_STORED, knowledge.serializeNBT());
        tag.putString(TAG_OWNER, player.getName().getString());
        player.sendSystemMessage(Component.translatable("tc.thaumonomicon_written.recorded"));
    }

    private static void grant(ServerPlayer player, ItemStack stack, IPlayerKnowledge knowledge) {
        CompoundTag stored = stack.getTag().getCompound(TAG_STORED);

        ListTag researchList = stored.getList("research", Tag.TAG_COMPOUND);
        for (int i = 0; i < researchList.size(); i++) {
            CompoundTag entry = researchList.getCompound(i);
            String key = entry.getString("key");
            if (key.isEmpty()) {
                continue;
            }
            if (!knowledge.isResearchKnown(key)) {
                knowledge.addResearch(key);
            }
            int stage = entry.getInt("stage");
            if (stage > knowledge.getResearchStage(key)) {
                knowledge.setResearchStage(key, stage);
            }

            if (WrittenBookRules.grantsPageFlag(entry.getString("flags"))) {
                knowledge.setResearchFlag(key, IPlayerKnowledge.EnumResearchFlag.PAGE);
            }
        }

        ListTag knowledgeList = stored.getList("knowledge", Tag.TAG_COMPOUND);
        for (int i = 0; i < knowledgeList.size(); i++) {
            CompoundTag entry = knowledgeList.getCompound(i);
            String key = entry.getString("key");
            int amount = entry.getInt("amount");
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

        ListTag presentedList = stored.getList("presented", Tag.TAG_COMPOUND);
        for (int i = 0; i < presentedList.size(); i++) {
            String key = presentedList.getCompound(i).getString("key");
            if (!key.isEmpty()) {
                knowledge.addPresentedDiscovery(key);
            }
        }

        knowledge.sync(player);
        player.sendSystemMessage(Component.translatable("tc.thaumonomicon_written.granted",
                stack.getTag().getString(TAG_OWNER)));

        stack.shrink(1);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_OWNER)) {
            tooltip.add(Component.translatable("tc.thaumonomicon_written.owner", tag.getString(TAG_OWNER))
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("tc.thaumonomicon_written.empty")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return hasStored(stack);
    }
}
