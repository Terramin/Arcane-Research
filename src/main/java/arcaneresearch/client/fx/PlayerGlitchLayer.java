package arcaneresearch.client.fx;

import arcaneresearch.Arcaneresearch;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
/** Glitch overlay layer, rebuilt on the NeoForge 26.x render-state API. */
public class PlayerGlitchLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

    private static final Identifier GLITCH_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/glowportal.png");

    private static final RenderType GLITCH_TYPE = RenderType.create("arcaneresearch_player_glitch",
            RenderSetup.builder(ArcaneFxPipelines.PLAYER_GLITCH)
                    .withTexture("Sampler0", GLITCH_TEXTURE)
                    .createRenderSetup());

    public PlayerGlitchLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                       AvatarRenderState state, float yRot, float xRot) {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Player player = level.getEntity(state.id) instanceof Player p ? p : null;
        if (player == null) {
            return;
        }
        float amount = PortalChargeFx.getAmount(player.getUUID());
        if (amount <= 0.01F) {
            return;
        }
        collector.order(1).submitModel(getParentModel(), state, poseStack, GLITCH_TYPE,
                packedLight, OverlayTexture.NO_OVERLAY,
                ARGB.colorFromFloat(Math.min(0.85F, amount), 1.0F, 1.0F, 1.0F));
    }
}
