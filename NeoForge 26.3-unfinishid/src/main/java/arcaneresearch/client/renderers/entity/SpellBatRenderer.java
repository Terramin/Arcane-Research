package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.util.ARGB;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.entity.SpellBatModel;
import arcaneresearch.common.entities.monster.EntitySpellBat;

// NEOFORGE_PORT (26.3): MobRenderer<T, S, M>. Translucency via getRenderType
// hook (was override returning entityTranslucent), fullbright via light
// hooks + lightCoords, body tint via getModelTint hook (was model multiplying
// stored tintR/G/B in renderToBuffer).
public class SpellBatRenderer extends MobRenderer<EntitySpellBat, SpellBatRenderer.State, SpellBatModel> {

    public static class State extends LivingEntityRenderState {
        public int color = 0xFFFFFF;
    }

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/spellbat.png");

    public SpellBatRenderer(EntityRendererProvider.Context context) {
        super(context, new SpellBatModel(context.bakeLayer(SpellBatModel.LAYER_LOCATION)), 0.25F);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntitySpellBat entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.color = entity.color;
        state.lightCoords = 0xF000F0;
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return TEXTURE;
    }

    @Override
    protected RenderType getRenderType(State state, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderTypes.entityTranslucent(getTextureLocation(state));
    }

    @Override
    protected int getModelTint(State state) {
        int color = state.color;
        return ARGB.colorFromFloat(0.5F, ((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F);
    }

    @Override
    protected void scale(State state, PoseStack poseStack) {
        poseStack.scale(0.35F, 0.35F, 0.35F);
    }

    @Override
    protected void setupRotations(State state, PoseStack poseStack, float ageInTicks, float rotationYaw) {
        super.setupRotations(state, poseStack, ageInTicks, rotationYaw);
        poseStack.translate(0.0F, -0.1F, 0.0F);
    }

    @Override
    protected int getBlockLightLevel(EntitySpellBat entity, BlockPos pos) {
        return 15;
    }

    @Override
    protected int getSkyLightLevel(EntitySpellBat entity, BlockPos pos) {
        return 15;
    }
}