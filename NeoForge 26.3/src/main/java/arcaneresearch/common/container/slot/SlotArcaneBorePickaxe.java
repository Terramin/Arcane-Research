package arcaneresearch.common.container.slot;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import arcaneresearch.common.entities.construct.EntityArcaneBore;

@Deprecated
public class SlotArcaneBorePickaxe extends SlotMobEquipment {
    public SlotArcaneBorePickaxe(EntityArcaneBore bore, int slot, int x, int y) {
        super(bore, slot, x, y);
    }

    public static boolean isValidPickaxe(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        // NEOFORGE_PORT: PickaxeItem class removed in 26.x (tools are data-driven);
        // tag + correct-tool checks below cover the same items.
        if (stack.is(ItemTags.PICKAXES)) {
            return true;
        }
        return stack.isCorrectToolForDrops(Blocks.STONE.defaultBlockState());
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return isValidPickaxe(stack);
    }

    @Override
    public void setChanged() {
    }
}
