package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier; 
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.entity.TaintacleModel;
import arcaneresearch.common.entities.monster.boss.EntityTaintacleGiant;
import arcaneresearch.common.entities.monster.tainted.EntityTaintacle;
import arcaneresearch.common.entities.monster.tainted.EntityTaintacleSmall;




public class TaintacleRenderer extends MobRenderer<EntityTaintacle, TaintacleRenderer.State, TaintacleModel> {

    public static class State extends LivingEntityRenderState {
        public float height;
        public float grow;
        public float shadow = 0.6F;
        public int tipIndex;
        public int anchor;
        public float flail = 1.0F;
        public float hurt;
    }

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/taintacle.png");

    public TaintacleRenderer(EntityRendererProvider.Context context) {
        super(context, new TaintacleModel(context.bakeLayer(TaintacleModel.LAYER_LOCATION)), 0.6F);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityTaintacle entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        float height = entity.getBbHeight();
        float hc = height * 10.0F;
        float grow = 0.0F;
        if (entity.tickCount < hc) {
            grow = (hc - entity.tickCount) / hc * height;
        }
        state.height = height;
        state.grow = grow;
        if (entity instanceof EntityTaintacleGiant) {
            state.shadow = 1.0F;
        } else if (entity instanceof EntityTaintacleSmall) {
            state.shadow = 0.2F;
        } else {
            state.shadow = 0.6F;
        }
        int tipIndex = TaintacleModel.chainLengthFor(entity) - 2;
        state.tipIndex = tipIndex;
        state.anchor = tipIndex <= TaintacleModel.SMALL_TIP ? TaintacleModel.SMALL_TIP
                : tipIndex <= TaintacleModel.NORMAL_TIP ? TaintacleModel.NORMAL_TIP : TaintacleModel.GIANT_TIP;
        state.flail = entity.flailIntensity;
        state.hurt = entity.hurtTime;
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return TEXTURE;
    }

    @Override
    protected float getShadowRadius(State state) {
        return state.shadow;
    }

    @Override
    protected void scale(State state, PoseStack poseStack) {
        float height = state.height;
        float s = height / 3.0F;
        float off = ((height == 3.0F) ? 0.6F : 1.2F) + state.grow + 1.501F * (s - 1.0F);
        poseStack.translate(0.0F, off, 0.0F);
        poseStack.scale(s, s, s);
    }
}