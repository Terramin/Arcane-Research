package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.entity.FireBatModel;
import arcaneresearch.common.entities.monster.EntityFireBat;

// NEOFORGE_PORT (26.3): MobRenderer<T, S, M>; old scale()/setupRotations()
// overrides map 1:1 onto the state-based hooks; hanging flag via State.
public class FireBatRenderer extends MobRenderer<EntityFireBat, FireBatRenderer.State, FireBatModel> {

    public static class State extends LivingEntityRenderState {
        public boolean hanging;
    }

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/firebat.png");

    public FireBatRenderer(EntityRendererProvider.Context context) {
        super(context, new FireBatModel(context.bakeLayer(FireBatModel.LAYER_LOCATION)), 0.25F);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityFireBat entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.hanging = entity.getIsBatHanging();
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return TEXTURE;
    }

    @Override
    protected void scale(State state, PoseStack poseStack) {
        poseStack.scale(0.35F, 0.35F, 0.35F);
    }

    @Override
    protected void setupRotations(State state, PoseStack poseStack, float ageInTicks, float rotationYaw) {
        super.setupRotations(state, poseStack, ageInTicks, rotationYaw);
        if (!state.hanging) {
            poseStack.translate(0.0F, Mth.cos(ageInTicks * 0.3F) * 0.1F, 0.0F);
        } else {
            poseStack.translate(0.0F, -0.1F, 0.0F);
        }
    }
}