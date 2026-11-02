package arcaneresearch.client.renderers.tile;

import arcaneresearch.common.tiles.aura.EldritchAltarTile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import arcaneresearch.init.ModItems;

@OnlyIn(Dist.CLIENT)
public class EldritchAltarRenderer implements BlockEntityRenderer<EldritchAltarTile> {
    private static final ResourceLocation CAP_ALTAR =
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/block/obelisk_cap_altar.png");
    private static final ResourceLocation CAP_OUTER =
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/models/obelisk_cap_2.png");

    private static final Quaternionf ROT_YP_90 = Axis.YP.rotationDegrees(90.0F);
    private static final Quaternionf ROT_XP_NEG_18 = Axis.XP.rotationDegrees(-18.0F);
    private static final Quaternionf[] EYE_ROTATIONS = new Quaternionf[4];

    static {
        EYE_ROTATIONS[0] = new Quaternionf();
        EYE_ROTATIONS[1] = Axis.YP.rotationDegrees(90.0F);
        EYE_ROTATIONS[2] = Axis.YP.rotationDegrees(180.0F);
        EYE_ROTATIONS[3] = Axis.YP.rotationDegrees(270.0F);
    }

    public EldritchAltarRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull EldritchAltarTile tile, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = tile.getLevel();

        ResourceLocation capTexture = CAP_ALTAR;
        if (level != null) {
            try {
                ResourceLocation dimLoc = level.dimension().location();
                if (dimLoc.getPath().contains("outer")) {
                    capTexture = CAP_OUTER;
                }
            } catch (Exception ignored) {
            }
        }
        ObeliskCapModels.renderCap(poseStack, buffer, capTexture, packedLight, packedOverlay);

        if (level == null) return;

        int eyes;
        try {
            eyes = tile.getEyes();
        } catch (Exception ignored) {
            return;
        }
        if (eyes <= 0) return;

        if (ModItems.ELDRITCH_EYE == null) return;
        Item eyeItem;
        try {
            eyeItem = ModItems.ELDRITCH_EYE.get();
        } catch (Exception ignored) {
            return;
        }
        ItemStack eye = new ItemStack(eyeItem);
        if (eye.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();

        int count = Math.min(eyes, 4);
        for (int a = 0; a < count; a++) {
            poseStack.pushPose();
            try {
                poseStack.translate(0.5D, 0.0D, 0.5D);
                Quaternionf rot = EYE_ROTATIONS[a];
                if (a != 0) {
                    poseStack.mulPose(rot);
                }
                poseStack.translate(0.46D, 0.2D, 0.0D);
                poseStack.mulPose(ROT_YP_90);
                poseStack.mulPose(ROT_XP_NEG_18);
                poseStack.scale(0.5F, 0.5F, 0.5F);
                itemRenderer.renderStatic(eye, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                        poseStack, buffer, level, 0);
            } catch (Throwable ignored) {
            } finally {
                poseStack.popPose();
            }
        }
    }
}