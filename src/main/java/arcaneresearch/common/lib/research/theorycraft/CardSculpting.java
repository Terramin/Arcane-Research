package arcaneresearch.common.lib.research.theorycraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;
public class CardSculpting extends TheorycraftCard {
    @Override
    public int getInspirationCost() {
        return 1;
    }
    @Override
    public String getResearchCategory() {
        return "GOLEMANCY";
    }
    @Override
    public String getLocalizedName() {
        return "card.sculpting.name";
    }
    @Override
    public String getLocalizedText() {
        return "card.sculpting.text";
    }
    @Override
    public ItemStack[] getRequiredItems() {
        return new ItemStack[]{new ItemStack(Items.CLAY_BALL)};
    }
    @Override
    public boolean[] getRequiredItemsConsumed() {
        return new boolean[]{true};
    }
    @Override
    public boolean activate(Player player, ResearchTableData data) {
        data.addTotal(getResearchCategory(), 20);
        data.bonusDraws++;
        return true;
    }
}
