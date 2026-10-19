package arcaneresearch.api.research;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectHelper;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.ArcaneresearchApi;

public class ScanAspect implements IScanThing {

    private final String research;
    private final Aspect aspect;

    public ScanAspect(String research, Aspect aspect) {
        this.research = research;
        this.aspect = aspect;
    }

    @Override
    public boolean checkThing(Player player, Object obj) {
        if (obj == null) return false;

        AspectList al = null;

        if (obj instanceof Entity && !(obj instanceof ItemEntity)) {
            al = AspectHelper.getEntityAspects((Entity) obj);
        } else {
            ItemStack is = null;
            if (obj instanceof ItemStack) {
                is = (ItemStack) obj;
            }
            if (obj instanceof ItemEntity && ((ItemEntity)obj).getItem() != null) {
                is = ((ItemEntity)obj).getItem();
            }
            if (obj instanceof BlockPos) {
                Block b = player.level().getBlockState((BlockPos) obj).getBlock();
                is = new ItemStack(b);
            }

            if (is != null) {

                al = ArcaneresearchApi.internalMethods.getObjectAspects(is);
            }
        }

        return al != null && al.getAmount(aspect) > 0;
    }

    @Override
    public void onSuccess(Player player, Object obj) {
        ArcaneresearchApi.internalMethods.addKnowledge(player,
            arcaneresearch.api.capabilities.IPlayerKnowledge.EnumKnowledgeType.OBSERVATION,
            arcaneresearch.api.research.ResearchCategories.getResearchCategory("AUROMANCY"), 1);
        ArcaneresearchApi.internalMethods.addKnowledge(player,
            arcaneresearch.api.capabilities.IPlayerKnowledge.EnumKnowledgeType.OBSERVATION,
            arcaneresearch.api.research.ResearchCategories.getResearchCategory("BASICS"), 1);
        ArcaneresearchApi.internalMethods.addKnowledge(player,
            arcaneresearch.api.capabilities.IPlayerKnowledge.EnumKnowledgeType.OBSERVATION,
            arcaneresearch.api.research.ResearchCategories.getResearchCategory("ALCHEMY"), 1);
    }

    @Override
    public String getResearchKey(Player player, Object object) {
        return research;
    }
}