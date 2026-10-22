package arcaneresearch.client.fx.other;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;

import net.minecraft.resources.Identifier; 
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.fx.particles.ArcaneresearchParticle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import arcaneresearch.client.fx.BeamEngine;
import arcaneresearch.client.fx.particles.TCSprites;


public class FXSonic extends SingleQuadParticle implements BeamEngine.BeamEffect {

    private static final Identifier[] RIPPLE_TEXTURES = new Identifier[15];

    static {
        for (int i = 0; i < 15; i++) {
            RIPPLE_TEXTURES[i] = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/ripple" + (i + 1) + ".png");
        }
    }

    private final Entity target;
    private final float yaw;
    private final float pitch;

    public FXSonic(ClientLevel level, double x, double y, double z, Entity target, int maxAge) {
        super(level, x, y, z, null);

        this.target = target;
        this.yaw = target.getYHeadRot();
        this.pitch = target.getXRot();

        this.rCol = 1.0f;
        this.gCol = 1.0f;
        this.bCol = 1.0f;
        this.gravity = 0.0f;
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;

        this.lifetime = maxAge + this.random.nextInt(maxAge / 2);
        this.quadSize = 1.0f;

        this.x = target.getX();
        this.xo = this.x;
        this.y = target.getY() + target.getEyeHeight();
        this.yo = this.y;
        this.z = target.getZ();
        this.zo = this.z;
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

        this.x = this.target.getX();
        this.y = this.target.getY() + this.target.getEyeHeight();
        this.z = this.target.getZ();
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

    
    
    public Identifier currentTexture(float partialTicks) {
        float fade = (this.age + partialTicks) / this.lifetime;
        int frame = Math.min(15, (int) (14.0f * fade) + 1);
        return RIPPLE_TEXTURES[frame - 1];
    }

    @Override
    public RenderType beamRenderType() {
        return BeamEngine.normalBeamNoDepthCull(currentTexture(0.0f));
    }

    @Override
    public java.util.List<RenderType> beamRenderTypes(float partialTicks) {
        return java.util.List.of(BeamEngine.normalBeamNoDepthCull(currentTexture(partialTicks)));
    }

    @Override
    public java.util.List<RenderType> beamRenderTypes() {
        return java.util.List.of(beamRenderType());
    }

    @Override
    
    public boolean beamTick() {
        tick();
        return !this.removed;
    }

    @Override
    public void emitQuads(VertexConsumer consumer, Camera camera, float partialTicks) {

        float fade = (this.age + partialTicks) / this.lifetime;
        int frame = Math.min(15, (int)(14.0f * fade) + 1);



        float px = (float)(Mth.lerp(partialTicks, this.xo, this.x) - camera.position().x);
        float py = (float)(Mth.lerp(partialTicks, this.yo, this.y) - camera.position().y);
        float pz = (float)(Mth.lerp(partialTicks, this.zo, this.z) - camera.position().z);
        float h = this.target.getBbHeight();
        float w = this.target.getBbWidth();

        com.mojang.blaze3d.vertex.PoseStack poseStack = new com.mojang.blaze3d.vertex.PoseStack();
        poseStack.translate(px, py, pz);
        
        
        poseStack.rotate(Axis.YP.rotationDegrees(-this.yaw));
        poseStack.rotate(Axis.XP.rotationDegrees(this.pitch));
        poseStack.translate(0.0, 0.0, 2.0f * h + w / 2.0f);
        poseStack.scale(0.25f * h, 0.25f * h, -1.0f * h);

        HemisModel.render(poseStack, consumer, 0.5f, 0.5f, 0.5f, 1.0f);


    }

}