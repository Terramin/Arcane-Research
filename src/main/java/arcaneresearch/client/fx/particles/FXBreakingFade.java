package arcaneresearch.client.fx.particles;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.util.Mth;
import arcaneresearch.client.fx.particles.TCSprites;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import org.joml.Quaternionf;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.resources.Identifier;

public class FXBreakingFade extends ArcaneresearchParticle {

    private final ItemStack itemStack;

    public FXBreakingFade(ClientLevel level, double x, double y, double z, Item item) {
        this(level, x, y, z, item, 0);
    }

    public FXBreakingFade(ClientLevel level, double x, double y, double z, Item item, int meta) {
        this(level, x, y, z, 0, 0, 0, item, meta);
    }

    public FXBreakingFade(ClientLevel level, double x, double y, double z,
                          double vx, double vy, double vz, Item item, int meta) {
        super(level, x, y, z, vx, vy, vz);

        this.itemStack = new ItemStack(item);

        // NEOFORGE_PORT: ItemRenderer.getModel().getParticleIcon() removed; see pickItemSprite.
        this.setSprite(pickItemSprite(this.itemStack, level, this.random));

        this.gravity = 1.0f;
        this.quadSize = 0.1f;
        this.lifetime = 20;
        this.alpha = 1.0f;

        this.xd = vx + (this.random.nextFloat() - 0.5f) * 0.2f;
        this.yd = vy + this.random.nextFloat() * 0.2f;
        this.zd = vz + (this.random.nextFloat() - 0.5f) * 0.2f;
    }

    public FXBreakingFade setParticleMaxAge(int age) {
        this.lifetime = age;
        return this;
    }

    public FXBreakingFade setParticleGravity(float g) {
        this.gravity = g;
        return this;
    }

    public FXBreakingFade setSpeed(double vx, double vy, double vz) {
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        return this;
    }

    public void boom() {
        float f = (float)(Math.random() + Math.random() + 1.0) * 0.15f;
        float len = Mth.sqrt((float)(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd));
        this.xd = this.xd / len * f * 0.964f;
        this.yd = this.yd / len * f * 0.964f + 0.1f;
        this.zd = this.zd / len * f * 0.964f;
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

        this.yd -= 0.04 * this.gravity;

        this.move(this.xd, this.yd, this.zd);

        this.xd *= 0.98;
        this.yd *= 0.98;
        this.zd *= 0.98;

        if (this.onGround) {
            this.xd *= 0.7;
            this.zd *= 0.7;
        }
    }



    // NEOFORGE_PORT (26.3): Forge getModel(item).getParticleIcon() -> the item model's
    // particle material (vanilla BreakingItemParticle path); also right for block items.
    private static net.minecraft.client.renderer.texture.TextureAtlasSprite pickItemSprite(
            net.minecraft.world.item.ItemStack item, ClientLevel level,
            net.minecraft.util.RandomSource random) {
        try {
            net.minecraft.client.renderer.item.ItemStackRenderState state =
                    new net.minecraft.client.renderer.item.ItemStackRenderState();
            Minecraft.getInstance().getItemModelResolver().updateForTopItem(state, item,
                    net.minecraft.world.item.ItemDisplayContext.GROUND, level, null, 0);
            net.minecraft.client.resources.model.sprite.Material.Baked material =
                    state.pickParticleMaterial(random);
            return material != null ? material.sprite() : null;
        } catch (Exception e) {
            return null; // guarded by getU0..V1/getLayer above
        }
    }

    // NEOFORGE_PORT: same math as the old render(); fade baked per frame.
    @Override
    public void extract(net.minecraft.client.renderer.state.level.QuadParticleRenderState state,
            net.minecraft.client.Camera camera, float partialTicks) {
        float fade = 1.0f - (float) this.age / (float) this.lifetime;

        net.minecraft.world.phys.Vec3 cameraPos = camera.position();
        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        org.joml.Quaternionf quaternion = camera.rotation();
        float size = this.quadSize;

        state.add(getLayer(), x, y, z,
                quaternion.x(), quaternion.y(), quaternion.z(), quaternion.w(), size,
                this.getU0(), this.getU1(), this.getV0(), this.getV1(),
                net.minecraft.util.ARGB.color((int) ((this.alpha * fade) * 255.0F), (int) ((this.rCol) * 255.0F), (int) ((this.gCol) * 255.0F), (int) ((this.bCol) * 255.0F)),
                this.getLightCoords(partialTicks));
    }

    @Override
    public net.minecraft.client.particle.ParticleRenderType getGroup() {
        return net.minecraft.client.particle.ParticleRenderType.SINGLE_QUADS;
    }

    // NEOFORGE_PORT: sprite fetch can fail pre-reload; guard so a missing sprite
    // falls back to the sheet instead of NPEing (practically unreachable in-game).
    @Override
    protected float getU0() {
        return this.sprite != null ? this.sprite.getU0() : 0.0f;
    }

    @Override
    protected float getU1() {
        return this.sprite != null ? this.sprite.getU1() : 1.0f;
    }

    @Override
    protected float getV0() {
        return this.sprite != null ? this.sprite.getV0() : 0.0f;
    }

    @Override
    protected float getV1() {
        return this.sprite != null ? this.sprite.getV1() : 1.0f;
    }

    // NEOFORGE_PORT (26.3): Forge TERRAIN_SHEET was alpha-blended (the 0.5 alpha and
    // the fade show); bySprite would pick the opaque layer for opaque item sprites.
    @Override
    public SingleQuadParticle.Layer getLayer() {
        if (this.sprite == null) {
            return TCSprites.translucent();
        }
        Identifier atlas = this.sprite.atlasLocation();
        if (atlas.equals(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS)) {
            return SingleQuadParticle.Layer.TRANSLUCENT_TERRAIN;
        }
        if (atlas.equals(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_ITEMS)) {
            return SingleQuadParticle.Layer.TRANSLUCENT_ITEMS;
        }
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }
}