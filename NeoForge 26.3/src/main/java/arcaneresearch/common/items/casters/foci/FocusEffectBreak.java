package arcaneresearch.common.items.casters.foci;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
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

public class FocusEffectBreak extends FocusEffect {

    @Override
    public String getResearch() {
        return "FOCUSBREAK";
    }

    @Override
    public String getKey() {
        return "arcaneresearch.BREAK";
    }

    @Override
    public Aspect getAspect() {
        return Aspect.ENTROPY;
    }

    @Override
    public int getComplexity() {
        int fortuneValue = getSettingValue("fortune");
        int fortuneComplexity = (fortuneValue == 0) ? 0 : ((fortuneValue + 1) * 3);
        return getSettingValue("power") * 3 + getSettingValue("silk") * 4 + fortuneComplexity;
    }

    @Override
    public boolean execute(HitResult target, @Nullable Trajectory trajectory, float finalPower, int num) {
        if (getPackage() == null || getPackage().world == null) {
            return false;
        }

        if (target.getType() != HitResult.Type.BLOCK || !(target instanceof BlockHitResult blockHit)) {
            return true;
        }

        Level world = getPackage().world;
        BlockPos pos = blockHit.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (!world.isClientSide()) {
            Vec3 loc = target.getLocation();
            PacketHandler.sendToNear(new PacketFXFocusPartImpact(loc.x(), loc.y(), loc.z(), new String[]{getKey()}), world, loc.x(), loc.y(), loc.z(), 64.0);
        }

        Entity caster = getPackage().getCaster();
        if (!(caster instanceof Player)) {
            return true;
        }

        boolean silk = getSettingValue("silk") > 0;
        int fortune = getSettingValue("fortune");
        float strength = getSettingValue("power") * finalPower;

        float dur = (float) Math.sqrt(state.getDestroySpeed(world, pos) * 100.0f);
        ServerEvents.addBreaker(world, pos, state, (Player) caster, true, silk, fortune,
                strength, dur, dur, (int) (dur / strength / 3.0f * num),
                0.25f + (silk ? 0.25f : 0.0f) + fortune * 0.1f, null);

        return true;
    }

    @Override
    public NodeSetting[] createSettings() {
        int[] silk = {0, 1};
        String[] silkDesc = {"focus.common.no", "focus.common.yes"};

        int[] fortune = {0, 1, 2, 3, 4};
        String[] fortuneDesc = {"focus.common.no", "I", "II", "III", "IV"};

        return new NodeSetting[]{
                new NodeSetting("power", "focus.break.power",
                        new NodeSetting.NodeSettingIntRange(1, 5)),
                new NodeSetting("fortune", "focus.common.fortune",
                        new NodeSetting.NodeSettingIntList(fortune, fortuneDesc)),
                new NodeSetting("silk", "focus.common.silk",
                        new NodeSetting.NodeSettingIntList(silk, silkDesc))
        };
    }

    @Override
    public void renderParticleFX(Level level, double posX, double posY, double posZ,
                                 double motionX, double motionY, double motionZ) {

        FXDispatcher.GenPart part = new FXDispatcher.GenPart();
        part.age = 6 + level.getRandom().nextInt(6);
        int q = level.getRandom().nextInt(4);
        part.partStart = 704 + q * 3;
        part.partNum = 3;
        part.partInc = 1;
        part.slowDown = 0.8;
        part.scale = new float[]{(float) (1.7 + level.getRandom().nextGaussian() * 0.3)};
        part.loop = false;
        part.layer = 0;
        FXDispatcher.INSTANCE.drawGenericParticles(posX, posY, posZ, motionX, motionY, motionZ, part);
    }

    @Override
    public void onCast(Entity caster) {
        if (caster != null && caster.level() != null) {
            caster.level().playSound(null, caster.blockPosition().above(),
                    SoundEvents.END_GATEWAY_SPAWN, SoundSource.PLAYERS,
                    0.1f, 2.0f + (float) (caster.level().getRandom().nextGaussian() * 0.05));
        }
    }
}
