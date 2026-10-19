package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.zombie.ZombieModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.entities.monster.EntityBrainyZombie;
import arcaneresearch.common.entities.monster.EntityGiantBrainyZombie;

// NEOFORGE_PORT (26.3): vanilla ZombieModel/ZombieRenderState (moved packages);
// giant scale via State (entity switch in extract, applied in scale hook).
public class BrainyZombieRenderer extends HumanoidMobRenderer<EntityBrainyZombie, BrainyZombieRenderer.State, ZombieModel<BrainyZombieRenderer.State>> {

    public static class State extends ZombieRenderState {
        public float giantScale = 1.0F;
    }

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/bzombie.png");

    public BrainyZombieRenderer(EntityRendererProvider.Context context) {
        super(context, new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityBrainyZombie entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.giantScale = entity instanceof EntityGiantBrainyZombie giant ? giant.getScale() : 1.0F;
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return TEXTURE;
    }

    @Override
    protected void scale(State state, PoseStack poseStack) {
        float s = state.giantScale;
        poseStack.scale(s, s, s);
        super.scale(state, poseStack);
    }
}