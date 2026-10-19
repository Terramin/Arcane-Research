package arcaneresearch.common.lib.research.theorycraft;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.research.theorycraft.ITheorycraftAid;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;
import arcaneresearch.init.ModBlocks;
public class AidBasicEldritch implements ITheorycraftAid {
    @Override
    public Object getAidObject() {
        return new ItemStack(ModBlocks.ELDRITCH_STONE_TILE.get());
    }
    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends TheorycraftCard>[] getCards() {
        return new Class[]{CardRealization.class, CardRevelation.class, CardTruth.class};
    }
}
