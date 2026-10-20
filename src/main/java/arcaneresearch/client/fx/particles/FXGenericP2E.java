package arcaneresearch.client.fx.particles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
@OnlyIn(Dist.CLIENT)
public class FXGenericP2E extends FXGeneric {
    private Entity target;
    public FXGenericP2E(ClientLevel level, double x, double y, double z, Entity target) {
        super(level, x, y, z, 0, 0, 0);
        this.setSize(0.1f, 0.1f);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.target = target;
        double dx = target.getX() - x;
        double dy = target.getY() - y;
        double dz = target.getZ() - z;
        int base = (int)(Mth.sqrt((float)(dx * dx + dy * dy + dz * dz)) * 5.0f);
        if (base < 1) base = 1;
        this.lifetime = base;
        float f3 = 0.01f;
        this.xd = (float)random.nextGaussian() * f3;
        this.yd = (float)random.nextGaussian() * f3;
        this.zd = (float)random.nextGaussian() * f3;
        this.gravity = 0.2f;
    }
    @Override
    public void tick() {
        super.tick();
        if (target == null || !target.isAlive()) {
            this.remove();
            return;
        }
        double dx = target.getX() - x;
        double dy = target.getY() + target.getBbHeight() / 2.0 - y;
        double dz = target.getZ() - z;
        double d13 = 0.3;
        double dist = Mth.sqrt((float)(dx * dx + dy * dy + dz * dz));
        if (dist < 4.0) {
            quadSize *= 0.9f;
            d13 = 0.6;
        }
        if (dist < 0.25) {
            this.remove();
            return;
        }
        dx /= dist;
        dy /= dist;
        dz /= dist;
        xd += dx * d13;
        yd += dy * d13;
        zd += dz * d13;
        xd = Mth.clamp((float)xd, -0.35f, 0.35f);
        yd = Mth.clamp((float)yd, -0.35f, 0.35f);
        zd = Mth.clamp((float)zd, -0.35f, 0.35f);
    }
}
