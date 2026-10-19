package arcaneresearch.api.research;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import arcaneresearch.api.capabilities.IPlayerKnowledge.EnumKnowledgeType;
public class ResearchStage {
    private String text;
    private ResourceLocation[] recipes;
    private Object[] obtain;
    private Object[] craft;
    private int[] craftReference;
    private Knowledge[] know;
    private String[] research;
    private String[] researchIcon;
    private String[] discovery;
    private boolean hex;
    private String[] structure;
    private int warp;
    public String getText() {
        return text;
    }
    public Component getTextLocalized() {
        return Component.translatable(getText());
    }
    public void setText(String text) {
        this.text = text;
    }
    public ResourceLocation[] getRecipes() {
        return recipes;
    }
    public void setRecipes(ResourceLocation[] recipes) {
        this.recipes = recipes;
    }
    public Object[] getObtain() {
        return obtain;
    }
    public void setObtain(Object[] obtain) {
        this.obtain = obtain;
    }
    public Object[] getCraft() {
        return craft;
    }
    public void setCraft(Object[] craft) {
        this.craft = craft;
    }
    public int[] getCraftReference() {
        return craftReference;
    }
    public void setCraftReference(int[] craftReference) {
        this.craftReference = craftReference;
    }
    public Knowledge[] getKnow() {
        return know;
    }
    public void setKnow(Knowledge[] know) {
        this.know = know;
    }
    public String[] getResearch() {
        return research;
    }
    public void setResearch(String[] research) {
        this.research = research;
    }
    public String[] getResearchIcon() {
        return researchIcon;
    }
    public void setResearchIcon(String[] researchIcon) {
        this.researchIcon = researchIcon;
    }
    public String[] getStructure() {
        return structure;
    }
    public void setStructure(String[] structure) {
        this.structure = structure;
    }
    public String[] getDiscovery() {
        return discovery;
    }
    public void setDiscovery(String[] discovery) {
        this.discovery = discovery;
    }
    public boolean isHex() {
        return hex;
    }
    public void setHex(boolean hex) {
        this.hex = hex;
    }
    public int getWarp() {
        return warp;
    }
    public void setWarp(int warp) {
        this.warp = warp;
    }
    public static class Knowledge {
        public EnumKnowledgeType type;
        public ResearchCategory category;
        public int amount = 0;
        public Knowledge(EnumKnowledgeType type, ResearchCategory category, int amount) {
            this.type = type;
            this.category = category;
            this.amount = amount;
        }
        public static Knowledge parse(String text) {
            String[] s = text.split(";");
            if (s.length == 2) {
                int num = 0;
                try {
                    num = Integer.parseInt(s[1]);
                } catch (Exception e) {
                }
                EnumKnowledgeType t = EnumKnowledgeType.valueOf(s[0].toUpperCase());
                if (t != null && !t.hasFields() && num > 0) {
                    return new Knowledge(t, null, num);
                }
            } else if (s.length == 3) {
                int num = 0;
                try {
                    num = Integer.parseInt(s[2]);
                } catch (Exception e) {
                }
                EnumKnowledgeType t = EnumKnowledgeType.valueOf(s[0].toUpperCase());
                ResearchCategory f = ResearchCategories.getResearchCategory(s[1].toUpperCase());
                if (t != null && f != null && num > 0) {
                    return new Knowledge(t, f, num);
                }
            }
            return null;
        }
    }
}
