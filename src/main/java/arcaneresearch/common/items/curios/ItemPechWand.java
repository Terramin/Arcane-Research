package arcaneresearch.common.items.curios;

import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.ResearchCategory;
import arcaneresearch.init.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import arcaneresearch.message.api.StoryTellingAPI;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ItemPechWand extends Item {

    public ItemPechWand() {
        super(new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.RARE));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean clientSide = level.isClientSide();

        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
        if (knowledge == null) {
            return InteractionResultHolder.pass(stack);
        }

        if (!knowledge.isResearchKnown("BASEAUROMANCY")) {
            if (!clientSide) {
                player.sendSystemMessage(Component.translatable("not.pechwand")
                        .withStyle(ChatFormatting.RED));
            }
            return InteractionResultHolder.pass(stack);
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        SoundEvent learnSound = ModSounds.LEARN.get();
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                learnSound, SoundSource.NEUTRAL,
                0.5f, 0.4f / (level.random.nextFloat() * 0.4f + 0.8f));

        if (!clientSide) {
            if (!knowledge.isResearchKnown("FOCUSPECH")) {
                ArcaneresearchApi.internalMethods.progressResearch(player, "FOCUSPECH");
                if (player instanceof ServerPlayer serverPlayer) {
                    StoryTellingAPI.sendStandartMsg(serverPlayer,
                            Component.literal("§5" + Component.translatable("got.pechwand").getString()));
                }
            }

            var categories = ResearchCategories.researchCategories;
            if (categories != null) {
                var categoryValues = categories.values();
                int validCount = 0;
                for (ResearchCategory category : categoryValues) {
                    if (category != null) {
                        validCount++;
                    }
                }

                if (validCount > 0) {
                    ResearchCategory[] rc = new ResearchCategory[validCount];
                    int index = 0;
                    for (ResearchCategory category : categoryValues) {
                        if (category != null) {
                            rc[index++] = category;
                        }
                    }

                    RandomSource random = player.getRandom();

                    int oProg = IPlayerKnowledge.EnumKnowledgeType.OBSERVATION.getProgression();
                    ArcaneresearchApi.internalMethods.addKnowledge(player,
                            IPlayerKnowledge.EnumKnowledgeType.OBSERVATION,
                            rc[random.nextInt(rc.length)],
                            Mth.nextInt(random, oProg / 3, oProg / 2));

                    int tProg = IPlayerKnowledge.EnumKnowledgeType.THEORY.getProgression();
                    ArcaneresearchApi.internalMethods.addKnowledge(player,
                            IPlayerKnowledge.EnumKnowledgeType.THEORY,
                            rc[random.nextInt(rc.length)],
                            Mth.nextInt(random, tProg / 5, tProg / 4));
                }
            }
        }

        player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
        return InteractionResultHolder.pass(stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.curio.text").withStyle(style -> style.withColor(0x808080)));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}