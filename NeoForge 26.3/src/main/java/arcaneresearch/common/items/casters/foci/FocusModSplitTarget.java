package arcaneresearch.common.items.casters.foci;

import net.minecraft.world.phys.HitResult;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.casters.FocusModSplit;

public class FocusModSplitTarget extends FocusModSplit {

    @Override
    public String getResearch() {
        return "FOCUSSPLIT";
    }

    @Override
    public String getKey() {
        return "arcaneresearch.SPLITTARGET";
    }

    @Override
    public int getComplexity() {
        return 4;
    }

    @Override
    public Aspect getAspect() {
        return Aspect.ENTROPY;
    }

    @Override
    public int getSplitCount() {

        return 2;
    }

    @Override
    public EnumSupplyType[] mustBeSupplied() {
        return new EnumSupplyType[]{EnumSupplyType.TARGET};
    }

    @Override
    public EnumSupplyType[] willSupply() {
        return new EnumSupplyType[]{EnumSupplyType.TARGET};
    }

    @Override
    public HitResult[] supplyTargets() {
        if (getParent() == null) {
            return new HitResult[0];
        }

        return getParent().supplyTargets();
    }

    @Override
    public boolean execute() {
        return true;
    }
}
