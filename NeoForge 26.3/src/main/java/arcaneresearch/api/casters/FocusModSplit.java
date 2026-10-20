package arcaneresearch.api.casters;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import arcaneresearch.api.aspects.Aspect;

import java.util.ArrayList;
import java.util.List;

public abstract class FocusModSplit extends FocusMod {

    private List<FocusPackage> splitPackages = new ArrayList<>();

    public abstract int getSplitCount();

    public float getSplitAngle() {
        return 15.0f;
    }

    public List<FocusPackage> getSplitPackages() {
        return splitPackages;
    }

    public void setSplitPackages(List<FocusPackage> packages) {
        this.splitPackages = packages;
    }

    @Override
    public EnumSupplyType[] willSupply() {
        return new EnumSupplyType[] { EnumSupplyType.TRAJECTORY };
    }

    @Override
    public Aspect getAspect() {
        return Aspect.ENTROPY;
    }

    public void deserialize(CompoundTag nbt) {
        ListTag nodelist = nbt.getListOrEmpty("packages");
        splitPackages.clear();
        for (int x = 0; x < nodelist.size(); x++) {
            CompoundTag nodenbt = nodelist.getCompoundOrEmpty(x);
            FocusPackage fp = new FocusPackage();
            fp.deserialize(nodenbt);
            splitPackages.add(fp);
        }
    }

    public CompoundTag serialize() {
        CompoundTag nbt = new CompoundTag();
        ListTag nodelist = new ListTag();
        for (FocusPackage node : splitPackages) {
            nodelist.add(node.serialize());
        }
        nbt.put("packages", nodelist);
        return nbt;
    }

    public boolean execute() {

        return true;
    }

    @Override
    public float getPowerMultiplier() {
        return .75f;
    }
}
