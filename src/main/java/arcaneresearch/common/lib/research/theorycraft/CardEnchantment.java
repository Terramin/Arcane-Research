package arcaneresearch.common.lib.research.theorycraft;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;

public class CardEnchantment extends TheorycraftCard {

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
        return "card.enchantment.name";
    }

    @Override
    public String getLocalizedText() {
        return "card.enchantment.text";
    }

    @Override
    public boolean activate(Player player, ResearchTableData data) {
        if (player.experienceLevel >= 5) {
            player.giveExperienceLevels(-5);
            data.addTotal("INFUSION", Mth.nextInt(player.getRandom(), 15, 20));
            data.addTotal("AUROMANCY", Mth.nextInt(player.getRandom(), 15, 20));
            return true;
        }
        return false;
    }
}
