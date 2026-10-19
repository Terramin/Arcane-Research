package arcaneresearch.common.items.casters.foci;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.casters.FocusMedium;
import arcaneresearch.api.casters.IFocusElement;
import arcaneresearch.api.casters.Trajectory;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.fx.PacketFXFocusEffect;

import java.util.ArrayList;
import java.util.List;

public class FocusMediumTouch extends FocusMedium {

    @Override
    public String getResearch() {
        return "BASEAUROMANCY";
    }

    @Override
    public String getKey() {
        return "arcaneresearch.TOUCH";
    }

    @Override
    public int getComplexity() {
        return 2;
    }

    @Override
    public EnumSupplyType[] willSupply() {
        return new EnumSupplyType[]{EnumSupplyType.TRAJECTORY, EnumSupplyType.TARGET};
    }

    @Override
    public Aspect getAspect() {
        return Aspect.AVERSION;
    }

    @Override
    public Trajectory[] supplyTrajectories() {
        if (getPackage() == null || getPackage().world == null) {
            return new Trajectory[0];
        }

        Entity caster = getCaster();
        if (caster == null) {
            return new Trajectory[0];
        }

        double range = getRange(caster);
        List<Trajectory> out = new ArrayList<>();

        for (Trajectory sT : getSourceTrajectories(caster)) {
            Vec3 dir = sT.direction.normalize();
            Vec3 far = sT.source.add(dir.scale(range));
            HitResult ray = arcaneresearch.common.lib.utils.EntityUtils.getPointedEntityRay(
                    getPackage().world, caster, sT.source, dir, 0.25, range, 0.25f, false);
            Vec3 end;
            if (ray == null) {
                BlockHitResult bhit = getPackage().world.clip(new ClipContext(
                        sT.source, far,
                        ClipContext.Block.OUTLINE,
                        ClipContext.Fluid.NONE,
                        caster));
                end = (bhit.getType() != HitResult.Type.MISS) ? bhit.getLocation() : far;
            } else if (ray instanceof EntityHitResult ehr && ehr.getEntity() != null) {
                end = sT.source.add(dir.scale(sT.source.distanceTo(ehr.getEntity().position())));
            } else {
                end = ray.getLocation();
            }
            out.add(new Trajectory(end, dir));
        }
        return out.toArray(new Trajectory[0]);
    }

    @Override
    public HitResult[] supplyTargets() {
        if (getPackage() == null || getPackage().world == null) {
            return new HitResult[0];
        }

        Entity caster = getCaster();
        if (caster == null) {
            return new HitResult[0];
        }

        double range = getRange(caster);
        List<HitResult> out = new ArrayList<>();
        for (Trajectory sT : getSourceTrajectories(caster)) {
            Vec3 dir = sT.direction.normalize();
            Vec3 far = sT.source.add(dir.scale(range));
            HitResult ray = arcaneresearch.common.lib.utils.EntityUtils.getPointedEntityRay(
                    getPackage().world, caster, sT.source, dir, 0.25, range, 0.25f, false);
            if (ray == null) {
                ray = getPackage().world.clip(new ClipContext(
                        sT.source, far,
                        ClipContext.Block.OUTLINE,
                        ClipContext.Fluid.NONE,
                        caster));
            }
            if (ray != null && ray.getType() != HitResult.Type.MISS) {
                out.add(ray);
            }
        }
        return out.toArray(new HitResult[0]);
    }

    protected Trajectory[] getSourceTrajectories(Entity caster) {
        if (getParent() != null) {
            Trajectory[] parents = getParent().supplyTrajectories();
            if (parents != null && parents.length > 0) {
                return parents;
            }
        }
        return new Trajectory[]{new Trajectory(caster.getEyePosition(), caster.getLookAngle())};
    }

    protected double getRange(Entity caster) {
        if (caster instanceof Player player) {

            return player.getAbilities().instabuild ? 5.0 : 4.5;
        }
        return 4.5;
    }

    protected Entity getCaster() {
        if (getPackage() == null || getPackage().getCasterUUID() == null) {
            return null;
        }

        if (getPackage().world != null) {
            for (Player player : getPackage().world.players()) {
                if (player.getUUID().equals(getPackage().getCasterUUID())) {
                    return player;
                }
            }
        }
        return null;
    }

    @Override
    public boolean execute(Trajectory trajectory) {
        if (getPackage() == null || getPackage().world == null) {
            return false;
        }
        List<IFocusElement> fe = getPackage().getEffects();
        if (fe != null && !fe.isEmpty()) {
            String[] effects = new String[fe.size()];
            for (int a = 0; a < fe.size(); ++a) {
                effects[a] = fe.get(a).getKey();
            }
            if (!getPackage().world.isClientSide()) {
                PacketHandler.sendToNear(new PacketFXFocusEffect((float) trajectory.source.x, (float) trajectory.source.y, (float) trajectory.source.z,
                                (float) trajectory.direction.x / 2.0f, (float) trajectory.direction.y / 2.0f, (float) trajectory.direction.z / 2.0f, effects), getPackage().world, trajectory.source.x, trajectory.source.y, trajectory.source.z, 64.0);
            }
        }
        return true;
    }
}
