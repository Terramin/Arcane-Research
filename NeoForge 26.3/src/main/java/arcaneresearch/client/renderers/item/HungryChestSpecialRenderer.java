package arcaneresearch.client.renderers.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.renderers.TCRenderTypes;

import java.util.function.Consumer;

// NEOFORGE_PORT (26.3): HungryChestItemRenderer (BEWLR) -> SpecialModelRenderer.
// Forge renderByItem() body 1:1 (closed chest parts).
public class HungryChestSpecialRenderer implements SpecialModelRenderer<Void> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/block/chesthungry.png");

    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;

    public HungryChestSpecialRenderer(ModelPart root) {
        this.bottom = root.getChild("bottom");
        this.lid = root.getChild("lid");
        this.lock = root.getChild("lock");
    }

    @Override
    public @Nullable Void extractArgument(ItemStack stack) {
        return null;
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        output.accept(new org.joml.Vector3f(0, 0, 0));
        output.accept(new org.joml.Vector3f(1, 1, 1));
    }

    @Override
    public void submit(@Nullable Void arg, PoseStack poseStack, SubmitNodeCollector collector,
                       int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        lid.xRot = 0.0f;
        lock.xRot = 0.0f;

        net.minecraft.client.renderer.rendertype.RenderType type = TCRenderTypes.objSolid(TEXTURE);
        collector.submitModelPart(lid, poseStack, type, lightCoords,
                OverlayTexture.NO_OVERLAY, null, -1, outlineColor);
        collector.submitModelPart(lock, poseStack, type, lightCoords,
                OverlayTexture.NO_OVERLAY, null, -1, outlineColor);
        collector.submitModelPart(bottom, poseStack, type, lightCoords,
                OverlayTexture.NO_OVERLAY, null, -1, outlineColor);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<Void> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
            ModelPart root = context.entityModelSet().bakeLayer(ModelLayers.CHEST);
            return new HungryChestSpecialRenderer(root);
        }
    }
}