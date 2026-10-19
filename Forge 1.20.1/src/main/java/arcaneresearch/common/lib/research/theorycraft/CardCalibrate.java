package arcaneresearch.common.lib.research.theorycraft;
import net.minecraft.world.entity.player.Player;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;
public class CardCalibrate extends TheorycraftCard {
    @Override
    public int getInspirationCost() {
        return 1;
    }
    @Override
    public String getResearchCategory() {
        return "ARTIFICE";
    }
    @Override
    public String getLocalizedName() {
        return "card.calibrate.name";
    }
    @Override
    public String getLocalizedText() {
        return "card.calibrate.text";
    }
    @Override
    public boolean activate(Player player, ResearchTableData data) {
        data.addTotal(getResearchCategory(), 15);
        data.bonusDraws++;
        return true;
    }
}
