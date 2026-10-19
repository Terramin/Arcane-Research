package arcaneresearch.api.research;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ResearchAddendum {

    private String text;

    private Identifier[] recipes;

    private String[] research;

    public String getText() {
        return text;
    }

    public Component getTextLocalized() {
        return Component.translatable(getText());
    }

    public void setText(String text) {
        this.text = text;
    }

    public Identifier[] getRecipes() {
        return recipes;
    }

    public void setRecipes(Identifier[] recipes) {
        this.recipes = recipes;
    }

    public String[] getResearch() {
        return research;
    }

    public void setResearch(String[] research) {
        this.research = research;
    }
}
