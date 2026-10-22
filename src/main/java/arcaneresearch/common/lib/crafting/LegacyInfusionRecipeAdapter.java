package arcaneresearch.common.lib.crafting;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.crafting.InfusionRecipe;

import java.util.List;

public class LegacyInfusionRecipeAdapter extends InfusionRecipeType {

    private final InfusionRecipe legacy;

    public LegacyInfusionRecipeAdapter(Identifier id, InfusionRecipe legacy) {
        
        
        
        
        super(legacy.getGroup() == null ? "" : legacy.getGroup(),
                legacy.getRecipeInput(),
                legacy.getComponents(), legacy.getAspects() == null
                        ? new AspectList() : legacy.getAspects(),
                legacy.getRecipeOutput() instanceof ItemStack stack && !stack.isEmpty()
                        ? ItemStackTemplate.fromStack(stack) : null,
                legacy.getResearch() == null ? "" : legacy.getResearch(),
                legacy.instability);
        this.legacy = legacy;
    }

    public InfusionRecipe getLegacyRecipe() {
        return legacy;
    }

    public Object getLegacyOutput(Player player, ItemStack input, List<ItemStack> comps) {
        return legacy.getRecipeOutput(player, input, comps);
    }

    @Override
    public boolean matchesInfusion(List<ItemStack> pedestalItems, ItemStack centralItemStack,
                                   Level level, Player player) {
        try {
            return legacy.matches(pedestalItems, centralItemStack, level, player);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public ItemStack getRecipeOutput(Player player, ItemStack input, List<ItemStack> pedestalItems) {
        Object out = getLegacyOutput(player, input, pedestalItems);
        if (out instanceof ItemStack stack) {
            return stack.copy();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public AspectList getAspects(Player player, ItemStack input, List<ItemStack> pedestalItems) {
        try {
            AspectList al = legacy.getAspects(player, input, pedestalItems);
            return al == null ? new AspectList() : al;
        } catch (Exception e) {
            return new AspectList();
        }
    }

    @Override
    public int getInstability(Player player, ItemStack input, List<ItemStack> pedestalItems) {
        try {
            return legacy.getInstability(player, input, pedestalItems);
        } catch (Exception e) {
            return legacy.instability;
        }
    }

    @Override
    public net.minecraft.world.item.crafting.RecipeSerializer<InfusionRecipeType> getSerializer() {
        return InfusionRecipeType.SERIALIZER;
    }
}
