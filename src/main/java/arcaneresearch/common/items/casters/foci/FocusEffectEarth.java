package arcaneresearch.common.items.casters.foci;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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
import arcaneresearch.common.lib.events.ServerEvents;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.fx.PacketFXFocusPartImpact;

import javax.annotation.Nullable;

public class FocusEffectEarth extends FocusEffect {

    @Override
    public String getResearch() {
        return "FOCUSELEMENTAL";
    }

    @Override
    public String getKey() {
        return "arcaneresearch.EARTH";
    }

    @Override
    public Aspect getAspect() {
        return Aspect.EARTH;
    }

    @Override
    public int getComplexity() {
        return getSettingValue("power") * 3;
    }

    @Override
    public float getDamageForDisplay(float finalPower) {
        return 2 * getSettingValue("power") * finalPower;
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

            if (hitEntity == null) {
                return false;
            }

            float damage = getDamageForDisplay(finalPower);

            Entity caster = getPackage().getCaster();
            DamageSource damageSource;
            if (caster != null) {
                damageSource = world.damageSources().thrown(hitEntity, caster);
            } else {
                damageSource = world.damageSources().magic();
            }

            hitEntity.hurt(damageSource, damage);
            return true;
        } else if (target.getType() == HitResult.Type.BLOCK && target instanceof BlockHitResult blockHit) {

            BlockPos pos = blockHit.getBlockPos();
            BlockState state = world.getBlockState(pos);
            if (getPackage().getCaster() instanceof Player player
                    && state.getDestroySpeed(world, pos) <= getDamageForDisplay(finalPower) / 25.0f) {
                ServerEvents.addBreaker(world, pos, state, player, false, false, 0,
                        1.0f, 0.0f, 1.0f, num, 0.1f, null);
            }
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
        FXDispatcher.GenPart pp = new FXDispatcher.GenPart();
        pp.grav = 0.4f;
        pp.layer = 1;
        pp.age = 20 + level.getRandom().nextInt(10);
        pp.alpha = new float[]{1.0f, 0.0f};
        pp.partStart = 75 + level.getRandom().nextInt(4);
        pp.partInc = 1;
        pp.partNum = 1;
        pp.slowDown = 0.9;
        pp.rot = (float) level.getRandom().nextGaussian();
        float s = (float) (1.0 + level.getRandom().nextGaussian() * 0.20000000298023224);
        pp.scale = new float[]{s, s / 2.0f};
        FXDispatcher.INSTANCE.drawGenericParticles(posX, posY, posZ, motionX, motionY, motionZ, pp);
    }

    @Override
    public void onCast(Entity caster) {
        if (caster != null && caster.level() != null) {
            caster.level().playSound(null, caster.blockPosition().above(),
                    SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.PLAYERS,
                    0.25f, 1.0f + (float) (caster.level().getRandom().nextGaussian() * 0.05));
        }
    }
}
