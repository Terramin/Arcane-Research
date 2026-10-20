package arcaneresearch.common.lib.research.theorycraft;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;

public class CardDragonEgg extends TheorycraftCard {

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
        return "card.dragonegg.name";
    }

    @Override
    public String getLocalizedText() {
        return "card.dragonegg.text";
    }

    @Override
    public boolean activate(Player player, ResearchTableData data) {

        String[] cats = ResearchCategories.researchCategories.keySet().toArray(new String[0]);
        for (int a = 0; a < 10; a++) {
            String cat = cats[player.getRandom().nextInt(cats.length)];
            data.addTotal(cat, Mth.nextInt(player.getRandom(), 2, 5));
        }
        return true;
    }
}
