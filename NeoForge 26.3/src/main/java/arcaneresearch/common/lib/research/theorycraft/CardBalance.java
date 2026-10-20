package arcaneresearch.common.lib.research.theorycraft;

import net.minecraft.world.entity.player.Player;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;

public class CardBalance extends TheorycraftCard {

    @Override
    public int getInspirationCost() {
        return 1;
    }

    @Override
    public boolean isAidOnly() {
        return true;
    }

    @Override
    public String getLocalizedName() {
        return "card.balance.name";
    }

    @Override
    public String getLocalizedText() {
        return "card.balance.text";
    }

    @Override
    public boolean initialize(Player player, ResearchTableData data) {

        int total = 0;
        int size = 0;
        for (String c : data.categoryTotals.keySet()) {
            if (data.categoriesBlocked.contains(c)) continue;
            total += data.categoryTotals.get(c);
            size++;
        }
        return data.categoriesBlocked.size() < data.categoryTotals.size() - 1 && total >= size;
    }

    @Override
    public boolean activate(Player player, ResearchTableData data) {
        int total = 0;
        int size = 0;
        for (String c : data.categoryTotals.keySet()) {
            if (data.categoriesBlocked.contains(c)) continue;
            total += data.categoryTotals.get(c);
            size++;
        }
        if (data.categoriesBlocked.size() >= data.categoryTotals.size() - 1 || total < size) {
            return false;
        }

        for (String category : data.categoryTotals.keySet()) {
            if (data.categoriesBlocked.contains(category)) continue;
            data.categoryTotals.put(category, total / size);
        }

        data.addTotal("BASICS", 5);
        data.penaltyStart++;
        return true;
    }
}
