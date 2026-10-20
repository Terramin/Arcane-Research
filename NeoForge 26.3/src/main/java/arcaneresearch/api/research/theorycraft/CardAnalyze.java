package arcaneresearch.api.research.theorycraft;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.ResearchCategory;

import java.util.ArrayList;
import java.util.Random;

public class CardAnalyze extends TheorycraftCard {

    String cat = null;

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
        ArrayList<String> cats = new ArrayList<>();
        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
        if (knowledge == null) return false;

        for (ResearchCategory rc : ResearchCategories.researchCategories.values()) {
            if ("BASICS".equals(rc.key)) continue;
            if (knowledge.getKnowledge(IPlayerKnowledge.EnumKnowledgeType.OBSERVATION, rc.key) > 0) {
                cats.add(rc.key);
            }
        }
        if (!cats.isEmpty()) {
            cat = cats.get(r.nextInt(cats.size()));
        }
        return cat != null;
    }

    @Override
    public int getInspirationCost() {
        return 2;
    }

    @Override
    public String getLocalizedName() {

        return Component.translatable("card.analyze.name", ChatFormatting.DARK_BLUE + "" + ChatFormatting.BOLD
                + Component.translatable("tc.research_category." + cat).getString()
                + ChatFormatting.RESET).getString();
    }

    @Override
    public String getLocalizedText() {

        return Component.translatable("card.analyze.text", ChatFormatting.BOLD
                + Component.translatable("tc.research_category." + cat).getString() + ChatFormatting.RESET,
                ChatFormatting.BOLD + Component.translatable("tc.research_category.BASICS").getString()
                        + ChatFormatting.RESET).getString();
    }

    @Override
    public boolean activate(Player player, ResearchTableData data) {
        if (cat == null) return false;
        ResearchCategory rc = ResearchCategories.getResearchCategory(cat);
        if (rc == null) return false;

        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
        if (knowledge == null) return false;

        int k = knowledge.getKnowledge(IPlayerKnowledge.EnumKnowledgeType.OBSERVATION, rc.key);
        if (k >= 1) {
            data.addTotal("BASICS", 5);
            knowledge.addKnowledge(IPlayerKnowledge.EnumKnowledgeType.OBSERVATION, rc.key, -IPlayerKnowledge.EnumKnowledgeType.OBSERVATION.getProgression());
            data.addTotal(cat, Mth.nextInt(player.getRandom(), 25, 50));
            return true;
        }
        return false;
    }
}