package arcaneresearch.common.items.casters.foci;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import arcaneresearch.common.lib.network.fx.PacketFXBlockBamf;
import arcaneresearch.init.ModBlocks;

import javax.annotation.Nullable;

public class FocusEffectCurse extends FocusEffect {

    @Override
    public String getResearch() {
        return "FOCUSCURSE";
    }

    @Override
    public String getKey() {
        return "arcaneresearch.CURSE";
    }

    @Override
    public Aspect getAspect() {
        return Aspect.DEATH;
    }

    @Override
    public int getComplexity() {
        return getSettingValue("duration") + getSettingValue("power") * 3;
    }

    @Override
    public float getDamageForDisplay(float finalPower) {
        return (1.0f + getSettingValue("power")) * finalPower;
    }

    @Override
    public boolean execute(HitResult target, @Nullable Trajectory trajectory, float finalPower, int num) {
        if (getPackage() == null || getPackage().world == null) {
            return false;
        }

        Level world = getPackage().world;

        if (!world.isClientSide()) {
            Vec3 loc = target.getLocation();
            PacketHandler.sendToNear(new PacketFXBlockBamf(loc.x(), loc.y(), loc.z(), 6946821, true, true, null), world, loc.x(), loc.y(), loc.z(), 64.0);
        }

        if (target.getType() == HitResult.Type.ENTITY && target instanceof EntityHitResult entityHit) {
            Entity hitEntity = entityHit.getEntity();

            if (hitEntity == null) {
                return false;
            }

            float damage = getDamageForDisplay(finalPower);
            int duration = 20 * getSettingValue("duration");
            int effectLevel = Math.max(0, (int) (getSettingValue("power") * finalPower / 2.0f));

            Entity caster = getPackage().getCaster();
            DamageSource damageSource;
            if (caster != null) {
                damageSource = world.damageSources().indirectMagic(hitEntity, caster);
            } else {
                damageSource = world.damageSources().magic();
            }

            hitEntity.hurt(damageSource, damage);

            if (hitEntity instanceof LivingEntity living) {

                living.addEffect(new MobEffectInstance(MobEffects.POISON, duration, effectLevel));

                float chance = 0.85f;

                if (world.getRandom().nextFloat() < chance) {
                    living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, effectLevel));
                    chance -= 0.15f;
                }

                if (world.getRandom().nextFloat() < chance) {
                    living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, effectLevel));
                    chance -= 0.15f;
                }

                if (world.getRandom().nextFloat() < chance) {
                    living.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, duration * 2, effectLevel));
                    chance -= 0.15f;
                }

                if (world.getRandom().nextFloat() < chance) {
                    living.addEffect(new MobEffectInstance(MobEffects.HUNGER, duration * 3, effectLevel));
                    chance -= 0.15f;
                }

                if (world.getRandom().nextFloat() < chance) {
                    living.addEffect(new MobEffectInstance(MobEffects.UNLUCK, duration * 3, effectLevel));
                }
            }

            return true;
        } else {

            float f = (float) Math.min(8.0, 1.5 * getSettingValue("power") * finalPower);
            BlockPos hitPos = BlockPos.containing(target.getLocation());
            Vec3 hv = target.getLocation();
            int r = (int) Math.ceil(f);
            for (BlockPos p : BlockPos.betweenClosed(hitPos.offset(-r, -r, -r), hitPos.offset(r, r, r))) {
                if (p.distToCenterSqr(hv.x(), hv.y(), hv.z()) <= f * f
                        && world.isEmptyBlock(p.above())
                        && world.getBlockState(p).isSolidRender()) {
                    world.setBlock(p.above(), ModBlocks.EFFECT_SAP.get().defaultBlockState(), 3);
                }
            }
            return true;
        }
    }

    @Override
    public NodeSetting[] createSettings() {
        return new NodeSetting[]{
                new NodeSetting("power", "focus.common.power",
                        new NodeSetting.NodeSettingIntRange(1, 5)),
                new NodeSetting("duration", "focus.common.duration",
                        new NodeSetting.NodeSettingIntRange(1, 10))
        };
    }

    @Override
    public void renderParticleFX(Level level, double posX, double posY, double posZ,
                                 double motionX, double motionY, double motionZ) {

        FXDispatcher.GenPart part = new FXDispatcher.GenPart();
        part.age = 8;
        part.redStart = 0.41f + level.getRandom().nextFloat() * 0.2f;
        part.greenStart = 0.0f;
        part.blueStart = 0.019f + level.getRandom().nextFloat() * 0.2f;
        part.alpha = new float[]{0.0f, level.getRandom().nextFloat(), level.getRandom().nextFloat(),
                level.getRandom().nextFloat(), 0.0f};
        part.grid = 16;
        part.partStart = 72 + level.getRandom().nextInt(4);
        part.partNum = 1;
        part.partInc = 1;
        part.scale = new float[]{2.0f + level.getRandom().nextFloat() * 4.0f};
        part.loop = false;
        part.slowDown = 0.9;
        part.grav = 0.0f;
        part.rotstart = level.getRandom().nextFloat();
        part.rot = 0.0f;
        part.layer = 0;
        part.delay = level.getRandom().nextInt(4);
        FXDispatcher.INSTANCE.drawGenericParticles(posX, posY, posZ, motionX, motionY, motionZ, part);
    }

    @Override
    public void onCast(Entity caster) {
        if (caster != null && caster.level() != null) {
            caster.level().playSound(null, caster.blockPosition().above(),
                    SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.PLAYERS,
                    0.15f, 1.0f + caster.level().getRandom().nextFloat() / 2.0f);
        }
    }
}
