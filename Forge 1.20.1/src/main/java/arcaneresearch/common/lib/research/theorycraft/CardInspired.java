package arcaneresearch.common.lib.research.theorycraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;
public class CardInspired extends TheorycraftCard {
    private String cat = null;
    private int amt;
    @Override
    public CompoundTag serialize() {
        CompoundTag nbt = super.serialize();
        if (cat != null) nbt.putString("cat", cat);
        nbt.putInt("amt", amt);
        return nbt;
    }
    @Override
    public void deserialize(CompoundTag nbt) {
        super.deserialize(nbt);
        cat = nbt.getString("cat");
        amt = nbt.getInt("amt");
    }
    @Override
    public String getResearchCategory() {
        return cat;
    }
    @Override
    public boolean initialize(Player player, ResearchTableData data) {
        if (data.categoryTotals.isEmpty()) return false;
        int hVal = 0;
        String hKey = "";
        for (String category : data.categoryTotals.keySet()) {
            int q = data.getTotal(category);
            if (q > hVal) {
                hVal = q;
                hKey = category;
            }
        }
        cat = hKey;
        amt = 10 + (hVal / 2);
        return true;
    }
    @Override
    public int getInspirationCost() {
        return 2;
    }
    @Override
    public String getLocalizedName() {
        return "card.inspired.name";
    }
    @Override
    public String getLocalizedText() {
        return "card.inspired.text";
    }
    @Override
    public boolean activate(Player player, ResearchTableData data) {
        if (cat == null || cat.isEmpty()) return false;
        data.addTotal(cat, amt);
        return true;
    }
}
