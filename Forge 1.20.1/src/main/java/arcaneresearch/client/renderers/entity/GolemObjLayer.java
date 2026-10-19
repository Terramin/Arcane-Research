package arcaneresearch.client.renderers.entity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.IGolemProperties;
import arcaneresearch.api.golems.parts.PartModel;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.client.renderers.entity.golem.GolemRenderContext;
import arcaneresearch.client.renderers.entity.golem.GolemRenderHooks;
import arcaneresearch.client.renderers.entity.golem.ObjModel;
import arcaneresearch.common.golems.EntityArcaneresearchGolem;
import java.awt.*;
import java.util.*;
import java.util.List;
@OnlyIn(Dist.CLIENT)
public class GolemObjLayer extends RenderLayer<EntityArcaneresearchGolem, arcaneresearch.client.models.entity.GolemModel> {
    private static final ResourceLocation BASE_MODEL =
            new ResourceLocation(Arcaneresearch.MODID, "models/obj/golem_base.obj");
    private final Map<Integer, EnumMap<PartModel.EnumAttachPoint, List<PartModel>>> partsCache = new HashMap<>();
    private final Map<Integer, Long> partsCacheProps = new HashMap<>();
    private boolean ghost = false;
    private boolean ghostXray = false;
    private float ghostR = 1.0F;
    private float ghostG = 1.0F;
    private float ghostB = 1.0F;
    private float ghostA = 1.0F;
    public GolemObjLayer(RenderLayerParent<EntityArcaneresearchGolem, arcaneresearch.client.models.entity.GolemModel> parent) {
        super(parent);
    }
    public void setGhost(float r, float g, float b, float a, boolean xray) {
        this.ghost = true;
        this.ghostXray = xray;
        this.ghostR = r;
        this.ghostG = g;
        this.ghostB = b;
        this.ghostA = a;
    }
    public void clearGhost() {
        this.ghost = false;
        this.ghostXray = false;
        this.ghostR = 1.0F;
        this.ghostG = 1.0F;
        this.ghostB = 1.0F;
        this.ghostA = 1.0F;
    }
    private VertexConsumer layerBuffer(MultiBufferSource buffer, ResourceLocation tex) {
        if (!ghost) {
            return buffer.getBuffer(TCRenderTypes.objSolid(tex));
        }
        if (ghostXray) {
            return buffer.getBuffer(TCRenderTypes.ghostXray(tex));
        }
        return buffer.getBuffer(TCRenderTypes.golemGhost(tex));
    }
    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       EntityArcaneresearchGolem entity, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        IGolemProperties props = entity.getProperties();
        if (props == null || props.getMaterial() == null) {
            return;
        }
        boolean flash = entity.hurtTime > 0 || entity.deathTime > 0;
        int flashLight = flash ? LightTexture.pack(15, 15) : packedLight;
        float tintR = 1.0F;
        float tintG = flash ? 0.35F : 1.0F;
        float tintB = flash ? 0.35F : 1.0F;
        ResourceLocation matTexture = props.getMaterial().texture;
        boolean holding = !entity.getMainHandItem().isEmpty();
        boolean aflag = props.hasTrait(EnumGolemTrait.WHEELED) || props.hasTrait(EnumGolemTrait.FLYER);
        double dx = entity.getX() - entity.xo;
        double dz = entity.getZ() - entity.zo;
        double speed = dx * dx + dz * dz;
        if (entity.redrawParts || !partsCache.containsKey(entity.getId())
                || !partsCacheProps.containsKey(entity.getId())
                || partsCacheProps.get(entity.getId()) != props.toLong()) {
            entity.redrawParts = false;
            createPartsCache(entity);
        }
        float swingProgress = entity.getAttackAnim(partialTick);
        float bry = 0.0f;
        float rx = (float) Math.toDegrees(Mth.sin(ageInTicks * 0.067f) * 0.03f);
        float rz = (float) Math.toDegrees(Mth.cos(ageInTicks * 0.09f) * 0.05f + 0.05f);
        float rrx = 0.0f;
        float rry = 0.0f;
        float rrz = 0.0f;
        float rlx = 0.0f;
        float rly = 0.0f;
        float rlz = 0.0f;
        float f1;
        if (holding) {
            rrx = 90.0f - rz / 2.0f;
            rrz = -2.0f;
            rlx = 90.0f - rz / 2.0f;
            rlz = 2.0f;
        } else {
            if (aflag) {
                rrx = rx * 2.0f;
                rlx = -rx * 2.0f;
            } else {
                f1 = Mth.cos(limbSwing * 0.6662f + (float) Math.PI) * 2.0f * limbSwingAmount * 0.5f;
                rrx = (float) Math.toDegrees(f1) + rx;
                f1 = Mth.cos(limbSwing * 0.6662f) * 2.0f * limbSwingAmount * 0.5f;
                rlx = (float) Math.toDegrees(f1) - rx;
            }
            rrz += rz + 2.0f;
            rlz -= rz + 2.0f;
        }
        if (swingProgress > 0.0f) {
            float wiggle = -Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI * 2.0f) * 0.2f;
            bry = (float) Math.toDegrees(wiggle);
            rrz = -(float) Math.toDegrees(Mth.sin(wiggle) * 3.0f);
            rrx = (float) Math.toDegrees(-Mth.cos(wiggle) * 5.0f);
            rry += bry;
        }
        poseStack.pushPose();
        GolemRenderHooks.rotateDeg(poseStack, bry, 0.0f, 1.0f, 0.0f);
        float lean = aflag ? 75.0f : 25.0f;
        GolemRenderHooks.rotateDeg(poseStack, (float) (speed * lean), -1.0f, 0.0f, 0.0f);
        GolemRenderHooks.rotateDeg(poseStack,
                (float) (speed * lean * 0.06 * (entity.getYRot() - entity.yRotO)), 0.0f, 0.0f, -1.0f);
        EnumMap<PartModel.EnumAttachPoint, List<PartModel>> parts = partsCache.get(entity.getId());
        ObjModel base = ObjModel.load(BASE_MODEL);
        poseStack.pushPose();
        poseStack.translate(0.0, 0.5, 0.0);
        if (base != null) {
            base.renderGroup(poseStack, layerBuffer(buffer, matTexture), "chest",
                    flashLight, OverlayTexture.NO_OVERLAY,
                    ghostR * tintR, ghostG * tintG, ghostB * tintB, ghostA);
            base.renderGroup(poseStack, layerBuffer(buffer, matTexture), "waist",
                    flashLight, OverlayTexture.NO_OVERLAY,
                    ghostR * tintR, ghostG * tintG, ghostB * tintB, ghostA);
            if (entity.getGolemColor() > 0) {
                float[] diffuse = net.minecraft.world.item.DyeColor.byId(entity.getGolemColor() - 1)
                        .getTextureDiffuseColors();
                Color c = new Color(((int) (diffuse[0] * 255.0f) << 16)
                        | ((int) (diffuse[1] * 255.0f) << 8) | (int) (diffuse[2] * 255.0f));
                base.renderGroup(poseStack, layerBuffer(buffer, matTexture), "flag",
                        flashLight, OverlayTexture.NO_OVERLAY,
                        c.getRed() / 255.0f * ghostR * tintR, c.getGreen() / 255.0f * ghostG * tintG,
                        c.getBlue() / 255.0f * ghostB * tintB, ghostA);
            }
        }
        if (parts != null) {
            for (PartModel part : parts.get(PartModel.EnumAttachPoint.BODY)) {
                renderPart(poseStack, entity, part, matTexture, buffer, flashLight,
                        tintR, tintG, tintB, partialTick,
                        PartModel.EnumLimbSide.MIDDLE);
            }
        }
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0, 0.75, -0.03125);
        GolemRenderHooks.rotateDeg(poseStack, netHeadYaw, 0.0f, -1.0f, 0.0f);
        GolemRenderHooks.rotateDeg(poseStack, headPitch, -1.0f, 0.0f, 0.0f);
        if (parts != null) {
            for (PartModel part : parts.get(PartModel.EnumAttachPoint.HEAD)) {
                renderPart(poseStack, entity, part, matTexture, buffer, flashLight,
                        tintR, tintG, tintB, partialTick,
                        PartModel.EnumLimbSide.MIDDLE);
            }
        }
        if (base != null) {
            base.renderGroup(poseStack, layerBuffer(buffer, matTexture), "head",
                    flashLight, OverlayTexture.NO_OVERLAY,
                    ghostR * tintR, ghostG * tintG, ghostB * tintB, ghostA);
        }
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.20625, 0.6875, 0.0);
        if (parts != null && !parts.get(PartModel.EnumAttachPoint.ARMS).isEmpty()) {
            PartModel part = parts.get(PartModel.EnumAttachPoint.ARMS).get(0);
            rrx = part.preRenderArmRotationX(entity, partialTick, PartModel.EnumLimbSide.RIGHT, rrx);
            rry = part.preRenderArmRotationY(entity, partialTick, PartModel.EnumLimbSide.RIGHT, rry);
            rrz = part.preRenderArmRotationZ(entity, partialTick, PartModel.EnumLimbSide.RIGHT, rrz);
        }
        GolemRenderHooks.rotateDeg(poseStack, rrx, 1.0f, 0.0f, 0.0f);
        GolemRenderHooks.rotateDeg(poseStack, rry, 0.0f, 1.0f, 0.0f);
        GolemRenderHooks.rotateDeg(poseStack, rrz, 0.0f, 0.0f, 1.0f);
        if (base != null) {
            base.renderGroup(poseStack, layerBuffer(buffer, matTexture), "arm",
                    flashLight, OverlayTexture.NO_OVERLAY,
                    ghostR * tintR, ghostG * tintG, ghostB * tintB, ghostA);
        }
        if (parts != null) {
            for (PartModel part : parts.get(PartModel.EnumAttachPoint.ARMS)) {
                renderPart(poseStack, entity, part, matTexture, buffer, flashLight,
                        tintR, tintG, tintB, partialTick,
                        PartModel.EnumLimbSide.RIGHT);
            }
        }
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(-0.20625, 0.6875, 0.0);
        if (parts != null && !parts.get(PartModel.EnumAttachPoint.ARMS).isEmpty()) {
            PartModel part = parts.get(PartModel.EnumAttachPoint.ARMS).get(0);
            rlx = part.preRenderArmRotationX(entity, partialTick, PartModel.EnumLimbSide.LEFT, rlx);
            rly = part.preRenderArmRotationY(entity, partialTick, PartModel.EnumLimbSide.LEFT, rly);
            rlz = part.preRenderArmRotationZ(entity, partialTick, PartModel.EnumLimbSide.LEFT, rlz);
        }
        GolemRenderHooks.rotateDeg(poseStack, rlx, 1.0f, 0.0f, 0.0f);
        GolemRenderHooks.rotateDeg(poseStack, rly + 180.0f, 0.0f, 1.0f, 0.0f);
        GolemRenderHooks.rotateDeg(poseStack, rlz, 0.0f, 0.0f, -1.0f);
        if (base != null) {
            base.renderGroup(poseStack, layerBuffer(buffer, matTexture), "arm",
                    flashLight, OverlayTexture.NO_OVERLAY,
                    ghostR * tintR, ghostG * tintG, ghostB * tintB, ghostA);
        }
        if (parts != null) {
            for (PartModel part : parts.get(PartModel.EnumAttachPoint.ARMS)) {
                renderPart(poseStack, entity, part, matTexture, buffer, flashLight,
                        tintR, tintG, tintB, partialTick,
                        PartModel.EnumLimbSide.LEFT);
            }
        }
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.09375, 0.375, 0.0);
        f1 = Mth.cos(limbSwing * 0.6662f) * limbSwingAmount;
        GolemRenderHooks.rotateDeg(poseStack, (float) Math.toDegrees(f1), 1.0f, 0.0f, 0.0f);
        if (parts != null) {
            for (PartModel part : parts.get(PartModel.EnumAttachPoint.LEGS)) {
                renderPart(poseStack, entity, part, matTexture, buffer, flashLight,
                        tintR, tintG, tintB, partialTick,
                        PartModel.EnumLimbSide.RIGHT);
            }
        }
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(-0.09375, 0.375, 0.0);
        f1 = Mth.cos(limbSwing * 0.6662f + (float) Math.PI) * limbSwingAmount;
        GolemRenderHooks.rotateDeg(poseStack, (float) Math.toDegrees(f1), 1.0f, 0.0f, 0.0f);
        if (parts != null) {
            for (PartModel part : parts.get(PartModel.EnumAttachPoint.LEGS)) {
                renderPart(poseStack, entity, part, matTexture, buffer, flashLight,
                        tintR, tintG, tintB, partialTick,
                        PartModel.EnumLimbSide.LEFT);
            }
        }
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0, 0.625, 0.0);
        GolemRenderHooks.rotateDeg(poseStack, 90.0f - rz * 0.5f, 1.0f, 0.0f, 0.0f);
        ItemStack held = entity.getMainHandItem();
        if (held != null && !held.isEmpty()) {
            GolemRenderHooks.renderHeldItem(entity, poseStack, buffer, flashLight,
                    OverlayTexture.NO_OVERLAY, held);
        }
        poseStack.popPose();
        poseStack.popPose();
    }
    private void renderPart(PoseStack poseStack, EntityArcaneresearchGolem golem, PartModel part,
                            ResourceLocation matTexture, MultiBufferSource buffer, int packedLight,
                            float tintR, float tintG, float tintB,
                            float partialTick, PartModel.EnumLimbSide side) {
        ObjModel model = ObjModel.load(part.getObjModel());
        if (model == null) {
            return;
        }
        IGolemAPI api = golem;
        for (String op : model.getGroupNames()) {
            poseStack.pushPose();
            ResourceLocation tex = part.useMaterialTextureForObjectPart(op) ? matTexture : part.getTexture();
            if (tex == null) {
                tex = matTexture;
            }
            GolemRenderContext.set(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
            part.preRenderObjectPart(op, api, partialTick, side);
            model.renderGroup(poseStack, layerBuffer(buffer, tex), op,
                    packedLight, OverlayTexture.NO_OVERLAY,
                    ghostR * tintR, ghostG * tintG, ghostB * tintB, ghostA);
            part.postRenderObjectPart(op, api, partialTick, side);
            GolemRenderContext.clear();
            poseStack.popPose();
        }
    }
    private void createPartsCache(EntityArcaneresearchGolem golem) {
        EnumMap<PartModel.EnumAttachPoint, List<PartModel>> pl = new EnumMap<>(PartModel.EnumAttachPoint.class);
        pl.put(PartModel.EnumAttachPoint.BODY, new ArrayList<>());
        pl.put(PartModel.EnumAttachPoint.HEAD, new ArrayList<>());
        pl.put(PartModel.EnumAttachPoint.ARMS, new ArrayList<>());
        pl.put(PartModel.EnumAttachPoint.LEGS, new ArrayList<>());
        IGolemProperties props = golem.getProperties();
        if (props.getHead() != null && props.getHead().model != null) {
            pl.get(props.getHead().model.getAttachPoint()).add(props.getHead().model);
        }
        if (props.getArms() != null && props.getArms().model != null) {
            pl.get(props.getArms().model.getAttachPoint()).add(props.getArms().model);
        }
        if (props.getLegs() != null && props.getLegs().model != null) {
            pl.get(props.getLegs().model.getAttachPoint()).add(props.getLegs().model);
        }
        if (props.getAddon() != null && props.getAddon().model != null) {
            pl.get(props.getAddon().model.getAttachPoint()).add(props.getAddon().model);
        }
        if (partsCache.size() > 256) {
            partsCache.clear();
            partsCacheProps.clear();
        }
        partsCache.put(golem.getId(), pl);
        partsCacheProps.put(golem.getId(), props.toLong());
    }
}
