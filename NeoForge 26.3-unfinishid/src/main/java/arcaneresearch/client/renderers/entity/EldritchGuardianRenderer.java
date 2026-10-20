package arcaneresearch.client.renderers.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.world.Difficulty;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.entity.EldritchGuardianModel;
import arcaneresearch.common.entities.monster.EntityEldritchGuardian;
import arcaneresearch.common.world.maze.MazeFeature;

// NEOFORGE_PORT (26.3): arm lifts via shared GuardianState (model is shared
// with the warden renderer).
public class EldritchGuardianRenderer extends MobRenderer<EntityEldritchGuardian, EldritchGuardianRenderer.State, EldritchGuardianModel> {

    public static class State extends EldritchGuardianModel.GuardianState {
    }

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/eldritch_guardian.png");

    public EldritchGuardianRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchGuardianModel(context.bakeLayer(EldritchGuardianModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityEldritchGuardian entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.armLiftR = entity.armLiftR;
        state.armLiftL = entity.armLiftL;
        // NEOFORGE_PORT (26.3): Forge model.renderAlpha; the 8-float renderToBuffer that
        // applied it is dead, so the per-entity fade goes through getModelTint.
        state.ghostAlpha = ghostAlpha(entity, true);
    }

    @Override
    protected int getModelTint(State state) {
        return net.minecraft.util.ARGB.white(state.ghostAlpha);
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return TEXTURE;
    }

    // NEOFORGE_PORT (Forge 1:1): Forge overrode getRenderType(entity, ...) to
    // return entityTranslucent; 26.3 takes the render state. Translucent so the
    // model vertex alpha (ghost fade) is honoured like the phantom slime.
    @Override
    protected RenderType getRenderType(State state, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderTypes.entityTranslucent(getTextureLocation(state));
    }

    /**
     * Паритет java_old 37-58: alpha = 0.6 вблизи (d^2 &lt; 256), далее
     * линейно к 0 на дальности «видимости» (d^2 &lt; 576 Hard / 1024 иное);
     * 1.0 — во «своём» измерении (гардиан во Внешних землях непрозрачен).
     * Общая математика призрачного фейда; используется и рендером вардена.
     */
    static float ghostAlpha(net.minecraft.world.entity.Entity entity, boolean opaqueInOuterLands) {
        try {
            if (opaqueInOuterLands && entity.level().dimension().equals(MazeFeature.DIMENSION)) {
                return 1.0f;
            }
        } catch (Exception ignored) {
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return 1.0f;
        }
        float visRange = entity.level().getDifficulty() == Difficulty.HARD ? 576.0f : 1024.0f;
        float fadeStart = 256.0f;
        double distSq = entity.distanceToSqr(player.getX(), player.getY(), player.getZ());
        if (distSq < fadeStart) {
            return 0.6f;
        }
        return (1.0f - Math.min(visRange - fadeStart, (float) distSq - fadeStart)
                / (visRange - fadeStart)) * 0.6f;
    }
}
