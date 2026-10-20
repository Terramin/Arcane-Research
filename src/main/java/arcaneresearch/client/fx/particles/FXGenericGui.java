package arcaneresearch.client.fx.particles;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;
@OnlyIn(Dist.CLIENT)
public class FXGenericGui extends FXGeneric {
    private boolean wasInGui;
    public FXGenericGui(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        this.wasInGui = Minecraft.getInstance().screen != null;
    }
    public FXGenericGui(ClientLevel level, double x, double y, double z,
                        double vx, double vy, double vz) {
        super(level, x, y, z, vx, vy, vz);
        this.wasInGui = Minecraft.getInstance().screen != null;
    }
    @Override
    public void tick() {
        super.tick();
        if (this.wasInGui && Minecraft.getInstance().screen == null) {
            this.remove();
        }
    }
    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 cameraPos = camera.getPosition();
        float f5 = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float f6 = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float f7 = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());
        float tx1 = (float) spriteIndexX / (float) gridSize;
        float tx2 = tx1 + 1.0f / gridSize;
        float ty1 = (float) spriteIndexY / (float) gridSize;
        float ty2 = ty1 + 1.0f / gridSize;
        if (this.sprite != null) {
            tx1 = this.sprite.getU0();
            tx2 = this.sprite.getU1();
            ty1 = this.sprite.getV0();
            ty2 = this.sprite.getV1();
        }
        if (flipped) {
            float t = tx1;
            tx1 = tx2;
            tx2 = t;
        }
        float fs = Mth.clamp((this.age + partialTicks) / this.lifetime, 0.0f, 1.0f);
        float pr = startR + (endR - startR) * fs;
        float pg = startG + (endG - startG) * fs;
        float pb = startB + (endB - startB) * fs;
        int light = this.getLightColor(partialTicks);
        float ts = 0.1f * this.quadSize;
        Quaternionf quaternion;
        if (this.roll == 0.0f && !angled) {
            quaternion = camera.rotation();
        } else if (angled) {
            quaternion = new Quaternionf();
            quaternion.rotateY((float) Math.toRadians(-angleYaw + 90.0f));
            quaternion.rotateX((float) Math.toRadians(anglePitch + 90.0f));
            if (this.roll != 0.0f) {
                quaternion.rotateZ(Mth.lerp(partialTicks, this.oRoll, this.roll));
            }
        } else {
            quaternion = new Quaternionf(camera.rotation());
            quaternion.rotateZ(Mth.lerp(partialTicks, this.oRoll, this.roll));
        }
        Vector3f[] vertices = new Vector3f[]{
                new Vector3f(-ts, -ts, 0.0f),
                new Vector3f(-ts, ts, 0.0f),
                new Vector3f(ts, ts, 0.0f),
                new Vector3f(ts, -ts, 0.0f)
        };
        for (Vector3f vertex : vertices) {
            vertex.rotate(quaternion);
            vertex.add(f5, f6, f7);
        }
        buffer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z())
                .uv(tx2, ty2).color(pr, pg, pb, this.alpha)
                .uv2(light).endVertex();
        buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z())
                .uv(tx2, ty1).color(pr, pg, pb, this.alpha)
                .uv2(light).endVertex();
        buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z())
                .uv(tx1, ty1).color(pr, pg, pb, this.alpha)
                .uv2(light).endVertex();
        buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z())
                .uv(tx1, ty2).color(pr, pg, pb, this.alpha)
                .uv2(light).endVertex();
    }
    @Override
    public ParticleRenderType getRenderType() {
        return renderTypeForLayer(layer);
    }
}
