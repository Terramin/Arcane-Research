package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.lib.UtilsFX;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.tiles.crafting.DeconstructTile;
import arcaneresearch.init.ModItems;

/**
 * Порт {@code thaumcraft.client.render.DeconstructionTableRenderer} (reforged):
 * парящий вращающийся предмет из слота и плоский тег выпавшего аспекта над столом.
 *
 * <p>NEOFORGE_PORT (Forge e45d90b dissolved temp.decon ->
 * client/renderers/tile/DeconstructTileRenderer.java, typed to DeconstructTile;
 * 26.3 render-state rework, паритет PedestalRenderer/AlembicRenderer):
 * render(tile, partialTicks, poseStack, buffer, light, overlay) -&gt;
 * createRenderState/extractRenderState/submit; PoseStack.mulPose -&gt; rotate;
 * предметы — через ItemModelResolver.updateForTopItem + ItemStackRenderState.submit;
 * тег аспекта — submitCustomGeometry с TCRenderTypes.translucentPortal
 * (addVertex/setColor/setUv/setOverlay/setLight/setNormal).</p>
 *
 * <p><b>Ghost-паритет:</b> Forge рисовал парящий предмет «призраком» —
 * {@code GhostItem.render(..., ghostAlpha = 0.75)} (обёртка MultiBufferSource с
 * подменой TransparencyStateShard → LIGHTNING + масштабированием цвета вершин
 * R0.85/G0.72/B1.0 × 0.75, паритет TC4 TileDeconstructionTableRenderer 57-59).
 * В 26.3 ItemFeatureRenderer сам выбирает материал-render types и классический
 * перехват невозможен, поэтому квады читаются из разрешённого
 * ItemStackRenderState и переэмитируются через {@code putBakedQuad} на
 * ghost-слой (см. {@link arcaneresearch.client.lib.GhostItem} +
 * TCRenderTypes.ghostItem). Спец-модели (wand и т.п.) в этом случае
 * рисуются обычным fallback'ом.</p>
 */
@OnlyIn(Dist.CLIENT)
public class DeconstructTileRenderer implements BlockEntityRenderer<DeconstructTile, DeconstructTileRenderer.State> {

    private static final float TAG_SCALE = 0.024F;

    public static class State extends BlockEntityRenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
        public final ItemStackRenderState thaumometer = new ItemStackRenderState();
        public boolean hasItem;
        public boolean hasThaumometer;
        public Aspect aspect;
        public double ticks;
    }

    private final ItemModelResolver itemModelResolver;

    public DeconstructTileRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(DeconstructTile tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        Level level = tile.getLevel();
        state.ticks = (level != null ? level.getGameTime() : 0) + partialTicks;

        state.hasThaumometer = ModItems.THAUMOMETER.get() != null;
        if (state.hasThaumometer) {
            itemModelResolver.updateForTopItem(state.thaumometer,
                    new ItemStack(ModItems.THAUMOMETER.get()), ItemDisplayContext.GROUND,
                    level, null, 0);
        }

        ItemStack stack = tile.getItem(0);
        state.hasItem = !stack.isEmpty();
        if (state.hasItem) {
            ItemStack renderStack = stack.copy();
            renderStack.setCount(1);
            itemModelResolver.updateForTopItem(state.item, renderStack, ItemDisplayContext.GROUND,
                    level, null, 0);
        }

        state.aspect = tile.currentAspect();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        // Паритет TC4: таумометр — EntityItem на столе, масштаб 0.8, без
        // вращения и покачивания (field_82407_g = true, field_70290_d = 0):
        // плоская иконка 0.5 * 0.8 = 0.4 блока лежит на столешнице
        // (0.92 + константный боб 0.1 = 1.02 над их моделью).
        // В порту столешница на y = 1.0 (deconstruction_table.json); диск
        // scanner.obj 2.8 в поперечнике, GROUND-скейл 0.35; паритетный скейл
        // рендера 0.41 давал 0.4 блока — по запросу увеличен дважды по 25%
        // (0.640625, ~0.63 блока); ориентация GROUND (без вращения) — плашмя.
        //
        // ВАЖНО: рендер предмета в 26.3 делает центрирование сам (модель
        // 0..1), а OBJ-диск (центр в origin) уезжает на пол-«модели»
        // вниз/вбок и тонет в столе (в GUI это скомпенсировано translation
        // [2.8,-2.8,0] в thaumometer.json). Здесь компенсируем сдвиг явно:
        // +0.5 * итоговый скейл по всем осям, чтобы центр диска попал в
        // (0.5, y, 0.5).
        if (state.hasThaumometer) {
            float scale = 0.640625F;       // скейл рендера (паритет 0.41, два раза по +25%)
            float total = scale * 0.35F;   // итоговый масштаб диска (~0.63 блока поперечник)
            float comp = 0.5F * total;
            poseStack.pushPose();
            poseStack.translate(0.5F + comp, 1.024F + comp, 0.5F + comp);
            poseStack.scale(scale, scale, scale);
            state.thaumometer.submit(poseStack, collector, state.lightCoords,
                    OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        if (state.hasItem) {
            poseStack.pushPose();
            poseStack.translate(0.5F, (float) (1.15F + Mth.sin(state.ticks / 14.0) * 0.1F + 0.1F), 0.5F);
            poseStack.rotate(Axis.YP.rotationDegrees((float) (state.ticks % 360.0)));
            // NEOFORGE_PORT (GhostItem 1:1): Forge рисовал парящий предмет
            // GhostItem.render(..., GROUND, GhostItem.GHOST_ALPHA=0.75) — аддитивный
            // «призрак» с тинтом. Порт рендера: client/lib/GhostItem (переэмиссия
            // квад'ов на ghost-слой, бо ItemFeatureRenderer не стилизуем).
            arcaneresearch.client.lib.GhostItem.render(state.item, poseStack, collector, state.lightCoords);
            poseStack.popPose();
        }

        Aspect aspect = state.aspect;
        if (aspect != null && aspect.getImage() != null) {
            int rgb = aspect.getColor();
            float r = ((rgb >> 16) & 0xFF) / 255.0F;
            float g = ((rgb >> 8) & 0xFF) / 255.0F;
            float b = (rgb & 0xFF) / 255.0F;
            poseStack.pushPose();
            poseStack.translate(0.5F, 1.081F, 0.5F);
            poseStack.rotate(Axis.XP.rotationDegrees(90.0F));
            poseStack.rotate(Axis.ZP.rotationDegrees((float) (state.ticks % 360.0)));
            poseStack.scale(TAG_SCALE, TAG_SCALE, TAG_SCALE);
            // NEOFORGE_PORT (Forge 1:1): вырезка фона через общий helper.
            RenderType type = TCRenderTypes.translucentPortal(UtilsFX.strippedAspectTexture(aspect.getImage()));
            int packedLight = state.lightCoords;
            collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
                quad(consumer, pose, -8.0F, -8.0F, 0.0F, 0.0F, 0.0F, packedLight, r, g, b, 0.8F);
                quad(consumer, pose, -8.0F, 8.0F, 0.0F, 0.0F, 1.0F, packedLight, r, g, b, 0.8F);
                quad(consumer, pose, 8.0F, 8.0F, 0.0F, 1.0F, 1.0F, packedLight, r, g, b, 0.8F);
                quad(consumer, pose, 8.0F, -8.0F, 0.0F, 1.0F, 0.0F, packedLight, r, g, b, 0.8F);
            });
            poseStack.popPose();
        }
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                             float x, float y, float z, float u, float v, int light,
                             float r, float g, float b, float a) {
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose, 0, 0, 1);
    }
}
