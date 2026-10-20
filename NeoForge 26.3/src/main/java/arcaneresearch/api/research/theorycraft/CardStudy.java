package arcaneresearch.api.research.theorycraft;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Random;

public class CardStudy extends TheorycraftCard {

    String cat = "BASICS";

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

        return Component.translatable("card.study.name", ChatFormatting.DARK_BLUE + "" + ChatFormatting.BOLD
                + Component.translatable("tc.research_category." + cat).getString()
                + ChatFormatting.RESET).getString();
    }

    @Override
    public String getLocalizedText() {

        return Component.translatable("card.study.text", ChatFormatting.BOLD
                + Component.translatable("tc.research_category." + cat).getString()
                + ChatFormatting.RESET).getString();
    }

    @Override
    public boolean activate(Player player, ResearchTableData data) {
        data.addTotal(cat, Mth.nextInt(player.getRandom(), 15, 25));
        return true;
    }
}