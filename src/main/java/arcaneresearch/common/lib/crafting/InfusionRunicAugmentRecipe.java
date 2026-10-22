package arcaneresearch.common.lib.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.RecipeMatcher; 
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.common.lib.events.PlayerEvents;
import arcaneresearch.init.ModItems;
import arcaneresearch.init.ModRecipeSerializers;

import java.util.List;

public class InfusionRunicAugmentRecipe extends InfusionRecipeType {

    public static final Identifier ID =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "runic_augment");

    public InfusionRunicAugmentRecipe() {
        
        
        
        super("", null, baseComponents(), new AspectList(),
                null, "RUNICSHIELDING", 5);
    }

    public InfusionRunicAugmentRecipe(ItemStack in) {
        this();
    }

    private static NonNullList<Ingredient> baseComponents() {
        NonNullList<Ingredient> comps = NonNullList.create();
        comps.add(Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT));
        comps.add(Ingredient.of(ModItems.SALIS_MUNDUS.get()));
        return comps;
    }

     

 
    public static NonNullList<Ingredient> componentsForCharge(int charge) {
        NonNullList<Ingredient> comps = NonNullList.create();
        comps.add(Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT));
        for (int c = 0; c < 1 + charge; c++) {
            comps.add(Ingredient.of(ModItems.SALIS_MUNDUS.get()));
        }
        return comps;
    }

    public static boolean isRunicTarget(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        
        return stack.getItem() instanceof arcaneresearch.api.items.IRunicArmor;
    }

    public NonNullList<Ingredient> getComponents(ItemStack input) {
        return componentsForCharge(PlayerEvents.getRunicCharge(input));
    }

    @Override
    public boolean matchesInfusion(List<ItemStack> pedestalItems, ItemStack central,
                                   Level level, Player player) {
        if (central.isEmpty() || !ArcaneresearchCapabilities.isResearchKnown(player, getResearch())) {
            return false;
        }
        if (!isRunicTarget(central)) {
            return false;
        }
        int charge = PlayerEvents.getRunicCharge(central);
        return RecipeMatcher.findMatches(pedestalItems, componentsForCharge(charge)) != null;
    }

    @Override
    public ItemStack getRecipeOutput(Player player, ItemStack input, List<ItemStack> comps) {
        if (input == null || input.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack out = input.copy();
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, out, t -> t.putByte("TC.RUNIC", (byte) (PlayerEvents.getRunicCharge(input) + 1)));
        return out;
    }

    @Override
    public AspectList getAspects(Player player, ItemStack input, List<ItemStack> comps) {
        AspectList out = new AspectList();
        if (input == null || input.isEmpty()) {
            return out;
        }
        
        int vis = (int) (32.0D * Math.pow(2.0D, PlayerEvents.getRunicCharge(input)));
        if (vis > 0) {
            out.add(Aspect.PROTECT, vis / 2);
            out.add(Aspect.MAGIC, vis / 2);
            out.add(Aspect.ENERGY, vis);
        }
        return out;
    }

    @Override
    public int getInstability(Player player, ItemStack input, List<ItemStack> comps) {
        if (input == null || input.isEmpty()) {
            return 5;
        }
        return 5 + PlayerEvents.getRunicCharge(input) / 2;
    }

    @Override
    public net.minecraft.world.item.crafting.RecipeSerializer<InfusionRecipeType> getSerializer() {
        return ModRecipeSerializers.INFUSION.get();
    }
}
