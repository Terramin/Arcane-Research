package arcaneresearch.common.lib.research.theorycraft;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.research.theorycraft.ITheorycraftAid;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;
import arcaneresearch.init.ModBlocks;
public class AidBasicInfusion implements ITheorycraftAid {
    @Override
    public Object getAidObject() {
        return new ItemStack(ModBlocks.INFUSION_MATRIX.get());
    }
    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends TheorycraftCard>[] getCards() {
        return new Class[]{CardMeasure.class, CardChannel.class, CardInfuse.class};
    }
}
