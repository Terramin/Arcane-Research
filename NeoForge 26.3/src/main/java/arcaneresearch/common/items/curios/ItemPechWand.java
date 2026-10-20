package arcaneresearch.common.items.curios;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.ResearchCategory;

import java.util.function.Consumer;

public class ItemPechWand extends Item {

    public ItemPechWand(Item.Properties props) {
        super(props
                .stacksTo(1)
                .rarity(Rarity.RARE));
    }

    // NEOFORGE_PORT: use() now returns InteractionResult (holder removed);
    // InteractionResultHolder.pass(stack) -> InteractionResult.PASS.
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);

        if (knowledge == null) {
            return InteractionResult.PASS;
        }

        if (!knowledge.isResearchKnown("BASEAUROMANCY")) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("not.pechwand")
                        .withStyle(ChatFormatting.RED));
            }

            return InteractionResult.PASS;
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                arcaneresearch.init.ModSounds.LEARN.get(), SoundSource.NEUTRAL,
                0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));

        if (!level.isClientSide()) {

            if (!knowledge.isResearchKnown("FOCUSPECH")) {
                ArcaneresearchApi.internalMethods.progressResearch(player, "FOCUSPECH");
                player.sendSystemMessage(Component.translatable("got.pechwand")
                        .withStyle(ChatFormatting.DARK_PURPLE));
            }

            ResearchCategory[] rc = ResearchCategories.researchCategories.values()
                    .toArray(new ResearchCategory[0]);
            if (rc.length > 0) {
                int oProg = IPlayerKnowledge.EnumKnowledgeType.OBSERVATION.getProgression();
                ArcaneresearchApi.internalMethods.addKnowledge(player,
                        IPlayerKnowledge.EnumKnowledgeType.OBSERVATION,
                        rc[player.getRandom().nextInt(rc.length)],
                        Mth.nextInt(player.getRandom(), oProg / 3, oProg / 2));
                int tProg = IPlayerKnowledge.EnumKnowledgeType.THEORY.getProgression();
                ArcaneresearchApi.internalMethods.addKnowledge(player,
                        IPlayerKnowledge.EnumKnowledgeType.THEORY,
                        rc[player.getRandom().nextInt(rc.length)],
                        Mth.nextInt(player.getRandom(), tProg / 5, tProg / 4));
            }
        }

        player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));

        return InteractionResult.PASS;
    }

    // NEOFORGE_PORT: OnlyIn/Dist moved to net.neoforged.api.distmarker; appendHoverText signature
    // changed in 26.x (ItemStack, Level, List) -> (ItemStack, TooltipContext, TooltipDisplay,
    // Consumer, TooltipFlag).
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.curio.text").withStyle(style -> style.withColor(0x808080)));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}