package arcaneresearch.common.lib.research.theorycraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;
public class CardGlyphs extends TheorycraftCard {
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
        return "card.glyph.name";
    }
    @Override
    public String getLocalizedText() {
        return "card.glyph.text";
    }
    @Override
    public boolean activate(Player player, ResearchTableData data) {
        String[] cats = ResearchCategories.researchCategories.keySet().toArray(new String[0]);
        data.addTotal(cats[player.getRandom().nextInt(cats.length)],
                Mth.nextInt(player.getRandom(), 10, 20));
        data.addTotal("ELDRITCH", Mth.nextInt(player.getRandom(), 10, 20));
        ArcaneresearchApi.internalMethods.addWarpToPlayer(player, 5, IPlayerWarp.EnumWarpType.TEMPORARY);
        return true;
    }
}
