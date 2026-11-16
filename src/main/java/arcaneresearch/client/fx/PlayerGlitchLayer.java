package arcaneresearch.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.Util;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class PlayerGlitchLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public PlayerGlitchLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        float amount = PortalChargeFx.getAmount(player.getUUID());
        if (amount <= 0.01F) {
            return;
        }
        ShaderInstance shader = FxWaveShaders.playerGlitch;
        if (shader == null) {
            return;
        }
        VertexConsumer consumer = buffer.getBuffer(FxWaveShaders.playerGlitchType());
        float time = (Util.getMillis() % 3600000L) / 1000.0F;
        shader.safeGetUniform("GlitchTime").set(time);
        shader.safeGetUniform("GlitchAmount").set(amount);
        getParentModel().renderToBuffer(poseStack, consumer, packedLight,
                OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}
