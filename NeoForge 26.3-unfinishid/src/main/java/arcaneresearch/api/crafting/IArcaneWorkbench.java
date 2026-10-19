package arcaneresearch.api.crafting;

import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.RecipeInput;

// NEOFORGE_PORT: vanilla Recipe<T extends RecipeInput> (26.x) bounds require the workbench
//   inventory to be a RecipeInput. getItem(int) already matches Container;
//   size()/isEmpty() are bridged explicitly (Container has no size(), and its abstract
//   isEmpty() would clash with RecipeInput's default). Implementers unchanged.
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
