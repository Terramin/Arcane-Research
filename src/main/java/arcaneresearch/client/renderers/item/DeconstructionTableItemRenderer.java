package arcaneresearch.client.renderers.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.client.renderers.DeconstructionTablePlacement;
import arcaneresearch.debug.ArcaneLogger;
import arcaneresearch.debug.LogLevel;
import arcaneresearch.debug.ProfileCategory;
import arcaneresearch.init.ModItems;

/** Item model of the deconstruction table with the thaumometer on top. */
@OnlyIn(Dist.CLIENT)
public final class DeconstructionTableItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ArcaneLogger LOGGER = ArcaneLogger.forCategory(ProfileCategory.RENDER);

    private ItemStack thaumometer;

    public DeconstructionTableItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transform, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Minecraft mc = Minecraft.getInstance();
        BakedModel base = mc.getItemRenderer().getModel(stack, mc.level, mc.player, 0);
        if (base instanceof CustomRendererItemModel wrapper) {
            base = wrapper.base();
        }
        ItemOverrides overrides = base.getOverrides();
        if (overrides != null) {
            BakedModel resolved = overrides.resolve(base, stack, mc.level, mc.player, 0);
            if (resolved != null) {
                base = resolved;
            }
        }
        for (BakedModel pass : base.getRenderPasses(stack, true)) {
            for (RenderType type : pass.getRenderTypes(stack, true)) {
                mc.getItemRenderer().renderModelLists(pass, stack, packedLight, packedOverlay,
                        poseStack, buffer.getBuffer(type));
            }
        }

        if (thaumometer == null) {
            thaumometer = new ItemStack(ModItems.THAUMOMETER.get());
        }
        LOGGER.once("deconstruction.item.thaumometer." + transform.name(), LogLevel.DEBUG,
                "deconstruction table item thaumometer ctx={} at={},{},{} scale={}",
                transform, DeconstructionTablePlacement.X, DeconstructionTablePlacement.Y,
                DeconstructionTablePlacement.Z, DeconstructionTablePlacement.SCALE);

        poseStack.pushPose();
        try {

            poseStack.translate(DeconstructionTablePlacement.X, DeconstructionTablePlacement.Y,
                    DeconstructionTablePlacement.Z);
            poseStack.scale(DeconstructionTablePlacement.SCALE, DeconstructionTablePlacement.SCALE,
                    DeconstructionTablePlacement.SCALE);
            mc.getItemRenderer().renderStatic(thaumometer, ItemDisplayContext.GROUND,
                    packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, mc.level, 0);
        } finally {
            poseStack.popPose();
        }
    }
}
