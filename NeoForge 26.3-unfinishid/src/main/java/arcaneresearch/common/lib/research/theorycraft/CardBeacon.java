package arcaneresearch.common.lib.research.theorycraft;

import net.minecraft.world.entity.player.Player;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;

public class CardBeacon extends TheorycraftCard {

    @Override
    public boolean isAidOnly() {
        return true;
    }

    @Override
    public int getInspirationCost() {
        return -2;
    }

    @Override
    public String getLocalizedName() {
        return "card.beacon.name";
    }

    @Override
    public String getLocalizedText() {
        return "card.beacon.text";
    }

    @Override
    public boolean activate(Player player, ResearchTableData data) {
        data.bonusDraws++;
        data.penaltyStart++;
        return true;
    }
}
