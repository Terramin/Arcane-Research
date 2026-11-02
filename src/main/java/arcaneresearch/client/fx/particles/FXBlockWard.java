package arcaneresearch.client.fx.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public class FXBlockWard extends TextureSheetParticle {
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[15];

    static {
        for (int i = 0; i < 15; i++) {
            TEXTURES[i] = ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/models/hemis" + (i + 1) + ".png");
        }
    }

    private final Direction side;

    private final float offsetX;
    private final float offsetY;
    private final float offsetZ;

    private final Quaternionf staticRotation;
    private final boolean hasLocal;
    private final float localX, localY, localZ;
    private final Quaternionf localRotation;

    private final Matrix4f scratchMatrix = new Matrix4f();

    public FXBlockWard(ClientLevel level, double x, double y, double z,
                       Direction side, float hitX, float hitY, float hitZ) {
        super(level, x, y, z, 0, 0, 0);

        this.side = side;
        this.gravity = 0.0f;
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        this.lifetime = 12 + random.nextInt(5);
        this.setSize(0.01f, 0.01f);
        this.quadSize = (float) (1.4 + random.nextGaussian() * 0.3);

        int stepX = side.getStepX();
        int stepY = side.getStepY();
        int stepZ = side.getStepZ();

        float roll = random.nextInt(360);

        Quaternionf rot = new Quaternionf();

        if (stepY != 0) {
            rot.rotateY(stepY * Mth.HALF_PI);
        } else if (stepX != 0) {
            rot.rotateX(-stepX * Mth.HALF_PI);
        } else if (stepZ != 0) {
            rot.rotateY(stepZ * Mth.HALF_PI);
        }

        rot.rotateZ(roll * Mth.DEG_TO_RAD);
        this.staticRotation = rot;

        if (stepZ > 0) {
            hasLocal = true;
            localX = 0; localY = 0; localZ = 0.505f;
            localRotation = Axis.YN.rotationDegrees(180.0f);
        } else if (stepZ < 0) {
            hasLocal = true;
            localX = 0; localY = 0; localZ = -0.505f;
            localRotation = null;
        } else if (stepX > 0) {
            hasLocal = true;
            localX = 0.505f; localY = 0; localZ = 0;
            localRotation = Axis.YP.rotationDegrees(90.0f);
        } else if (stepX < 0) {
            hasLocal = true;
            localX = -0.505f; localY = 0; localZ = 0;
            localRotation = Axis.YN.rotationDegrees(90.0f);
        } else if (stepY > 0) {
            hasLocal = true;
            localX = 0; localY = 0.505f; localZ = 0;
            localRotation = Axis.XP.rotationDegrees(90.0f);
        } else if (stepY < 0) {
            hasLocal = true;
            localX = 0; localY = -0.505f; localZ = 0;
            localRotation = Axis.XN.rotationDegrees(90.0f);
        } else {
            hasLocal = false;
            localX = localY = localZ = 0;
            localRotation = null;
        }

        this.offsetX = stepX != 0 ? 0 : Mth.clamp(hitX - 0.6f + random.nextFloat() * 0.2f, -0.4f, 0.4f);
        this.offsetY = stepY != 0 ? 0 : Mth.clamp(hitY - 0.6f + random.nextFloat() * 0.2f, -0.4f, 0.4f);
        this.offsetZ = stepZ != 0 ? 0 : Mth.clamp(hitZ - 0.6f + random.nextFloat() * 0.2f, -0.4f, 0.4f);

        this.rCol = 1.0f;
        this.gCol = 1.0f;
        this.bCol = 1.0f;
        this.alpha = 0.0f;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        float threshold = lifetime / 5.0f;

        if (age <= threshold) {
            this.alpha = age / threshold;
        } else {
            this.alpha = (lifetime - age) / (float) lifetime;
        }

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.yd -= 0.04 * this.gravity;
        this.x += this.xd;
        this.y += this.yd;
        this.z += this.zd;
    }

    @Override
    public void render(com.mojang.blaze3d.vertex.@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTicks) {
        renderWard(camera, partialTicks);
    }

    protected void renderWard(Camera camera, float partialTicks) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();

        float fade = (age + partialTicks) / lifetime;
        int frame = Math.min(14, (int) (15.0f * fade));

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, TEXTURES[frame]);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(770, 1);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        Vec3 camPos = camera.getPosition();

        float px = (float) (Mth.lerp(partialTicks, xo, x) - camPos.x() + offsetX);
        float py = (float) (Mth.lerp(partialTicks, yo, y) - camPos.y() + offsetY);
        float pz = (float) (Mth.lerp(partialTicks, zo, z) - camPos.z() + offsetZ);

        scratchMatrix.identity();
        scratchMatrix.translate(px, py, pz);
        scratchMatrix.rotate(staticRotation);

        if (hasLocal) {
            scratchMatrix.translate(localX, localY, localZ);

            if (localRotation != null) {
                scratchMatrix.rotate(localRotation);
            }
        }

        float size = quadSize * 0.5f;
        float renderAlpha = alpha / 2.0f;

        float cr = this.rCol;
        float cg = this.gCol;
        float cb = this.bCol;

        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        builder.vertex(scratchMatrix, -size, size, 0)
                .uv(0, 1).color(cr, cg, cb, renderAlpha).endVertex();
        builder.vertex(scratchMatrix, size, size, 0)
                .uv(1, 1).color(cr, cg, cb, renderAlpha).endVertex();
        builder.vertex(scratchMatrix, size, -size, 0)
                .uv(1, 0).color(cr, cg, cb, renderAlpha).endVertex();
        builder.vertex(scratchMatrix, -size, -size, 0)
                .uv(0, 0).color(cr, cg, cb, renderAlpha).endVertex();
        tesselator.end();

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    public boolean shouldCull() {
        return false;
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