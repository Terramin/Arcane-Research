package arcaneresearch.common.items.casters.foci;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.casters.FocusMedium;
import arcaneresearch.api.casters.FocusPackage;
import arcaneresearch.api.casters.NodeSetting;
import arcaneresearch.api.casters.Trajectory;
import arcaneresearch.common.entities.projectile.EntityFocusCloud;
public class FocusMediumCloud extends FocusMedium {
    @Override
    public String getResearch() {
        return "FOCUSCLOUD";
    }
    @Override
    public String getKey() {
        return "arcaneresearch.CLOUD";
    }
    @Override
    public Aspect getAspect() {
        return Aspect.ALCHEMY;
    }
    @Override
    public int getComplexity() {
        return 4 + getSettingValue("radius") * 2 + getSettingValue("duration") / 5;
    }
    @Override
    public EnumSupplyType[] willSupply() {
        return new EnumSupplyType[]{EnumSupplyType.TARGET};
    }
    @Override
    public EnumSupplyType[] mustBeSupplied() {
        return new EnumSupplyType[]{EnumSupplyType.TRAJECTORY};
    }
    @Override
    public boolean execute(Trajectory trajectory) {
        FocusPackage remainingPackage = getRemainingPackage();
        if (remainingPackage == null || getPackage() == null || getPackage().world == null) {
            return false;
        }
        float radius = getSettingValue("radius");
        int duration = getSettingValue("duration");
        EntityFocusCloud cloud = new EntityFocusCloud(remainingPackage, trajectory, (float) radius, duration);
        return getPackage().world.addFreshEntity(cloud);
    }
    @Override
    public boolean hasIntermediary() {
        return true;
    }
    @Override
    public NodeSetting[] createSettings() {
        return new NodeSetting[]{
                new NodeSetting("radius", "focus.common.radius",
                        new NodeSetting.NodeSettingIntRange(1, 3)),
                new NodeSetting("duration", "focus.common.duration",
                        new NodeSetting.NodeSettingIntRange(5, 30))
        };
    }
    @Override
    public float getPowerMultiplier() {
        return 0.5f;
    }
}
