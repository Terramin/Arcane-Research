package arcaneresearch.common.items.casters.foci;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.casters.FocusEffect;
import arcaneresearch.api.casters.NodeSetting;
import arcaneresearch.api.casters.Trajectory;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.fx.PacketFXFocusPartImpact;

import javax.annotation.Nullable;

public class FocusEffectFlux extends FocusEffect {

    @Override
    public String getResearch() {
        return "FOCUSFLUX";
    }

    @Override
    public String getKey() {
        return "arcaneresearch.FLUX";
    }

    @Override
    public Aspect getAspect() {
        return Aspect.FLUX;
    }

    @Override
    public int getComplexity() {
        return getSettingValue("power") * 3;
    }

    @Override
    public float getDamageForDisplay(float finalPower) {
        return (3 + getSettingValue("power")) * finalPower;
    }

    @Override
    public boolean execute(HitResult target, @Nullable Trajectory trajectory, float finalPower, int num) {
        if (getPackage() == null || getPackage().world == null) {
            return false;
        }

        Level world = getPackage().world;

        if (!world.isClientSide()) {
            Vec3 loc = target.getLocation();
            PacketHandler.sendToNear(new PacketFXFocusPartImpact(loc.x(), loc.y(), loc.z(), new String[]{getKey()}), world, loc.x(), loc.y(), loc.z(), 64.0);
        }

        if (target.getType() == HitResult.Type.ENTITY && target instanceof EntityHitResult entityHit) {
            Entity hitEntity = entityHit.getEntity();

            if (hitEntity == null) {
                return false;
            }

            float damage = getDamageForDisplay(finalPower);

            Entity caster = getPackage().getCaster();
            DamageSource damageSource;
            if (caster != null) {
                damageSource = world.damageSources().indirectMagic(hitEntity, caster);
            } else {
                damageSource = world.damageSources().magic();
            }

            hitEntity.hurt(damageSource, damage);

            return true;
        }

        return false;
    }

    @Override
    public NodeSetting[] createSettings() {
        return new NodeSetting[]{
                new NodeSetting("power", "focus.common.power",
                        new NodeSetting.NodeSettingIntRange(1, 5))
        };
    }

    @Override
    public void renderParticleFX(Level level, double posX, double posY, double posZ,
                                 double motionX, double motionY, double motionZ) {

        FXDispatcher.GenPart part = new FXDispatcher.GenPart();
        part.age = (int) (15.0f + 10.0f * level.getRandom().nextFloat());
        part.redStart = 0.25f + level.getRandom().nextFloat() * 0.25f;
        part.greenStart = 0.0f;
        part.blueStart = 0.25f + level.getRandom().nextFloat() * 0.25f;
        part.alpha = new float[]{0.0f, 1.0f, 1.0f, 0.0f};
        part.grid = 64;
        part.partStart = 128;
        part.partNum = 14;
        part.partInc = 1;
        part.scale = new float[]{2.0f + level.getRandom().nextFloat(), 0.25f + level.getRandom().nextFloat() * 0.25f};
        part.loop = true;
        part.slowDown = 0.9;
        part.grav = (float) (level.getRandom().nextGaussian() * 0.1);
        part.rot = (float) level.getRandom().nextGaussian();
        part.layer = 0;
        part.delay = level.getRandom().nextInt(4);
        part.randX = 0.0125;
        part.randY = 0.0125;
        part.randZ = 0.0125;
        double jx = motionX + level.getRandom().nextGaussian() * 0.01;
        double jy = motionY + level.getRandom().nextGaussian() * 0.01;
        double jz = motionZ + level.getRandom().nextGaussian() * 0.01;
        FXDispatcher.INSTANCE.drawGenericParticles(posX, posY, posZ, jx, jy, jz, part);
    }

    @Override
    public void onCast(Entity caster) {
        if (caster != null && caster.level() != null) {
            caster.level().playSound(null, caster.blockPosition().above(),
                    SoundEvents.CHORUS_FLOWER_GROW, SoundSource.PLAYERS,
                    2.0f, 2.0f + (float) (caster.level().getRandom().nextGaussian() * 0.1));
        }
    }
}
