package arcaneresearch.common.items.misc;

import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.api.research.ResearchCategories;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemCrimsonRites extends Item {

    public ItemCrimsonRites() {
        super(new Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        int actualWarp = ArcaneresearchApi.internalMethods.getActualWarp(player);
        if (actualWarp <= 20) {
            player.sendSystemMessage(Component.literal("You need more warp to perform the rites."));
            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        var category = ResearchCategories.getResearchCategory("ELDRITCH");
        if (category == null) {
            return InteractionResultHolder.fail(stack);
        }

        RandomSource random = player.getRandom();

        ArcaneresearchApi.internalMethods.completeResearch(player, "CrimsonRites");

        ArcaneresearchApi.internalMethods.addKnowledge(
                player,
                IPlayerKnowledge.EnumKnowledgeType.OBSERVATION,
                category,
                random.nextInt(100)
        );

        ArcaneresearchApi.internalMethods.addKnowledge(
                player,
                IPlayerKnowledge.EnumKnowledgeType.THEORY,
                category,
                random.nextInt(100)
        );

        ArcaneresearchApi.internalMethods.addWarpToPlayer(player, 1, IPlayerWarp.EnumWarpType.NORMAL);
        ArcaneresearchApi.internalMethods.addWarpToPlayer(player, 5, IPlayerWarp.EnumWarpType.TEMPORARY);

        if (random.nextBoolean()) {
            ArcaneresearchApi.internalMethods.addWarpToPlayer(player, 1, IPlayerWarp.EnumWarpType.PERMANENT);
        }

        return InteractionResultHolder.sidedSuccess(stack, false);
    }
}