package arcaneresearch.client.renderers.entity.golem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.parts.PartModel;
@OnlyIn(Dist.CLIENT)
public class PartModelHauler extends PartModel {
    public PartModelHauler(ResourceLocation objModel, ResourceLocation objTexture, EnumAttachPoint attachPoint) {
        super(objModel, objTexture, attachPoint);
    }
    @Override
    public void postRenderObjectPart(String partName, IGolemAPI golem, float partialTicks, EnumLimbSide side) {
        if (GolemRenderContext.poseStack == null || GolemRenderContext.buffer == null) {
            return;
        }
        if (golem.getCarrying().size() > 1 && golem.getCarrying().get(1) != null) {
            ItemStack itemstack = golem.getCarrying().get(1);
            if (itemstack != null && !itemstack.isEmpty()) {
                GolemRenderHooks.renderCarried(golem, GolemRenderContext.poseStack,
                        GolemRenderContext.buffer, GolemRenderContext.packedLight,
                        GolemRenderContext.packedOverlay, itemstack);
            }
        }
    }
}
