package arcaneresearch.common.items.casters.foci;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.casters.FocusMedium;
import arcaneresearch.api.casters.FocusPackage;
import arcaneresearch.api.casters.NodeSetting;
import arcaneresearch.api.casters.Trajectory;
import arcaneresearch.common.entities.projectile.EntityFocusProjectile;

public class FocusMediumProjectile extends FocusMedium {

    public static final int OPTION_NONE = 0;

    public static final int OPTION_BOUNCY = 1;

    public static final int OPTION_SEEKING_HOSTILE = 2;

    public static final int OPTION_SEEKING_FRIENDLY = 3;

    @Override
    public String getResearch() {
        return "FOCUSPROJECTILE@2";
    }

    @Override
    public String getKey() {
        return "arcaneresearch.PROJECTILE";
    }

    @Override
    public int getComplexity() {
        int c = 4 + (getSettingValue("speed") - 1) / 2;
        switch (getSettingValue("option")) {
            case OPTION_BOUNCY:
                c += 3;
                break;
            case OPTION_SEEKING_HOSTILE:
            case OPTION_SEEKING_FRIENDLY:
                c += 5;
                break;
        }
        return c;
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
        float speed = getSettingValue("speed") / 3.0f;
        FocusPackage p = getRemainingPackage();

        if (p == null || getPackage() == null || getPackage().getCasterUUID() == null) {
            return false;
        }

        if (getPackage().world != null) {
            EntityFocusProjectile projectile = new EntityFocusProjectile(p, speed, trajectory, getSettingValue("option"));
            return getPackage().world.addFreshEntity(projectile);
        }

        return false;
    }

    @Override
    public boolean hasIntermediary() {

        return true;
    }

    @Override
    public NodeSetting[] createSettings() {
        int[] option = {OPTION_NONE, OPTION_BOUNCY, OPTION_SEEKING_HOSTILE, OPTION_SEEKING_FRIENDLY};
        String[] optionDesc = {
                "focus.common.none",
                "focus.projectile.bouncy",
                "focus.projectile.seeking.hostile",
                "focus.projectile.seeking.friendly"
        };

        return new NodeSetting[]{
                new NodeSetting("option", "focus.common.options",
                        new NodeSetting.NodeSettingIntList(option, optionDesc), "FOCUSPROJECTILE"),
                new NodeSetting("speed", "focus.projectile.speed",
                        new NodeSetting.NodeSettingIntRange(1, 5))
        };
    }

    @Override
    public Aspect getAspect() {
        return Aspect.MOTION;
    }
}