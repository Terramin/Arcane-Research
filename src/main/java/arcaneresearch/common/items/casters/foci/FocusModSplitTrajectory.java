package arcaneresearch.common.items.casters.foci;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.casters.FocusModSplit;
import arcaneresearch.api.casters.Trajectory;
public class FocusModSplitTrajectory extends FocusModSplit {
    @Override
    public String getResearch() {
        return "FOCUSSPLIT";
    }
    @Override
    public String getKey() {
        return "arcaneresearch.SPLITTRAJECTORY";
    }
    @Override
    public int getComplexity() {
        return 5;
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
        return new EnumSupplyType[]{EnumSupplyType.TRAJECTORY};
    }
    @Override
    public EnumSupplyType[] willSupply() {
        return new EnumSupplyType[]{EnumSupplyType.TRAJECTORY};
    }
    @Override
    public Trajectory[] supplyTrajectories() {
        if (getParent() == null) {
            return new Trajectory[0];
        }
        return getParent().supplyTrajectories();
    }
    @Override
    public boolean execute() {
        return true;
    }
}
