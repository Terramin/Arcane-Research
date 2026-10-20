package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.entity.EldritchGuardianModel;
import arcaneresearch.common.entities.monster.boss.EntityEldritchWarden;

// NEOFORGE_PORT (26.3): shares EldritchGuardianModel via GuardianState.
public class EldritchWardenRenderer extends MobRenderer<EntityEldritchWarden, EldritchWardenRenderer.State, EldritchGuardianModel> {

    public static class State extends EldritchGuardianModel.GuardianState {
        // NEOFORGE_PORT (Forge 1:1): spawn rise inputs (render-only).
        public int spawnTimer;
        public float bbHeight;
    }

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/eldritch_warden.png");

    public EldritchWardenRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchGuardianModel(context.bakeLayer(EldritchGuardianModel.LAYER_LOCATION)), 0.6F);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityEldritchWarden entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.armLiftR = entity.armLiftR;
        state.armLiftL = entity.armLiftL;
        state.spawnTimer = entity.getSpawnTimer();
        state.bbHeight = entity.getBbHeight();
        // NEOFORGE_PORT (Forge 1:1): прозрачность как у гардиана (запрос
        // пользователя): фейд 0.6→0; во Внешних землях варден непрозрачен.
        state.ghostAlpha = EldritchGuardianRenderer.ghostAlpha(entity, true);
    }

    // NEOFORGE_PORT (26.3): ghost fade via model tint (Forge model.renderAlpha).
    @Override
    protected int getModelTint(State state) {
        return net.minecraft.util.ARGB.white(state.ghostAlpha);
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return TEXTURE;
    }

    // NEOFORGE_PORT (Forge 1:1): как у гардиана — вершинная альфа модели
    // работает как у призрака (entityTranslucent).
    @Override
    protected RenderType getRenderType(State state, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderTypes.entityTranslucent(getTextureLocation(state));
    }

    @Override
    protected void scale(State state, PoseStack poseStack) {
        // TC4 func_77041_b: glScalef(1.5, 1.5, 1.5).
        poseStack.scale(1.5F, 1.5F, 1.5F);
    }

    @Override
    protected void setupRotations(State state, PoseStack poseStack, float ageInTicks, float rotationYaw) {
        super.setupRotations(state, poseStack, ageInTicks, rotationYaw);
        // java_old: d3 -= height * (spawnTimer / 150.0F) — поднимается из земли
        // вместе с дым-спиралью из clientTick.
        if (state.spawnTimer > 0) {
            poseStack.translate(0.0F, -state.bbHeight * (state.spawnTimer / 150.0F), 0.0F);
        }
    }
}
