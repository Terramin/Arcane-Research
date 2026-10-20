package arcaneresearch.api.casters;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.HitResult;
import arcaneresearch.api.aspects.Aspect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public abstract class FocusNode implements IFocusElement {

    public FocusNode() {
        super();
        initialize();
    }

    public String getUnlocalizedName() {
        return getKey() + ".name";
    }

    public String getUnlocalizedText() {
        return getKey() + ".text";
    }

    public abstract int getComplexity();

    public abstract Aspect getAspect();

    public abstract EnumSupplyType[] mustBeSupplied();

    public abstract EnumSupplyType[] willSupply();

    public boolean canSupply(EnumSupplyType type) {
        if (willSupply() != null) {
            for (EnumSupplyType st : willSupply()) {
                if (st == type) return true;
            }
        }
        return false;
    }

    public enum EnumSupplyType {

        TARGET,

        TRAJECTORY
    }

    public HitResult[] supplyTargets() {
        return null;
    }

    public Trajectory[] supplyTrajectories() {
        return null;
    }

    FocusPackage pack;

    public void setPackage(FocusPackage pack) {
        this.pack = pack;
    }

    public FocusPackage getPackage() {
        return pack;
    }

    public FocusPackage getRemainingPackage() {
        FocusPackage p = getPackage();
        if (p == null || p.nodes == null) return null;

        List<IFocusElement> remaining = p.nodes.subList(p.index + 1, p.nodes.size());
        List<IFocusElement> newList = Collections.synchronizedList(new ArrayList<>());
        newList.addAll(remaining);

        FocusPackage newPack = new FocusPackage();
        newPack.setUniqueID(p.getUniqueID());
        newPack.world = p.world;
        newPack.multiplyPower(p.getPower());
        newPack.nodes = newList;
        newPack.setCasterUUID(p.getCasterUUID());

        return newList.isEmpty() ? null : newPack;
    }

    private FocusNode parent;

    public FocusNode getParent() {
        return parent;
    }

    public void setParent(FocusNode parent) {
        this.parent = parent;
    }

    HashMap<String, NodeSetting> settings = new HashMap<>();

    public Set<String> getSettingList() {
        return settings.keySet();
    }

    public NodeSetting getSetting(String key) {
        return settings.get(key);
    }

    public int getSettingValue(String key) {
        return settings.containsKey(key) ? settings.get(key).getValue() : 0;
    }

    public NodeSetting[] createSettings() {
        return null;
    }

    public void initialize() {
        NodeSetting[] set = createSettings();
        if (set != null) {
            for (NodeSetting setting : set) {
                settings.put(setting.key, setting);
            }
        }
    }

    public float getPowerMultiplier() {
        return 1;
    }

    public boolean isExclusive() {
        return false;
    }

    public void serialize(CompoundTag tag) {

        CompoundTag settingsTag = new CompoundTag();
        for (NodeSetting setting : settings.values()) {
            settingsTag.putInt(setting.key, setting.getValue());
        }
        tag.put("settings", settingsTag);
    }

    public void deserialize(CompoundTag tag) {

        if (tag.contains("settings")) {
            CompoundTag settingsTag = tag.getCompoundOrEmpty("settings");
            for (String key : settings.keySet()) {
                if (settingsTag.contains(key)) {
                    settings.get(key).setValue(settingsTag.getIntOr(key, 0));
                }
            }
        }
    }
}
