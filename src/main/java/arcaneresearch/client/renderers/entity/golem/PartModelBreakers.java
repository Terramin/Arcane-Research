package arcaneresearch.client.renderers.entity.golem;

import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.parts.PartModel;

public class PartModelBreakers extends PartModel {

    private final java.util.Map<Integer, float[]> ani = new java.util.HashMap<>();

    public PartModelBreakers(Identifier objModel, Identifier objTexture, EnumAttachPoint attachPoint) {
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
        // NEOFORGE_PORT: LivingEntity.getAttackAnim gone -> golem swing phase.
        float swing = entity instanceof arcaneresearch.common.golems.EntityArcaneresearchGolem golemEntity
                ? golemEntity.getArmSwingPhase(partialTicks) : 0.0F;
        float f = Math.max(lastSpeed, swing * 20.0f);
        float rot = lastRot + f;
        lastSpeed = f * 0.99f;
        ani.put(entity.getId(), new float[]{lastSpeed, rot});
        GolemRenderContext.poseStack.translate(0.0, -0.34, 0.0);
        float axis = (side == EnumLimbSide.LEFT) ? -1.0f : 1.0f;
        float angle = (entity.tickCount + partialTicks) / 2.0f + rot + ((side == EnumLimbSide.LEFT) ? 22 : 0);
        GolemRenderHooks.rotateDeg(GolemRenderContext.poseStack, angle, axis, 0.0f, 0.0f);
    }
}