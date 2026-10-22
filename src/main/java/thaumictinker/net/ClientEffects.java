package thaumictinker.net;

import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.client.Minecraft;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import arcaneresearch.client.fx.FXDispatcher;
import thaumictinker.foci.FociEffects;

 


 
@OnlyIn(Dist.CLIENT)
public final class ClientEffects {

    private ClientEffects() {
    }

    public static void handle(TtNet.FociEffectPayload payload) {
        double x = payload.bx + 0.5;
        double y = payload.by + 0.5;
        double z = payload.bz + 0.5;
        switch (payload.kind) {
            case FociEffects.PICK_BURST -> burst(x, y, z, 1.0F, 0.75F, 0.2F);
            case FociEffects.PLACE_BURST -> burst(x, y, z, 0.2F, 0.9F, 1.0F);
            case FociEffects.SMELT_DONE -> burst(x, y, z, 1.0F, 0.5F, 0.0F);
            case FociEffects.SMELT_BEAM, FociEffects.SMELT_PROGRESS -> FXDispatcher.INSTANCE.sparkle((float) x, (float) y, (float) z, 1.0F, 0.5F, 0.1F);
            default -> {
            }
        }
    }

    private static void burst(double x, double y, double z, float r, float g, float b) {
        FXDispatcher.INSTANCE.blockRunes(x, y, z, r, g, b, 20, 0.0F);
        Minecraft.getInstance().level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.05, 0.0);
    }

      
    public static void cropFx(TtNet.CropFxPayload payload) {
        FXDispatcher.INSTANCE.essentiaTrailFx(payload.from, payload.to, 50, payload.color, 1.0F, 0);
    }

      
    public static void elementFx(TtNet.ElementsFxPayload payload) {
        switch (payload.kind) {
            case TtNet.ElementsFxPayload.SPARKLE ->
                    FXDispatcher.INSTANCE.sparkle((float) payload.x, (float) payload.y, (float) payload.z, 0.6F, 0.8F, 1.0F);
            case TtNet.ElementsFxPayload.BURST -> burst(payload.x, payload.y, payload.z, 0.3F, 0.6F, 1.0F);
            case TtNet.ElementsFxPayload.WISPS -> {
                for (int i = 0; i < 3; i++) {
                    FXDispatcher.INSTANCE.sparkle(
                            (float) (payload.x + (ClientEffects.random() - 0.5) * payload.size * 2.0),
                            (float) (payload.y + (ClientEffects.random() - 0.5) * payload.size * 2.0),
                            (float) (payload.z + (ClientEffects.random() - 0.5) * payload.size * 2.0),
                            1.0F, 0.5F, 0.0F);
                }
            }
            default -> {
            }
        }
    }

    private static double random() {
        return Math.random();
    }

      
    public static void armorStatus(boolean enabled) {
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.screen != null) {
            return;
        }
        minecraft.gui.setOverlayMessage(net.minecraft.network.chat.Component.translatable(
                enabled ? "ttinkerer.ttmisc.enableAllArmor" : "ttinkerer.ttmisc.disableAllArmor"), false);
    }

     


 
    public static void shadowbeam(TtNet.ShadowbeamPayload payload) {
        FXDispatcher fx = FXDispatcher.INSTANCE;
        int size = payload.path.size();
        int step = Math.max(1, size / 64);
        for (int index = 0; index < size; index += step) {
            TtNet.ShadowbeamPayload.Vec3f point = payload.path.get(index);
            fx.spark(point.x(), point.y(), point.z(), 2.0F, 0.15F, 0.02F, 0.25F, 0.8F);
        }
    }
}
