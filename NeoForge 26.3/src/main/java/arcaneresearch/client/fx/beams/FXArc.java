package arcaneresearch.client.fx.beams;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import org.joml.Quaternionf;
import arcaneresearch.client.fx.particles.FXGeneric;
import arcaneresearch.client.fx.particles.TCSprites;
import arcaneresearch.client.lib.events.handler.ParticleHandler;
import arcaneresearch.common.lib.utils.Utils;

import java.util.ArrayList;
import java.util.List;

// NEOFORGE_PORT: 1:1 logic from Forge 1.20.1 (points/embers/tick kept).
// REQUIRED changes: TextureSheetParticle removed -> SingleQuadParticle; per-segment
// camera quads now emit through QuadParticleRenderState.add (same centers/sizes/UVs/colors).
// Sheet UV fractions remapped into the stitched sprite (TCSprites); fullbright kept.
public class FXArc extends SingleQuadParticle {

    protected static final Identifier BEAM_TEXTURE =
            Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/beamh.png");

    protected List<Vec3> points = new ArrayList<>();
    protected double targetX;
    protected double targetY;
    protected double targetZ;
    protected float length;

    public FXArc(ClientLevel level, double x, double y, double z,
                 double targetX, double targetY, double targetZ,
                 float r, float g, float b, double heightGravity) {
        super(level, x, y, z, 0, 0, 0, null);

        this.rCol = r;
        this.gCol = g;
        this.bCol = b;

        this.setSize(0.02f, 0.02f);
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;

        this.targetX = targetX - x;
        this.targetY = targetY - y;
        this.targetZ = targetZ - z;

        this.lifetime = 3;

        calculateArcPoints(heightGravity);

        for (int i = 1; i < points.size() - 1; i++) {
            spawnEmber(points.get(i));
        }
    }

    protected void calculateArcPoints(double heightGravity) {
        Vec3 start = Vec3.ZERO;
        Vec3 end = new Vec3(this.targetX, this.targetY, this.targetZ);

        this.length = (float) end.length();

        double gravity = 0.115;
        double noise = 0.25;

        Vec3 velocity = Utils.calculateVelocity(start, end, heightGravity, gravity);
        double stepLengthSq = velocity.lengthSqr();

        Vec3 current = start;
        points.add(start);

        for (int i = 0; i < 50 && current.distanceToSqr(end) > stepLengthSq; i++) {
            Vec3 next = current.add(velocity);
            current = next;

            Vec3 noisyPoint = next.add(
                    (this.random.nextDouble() - this.random.nextDouble()) * noise,
                    (this.random.nextDouble() - this.random.nextDouble()) * noise,
                    (this.random.nextDouble() - this.random.nextDouble()) * noise
            );
            points.add(noisyPoint);

            velocity = velocity.subtract(0, gravity / 1.9, 0);
        }

        points.add(end);
    }

    private void spawnEmber(Vec3 offset) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        FXGeneric fb = new FXGeneric(this.level,
                this.x + offset.x, this.y + offset.y, this.z + offset.z, 0.0, 0.0, 0.0);
        int emberAge = 30 + this.random.nextInt(20);
        fb.setMaxAge(emberAge);
        fb.setColorRange(Mth.clamp(this.rCol * 3.0f, 0.0f, 1.0f),
                Mth.clamp(this.gCol * 3.0f, 0.0f, 1.0f),
                Mth.clamp(this.bCol * 3.0f, 0.0f, 1.0f),
                this.random.nextFloat(), this.random.nextFloat(), this.random.nextFloat());
        float[] alphas = new float[6 + this.random.nextInt(emberAge / 3)];
        for (int a = 1; a < alphas.length - 1; ++a) {
            alphas[a] = this.random.nextFloat();
        }
        alphas[0] = 1.0f;
        fb.setAlphaKeyframes(alphas);
        boolean sp = this.random.nextFloat() < 0.2f;
        fb.setParticles(sp ? 320 : 512, 16, 1);
        fb.setLoop(true);
        fb.setGravity(sp ? 0.0f : 0.125f);

        fb.setScaleKeyframes(0.05f, 0.0125f);
        fb.setLayer(0);
        fb.setSlowDown(0.995);
        fb.setRandomMovementScale(0.0025f, 0.001f, 0.0025f);
        int delay = 2 + this.random.nextInt(3);

        int ps = mc.options.particles().get().ordinal();
        if (mc.getFps() < 30) {
            ++ps;
        }
        if (this.level.getRandom().nextInt(3) < ps) {
            return;
        }
        ParticleHandler.schedule(() -> mc.particleEngine.add(fb), delay);
    }

    @Deprecated
    protected Vec3 calculateVelocity(Vec3 start, Vec3 end, double height, double gravity) {
        double dx = end.x - start.x;
        double dy = end.y - start.y;
        double dz = end.z - start.z;

        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        double time = horizontalDist / 0.5;
        if (time < 1) time = 1;

        double vx = dx / time;
        double vz = dz / time;
        double vy = (dy + 0.5 * gravity * time * time) / time + height;

        return new Vec3(vx, vy, vz);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    // NEOFORGE_PORT: per-segment camera quads -> state.add (same math as the old render()).
    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float partialTicks) {
        if (points.size() < 2) {
            return;
        }
        Vec3 cameraPos = camera.position();
        double px = Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x();
        double py = Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y();
        double pz = Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z();

        float alpha = 1.0f - (this.age + partialTicks) / this.lifetime;
        float size = 0.125f;
        int argb = net.minecraft.util.ARGB.color((int) ((alpha) * 255.0F), (int) ((this.rCol) * 255.0F), (int) ((this.gCol) * 255.0F), (int) ((this.bCol) * 255.0F));
        int light = 0xF000F0;

        Quaternionf quaternion = camera.rotation();
        for (int i = 0; i < points.size() - 1; i++) {
            Vec3 p = points.get(i);
            float mx = (float) (px + (p.x + points.get(i + 1).x) / 2.0);
            float my = (float) (py + (p.y + points.get(i + 1).y) / 2.0);
            float mz = (float) (pz + (p.z + points.get(i + 1).z) / 2.0);

            float u = (i % 16) / 64.0f;
            state.add(getLayer(), mx, my, mz,
                    quaternion.x(), quaternion.y(), quaternion.z(), quaternion.w(), size,
                    (u), (u + 0.015625f),
                    (0.0f), (0.015625f),
                    argb, light);
        }
    }

    @Override
    public ParticleRenderType getGroup() {
        return ParticleRenderType.SINGLE_QUADS;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TCSprites.translucent();
    }

    public void setRGB(float r, float g, float b) {
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
    }

    public List<Vec3> getPoints() {
        return points;
    }

    public float getLength() {
        return length;
    }
}