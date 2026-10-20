package arcaneresearch.client.renderers.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.util.ARGB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber; // NEOFORGE_PORT: was Mod
import arcaneresearch.Arcaneresearch;

// NEOFORGE_PORT (26.3): fibre overlay re-renders the parent model translucent
// via collector.order(1).submitModel (oracle TaintOverlayLayer shape). Layers
// have no entity access, so champion-13 gating uses the Tainted opt-in flag
// on the state (our Mob renderers set it in extractRenderState from
// ChampionModifier); other states render nothing.
// States may opt in via Tainted; without it the layer resolves the entity by position.
public class LayerTainted<S extends LivingEntityRenderState, M extends EntityModel<? super S>>
        extends net.minecraft.client.renderer.entity.layers.RenderLayer<S, M> {

    /** Opt-in flag for states whose entity is a tainted champion (id 13). */
    public interface Tainted {
        default boolean isTaintedChampion() {
            return false;
        }
    }

    // NEOFORGE_PORT: layers get no entity access in 26.x (Forge read it in
    // render()); states may opt in via Tainted, otherwise resolve the living
    // entity by render position (covers vanilla mobs too, as in Forge).
    private static boolean isTaintedChampion(net.minecraft.client.renderer.entity.state.LivingEntityRenderState state) {
        if (state instanceof Tainted t && t.isTaintedChampion()) {
            return true;
        }
        try {
            net.minecraft.client.multiplayer.ClientLevel level =
                    net.minecraft.client.Minecraft.getInstance().level;
            if (level == null) {
                return false;
            }
            net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(
                    state.x - 0.5, state.y - 0.5, state.z - 0.5,
                    state.x + 0.5, state.y + state.boundingBoxHeight + 0.5, state.z + 0.5);
            net.minecraft.world.entity.LivingEntity best = null;
            double bestSq = Double.MAX_VALUE;
            for (net.minecraft.world.entity.LivingEntity living :
                    level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, box)) {
                double dsq = living.distanceToSqr(state.x, state.y, state.z);
                if (dsq < bestSq) {
                    bestSq = dsq;
                    best = living;
                }
            }
            return best != null
                    && arcaneresearch.common.entities.monster.mods.ChampionModifier.getChampionId(best) == 13;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static final Identifier TAINT_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/taint_fibres.png");

    public LayerTainted(net.minecraft.client.renderer.entity.RenderLayerParent<S, M> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                       S state, float yRot, float xRot) {
        if (!isTaintedChampion(state)) {
            return;
        }
        boolean invisible = state.isInvisible;
        // NEOFORGE_PORT: 7-arg submitModel (model, state, pose, type, light,
        // overlay, tint); render-phase: verify tint slot against vanilla callers.
        collector.order(1).submitModel(getParentModel(), state, poseStack,
                RenderTypes.entityTranslucent(TAINT_TEXTURE),
                invisible ? 0 : packedLight,
                // NEOFORGE_PORT (26.3): Forge drew the fibres with NO_OVERLAY (no hurt tint).
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                ARGB.colorFromFloat(invisible ? 0.0F : 0.66F, 1.0F, 1.0F, 1.0F));
    }

    @EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
    public static class Setup {
        @SubscribeEvent
        public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
            for (net.minecraft.world.entity.EntityType<?> type : event.getEntityTypes()) {
                if (event.getRenderer(type) instanceof LivingEntityRenderer<?, ?, ?> living) {
                    addTaintedLayer(living);
                }
            }
        }

        private static <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void addTaintedLayer(
                LivingEntityRenderer<?, S, M> renderer) {
            renderer.addLayer(new LayerTainted<>(renderer));
        }
    }
}