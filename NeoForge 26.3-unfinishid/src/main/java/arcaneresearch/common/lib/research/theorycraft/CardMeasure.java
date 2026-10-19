package arcaneresearch.common.lib.research.theorycraft;

import net.minecraft.world.entity.player.Player;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;

public class CardMeasure extends TheorycraftCard {

    @Override
    public int getInspirationCost() {
        return 1;
    }

    @Override
    public String getResearchCategory() {
        return "INFUSION";
    }

    @Override
    public String getLocalizedName() {
        return "card.measure.name";
    }

    @Override
    public String getLocalizedText() {
        return "card.measure.text";
    }

    @Override
    public boolean activate(Player player, ResearchTableData data) {

        data.addTotal(getResearchCategory(), 15);
        data.bonusDraws++;
        return true;
    }
}
