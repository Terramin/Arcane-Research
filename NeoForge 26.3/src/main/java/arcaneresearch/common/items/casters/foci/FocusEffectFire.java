package arcaneresearch.common.items.casters.foci;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
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

public class FocusEffectFire extends FocusEffect {

    @Override
    public String getResearch() {
        return "BASEAUROMANCY";
    }

    @Override
    public String getKey() {
        return "arcaneresearch.FIRE";
    }

    @Override
    public Aspect getAspect() {
        return Aspect.FIRE;
    }

    @Override
    public int getComplexity() {
        return getSettingValue("duration") + getSettingValue("power") * 2;
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

        Vec3 hitVec = target.getLocation();
        if (!world.isClientSide()) {
            PacketHandler.sendToNear(new PacketFXFocusPartImpact(hitVec.x, hitVec.y, hitVec.z, new String[]{getKey()}),
                    world, hitVec.x, hitVec.y, hitVec.z, 64.0);
        }

        if (target.getType() == HitResult.Type.ENTITY && target instanceof EntityHitResult entityHit) {
            Entity hitEntity = entityHit.getEntity();

            if (hitEntity == null || hitEntity.fireImmune()) {
                return false;
            }

            float fireDuration = (float) (1 + getSettingValue("duration") * getSettingValue("duration"));
            float damage = getDamageForDisplay(finalPower);
            fireDuration *= finalPower;

            Entity caster = getPackage().getCaster();
            DamageSource damageSource;
            if (caster != null) {
                var damageTypes = world.registryAccess().lookupOrThrow(
                        net.minecraft.core.registries.Registries.DAMAGE_TYPE);
                damageSource = new DamageSource(
                        damageTypes.getOrThrow(
                                net.minecraft.world.damagesource.DamageTypes.ON_FIRE),
                        hitEntity, caster);
            } else {
                damageSource = world.damageSources().onFire();
            }

            hitEntity.hurt(damageSource, damage);

            if (fireDuration > 0.0f) {
                hitEntity.igniteForSeconds(Math.round(fireDuration));
            }

            return true;
        } else if (target.getType() == HitResult.Type.BLOCK && target instanceof BlockHitResult blockHit) {

            if (getSettingValue("duration") > 0) {
                BlockPos pos = blockHit.getBlockPos().relative(blockHit.getDirection());

                if (world.isEmptyBlock(pos) && world.getRandom().nextFloat() < finalPower) {
                    world.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS,
                            1.0f, world.getRandom().nextFloat() * 0.4f + 0.8f);
                    world.setBlock(pos, Blocks.FIRE.defaultBlockState(), 11);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public NodeSetting[] createSettings() {
        return new NodeSetting[]{
                new NodeSetting("power", "focus.common.power",
                        new NodeSetting.NodeSettingIntRange(1, 5)),
                new NodeSetting("duration", "focus.fire.burn",
                        new NodeSetting.NodeSettingIntRange(0, 5))
        };
    }

    @Override
    public void renderParticleFX(Level level, double posX, double posY, double posZ,
                                 double motionX, double motionY, double motionZ) {
        FXDispatcher.GenPart pp = new FXDispatcher.GenPart();
        pp.grav = -0.2f;
        pp.age = 10;
        pp.alpha = new float[]{0.7f};
        pp.partStart = 640;
        pp.partInc = 1;
        pp.partNum = 10;
        pp.slowDown = 0.75;
        pp.scale = new float[]{(float) (1.5 + level.getRandom().nextGaussian() * 0.20000000298023224)};
        FXDispatcher.INSTANCE.drawGenericParticles(posX, posY, posZ, motionX, motionY, motionZ, pp);
    }

    @Override
    public void onCast(Entity caster) {
        if (caster != null && caster.level() != null) {
            caster.level().playSound(null, caster.blockPosition().above(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS,
                    1.0f, 1.0f + (float) (caster.level().getRandom().nextGaussian() * 0.05));
        }
    }
}
