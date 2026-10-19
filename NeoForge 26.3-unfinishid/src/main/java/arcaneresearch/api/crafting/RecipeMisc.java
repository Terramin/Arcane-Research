package arcaneresearch.api.crafting;

import net.minecraft.world.item.ItemStack;

public class RecipeMisc {

    public enum MiscRecipeType {
        SMELTING
    }

    private MiscRecipeType type;
    private ItemStack input;
    private ItemStack output;

    public RecipeMisc(ItemStack input, ItemStack output, MiscRecipeType type) {
        this.input = input;
        this.output = output;
        this.type = type;
    }

    public MiscRecipeType getType() {
        return type;
    }

    public void setType(MiscRecipeType type) {
        this.type = type;
    }

    public ItemStack getInput() {
        return input;
    }

    public void setInput(ItemStack input) {
        this.input = input;
    }

    public ItemStack getOutput() {
        return output;
    }

    public void setOutput(ItemStack output) {
        this.output = output;
    }
}
