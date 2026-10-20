package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.entity.CrossbowModel;
import arcaneresearch.common.entities.construct.EntityTurretCrossbow;

// NEOFORGE_PORT (26.3): load progress + swing phase via State (swing from
// ArmedEntityRenderState.swingAnimation; LivingEntity.getAttackAnim gone).
// yBodyRot zeroing moved to extract (was render()).
public class TurretCrossbowRenderer extends MobRenderer<EntityTurretCrossbow, TurretCrossbowRenderer.State, CrossbowModel> {

    public static class State extends ArmedEntityRenderState {
        public float loadProgress;
        public float swing;
        public boolean minecart;
    }

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/crossbow.png");

    private final ItemModelResolver itemModelResolver;

    public TurretCrossbowRenderer(EntityRendererProvider.Context context) {
        super(context, new CrossbowModel(context.bakeLayer(CrossbowModel.LAYER_LOCATION)), 0.5F);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityTurretCrossbow entity, State state, float partialTicks) {
        entity.yBodyRot = 0.0F;
        entity.yBodyRotO = 0.0F;
        super.extractRenderState(entity, state, partialTicks);
        ArmedEntityRenderState.extractArmedEntityRenderState(entity, state, this.itemModelResolver, partialTicks);
        state.loadProgress = entity.getLoadProgress(partialTicks);
        state.swing = entity.getShootSwingPhase(partialTicks);
        state.minecart = entity.isPassenger()
                && entity.getVehicle() instanceof net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return TEXTURE;
    }

    @Override
    protected boolean shouldShowName(EntityTurretCrossbow entity, double distance) {
        return false;
    }
}