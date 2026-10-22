package arcaneresearch.api.crafting;

import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.RecipeInput;





public interface IArcaneWorkbench extends Container, RecipeInput {

    @Override
    default int size() {
        return getContainerSize();
    }

    @Override
    default boolean isEmpty() {
        for (int i = 0; i < size(); i++) {
            if (!getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
