package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier; 
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.entity.EldritchCrabModel;
import arcaneresearch.common.entities.monster.EntityEldritchCrab;


public class EldritchCrabRenderer extends MobRenderer<EntityEldritchCrab, EldritchCrabRenderer.State, EldritchCrabModel> {

    public static class State extends LivingEntityRenderState {
        public boolean helm;
    }

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/crab.png");

    public EldritchCrabRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchCrabModel(context.bakeLayer(EldritchCrabModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityEldritchCrab entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.helm = entity.hasHelm();
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return TEXTURE;
    }

    @Override
    protected void scale(State state, PoseStack poseStack) {
        poseStack.scale(0.8F, 0.8F, 0.8F);
    }
}