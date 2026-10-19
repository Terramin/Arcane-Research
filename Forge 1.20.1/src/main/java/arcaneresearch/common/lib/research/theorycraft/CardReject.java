package arcaneresearch.common.lib.research.theorycraft;
import net.minecraft.world.entity.player.Player;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;
@Deprecated(forRemoval = true)
public class CardReject extends TheorycraftCard {
    @Override
    public int getInspirationCost() {
        return 0;
    }
    @Override
    public String getLocalizedName() {
        return "card.reject.name";
    }
    @Override
    public String getLocalizedText() {
        return "card.reject.text";
    }
    @Override
    public boolean activate(Player player, ResearchTableData data) {
        data.addTotal("BASICS", 3);
        return true;
    }
}
