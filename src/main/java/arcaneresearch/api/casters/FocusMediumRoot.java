package arcaneresearch.api.casters;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.api.aspects.Aspect;

public class FocusMediumRoot extends FocusMedium {

    Trajectory[] trajectories = null;
    HitResult[] targets = null;

    public FocusMediumRoot() {
        super();
    }

    public FocusMediumRoot(Trajectory[] trajectories, HitResult[] targets) {
        super();
        this.trajectories = trajectories;
        this.targets = targets;
    }

    @Override
    public String getResearch() {
        return "BASEAUROMANCY";
    }

    @Override
    public String getKey() {
        return "ROOT";
    }

    @Override
    public int getComplexity() {
        return 0;
    }

    @Override
    public Aspect getAspect() {
        return null;
    }

    @Override
    public EnumSupplyType[] mustBeSupplied() {
        return null;
    }

    @Override
    public EnumSupplyType[] willSupply() {
        return new EnumSupplyType[] { EnumSupplyType.TARGET, EnumSupplyType.TRAJECTORY };
    }

    @Override
    public HitResult[] supplyTargets() {
        return targets;
    }

    @Override
    public Trajectory[] supplyTrajectories() {
        return trajectories;
    }

    public void setupFromCaster(LivingEntity caster) {
        trajectories = new Trajectory[]{new Trajectory(generateSourceVector(caster), caster.getLookAngle())};
        targets = new HitResult[]{new net.minecraft.world.phys.EntityHitResult(caster)};
    }

    public void setupFromCasterToTarget(LivingEntity caster, Entity target, double offset) {
        Vec3 sv = generateSourceVector(caster);
        Vec3 tv = new Vec3(target.getX() - sv.x,
                target.getBoundingBox().minY + (target.getBbHeight() / 2.0F) - sv.y + offset,
                target.getZ() - sv.z);
        trajectories = new Trajectory[]{new Trajectory(sv, tv.normalize())};
        targets = new HitResult[]{new net.minecraft.world.phys.EntityHitResult(caster)};
    }

    public void setupFromCasterToTargetLoc(LivingEntity caster, double x, double y, double z) {
        Vec3 sv = generateSourceVector(caster);
        Vec3 lv = new Vec3(x - sv.x, y - sv.y, z - sv.z);
        trajectories = new Trajectory[]{new Trajectory(sv, lv.normalize())};
        targets = new HitResult[]{new net.minecraft.world.phys.EntityHitResult(caster)};
    }

    private Vec3 generateSourceVector(LivingEntity e) {
        Vec3 v = e.position();
        v = v.add(0, e.getEyeHeight() - 0.10000000149011612D, 0);
        return v;
    }
}
