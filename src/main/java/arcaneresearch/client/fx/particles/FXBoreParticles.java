package arcaneresearch.client.fx.particles;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import org.joml.Quaternionf;
import net.minecraft.resources.Identifier;


public class FXBoreParticles extends ArcaneresearchParticle {

    private final BlockState blockState;
    private final ItemStack itemStack;
    private int side;
    private Entity target;
    private double targetX;
    private double targetY;
    private double targetZ;

    public FXBoreParticles(ClientLevel level, double x, double y, double z,
                           double tx, double ty, double tz,
                           BlockState state, int side) {
        super(level, x, y, z);

        this.blockState = state;
        this.itemStack = null;
        this.side = side;

        this.setSprite(pickBlockSprite(state));
        
        
        if (this.sprite == null) {
            this.remove();
            return;
        }

        this.rCol = 0.6f;
        this.gCol = 0.6f;
        this.bCol = 0.6f;
        this.quadSize = this.random.nextFloat() * 0.3f + 0.4f;

        this.targetX = tx;
        this.targetY = ty;
        this.targetZ = tz;

        double dx = tx - x;
        double dy = ty - y;
        double dz = tz - z;
        int base = (int)(Mth.sqrt((float)(dx * dx + dy * dy + dz * dz)) * 10.0f);
        if (base < 1) base = 1;
        this.lifetime = base / 2 + this.random.nextInt(base);

        float f3 = 0.01f;
        this.xd = this.random.nextGaussian() * f3;
        this.yd = this.random.nextGaussian() * f3;
        this.zd = this.random.nextGaussian() * f3;
        this.gravity = 0.01f;
    }

    public FXBoreParticles(ClientLevel level, double x, double y, double z,
                           double tx, double ty, double tz,
                           double sx, double sy, double sz,
                           ItemStack item) {
        super(level, x, y, z, sx, sy, sz);

        this.blockState = null;
        this.itemStack = item;

        this.setSprite(pickItemSprite(item, level, this.random));

        this.rCol = 0.6f;
        this.gCol = 0.6f;
        this.bCol = 0.6f;
        this.quadSize = this.random.nextFloat() * 0.3f + 0.4f;

        this.targetX = tx;
        this.targetY = ty;
        this.targetZ = tz;

        double dx = tx - x;
        double dy = ty - y;
        double dz = tz - z;
        int base = (int)(Mth.sqrt((float)(dx * dx + dy * dy + dz * dz)) * 10.0f);
        if (base < 1) base = 1;
        this.lifetime = base / 2 + this.random.nextInt(base);

        this.xd = sx + this.random.nextGaussian() * 0.01f;
        this.yd = sy + this.random.nextGaussian() * 0.01f;
        this.zd = sz + this.random.nextGaussian() * 0.01f;
        this.gravity = 0.01f;

        Entity renderEntity = Minecraft.getInstance().getCameraEntity();
        int visibleDistance = Minecraft.getInstance().options.graphicsPreset().get().ordinal() >= 1 ? 64 : 32;
        if (renderEntity != null && renderEntity.distanceToSqr(x, y, z) > visibleDistance * visibleDistance) {
            this.lifetime = 0;
        }
    }

    public FXBoreParticles setTarget(Entity target) {
        this.target = target;
        return this;
    }

    public FXBoreParticles getObjectColor(BlockPos pos) {

        if (this.blockState == null || this.level.getBlockState(pos) != this.blockState) {
            if (this.itemStack != null) {
                try {
                    
                    
                    applyItemTintFallback(this.itemStack);
                } catch (Exception ignored) {}
            }
            return this;
        }
        if (this.blockState.getBlock() == Blocks.GRASS_BLOCK && this.side != 1) {
            return this;
        }
        try {
            
            
            net.minecraft.client.color.block.BlockTintSource tint =
                    Minecraft.getInstance().getBlockColors().getTintSource(this.blockState, 0);
            if (tint != null) {
                int color = tint.colorAsTerrainParticle(this.blockState, this.level, pos);
                this.rCol *= (color >> 16 & 0xFF) / 255.0f;
                this.gCol *= (color >> 8 & 0xFF) / 255.0f;
                this.bCol *= (color & 0xFF) / 255.0f;
            }
        } catch (Exception ignored) {}
        return this;
    }

    private void applyItemTintFallback(ItemStack stack) {
        
        
        net.minecraft.world.item.component.DyedItemColor dyed =
                stack.get(net.minecraft.core.component.DataComponents.DYED_COLOR);
        if (dyed != null) {
            int color = dyed.rgb();
            this.rCol *= (color >> 16 & 0xFF) / 255.0f;
            this.gCol *= (color >> 8 & 0xFF) / 255.0f;
            this.bCol *= (color & 0xFF) / 255.0f;
        }
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.target != null) {
            this.targetX = this.target.getX();
            this.targetY = this.target.getY() + this.target.getEyeHeight();
            this.targetZ = this.target.getZ();
        }

        if (this.age++ >= this.lifetime ||
            (Mth.floor(this.x) == Mth.floor(this.targetX) &&
             Mth.floor(this.y) == Mth.floor(this.targetY) &&
             Mth.floor(this.z) == Mth.floor(this.targetZ))) {
            this.remove();
            return;
        }

        this.move(this.xd, this.yd, this.zd);

        this.xd *= 0.985;
        this.yd *= 0.95;
        this.zd *= 0.985;

        double dx = this.targetX - this.x;
        double dy = this.targetY - this.y;
        double dz = this.targetZ - this.z;
        double dist = Mth.sqrt((float)(dx * dx + dy * dy + dz * dz));
        double clamp = Math.min(0.25, dist / 15.0);

        if (dist < 2.0) {
            this.quadSize *= 0.9f;
        }

        dx /= dist;
        dy /= dist;
        dz /= dist;

        this.xd += dx * clamp;
        this.yd += dy * clamp;
        this.zd += dz * clamp;

        this.xd = Mth.clamp(this.xd, -clamp, clamp);
        this.yd = Mth.clamp(this.yd, -clamp, clamp);
        this.zd = Mth.clamp(this.zd, -clamp, clamp);

        this.xd += this.random.nextGaussian() * 0.005;
        this.yd += this.random.nextGaussian() * 0.005;
        this.zd += this.random.nextGaussian() * 0.005;
    }



    
    
    private static net.minecraft.client.renderer.texture.TextureAtlasSprite pickBlockSprite(
            net.minecraft.world.level.block.state.BlockState state) {
        try {
            return net.minecraft.client.Minecraft.getInstance().getModelManager()
                    .getBlockStateModelSet().getParticleMaterial(state).sprite();
        } catch (Exception e) {
            return null; 
        }
    }

    
    
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
            return null; 
        }
    }

    
    
    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float partialTicks) {
        Vec3 cameraPos = camera.position();
        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());
        Quaternionf quaternion = camera.rotation();
        state.add(getLayer(), x, y, z,
                quaternion.x(), quaternion.y(), quaternion.z(), quaternion.w(), 0.1f * this.quadSize,
                this.getU1(), this.getU0(), this.getV0(), this.getV1(),
                ARGB.colorFromFloat(1.0f, this.rCol, this.gCol, this.bCol),
                this.getLightCoords(partialTicks));
    }

    @Override
    public net.minecraft.client.particle.ParticleRenderType getGroup() {
        return net.minecraft.client.particle.ParticleRenderType.SINGLE_QUADS;
    }

    
    
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

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return this.sprite != null ? SingleQuadParticle.Layer.bySprite(this.sprite) : TCSprites.translucent();
    }
}