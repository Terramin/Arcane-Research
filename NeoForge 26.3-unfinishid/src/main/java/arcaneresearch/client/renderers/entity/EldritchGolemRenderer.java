package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.entity.EldritchGolemModel;
import arcaneresearch.common.entities.monster.boss.EntityEldritchGolem;

// NEOFORGE_PORT (26.3): spawn/headless/attack inputs via State.
public class EldritchGolemRenderer extends MobRenderer<EntityEldritchGolem, EldritchGolemRenderer.State, EldritchGolemModel> {

    public static class State extends LivingEntityRenderState {
        public int spawnTimer;
        public boolean headless;
        public int attackTimer;
    }

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/eldritch_golem.png");

    public EldritchGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchGolemModel(context.bakeLayer(EldritchGolemModel.LAYER_LOCATION)), 0.7F);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityEldritchGolem entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.spawnTimer = entity.getSpawnTimer();
        state.headless = entity.isHeadless();
        state.attackTimer = entity.getAttackTimer();
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return TEXTURE;
    }

    @Override
    protected void scale(State state, PoseStack poseStack) {
        poseStack.scale(1.7F, 1.7F, 1.7F);
    }
}