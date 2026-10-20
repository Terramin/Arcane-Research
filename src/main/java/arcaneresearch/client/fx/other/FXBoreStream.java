package arcaneresearch.client.fx.other;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import arcaneresearch.lib.CoreGLE;
import arcaneresearch.lib.GLEEmitter;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.ARGB;
import arcaneresearch.client.fx.BeamEngine;
import arcaneresearch.client.fx.particles.TCSprites;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.lib.vec.Quat;

import java.awt.*;
import java.util.ArrayList;

// NEOFORGE_PORT: custom geometry -> BeamEngine (see BeamEngine.BeamEffect).
public class FXBoreStream extends SingleQuadParticle implements BeamEngine.BeamEffect {

    private static final Identifier TEX0 =
            Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/essentia.png");

    private final Entity target;
    private final double startX;
    private final double startY;
    private final double startZ;
    private final CoreGLE gle = new CoreGLE();
    public int length;
    int layer;
    double[][] points;
    float[][] colours;
    double[] radii;
    int growing;
    ArrayList<Quat> vecs;
    private int count;
    private BlockPos startPos;

    public FXBoreStream(ClientLevel level, double x, double y, double z,
                        Entity target, int count, int color, float scale, int extend, double my) {
        super(level, x, y, z, 0.0, 0.0, 0.0, null); // NEOFORGE_PORT: sprite unused (custom UVs + raw layer)
        this.count = 0;
        this.length = 5;
        String key = "";
        this.startPos = null;
        this.layer = 1;
        this.growing = -1;
        this.vecs = new ArrayList<>();
        this.quadSize = (float) (scale * (1.0 + random.nextGaussian() * 0.15000000596046448));
        this.length = Math.max(5, extend);
        this.count = count;
        this.target = target;
        this.lifetime = this.length * 10;
        this.xd = Mth.sin(count / 4.0f) * 0.15f;
        this.yd = my + Mth.sin(count / 3.0f) * 0.15f;
        this.zd = Mth.sin(count / 2.0f) * 0.15f;
        Color c = new Color(color);
        this.rCol = c.getRed() / 255.0f;
        this.gCol = c.getGreen() / 255.0f;
        this.bCol = c.getBlue() / 255.0f;
        this.gravity = 0.2f;
        this.vecs.add(new Quat(0.0, 0.0, 0.0, 0.001));
        this.vecs.add(new Quat(0.0, 0.0, 0.0, 0.001));
        this.startX = this.x;
        this.startY = this.y;
        this.startZ = this.z;
        this.startPos = BlockPos.containing(startX, startY, startZ);
    }

    public int getFXLayer() {
        return layer;
    }

    public void setFXLayer(int l) {
        layer = l;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime || length < 1) {
            this.remove();
            return;
        }
        this.yd += 0.01 * this.gravity;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.985;
        this.yd *= 0.985;
        this.zd *= 0.985;
        double dx = target.getX() - this.x;
        double dy = target.getY() + target.getEyeHeight() - this.y;
        double dz = target.getZ() - this.z;
        double d11 = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double clamp = d11 / 10.0;
        this.xd = Mth.clamp((float) this.xd, (float) -clamp, (float) clamp);
        this.yd = Mth.clamp((float) this.yd, (float) -clamp, (float) clamp);
        this.zd = Mth.clamp((float) this.zd, (float) -clamp, (float) clamp);
        dx /= d11;
        dy /= d11;
        dz /= d11;
        this.xd += dx * (clamp / Math.min(1.0, d11));
        this.yd += dy * (clamp / Math.min(1.0, d11));
        this.zd += dz * (clamp / Math.min(1.0, d11));
        float scale = this.quadSize * (0.75f + Mth.sin((count + this.age) / 2.0f) * 0.25f);
        if (d11 < 1.0) {
            float f = Mth.sin((float) (d11 * Math.PI / 2.0));
            scale *= f;
            this.quadSize *= f;
        }
        if (this.quadSize > 0.001) {
            vecs.add(new Quat(scale, this.x - startX, this.y - startY, this.z - startZ));
        } else {
            if (growing < 0) {
                growing = this.age;
            }
            --length;
        }
        if (vecs.size() > length) {
            vecs.remove(0);
        }
        points = new double[vecs.size()][3];
        colours = new float[vecs.size()][4];
        radii = new double[vecs.size()];
        int c = vecs.size();
        for (Quat v : vecs) {
            --c;
            float variance = 1.0f + Mth.sin((c + this.age) / 3.0f) * 0.2f;
            float xx = Mth.sin((c + this.age) / 6.0f) * 0.03f;
            float yy = Mth.sin((c + this.age) / 7.0f) * 0.03f;
            float zz = Mth.sin((c + this.age) / 8.0f) * 0.03f;
            points[c][0] = v.x + xx;
            points[c][1] = v.y + yy;
            points[c][2] = v.z + zz;
            radii[c] = v.s * variance;
            if (c > vecs.size() - 10) {
                radii[c] *= Mth.cos((c - (vecs.size() - 12)) / 10.0f * (float) (Math.PI / 2.0));
            }
            if (c == 0) {
                radii[c] = 0.0;
            } else if (c == 1) {
                radii[c] = 0.0;
            } else if (c == 2) {
                radii[c] = (this.quadSize * 0.5 + radii[c]) / 2.0;
            } else if (c == 3) {
                radii[c] = (this.quadSize + radii[c]) / 2.0;
            } else if (c == 4) {
                radii[c] = (this.quadSize + radii[c] * 2.0) / 3.0;
            }
            float v2 = 1.0f - Mth.sin((c + this.age) / 2.0f) * 0.1f;
            colours[c][0] = this.rCol * v2;
            colours[c][1] = this.gCol * v2;
            colours[c][2] = this.bCol * v2;
            colours[c][3] = 1.0f;
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
        return BeamEngine.additiveBeam(TEX0);
    }

    @Override
    // NEOFORGE_PORT: BeamEngine drives ticking (engine never sees these).
    public boolean beamTick() {
        tick();
        return !this.removed;
    }

    @Override
    public void emitQuads(VertexConsumer consumer, Camera camera, float partialTicks) {
        if (points == null || points.length <= 2) {
            return;
        }
        Vec3 cameraPos = camera.position();
        PoseStack poseStack = new PoseStack();
        poseStack.translate(startX - cameraPos.x(), startY - cameraPos.y(), startZ - cameraPos.z());
        gle.set_POLYCYL_TESS(8);
        gle.gleSetJoinStyle(1042);
        gle.setEmitter(new GLEEmitter(poseStack, consumer, 0xF000F0));

        gle.glePolyCone(points.length, points, colours, radii, 0.075f,
                (growing < 0) ? 0.0f : (0.075f * (this.age - growing + partialTicks)));
    }

    // NEOFORGE_PORT: getLightColor -> getLightCoords (same fullbright contract).
    @Override
    protected int getLightCoords(float partialTick) {
        return 0xF000F0;
    }

    public void setGravity(float value) {
        this.gravity = value;
    }

    public BlockPos getStartPos() {
        return startPos;
    }

    public void setStartPos(BlockPos startPos) {
        this.startPos = startPos;
    }
}