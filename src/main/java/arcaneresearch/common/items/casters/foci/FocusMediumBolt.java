package arcaneresearch.common.items.casters.foci;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.casters.FocusEngine;
import arcaneresearch.api.casters.IFocusElement;
import arcaneresearch.api.casters.Trajectory;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.fx.PacketFXZap;
import java.awt.*;
import java.util.List;
public class FocusMediumBolt extends FocusMediumTouch {
    private static final float BOLT_RANGE = 16.0f;
    @Override
    public String getResearch() {
        return "FOCUSBOLT";
    }
    @Override
    public String getKey() {
        return "arcaneresearch.BOLT";
    }
    @Override
    public int getComplexity() {
        return 5;
    }
    @Override
    public Aspect getAspect() {
        return Aspect.ENERGY;
    }
    @Override
    public boolean execute(Trajectory trajectory) {
        if (getPackage() == null || getPackage().world == null) {
            return false;
        }
        Vec3 start = trajectory.source;
        Vec3 direction = trajectory.direction.normalize();
        Vec3 end = start.add(direction.scale(BOLT_RANGE));
        Entity caster = getCaster();
        HitResult hit = arcaneresearch.common.lib.utils.EntityUtils.getPointedEntityRay(
                getPackage().world, caster, start, direction, 0.25, BOLT_RANGE, 0.25f, false);
        if (hit == null || hit.getType() == HitResult.Type.MISS) {
            hit = getPackage().world.clip(new ClipContext(
                    start, end,
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    null));
            if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                end = hit.getLocation();
            }
        } else if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() != null) {
            end = start.add(direction.scale(start.distanceTo(entityHit.getEntity().position())));
        }
        int color = calculateEffectColor();
        if (!getPackage().world.isClientSide()) {
            PacketHandler.sendToNear(new PacketFXZap(start, end, color, getPackage().getPower() * 0.66f),
                    new PacketDistributor.TargetPoint(start.x, start.y, start.z, 64.0, getPackage().world.dimension()));
        }
        return true;
    }
    @Override
    protected double getRange(Entity caster) {
        return BOLT_RANGE;
    }
    private int calculateEffectColor() {
        List<IFocusElement> effects = getPackage().getEffects();
        if (effects == null || effects.isEmpty()) {
            return 0x5599FF;
        }
        int r = 0;
        int g = 0;
        int b = 0;
        for (IFocusElement ef : effects) {
            Color c = new Color(FocusEngine.getElementColor(ef.getKey()));
            r += c.getRed();
            g += c.getGreen();
            b += c.getBlue();
        }
        r /= effects.size();
        g /= effects.size();
        b /= effects.size();
        return new Color(r, g, b).getRGB();
    }
}
