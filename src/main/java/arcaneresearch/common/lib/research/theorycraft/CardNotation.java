package arcaneresearch.common.lib.research.theorycraft;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;

public class CardNotation extends TheorycraftCard {

    private String cat1, cat2;

    @Override
    public CompoundTag serialize() {
        CompoundTag nbt = super.serialize();
        if (cat1 != null) nbt.putString("cat1", cat1);
        if (cat2 != null) nbt.putString("cat2", cat2);
        return nbt;
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        super.deserialize(nbt);
        cat1 = nbt.getStringOr("cat1", "");
        cat2 = nbt.getStringOr("cat2", "");
    }

    @Override
    public boolean isAidOnly() {
        return true;
    }

    @Override
    public int getInspirationCost() {
        return 1;
    }

    @Override
    public String getLocalizedName() {
        return "card.notation.name";
    }

    @Override
    public String getLocalizedText() {
        return "card.notation.text";
    }

    @Override
    public boolean initialize(Player player, ResearchTableData data) {
        if (data.categoryTotals.size() < 2) return false;

        int lVal = Integer.MAX_VALUE;
        String lKey = "";
        int hVal = 0;
        String hKey = "";

        for (String category : data.categoryTotals.keySet()) {
            int q = data.getTotal(category);
            if (q < lVal) {
                lVal = q;
                lKey = category;
            }
            if (q > hVal) {
                hVal = q;
                hKey = category;
            }
        }

        if (hKey.equals(lKey) || lVal <= 0) return false;
        cat1 = lKey;
        cat2 = hKey;
        return true;
    }

    @Override
    public boolean activate(Player player, ResearchTableData data) {
        if (cat1 == null || cat2 == null) return false;

        int lVal = data.getTotal(cat1);
        data.addTotal(cat1, -lVal);
        data.addTotal(cat2, lVal / 2 + Mth.nextInt(player.getRandom(), 0, lVal / 2));
        return true;
    }
}
