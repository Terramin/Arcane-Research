package arcaneresearch.api.wands;

import java.util.LinkedHashMap;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class WandRod {
    private String tag;
    private int craftCost;
    int capacity;
    protected Identifier texture;
    ItemStack item;
    IWandRodOnUpdate onUpdate;
    private boolean isStaff = false;
    private boolean potencyBonus = false;

    public static LinkedHashMap<String, WandRod> rods = new LinkedHashMap<>();

    public static WandRod getRod(String tag) {
        return rods.get(tag);
    }

    public WandRod(String tag, int capacity, ItemStack item, int craftCost, Identifier texture) {
        setTag(tag);
        this.capacity = capacity;
        this.texture = texture;
        this.item = item;
        setCraftCost(craftCost);
        rods.put(tag, this);
    }

    public WandRod(String tag, int capacity, ItemStack item, int craftCost, IWandRodOnUpdate onUpdate,
            Identifier texture) {
        setTag(tag);
        this.capacity = capacity;
        this.texture = texture;
        this.item = item;
        setCraftCost(craftCost);
        rods.put(tag, this);
        this.onUpdate = onUpdate;
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Identifier getTexture() {
        return this.texture;
    }

    public void setTexture(Identifier texture) {
        this.texture = texture;
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

    public IWandRodOnUpdate getOnUpdate() {
        return this.onUpdate;
    }

    public void setOnUpdate(IWandRodOnUpdate onUpdate) {
        this.onUpdate = onUpdate;
    }

    public String getResearch() {
        return "ROD_" + getTag();
    }

    public boolean isStaff() {
        return this.isStaff;
    }

    public void setStaff(boolean isStaff) {
        this.isStaff = isStaff;
    }

    public boolean hasPotencyBonus() {
        return this.potencyBonus;
    }

    public void setPotencyBonus(boolean potencyBonus) {
        this.potencyBonus = potencyBonus;
    }
}
