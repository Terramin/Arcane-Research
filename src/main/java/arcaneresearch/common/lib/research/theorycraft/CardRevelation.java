package arcaneresearch.common.lib.research.theorycraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;
public class CardRevelation extends TheorycraftCard {
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
        return "card.revelation.name";
    }
    @Override
    public String getLocalizedText() {
        return "card.revelation.text";
    }
    @Override
    public boolean activate(Player player, ResearchTableData data) {
        String[] cats = arcaneresearch.api.research.ResearchCategories.researchCategories.keySet()
                .toArray(new String[0]);
        data.addTotal(cats[player.getRandom().nextInt(cats.length)],
                Mth.nextInt(player.getRandom(), 5, 10));
        data.addTotal("ELDRITCH", 30);
        ArcaneresearchApi.internalMethods.addWarpToPlayer(player, 5, IPlayerWarp.EnumWarpType.TEMPORARY);
        ArcaneresearchApi.internalMethods.addWarpToPlayer(player, 1, IPlayerWarp.EnumWarpType.NORMAL);
        data.penaltyStart++;
        return true;
    }
}
