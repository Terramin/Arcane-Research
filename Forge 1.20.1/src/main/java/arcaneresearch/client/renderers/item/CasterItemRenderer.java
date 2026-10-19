package arcaneresearch.client.renderers.item;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import arcaneresearch.common.items.casters.ItemCaster;
import java.util.function.Consumer;
@OnlyIn(Dist.CLIENT)
public class CasterItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final ItemRenderer itemRenderer;
    public CasterItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }
    public static void registerClientItemExtensions(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final CasterItemRenderer renderer = new CasterItemRenderer();
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        });
    }
    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BakedModel model = itemRenderer.getModel(stack, Minecraft.getInstance().level, null, 0);
        itemRenderer.render(stack, displayContext, false, poseStack, buffer, packedLight, packedOverlay, model);
        ItemStack focusStack = getFocusStack(stack);
        if (focusStack != null && !focusStack.isEmpty()) {
            renderFocusOnGauntlet(focusStack, displayContext, poseStack, buffer, packedLight, packedOverlay);
        }
    }
    private void renderFocusOnGauntlet(ItemStack focusStack, ItemDisplayContext displayContext,
                                       PoseStack poseStack, MultiBufferSource buffer,
                                       int packedLight, int packedOverlay) {
        poseStack.pushPose();
        float cx = 8.0f / 16.0f;
        float cy = 4.5f / 16.0f;
        float cz = 10.25f / 16.0f;
        switch (displayContext) {
            case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
                poseStack.translate(0.0625f, -0.125f, 0.0625f);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                poseStack.mulPose(Axis.ZP.rotationDegrees(180));
                poseStack.scale(0.5f, 0.5f, 0.5f);
            }
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
                poseStack.translate(0.125f, -0.0625f, 0.3125f);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                poseStack.mulPose(Axis.ZP.rotationDegrees(180));
                poseStack.scale(0.6f, 0.6f, 0.6f);
            }
            case GUI -> {
                poseStack.translate(-0.375f, -0.3125f, 0.0f);
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
                poseStack.scale(0.75f, 0.75f, 0.75f);
            }
            case GROUND -> {
                poseStack.translate(0.0f, -0.5f, 0.0f);
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
                poseStack.scale(0.5f, 0.5f, 0.5f);
            }
            case FIXED -> {
                poseStack.translate(0.0f, -0.5f, 0.0f);
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
                poseStack.scale(0.5f, 0.5f, 0.5f);
            }
            default -> {
                poseStack.translate(cx, cy, cz);
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
                poseStack.scale(0.5f, 0.5f, 0.5f);
            }
        }
        itemRenderer.renderStatic(focusStack, ItemDisplayContext.FIXED, packedLight,
                packedOverlay, poseStack, buffer, Minecraft.getInstance().level, 0);
        poseStack.popPose();
    }
    private ItemStack getFocusStack(ItemStack casterStack) {
        if (casterStack.getItem() instanceof ItemCaster caster) {
            return caster.getFocusStack(casterStack);
        }
        return ItemStack.EMPTY;
    }
}