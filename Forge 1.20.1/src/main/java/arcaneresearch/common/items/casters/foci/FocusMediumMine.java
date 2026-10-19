package arcaneresearch.common.items.casters.foci;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.casters.FocusMedium;
import arcaneresearch.api.casters.FocusPackage;
import arcaneresearch.api.casters.NodeSetting;
import arcaneresearch.api.casters.Trajectory;
import arcaneresearch.common.entities.projectile.EntityFocusMine;
public class FocusMediumMine extends FocusMedium {
    public static final int TARGET_ENEMY = 0;
    public static final int TARGET_FRIEND = 1;
    @Override
    public String getResearch() {
        return "FOCUSMINE";
    }
    @Override
    public String getKey() {
        return "arcaneresearch.MINE";
    }
    @Override
    public int getComplexity() {
        return 4;
    }
    @Override
    public Aspect getAspect() {
        return Aspect.TRAP;
    }
    @Override
    public EnumSupplyType[] willSupply() {
        return new EnumSupplyType[]{EnumSupplyType.TARGET, EnumSupplyType.TRAJECTORY};
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
        boolean targetFriendly = getSettingValue("target") == TARGET_FRIEND;
        EntityFocusMine mine = new EntityFocusMine(remainingPackage, trajectory, targetFriendly);
        return getPackage().world.addFreshEntity(mine);
    }
    @Override
    public boolean hasIntermediary() {
        return true;
    }
    @Override
    public NodeSetting[] createSettings() {
        int[] target = {TARGET_ENEMY, TARGET_FRIEND};
        String[] targetDesc = {"focus.common.enemy", "focus.common.friend"};
        return new NodeSetting[]{
                new NodeSetting("target", "focus.common.target",
                        new NodeSetting.NodeSettingIntList(target, targetDesc))
        };
    }
}
