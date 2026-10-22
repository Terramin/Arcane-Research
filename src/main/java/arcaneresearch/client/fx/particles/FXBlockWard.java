package arcaneresearch.client.fx.particles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ARGB;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier; 
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import org.joml.Matrix4f;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import arcaneresearch.client.fx.BeamEngine;


public class FXBlockWard extends SingleQuadParticle implements arcaneresearch.client.fx.BeamEngine.BeamEffect {

    private static final Identifier[] TEXTURES = new Identifier[15];

    static {
        for (int i = 0; i < 15; i++) {
            TEXTURES[i] = Identifier.fromNamespaceAndPath("arcaneresearch", "textures/models/hemis" + (i + 1) + ".png");
        }
    }

    private final Direction side;
    private final int rotation;
    private final float offsetX;
    private final float offsetY;
    private final float offsetZ;

    public FXBlockWard(ClientLevel level, double x, double y, double z,
                       Direction side, float hitX, float hitY, float hitZ) {
        super(level, x, y, z, 0, 0, 0, null); 

        this.side = side;

        this.gravity = 0.0f;
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;

        this.lifetime = 12 + random.nextInt(5);
        this.setSize(0.01f, 0.01f);

        this.xo = x;
        this.yo = y;
        this.zo = z;

        this.quadSize = (float)(1.4 + random.nextGaussian() * 0.3);
        this.rotation = random.nextInt(360);

        this.offsetX = side.getStepX() != 0 ? 0 : Mth.clamp(hitX - 0.6f + random.nextFloat() * 0.2f, -0.4f, 0.4f);
        this.offsetY = side.getStepY() != 0 ? 0 : Mth.clamp(hitY - 0.6f + random.nextFloat() * 0.2f, -0.4f, 0.4f);
        this.offsetZ = side.getStepZ() != 0 ? 0 : Mth.clamp(hitZ - 0.6f + random.nextFloat() * 0.2f, -0.4f, 0.4f);

        this.rCol = 1.0f;
        this.gCol = 1.0f;
        this.bCol = 1.0f;
        this.alpha = 0.0f;
    }

    @Override
    public void tick() {
        this.xo = x;
        this.yo = y;
        this.zo = z;

        float threshold = lifetime / 5.0f;
        if (age <= threshold) {
            this.alpha = age / threshold;
        } else {
            this.alpha = (lifetime - age) / (float)lifetime;
        }

        if (this.age++ >= this.lifetime) {
            this.remove();
        }

        this.yd -= 0.04 * this.gravity;
        this.x += this.xd;
        this.y += this.yd;
        this.z += this.zd;
    }

    @Override
public void extract(net.minecraft.client.renderer.state.level.QuadParticleRenderState state, Camera camera, float partialTicks) {
    }

    @Override
    public net.minecraft.client.particle.ParticleRenderType getGroup() {
        return net.minecraft.client.particle.ParticleRenderType.NO_RENDER;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TCSprites.translucent();
    }

    
    
    public net.minecraft.resources.Identifier currentTexture(float partialTicks) {
        float fade = (age + partialTicks) / lifetime;
        int frame = Math.min(14, (int) (15.0f * fade));
        return TEXTURES[frame];
    }

    @Override
    public RenderType beamRenderType() {
        return arcaneresearch.client.fx.BeamEngine.additiveBeam(currentTexture(0.0f));
    }

    @Override
    public java.util.List<RenderType> beamRenderTypes(float partialTicks) {
        return java.util.List.of(
                arcaneresearch.client.fx.BeamEngine.additiveBeam(currentTexture(partialTicks)));
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
        renderWard(consumer, camera, partialTicks);
    }

    protected void renderWard(VertexConsumer consumer, Camera camera, float partialTicks) {

        float fade = (age + partialTicks) / lifetime;
        int frame = Math.min(14, (int)(15.0f * fade));



        Vec3 camPos = camera.position();
        float px = (float)(Mth.lerp(partialTicks, xo, x) - camPos.x() + offsetX);
        float py = (float)(Mth.lerp(partialTicks, yo, y) - camPos.y() + offsetY);
        float pz = (float)(Mth.lerp(partialTicks, zo, z) - camPos.z() + offsetZ);

        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(px, py, pz);

        poseStack.rotate(Axis.YP.rotationDegrees(90.0f * side.getStepY()));
        poseStack.rotate(Axis.XN.rotationDegrees(90.0f * side.getStepX()));
        poseStack.rotate(Axis.YP.rotationDegrees(90.0f * side.getStepZ()));

        poseStack.rotate(Axis.ZP.rotationDegrees(rotation));

        if (side.getStepZ() > 0) {
            poseStack.translate(0, 0, 0.505);
            poseStack.rotate(Axis.YN.rotationDegrees(180.0f));
        } else if (side.getStepZ() < 0) {
            poseStack.translate(0, 0, -0.505);
        } else if (side.getStepX() > 0) {
            poseStack.translate(0.505, 0, 0);
            poseStack.rotate(Axis.YP.rotationDegrees(90.0f));
        } else if (side.getStepX() < 0) {
            poseStack.translate(-0.505, 0, 0);
            poseStack.rotate(Axis.YN.rotationDegrees(90.0f));
        } else if (side.getStepY() > 0) {
            poseStack.translate(0, 0.505, 0);
            poseStack.rotate(Axis.XP.rotationDegrees(90.0f));
        } else if (side.getStepY() < 0) {
            poseStack.translate(0, -0.505, 0);
            poseStack.rotate(Axis.XN.rotationDegrees(90.0f));
        }

        Matrix4f matrix = poseStack.last().pose();

        float size = quadSize * 0.5f;
        float renderAlpha = alpha / 2.0f;


        consumer.addVertex(matrix, -size, size, 0)
                .setUv(0, 1).setColor(rCol, gCol, bCol, renderAlpha)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex(matrix, size, size, 0)
                .setUv(1, 1).setColor(rCol, gCol, bCol, renderAlpha)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex(matrix, size, -size, 0)
                .setUv(1, 0).setColor(rCol, gCol, bCol, renderAlpha)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex(matrix, -size, -size, 0)
                .setUv(0, 0).setColor(rCol, gCol, bCol, renderAlpha)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);


        poseStack.popPose();

    }



    public void setColor(float r, float g, float b) {
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
    }

    public void setGravity(float value) {
        this.gravity = value;
    }
}