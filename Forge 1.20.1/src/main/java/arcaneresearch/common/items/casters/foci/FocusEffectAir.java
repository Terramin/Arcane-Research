package arcaneresearch.common.items.casters.foci;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.casters.FocusEffect;
import arcaneresearch.api.casters.NodeSetting;
import arcaneresearch.api.casters.Trajectory;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.fx.PacketFXFocusPartImpact;
import arcaneresearch.init.ModSounds;
import javax.annotation.Nullable;
public class FocusEffectAir extends FocusEffect {
    @Override
    public String getResearch() {
        return "FOCUSELEMENTAL";
    }
    @Override
    public String getKey() {
        return "arcaneresearch.AIR";
    }
    @Override
    public Aspect getAspect() {
        return Aspect.AIR;
    }
    @Override
    public int getComplexity() {
        return getSettingValue("power") * 2;
    }
    @Override
    public float getDamageForDisplay(float finalPower) {
        return (1 + getSettingValue("power")) * finalPower;
    }
    @Override
    public boolean execute(HitResult target, @Nullable Trajectory trajectory, float finalPower, int num) {
        if (getPackage() == null || getPackage().world == null) {
            return false;
        }
        Level world = getPackage().world;
        Vec3 hitPos = target.getLocation();
        if (!world.isClientSide()) {
            PacketHandler.sendToNear(new PacketFXFocusPartImpact(hitPos.x, hitPos.y, hitPos.z, new String[]{getKey()}),
                    new PacketDistributor.TargetPoint(hitPos.x, hitPos.y, hitPos.z, 64.0, world.dimension()));
        }
        world.playSound(null, hitPos.x, hitPos.y, hitPos.z,
                SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.5f, 0.66f);
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
            if (hitEntity instanceof LivingEntity living) {
                float knockbackStrength = damage * 0.25f;
                if (trajectory != null) {
                    living.knockback(knockbackStrength,
                            -trajectory.direction.x,
                            -trajectory.direction.z);
                } else {
                    float yawRad = hitEntity.getYRot() * ((float) Math.PI / 180F);
                    living.knockback(knockbackStrength,
                            -Mth.sin(yawRad),
                            Mth.cos(yawRad));
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
                        new NodeSetting.NodeSettingIntRange(1, 5))
        };
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderParticleFX(Level level, double posX, double posY, double posZ,
                                 double motionX, double motionY, double motionZ) {
        FXDispatcher.GenPart pp = new FXDispatcher.GenPart();
        pp.grav = -0.1f;
        pp.age = 20 + level.random.nextInt(10);
        pp.alpha = new float[]{0.5f, 0.0f};
        pp.grid = 32;
        pp.partStart = 337;
        pp.partInc = 1;
        pp.partNum = 5;
        pp.slowDown = 0.75;
        pp.rot = (float) level.random.nextGaussian() / 2.0f;
        float s = (float) (2.0 + level.random.nextGaussian() * 0.5);
        pp.scale = new float[]{s, s * 2.0f};
        FXDispatcher.INSTANCE.drawGenericParticles(posX, posY, posZ, motionX, motionY, motionZ, pp);
    }
    @Override
    public void onCast(Entity caster) {
        if (caster != null && caster.level() != null) {
            caster.level().playSound(null, caster.blockPosition().above(),
                    ModSounds.WIND.get(), SoundSource.PLAYERS,
                    0.125f, 2.0f);
        }
    }
}
