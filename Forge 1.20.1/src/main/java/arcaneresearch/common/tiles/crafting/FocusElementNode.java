package arcaneresearch.common.tiles.crafting;
import net.minecraft.nbt.CompoundTag;
import arcaneresearch.api.casters.FocusEngine;
import arcaneresearch.api.casters.FocusNode;
import arcaneresearch.api.casters.IFocusElement;
import java.util.HashMap;
public class FocusElementNode {
    public int x;
    public int y;
    public int id;
    public boolean target;
    public boolean trajectory;
    public int parent;
    public int[] children;
    public float complexityMultiplier;
    public FocusNode node;
    public FocusElementNode() {
        target = false;
        trajectory = false;
        parent = -1;
        children = new int[0];
        complexityMultiplier = 1.0f;
        node = null;
    }
    public float getPower(HashMap<Integer, FocusElementNode> data) {
        if (node == null) {
            return 1.0f;
        }
        float pow = node.getPowerMultiplier();
        FocusElementNode p = data.get(parent);
        if (p != null && p.node != null) {
            pow *= p.getPower(data);
        }
        return pow;
    }
    public void deserialize(CompoundTag nbt) {
        x = nbt.getInt("x");
        y = nbt.getInt("y");
        id = nbt.getInt("id");
        target = nbt.getBoolean("target");
        trajectory = nbt.getBoolean("trajectory");
        parent = nbt.getInt("parent");
        children = nbt.getIntArray("children");
        complexityMultiplier = nbt.getFloat("complexity");
        IFocusElement fe = FocusEngine.getElement(nbt.getString("key"));
        if (fe instanceof FocusNode focusNode) {
            try {
                node = focusNode.getClass().getDeclaredConstructor().newInstance();
                node.initialize();
                if (node.getSettingList() != null) {
                    for (String settingKey : node.getSettingList()) {
                        if (nbt.contains("setting." + settingKey)) {
                            node.getSetting(settingKey).setValue(nbt.getInt("setting." + settingKey));
                        }
                    }
                }
            } catch (Exception e) {
                node = focusNode;
                node.initialize();
            }
        }
    }
    public CompoundTag serialize() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("x", x);
        nbt.putInt("y", y);
        nbt.putInt("id", id);
        nbt.putBoolean("target", target);
        nbt.putBoolean("trajectory", trajectory);
        nbt.putInt("parent", parent);
        nbt.putIntArray("children", children);
        nbt.putFloat("complexity", complexityMultiplier);
        if (node != null) {
            nbt.putString("key", node.getKey());
            if (node.getSettingList() != null) {
                for (String settingKey : node.getSettingList()) {
                    nbt.putInt("setting." + settingKey, node.getSettingValue(settingKey));
                }
            }
        }
        return nbt;
    }
}
