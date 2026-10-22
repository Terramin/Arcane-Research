package arcaneresearch.common.items.misc;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.api.research.ResearchCategories;

public class ItemCrimsonRites extends Item {

    public ItemCrimsonRites(Item.Properties props) {
        super(props.stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE));
    }

    
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            int actualWarp = ArcaneresearchApi.internalMethods.getActualWarp(player);
            if (actualWarp > 20) {
                ArcaneresearchApi.internalMethods.completeResearch(player, "CrimsonRites");
                ArcaneresearchApi.internalMethods.addKnowledge(player, IPlayerKnowledge.EnumKnowledgeType.OBSERVATION,
                        ResearchCategories.getResearchCategory("ELDRITCH"), player.getRandom().nextInt(100));
                ArcaneresearchApi.internalMethods.addKnowledge(player, IPlayerKnowledge.EnumKnowledgeType.THEORY,
                        ResearchCategories.getResearchCategory("ELDRITCH"), player.getRandom().nextInt(100));
                ArcaneresearchApi.internalMethods.addWarpToPlayer(player, 1, IPlayerWarp.EnumWarpType.NORMAL);
                ArcaneresearchApi.internalMethods.addWarpToPlayer(player, 5, IPlayerWarp.EnumWarpType.TEMPORARY);
                if (player.getRandom().nextBoolean()) {
                    ArcaneresearchApi.internalMethods.addWarpToPlayer(player, 1, IPlayerWarp.EnumWarpType.PERMANENT);
                }
            } else {
                player.sendSystemMessage(Component.literal("You need more warp to perform the rites."));
            }
        }

        
        
        return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
    }
}
