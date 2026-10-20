package arcaneresearch.common.lib.research.theorycraft;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;
import arcaneresearch.common.tiles.crafting.TileResearchTable;

public class CardScripting extends TheorycraftCard {

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
        return "card.scripting.name";
    }

    @Override
    public String getLocalizedText() {
        return "card.scripting.text";
    }

    @Override
    public boolean activate(Player player, ResearchTableData data) {

        if (data.table instanceof TileResearchTable table) {
            ItemStack tools = table.getItem(TileResearchTable.SLOT_SCRIBING_TOOLS);
            ItemStack paper = table.getItem(TileResearchTable.SLOT_PAPER);
            if (!tools.isEmpty() && tools.getDamageValue() < tools.getMaxDamage()
                    && !paper.isEmpty()) {
                table.consumeInkFromTable();
                table.consumePaperFromTable();
                data.addTotal(getResearchCategory(), 25);
                return true;
            }
        }
        return false;
    }
}
