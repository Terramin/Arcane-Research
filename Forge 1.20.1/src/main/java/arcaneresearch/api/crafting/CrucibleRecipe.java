package arcaneresearch.api.crafting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
public class CrucibleRecipe implements IArcaneresearchRecipe {
    private final ItemStack recipeOutput;
    private Ingredient catalyst;
    private AspectList aspects;
    private final String research;
    private String group = "";
    public int hash;
    public CrucibleRecipe(String researchKey, ItemStack result, Object catalyst, AspectList aspects) {
        this.recipeOutput = result;
        this.aspects = aspects;
        this.research = researchKey;
        this.catalyst = ArcaneresearchApiHelper.getIngredient(catalyst);
        if (this.catalyst == null) {
            throw new RuntimeException("Invalid crucible recipe catalyst: " + catalyst);
        }
        generateHash();
    }
    private void generateHash() {
        StringBuilder hc = new StringBuilder(research);
        hc.append(recipeOutput.toString());
        if (recipeOutput.hasTag()) {
            hc.append(recipeOutput.getTag().toString());
        }
        for (ItemStack is : catalyst.getItems()) {
            hc.append(is.toString());
            if (is.hasTag()) {
                hc.append(is.getTag().toString());
            }
        }
        hash = hc.toString().hashCode();
    }
    public boolean matches(AspectList crucibleAspects, ItemStack catalystStack) {
        if (!catalyst.test(catalystStack)) return false;
        if (crucibleAspects == null) return false;
        for (Aspect aspect : aspects.getAspects()) {
            if (crucibleAspects.getAmount(aspect) < aspects.getAmount(aspect)) {
                return false;
            }
        }
        return true;
    }
    public boolean catalystMatches(ItemStack catalystStack) {
        return catalyst.test(catalystStack);
    }
    public AspectList removeMatching(AspectList crucibleAspects) {
        AspectList result = new AspectList();
        result.aspects.putAll(crucibleAspects.aspects);
        for (Aspect aspect : aspects.getAspects()) {
            result.remove(aspect, aspects.getAmount(aspect));
        }
        return result;
    }
    public ItemStack getRecipeOutput() {
        return recipeOutput.copy();
    }
    @Override
    public String getResearch() {
        return research;
    }
    public Ingredient getCatalyst() {
        return catalyst;
    }
    public void setCatalyst(Ingredient catalyst) {
        this.catalyst = catalyst;
    }
    public AspectList getAspects() {
        return aspects;
    }
    public void setAspects(AspectList aspects) {
        this.aspects = aspects;
    }
    public String getGroup() {
        return group;
    }
    public CrucibleRecipe setGroup(ResourceLocation location) {
        this.group = location.toString();
        return this;
    }
    public CrucibleRecipe setGroup(String group) {
        this.group = group;
        return this;
    }
}
