package arcaneresearch.client.renderers.tile;
import arcaneresearch.common.tiles.aura.EldritchCapTile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
@OnlyIn(Dist.CLIENT)
public class EldritchCapRenderer implements BlockEntityRenderer<EldritchCapTile> {
    private static final ResourceLocation CAP =
            new ResourceLocation("arcaneresearch", "textures/models/obelisk_cap.png");
    private static final ResourceLocation CAP_OUTER =
            new ResourceLocation("arcaneresearch", "textures/models/obelisk_cap_2.png");
    public EldritchCapRenderer(BlockEntityRendererProvider.Context context) {
    }
    @Override
    public void render(EldritchCapTile tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tile.getLevel() == null) {
            return;
        }
        ResourceLocation texture = CAP;
        try {
            if (tile.getLevel().dimension().location().getPath().contains("outer")) {
                texture = CAP_OUTER;
            }
        } catch (Exception ignored) {
        }
        ObeliskCapModels.renderCap(poseStack, buffer, texture, packedLight, packedOverlay);
    }
}
