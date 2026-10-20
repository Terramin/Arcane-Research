package arcaneresearch.common.lib.research;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.aspects.AspectHelper;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.research.IScanThing;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.ResearchCategory;
import arcaneresearch.api.research.ScanningManager;

public class ScanGeneric implements IScanThing {

    @Override
    public boolean checkThing(Player player, Object obj) {
        if (obj == null) return false;

        AspectList aspects = null;

        if (obj instanceof Entity && !(obj instanceof ItemEntity)) {
            aspects = AspectHelper.getEntityAspects((Entity) obj);
        } else {
            ItemStack stack = ScanningManager.getItemFromParams(player, obj);
            if (stack != null && !stack.isEmpty()) {

                aspects = ArcaneresearchApi.internalMethods.getObjectAspects(stack);
            }
        }

        return aspects != null && aspects.size() > 0;
    }

    @Override
    public void onSuccess(Player player, Object obj) {
        if (obj == null) return;

        AspectList aspects = null;

        if (obj instanceof Entity && !(obj instanceof ItemEntity)) {
            aspects = AspectHelper.getEntityAspects((Entity) obj);
        } else {
            ItemStack stack = ScanningManager.getItemFromParams(player, obj);
            if (stack != null && !stack.isEmpty()) {
                aspects = ArcaneresearchApi.internalMethods.getObjectAspects(stack);
            }
        }

        if (aspects != null) {

            for (ResearchCategory category : ResearchCategories.researchCategories.values()) {
                int knowledge = category.applyFormula(aspects);
                if (knowledge > 0) {
                    ArcaneresearchApi.internalMethods.addKnowledge(player,
                            IPlayerKnowledge.EnumKnowledgeType.OBSERVATION,
                            category, knowledge);
                }
            }
        }
    }

    @Override
    public String getResearchKey(Player player, Object obj) {
        if (obj instanceof Entity entity && !(obj instanceof ItemEntity)) {
            String entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
            return "!" + entityKey;
        }

        ItemStack stack = ScanningManager.getItemFromParams(player, obj);
        if (stack != null && !stack.isEmpty()) {
            String itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            return "!" + itemKey;
        }

        return null;
    }
}
