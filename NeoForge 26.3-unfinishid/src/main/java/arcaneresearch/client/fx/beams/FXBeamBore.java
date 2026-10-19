package arcaneresearch.client.fx.beams;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.ARGB;
import arcaneresearch.client.fx.BeamEngine;
import arcaneresearch.client.fx.particles.TCSprites;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

// NEOFORGE_PORT: custom geometry -> BeamEngine (see BeamEngine.BeamEffect).
public class FXBeamBore extends SingleQuadParticle implements BeamEngine.BeamEffect {

    private static final Identifier BEAM_TEXTURE = Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/beam.png");
    private static final Identifier BEAM1_TEXTURE = Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/beam1.png");
    private static final Identifier BEAM2_TEXTURE = Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/beam2.png");
    private static final Identifier BEAM3_TEXTURE = Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/beam3.png");
    private static final Identifier PARTICLE_TEXTURE = Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/particles.png");
    private static final Identifier NODE_TEXTURE = Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/auranodes.png");
    public int impact = 0;
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

    public FXBeamBore(ClientLevel level, double px, double py, double pz,
                      double tx, double ty, double tz,
                      float r, float g, float b, int maxAge) {
        super(level, px, py, pz, 0, 0, 0, null); // NEOFORGE_PORT: sprite unused (custom UVs + raw layer)

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
        int visibleDistance = mc.options.graphicsPreset().get().ordinal() > 0 ? 64 : 32;
        if (mc.getCameraEntity() != null && mc.getCameraEntity().distanceToSqr(px, py, pz) > visibleDistance * visibleDistance) {
            this.lifetime = 0;
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

    protected void calculateBeamGeometry() {
        float dx = (float) (x - targetX);
        float dy = (float) (y - targetY);
        float dz = (float) (z - targetZ);

        this.length = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        double horizontalDist = Mth.sqrt(dx * dx + dz * dz);

        this.rotYaw = (float) (Math.atan2(dx, dz) * 180.0 / Math.PI);
        this.rotPitch = (float) (Math.atan2(dy, horizontalDist) * 180.0 / Math.PI);
    }

    @Override
    public void tick() {

        this.xo = x;
        this.yo = y;
        this.zo = z;

        this.prevTargetX = targetX;
        this.prevTargetY = targetY;
        this.prevTargetZ = targetZ;

        this.prevYaw = rotYaw;
        this.prevPitch = rotPitch;

        calculateBeamGeometry();

        if (impact > 0) {
            impact--;
        }

        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    @Override
public void extract(QuadParticleRenderState state, Camera camera, float partialTicks) {
    }

    @Override
    public net.minecraft.client.particle.ParticleRenderType getGroup() {
        return net.minecraft.client.particle.ParticleRenderType.NO_RENDER;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TCSprites.translucent();
    }

    @Override
    public RenderType beamRenderType() {
        // NEOFORGE_PORT: Forge renderBeam() texture switch by beamType 1:1
        // (beam1/2/3.png vs beam.png); the bind carries it now.
        return BeamEngine.additiveBeam(switch (beamType) {
            case 1 -> BEAM1_TEXTURE;
            case 2 -> BEAM2_TEXTURE;
            case 3 -> BEAM3_TEXTURE;
            default -> BEAM_TEXTURE;
        });
    }

            // 26.3 green build: annotation dropped (base signature changed).
    private static final Identifier TC_SHEET_RAW =
            Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/particles.png");

    @Override
    public java.util.List<RenderType> beamRenderTypes() {
        // NEOFORGE_PORT: body (beam texture) + source glow (node texture) + impact (raw sheet).
        return java.util.List.of(beamRenderType(), BeamEngine.additiveBeam(NODE_TEXTURE),
                BeamEngine.additiveBeam(TC_SHEET_RAW));
    }

    @Override
    // NEOFORGE_PORT: BeamEngine drives ticking (engine never sees these).
    public boolean beamTick() {
        tick();
        return !this.removed;
    }

    @Override
    public void emitQuads(VertexConsumer consumer, Camera camera, float partialTicks) {
        renderBeam(consumer, camera, partialTicks);
    }

    @Override
    public void emitQuads(VertexConsumer consumer, Camera camera, float partialTicks, RenderType type) {
        if (type.equals(BeamEngine.additiveBeam(NODE_TEXTURE))) {
            renderSource(consumer, camera, partialTicks);
        } else if (type.equals(BeamEngine.additiveBeam(TC_SHEET_RAW))) {
            if (impact > 0) {
                renderImpact(consumer, camera, partialTicks);
            }
        } else {
            renderBeam(consumer, camera, partialTicks);
        }
    }

    protected void renderBeam(VertexConsumer consumer, Camera camera, float partialTicks) {

        float size = 1.0f;
        if (pulse) {
            size = Math.min(age / 4.0f, 1.0f);
            size = prevSize + (size - prevSize) * partialTicks;
        }

        float opacity = 0.4f;
        if (pulse && lifetime - age <= 4) {
            opacity = 0.4f - (4 - (lifetime - age)) * 0.1f;
        }


        Vec3 camPos = camera.position();
        float sx = (float) (Mth.lerp(partialTicks, xo, x) - camPos.x());
        float sy = (float) (Mth.lerp(partialTicks, yo, y) - camPos.y());
        float sz = (float) (Mth.lerp(partialTicks, zo, z) - camPos.z());

        float yaw = Mth.lerp(partialTicks, prevYaw, rotYaw);
        float pitch = Mth.lerp(partialTicks, prevPitch, rotPitch);

        Minecraft mc = Minecraft.getInstance();
        float slide = mc.player != null ? (float) mc.player.tickCount + partialTicks : 0;
        if (reverse) slide *= -1.0f;
        float uvOffset = -slide * 0.2f - Mth.floor(-slide * 0.1f);

        float rot = (level.getLevelData().getGameTime() % ((float) 360 / rotationSpeed)) * rotationSpeed + rotationSpeed * partialTicks;

        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(sx, sy, sz);
        poseStack.rotate(Axis.XP.rotationDegrees(90.0f));
        poseStack.rotate(Axis.ZN.rotationDegrees(180.0f + yaw));
        poseStack.rotate(Axis.XP.rotationDegrees(pitch));
        poseStack.rotate(Axis.YP.rotationDegrees(rot));

        Matrix4f matrix = poseStack.last().pose();

        float beamWidth = 0.15f * size;
        float beamWidthEnd = beamWidth * endMod;
        float beamLength = this.length * size;


        for (int t = 0; t < 3; t++) {
            float u0 = 0.0f;
            float u1 = 1.0f;
            float v0 = uvOffset + t / 3.0f;
            float v1 = beamLength + v0;

            consumer.addVertex(matrix, -beamWidthEnd, beamLength, 0)
                    .setUv(u1, v1).setColor(rCol, gCol, bCol, opacity)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
            consumer.addVertex(matrix, -beamWidth, 0, 0)
                    .setUv(u1, v0).setColor(rCol, gCol, bCol, opacity)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
            consumer.addVertex(matrix, beamWidth, 0, 0)
                    .setUv(u0, v0).setColor(rCol, gCol, bCol, opacity)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
            consumer.addVertex(matrix, beamWidthEnd, beamLength, 0)
                    .setUv(u0, v1).setColor(rCol, gCol, bCol, opacity)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);

            poseStack.rotate(Axis.YP.rotationDegrees(60.0f));
            matrix = poseStack.last().pose();
        }


        poseStack.popPose();




        prevSize = size;
    }

    protected void renderSource(VertexConsumer consumer, Camera camera, float partialTicks) {

        Vec3 camPos = camera.position();
        float sx = (float) (Mth.lerp(partialTicks, xo, x) - camPos.x());
        float sy = (float) (Mth.lerp(partialTicks, yo, y) - camPos.y());
        float sz = (float) (Mth.lerp(partialTicks, zo, z) - camPos.z());

        float opacity = 0.8f;
        if (pulse && lifetime - age <= 4) {
            opacity = 0.8f - (4 - (lifetime - age)) * 0.2f;
        }

        int frame = age % 32;
        float u0 = frame / 32.0f;
        float u1 = u0 + 0.03125f;
        float v0 = 0.09375f;
        float v1 = v0 + 0.03125f;

        float glowSize = 0.33f;

        Quaternionf rotation = camera.rotation();


        float[][] offsets = {
                {-glowSize, -glowSize},
                {-glowSize, glowSize},
                {glowSize, glowSize},
                {glowSize, -glowSize}
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

            consumer.addVertex(sx + rx, sy + ry, sz + rz)
                    .setUv(uvs[i][0], uvs[i][1])
                    .setColor(rCol, gCol, bCol, opacity)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
        }


    }

    protected void renderImpact(VertexConsumer consumer, Camera camera, float partialTicks) {

        Vec3 camPos = camera.position();
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

            consumer.addVertex(tx + rx, ty + ry, tz + rz)
                    .setUv(uvs[i][0], uvs[i][1])
                    .setColor(rCol, gCol, bCol, 0.66f)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
        }


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

    public float getLength() {
        return length;
    }
}