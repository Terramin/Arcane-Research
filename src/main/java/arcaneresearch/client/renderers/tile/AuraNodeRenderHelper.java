package arcaneresearch.client.renderers.tile;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.items.IRevealer;
import arcaneresearch.client.lib.events.WandTipHelper;
import arcaneresearch.common.items.tools.ItemThaumometer;
import arcaneresearch.common.lib.aura.INode;
import arcaneresearch.common.lib.aura.NodeModifier;
import arcaneresearch.common.lib.aura.NodeType;
@OnlyIn(Dist.CLIENT)
public class AuraNodeRenderHelper extends RenderStateShard {
    private static final ResourceLocation NODETEX =
            new ResourceLocation(Arcaneresearch.MODID, "textures/misc/nodes.png");
    private static final ResourceLocation WISPYTEX =
            new ResourceLocation(Arcaneresearch.MODID, "textures/misc/wispy.png");
    private static final int FRAMES = 32;
    private static final int FULLBRIGHT = 0xF000F0;
    private static final RenderType NODE_ADD = createNodeLayer(true, false);
    private static final RenderType NODE_ADD_XRAY = createNodeLayer(true, true);
    private static final RenderType NODE_ALPHA = createNodeLayer(false, false);
    private static final RenderType NODE_ALPHA_XRAY = createNodeLayer(false, true);
    private static final RenderType FLOATY = createFloatyLayer();
    private AuraNodeRenderHelper() {
        super("aura_node", () -> {
        }, () -> {
        });
    }
    private static RenderType floatyLayer() {
        return FLOATY;
    }
    private static RenderType createFloatyLayer() {
        return RenderType.create("aura_floaty", DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.TRIANGLE_STRIP, 256, true, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(WISPYTEX, false, false))
                        .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setOverlayState(RenderStateShard.OVERLAY)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .createCompositeState(true));
    }
    private static RenderType nodeLayer(boolean additive, boolean depthIgnore) {
        if (additive) {
            return depthIgnore ? NODE_ADD_XRAY : NODE_ADD;
        }
        return depthIgnore ? NODE_ALPHA_XRAY : NODE_ALPHA;
    }
    private static RenderType createNodeLayer(boolean additive, boolean depthIgnore) {
        String name = "aura_node_" + (additive ? "add" : "alpha") + (depthIgnore ? "_xray" : "");
        return RenderType.create(name, DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS, 256, true, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(NODETEX, false, false))
                        .setTransparencyState(additive
                                ? RenderStateShard.LIGHTNING_TRANSPARENCY
                                : RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setOverlayState(RenderStateShard.OVERLAY)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .setDepthTestState(depthIgnore
                                ? RenderStateShard.NO_DEPTH_TEST
                                : RenderStateShard.LEQUAL_DEPTH_TEST)
                        .createCompositeState(true));
    }
    public static Visibility resolve(Player player, boolean inJar) {
        if (inJar) {
            return new Visibility(true, false, 64.0D);
        }
        if (player != null) {
            try {
                ItemStack head = player.getInventory().getArmor(3);
                if (!head.isEmpty() && head.getItem() instanceof IRevealer
                        && ((IRevealer) head.getItem()).showNodes(head, player)) {
                    return new Visibility(true, true, 64.0D);
                }
                for (ItemStack curio : arcaneresearch.common.lib.compat.CuriosCompat.findHeadSlotItems(player,
                        s -> !s.isEmpty() && s.getItem() instanceof IRevealer)) {
                    if (((IRevealer) curio.getItem()).showNodes(curio, player)) {
                        return new Visibility(true, true, 64.0D);
                    }
                }
                if (player.getMainHandItem().getItem() instanceof ItemThaumometer
                        || player.getOffhandItem().getItem() instanceof ItemThaumometer) {
                    return new Visibility(true, true, 48.0D);
                }
            } catch (Exception ignored) {
            }
        }
        return new Visibility(false, false, 64.0D);
    }
    public static void renderNode(PoseStack poseStack, MultiBufferSource buffer, INode node,
                                  BlockPos pos, LivingEntity viewer, Vector3f center, float size,
                                  Visibility vis, float partialTicks) {
        if (node == null) {
            return;
        }
        renderNode(poseStack, buffer, node.getAspects(), node.getNodeType(),
                node.getNodeModifier(), pos, viewer, center, size, vis, partialTicks);
    }
    public static void renderNode(PoseStack poseStack, MultiBufferSource buffer, AspectList aspects,
                                  NodeType type, NodeModifier mod, BlockPos pos, LivingEntity viewer, Vector3f center,
                                  float size, Visibility vis, float partialTicks) {
        if (aspects == null) {
            return;
        }
        long nano = System.nanoTime();
        int frame = (int) ((nano / 40000000L + pos.getX()) % FRAMES);
        int ticks = viewer != null ? viewer.tickCount : 0;
        if (aspects.size() > 0 && vis.visible) {
            double cx = pos.getX() + 0.5D;
            double cy = pos.getY() + 0.5D;
            double cz = pos.getZ() + 0.5D;
            double distance = viewer != null
                    ? Math.sqrt(viewer.distanceToSqr(cx, cy, cz)) : 0.0D;
            if (distance > vis.viewDistance) {
                return;
            }
            float alpha = (float) ((vis.viewDistance - distance) / vis.viewDistance);
            alpha = Math.min(1.0F, alpha * 1.15F);
            if (mod != null) {
                switch (mod) {
                    case BRIGHT:
                        alpha *= 1.5F;
                        break;
                    case FADING:
                        alpha *= 0.66F;
                        break;
                    case PALE:
                        alpha *= (float) Math.sin(ticks / 3.0F) * 0.25F + 0.33F;
                        break;
                    default:
                        break;
                }
            }
            long time = nano / 5000000L;
            float count = 0.0F;
            float average = 0.0F;
            for (Aspect aspect : aspects.getAspects()) {
                if (aspect == null) {
                    continue;
                }
                float a = alpha;
                if (aspect.getBlend() == 771) {
                    a *= 1.5F;
                }
                average += aspects.getAmount(aspect);
                float scale = (float) Math.sin(ticks / (14.0F - count)) * 0.25F + 0.5F;
                scale = 0.2F + scale * aspects.getAmount(aspect) / 50.0F;
                scale *= size;
                float angle = (float) (time % (5000 + 500 * count))
                        / (5000.0F + 500.0F * count) * ((float) Math.PI * 2.0F);
                renderStrip(poseStack, buffer.getBuffer(nodeLayer(aspect.getBlend() != 771,
                                vis.depthIgnore)), center, angle, scale,
                        a / Math.max(1.0F, aspects.size() / 2.0F), 0, frame, aspect.getColor());
                count++;
            }
            average /= Math.max(1, aspects.size());
            float scale = 0.1F + average / 150.0F;
            scale *= size;
            boolean additive = true;
            int strip = 1;
            float angle = (float) (time % 5000L) / 5000.0F * ((float) Math.PI * 2.0F);
            if (type != null) {
                switch (type) {
                    case NORMAL:
                        strip = 1;
                        break;
                    case UNSTABLE:
                        strip = 6;
                        angle = 0.0F;
                        break;
                    case DARK:
                        additive = false;
                        strip = 2;
                        break;
                    case TAINTED:
                        additive = false;
                        strip = 5;
                        break;
                    case PURE:
                        strip = 4;
                        break;
                    case HUNGRY:
                        scale *= 0.75F;
                        strip = 3;
                        break;
                    default:
                        break;
                }
            }
            renderStrip(poseStack, buffer.getBuffer(nodeLayer(additive, vis.depthIgnore)),
                    center, angle, scale, alpha, strip, frame, 0xFFFFFF);
        } else {
            renderStrip(poseStack, buffer.getBuffer(nodeLayer(true, false)),
                    center, 0.0F, 0.5F * size, 0.1F, 1, frame, 0xFFFFFF);
        }
    }
    public static void drawFloatyLine(PoseStack poseStack, MultiBufferSource buffer,
                                      Vector3f from, Vector3f to, int color, float speed, float distance, float width) {
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        float dx0 = from.x() - to.x();
        float dy0 = from.y() - to.y();
        float dz0 = from.z() - to.z();
        float dist = (float) Math.sqrt(dx0 * dx0 + dy0 * dy0 + dz0 * dz0);
        if (dist < 0.001F) {
            return;
        }
        float quality = 16.0F;
        float length = Math.max(1.0F, Math.round(dist) * quality / 2.0F);
        float time = System.nanoTime() / 30000000L;
        int steps = (int) (length * distance);
        VertexConsumer builder = buffer.getBuffer(floatyLayer());
        float[] last = new float[3];
        for (int pass = 0; pass < 2; pass++) {
            for (int i = 0; i <= steps; i++) {
                float f2 = i / length;
                float envelope = 1.0F - Math.abs(i - length / 2.0F) / length / 2.0F;
                float wobble = envelope * 0.5F;
                float dx = dx0 + (float) Math.sin((to.z() % 16.0F
                        + dist * (1.0F - f2) * quality / 2.0F - time % 32767.0F / 5.0F) / 4.0D) * wobble;
                float dy = dy0 + (float) Math.sin((to.x() % 16.0F
                        + dist * (1.0F - f2) * quality / 2.0F - time % 32767.0F / 5.0F) / 3.0D) * wobble;
                float dz = dz0 + (float) Math.sin((to.y() % 16.0F
                        + dist * (1.0F - f2) * quality / 2.0F - time % 32767.0F / 5.0F) / 2.0D) * wobble;
                float u = (1.0F - f2) * dist - time * speed;
                float px = to.x() + dx * f2;
                float py = to.y() + dy * f2;
                float pz = to.z() + dz * f2;
                if (pass == 0) {
                    stripVertex(poseStack, builder, px, py - width, pz, u, 1.0F, r, g, b, envelope);
                    stripVertex(poseStack, builder, px, py + width, pz, u, 0.0F, r, g, b, envelope);
                    last[0] = px;
                    last[1] = py + width;
                    last[2] = pz;
                } else {
                    if (i == 0) {
                        stripVertex(poseStack, builder, last[0], last[1], last[2], u, 0.0F,
                                r, g, b, 0.0F);
                        stripVertex(poseStack, builder, px - width, py, pz, u, 1.0F,
                                r, g, b, 0.0F);
                    }
                    stripVertex(poseStack, builder, px - width, py, pz, u, 1.0F, r, g, b, envelope);
                    stripVertex(poseStack, builder, px + width, py, pz, u, 0.0F, r, g, b, envelope);
                }
            }
        }
    }
    public static void drawDrainBeam(PoseStack poseStack, MultiBufferSource buffer,
                                     BlockPos nodePos, Vector3f center, net.minecraft.world.entity.Entity target,
                                     Aspect aspect, long gameTime, long lastTapGameTime, float partialTicks) {
        if (target == null || !target.isAlive() || gameTime - lastTapGameTime > 10L) {
            return;
        }
        try {
            Vec3 tip = wandTip(target, partialTicks);
            Vector3f hand = new Vector3f(
                    (float) (tip.x - nodePos.getX()),
                    (float) (tip.y - nodePos.getY()),
                    (float) (tip.z - nodePos.getZ()));
            int color = aspect != null ? aspect.getColor() : 0xFFFFFF;
            drawFloatyLine(poseStack, buffer, center, hand, color, -0.02F, 1.0F, 0.15F);
        } catch (Exception ignored) {
        }
    }
    public static Vec3 wandTip(net.minecraft.world.entity.Entity target, float partialTicks) {
        double px;
        double py;
        double pz;
        try {
            px = net.minecraft.util.Mth.lerp(partialTicks, target.xo, target.getX());
            py = net.minecraft.util.Mth.lerp(partialTicks, target.yo, target.getY());
            pz = net.minecraft.util.Mth.lerp(partialTicks, target.zo, target.getZ());
        } catch (Exception e) {
            return new Vec3(target.getX(), target.getY() + target.getBbHeight() * 0.8D, target.getZ());
        }
        if (!(target instanceof Player player)) {
            return new Vec3(px, py + target.getBbHeight() * 0.8D, pz);
        }
        boolean first = false;
        try {
            first = Minecraft.getInstance().options.getCameraType().isFirstPerson()
                    && Minecraft.getInstance().cameraEntity == player;
        } catch (Exception ignored) {
        }
        float charge = WandTipHelper.charge(player);
        if (!first) {
            Vec3 look;
            try {
                look = player.getViewVector(partialTicks);
            } catch (Exception e) {
                look = player.getLookAngle();
            }
            Vec3 right3 = new Vec3(-look.z, 0.0D, look.x);
            if (right3.lengthSqr() < 1.0E-6D) {
                right3 = new Vec3(1.0D, 0.0D, 0.0D);
            } else {
                right3 = right3.normalize();
            }
            double side = (WandTipHelper.isRightHand(player) ? 0.35D : -0.35D) * (1.0D - 0.4D * charge);
            Vec3 base = new Vec3(px, py + player.getEyeHeight() * 0.8D, pz);
            return base.add(look.scale(0.7D + 0.3D * charge))
                    .add(right3.scale(side))
                    .add(0.0D, 0.15D * charge, 0.0D);
        }
        return WandTipHelper.tipWorld(player, partialTicks);
    }
    private static void stripVertex(PoseStack poseStack, VertexConsumer builder,
                                    float x, float y, float z, float u, float v,
                                    float r, float g, float b, float a) {
        builder.vertex(poseStack.last().pose(), x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(0, 10)
                .uv2(FULLBRIGHT)
                .normal(poseStack.last().normal(), 0.0F, 1.0F, 0.0F)
                .endVertex();
    }
    private static void renderStrip(PoseStack poseStack, VertexConsumer builder, Vector3f center,
                                    float angle, float scale, float alpha, int strip, int frame, int color) {
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        float u0 = frame / (float) FRAMES;
        float u1 = (frame + 1) / (float) FRAMES;
        float v0 = strip / (float) FRAMES;
        float v1 = (strip + 1.0F) / (float) FRAMES;
        Quaternionf cam = new Quaternionf();
        try {
            cam.set(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        } catch (Exception ignored) {
            cam.identity();
        }
        float[][] corners = {{-scale, -scale}, {-scale, scale}, {scale, scale}, {scale, -scale}};
        float[][] uvs = {{u1, v1}, {u1, v0}, {u0, v0}, {u0, v1}};
        Vector3f normal = new Vector3f(0.0F, 0.0F, 1.0F).rotate(cam);
        for (int i = 0; i < 4; i++) {
            Vector3f v = new Vector3f(corners[i][0], corners[i][1], 0.0F);
            v.rotateZ(angle);
            v.rotate(cam);
            v.add(center);
            builder.vertex(poseStack.last().pose(), v.x(), v.y(), v.z())
                    .color(r, g, b, alpha)
                    .uv(uvs[i][0], uvs[i][1])
                    .overlayCoords(0, 10)
                    .uv2(FULLBRIGHT)
                    .normal(poseStack.last().normal(), normal.x(), normal.y(), normal.z())
                    .endVertex();
        }
    }
    public static final class Visibility {
        public final boolean visible;
        public final boolean depthIgnore;
        public final double viewDistance;
        public Visibility(boolean visible, boolean depthIgnore, double viewDistance) {
            this.visible = visible;
            this.depthIgnore = depthIgnore;
            this.viewDistance = viewDistance;
        }
    }
}
