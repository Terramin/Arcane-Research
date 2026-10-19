package arcaneresearch.common.lib.research.theorycraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;
public class CardDarkWhispers extends TheorycraftCard {
    @Override
    public boolean isAidOnly() {
        return true;
    }
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
        return "card.darkwhisper.name";
    }
    @Override
    public String getLocalizedText() {
        return "card.darkwhisper.text";
    }
    @Override
    public boolean activate(Player player, ResearchTableData data) {
        int level = player.experienceLevel;
        player.giveExperienceLevels(-(10 + level));
        if (level > 0) {
            for (String category : ResearchCategories.researchCategories.keySet()) {
                if (player.getRandom().nextBoolean()) {
                    continue;
                }
                data.addTotal(category, Mth.nextInt(player.getRandom(), 0, Math.max(1, (int) Math.sqrt(level))));
            }
        }
        data.addTotal("ELDRITCH", Mth.nextInt(player.getRandom(), Math.max(1, level / 5), Math.max(5, level / 2)));
        ArcaneresearchApi.internalMethods.addWarpToPlayer(player, Math.max(1, (int) Math.sqrt(level)), IPlayerWarp.EnumWarpType.NORMAL);
        if (player.getRandom().nextBoolean()) {
            data.bonusDraws++;
        }
        return true;
    }
}
