package arcaneresearch.client.fx.beams;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
@OnlyIn(Dist.CLIENT)
public class FXBeamWand extends TextureSheetParticle {
    private static final ResourceLocation BEAM_TEXTURE = new ResourceLocation("arcaneresearch", "textures/misc/beam.png");
    private static final ResourceLocation BEAM1_TEXTURE = new ResourceLocation("arcaneresearch", "textures/misc/beam1.png");
    private static final ResourceLocation BEAM2_TEXTURE = new ResourceLocation("arcaneresearch", "textures/misc/beam2.png");
    private static final ResourceLocation BEAM3_TEXTURE = new ResourceLocation("arcaneresearch", "textures/misc/beam3.png");
    private static final ResourceLocation PARTICLE_TEXTURE = new ResourceLocation("arcaneresearch", "textures/misc/particles.png");
    public int impact = 0;
    protected LivingEntity sourceEntity;
    protected double offset;
    protected double targetX;
    protected double targetY;
    protected double targetZ;
    protected double prevTargetX;
    protected double prevTargetY;
    protected double prevTargetZ;
    protected float length;
    protected float rotYaw;
    protected float rotPitch;
    protected float prevYaw;
    protected float prevPitch;
    protected int beamType = 0;
    protected float endMod = 1.0f;
    protected boolean reverse = false;
    protected boolean pulse = true;
    protected int rotationSpeed = 5;
    protected float prevSize = 0.0f;
    public FXBeamWand(ClientLevel level, LivingEntity source,
                      double tx, double ty, double tz,
                      float r, float g, float b, int maxAge) {
        super(level, source.getX(), source.getY(), source.getZ(), 0, 0, 0);
        this.sourceEntity = source;
        this.offset = source.getBbHeight() / 2.0f + 0.25;
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
        this.setSize(0.02f, 0.02f);
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        this.targetX = tx;
        this.targetY = ty;
        this.targetZ = tz;
        this.prevTargetX = tx;
        this.prevTargetY = ty;
        this.prevTargetZ = tz;
        calculateBeamGeometry();
        this.prevYaw = rotYaw;
        this.prevPitch = rotPitch;
        this.lifetime = maxAge;
        Minecraft mc = Minecraft.getInstance();
        if (mc.cameraEntity != null) {
            int visibleDistance = mc.options.graphicsMode().get().getId() > 0 ? 50 : 25;
            if (mc.cameraEntity.distanceTo(source) > visibleDistance) {
                this.lifetime = 0;
            }
        }
    }
    public void updateBeam(double x, double y, double z) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
        while (this.lifetime - this.age < 4) {
            this.lifetime++;
        }
    }
    protected void calculateBeamGeometry() {
        double sx = sourceEntity.getX();
        double sy = sourceEntity.getY() + offset;
        double sz = sourceEntity.getZ();
        float dx = (float) (sx - targetX);
        float dy = (float) (sy - targetY);
        float dz = (float) (sz - targetZ);
        this.length = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        double horizontalDist = Mth.sqrt(dx * dx + dz * dz);
        this.rotYaw = (float) (Math.atan2(dx, dz) * 180.0 / Math.PI);
        this.rotPitch = (float) (Math.atan2(dy, horizontalDist) * 180.0 / Math.PI);
    }
    @Override
    public void tick() {
        this.xo = sourceEntity.getX();
        this.yo = sourceEntity.getY() + offset;
        this.zo = sourceEntity.getZ();
        this.prevTargetX = targetX;
        this.prevTargetY = targetY;
        this.prevTargetZ = targetZ;
        this.prevYaw = rotYaw;
        this.prevPitch = rotPitch;
        calculateBeamGeometry();
        while (rotPitch - prevPitch < -180.0f) prevPitch -= 360.0f;
        while (rotPitch - prevPitch >= 180.0f) prevPitch += 360.0f;
        while (rotYaw - prevYaw < -180.0f) prevYaw -= 360.0f;
        while (rotYaw - prevYaw >= 180.0f) prevYaw += 360.0f;
        if (impact > 0) {
            impact--;
        }
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }
    @Override
    public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTicks) {
        renderBeam(camera, partialTicks);
    }
    protected void renderBeam(Camera camera, float partialTicks) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        float size = 1.0f;
        if (pulse) {
            size = Math.min(age / 4.0f, 1.0f);
            size = prevSize + (size - prevSize) * partialTicks;
        }
        float opacity = 0.4f;
        if (pulse && lifetime - age <= 4) {
            opacity = 0.4f - (4 - (lifetime - age)) * 0.1f;
        }
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        ResourceLocation texture = switch (beamType) {
            case 1 -> BEAM1_TEXTURE;
            case 2 -> BEAM2_TEXTURE;
            case 3 -> BEAM3_TEXTURE;
            default -> BEAM_TEXTURE;
        };
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(770, 1);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        Vec3 camPos = camera.getPosition();
        float sx;
        float sy;
        float sz;
        float yaw;
        float pitch;
        float beamLen;
        Minecraft mcBeam = Minecraft.getInstance();
        if (sourceEntity != null && sourceEntity == mcBeam.player
                && mcBeam.options.getCameraType().isFirstPerson()) {
            Vec3 tip = null;
            if (sourceEntity instanceof net.minecraft.world.entity.player.Player p) {
                tip = arcaneresearch.client.lib.events.WandTipHelper.tipWorld(p, partialTicks,
                        0.0F, 0.0F, beamType == 2
                                ? arcaneresearch.client.lib.events.WandTipHelper.TIP_DZ_BEAM
                                : 0.0F);
            }
            if (tip == null) {
                Vec3 look0 = sourceEntity.getViewVector(partialTicks);
                tip = new Vec3(camPos.x + look0.x, camPos.y + look0.y, camPos.z + look0.z);
            }
            double stx = tip.x;
            double sty = tip.y;
            double stz = tip.z;
            double etx = Mth.lerp(partialTicks, prevTargetX, targetX);
            double ety = Mth.lerp(partialTicks, prevTargetY, targetY);
            double etz = Mth.lerp(partialTicks, prevTargetZ, targetZ);
            float dx = (float) (stx - etx);
            float dy = (float) (sty - ety);
            float dz = (float) (stz - etz);
            beamLen = Mth.sqrt(dx * dx + dy * dy + dz * dz);
            float horiz = Mth.sqrt(dx * dx + dz * dz);
            yaw = (float) (Math.atan2(dx, dz) * 180.0 / Math.PI);
            pitch = (float) (Math.atan2(dy, horiz) * 180.0 / Math.PI);
            sx = (float) (stx - camPos.x);
            sy = (float) (sty - camPos.y);
            sz = (float) (stz - camPos.z);
        } else {
            assert sourceEntity != null;
            double prevSX = sourceEntity.xo;
            double prevSY = sourceEntity.yo + offset;
            double prevSZ = sourceEntity.zo;
            double currSX = sourceEntity.getX();
            double currSY = sourceEntity.getY() + offset;
            double currSZ = sourceEntity.getZ();
            float yawRad = sourceEntity.getYRot() * ((float) Math.PI / 180.0f);
            float cosYaw = Mth.cos(yawRad);
            float sinYaw = Mth.sin(yawRad);
            prevSX -= cosYaw * 0.066f;
            prevSY -= 0.06;
            prevSZ -= sinYaw * 0.04f;
            currSX -= cosYaw * 0.066f;
            currSY -= 0.06;
            currSZ -= sinYaw * 0.04f;
            Vec3 look = sourceEntity.getLookAngle();
            prevSX += look.x * 0.3;
            prevSY += look.y * 0.3;
            prevSZ += look.z * 0.3;
            currSX += look.x * 0.3;
            currSY += look.y * 0.3;
            currSZ += look.z * 0.3;
            sx = (float) (Mth.lerp(partialTicks, prevSX, currSX) - camPos.x());
            sy = (float) (Mth.lerp(partialTicks, prevSY, currSY) - camPos.y());
            sz = (float) (Mth.lerp(partialTicks, prevSZ, currSZ) - camPos.z());
            yaw = Mth.lerp(partialTicks, prevYaw, rotYaw);
            pitch = Mth.lerp(partialTicks, prevPitch, rotPitch);
            beamLen = this.length;
        }
        Minecraft mc = Minecraft.getInstance();
        float slide = mc.player != null ? (float) mc.player.tickCount + partialTicks : 0;
        if (reverse) slide *= -1.0f;
        float uvOffset = -slide * 0.2f - Mth.floor(-slide * 0.1f);
        float rot = (level.getGameTime() % ((float) 360 / rotationSpeed)) * rotationSpeed + rotationSpeed * partialTicks;
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(sx, sy, sz);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
        poseStack.mulPose(Axis.ZN.rotationDegrees(180.0f + yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(rot));
        Matrix4f matrix = poseStack.last().pose();
        float beamWidth = 0.15f * size;
        float beamWidthEnd = beamWidth * endMod;
        float beamLength = beamLen * size;
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        for (int t = 0; t < 3; t++) {
            float u0 = 0.0f;
            float u1 = 1.0f;
            float v0 = uvOffset + t / 3.0f;
            float v1 = beamLength + v0;
            builder.vertex(matrix, -beamWidthEnd, beamLength, 0)
                    .uv(u1, v1).color(rCol, gCol, bCol, opacity).endVertex();
            builder.vertex(matrix, -beamWidth, 0, 0)
                    .uv(u1, v0).color(rCol, gCol, bCol, opacity).endVertex();
            builder.vertex(matrix, beamWidth, 0, 0)
                    .uv(u0, v0).color(rCol, gCol, bCol, opacity).endVertex();
            builder.vertex(matrix, beamWidthEnd, beamLength, 0)
                    .uv(u0, v1).color(rCol, gCol, bCol, opacity).endVertex();
            poseStack.mulPose(Axis.YP.rotationDegrees(60.0f));
            matrix = poseStack.last().pose();
        }
        tesselator.end();
        poseStack.popPose();
        if (impact > 0) {
            renderImpact(camera, partialTicks);
        }
        RenderSystem.depthMask(true);
        RenderSystem.blendFunc(770, 771);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        prevSize = size;
    }
    protected void renderImpact(Camera camera, float partialTicks) {
        RenderSystem.setShaderTexture(0, PARTICLE_TEXTURE);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(770, 1);
        Vec3 camPos = camera.getPosition();
        float tx = (float) (Mth.lerp(partialTicks, prevTargetX, targetX) - camPos.x());
        float ty = (float) (Mth.lerp(partialTicks, prevTargetY, targetY) - camPos.y());
        float tz = (float) (Mth.lerp(partialTicks, prevTargetZ, targetZ) - camPos.z());
        float impactSize = endMod / 2.0f / (6 - impact);
        int frame = age % 16;
        float u0 = frame / 16.0f;
        float u1 = u0 + 0.0625f;
        float v0 = 0.3125f;
        float v1 = v0 + 0.0625f;
        Quaternionf rotation = camera.rotation();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        float[][] offsets = {
                {-impactSize, -impactSize},
                {-impactSize, impactSize},
                {impactSize, impactSize},
                {impactSize, -impactSize}
        };
        float[][] uvs = {
                {u1, v1},
                {u1, v0},
                {u0, v0},
                {u0, v1}
        };
        for (int i = 0; i < 4; i++) {
            float ox = offsets[i][0];
            float oy = offsets[i][1];
            float rx = ox * (1 - 2 * rotation.y() * rotation.y() - 2 * rotation.z() * rotation.z())
                    + oy * (2 * rotation.x() * rotation.y() - 2 * rotation.w() * rotation.z());
            float ry = ox * (2 * rotation.x() * rotation.y() + 2 * rotation.w() * rotation.z())
                    + oy * (1 - 2 * rotation.x() * rotation.x() - 2 * rotation.z() * rotation.z());
            float rz = ox * (2 * rotation.x() * rotation.z() - 2 * rotation.w() * rotation.y())
                    + oy * (2 * rotation.y() * rotation.z() + 2 * rotation.w() * rotation.x());
            builder.vertex(tx + rx, ty + ry, tz + rz)
                    .uv(uvs[i][0], uvs[i][1])
                    .color(rCol, gCol, bCol, 0.66f)
                    .endVertex();
        }
        tesselator.end();
        RenderSystem.blendFunc(770, 771);
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }
    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }
    @Override
    public boolean shouldCull() {
        return false;
    }
    public void setRGB(float r, float g, float b) {
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
    }
    public void setType(int type) {
        this.beamType = type;
    }
    public void setEndMod(float endMod) {
        this.endMod = endMod;
    }
    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }
    public void setPulse(boolean pulse) {
        this.pulse = pulse;
    }
    public void setRotationSpeed(int rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }
    @Deprecated
    public LivingEntity getSourceEntity() {
        return sourceEntity;
    }
    public float getLength() {
        return length;
    }
}
