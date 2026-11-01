package arcaneresearch.common.tiles.crafting;

import net.minecraft.nbt.CompoundTag;
import arcaneresearch.api.casters.FocusEngine;
import arcaneresearch.api.casters.FocusNode;
import arcaneresearch.api.casters.IFocusElement;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FocusElementNode {
    private static final int[] EMPTY_CHILDREN = new int[0];
    private static final int MAX_POWER_DEPTH = 256;
    private static final Map<Class<?>, Constructor<?>> CONSTRUCTOR_CACHE = new HashMap<>();

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
        children = EMPTY_CHILDREN;
        complexityMultiplier = 1.0f;
        node = null;
    }

    public float getPower(HashMap<Integer, FocusElementNode> data) {
        if (node == null || data == null) return 1.0f;

        float pow = node.getPowerMultiplier();

        Set<Integer> visited = null;
        FocusElementNode current = this;
        int depth = 0;

        while (current.parent >= 0 && depth++ < MAX_POWER_DEPTH) {
            if (visited == null) visited = new HashSet<>();
            if (!visited.add(current.id)) break;

            FocusElementNode p = data.get(current.parent);

            if (p == null || p.node == null) break;

            pow *= p.node.getPowerMultiplier();
            current = p;
        }

        return pow;
    }

    public void deserialize(CompoundTag nbt) {
        if (nbt == null) return;

        x = nbt.getInt("x");
        y = nbt.getInt("y");
        id = nbt.getInt("id");
        target = nbt.getBoolean("target");
        trajectory = nbt.getBoolean("trajectory");
        parent = nbt.getInt("parent");

        int[] rawChildren = nbt.getIntArray("children");
        children = rawChildren.length == 0 ? EMPTY_CHILDREN : rawChildren;

        complexityMultiplier = nbt.getFloat("complexity");

        node = null;

        IFocusElement fe = FocusEngine.getElement(nbt.getString("key"));

        if (!(fe instanceof FocusNode focusNode)) return;

        FocusNode inst = newInstance(focusNode);

        if (inst == null) {
            node = focusNode;
            try {
                focusNode.initialize();
            } catch (Exception ignored) {
                node = null;
            }
            return;
        }

        try {
            inst.initialize();
        } catch (Exception ignored) {
            node = focusNode;
            return;
        }

        Set<String> settings = inst.getSettingList();

        if (settings != null) {
            for (String settingKey : settings) {
                String nbtKey = "setting." + settingKey;

                if (!nbt.contains(nbtKey)) continue;

                Object setting = inst.getSetting(settingKey);

                if (setting == null) continue;

                try {
                    setting.getClass().getMethod("setValue", int.class)
                            .invoke(setting, nbt.getInt(nbtKey));
                } catch (Exception ignored) {
                }
            }
        }

        node = inst;
    }

    private static FocusNode newInstance(FocusNode prototype) {
        Class<?> clazz = prototype.getClass();
        Constructor<?> ctor = CONSTRUCTOR_CACHE.get(clazz);

        if (ctor == null) {
            try {
                ctor = clazz.getDeclaredConstructor();
                ctor.setAccessible(true);
                CONSTRUCTOR_CACHE.put(clazz, ctor);
            } catch (Exception e) {
                return null;
            }
        }

        try {
            return (FocusNode) ctor.newInstance();
        } catch (Exception e) {
            return null;
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

        if (node == null) return nbt;

        nbt.putString("key", node.getKey());

        Set<String> settings = node.getSettingList();

        if (settings == null) return nbt;

        for (String settingKey : settings) {
            try {
                nbt.putInt("setting." + settingKey, node.getSettingValue(settingKey));
            } catch (Exception ignored) {
            }
        }

        return nbt;
    }
}