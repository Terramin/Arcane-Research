package arcaneresearch.common.items.casters.foci;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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

public class FocusEffectHeal extends FocusEffect {

    @Override
    public String getResearch() {
        return "FOCUSHEAL";
    }

    @Override
    public String getKey() {
        return "arcaneresearch.HEAL";
    }

    @Override
    public Aspect getAspect() {
        return Aspect.LIFE;
    }

    @Override
    public int getComplexity() {
        return getSettingValue("power") * 4;
    }

    @Override
    public float getDamageForDisplay(float finalPower) {

        return -getSettingValue("power") * finalPower;
    }

    @Override
    public boolean execute(HitResult target, @Nullable Trajectory trajectory, float finalPower, int num) {
        if (getPackage() == null || getPackage().world == null) {
            return false;
        }

        Level world = getPackage().world;

        Vec3 hitVec = target.getLocation();
        if (!world.isClientSide()) {
            PacketHandler.sendToNear(new PacketFXFocusPartImpact(hitVec.x, hitVec.y, hitVec.z, new String[]{getKey()}), world, hitVec.x, hitVec.y, hitVec.z, 64.0);
        }

        if (target.getType() == HitResult.Type.ENTITY && target instanceof EntityHitResult entityHit) {
            Entity hitEntity = entityHit.getEntity();

            if (hitEntity == null || !(hitEntity instanceof LivingEntity living)) {
                return false;
            }

            if (living.isInvertedHealAndHarm()) {
                float damage = getSettingValue("power") * finalPower * 1.5f;

                Entity caster = getPackage().getCaster();
                DamageSource damageSource;
                if (caster != null) {
                    damageSource = world.damageSources().indirectMagic(living, caster);
                } else {
                    damageSource = world.damageSources().magic();
                }

                living.hurt(damageSource, damage);
            } else {

                float healAmount = getSettingValue("power") * finalPower;
                living.heal(healAmount);
            }

            return true;
        }

        return false;
    }

    @Override
    public NodeSetting[] createSettings() {
        return new NodeSetting[]{
                new NodeSetting("power", "focus.heal.power",
                        new NodeSetting.NodeSettingIntRange(1, 5))
        };
    }

    @Override
    public void renderParticleFX(Level level, double posX, double posY, double posZ,
                                 double motionX, double motionY, double motionZ) {
        double vx = motionX + level.getRandom().nextGaussian() * 0.01;
        double vy = motionY + level.getRandom().nextGaussian() * 0.01;
        double vz = motionZ + level.getRandom().nextGaussian() * 0.01;
        FXDispatcher.GenPart pp = new FXDispatcher.GenPart();
        pp.age = (int) (10.0f + 10.0f * level.getRandom().nextFloat());
        pp.redStart = 1.0f;
        pp.greenStart = 1.0f;
        pp.blueStart = 1.0f;
        pp.alpha = new float[]{0.0f, 0.7f, 0.7f, 0.0f};
        pp.grid = 64;
        pp.partStart = 0;
        pp.partNum = 1;
        pp.partInc = 1;
        pp.scale = new float[]{level.getRandom().nextFloat() * 2.0f, level.getRandom().nextFloat()};
        pp.slowDown = 0.8;
        pp.grav = (float) (level.getRandom().nextGaussian() * 0.10000000149011612);
        pp.rot = (float) level.getRandom().nextGaussian();
        pp.randX = 0.0125;
        pp.randY = 0.0125;
        pp.randZ = 0.0125;
        pp.delay = level.getRandom().nextInt(4);
        FXDispatcher.INSTANCE.drawGenericParticles(posX, posY, posZ, vx, vy, vz, pp);
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
