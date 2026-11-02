package arcaneresearch.client.fx.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class FXGenericGui extends FXGeneric {
    private static final float SIZE_FACTOR = 0.1f;

    private final Vector3f guiScratch = new Vector3f();
    private final Quaternionf guiQuat = new Quaternionf();

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
    public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTicks) {
        if (this.lifetime <= 0) return;

        Vec3 cameraPos = camera.getPosition();

        float px = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float py = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float pz = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        float tx1;
        float tx2;
        float ty1;
        float ty2;

        TextureAtlasSprite sprite = this.sprite;

        tx1 = sprite.getU0();
        tx2 = sprite.getU1();
        ty1 = sprite.getV0();
        ty2 = sprite.getV1();

        if (this.flipped) {
            float t = tx1;
            tx1 = tx2;
            tx2 = t;
        }

        float fs = Mth.clamp((this.age + partialTicks) / this.lifetime, 0.0f, 1.0f);

        float pr = Mth.lerp(fs, this.startR, this.endR);
        float pg = Mth.lerp(fs, this.startG, this.endG);
        float pb = Mth.lerp(fs, this.startB, this.endB);

        float ca = this.alpha;
        float ts = SIZE_FACTOR * this.quadSize;

        int light = this.getLightColor(partialTicks);

        Quaternionf quaternion;

        if (this.roll == 0.0f && !this.angled) {
            quaternion = camera.rotation();
        } else if (this.angled) {
            quaternion = guiQuat.identity();
            quaternion.rotateY((-this.angleYaw + 90.0f) * Mth.DEG_TO_RAD);
            quaternion.rotateX((this.anglePitch + 90.0f) * Mth.DEG_TO_RAD);

            if (this.roll != 0.0f) {
                quaternion.rotateZ(Mth.lerp(partialTicks, this.oRoll, this.roll));
            }
        } else {
            quaternion = guiQuat.set(camera.rotation());
            quaternion.rotateZ(Mth.lerp(partialTicks, this.oRoll, this.roll));
        }

        Vector3f v = guiScratch;

        v.set(-ts, -ts, 0.0f).rotate(quaternion).add(px, py, pz);
        buffer.vertex(v.x(), v.y(), v.z()).uv(tx2, ty2).color(pr, pg, pb, ca).uv2(light).endVertex();

        v.set(-ts, ts, 0.0f).rotate(quaternion).add(px, py, pz);
        buffer.vertex(v.x(), v.y(), v.z()).uv(tx2, ty1).color(pr, pg, pb, ca).uv2(light).endVertex();

        v.set(ts, ts, 0.0f).rotate(quaternion).add(px, py, pz);
        buffer.vertex(v.x(), v.y(), v.z()).uv(tx1, ty1).color(pr, pg, pb, ca).uv2(light).endVertex();

        v.set(ts, -ts, 0.0f).rotate(quaternion).add(px, py, pz);
        buffer.vertex(v.x(), v.y(), v.z()).uv(tx1, ty2).color(pr, pg, pb, ca).uv2(light).endVertex();
    }
}