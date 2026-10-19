package arcaneresearch.common.golems.parts;

import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.parts.GolemLeg;
import arcaneresearch.client.fx.FXDispatcher;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class GolemLegLevitator implements GolemLeg.ILegFunction {

    @Override
    public void onUpdateTick(IGolemAPI golem) {
        if (golem.getGolemWorld().isClientSide()) {
            doFlyFX(golem);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void doFlyFX(IGolemAPI golem) {
        if (!golem.getGolemEntity().onGround() || golem.getGolemEntity().tickCount % 5 == 0) {
            FXDispatcher.INSTANCE.drawGolemFlyParticles(
                    golem.getGolemEntity().getX(), golem.getGolemEntity().getY() + 0.1D,
                    golem.getGolemEntity().getZ(),
                    golem.getGolemWorld().getRandom().nextGaussian() / 100.0D, -0.1D,
                    golem.getGolemWorld().getRandom().nextGaussian() / 100.0D);
        }
    }
}
