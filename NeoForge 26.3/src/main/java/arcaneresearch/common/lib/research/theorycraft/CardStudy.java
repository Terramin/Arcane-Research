package arcaneresearch.common.lib.research.theorycraft;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;

import java.util.List;
import java.util.Random;

public class CardStudy extends TheorycraftCard {

    private String cat = "BASICS";

    @Override
    public CompoundTag serialize() {
        CompoundTag nbt = super.serialize();
        nbt.putString("cat", cat);
        return nbt;
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        super.deserialize(nbt);
        cat = nbt.getStringOr("cat", "");
    }

    @Override
    public String getResearchCategory() {
        return cat;
    }

    @Override
    public boolean initialize(Player player, ResearchTableData data) {
        Random r = new Random(getSeed());
        List<String> list = data.getAvailableCategories(player);
        if (list.isEmpty()) return false;
        cat = list.get(r.nextInt(list.size()));
        return cat != null;
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
        return "card.study.name";
    }

    @Override
    public String getLocalizedText() {
        return "card.study.text";
    }

    @Override
    public boolean activate(Player player, ResearchTableData data) {
        data.addTotal(cat, Mth.nextInt(player.getRandom(), 15, 25));
        return true;
    }
}
