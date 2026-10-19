package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.entity.TaintSeedModel;
import arcaneresearch.common.entities.monster.tainted.EntityTaintSeed;
import arcaneresearch.common.entities.monster.tainted.EntityTaintSeedPrime;

// NEOFORGE_PORT (26.3): grow/pulse/shadow inputs via State (were entity reads
// in scale()); model animation inputs (hurt/attack phase) via State.
public class TaintSeedRenderer extends MobRenderer<EntityTaintSeed, TaintSeedRenderer.State, TaintSeedModel> {

    public static class State extends LivingEntityRenderState {
        public float height;
        public float grow;
        public boolean prime;
        public float hurtAmount;
        public float attackPhase;
    }

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/taintseed.png");

    public TaintSeedRenderer(EntityRendererProvider.Context context) {
        super(context, new TaintSeedModel(context.bakeLayer(TaintSeedModel.LAYER_LOCATION)), 0.4F);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityTaintSeed entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        float height = entity.getBbHeight();
        float hc = height * 10.0F;
        float grow = 0.0F;
        if (entity.tickCount < hc) {
            grow = (hc - entity.tickCount) / hc * height;
        }
        state.height = height;
        state.grow = grow;
        state.prime = entity instanceof EntityTaintSeedPrime;
        state.hurtAmount = entity.hurtTime / 200.0F;
        state.attackPhase = entity.attackAnim;
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return TEXTURE;
    }

    @Override
    protected float getShadowRadius(State state) {
        return state.prime ? 0.6F : 0.5F;
    }

    @Override
    protected void scale(State state, PoseStack poseStack) {
        float height = state.height;
        float s = height / 2.0F;
        float offset = (((height == 3.0F) ? 0.6F : 1.2F) + state.grow) + 1.501F * (s - 1.0F);
        poseStack.translate(0.0F, offset, 0.0F);
        float pulse = 1.0F + 0.03F * (float) Math.sin(state.ageInTicks / 12.0F);
        poseStack.scale(s * pulse, s, s * pulse);
    }
}