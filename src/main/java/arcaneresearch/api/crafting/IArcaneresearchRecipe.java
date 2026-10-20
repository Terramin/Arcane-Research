package arcaneresearch.api.crafting;
public interface IArcaneresearchRecipe {
    String getResearch();
    default String getGroup() {
        return null;
    }
}
