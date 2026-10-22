package arcaneresearch.client.renderers.entity.golem;

import net.minecraft.resources.Identifier; 
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.parts.PartModel;

public class PartModelHauler extends PartModel {

    public PartModelHauler(Identifier objModel, Identifier objTexture, EnumAttachPoint attachPoint) {
        super(objModel, objTexture, attachPoint);
    }

    @Override
    public void postRenderObjectPart(String partName, IGolemAPI golem, float partialTicks, EnumLimbSide side) {
        
        
        if (GolemRenderContext.poseStack == null || GolemRenderContext.collector == null
                || GolemRenderContext.state == null) {
            return;
        }
        GolemRenderHooks.renderCarried(GolemRenderContext.state.carriedState,
                GolemRenderContext.state.carriedIsBlock, GolemRenderContext.poseStack,
                GolemRenderContext.collector, GolemRenderContext.packedLight,
                GolemRenderContext.packedOverlay);
    }
}