package arcaneresearch.common.lib.research.theorycraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import arcaneresearch.api.research.theorycraft.ITheorycraftAid;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;
public class AidBeacon implements ITheorycraftAid {
    @Override
    public Object getAidObject() {
        return new ItemStack(Blocks.BEACON);
    }
    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends TheorycraftCard>[] getCards() {
        return new Class[]{CardBeacon.class};
    }
}
