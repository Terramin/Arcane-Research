package arcaneresearch.client.renderers.tile;
import arcaneresearch.common.tiles.aura.EldritchAltarTile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.init.ModItems;
@OnlyIn(Dist.CLIENT)
public class EldritchAltarRenderer implements BlockEntityRenderer<EldritchAltarTile> {
    private static final ResourceLocation CAP_ALTAR =
            new ResourceLocation("arcaneresearch", "textures/block/obelisk_cap_altar.png");
    private static final ResourceLocation CAP_OUTER =
            new ResourceLocation("arcaneresearch", "textures/models/obelisk_cap_2.png");
    public EldritchAltarRenderer(BlockEntityRendererProvider.Context context) {
    }
    @Override
    public void render(EldritchAltarTile tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ResourceLocation capTexture = CAP_ALTAR;
        try {
            if (tile.getLevel() != null
                    && tile.getLevel().dimension().location().getPath().contains("outer")) {
                capTexture = CAP_OUTER;
            }
        } catch (Exception ignored) {
        }
        ObeliskCapModels.renderCap(poseStack, buffer, capTexture, packedLight, packedOverlay);
        int eyes = 0;
        try {
            eyes = tile.getEyes();
        } catch (Exception ignored) {
            return;
        }
        if (eyes <= 0) {
            return;
        }
        ItemStack eye;
        try {
            eye = new ItemStack(ModItems.ELDRITCH_EYE.get());
        } catch (Exception ignored) {
            return;
        }
        for (int a = 0; a < Math.min(eyes, 4); a++) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.0D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(a * 90.0F));
            poseStack.translate(0.46D, 0.2D, 0.0D);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(-18.0F));
            poseStack.scale(0.5F, 0.5F, 0.5F);
            try {
                Minecraft.getInstance().getItemRenderer().renderStatic(
                        eye, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                        poseStack, buffer, tile.getLevel(), 0);
            } catch (Exception ignored) {
            }
            poseStack.popPose();
        }
    }
}
