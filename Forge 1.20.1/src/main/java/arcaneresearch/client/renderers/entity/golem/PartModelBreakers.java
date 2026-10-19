package arcaneresearch.client.renderers.entity.golem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.parts.PartModel;
@OnlyIn(Dist.CLIENT)
public class PartModelBreakers extends PartModel {
    private final java.util.Map<Integer, float[]> ani = new java.util.HashMap<>();
    public PartModelBreakers(ResourceLocation objModel, ResourceLocation objTexture, EnumAttachPoint attachPoint) {
        super(objModel, objTexture, attachPoint);
    }
    @Override
    public void preRenderObjectPart(String partName, IGolemAPI golem, float partialTicks, EnumLimbSide side) {
        if (!partName.equals("grinder") || GolemRenderContext.poseStack == null) {
            return;
        }
        LivingEntity entity = golem.getGolemEntity();
        float lastSpeed = 0.0f;
        float lastRot = 0.0f;
        if (ani.containsKey(entity.getId())) {
            lastSpeed = ani.get(entity.getId())[0];
            lastRot = ani.get(entity.getId())[1];
        }
        float f = Math.max(lastSpeed, entity.getAttackAnim(partialTicks) * 20.0f);
        float rot = lastRot + f;
        lastSpeed = f * 0.99f;
        ani.put(entity.getId(), new float[]{lastSpeed, rot});
        GolemRenderContext.poseStack.translate(0.0, -0.34, 0.0);
        float axis = (side == EnumLimbSide.LEFT) ? -1.0f : 1.0f;
        float angle = (entity.tickCount + partialTicks) / 2.0f + rot + ((side == EnumLimbSide.LEFT) ? 22 : 0);
        GolemRenderHooks.rotateDeg(GolemRenderContext.poseStack, angle, axis, 0.0f, 0.0f);
    }
}
