package arcaneresearch.common.lib.research.theorycraft;
import net.minecraft.world.entity.player.Player;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;
public class CardPonder extends TheorycraftCard {
    @Override
    public int getInspirationCost() {
        return 2;
    }
    @Override
    public String getLocalizedName() {
        return "card.ponder.name";
    }
    @Override
    public String getLocalizedText() {
        return "card.ponder.text";
    }
    @Override
    public boolean initialize(Player player, ResearchTableData data) {
        return data.categoriesBlocked.size() < data.categoryTotals.size();
    }
    @Override
    public boolean activate(Player player, ResearchTableData data) {
        int a = 25;
        int tries = 0;
        while (a > 0 && tries < 1000) {
            tries++;
            for (String category : data.categoryTotals.keySet()) {
                if (data.categoriesBlocked.contains(category)) {
                    if (data.categoryTotals.size() <= 1) return false;
                    continue;
                }
                data.addTotal(category, 1);
                a--;
                if (a <= 0) break;
            }
        }
        data.addTotal("BASICS", 5);
        data.bonusDraws++;
        return a != 25;
    }
}
