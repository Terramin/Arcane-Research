package arcaneresearch.discovered.table;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.discovered.note.ItemDiscoveredResearchNote;
import arcaneresearch.discovered.note.DiscoveredNoteData;

/**
 * Паритет TC3 SlotResearchTableOutput: только незавершённые записки
 * под нужные открытия (готовые/потраченные/без discovery — мимо).
 */
public class DiscoveredNoteSlot extends Slot {

    private final Player player;

    public DiscoveredNoteSlot(Container container, int slot, int x, int y, Player player) {
        super(container, slot, x, y);
        this.player = player;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemDiscoveredResearchNote)
                || DiscoveredNoteData.read(stack).isComplete()) {
            return false;
        }
        String key = DiscoveredNoteData.read(stack).key;
        if (key == null || player == null) {
            return true;
        }
        return !DiscoveredTableResearch.isDiscoverySpent(player, key)
                && !ArcaneresearchCapabilities.isResearchComplete(player, key);
    }
}
