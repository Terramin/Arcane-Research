package arcaneresearch.common.lib.research.theorycraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;
public class CardTruth extends TheorycraftCard {
    @Override
    public int getInspirationCost() {
        return 1;
    }
    @Override
    public String getResearchCategory() {
        return "ELDRITCH";
    }
    @Override
    public String getLocalizedName() {
        return "card.truth.name";
    }
    @Override
    public String getLocalizedText() {
        return "card.truth.text";
    }
    @Override
    public boolean activate(Player player, ResearchTableData data) {
        data.addTotal("ELDRITCH", Mth.nextInt(player.getRandom(), 10, 25));
        data.bonusDraws++;
        ArcaneresearchApi.internalMethods.addWarpToPlayer(player, 3, IPlayerWarp.EnumWarpType.TEMPORARY);
        return true;
    }
}
