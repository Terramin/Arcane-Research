package arcaneresearch.api.wands;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.aspects.Aspect;
public class WandCap {
    private String tag;
    private int craftCost;
    float baseCostModifier;
    int chargeBonus;
    float rechargeMultiplier = 1.0F;
    List<Aspect> specialCostModifierAspects;
    float specialCostModifier;
    ResourceLocation texture;
    ItemStack item;
    public static LinkedHashMap<String, WandCap> caps = new LinkedHashMap<>();
    public WandCap(String tag, float discount, int charge, ItemStack item, int craftCost, ResourceLocation texture) {
        setTag(tag);
        this.baseCostModifier = discount;
        this.specialCostModifierAspects = null;
        this.texture = texture;
        this.item = item;
        this.chargeBonus = charge;
        setCraftCost(craftCost);
        caps.put(tag, this);
    }
    public WandCap(String tag, float discount, int charge, List<Aspect> specialAspects, float discountSpecial,
            ItemStack item, int craftCost, ResourceLocation texture) {
        setTag(tag);
        this.baseCostModifier = discount;
        this.specialCostModifierAspects = specialAspects;
        this.specialCostModifier = discountSpecial;
        this.texture = texture;
        this.item = item;
        this.chargeBonus = charge;
        setCraftCost(craftCost);
        caps.put(tag, this);
    }
    public float getBaseCostModifier() {
        return this.baseCostModifier;
    }
    public List<Aspect> getSpecialCostModifierAspects() {
        return this.specialCostModifierAspects;
    }
    public float getSpecialCostModifier() {
        return this.specialCostModifier;
    }
    public int getChargeBonus() {
        return this.chargeBonus;
    }
    public float getRechargeMultiplier() {
        return this.rechargeMultiplier;
    }
    public void setRechargeMultiplier(float rechargeMultiplier) {
        this.rechargeMultiplier = rechargeMultiplier;
    }
    public ResourceLocation getTexture() {
        return this.texture;
    }
    public void setTexture(ResourceLocation texture) {
        this.texture = texture;
    }
    public String getTag() {
        return this.tag;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }
    public ItemStack getItem() {
        return this.item;
    }
    public void setItem(ItemStack item) {
        this.item = item;
    }
    public int getCraftCost() {
        return this.craftCost;
    }
    public void setCraftCost(int craftCost) {
        this.craftCost = craftCost;
    }
    public String getResearch() {
        return "CAP_" + getTag();
    }
}
