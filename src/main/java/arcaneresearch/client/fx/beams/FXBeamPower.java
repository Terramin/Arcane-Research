package arcaneresearch.client.fx.beams;

import arcaneresearch.Arcaneresearch;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

/**
 * Thaumcraft 4's vis-channel beam. Two crossed additive ribbons with a
 * scrolling texture and a flare at the target end. Kept alive by the relay
 * tile each tick; the pulse momentarily brightens it.
 */
@OnlyIn(Dist.CLIENT)
public class FXBeamPower extends TextureSheetParticle {
    private static final ResourceLocation BEAM = ResourceLocation.fromNamespaceAndPath(
            Arcaneresearch.MODID, "textures/misc/beam1.png");

    private double targetX, targetY, targetZ;
    private double prevTargetX, prevTargetY, prevTargetZ;
    private float length;
    private float rotYaw, rotPitch, prevYaw, prevPitch;
    private float opacity = 0.3F;

    public FXBeamPower(ClientLevel level, double sx, double sy, double sz,
                       double tx, double ty, double tz, float r, float g, float b, int duration) {
        super(level, sx, sy, sz, 0, 0, 0);
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
        this.setSize(0.02F, 0.02F);
        this.xd = this.yd = this.zd = 0;
        this.targetX = tx;
        this.targetY = ty;
        this.targetZ = tz;
        this.prevTargetX = tx;
        this.prevTargetY = ty;
        this.prevTargetZ = tz;
        this.calculateGeometry();
        this.prevYaw = rotYaw;
        this.prevPitch = rotPitch;
        this.lifetime = duration;
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        Minecraft mc = Minecraft.getInstance();
        if (mc.cameraEntity != null) {
            int visible = mc.options.graphicsMode().get().getId() > 0 ? 50 : 25;
            if (mc.cameraEntity.distanceToSqr(sx, sy, sz) > visible * visible) {
                this.lifetime = 0;
            }
        }
    }

    public void updateBeam(double sx, double sy, double sz, double tx, double ty, double tz) {
        this.x = sx;
        this.y = sy;
        this.z = sz;
        this.targetX = tx;
        this.targetY = ty;
        this.targetZ = tz;
        while (this.lifetime - this.age < 4) {
            this.lifetime++;
        }
    }

    public void setPulse(boolean pulse, float r, float g, float b) {
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
        if (pulse) {
            this.opacity = 0.8F;
        }
    }

    private void calculateGeometry() {
        float dx = (float) (this.x - targetX);
        float dy = (float) (this.y - targetY);
        float dz = (float) (this.z - targetZ);
        this.length = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        float horizontal = Mth.sqrt(dx * dx + dz * dz);
        this.rotYaw = (float) (Math.atan2(dx, dz) * 180.0 / Math.PI);
        this.rotPitch = (float) (Math.atan2(dy, horizontal) * 180.0 / Math.PI);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.prevTargetX = targetX;
        this.prevTargetY = targetY;
        this.prevTargetZ = targetZ;
        this.prevYaw = rotYaw;
        this.prevPitch = rotPitch;
        this.calculateGeometry();

        if (this.opacity > 0.3F) this.opacity -= 0.025F;
        if (this.opacity < 0.3F) this.opacity = 0.3F;

        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    @Override
    public void render(@NotNull VertexConsumer ignored, @NotNull Camera camera, float partialTicks) {
        if (this.length <= 0.0F) return;
        Vec3 camPos = camera.getPosition();
        float sx = (float) (Mth.lerp(partialTicks, this.xo, this.x) - camPos.x);
        float sy = (float) (Mth.lerp(partialTicks, this.yo, this.y) - camPos.y);
        float sz = (float) (Mth.lerp(partialTicks, this.zo, this.z) - camPos.z);
        float tx = (float) (Mth.lerp(partialTicks, prevTargetX, targetX) - camPos.x);
        float ty = (float) (Mth.lerp(partialTicks, prevTargetY, targetY) - camPos.y);
        float tz = (float) (Mth.lerp(partialTicks, prevTargetZ, targetZ) - camPos.z);

        float dx = sx - tx;
        float dy = sy - ty;
        float dz = sz - tz;
        float beamLen = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        float yaw = Mth.lerp(partialTicks, prevYaw, rotYaw);
        float pitch = Mth.lerp(partialTicks, prevPitch, rotPitch);

        Minecraft mc = Minecraft.getInstance();
        float slide = mc.player != null ? mc.player.tickCount + partialTicks : 0.0F;
        float uvOffset = -slide * 0.2F - Mth.floor(-slide * 0.1F);
        float opMod = hasRevealer() ? 1.0F : 0.1F;

        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, BEAM);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(770, 1);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        PoseStack pose = new PoseStack();
        pose.pushPose();
        pose.translate(sx, sy, sz);
        pose.mulPose(Axis.XP.rotationDegrees(90.0F));
        pose.mulPose(Axis.ZN.rotationDegrees(180.0F + yaw));
        pose.mulPose(Axis.XP.rotationDegrees(pitch));

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        float halfWidth = 0.15F * 0.7F;
        for (int t = 0; t < 2; t++) {
            float v0 = uvOffset + t / 3.0F;
            float v1 = beamLen + v0;
            Matrix4f m = pose.last().pose();
            emit(builder, m, -halfWidth, beamLen, 1.0F, v1, opacity * opMod);
            emit(builder, m, -halfWidth, 0.0F, 1.0F, v0, opacity * opMod);
            emit(builder, m, halfWidth, 0.0F, 0.0F, v0, opacity * opMod);
            emit(builder, m, halfWidth, beamLen, 0.0F, v1, opacity * opMod);
            pose.mulPose(Axis.YP.rotationDegrees(90.0F));
        }
        tesselator.end();
        pose.popPose();

        RenderSystem.depthMask(true);
        RenderSystem.blendFunc(770, 771);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

    private void emit(VertexConsumer builder, Matrix4f m, float x, float y, float u, float v, float a) {
        builder.vertex(m, x, y, 0.0F).uv(u, v).color(rCol, gCol, bCol, a).endVertex();
    }

    private static boolean hasRevealer() {
        Minecraft mc = Minecraft.getInstance();
        // Use the camera entity so a third-party observer/spectator sees the
        // brightness of the beam according to *their* equipment.
        net.minecraft.world.entity.Entity camera = mc.getCameraEntity();
        net.minecraft.world.entity.player.Player viewer = camera instanceof net.minecraft.world.entity.player.Player p
                ? p : mc.player;
        if (viewer == null) return false;
        ItemStack offhand = viewer.getOffhandItem();
        if (!offhand.isEmpty() && offhand.getItem() instanceof arcaneresearch.api.items.IRevealer) {
            return true;
        }
        for (ItemStack stack : viewer.getArmorSlots()) {
            if (!stack.isEmpty() && stack.getItem() instanceof arcaneresearch.api.items.IRevealer) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    public boolean shouldCull() {
        return false;
    }
}
