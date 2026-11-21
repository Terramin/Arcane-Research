package arcaneresearch.common.items.wands;

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
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class PechWandItem extends Item {

    public PechWandItem(Item.Properties props) {
        super(props
                .stacksTo(1)
                .rarity(Rarity.RARE));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean clientSide = level.isClientSide();

        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
        if (knowledge == null) {
            return InteractionResult.PASS;
        }

        if (!knowledge.isResearchKnown("BASEAUROMANCY")) {
            if (!clientSide) {
                player.sendSystemMessage(Component.translatable("not.pechwand")
                        .withStyle(ChatFormatting.RED));
            }
            return InteractionResult.PASS;
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        SoundEvent learnSound = ModSounds.LEARN.get();
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                learnSound, SoundSource.NEUTRAL,
                0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));

        if (!clientSide) {
            if (!knowledge.isResearchKnown("FOCUSPECH")) {
                ArcaneresearchApi.internalMethods.progressResearch(player, "FOCUSPECH");
                player.sendSystemMessage(Component.literal("§5" + Component.translatable("got.pechwand").getString()));
            }

            var categories = ResearchCategories.researchCategories;
            if (categories != null) {
                java.util.List<ResearchCategory> valid = new java.util.ArrayList<>();
                for (ResearchCategory category : categories.values()) {
                    if (category != null) {
                        valid.add(category);
                    }
                }

                if (!valid.isEmpty()) {
                    RandomSource random = player.getRandom();
                    ResearchCategory[] rc = valid.toArray(new ResearchCategory[0]);

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
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.curio.text")
                .withStyle(style -> style.withColor(0x808080)));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}
