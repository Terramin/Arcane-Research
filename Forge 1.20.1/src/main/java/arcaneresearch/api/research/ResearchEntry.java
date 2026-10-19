package arcaneresearch.api.research;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.research.ResearchStage.Knowledge;
import java.util.Arrays;
public class ResearchEntry {
    private String key;
    private String category;
    private String name;
    private String[] parents;
    private String[] siblings;
    private int displayColumn;
    private int displayRow;
    private Object[] icons;
    private EnumResearchMeta[] meta;
    private ItemStack[] rewardItem;
    private Knowledge[] rewardKnow;
    private ResearchStage[] stages;
    private ResearchAddendum[] addenda;
    public enum EnumResearchMeta {
        ROUND,
        SPIKY,
        REVERSE,
        HIDDEN,
        AUTOUNLOCK,
        HEX
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public String getName() {
        return name;
    }
    public Component getLocalizedName() {
        return Component.translatable(getName());
    }
    public void setName(String name) {
        this.name = name;
    }
    public String[] getParents() {
        return parents;
    }
    public String[] getParentsClean() {
        if (parents == null) return null;
        String[] out = getParentsStripped();
        for (int i = 0; i < out.length; i++) {
            if (out[i].contains("@")) {
                out[i] = out[i].substring(0, out[i].indexOf("@"));
            }
        }
        return out;
    }
    public String[] getParentsStripped() {
        if (parents == null) return null;
        String[] out = new String[parents.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = "" + parents[i];
            if (out[i].startsWith("~")) {
                out[i] = out[i].substring(1);
            }
        }
        return out;
    }
    public void setParents(String[] parents) {
        this.parents = parents;
    }
    public String[] getSiblings() {
        return siblings;
    }
    public void setSiblings(String[] siblings) {
        this.siblings = siblings;
    }
    public int getDisplayColumn() {
        return displayColumn;
    }
    public void setDisplayColumn(int displayColumn) {
        this.displayColumn = displayColumn;
    }
    public int getDisplayRow() {
        return displayRow;
    }
    public void setDisplayRow(int displayRow) {
        this.displayRow = displayRow;
    }
    public Object[] getIcons() {
        return icons;
    }
    public void setIcons(Object[] icons) {
        this.icons = icons;
    }
    public EnumResearchMeta[] getMeta() {
        return meta;
    }
    public boolean hasMeta(EnumResearchMeta me) {
        return meta != null && Arrays.asList(meta).contains(me);
    }
    public void setMeta(EnumResearchMeta[] meta) {
        this.meta = meta;
    }
    public ResearchStage[] getStages() {
        return stages;
    }
    public void setStages(ResearchStage[] stages) {
        this.stages = stages;
    }
    public ItemStack[] getRewardItem() {
        return rewardItem;
    }
    public void setRewardItem(ItemStack[] rewardItem) {
        this.rewardItem = rewardItem;
    }
    public Knowledge[] getRewardKnow() {
        return rewardKnow;
    }
    public void setRewardKnow(Knowledge[] rewardKnow) {
        this.rewardKnow = rewardKnow;
    }
    public ResearchAddendum[] getAddenda() {
        return addenda;
    }
    public void setAddenda(ResearchAddendum[] addenda) {
        this.addenda = addenda;
    }
}
