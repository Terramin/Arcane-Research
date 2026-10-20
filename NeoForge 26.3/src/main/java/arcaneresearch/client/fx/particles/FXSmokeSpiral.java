package arcaneresearch.client.fx.particles;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.util.Mth;
import arcaneresearch.client.fx.particles.TCSprites;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import org.joml.Quaternionf;

// NEOFORGE_PORT: camera spiral quad -> base extract path (same math).
public class FXSmokeSpiral extends SingleQuadParticle {

    protected float radius;
    protected int startAngle;
    protected int minY;
    protected static final int GRID_SIZE = 64;

    public FXSmokeSpiral(ClientLevel level, double x, double y, double z,
                         float radius, int startAngle, int minY) {
        super(level, x, y, z, 0, 0, 0, null); // NEOFORGE_PORT: sprite unused (custom UVs + raw layer)

        this.radius = radius;
        this.startAngle = startAngle;
        this.minY = minY;

        this.gravity = -0.01f;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;

        this.quadSize = 1.0f;
        this.lifetime = 20 + this.random.nextInt(10);

        this.setSize(0.01f, 0.01f);
        this.alpha = 1.0f;
    }

    @Override
    public void tick() {

        this.alpha = (this.lifetime - this.age) / (float) this.lifetime;

        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    @Override
    // NEOFORGE_PORT: same spiral math; immediate verts -> state.add (atlas-remapped UVs).
    public void extract(QuadParticleRenderState state, Camera camera, float partialTicks) {
        Vec3 cameraPos = camera.position();

        float progress = (this.age + partialTicks) / this.lifetime;
        float spiralAngle = this.startAngle + 720.0f * progress;
        float pitchAngle = 90.0f - 180.0f * progress;

        float spiralRad = spiralAngle / 180.0f * (float) Math.PI;
        float pitchRad = pitchAngle / 180.0f * (float) Math.PI;

        float offsetX = -Mth.sin(spiralRad) * Mth.cos(pitchRad) * this.radius;
        float offsetY = -Mth.sin(pitchRad) * this.radius;
        float offsetZ = Mth.cos(spiralRad) * Mth.cos(pitchRad) * this.radius;

        float finalY = (float) Math.max(this.y + offsetY, this.minY + 0.1);

        float x = (float) (this.x + offsetX - cameraPos.x());
        float y = (float) (finalY - cameraPos.y());
        float z = (float) (this.z + offsetZ - cameraPos.z());

        int spriteIndex = (int) (1.0f + progress * 4.0f);
        float u0 = (spriteIndex % 16) / 64.0f;
        float u1 = u0 + 0.015625f;
        float v0 = (spriteIndex / 16) / 64.0f;
        float v1 = v0 + 0.015625f;

        float size = 0.15f * this.quadSize;
        float displayAlpha = 0.66f * this.alpha;

        int light = this.getLightCoords(partialTicks);

        Quaternionf quaternion = camera.rotation();
        state.add(getLayer(), x, y, z,
                quaternion.x(), quaternion.y(), quaternion.z(), quaternion.w(), size,
                (u0), (u1),
                (v0), (v1),
                net.minecraft.util.ARGB.color((int) ((displayAlpha) * 255.0F), (int) ((this.rCol) * 255.0F), (int) ((this.gCol) * 255.0F), (int) ((this.bCol) * 255.0F)), light);
    }

    @Override
    public net.minecraft.client.particle.ParticleRenderType getGroup() {
        return net.minecraft.client.particle.ParticleRenderType.SINGLE_QUADS;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TCSprites.translucent();
    }

    @Override
    public void setColor(float r, float g, float b) {
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
    }
}