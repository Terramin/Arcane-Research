package arcaneresearch.common.lib.research;

import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.aspects.AspectHelper;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.research.IScanThing;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.ResearchCategory;
import arcaneresearch.api.research.ScanningManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ScanGeneric implements IScanThing {

    @Override
    public boolean checkThing(Player player, Object obj) {
        AspectList aspects = resolveAspects(player, obj);
        return aspects != null && aspects.size() > 0;
    }

    @Override
    public void onSuccess(Player player, Object obj) {
        if (player == null) return;

        AspectList aspects = resolveAspects(player, obj);
        if (aspects == null || aspects.size() == 0) return;

        for (ResearchCategory category : ResearchCategories.researchCategories.values()) {
            if (category == null) continue;

            int knowledge = category.applyFormula(aspects);
            if (knowledge > 0) {
                ArcaneresearchApi.internalMethods.addKnowledge(player,
                        IPlayerKnowledge.EnumKnowledgeType.OBSERVATION,
                        category, knowledge);
            }
        }
    }

    @Override
    public String getResearchKey(Player player, Object obj) {
        if (obj == null) return null;

        if (obj instanceof Entity entity && !(obj instanceof ItemEntity)) {
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            return "!" + id;
        }

        if (player == null) return null;

        ItemStack stack = ScanningManager.getItemFromParams(player, obj);
        if (stack == null || stack.isEmpty()) return null;

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return "!" + id;
    }

    private static AspectList resolveAspects(Player player, Object obj) {
        if (obj == null) return null;

        if (obj instanceof Entity entity && !(obj instanceof ItemEntity)) {
            return AspectHelper.getEntityAspects(entity);
        }

        if (player == null) return null;

        ItemStack stack = ScanningManager.getItemFromParams(player, obj);
        if (stack == null || stack.isEmpty()) return null;

        return ArcaneresearchApi.internalMethods.getObjectAspects(stack);
    }
}