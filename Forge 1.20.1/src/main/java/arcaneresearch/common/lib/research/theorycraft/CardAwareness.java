package arcaneresearch.common.lib.research.theorycraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;
public class CardAwareness extends TheorycraftCard {
    @Override
    public int getInspirationCost() {
        return 1;
    }
    @Override
    public String getResearchCategory() {
        return "AUROMANCY";
    }
    @Override
    public String getLocalizedName() {
        return "card.awareness.name";
    }
    @Override
    public String getLocalizedText() {
        return "card.awareness.text";
    }
    @Override
    public boolean activate(Player player, ResearchTableData data) {
        data.addTotal(getResearchCategory(), 20);
        if (player.getRandom().nextFloat() < 0.33f) {
            data.addTotal("ELDRITCH", Mth.nextInt(player.getRandom(), 1, 5));
            ArcaneresearchApi.internalMethods.addWarpToPlayer(player, 1, IPlayerWarp.EnumWarpType.NORMAL);
        }
        return true;
    }
}
