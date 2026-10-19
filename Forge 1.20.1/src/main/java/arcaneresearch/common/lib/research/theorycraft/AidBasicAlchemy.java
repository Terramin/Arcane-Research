package arcaneresearch.common.lib.research.theorycraft;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.research.theorycraft.ITheorycraftAid;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;
import arcaneresearch.init.ModBlocks;
public class AidBasicAlchemy implements ITheorycraftAid {
    @Override
    public Object getAidObject() {
        return new ItemStack(ModBlocks.CRUCIBLE.get());
    }
    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends TheorycraftCard>[] getCards() {
        return new Class[]{CardConcentrate.class, CardReactions.class, CardSynthesis.class};
    }
}
