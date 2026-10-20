package arcaneresearch.client.fx.other;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.api.distmarker.Dist;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.Arcaneresearch;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import arcaneresearch.client.fx.BeamEngine;
import arcaneresearch.client.fx.particles.TCSprites;

// NEOFORGE_PORT: custom geometry -> BeamEngine (see BeamEngine.BeamEffect).
public class FXShieldRunes extends SingleQuadParticle implements BeamEngine.BeamEffect {

    private static final Identifier[] RIPPLE_TEXTURES = new Identifier[15];
    private static final Identifier[] HEMIS_TEXTURES = new Identifier[15];

    static {
        for (int i = 0; i < 15; i++) {
            RIPPLE_TEXTURES[i] = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/ripple" + (i + 1) + ".png");
            HEMIS_TEXTURES[i] = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/hemis" + (i + 1) + ".png");
        }
    }

    private final Entity target;
    private final float yaw;
    private final float pitch;

    public FXShieldRunes(ClientLevel level, double x, double y, double z, Entity target, int maxAge, float yaw, float pitch) {
        super(level, x, y, z, 0.0, 0.0, 0.0, null); // NEOFORGE_PORT: sprite unused (custom UVs + raw layer)

        this.rCol = 1.0f;
        this.gCol = 1.0f;
        this.bCol = 1.0f;
        this.gravity = 0.0f;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.lifetime = maxAge + this.random.nextInt(maxAge / 2);

        this.target = target;
        this.yaw = yaw;
        this.pitch = pitch;
        if (target != null) {
            this.xo = this.x = target.getX();
            this.yo = this.y = (target.getBoundingBox().minY + target.getBoundingBox().maxY) / 2.0;
            this.zo = this.z = target.getZ();
        }
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        if (this.target != null) {
            this.x = this.target.getX();
            this.y = (this.target.getBoundingBox().minY + this.target.getBoundingBox().maxY) / 2.0;
            this.z = this.target.getZ();
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

    // NEOFORGE_PORT: Forge computed the frame once from age+partialTicks and used
    // it for both texture bind and emission; the bind rides beamRenderTypes(partialTicks).
    public Identifier currentTexture(float partialTicks) {
        float fade = (this.age + partialTicks) / this.lifetime;
        int frame = Math.min(15, (int) (14.0f * fade) + 1);
        if (this.target != null && this.target instanceof Monster
                && !(this.target instanceof arcaneresearch.common.entities.monster.cult.EntityCultist)) {
            return RIPPLE_TEXTURES[frame - 1];
        }
        return HEMIS_TEXTURES[frame - 1];
    }

    @Override
    public RenderType beamRenderType() {
        return BeamEngine.additiveBeam(currentTexture(0.0f));
    }

    @Override
    public java.util.List<RenderType> beamRenderTypes(float partialTicks) {
        return java.util.List.of(BeamEngine.additiveBeam(currentTexture(partialTicks)));
    }

    @Override
    public java.util.List<RenderType> beamRenderTypes() {
        return java.util.List.of(beamRenderType());
    }

    @Override
    // NEOFORGE_PORT: BeamEngine drives ticking (engine never sees these).
    public boolean beamTick() {
        tick();
        return !this.removed;
    }

    @Override
    public void emitQuads(VertexConsumer consumer, Camera camera, float partialTicks) {

        float fade = (this.age + partialTicks) / this.lifetime;
        int frame = Math.min(15, (int)(14.0f * fade) + 1);


        Identifier texture;
        float b = 1.0f;
        if (this.target != null && this.target instanceof Monster
                && !(this.target instanceof arcaneresearch.common.entities.monster.cult.EntityCultist)) {
            texture = RIPPLE_TEXTURES[frame - 1];
            b = 0.5f;
        } else {
            texture = HEMIS_TEXTURES[frame - 1];
        }


        float px = (float)(Mth.lerp(partialTicks, this.xo, this.x) - camera.position().x);
        float py = (float)(Mth.lerp(partialTicks, this.yo, this.y) - camera.position().y);
        float pz = (float)(Mth.lerp(partialTicks, this.zo, this.z) - camera.position().z);
        float th = (this.target == null) ? 2.0f : this.target.getBbHeight();

        PoseStack poseStack = new PoseStack();
        poseStack.translate(px, py, pz);
        poseStack.rotate(Axis.YP.rotationDegrees(180.0f - this.yaw));
        poseStack.rotate(Axis.XP.rotationDegrees(-this.pitch));
        poseStack.scale(0.2f * th, 0.2f * th, 0.2f * th);

        float a = Math.min(1.0f, (1.0f - fade) * 3.0f);
        if (this.target == null) {
            HemisModel.render(poseStack, consumer, 0.65f, 0.1f, 0.5f, a);
        } else {
            HemisModel.render(poseStack, consumer, b, b, b, a);
        }


    }

}