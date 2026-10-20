package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.entity.PechModel;
import arcaneresearch.client.renderers.entity.mob.PechHeldItemLayer;
import arcaneresearch.common.entities.monster.EntityPech;

// NEOFORGE_PORT (26.3 render-state rework): MobRenderer<T, S, M); state carries
// every entity field the old render()/model animation read (pechType for
// textures, mumble jaw, swing phase, crouch shift, main arm). Hand items come
// free via ArmedEntityRenderState.extractArmedEntityRenderState.
public class PechRenderer extends MobRenderer<EntityPech, PechRenderer.State, PechModel> {

    public static class State extends ArmedEntityRenderState {
        public int pechType;
        public float mumble;
        public boolean crouching;
        public boolean riding;
        public boolean holdingBow;
        public HumanoidArm mainArm = HumanoidArm.RIGHT;
    }

    private static final Identifier TEXTURE_FORAGER =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/pech_forage.png");
    private static final Identifier TEXTURE_MAGE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/pech_thaum.png");
    private static final Identifier TEXTURE_STALKER =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/pech_stalker.png");

    private final ItemModelResolver itemModelResolver;

    public PechRenderer(EntityRendererProvider.Context context) {
        super(context, new PechModel(context.bakeLayer(PechModel.LAYER_LOCATION)), 0.4F);
        this.itemModelResolver = context.getItemModelResolver();
        this.addLayer(new PechHeldItemLayer(this));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityPech entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        ArmedEntityRenderState.extractArmedEntityRenderState(entity, state, this.itemModelResolver, partialTicks);
        state.pechType = entity.getPechType();
        state.mumble = entity.getMumble();
        state.crouching = entity.isShiftKeyDown();
        state.riding = entity.isPassenger();
        state.mainArm = entity.getMainArm();
        state.holdingBow = entity.getMainHandItem().getItem() instanceof net.minecraft.world.item.BowItem;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        // NEOFORGE_PORT: old render() crouch dip (-0.125) preserved here.
        poseStack.pushPose();
        if (state.crouching) {
            poseStack.translate(0.0F, -0.125F, 0.0F);
        }
        super.submit(state, poseStack, collector, camera);
        poseStack.popPose();
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return switch (state.pechType) {
            case EntityPech.TYPE_MAGE -> TEXTURE_MAGE;
            case EntityPech.TYPE_FORAGER -> TEXTURE_STALKER;
            default -> TEXTURE_FORAGER;
        };
    }
}