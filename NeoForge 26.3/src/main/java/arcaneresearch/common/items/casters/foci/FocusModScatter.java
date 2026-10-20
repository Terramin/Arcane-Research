package arcaneresearch.common.items.casters.foci;

import net.minecraft.world.phys.Vec3;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.casters.FocusMod;
import arcaneresearch.api.casters.NodeSetting;
import arcaneresearch.api.casters.Trajectory;

import java.util.ArrayList;
import java.util.List;

public class FocusModScatter extends FocusMod {

    @Override
    public String getResearch() {
        return "FOCUSSCATTER";
    }

    @Override
    public String getKey() {
        return "arcaneresearch.SCATTER";
    }

    @Override
    public int getComplexity() {

        return (int) Math.max(2.0f, 2.0f * (getSettingValue("forks") - getSettingValue("cone") / 45.0f));
    }

    @Override
    public Aspect getAspect() {
        return Aspect.ENTROPY;
    }

    @Override
    public NodeSetting[] createSettings() {
        int[] angles = {10, 30, 60, 90, 180, 270, 360};
        String[] anglesDesc = {"10", "30", "60", "90", "180", "270", "360"};

        return new NodeSetting[]{
                new NodeSetting("forks", "focus.scatter.forks",
                        new NodeSetting.NodeSettingIntRange(2, 10)),
                new NodeSetting("cone", "focus.scatter.cone",
                        new NodeSetting.NodeSettingIntList(angles, anglesDesc))
        };
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
        if (getParent() == null || getPackage() == null || getPackage().world == null) {
            return new Trajectory[0];
        }

        Trajectory[] parentTrajectories = getParent().supplyTrajectories();
        if (parentTrajectories == null || parentTrajectories.length == 0) {
            return new Trajectory[0];
        }

        List<Trajectory> result = new ArrayList<>();
        int forks = getSettingValue("forks");
        int angle = getSettingValue("cone");

        double spread = angle * 0.0075;

        for (Trajectory sourceTrajectory : parentTrajectories) {
            Vec3 source = sourceTrajectory.source;
            Vec3 direction = sourceTrajectory.direction.normalize();

            for (int i = 0; i < forks; i++) {

                double dx = getPackage().world.getRandom().nextGaussian() * spread;
                double dy = getPackage().world.getRandom().nextGaussian() * spread;
                double dz = getPackage().world.getRandom().nextGaussian() * spread;

                Vec3 newDirection = direction.add(dx, dy, dz).normalize();
                result.add(new Trajectory(source, newDirection));
            }
        }

        return result.toArray(new Trajectory[0]);
    }

    @Override
    public float getPowerMultiplier() {

        return 1.0f / (getSettingValue("forks") / 2.0f);
    }

    @Override
    public boolean execute() {
        return true;
    }

    @Override
    public boolean isExclusive() {

        return true;
    }
}
