package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier; 
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.IGolemProperties;
import arcaneresearch.api.golems.parts.PartModel;
import arcaneresearch.client.models.entity.GolemModel;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.client.renderers.entity.golem.GolemRenderContext;
import arcaneresearch.client.renderers.entity.golem.GolemRenderHooks;
import arcaneresearch.client.renderers.entity.golem.ObjModel;
import arcaneresearch.common.golems.EntityArcaneresearchGolem;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;










public class GolemObjLayer extends RenderLayer<GolemRenderer.State, GolemModel> {

    private static final Identifier BASE_MODEL =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "models/obj/golem_base.obj");

    private final Map<Integer, EnumMap<PartModel.EnumAttachPoint, List<PartModel>>> partsCache = new HashMap<>();
    private final Map<Integer, Long> partsCacheProps = new HashMap<>();

    public GolemObjLayer(RenderLayerParent<GolemRenderer.State, GolemModel> parent) {
        super(parent);
    }

    @FunctionalInterface
    private interface GroupDraw {
        void draw(PoseStack poseStack, VertexConsumer consumer);
    }

    private static void emitGroup(PoseStack poseStack, SubmitNodeCollector collector,
                                  RenderType type, GroupDraw draw) {
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            PoseStack work = new PoseStack();
            work.last().pose().set(pose.pose());
            work.last().normal().set(pose.normal());
            draw.draw(work, consumer);
        });
    }

    private static RenderType typeFor(GolemRenderer.State state, Identifier tex, boolean xray) {
        if (xray) {
            return TCRenderTypes.ghostXray(tex);
        }
        if (state.ghostOn) {
            return TCRenderTypes.golemGhost(tex);
        }
        return TCRenderTypes.objSolid(tex);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                       GolemRenderer.State state, float yRot, float xRot) {
        if (state.golem == null || state.matTexture == null) {
            return;
        }
        fillCache(state);
        
        
        
        poseStack.pushPose();
        poseStack.translate(0.0F, 1.501F, 0.0F);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        if (!state.isInvisible) {
            emitBody(poseStack, collector, packedLight, state, 1.0F, 1.0F, 1.0F, 1.0F, false);
        } else if (state.ghostOn) {
            emitBody(poseStack, collector, packedLight, state, 1.0F, 1.0F, 1.0F, 0.15F, false);
        }
        if (state.sealXray) {
            emitBody(poseStack, collector, packedLight, state, 0.25F, 0.25F, 0.25F, 0.25F, true);
        }
        poseStack.popPose();
    }

    private void emitBody(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                          GolemRenderer.State state, float ghostR, float ghostG, float ghostB,
                          float ghostA, boolean xray) {
        EntityArcaneresearchGolem entity = state.golem;
        IGolemProperties props = entity.getProperties();
        if (props == null || props.getMaterial() == null) {
            return;
        }
        float partialTick = state.partialTicks;
        float ageInTicks = state.ageInTicks;
        float limbSwing = state.walkAnimationPos;
        float limbSwingAmount = state.walkAnimationSpeed;
        float netHeadYaw = state.yRot;
        float headPitch = state.xRot;

        int flashLight = state.flash ? 0xF000F0 : packedLight;
        float tintR = 1.0F;
        float tintG = state.flash ? 0.35F : 1.0F;
        float tintB = state.flash ? 0.35F : 1.0F;
        Identifier matTexture = state.matTexture;

        boolean holding = state.holding;
        boolean aflag = state.flyerLike;
        double speed = state.moveSpeedSq;

        float swingProgress = state.swing;
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

        GolemRenderContext.set(state, poseStack, collector, flashLight, OverlayTexture.NO_OVERLAY);
        try {
            EnumMap<PartModel.EnumAttachPoint, List<PartModel>> parts = partsCache.get(entity.getId());
            ObjModel base = ObjModel.load(BASE_MODEL);

            poseStack.pushPose();
            GolemRenderHooks.rotateDeg(poseStack, bry, 0.0f, 1.0f, 0.0f);
            float lean = aflag ? 75.0f : 25.0f;
            GolemRenderHooks.rotateDeg(poseStack, (float) (speed * lean), -1.0f, 0.0f, 0.0f);
            GolemRenderHooks.rotateDeg(poseStack,
                    (float) (speed * lean * 0.06 * state.turnDelta), 0.0f, 0.0f, -1.0f);

            poseStack.pushPose();
            poseStack.translate(0.0, 0.5, 0.0);
            if (base != null) {
                emitGroup(poseStack, collector, typeFor(state, matTexture, xray), (work, consumer) ->
                        base.renderGroup(work, consumer, "chest", flashLight, OverlayTexture.NO_OVERLAY,
                                ghostR * tintR, ghostG * tintG, ghostB * tintB, ghostA));
                emitGroup(poseStack, collector, typeFor(state, matTexture, xray), (work, consumer) ->
                        base.renderGroup(work, consumer, "waist", flashLight, OverlayTexture.NO_OVERLAY,
                                ghostR * tintR, ghostG * tintG, ghostB * tintB, ghostA));
                if (entity.getGolemColor() > 0) {
                    
                    int diffuse = net.minecraft.world.item.DyeColor.byId(entity.getGolemColor() - 1)
                            .getTextureDiffuseColor();
                    float fr = ((diffuse >> 16) & 0xFF) / 255.0F * ghostR * tintR;
                    float fg = ((diffuse >> 8) & 0xFF) / 255.0F * ghostG * tintG;
                    float fb = (diffuse & 0xFF) / 255.0F * ghostB * tintB;
                    emitGroup(poseStack, collector, typeFor(state, matTexture, xray), (work, consumer) ->
                            base.renderGroup(work, consumer, "flag", flashLight, OverlayTexture.NO_OVERLAY,
                                    fr, fg, fb, ghostA));
                }
            }
            if (parts != null) {
                for (PartModel part : parts.get(PartModel.EnumAttachPoint.BODY)) {
                    renderPart(poseStack, collector, state, part, matTexture, flashLight,
                            tintR, tintG, tintB, ghostR, ghostG, ghostB, ghostA, xray,
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
                    renderPart(poseStack, collector, state, part, matTexture, flashLight,
                            tintR, tintG, tintB, ghostR, ghostG, ghostB, ghostA, xray,
                            PartModel.EnumLimbSide.MIDDLE);
                }
            }
            if (base != null) {
                emitGroup(poseStack, collector, typeFor(state, matTexture, xray), (work, consumer) ->
                        base.renderGroup(work, consumer, "head", flashLight, OverlayTexture.NO_OVERLAY,
                                ghostR * tintR, ghostG * tintG, ghostB * tintB, ghostA));
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
                emitGroup(poseStack, collector, typeFor(state, matTexture, xray), (work, consumer) ->
                        base.renderGroup(work, consumer, "arm", flashLight, OverlayTexture.NO_OVERLAY,
                                ghostR * tintR, ghostG * tintG, ghostB * tintB, ghostA));
            }
            if (parts != null) {
                for (PartModel part : parts.get(PartModel.EnumAttachPoint.ARMS)) {
                    renderPart(poseStack, collector, state, part, matTexture, flashLight,
                            tintR, tintG, tintB, ghostR, ghostG, ghostB, ghostA, xray,
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
                emitGroup(poseStack, collector, typeFor(state, matTexture, xray), (work, consumer) ->
                        base.renderGroup(work, consumer, "arm", flashLight, OverlayTexture.NO_OVERLAY,
                                ghostR * tintR, ghostG * tintG, ghostB * tintB, ghostA));
            }
            if (parts != null) {
                for (PartModel part : parts.get(PartModel.EnumAttachPoint.ARMS)) {
                    renderPart(poseStack, collector, state, part, matTexture, flashLight,
                            tintR, tintG, tintB, ghostR, ghostG, ghostB, ghostA, xray,
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
                    renderPart(poseStack, collector, state, part, matTexture, flashLight,
                            tintR, tintG, tintB, ghostR, ghostG, ghostB, ghostA, xray,
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
                    renderPart(poseStack, collector, state, part, matTexture, flashLight,
                            tintR, tintG, tintB, ghostR, ghostG, ghostB, ghostA, xray,
                            PartModel.EnumLimbSide.LEFT);
                }
            }
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(0.0, 0.625, 0.0);
            GolemRenderHooks.rotateDeg(poseStack, 90.0f - rz * 0.5f, 1.0f, 0.0f, 0.0f);
            GolemRenderHooks.renderHeldItem(state.mainHandState, state.mainHandIsBlock,
                    poseStack, collector, flashLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();

            poseStack.popPose();
        } finally {
            GolemRenderContext.clear();
        }
    }

    private void renderPart(PoseStack poseStack, SubmitNodeCollector collector, GolemRenderer.State state,
                            PartModel part, Identifier matTexture, int packedLight,
                            float tintR, float tintG, float tintB,
                            float ghostR, float ghostG, float ghostB, float ghostA, boolean xray,
                            PartModel.EnumLimbSide side) {
        ObjModel model = ObjModel.load(part.getObjModel());
        if (model == null) {
            return;
        }
        IGolemAPI api = state.golem;
        float partialTick = state.partialTicks;
        for (String op : model.getGroupNames()) {
            poseStack.pushPose();
            Identifier tex = part.useMaterialTextureForObjectPart(op) ? matTexture : part.getTexture();
            if (tex == null) {
                tex = matTexture;
            }
            GolemRenderContext.set(state, poseStack, collector, packedLight, OverlayTexture.NO_OVERLAY);
            part.preRenderObjectPart(op, api, partialTick, side);
            RenderType type = typeFor(state, tex, xray);
            emitGroup(poseStack, collector, type, (work, consumer) ->
                    model.renderGroup(work, consumer, op, packedLight, OverlayTexture.NO_OVERLAY,
                            ghostR * tintR, ghostG * tintG, ghostB * tintB, ghostA));
            part.postRenderObjectPart(op, api, partialTick, side);
            GolemRenderContext.clear();
            poseStack.popPose();
        }
    }

    private void fillCache(GolemRenderer.State state) {
        EntityArcaneresearchGolem golem = state.golem;
        IGolemProperties props = golem.getProperties();
        Long known = partsCacheProps.get(golem.getId());
        if (!golem.redrawParts && partsCache.containsKey(golem.getId())
                && known != null && known == props.toLong()) {
            return;
        }
        golem.redrawParts = false;
        EnumMap<PartModel.EnumAttachPoint, List<PartModel>> pl = new EnumMap<>(PartModel.EnumAttachPoint.class);
        pl.put(PartModel.EnumAttachPoint.BODY, new ArrayList<>());
        pl.put(PartModel.EnumAttachPoint.HEAD, new ArrayList<>());
        pl.put(PartModel.EnumAttachPoint.ARMS, new ArrayList<>());
        pl.put(PartModel.EnumAttachPoint.LEGS, new ArrayList<>());
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