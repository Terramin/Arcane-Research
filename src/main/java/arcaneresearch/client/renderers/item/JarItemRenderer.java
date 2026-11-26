package arcaneresearch.client.renderers.item;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.renderers.JarLabelRenderer;
import arcaneresearch.common.blocks.essentia.BlockJarItem;
import arcaneresearch.debug.ArcaneLogger;
import arcaneresearch.debug.LogLevel;
import arcaneresearch.debug.ProfileCategory;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders the jar item geometry plus the paper label.
 *
 * <p>The label is glued to a fixed side of the model (never re-picked from the
 * live view matrix) and is anchored to the actual baked body quad of the jar,
 * so it sits on the real glass surface in every display context.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class JarItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ArcaneLogger LOGGER = ArcaneLogger.forCategory(ProfileCategory.RENDER);
    private static final float PLANE_EPS = 1.0E-4F;

    /**
     * Liquid is drawn before the glass and must not write depth: otherwise the
     * baked top faces of the body and lid clip the upper part of its side faces
     * when the item is viewed from an angle.
     */
    private static final RenderType LIQUID_TYPE = JarLiquidStates.create();

    public JarItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transform, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float lift = JarItemGroundOffset.lift(transform);
        if (lift != 0.0F) {
            poseStack.translate(0.0F, lift, 0.0F);
        }
        // A fixed side per context: no per-frame re-selection, so the label
        // cannot hop between faces while the item spins in hand or on the floor.
        int yaw = JarItemLabelSide.yawDegrees(transform);

        Minecraft mc = Minecraft.getInstance();
        boolean cutout = useCutout(stack, transform);
        BakedModel jar = null;
        BakedModel wrapped = mc.getItemRenderer().getModel(stack, mc.level, mc.player, 0);
        if (wrapped instanceof JarItemModel jarModel) {
            jar = renderJarModel(jarModel.base(), stack, cutout,
                    poseStack, buffer, packedLight, packedOverlay);
        }

        // The jar body is translucent; flush it so the label is composited on top.
        if (buffer instanceof MultiBufferSource.BufferSource source) {
            source.endBatch();
        }

        Aspect filter = BlockJarItem.getFilter(stack);
        if (filter == null) {
            return;
        }

        // The anchor is always taken from the model's +Z face; the yaw below
        // alone puts the label on the side required by the display context.
        JarLabelPlacement.Face face = bodyFace(jar, stack, cutout);
        JarLabelPlacement.Placement place = face == null
                ? null : JarLabelPlacement.fromFace(face, false);
        String source;
        if (place == null) {
            place = JarLabelPlacement.fallback(false);
            source = face == null ? "fallback:no-face" : "fallback:too-narrow";
        } else {
            source = "baked";
        }

        LOGGER.once("item.jarlabel." + transform.name() + '.' + source, LogLevel.DEBUG,
                "jar label ctx={} side={} yaw={} aspect={} placement={} source={}",
                transform, JarItemLabelSide.name(yaw), yaw, filter.getTag(), place, source);

        poseStack.pushPose();
        try {
            // ItemRenderer.render already applied translate(-0.5,-0.5,-0.5)
            // before calling the BEWLR. Undo it so the centred coordinates used
            // by the block renderer are not centred a second time, otherwise
            // the whole label is shifted half a block away from the jar.
            poseStack.translate(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            JarLabelRenderer.renderPlaced(place.x(), place.y(), place.z(), place.half(),
                    null, filter, poseStack, buffer, packedLight);
        } finally {
            poseStack.popPose();
        }
    }

    /** Renders the baked model (including fill-level overrides) and returns it. */
    private static BakedModel renderJarModel(BakedModel base, ItemStack stack, boolean cutout,
                                             PoseStack poseStack, MultiBufferSource buffer,
                                             int packedLight, int packedOverlay) {
        Minecraft mc = Minecraft.getInstance();
        ItemOverrides overrides = base.getOverrides();
        BakedModel model = overrides == null ? base : overrides.resolve(base, stack, mc.level, mc.player, 0);
        if (model == null) {
            model = base;
        }

        ItemRenderer renderer = mc.getItemRenderer();
        for (BakedModel pass : model.getRenderPasses(stack, cutout)) {
            List<BakedQuad> liquid = new ArrayList<>();
            List<BakedQuad> glass = new ArrayList<>();
            RandomSource random = RandomSource.create();
            for (Direction side : Direction.values()) {
                random.setSeed(42L);
                JarItemQuads.partition(pass.getQuads(null, side, random), liquid, glass);
            }
            random.setSeed(42L);
            JarItemQuads.partition(pass.getQuads(null, null, random), liquid, glass);

            // Liquid first and without depth writes. If the glass were drawn
            // first, its top quads would write depth and cut the upper part of
            // the liquid's side faces at oblique viewing angles.
            if (!liquid.isEmpty()) {
                renderer.renderQuadList(poseStack, buffer.getBuffer(LIQUID_TYPE), liquid,
                        stack, packedLight, packedOverlay);
            }
            for (RenderType type : pass.getRenderTypes(stack, cutout)) {
                renderer.renderQuadList(poseStack, buffer.getBuffer(type), glass,
                        stack, packedLight, packedOverlay);
            }
        }
        return model;
    }

    /** Same rule as vanilla {@code ItemRenderer.render} for the "fabulous" flag. */
    private static boolean useCutout(ItemStack stack, ItemDisplayContext transform) {
        if (transform == ItemDisplayContext.GUI || transform == ItemDisplayContext.GROUND
                || transform == ItemDisplayContext.FIXED || transform.firstPerson()) {
            return true;
        }
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return true;
        }
        return !(blockItem.getBlock() instanceof HalfTransparentBlock)
                && !(blockItem.getBlock() instanceof StainedGlassPaneBlock);
    }

    /**
     * Finds the outermost Z plane of the baked body and returns its bounds in
     * baked 0..1 coordinates, to be converted by {@link JarLabelPlacement}.
     */
    @Nullable
    private static JarLabelPlacement.Face bodyFace(BakedModel model, ItemStack stack, boolean cutout) {
        if (model == null) {
            return null;
        }
        List<float[]> rects = new ArrayList<>();
        RandomSource random = RandomSource.create();
        for (BakedModel pass : model.getRenderPasses(stack, cutout)) {
            for (Direction side : Direction.values()) {
                collectPlanarQuads(pass.getQuads(null, side, random), rects);
            }
            collectPlanarQuads(pass.getQuads(null, null, random), rects);
        }
        if (rects.isEmpty()) {
            return null;
        }

        // The label is always placed in the model's +Z frame, then rotated.
        float plane = Float.NEGATIVE_INFINITY;
        for (float[] rect : rects) {
            plane = Math.max(plane, rect[4]);
        }
        float minX = Float.POSITIVE_INFINITY, maxX = Float.NEGATIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
        boolean any = false;
        for (float[] rect : rects) {
            if (Math.abs(rect[4] - plane) > PLANE_EPS) {
                continue;
            }
            minX = Math.min(minX, rect[0]);
            maxX = Math.max(maxX, rect[1]);
            minY = Math.min(minY, rect[2]);
            maxY = Math.max(maxY, rect[3]);
            any = true;
        }
        if (!any) {
            return null;
        }
        return new JarLabelPlacement.Face(minX, maxX, minY, maxY, plane, plane);
    }

    /** Keeps only quads flat in Z; 1.20.1 BakedQuad layout is 8 ints per vertex. */
    private static void collectPlanarQuads(List<BakedQuad> quads, List<float[]> out) {
        for (BakedQuad quad : quads) {
            int[] vertices = quad.getVertices();
            float minX = Float.POSITIVE_INFINITY, maxX = Float.NEGATIVE_INFINITY;
            float minY = Float.POSITIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
            float minZ = Float.POSITIVE_INFINITY, maxZ = Float.NEGATIVE_INFINITY;
            for (int i = 0; i < 4; i++) {
                int base = i * 8;
                float x = Float.intBitsToFloat(vertices[base]);
                float y = Float.intBitsToFloat(vertices[base + 1]);
                float z = Float.intBitsToFloat(vertices[base + 2]);
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
                minZ = Math.min(minZ, z);
                maxZ = Math.max(maxZ, z);
            }
            if (maxZ - minZ > PLANE_EPS) {
                continue;
            }
            out.add(new float[]{minX, maxX, minY, maxY, minZ});
        }
    }

    /** Grants access to the protected vanilla render state shards. */
    private static final class JarLiquidStates extends RenderStateShard {
        private JarLiquidStates() {
            super("jar_liquid", () -> {
            }, () -> {
            });
        }

        static RenderType create() {
            return RenderType.create("arcaneresearch_jar_liquid",
                    DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256,
                    false, true,
                    RenderType.CompositeState.builder()
                            .setShaderState(new ShaderStateShard(GameRenderer::getRendertypeEntityTranslucentShader))
                            .setTextureState(new TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, false))
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setCullState(CULL)
                            .setLightmapState(LIGHTMAP)
                            .setOverlayState(OVERLAY)
                            .setWriteMaskState(COLOR_WRITE)
                            .setDepthTestState(LEQUAL_DEPTH_TEST)
                            .createCompositeState(true));
        }
    }
}
