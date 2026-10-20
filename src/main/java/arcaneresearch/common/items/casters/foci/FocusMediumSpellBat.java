package arcaneresearch.common.items.casters.foci;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.casters.FocusMedium;
import arcaneresearch.api.casters.FocusPackage;
import arcaneresearch.api.casters.NodeSetting;
import arcaneresearch.api.casters.Trajectory;
import arcaneresearch.common.entities.monster.EntitySpellBat;
public class FocusMediumSpellBat extends FocusMedium {
    @Override
    public String getResearch() {
        return "FOCUSSPELLBAT";
    }
    @Override
    public String getKey() {
        return "arcaneresearch.SPELLBAT";
    }
    @Override
    public Aspect getAspect() {
        return Aspect.BEAST;
    }
    @Override
    public int getComplexity() {
        return 8;
    }
    @Override
    public EnumSupplyType[] willSupply() {
        return new EnumSupplyType[]{EnumSupplyType.TARGET};
    }
    @Override
    public boolean execute(Trajectory trajectory) {
        if (getPackage() == null || getPackage().getCaster() == null) {
            return false;
        }
        FocusPackage remainingPackage = getRemainingPackage();
        boolean friendly = getSettingValue("target") == 1;
        EntitySpellBat bat = new EntitySpellBat(
                getPackage().getCaster().level(),
                remainingPackage,
                friendly
        );
        bat.setPos(trajectory.source.x, trajectory.source.y, trajectory.source.z);
        return getPackage().getCaster().level().addFreshEntity(bat);
    }
    @Override
    public boolean hasIntermediary() {
        return true;
    }
    @Override
    public float getPowerMultiplier() {
        return 0.33f;
    }
    @Override
    public NodeSetting[] createSettings() {
        int[] targetModes = {0, 1};
        String[] targetDescs = {"focus.common.enemy", "focus.common.friend"};
        return new NodeSetting[]{
                new NodeSetting("target", "focus.common.target",
                        new NodeSetting.NodeSettingIntList(targetModes, targetDescs))
        };
    }
}
