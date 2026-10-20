package arcaneresearch.common.items.casters.foci;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
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

public class FocusEffectFrost extends FocusEffect {

    @Override
    public String getResearch() {
        return "FOCUSELEMENTAL";
    }

    @Override
    public String getKey() {
        return "arcaneresearch.FROST";
    }

    @Override
    public Aspect getAspect() {
        return Aspect.COLD;
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
            PacketHandler.sendToNear(new PacketFXFocusPartImpact(hitVec.x, hitVec.y, hitVec.z, new String[]{getKey()}), world, hitVec.x, hitVec.y, hitVec.z, 64.0);
        }

        if (target.getType() == HitResult.Type.ENTITY && target instanceof EntityHitResult entityHit) {
            Entity hitEntity = entityHit.getEntity();

            if (hitEntity == null) {
                return false;
            }

            float damage = getDamageForDisplay(finalPower);
            int duration = 20 * getSettingValue("duration");
            int potency = (int) (1.0f + getSettingValue("power") * finalPower / 3.0f);

            Entity caster = getPackage().getCaster();
            DamageSource damageSource;
            if (caster != null) {
                damageSource = world.damageSources().thrown(hitEntity, caster);
            } else {
                damageSource = world.damageSources().freeze();
            }

            hitEntity.hurt(damageSource, damage);

            if (hitEntity instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, potency));
            }

            return true;
        } else if (target.getType() == HitResult.Type.BLOCK && target instanceof BlockHitResult blockHit) {

            float radius = Math.min(16.0f, 2 * getSettingValue("power") * finalPower);

            BlockPos centerPos = blockHit.getBlockPos();
            int r = Mth.ceil(radius);

            for (BlockPos mutablePos : BlockPos.betweenClosed(
                    centerPos.offset(-r, -r, -r), centerPos.offset(r, r, r))) {

                double distSq = mutablePos.distToCenterSqr(hitVec.x, hitVec.y, hitVec.z);
                if (distSq <= radius * radius) {
                    BlockState state = world.getBlockState(mutablePos);

                    if (state.getBlock() instanceof LiquidBlock &&
                            state.getFluidState().is(Fluids.WATER) &&
                            state.getFluidState().isSource() &&
                            Blocks.FROSTED_ICE.defaultBlockState().canSurvive(world, mutablePos)) {

                        world.setBlockAndUpdate(mutablePos, Blocks.FROSTED_ICE.defaultBlockState());

                        world.scheduleTick(mutablePos, Blocks.FROSTED_ICE,
                                Mth.nextInt(world.getRandom(), 60, 120));
                    }
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public NodeSetting[] createSettings() {
        return new NodeSetting[]{
                new NodeSetting("power", "focus.common.power",
                        new NodeSetting.NodeSettingIntRange(1, 5)),
                new NodeSetting("duration", "focus.common.duration",
                        new NodeSetting.NodeSettingIntRange(2, 10))
        };
    }

    @Override
    public void renderParticleFX(Level level, double posX, double posY, double posZ,
                                 double motionX, double motionY, double motionZ) {
        FXDispatcher.GenPart pp = new FXDispatcher.GenPart();
        pp.age = 40 + level.getRandom().nextInt(40);
        pp.alpha = new float[]{1.0f, 0.0f};
        pp.partStart = 8;
        pp.partNum = 1;
        pp.partInc = 1;
        pp.grav = 0.033f;
        pp.slowDown = 0.8;
        pp.randX = 0.0025;
        pp.randY = 1.0E-4;
        pp.randZ = 0.0025;
        pp.scale = new float[]{(float) (0.699999988079071 + level.getRandom().nextGaussian() * 0.30000001192092896)};
        pp.rotstart = level.getRandom().nextFloat() * 3.0f;
        pp.rot = (float) level.getRandom().nextGaussian() / 4.0f;
        FXDispatcher.INSTANCE.drawGenericParticles(posX, posY, posZ, motionX, motionY, motionZ, pp);
    }

    @Override
    public void onCast(Entity caster) {
        if (caster != null && caster.level() != null) {
            caster.level().playSound(null, caster.blockPosition().above(),
                    SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.PLAYERS,
                    0.2f, 1.0f + (float) (caster.level().getRandom().nextGaussian() * 0.05));
        }
    }
}
