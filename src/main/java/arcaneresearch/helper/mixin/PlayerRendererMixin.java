package arcaneresearch.helper.mixin;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds the portal-glitch render layer to the player (avatar) renderer.
 *
 * <p>The layer itself is loaded reflectively so that this mixin keeps compiling
 * independently of the client FX class, which is ported in a different scope.</p>
 */
@Mixin(AvatarRenderer.class)
public abstract class PlayerRendererMixin
        extends LivingEntityRenderer<Avatar, AvatarRenderState, PlayerModel> {

    public PlayerRendererMixin(EntityRendererProvider.Context context,
                               PlayerModel model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void arcaneresearch$addGlitchLayer(EntityRendererProvider.Context context, boolean slimSteve,
                                               CallbackInfo ci) {
        try {
            Class<?> layerClass = Class.forName("arcaneresearch.client.fx.PlayerGlitchLayer");
            java.lang.reflect.Constructor<?> ctor = layerClass.getConstructor(RenderLayerParent.class);
            Object layer = ctor.newInstance(this);
            //noinspection unchecked
            this.addLayer((RenderLayer<AvatarRenderState, PlayerModel>) layer);
        } catch (Throwable ignored) {
            // The FX layer is optional; never break rendering because of it.
        }
    }
}
