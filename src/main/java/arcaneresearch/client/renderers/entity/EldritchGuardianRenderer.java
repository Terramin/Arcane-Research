package arcaneresearch.client.renderers.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier; 
import net.minecraft.world.Difficulty;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.entity.EldritchGuardianModel;
import arcaneresearch.common.entities.monster.EntityEldritchGuardian;
import arcaneresearch.common.world.maze.MazeFeature;



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

    
    
    
    @Override
    protected RenderType getRenderType(State state, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderTypes.entityTranslucent(getTextureLocation(state));
    }

     




 
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
