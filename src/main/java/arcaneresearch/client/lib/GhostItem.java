package arcaneresearch.client.lib;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.joml.Matrix4f;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

 






















 
@net.neoforged.api.distmarker.OnlyIn(net.neoforged.api.distmarker.Dist.CLIENT)
public final class GhostItem {

      
    public static final float TINT_R = 0.85F;
    public static final float TINT_G = 0.72F;
    public static final float TINT_B = 1.0F;
    public static final float GHOST_ALPHA = 0.75F;

    
    
    private static final int GHOST_COLOR_ARGB =
            ((int) (255.0F * GHOST_ALPHA) << 24)
            | ((int) (255.0F * TINT_R) << 16)
            | ((int) (255.0F * TINT_G) << 8)
            | (int) (255.0F * TINT_B);

    
    
    private static final Field LAYERS_FIELD;
    private static final Field ACTIVE_COUNT_FIELD;
    private static final Field QUADS_FIELD;
    private static final Field ITEM_TRANSFORM_FIELD;
    private static final Field LOCAL_TRANSFORM_FIELD;
    private static final boolean REFLECTION_OK;

    static {
        Field layers = null, count = null, quads = null, transform = null, localTransform = null;
        boolean ok = true;
        try {
            layers = ItemStackRenderState.class.getDeclaredField("layers");
            layers.setAccessible(true);
            count = ItemStackRenderState.class.getDeclaredField("activeLayerCount");
            count.setAccessible(true);
            Class<?> layerClass = ItemStackRenderState.LayerRenderState.class;
            quads = layerClass.getDeclaredField("quads");
            quads.setAccessible(true);
            transform = layerClass.getDeclaredField("itemTransform");
            transform.setAccessible(true);
            localTransform = layerClass.getDeclaredField("localTransform");
            localTransform.setAccessible(true);
        } catch (Throwable t) {
            ok = false;
        }
        LAYERS_FIELD = layers;
        ACTIVE_COUNT_FIELD = count;
        QUADS_FIELD = quads;
        ITEM_TRANSFORM_FIELD = transform;
        LOCAL_TRANSFORM_FIELD = localTransform;
        REFLECTION_OK = ok;
    }

    private GhostItem() {
    }

     



 
    public static void render(ItemStackRenderState state, PoseStack poseStack,
                              SubmitNodeCollector collector, int packedLight) {
        if (state == null || state.isEmpty()) {
            return;
        }
        if (!REFLECTION_OK) {
            fallback(state, poseStack, collector, packedLight);
            return;
        }
        try {
            ItemStackRenderState.LayerRenderState[] layers =
                    (ItemStackRenderState.LayerRenderState[]) LAYERS_FIELD.get(state);
            int layerCount = ACTIVE_COUNT_FIELD.getInt(state);
            for (int i = 0; i < layerCount && i < layers.length; i++) {
                ItemStackRenderState.LayerRenderState layer = layers[i];
                ItemQuads quads = (ItemQuads) QUADS_FIELD.get(layer);
                if (quads == null || quads.isEmpty()) {
                    
                    
                    
                    
                    
                    fallback(state, poseStack, collector, packedLight);
                    return;
                }
                ItemTransform itemTransform = (ItemTransform) ITEM_TRANSFORM_FIELD.get(layer);
                Matrix4f localTransform = (Matrix4f) LOCAL_TRANSFORM_FIELD.get(layer);
                int[] tints = intArray(layer.tintLayers());

                
                poseStack.pushPose();
                itemTransform.apply(false, poseStack.last());
                poseStack.last().mulPose(localTransform);

                
                
                Map<Identifier, List<BakedQuad>> byAtlas = new LinkedHashMap<>();
                for (BakedQuad quad : quads.all()) {
                    TextureAtlasSprite sprite = quad.materialInfo().sprite();
                    byAtlas.computeIfAbsent(sprite.atlasLocation(), k -> new ArrayList<>()).add(quad);
                }
                for (Map.Entry<Identifier, List<BakedQuad>> entry : byAtlas.entrySet()) {
                    submitAtlas(entry.getValue(), tints, poseStack, collector, packedLight);
                }
                poseStack.popPose();
            }
        } catch (Throwable t) {
            fallback(state, poseStack, collector, packedLight);
        }
    }

    private static void submitAtlas(List<BakedQuad> atlasQuads, int[] tints,
                                    PoseStack poseStack, SubmitNodeCollector collector,
                                    int packedLight) {
        List<BakedQuad> quads = List.copyOf(atlasQuads);
        int[] tintCopy = tints;
        collector.submitCustomGeometry(poseStack,
                arcaneresearch.client.renderers.TCRenderTypes.ghostItem(
                        quads.get(0).materialInfo().sprite().atlasLocation()),
                (pose, consumer) -> {
                    QuadInstance instance = new QuadInstance();
                    instance.setLightCoords(packedLight);
                    instance.setOverlayCoords(OverlayTexture.NO_OVERLAY);
                    for (BakedQuad quad : quads) {
                        BakedQuad.MaterialInfo mat = quad.materialInfo();
                        int color = -1;
                        if (mat.isTinted()) {
                            int index = mat.tintIndex();
                            if (index >= 0 && index < tintCopy.length) {
                                color = tintCopy[index];
                            }
                        }
                        
                        instance.setColor(ARGB.multiply(color, GHOST_COLOR_ARGB));
                        consumer.putBakedQuad(pose, quad, instance);
                    }
                });
    }

    private static void fallback(ItemStackRenderState state, PoseStack poseStack,
                                 SubmitNodeCollector collector, int packedLight) {
        state.submit(poseStack, collector, packedLight, OverlayTexture.NO_OVERLAY, 0);
    }

    private static int[] intArray(java.util.List<Integer> list) {
        if (list == null) {
            return new int[0];
        }
        if (list instanceof it.unimi.dsi.fastutil.ints.IntList intList) {
            return intList.toIntArray();
        }
        int[] out = new int[list.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = list.get(i);
        }
        return out;
    }
}
