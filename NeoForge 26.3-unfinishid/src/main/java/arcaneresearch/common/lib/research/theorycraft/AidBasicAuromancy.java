package arcaneresearch.common.lib.research.theorycraft;

import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.research.theorycraft.ITheorycraftAid;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;
import arcaneresearch.init.ModBlocks;

public class AidBasicAuromancy implements ITheorycraftAid {

    @Override
    public Object getAidObject() {
        return new ItemStack(ModBlocks.FOCAL_MANIPULATOR.get());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends TheorycraftCard>[] getCards() {
        return new Class[]{CardFocus.class, CardAwareness.class, CardSpellbinding.class};
    }
}
