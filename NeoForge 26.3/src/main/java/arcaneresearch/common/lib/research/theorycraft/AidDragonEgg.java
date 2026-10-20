package arcaneresearch.common.lib.research.theorycraft;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import arcaneresearch.api.research.theorycraft.ITheorycraftAid;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;

public class AidDragonEgg implements ITheorycraftAid {

    @Override
    public Object getAidObject() {
        return new ItemStack(Blocks.DRAGON_EGG);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends TheorycraftCard>[] getCards() {
        return new Class[]{CardDragonEgg.class};
    }
}
